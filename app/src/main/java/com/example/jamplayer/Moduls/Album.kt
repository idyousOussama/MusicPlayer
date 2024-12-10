package com.example.jamplayer.Moduls

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true)
    var id : Int= 0,
    var name: String?,
                   var artist: String?,
                   val albumArt: String?,
var songsNumber: String?,
    var albumSongsList :ArrayList<MusicFile>
) : Serializable
