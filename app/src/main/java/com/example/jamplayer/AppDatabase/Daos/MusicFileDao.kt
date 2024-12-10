package com.example.jamplayer.AppDatabase.Daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.MusicFile

@Dao
interface MusicFileDao {
@Insert
fun insertNewMusicFile(vararg musicFile: MusicFile)
@Insert
fun insertMusicFileList(musicFileList : ArrayList<MusicFile>)
@Query("Select * from musicfile")
fun getAllMusics() :List<MusicFile>

    @Query("SELECT * FROM musicfile")
    fun getSongsByTite () :List<MusicFile>
@Query("update musicfile set isShort = :isShort where id = :id")
fun upDateSongsById(id : Int , isShort: Boolean)

    @Query("SELECT * FROM musicfile where isShort")
    fun getHiddenSongs() : List<MusicFile>
    @Query("upDate musicfile set isChecked = :isChecked where id = :id")
    fun upDateCheckedSongById(id:Int , isChecked : Boolean)

    @Query("select * from musicfile where title = :title  || album = :title")
    fun getSongsByTitle (title :String) : List<MusicFile>
    @Query("SELECT COUNT(*) FROM musicfile where isShort")
    fun getHiddenSongsNum (): Int

    @Query("SELECT * FROM musicfile where isShort = 0 ")
fun getUnHideSongs() : List<MusicFile>

@Query("upDate MusicFile set isLiked = :isLiked where id = :id")
fun upDateLikedSong(isLiked : Boolean , id :Int)
}
