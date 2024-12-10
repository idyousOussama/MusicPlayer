package com.example.jamplayer.AppDatabase.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.jamplayer.AppDatabase.Repositories.JamRepository
import com.example.jamplayer.Listeners.JamRoomListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamViewModel(application: Application) : AndroidViewModel(application) {
    val jamRepo = JamRepository(application)

fun insertMusicFile (vararg newMFile : MusicFile)   {
        jamRepo.insertMusicFile(*newMFile)
           }
 fun insertMusicFileList (mFileList : ArrayList<MusicFile>){
        jamRepo.insertMusicFileList(mFileList)
    }
fun insertNewAlbum(newAlbum : Album){
        jamRepo.insertNewAlbum(newAlbum)
        }
fun insertAlbumsList (albumList : ArrayList<Album>){
            jamRepo.insertAlbumsList(albumList)
    }
suspend fun getAllMusic () :List<MusicFile> {
          return jamRepo.getAllMusic()
    }
    suspend fun getAllAlbums() : LiveData<List<Album>> {
        return jamRepo.getAllAlbums()
    }
    fun getSongListByTitle(title : String , listener : JamRoomListener){
        jamRepo.getSongListByTitle(title, listener)
    }
    fun upDateSongsById(id : Int , isShort: Boolean) {
            jamRepo.upDateSongsById(id,isShort)
    }
    suspend fun getHiddenSongs() : List<MusicFile>{
        return jamRepo.getHiddenSongs()
    }
    suspend fun upDateCheckedSongById(id:Int, isChecked : Boolean){
            jamRepo.upDateCheckedSongById(id , isChecked)
    }
    suspend fun getSongsByTitle (title :String) : List<MusicFile>{
        return  jamRepo.getSongsByTitle(title)
    }
    suspend fun getHiddenSongsNum() : Int{
return jamRepo.getHiddenSongsNum()

    }
    suspend  fun getunhiddenSongs() : List<MusicFile>{
        return jamRepo.getunhiddenSongs()
    }
    suspend fun upDateLikedSong(isLiked : Boolean , id :Int) {
        jamRepo.upDateLikedSong(isLiked,id)
    }
}