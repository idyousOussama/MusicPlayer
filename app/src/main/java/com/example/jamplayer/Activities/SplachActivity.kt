package com.example.jamplayer.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.allAudios
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel

import com.example.jamplayer.Adapters.MainViewPagerAdapter
import com.example.jamplayer.AppDatabase.ViewModels.JamViewModel
import com.example.jamplayer.Fragments.AlbumsFragments
import com.example.jamplayer.Fragments.SongsFragment
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile

import com.example.jamplayer.R
import com.example.jamplayer.databinding.ActivityMainBinding
import com.example.jamplayer.databinding.ActivitySplachBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class SplachActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplachBinding
    private val musicImageCache = mutableMapOf<String, Bitmap?>()
    private lateinit var fetchedList : ArrayList<MusicFile>
    object ItemsManagers{
        lateinit var jamViewModel: JamViewModel
        var allAudios: ArrayList<MusicFile> = ArrayList()
        var albumList: ArrayList<Album> = ArrayList()
        val executor: ExecutorService = Executors.newFixedThreadPool(100)
    }

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
binding = ActivitySplachBinding.inflate(layoutInflater)
       setContentView(binding.root)
       jamViewModel = ViewModelProvider(this).get(JamViewModel::class.java)
       getPermission()
   }
    private fun etchAndInsertSongsList() {
        CoroutineScope(Dispatchers.Main).launch{
            binding.splachProgress.visibility = View.VISIBLE
            fetchedList = jamViewModel.getunhiddenSongs() as ArrayList
            if(fetchedList.isNotEmpty() ){
                allAudios = fetchedList
                getAlbums()
                Toast.makeText(baseContext,albumList.size.toString() , Toast.LENGTH_SHORT).show()
             var intent = Intent(baseContext,MainActivity::class.java)
                startActivity(intent)
                finish()
            }else
            {
                val songsList = getAllPhoneAudios()
                jamViewModel.insertMusicFileList(songsList)
                CoroutineScope(Dispatchers.Main).launch{
                    if(songsList.isNotEmpty() ){
                        fetchedList = jamViewModel.getunhiddenSongs() as ArrayList
                        allAudios = fetchedList
                        getAlbums()
                        Toast.makeText(baseContext,albumList.size.toString() , Toast.LENGTH_SHORT).show()
                        val intent = Intent(baseContext,MainActivity::class.java)
                        startActivity(intent)
                        finish()}}

            }
        }











    }


    private fun getAllPhoneAudios(): ArrayList<MusicFile> {
        val musicFilesList = ArrayList<MusicFile>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM
        )

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, "${MediaStore.Audio.Media.DATE_ADDED} DESC")
        cursor?.use {
            while (it.moveToNext()) {
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown Title"
                val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: continue
                val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                val album = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album"
                if (duration > 60000) {
                    val musicFile = MusicFile(0, data, getMusicImage(data), title, artist, album, duration.toString() ,false,false,false)
                    musicFilesList.add(musicFile)
                }else {
                    val musicFile = MusicFile(0, data, getMusicImage(data), title, artist, album, duration.toString() ,true , false,false)
                    musicFilesList.add(musicFile)
                }
            }
        }
        return musicFilesList
    }

    private fun getMusicImage(uri: String): Bitmap? {
        if (musicImageCache.containsKey(uri)) return musicImageCache[uri]
        val retriever = MediaMetadataRetriever()
        return try {
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

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            etchAndInsertSongsList() // Fetch songs if permission is already granted
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            etchAndInsertSongsList()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getAlbums() {
        val uri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )

        // Query the MediaStore
        val cursor: Cursor? = baseContext.contentResolver.query(
            uri,
            projection,
            null,
            null,
            // Sort Albums Alphabically
            MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC "
        )
        cursor?.use { c ->
            while (c.moveToNext()) {
                val title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)) ?: "Unknown Album"
                val artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)) ?: "Unknown Artist"
                val albumArt = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
                val songCount = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))

                // Filter songs belonging to the album
                val filteredSongs = allAudios.filter { it.album == title }
                // Create album object only if it has songs
                if (filteredSongs.isNotEmpty()) {
                    val album = Album(
                        name = title,
                        artist = artist,
                        albumArt = albumArt,
                        songsNumber = songCount.toString(),
                        albumSongsList = ArrayList(filteredSongs)
                    )
                    albumList.add(album)
                }
            }
        }

    }




}