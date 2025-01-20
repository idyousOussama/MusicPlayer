package com.example.jamplayer.Moduls

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "songs")
class SongTable (  @PrimaryKey
                   var id  : String ,
                   var title : String
                   , var isHide : Boolean) {
}