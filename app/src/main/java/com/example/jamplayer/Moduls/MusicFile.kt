package com.example.jamplayer.Moduls

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable
@Entity(tableName = "MusicFile")
data class MusicFile(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var path: String, var musicImage: Bitmap?, var title: String, var artist: String, var album:String,
    var duration: String , var isShort : Boolean , var isChecked : Boolean,var isLiked : Boolean,var songUri : String ,var playedNumber : Int ,var isPlayedRecently : Boolean) : Serializable
