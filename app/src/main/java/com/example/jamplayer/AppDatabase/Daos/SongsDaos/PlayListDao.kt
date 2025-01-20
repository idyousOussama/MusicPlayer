package com.example.jamplayer.AppDatabase.Daos.SongsDaos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.PlayList

@Dao
interface PlayListDao {
@Insert
fun insertNewPalyList(playList : PlayList)
 @Insert
 fun insertMultPlayLists(playLists : ArrayList<PlayList>)
 @Query("select * from PlayList")
 fun getAllPlayLists() : List<PlayList>

 @Query("UPDATE PlayList SET playlistSong = :songs WHERE id = :id")
 suspend fun upDatePlaylistSongsList(
  id: Int,
  songs: ArrayList<Int>
 )
 @Query("Select * from PlayList where title = :title")
fun getPlaylistByTitle(title : String) : PlayList
}