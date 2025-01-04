package com.example.jamplayer.Moduls

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    var userId : String
,var username : String){
    constructor() : this("","")
}
