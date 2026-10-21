package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app

@IntDef(
    SpeechTarget.ALL,
//    SpeechTarget.ASIDE,
//    SpeechTarget.DIALOGUE,
    SpeechTarget.BGM,
    SpeechTarget.TAG
)
@Retention(AnnotationRetention.SOURCE)
annotation class SpeechTarget {
    companion object {
        const val ALL = 0

        @Deprecated("Custom tags already exist")
        const val ASIDE = 1 // narration

        @Deprecated("Custom tags already exist")
        const val DIALOGUE = 2 // dialogue

        const val BGM = 3 // background music
        const val TAG = 4 // custom tag

        fun toText(@SpeechTarget target: Int): String {
            return when (target) {
                BGM -> {
                    app.getString(R.string.bgm)
                }

                else -> ""
            }
        }
    }
}