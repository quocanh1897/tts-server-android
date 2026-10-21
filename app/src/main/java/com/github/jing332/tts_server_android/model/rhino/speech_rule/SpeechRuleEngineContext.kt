package com.github.jing332.tts_server_android.model.rhino.speech_rule

import android.content.Context
import com.github.jing332.script.annotation.ScriptInterface
import com.github.jing332.script.simple.ext.JsExtensions
import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.Segment

class SpeechRuleEngineContext(
    override val context: Context, override val engineId: String = ""
) : JsExtensions(context, engineId) {

    /**
     * Create a segmenter. This is a factory method.
     * Params:
     * algorithm - segmentation algorithm. Chinese or English names are both accepted. Options:
     * Viterbi (viterbi): best balance of speed and quality
     * Double-array trie (dat): very fast dictionary segmentation, tens of millions of characters per second
     * Conditional random field (crf): higher accuracy for segmentation, part-of-speech tagging, and named-entity recognition; suited to demanding NLP tasks
     * Perceptron (perceptron): segmentation, part-of-speech tagging, and named-entity recognition; supports online learning
     * N-shortest path (nshort): slightly better named-entity recognition, at the cost of speed
     * Returns:
     * A segmenter
     */
    @JvmOverloads
    @ScriptInterface
    fun newSegment(algorithm: String = "viterbi"): Segment = HanLP.newSegment(algorithm)!!
}