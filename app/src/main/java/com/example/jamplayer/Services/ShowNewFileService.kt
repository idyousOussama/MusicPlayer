package com.example.jamplayer.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings

import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoTable
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.currentVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File


import android.os.Build
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext


class ShowNewFileService : Service() {
    private val musicImageCache = mutableMapOf<String, Bitmap?>()
    private val mainScope = CoroutineScope(Dispatchers.Main  )
    private val newVideosList = ArrayList<Video>()
    private var messedVideoList = ArrayList<Video>()
    private val newSongList = ArrayList<MusicFile>()
    private var messedSongsList = ArrayList<MusicFile>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ShowNewFileAction.SCANNINGVIDEOS.name -> scanningVideos()
            ShowNewFileAction.SCANNINGSONGS.name -> scanningSongs()

        }
        return START_STICKY
    }

    private fun scanningSongs() {
foundNewSongs()

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun scanningVideos (){
        foundNewVideos()
    }


    private fun getSongSacannMessage(): String? {
        return when {
            newSongList.isNotEmpty() && messedSongsList.isNotEmpty() -> {
                "${newSongList.size} song found and ${messedSongsList.size} songs removed."
            }
            messedSongsList.isNotEmpty()   && newSongList.isEmpty() -> {
                "${messedSongsList.size} songs removed."
            }
            messedSongsList.isEmpty() && newSongList.isNotEmpty()-> {
                "${newSongList.size} songs found."
            }
            else -> null
        }
    }

    private fun removeMessedVideos() {
        mainScope.launch {
            val allVideos = jamViewModel.getAllVideos()
            messedVideoList = allVideos.filter {checkSongIsNull(it.filePath, baseContext) } as ArrayList<Video>
            messedVideoList.forEach { video ->
                jamViewModel.deleteVideoById(video.id)
            }
            if(newVideosList.isEmpty() && messedVideoList.isEmpty()) {
            }else  {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        baseContext,
                        getVideoScannMessage(),
                        Toast.LENGTH_SHORT
                    ).show()
                    newVideosList.clear()
                    messedVideoList.clear()
                }
            }
        }

    }
    private fun removeMessedSongs() {
        mainScope.launch {
            val allSongs = jamViewModel.getAllMusic()
             messedSongsList = allSongs.filter {checkSongIsNull(it.path, baseContext) } as ArrayList<MusicFile>
            messedSongsList.forEach { song ->
                jamViewModel.removeSongById(song.id)
            }
            if(newSongList.isEmpty() &&  messedSongsList.isEmpty()) {

            }else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        baseContext,
                        getSongSacannMessage(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                newSongList.clear()
                messedSongsList.clear()
            }
        }
    }
enum class ShowNewFileAction() {
                               SCANNINGVIDEOS,SCANNINGSONGS

}

    private fun foundNewVideos() {
        val videoUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.DATE_ADDED
        )

        CoroutineScope(Dispatchers.Main).launch {
            val lastScanTime = getLastScanTime()

            val selection = "${MediaStore.Video.Media.DATE_ADDED} > ?"
            val selectionArgs = arrayOf(lastScanTime.toString())

            contentResolver.query(videoUri, projection, selection, selectionArgs, MediaStore.Video.Media.DATE_ADDED + " DESC")?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val dataUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val album =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                            ?: "Unknown Album"
                    // Check if video exists in the database
                    val existingVideo = jamViewModel.getVideoById(id.toString())
                    if (existingVideo == null && duration >= 6000) {
                        val newVideo = Video(id.toString(), title, dataUri.toString(), cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)), duration, album, path, false, 0, false, false)
                        jamViewModel.insertNewVideo(newVideo)
                        newVideosList.add(newVideo)
                        videosList.add(newVideo)
                    }else if (existingVideo == null && duration < 6000){
                        val newVideo = Video(id.toString(), title, dataUri.toString(), cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)), duration, album, path, false, 0, true, false)
                        jamViewModel.insertNewVideo(newVideo)
                        newVideosList.add(newVideo)
                        videosList.add(newVideo)

                    }
                }
            }
      removeMessedVideos()
        }
    }

    private fun foundNewSongs() {
        val audioUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM
        )

        CoroutineScope(Dispatchers.Main).launch {
            val lastScanTime = getLastScanTime()

            val selection = "${MediaStore.Audio.Media.DATE_ADDED} > ?"
            val selectionArgs = arrayOf(lastScanTime.toString())

            contentResolver.query(audioUri, projection, selection, selectionArgs, MediaStore.Audio.Media.DATE_ADDED + " DESC")?.use { cursor ->
                while (cursor.moveToNext()) {
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: continue
                    val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album"
                    val dataUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    var savedSong: MusicFile? = jamViewModel.getSongsById(id.toString())
                    if (savedSong == null && duration >= 60000) {
                        savedSong = MusicFile(id.toString(), data, getMusicImage(data), title, artist, album, duration.toString(), false, false, false, id.toString(), 0, false, dataUri.toString())
                        jamViewModel.insertMusicFile(savedSong)
                        newSongList.add(savedSong)
                        unHideSong.add(savedSong)
                    }else  if(savedSong == null && duration < 60000){
                        savedSong = MusicFile(id.toString(), data, getMusicImage(data), title, artist, album, duration.toString(), true, false, false, id.toString(), 0, false, dataUri.toString())
                        jamViewModel.insertMusicFile(savedSong)
                        newSongList.add(savedSong)
                    }

                }
            }
        removeMessedSongs()

        }
    }

    private fun getLastScanTime(): Long {
        // Retrieve the last scan time from SharedPreferences or database (default to 0 if not found)
        return 0L
    }



    private fun getVideoScannMessage(): String? {
        return when {
            newVideosList.isNotEmpty() && messedVideoList.isNotEmpty() -> {
                "${newVideosList.size} new videos found and ${messedVideoList.size} videos messed."
            }
            newVideosList.isNotEmpty() && messedVideoList.isEmpty() -> {
                "${newVideosList.size} new videos found."
            }
            messedVideoList.isNotEmpty()  && newVideosList.isEmpty()-> {
                "${messedVideoList.size} videos messed."
            }
            else -> null
        }
    }

    private fun getMusicImage(uri: String): Bitmap? {
        return musicImageCache[uri] ?: run {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(uri)
                val imageData = retriever.embeddedPicture
                val bitmap = imageData?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                musicImageCache[uri] = bitmap
                bitmap
            } catch (e: Exception) {
                null
            } finally {
                retriever.release()
            }
        }
    }

    private fun checkSongIsNull(filePath:String, context: Context): Boolean {
        return try {
            val file = File(filePath)
            !file.exists()
        } catch (e: Exception) {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}



