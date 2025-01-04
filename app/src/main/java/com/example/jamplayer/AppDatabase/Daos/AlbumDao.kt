package com.example.jamplayer.AppDatabase.Daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile

@Dao
interface AlbumDao {
    @Insert
    fun insertAlbum(vararg album: Album)
    @Insert
    fun insertAlbumList(albumList : ArrayList<Album>)
    @Query("Select * from albums")
    fun getAllMusics() : List<Album>

    @Query("SELECT * FROM albums WHERE name LIKE '%' || :title || '%'")
    fun getSongsByTitle (title : String) :List<Album>
 @Query("Select COUNT(*)  from albums where name = :albumName")
 fun checkIfAlbumIsExistsByName (albumName :String):Int

}