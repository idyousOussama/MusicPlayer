 package com.example.jamplayer.Activities


import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.disklrucache.DiskLruCache.Value
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.firebaseDB
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.user
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Adapters.MainViewPagerAdapter
import com.example.jamplayer.AppDatabase.ViewModels.JamViewModel

import com.example.jamplayer.Fragments.AlbumsFragments
import com.example.jamplayer.Fragments.PlayListsFragment
import com.example.jamplayer.Fragments.SongsFragment
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

 class MainActivity : AppCompatActivity() {
     private lateinit var binding: ActivityMainBinding
     private val ioScope = CoroutineScope(Dispatchers.IO + Job())
     private val musicImageCache = mutableMapOf<String, Bitmap?>()
private var lastAddedSongName : String? = null
     private var changedAlbumName : String? = null

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)
         userIsActive = true
         jamViewModel = ViewModelProvider(this).get(JamViewModel::class.java)
         initBtns()
         initViewPager()
         checkNewMessage()
         observeMediaStoreAlbumsChanges(baseContext.contentResolver, baseContext)
         observeMediaStoreSongsChanges(baseContext.contentResolver, baseContext)
     }
     private fun checkNewMessage() {
          val supportNewMessagesRef = firebaseDB.getReference("NewMessages").child( user!!.userId + "support0766501026")
         supportNewMessagesRef.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(p0: DataSnapshot) {
                 if(p0.exists()){
                     binding.mainRedDot.visibility = View.VISIBLE
                 }else{
                     binding.mainRedDot.visibility = View.GONE

                 }
             }
             override fun onCancelled(p0: DatabaseError) {
                 TODO("Not yet implemented")
             }
         })


     }


     private fun initBtns() {
         binding.mainSearchBtn.setOnClickListener {
             val intent = Intent(this, SearchActivity::class.java)
             startActivity(intent)
         }
         binding.mainSettingBtn.setOnClickListener {
             val intent = Intent(this, SettingsActivity::class.java)
             startActivity(intent)
         }
     }
     private fun initViewPager() {
         val   viewPagerAdapter = MainViewPagerAdapter(supportFragmentManager)
         viewPagerAdapter.addFragment(SongsFragment(),"Songs")
         viewPagerAdapter.addFragment(AlbumsFragments(),"Albums")
         viewPagerAdapter.addFragment(PlayListsFragment(),"Playlists")
         binding.viewPager.adapter = viewPagerAdapter
         binding.tablayout.setupWithViewPager(binding.viewPager)
     }
     override fun onDestroy() {
         super.onDestroy()
         userIsActive = false
     }
     private fun observeMediaStoreSongsChanges(contentResolver: ContentResolver, context: Context) {
         val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
             override fun onChange(selfChange: Boolean, uri: Uri?) {
                 super.onChange(selfChange, uri)
                 val projection = arrayOf(
                     MediaStore.Audio.Media.TITLE,
                     MediaStore.Audio.Media._ID,
                     MediaStore.Audio.Media.DURATION,
                     MediaStore.Audio.Media.DATA,
                     MediaStore.Audio.Media.ARTIST,
                     MediaStore.Audio.Media.ALBUM
                 )
                 ioScope.launch {
                     uri?.let {
                         contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                             if (cursor.moveToFirst()) {
                                 val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown Title"
                                 // Check if the song already exists in the database
                                 if ( lastAddedSongName != title || lastAddedSongName == null  ) {
                                     val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                                     val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                                     val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                                     val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: changedAlbumName
                                     if (duration > 60000) {
                                        try{
                                            jamViewModel.insertMusicFile(
                                                MusicFile(0, data, getMusicImage(data), title, artist, album!!, duration.toString(), false, false, false, title, 0,false)
                                            )
                                        }catch(ex : Exception){
                                           Toast.makeText(baseContext ,"There is  a problem in save a new Added Song ", Toast.LENGTH_SHORT)
                                        }
                                     } else {
                         try {
                             jamViewModel.insertMusicFile(
                                 MusicFile(0, data, getMusicImage(data), title, artist, album!!, duration.toString(), true, false, false, title, 0,false)
                             )
                         }catch(ex : Exception){
                             Toast.makeText(baseContext ,"There is a problem in save a new Song ", Toast.LENGTH_SHORT)
                         }
                                     }
                                     lastAddedSongName = title
                                     withContext(Dispatchers.Main) {
                                         Toast.makeText(context, "We have a new song!", Toast.LENGTH_SHORT).show()
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }

         contentResolver.registerContentObserver(
             MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
             true,
             mediaObserver
         )
     }
     private fun observeMediaStoreAlbumsChanges(contentResolver: ContentResolver, context: Context) {
         val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
             override fun onChange(selfChange: Boolean, uri: Uri?) {
                 super.onChange(selfChange, uri)
                 val projection = arrayOf(
                     MediaStore.Audio.Albums.ALBUM,
                     MediaStore.Audio.Albums.ARTIST,
                     MediaStore.Audio.Albums._ID,
                     MediaStore.Audio.Albums.ALBUM_ART,
                     MediaStore.Audio.Albums.NUMBER_OF_SONGS
                 )
                 ioScope.launch {
                     uri?.let {
                         CoroutineScope(Dispatchers.Main).launch {

                         val cursor = contentResolver.query(it, projection, null, null, null)
                         cursor?.let { c ->
                             if (c.moveToFirst()) {
                                 changedAlbumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)) ?: "Unknown Album"

                                     if(jamViewModel.checkIfAlbumIsExistsByName(changedAlbumName!!) == 0 ){
                                         val artistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)) ?: "Unknown Artist"
                                         val albumArt = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
                                         val songCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
                                      jamViewModel.insertNewAlbum(Album(0,changedAlbumName,artistName,albumArt,songCount.toString(),uri.toString()))
                                         c.close()
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         contentResolver.registerContentObserver(
             MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
             true,
             mediaObserver
         )
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

 }