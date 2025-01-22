package com.example.jamplayer.AppDatabase.Daos.SongsDaos

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.MusicFile

@Dao
interface MusicFileDao {
@Insert
fun insertNewMusicFile(musicFile: MusicFile)
@Insert
fun insertMusicFileList(musicFileList : ArrayList<MusicFile>)
@Query("Select * from musicfile")
fun getAllMusics() :List<MusicFile>

    @Query("SELECT * FROM musicfile")
    fun getSongsByTite () :List<MusicFile>
@Query("update musicfile set isShort = :isShort where id = :id")
fun upDateSongsById(id : String , isShort: Boolean)

@Query("Delete from musicfile where id = :id")
fun removeSongById(id : String)

    @Query("SELECT * FROM musicfile where isShort")
    fun getHiddenSongs() : List<MusicFile>
    @Query("upDate musicfile set isChecked = :isChecked where id = :id")
    fun upDateCheckedSongById(id:String , isChecked : Boolean)

    @Query("select * from musicfile where title = :title  || album = :title")
    fun getSongsByTitle (title :String) : List<MusicFile>
    @Query("SELECT COUNT(*) FROM musicfile where isShort")
    fun getHiddenSongsNum (): Int

    @Query("SELECT * FROM musicfile where isShort = 0 ")
fun getUnHideSongs() : List<MusicFile>

@Query("SELECT * FROM musicfile where id = :id ")
fun getSongsById(id : String) : MusicFile

@Query("upDate MusicFile set isLiked = :isLiked where id = :id")
fun upDateLikedSong(isLiked : Boolean , id :String)
    @Query("UPDATE musicfile SET title = :title, artist = :artist, musicImage = :image WHERE id = :id")
fun upDateCurrentSongById(id : String , title : String , artist :String , image : Bitmap?)

@Query("delete  from musicfile where id = :id")
fun deleteCurrentSongById(id : String)

@Query("update MusicFile set isShort = 1 where id = :id")
fun unHideCurrentSong(id : String)
@Query("Select * from musicfile where songUri = :uri")
fun getSingByUri(uri  :String) : MusicFile
@Query("Select COUNT(*) from musicfile where title = :title")
fun chechSongExistsBySongId(title : String):Int
@Query("select * from musicfile where album = :albumName")
fun getAlbumSongsByAlbumName(albumName : String) : List<MusicFile>
@Query("update MusicFile set playedNumber = playedNumber + 1   where id = :id")
fun upDateNumPlayedSongById(id:String)




}
