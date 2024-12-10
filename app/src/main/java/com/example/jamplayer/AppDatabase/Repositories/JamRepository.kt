package com.example.jamplayer.AppDatabase.Repositories

import android.app.Application
import android.provider.MediaStore.Audio.Albums
import androidx.lifecycle.LiveData
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.AppDatabase.JamRoom
import com.example.jamplayer.Listeners.JamRoomListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamRepository(application: Application) {

    val jamDB = JamRoom.getDatabase(application)
    val fileMusicDao = jamDB.musicFileDao()
    val albumDao = jamDB.AlbumDao()

 fun insertMusicFile (vararg newMFile : MusicFile)   {
     executor.execute {
         fileMusicDao.insertNewMusicFile(*newMFile)
     }


 }
    fun insertMusicFileList (mFileList : ArrayList<MusicFile>){
        executor.execute {
         fileMusicDao.insertMusicFileList(mFileList)
     }
 }
   fun insertNewAlbum(newAlbum : Album){
       executor.execute {
          albumDao.insertAlbum(newAlbum)
      }
  }
 fun insertAlbumsList (albumList : ArrayList<Album>){
     executor.execute {
        albumDao.insertAlbumList(albumList)
         }
     }
     suspend fun getAllMusic () : List<MusicFile> {
         return     withContext(Dispatchers.IO){
          fileMusicDao.getAllMusics()
        }


    }
    suspend fun getAllAlbums() : LiveData<List<Album>>{
        return withContext(Dispatchers.IO){
            albumDao.getAllMusics()
        }
    }
fun getSongListByTitle(title : String , listener : JamRoomListener){
    executor.execute {
        listener.getSongsByTitleListener(fileMusicDao.getSongsByTite())
    }
    }

    fun upDateSongsById(id : Int , isShort: Boolean) {
        executor.execute {
            fileMusicDao.upDateSongsById(id,isShort)
        }
    }

    suspend fun getHiddenSongs() : List<MusicFile>{
        return  withContext(Dispatchers.IO){
             fileMusicDao.getHiddenSongs()
        }
    }
    suspend fun upDateCheckedSongById(id:Int , isChecked : Boolean){
        withContext(Dispatchers.IO){
            fileMusicDao.upDateCheckedSongById(id , isChecked)
        }
    }
    suspend fun getSongsByTitle (title :String) : List<MusicFile>{
        return withContext(Dispatchers.IO){
            fileMusicDao.getSongsByTitle(title)
        }
    }

    suspend fun getHiddenSongsNum() : Int{
      return withContext(Dispatchers.IO){
          fileMusicDao.getHiddenSongsNum()
      }
    }
suspend  fun getunhiddenSongs() : List<MusicFile>{
    return withContext(Dispatchers.IO){
        fileMusicDao.getUnHideSongs()
    }

}

    suspend fun upDateLikedSong(isLiked : Boolean , id :Int) {
        withContext(Dispatchers.IO){
            fileMusicDao.upDateLikedSong(isLiked,id)
        }
    }

}

