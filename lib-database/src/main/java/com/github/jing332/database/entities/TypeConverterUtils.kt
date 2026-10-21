package com.github.jing332.database.entities

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object TypeConverterUtils {
    @OptIn(ExperimentalSerializationApi::class)
    val json by lazy {
        Json {
            ignoreUnknownKeys = true // Ignore unknown keys
            explicitNulls = false // Ignore null fields
            isLenient = true // Ignore fields that do not match JSON syntax
            allowStructuredMapKeys = true
        }
    }

    inline fun <reified T> decodeFromString(s: String?): T? {
        if (s == null) return null
        return try {
            json.decodeFromString<T>(s.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T> encodeToString(value :T?): String? {
        if (value == null) return null
        return try {
            json.encodeToString<T>(value)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}