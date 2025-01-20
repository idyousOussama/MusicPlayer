package com.example.jamplayer.AppDatabase.ViewModels.SongViewModel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.jamplayer.AppDatabase.Repositories.SongRepository.JamRepository
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

class JamViewModel(application: Application) : AndroidViewModel(application) {
    val jamRepo = JamRepository(application)

  suspend  fun insertMusicFile(newMFile: MusicFile) {
        jamRepo.insertMusicFile(newMFile)
    }

    suspend  fun insertMusicFileList(mFileList: ArrayList<MusicFile>) {
            jamRepo.insertMusicFileList(mFileList)
    }
    suspend  fun getSingByUri(uri  :String) : MusicFile {
    return jamRepo.getSingByUri(uri)
    }
    suspend  fun insertNewAlbum(newAlbum: Album) {
        jamRepo.insertNewAlbum(newAlbum)
    }

    suspend   fun insertAlbumsList(albumList: ArrayList<Album>) {
        jamRepo.insertAlbumsList(albumList)
    }

    suspend fun getAllMusic(): List<MusicFile> {
        return jamRepo.getAllMusic()
    }

    suspend fun getAllAlbums(): List<Album> {
        return jamRepo.getAllAlbums()
    }

    fun getSongListByTitle(title: String, listener: JamRoomListener) {
        jamRepo.getSongListByTitle(title, listener)
    }

    fun upDateSongsById(id: Int, isShort: Boolean) {
        jamRepo.upDateSongsById(id, isShort)
    }

    suspend fun getHiddenSongs(): List<MusicFile> {
        return jamRepo.getHiddenSongs()
    }

    suspend fun upDateCheckedSongById(id: Int, isChecked: Boolean) {
        jamRepo.upDateCheckedSongById(id, isChecked)
    }

    suspend fun getSongsByTitle(title: String): List<MusicFile> {
        return jamRepo.getSongsByTitle(title)
    }

    suspend fun getHiddenSongsNum(): Int {
        return jamRepo.getHiddenSongsNum()
    }

    suspend fun getunhiddenSongs(): List<MusicFile> {
        return jamRepo.getunhiddenSongs()
    }

    suspend fun upDateLikedSong(isLiked: Boolean, id: Int) {
        jamRepo.upDateLikedSong(isLiked, id)
    }

    suspend  fun insertSettings(settings: Settings) {
        jamRepo.insertSettings(settings)

    }

    suspend  fun setPlayingTime(playingTime: Int) {
        jamRepo.setPlayingTime(playingTime)

    }

    fun upDateItemSize(size: String) {
        jamRepo.upDateItemSize(size)

    }

    suspend fun getSettings(): Settings {
        return jamRepo.getSettings()
    }


    fun upDateFNSSetting(isEnable: Boolean) {

        jamRepo.upDateFNSSetting(isEnable)

    }

    fun upDateSecNSettings(isEnable: Boolean) {
        jamRepo.upDateSecNSettings(isEnable)
    }


    suspend fun insertUser(user: User) {
        jamRepo.insertUser(user)

    }

    suspend fun getUser(): User {
        return jamRepo.getUser()

    }

    suspend fun upDateCurrentSongById(id: Int, title: String, artist: String, image: Bitmap?) {
        jamRepo.upDateCurrentSongById(id, title, artist, image)
    }

   suspend fun deleteCurrentSongById(id: Int) {
        jamRepo.deleteCurrentSongById(id)

    }

    fun unHideCurrentSong(id: Int) {
        jamRepo.unHideCurrentSong(id)
    }

    suspend fun removeSongById(id : Int) {
            jamRepo.removeSongById(id)
    }
    suspend fun chechSongExistsBySongId(title : String):Int{
       return     jamRepo.chechSongExistsBySongId(title)
    }
    suspend fun getAlbumSongsByAlbumName(albumName : String) : List<MusicFile> {
        return  jamRepo.getAlbumSongsByAlbumName(albumName)
    }
    suspend fun checkIfAlbumIsExistsByName (albumName :String):Int {
            return jamRepo.checkIfAlbumIsExistsByName(albumName)
    }
    suspend  fun insertNewPalyList(playList : PlayList){
            jamRepo.insertNewPalyList(playList)
    }
    suspend  fun insertMultPlayLists(playLists : ArrayList<PlayList>) {
        jamRepo.insertMultPlayLists(playLists)
    }
    suspend  fun getAllPlayLists() : List<PlayList> {
        return jamRepo.getAllPlayLists()
    }
    suspend fun upDatePlaylistSongsList(playlistId : Int , upDatedPlayList:ArrayList<Int>){
jamRepo.upDatePlaylistSongsList(playlistId,upDatedPlayList)
    }
    suspend fun getPlaylistByTitle(title : String) : PlayList {
        return jamRepo.getPlaylistByTitle(title)
    }
    suspend  fun upDateNumPlayedSongById(id:Int) {
            jamRepo.upDateNumPlayedSongById(id)
    }
    suspend fun insertNewVideo(newVideo : VideoTable){
            jamRepo.insertNewVideo(newVideo)
    }
    suspend fun getAllVideos() : List<VideoTable> {
           return jamRepo.getAllVideos()
    }
    suspend fun getVideoById(id : String) : VideoTable {
           return jamRepo.getVideoById(id)
    }
    suspend fun HideAndUnhieVideo(isHide : Boolean , id : String) {
            jamRepo.HideAndUnhieVideo(isHide,id)
    }

    suspend fun setVideoPlayingTime (playingTime : Int) {
            jamRepo.setVideoPlayingTime(playingTime)
        }
    suspend fun upDateVideoTitleById(title : String , id : String){
            jamRepo.upDateVideoTitleById(title,id)
    }

    suspend fun getHiddenVideos() : List<VideoTable> {
          return jamRepo.getHiddenVideos()
    }

   suspend  fun upDateVideosHideById( id : String , isHide : Boolean ) {
            jamRepo.upDateVideosHideById(id,isHide)
    }
}