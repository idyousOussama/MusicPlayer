package com.example.jamplayer.AppDatabase.Daos.SongsDaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.User

@Dao
interface UserDao {
@Insert
fun insertUser(user : User)

@Query("Select * from user")
fun getUser() : User

}