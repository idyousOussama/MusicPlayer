package com.example.jamplayer.Activities.Songs



import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.playLists
import com.example.jamplayer.AppDatabase.ViewModels.SongViewModel.JamViewModel
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Moduls.Settings
import com.example.jamplayer.Moduls.User
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivitySplachBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors

class SplachActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplachBinding
    private val musicImageCache = mutableMapOf<String, Bitmap?>()
    private lateinit var fetchedList: ArrayList<MusicFile>
    object ItemsManagers {
        val firebaseDB: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
        lateinit var jamViewModel: JamViewModel
        var albumList: ArrayList<Album> = ArrayList()
        var videosList: ArrayList<Video> = ArrayList()
        var settings: Settings? = null
        var user: User? = null
        var playLists : ArrayList<PlayList> = ArrayList()
        var unHideSong : ArrayList<MusicFile> = ArrayList()
        val executor = Executors.newFixedThreadPool(10)


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplachBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
        ItemsManagers.jamViewModel = ViewModelProvider(this).get(JamViewModel::class.java)
        checkPermissionsAndInitialize()
    }

    private fun checkPermissionsAndInitialize() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        } else {
            initializeApp()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeApp()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun initializeApp() {
        CoroutineScope(Dispatchers.Main).launch {
            ItemsManagers.user = ItemsManagers.jamViewModel.getUser() ?: createUser()
            fetchedList = ItemsManagers.jamViewModel.getunhiddenSongs() as ArrayList<MusicFile>
            ItemsManagers.albumList = ItemsManagers.jamViewModel.getAllAlbums() as ArrayList<Album>
            if (fetchedList.isNotEmpty()) {
                ItemsManagers.unHideSong = fetchedList
                playLists = jamViewModel.getAllPlayLists() as ArrayList<PlayList>
                ItemsManagers.settings = ItemsManagers.jamViewModel.getSettings()
                navigateToMain()
            } else {
                val songsList = fetchAllPhoneAudios()
                if (songsList.isNotEmpty()) {
                    showGetStartedDialog(songsList)
                } else {
                    Toast.makeText(this@SplachActivity, "No songs found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private suspend fun createUser(): User {
        val userRef = ItemsManagers.firebaseDB.getReference("Users")
        val userId = userRef.push().key ?: throw IllegalStateException("Unable to generate user ID.")
        val newUser = User(userId, "DefaultUser")
        userRef.child(userId).setValue(newUser).await()
        ItemsManagers.jamViewModel.insertUser(newUser)
        return newUser
    }

    private fun showGetStartedDialog(songsList: ArrayList<MusicFile>) {
        binding.splachProgress.visibility = View.GONE
        val dialogView = LayoutInflater.from(this).inflate(R.layout.get_started_dialog_custom, null)
        val getStartedButton = dialogView.findViewById<TextView>(R.id.get_started_Btn)

        val dialog = Dialog(this).apply {
            setContentView(dialogView)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        getStartedButton.setOnClickListener {
            getStartedButton.isEnabled = false
            getStartedButton.text = "..."
            CoroutineScope(Dispatchers.Main).launch {
                ItemsManagers.jamViewModel.insertMusicFileList(songsList)
                ItemsManagers.settings = insertDefaultSettings()
                playLists = insertDefaultPlayLists()
                ItemsManagers.unHideSong = ItemsManagers.jamViewModel.getunhiddenSongs() as ArrayList
                ItemsManagers.jamViewModel.insertAlbumsList(loadAlbums())
                ItemsManagers.albumList = ItemsManagers.jamViewModel.getAllAlbums() as ArrayList
                navigateToMain()
            }
        }
        dialog.show()
    }

    private suspend fun insertDefaultPlayLists(): ArrayList<PlayList> {
        val likedSongList :ArrayList<Int> = ArrayList()
        val mostPlayedSongList :ArrayList<Int> = ArrayList()
        val defaultPlayLists : ArrayList<PlayList> = ArrayList()
        defaultPlayLists.add( PlayList(0 ,getString(R.string.likedSongs_text),R.drawable.full_heart,likedSongList))
        defaultPlayLists.add(PlayList(0 ,getString(R.string.mostPlayedSongs_text),R.drawable.most_played_icon,mostPlayedSongList) )
        jamViewModel.insertMultPlayLists(defaultPlayLists)
        return defaultPlayLists
    }

    private suspend fun insertDefaultSettings(): Settings {
        val newSettings = Settings(1, "Light", "small", 0, 0,true, true)
        ItemsManagers.jamViewModel.insertSettings(newSettings)
        Toast.makeText(this, "Settings initialized: ${newSettings.mode}", Toast.LENGTH_SHORT).show()
        return newSettings
    }
    private fun loadAlbums(): ArrayList<Album> {
        val albumsList = ArrayList<Album>()
        val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )
        contentResolver.query(albumUri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)) ?: "Unknown"
                val artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)) ?: "Unknown Artist"
                val albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
                val songCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
                albumsList.add(Album(0, albumName, artistName, albumArt, songCount.toString(),albumUri.toString()))
            }
        }
        return albumsList
    }
    private fun fetchAllPhoneAudios(): ArrayList<MusicFile> {
        val audioList = ArrayList<MusicFile>()
        val audioUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM
        )
        contentResolver.query(audioUri, projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")?.use { cursor ->
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown Title"
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: continue
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album"

                if (duration >= 60000) {
                    audioList.add(MusicFile(0, data, getMusicImage(data), title, artist, album, duration.toString(), false, false, false, id,0,false))
                }else{
                    audioList.add(MusicFile(0, data, getMusicImage(data), title, artist, album, duration.toString(), true, false, false, id , 0,false))

                }
            }
        }
        return audioList
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
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}