package com.github.jing332.common.audio

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import com.github.jing332.common.audio.AudioDecoderException.Companion.ERROR_CODE_NO_AUDIO_TRACK
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.coroutines.coroutineContext


class AudioDecoder {
    companion object {
        const val TAG = "AudioDecode"

        suspend fun InputStream.readPcmChunk(
            bufferSize: Int = 4096,
            chunkSize: Int = 2048,
            onRead: suspend (ByteArray) -> Unit,
        ) {
            var bufferFilledCount = 0
            val buffer = ByteArray(bufferSize)

            while (true) {
                coroutineContext.ensureActive()
                val readLen =
                    this.read(buffer, bufferFilledCount, chunkSize - bufferFilledCount)
                if (readLen == -1) {
                    if (bufferFilledCount > 0) {
                        val chunkData = buffer.copyOfRange(0, bufferFilledCount)
                        onRead.invoke(chunkData)
                    }
                    break
                }
                if (readLen == 0) {
                    delay(100)
                    continue
                }

                bufferFilledCount += readLen
                if (bufferFilledCount >= chunkSize) {
                    val chunkData = buffer.copyOfRange(0, chunkSize)

                    onRead.invoke(chunkData)
                    bufferFilledCount = 0
                }
            }
        }

        /**
         * Get the audio sample rate
         */
        private fun getFormats(srcData: ByteArray): List<MediaFormat> {
            kotlin.runCatching {
                val mediaExtractor = MediaExtractor()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mediaExtractor.setDataSource(ByteArrayMediaDataSource(srcData))
                else
                    mediaExtractor.setDataSource(
                        "data:" + "" + ";base64," + srcData.toByteString().base64()
                    )

                val formats = mutableListOf<MediaFormat>()
                for (i in 0 until mediaExtractor.trackCount) {
                    formats.add(mediaExtractor.getTrackFormat(i))
                }
                return formats
            }
            return emptyList()
        }

        /**
         * Get the sample rate and MIME type
         */
        fun getSampleRateAndMime(audio: ByteArray): Pair<Int, String> {
            val formats = getFormats(audio)

            var sampleRate = 0
            var mime = ""
            if (formats.isNotEmpty()) {
                sampleRate = formats[0].getInteger(MediaFormat.KEY_SAMPLE_RATE)
                mime = formats[0].getString(MediaFormat.KEY_MIME) ?: ""
            }

            return Pair(sampleRate, mime)
        }

    }

    private val currentMime: String = ""
    private var mediaCodec: MediaCodec? = null
    private var oldMime: String? = null

    private fun getMediaCodec(mime: String, mediaFormat: MediaFormat): MediaCodec {
        if (mediaCodec == null || mime != oldMime) {
            if (null != mediaCodec) {
                mediaCodec!!.release()
//                GcManager.doGC()
            }
            try {
                mediaCodec = MediaCodec.createDecoderByType(mime)
                oldMime = mime
            } catch (ioException: IOException) {
                // Device cannot create the decoder; rethrow
                ioException.printStackTrace()
                throw RuntimeException(ioException)
            }
        }
        mediaCodec!!.reset()
        mediaCodec!!.configure(mediaFormat, null, null, 0)
        return mediaCodec as MediaCodec
    }

