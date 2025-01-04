package com.example.jamplayer.Moduls

import android.graphics.Bitmap
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.graphics.BitmapFactory
import android.util.Base64
class Converter {
    // تحويل ArrayList إلى String (لتخزينها في قاعدة البيانات)
    @TypeConverter
     fun fromMusicFileList(value: ArrayList<MusicFile>?): String {
        return Gson().toJson(value)
    }

    // تحويل String إلى ArrayList (عند استرجاعها من قاعدة البيانات)
    @TypeConverter
    fun toMusicFileList(value: String): ArrayList<MusicFile> {
        val listType = object : TypeToken<ArrayList<MusicFile>>() {}.type
        return Gson().fromJson(value, listType)
    }


    // Convert Bitmap to Base64 String
    @TypeConverter
    fun fromBitmap(value: Bitmap?): String? {
        return if (value != null) {
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            value.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)  // You can choose other formats like JPEG
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } else {
            null
        }
    }

    // Convert Base64 String back to Bitmap
    @TypeConverter
    fun toBitmap(value: String?): Bitmap? {
        return if (!value.isNullOrEmpty()) {
            try {
                val decodedString = Base64.decode(value, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null  // Return null if something goes wrong
            }
        } else {
            null
        }
    }
    @TypeConverter
    fun fromArrayList(list: ArrayList<Int>?): String {
        return Gson().toJson(list ?: emptyList<Int>()) // Handle null by saving as an empty list
    }

    @TypeConverter
    fun toArrayList(value: String?): ArrayList<Int> {
        val listType = object : TypeToken<ArrayList<Int>>() {}.type
        return Gson().fromJson(value ?: "[]", listType) // Handle null by returning an empty list
    }
}