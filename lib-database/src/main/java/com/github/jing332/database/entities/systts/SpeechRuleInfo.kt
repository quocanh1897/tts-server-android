package com.github.jing332.database.entities.systts

import android.os.Parcelable
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.github.jing332.database.constants.SpeechTarget
import com.github.jing332.database.entities.MapConverters
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@TypeConverters(MapConverters::class)
data class SpeechRuleInfo(
    var target: Int = SpeechTarget.ALL,

    var isStandby: Boolean = false,
    var specifiedStandbyId: Long? = null,

    var tag: String = "",
    var tagRuleId: String = "",

    // Label shown at the top-right of the list
    var tagName: String = "",

    // Data stored for the tag
    // Example: key=role, value=Alice
    var tagData: Map<String, String> = mutableMapOf(),

    // Identifies a TTS config. After the script runs, tag and id are returned so the app can find what to speak
    var configId: Long = 0L
) : Parcelable {
    val mutableTagData: MutableMap<String, String>
        get() = tagData as MutableMap<String, String>


    /**
     * Whether the tags are the same
     * @return true if they match
     */
    fun isTagSame(rule: SpeechRuleInfo): Boolean {
        return tag == rule.tag && tagRuleId == rule.tagRuleId
    }

    fun resetTag() {
        tag = ""
        tagRuleId = ""
        tagName = ""
        mutableTagData.clear()
    }

    fun isTagDataEmpty(): Boolean = tagData.filterValues { it.isNotEmpty() }.isEmpty()

}