    suspend fun doDecode(
        srcData: ByteArray,
        sampleRate: Int,
        onRead: suspend (pcmData: ByteArray) -> Unit,
    ) {
        val mediaExtractor = MediaExtractor()
        try {
            // RIFF WAVEfmt: drop the file header
            if (srcData.size > 15 && srcData.copyOfRange(0, 15).decodeToString()
                    .endsWith("WAVEfmt")
            ) {
                val data = srcData.copyOfRange(44, srcData.size)
                onRead.invoke(data)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                mediaExtractor.setDataSource(ByteArrayMediaDataSource(srcData))
            else
                mediaExtractor.setDataSource(
                    "data:" + currentMime + ";base64," + srcData.toByteString().base64()
                )

            decodeInternal(mediaExtractor, sampleRate) {
                onRead.invoke(it)
            }
        } catch (e: Exception) {
            mediaCodec?.reset()
            throw AudioDecoderException(cause = e, message = "Audio decode failed")
        } finally {
            mediaExtractor.release()
        }
    }


    /**
     * @param sampleRate Opus audio must set the sample rate
     */
    suspend fun doDecode(
        ins: InputStream,
        sampleRate: Int = 0,
        timeoutUs: Long = 5000L,
        onRead: suspend (pcmData: ByteArray) -> Unit,
    ) {
        val mediaExtractor = MediaExtractor()
        try {
            val bytes = ByteArray(15)
            val len = ins.read(bytes)

            if (len == -1) throw AudioDecoderException(message = "Failed to read the first 15 bytes of the audio stream: len == -1")
            if (bytes.decodeToString().endsWith("WAVEfmt")) {
                ins.buffered().use { buffered ->
                    buffered.skip(29)
                    buffered.readPcmChunk { pcmData ->
                        onRead.invoke(pcmData)
                    }

                    buffered.close()
                }
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                mediaExtractor.setDataSource(InputStreamMediaDataSource(ins))
            else
                throw AudioDecoderException(
                    AudioDecoderException.ERROR_CODE_NOT_SUPPORT_A5,
                    "Audio stream decoder requires Android 5 or newer"
                )

            decodeInternal(mediaExtractor, sampleRate, timeoutUs) {
                onRead.invoke(it)
            }
        } catch (e: Exception) {
            mediaCodec?.reset()
            throw AudioDecoderException(cause = e, message = "Audio decode failed")
        } finally {
            mediaExtractor.release()
        }
    }

    private suspend fun decodeInternal(
        mediaExtractor: MediaExtractor,
        sampleRate: Int,
        timeoutUs: Long = 5000L,
        onRead: suspend (pcmData: ByteArray) -> Unit,
    ) {
        val trackFormat = mediaExtractor.selectAudioTrack()
        val mime = trackFormat.mime

        // Opus audio must set this to decode correctly
        if ("audio/opus" == mime) trackFormat.compatOpus(sampleRate)

        // Create the decoder
        val mediaCodec = getMediaCodec(mime, trackFormat)
        mediaCodec.start()

        val bufferInfo = BufferInfo()
        var inputBuffer: ByteBuffer?
        val startNanos = SystemClock.elapsedRealtimeNanos()
        while (coroutineContext.isActive) {
            // Get an available inputBuffer. -1 waits forever, 0 does not wait, 10*1000 is a 10 second timeout
            val inputIndex = mediaCodec.dequeueInputBuffer(timeoutUs)
            if (inputIndex < 0) break

            bufferInfo.presentationTimeUs = mediaExtractor.sampleTime

            inputBuffer = mediaCodec.getInputBuffer(inputIndex)
            if (inputBuffer != null) {
                inputBuffer.clear()
            } else
                continue

            // Number of samples read from the stream
            val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
            if (sampleSize > 0) {
                bufferInfo.size = sampleSize
                // Queue for decode
                mediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0)
                // Move to the next sample
                if (!mediaExtractor.nextSample(startNanos)) {
                    Log.d(TAG, "nextSample(): reached end of stream EOF")
                }
            } else
                break

            // Take the decoded data
            var outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            // May not finish in one call, so loop
            var outputBuffer: ByteBuffer?
            val pcmData = ByteArray(bufferInfo.size)

            while (coroutineContext.isActive && outputIndex >= 0) {
                outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
                if (outputBuffer != null) {
                    outputBuffer.get(pcmData)
                    outputBuffer.clear() // Clear after use so it can be reused
                }

                onRead.invoke(pcmData)
                mediaCodec.releaseOutputBuffer(/* index = */ outputIndex, /* render = */ false)
                outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun MediaExtractor.nextSample(startNanos: Long): Boolean {
//        while (sampleTime > SystemClock.elapsedRealtimeNanos() - startNanos) {
//            delay(100)
//        }
        return advance()
    }

    private val MediaFormat.mime: String
        get() = getString(MediaFormat.KEY_MIME) ?: ""


    private fun MediaExtractor.selectAudioTrack(): MediaFormat {
        var audioTrackIndex = -1
        var mime: String?
        var trackFormat: MediaFormat? = null
        for (i in 0 until trackCount) {
            trackFormat = getTrackFormat(i)
            mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (!TextUtils.isEmpty(mime) && mime!!.startsWith("audio")) {
                audioTrackIndex = i
                break
            }
        }
        if (audioTrackIndex == -1)
            throw AudioDecoderException(ERROR_CODE_NO_AUDIO_TRACK, "No audio track found")

        selectTrack(audioTrackIndex)
        return trackFormat!!
    }

    private fun MediaFormat.compatOpus(sampleRate: Int) {
        //Log.d(TAG, ByteString.of(trackFormat.getByteBuffer("csd-0")).hex());
        val buf = okio.Buffer()
        // Magic Signature: fixed header, 8 bytes, the string OpusHead
        buf.write("OpusHead".toByteArray(StandardCharsets.UTF_8))
        // Version: version number, 1 byte, fixed at 0x01
        buf.writeByte(1)
        // Channel Count: channel count, 1 byte, set from the stream, e.g. 0x02
        buf.writeByte(1)
        // Pre-skip: samples discarded from the decoder on playback, 2 bytes, little-endian, default 0x00,
        buf.writeShortLe(0)
        // Input Sample Rate (Hz): stream sample rate, 4 bytes, little-endian, set as needed
        buf.writeIntLe(sampleRate)
        //Output Gain: output gain, 2 bytes, little-endian, unused so default to 0x00, 0x00
        buf.writeShortLe(0)
        // Channel Mapping Family: channel mapping family, 1 byte, default 0x00
        buf.writeByte(0)
        //Channel Mapping Table: optional; ignored when Family is the default 0x00
        val csd1bytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val csd2bytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val hd: ByteString = buf.readByteString()
        val csd0: ByteBuffer = ByteBuffer.wrap(hd.toByteArray())
        setByteBuffer("csd-0", csd0)
        val csd1: ByteBuffer = ByteBuffer.wrap(csd1bytes)
        setByteBuffer("csd-1", csd1)
        val csd2: ByteBuffer = ByteBuffer.wrap(csd2bytes)
        setByteBuffer("csd-2", csd2)
    }
}