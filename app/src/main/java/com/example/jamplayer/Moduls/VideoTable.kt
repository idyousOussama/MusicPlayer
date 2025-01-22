package com.example.jamplayer.Moduls

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
data class VideoTable(
    var id  : String ,
    var title : String,
    var filepath: String
    , var isHide : Boolean)
