package com.example.jamplayer.AppDatabase.Repositories.SongRepository

import android.app.Application
import android.graphics.Bitmap
import androidx.room.Query
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.AppDatabase.JamRoom
import com.example.jamplayer.Listeners.JamRoomListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Moduls.Settings
import com.example.jamplayer.Moduls.User
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamRepository(application: Application) {

    val jamDB = JamRoom.getDatabase(application)
    val fileMusicDao = jamDB.musicFileDao()
    val albumDao = jamDB.AlbumDao()
    val settingsDao = jamDB.SettingsDao()
     val userDao = jamDB.userDao()
     val playlistDao = jamDB.PlaylistDao()
     val videoDao = jamDB.videoDao()








    suspend fun insertUser(user : User){
        withContext(Dispatchers.IO){
            userDao.insertUser(user)
        }
    }
 suspend   fun insertSettings ( settings : Settings)   {
withContext(Dispatchers.IO) {
    settingsDao.insertSettings(settings)

}
    }

    suspend  fun insertNewPalyList(playList : PlayList){
        withContext(Dispatchers.IO){
            playlistDao.insertNewPalyList(playList)
        }
    }
    suspend   fun insertMultPlayLists(playLists : ArrayList<PlayList>) {
        withContext(Dispatchers.IO){
            playlistDao.insertMultPlayLists(playLists)
        }
    }
 suspend fun insertMusicFile (newMFile : MusicFile)   {
     withContext(Dispatchers.IO) {
         fileMusicDao.insertNewMusicFile(newMFile)
     }
 }
   suspend fun insertMusicFileList (mFileList : ArrayList<MusicFile>){
        withContext(Dispatchers.IO) {
            fileMusicDao.insertMusicFileList(mFileList)
     }
 }
   suspend fun insertNewAlbum(newAlbum : Album){
       withContext(Dispatchers.IO) {          albumDao.insertAlbum(newAlbum)
      }
  }
 suspend fun insertAlbumsList (albumList : ArrayList<Album>){
     withContext(Dispatchers.IO) {
         albumDao.insertAlbumList(albumList)
         }
     }
   suspend fun setPlayingTime(playingTime : Int){
        withContext(Dispatchers.IO) {
            settingsDao.setPlayingTime(playingTime)
       }
    }



    suspend fun getAllMusic () : List<MusicFile> {
         return     withContext(Dispatchers.IO){
          fileMusicDao.getAllMusics()
        }


    }
    suspend fun getAllAlbums() : List<Album>{
        return withContext(Dispatchers.IO){
            albumDao.getAllMusics()
        }
    }
fun getSongListByTitle(title : String , listener : JamRoomListener){
    executor.execute {
        listener.getSongsByTitleListener(fileMusicDao.getSongsByTite())
    }
    }

    suspend fun getHiddenSongs() : List<MusicFile>{
        return  withContext(Dispatchers.IO){
             fileMusicDao.getHiddenSongs()
        }
    }
    suspend fun getSettings() : Settings{
        return  withContext(Dispatchers.IO){
             settingsDao.getSettings()
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

    suspend  fun getSingByUri(uri  :String) : MusicFile {
        return withContext(Dispatchers.IO){
            fileMusicDao.getSingByUri(uri)
        }
    }

    suspend  fun getAllPlayLists() : List<PlayList> {
        return withContext(Dispatchers.IO){
            playlistDao.getAllPlayLists()
        }
    }
    suspend fun getAlbumSongsByAlbumName(albumName : String) : List<MusicFile> {
        return withContext(Dispatchers.IO) {
            fileMusicDao.getAlbumSongsByAlbumName(albumName)
        }
    }
   suspend fun getPlaylistByTitle(title : String) : PlayList {
        return withContext(Dispatchers.IO){
            playlistDao.getPlaylistByTitle(title)
        }
    }

    suspend fun chechSongExistsBySongId(title : String):Int{
        return withContext(Dispatchers.IO){
            fileMusicDao.chechSongExistsBySongId(title)
        }
    }

    suspend fun getUser() : User {
        return withContext(Dispatchers.IO){
        userDao.getUser()
        }
    }

    suspend fun checkIfAlbumIsExistsByName (albumName :String):Int {
        return withContext(Dispatchers.IO){
            albumDao.checkIfAlbumIsExistsByName(albumName)
        }
    }
    suspend fun upDateCurrentSongById(id : Int , title : String , artist :String , image : Bitmap?) {
      withContext(Dispatchers.IO){
          fileMusicDao.upDateCurrentSongById(id,title,artist,image)
      }
    }
    suspend fun upDateCheckedSongById(id:Int , isChecked : Boolean){
        withContext(Dispatchers.IO){
            fileMusicDao.upDateCheckedSongById(id , isChecked)
        }
    }
    fun upDateSongsById(id : Int , isShort: Boolean) {
        executor.execute {
            fileMusicDao.upDateSongsById(id,isShort)
        }
    }
    suspend  fun upDateNumPlayedSongById(id:Int) {
        withContext(Dispatchers.IO){
         fileMusicDao.upDateNumPlayedSongById(id)
        }
    }
    fun upDateFNSSetting(isEnable : Boolean){
        executor.execute {
            settingsDao.upDateFNSSetting(isEnable)
        }
    }
    fun upDateSecNSettings(isEnable : Boolean){
        executor.execute {
            settingsDao.upDateSecNSettings(isEnable)
        }
    }
    fun upDateItemSize (size : String)   {
        executor.execute {
            settingsDao.upDateItemsType(size)
        }
    }


   suspend fun deleteCurrentSongById(id : Int){
        withContext(Dispatchers.IO){
            fileMusicDao.deleteCurrentSongById(id)
        }

   }

    fun unHideCurrentSong(id : Int){
        executor.execute {
            fileMusicDao.unHideCurrentSong(id)
        }
    }
    suspend fun removeSongById(id : Int) {
        withContext(Dispatchers.IO){
            fileMusicDao.removeSongById(id)
        }
    }
  suspend  fun upDatePlaylistSongsList( playlistId : Int , upDatedPlayList:ArrayList<Int>){
        withContext(Dispatchers.IO){
        playlistDao.upDatePlaylistSongsList(playlistId,upDatedPlayList)
        }

    }


suspend fun insertNewVideo(newVideo : VideoTable){
        withContext(Dispatchers.IO){
            videoDao.insertNewVideo(newVideo)
        }
    }
   suspend fun getAllVideos() : List<VideoTable> {
        return withContext(Dispatchers.IO){
            videoDao.getAllVideos()
        }
    }
   suspend fun getVideoById(id : String) : VideoTable {
        return  withContext(Dispatchers.IO){
            videoDao.getVideoById(id)
        }
    }
    suspend fun HideAndUnhieVideo(isHide : Boolean , id : String) {
        withContext(Dispatchers.IO){
            videoDao.HideAndUnhieVideo(isHide,id)
        }
    }

    suspend fun setVideoPlayingTime (playingTime : Int) {
        withContext(Dispatchers.IO) {
            settingsDao.setVideoPlayingTime(playingTime)
        }
    }
    suspend fun upDateVideoTitleById(title : String , id : String){
        withContext(Dispatchers.IO) {
            videoDao.upDateVideoTitleById(title,id)
        }
    }
 suspend fun getHiddenVideos() : List<VideoTable> {
     return withContext(Dispatchers.IO) {
     videoDao.getHiddenVideos()

     }
 }
    suspend fun upDateVideosHideById( id : String , isHide : Boolean ) {
        withContext(Dispatchers.IO) {
            videoDao.upDateVideosHideById(id,isHide)
        }

        }


}

