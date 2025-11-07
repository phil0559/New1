package com.example.new1.data

import androidx.room.TypeConverter
import org.json.JSONArray
import java.util.ArrayList

object RoomContentTypeConverters {
    @TypeConverter
    @JvmStatic
    fun fromStringList(values: List<String>?): String {
        if (values.isNullOrEmpty()) {
            return "[]"
        }
        val array = JSONArray()
        values.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) {
            return emptyList()
        }
        val array = JSONArray(value)
        val result = ArrayList<String>(array.length())
        for (i in 0 until array.length()) {
            result.add(array.optString(i))
        }
        return result
    }
}
