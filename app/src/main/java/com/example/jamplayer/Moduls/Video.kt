package com.example.jamplayer.Moduls

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Duration

@Entity(tableName = "video")
data class Video(
    @PrimaryKey()
    var id : String

    , var title : String , var path : String , var displayName: String ,var duration: Long,var album : String ,var filePath : String, var isSelected : Boolean , var currentMediaPlayerProgress : Int ,  var isHide : Boolean , var isChecked : Boolean)
