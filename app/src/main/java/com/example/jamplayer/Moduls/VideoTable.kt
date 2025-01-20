package com.example.jamplayer.Moduls

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "video")
data class VideoTable(
    @PrimaryKey
    var id  : String ,
    var title : String
    , var isHide : Boolean)
