package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ActionItem
import com.example.data.model.DiscussionTopic
import com.example.data.model.SpeakerStat
import com.example.data.model.TranscriptSegment
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // List<TranscriptSegment>
    @TypeConverter
    fun fromTranscriptSegments(list: List<TranscriptSegment>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, TranscriptSegment::class.java)
        val adapter = moshi.adapter<List<TranscriptSegment>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toTranscriptSegments(json: String?): List<TranscriptSegment> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, TranscriptSegment::class.java)
        val adapter = moshi.adapter<List<TranscriptSegment>>(type)
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // List<ActionItem>
    @TypeConverter
    fun fromActionItems(list: List<ActionItem>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, ActionItem::class.java)
        val adapter = moshi.adapter<List<ActionItem>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toActionItems(json: String?): List<ActionItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ActionItem::class.java)
        val adapter = moshi.adapter<List<ActionItem>>(type)
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // List<DiscussionTopic>
    @TypeConverter
    fun fromDiscussionTopics(list: List<DiscussionTopic>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, DiscussionTopic::class.java)
        val adapter = moshi.adapter<List<DiscussionTopic>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toDiscussionTopics(json: String?): List<DiscussionTopic> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, DiscussionTopic::class.java)
        val adapter = moshi.adapter<List<DiscussionTopic>>(type)
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // List<SpeakerStat>
    @TypeConverter
    fun fromSpeakerStats(list: List<SpeakerStat>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, SpeakerStat::class.java)
        val adapter = moshi.adapter<List<SpeakerStat>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toSpeakerStats(json: String?): List<SpeakerStat> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, SpeakerStat::class.java)
        val adapter = moshi.adapter<List<SpeakerStat>>(type)
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // List<String>
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
