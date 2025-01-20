package com.example.jamplayer.Moduls

import kotlin.time.Duration

data class Video(var id : String  , var title : String , var path : String , var displayName: String ,var duration: Long,var album : String ,var filePath : String, var isSelected : Boolean , var currentMediaPlayerProgress : Int ,  var isHide : Boolean , var isChecked : Boolean)
