package com.example.jamplayer.Moduls

import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PlayList")
data class PlayList (
    @PrimaryKey(autoGenerate = true)
    var id : Int,
    val title : String ,
    var icon : Int?
,var playlistSong : ArrayList<String>
)