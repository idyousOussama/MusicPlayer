package com.example.jamplayer.Moduls

import android.content.pm.PackageManager.ComponentEnabledSetting
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings (
    @PrimaryKey()
var id :Int = 0,
    var mode : String ,
    var itemType : String ,
    var playingTime : Long,
    var videoPlayingTime : Long,
   var FSNIsEnable : Boolean ,
    var SecNIsEnable : Boolean,

    )