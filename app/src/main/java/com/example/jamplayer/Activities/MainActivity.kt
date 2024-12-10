 package com.example.jamplayer.Activities


import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Adapters.MainViewPagerAdapter

import com.example.jamplayer.Fragments.AlbumsFragments
import com.example.jamplayer.Fragments.SongsFragment
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

 class MainActivity : AppCompatActivity() {
     private lateinit var binding: ActivityMainBinding
     private val musicImageCache = mutableMapOf<String, Bitmap?>()


     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)
         initBtns()
         initViewPager()
     checknewSongs()
     }

     private fun checknewSongs() {
CoroutineScope(Dispatchers.Default).launch {
    val phoneAudios  = getAllPhoneAudios()
    val dbSongs = jamViewModel.getAllMusic()

    if(phoneAudios.size > dbSongs.size){
        runOnUiThread {
            Toast.makeText(baseContext,"there is a more Songs fo adding this",Toast.LENGTH_SHORT).show()
        }
    }else if(phoneAudios.size < dbSongs.size){
        runOnUiThread {

            Toast.makeText(baseContext,"there is a more Songs fo removings this",Toast.LENGTH_SHORT).show()
        }

    }else if (phoneAudios.size == dbSongs.size) {
        runOnUiThread {
            Toast.makeText(baseContext,"the Audios is Equals",Toast.LENGTH_SHORT).show()
        }


    }
}

     }

     private fun initBtns() {
         binding.mainSearchBtn.setOnClickListener {
             val intent = Intent(this, SearchActivity::class.java)
             startActivity(intent)
         }
     }
     private fun initViewPager() {
         val   viewPagerAdapter = MainViewPagerAdapter(supportFragmentManager)
         viewPagerAdapter.addFragment(SongsFragment(),"Songs")
         viewPagerAdapter.addFragment(AlbumsFragments(),"Albums")
         binding.viewPager.adapter = viewPagerAdapter
         binding.tablayout.setupWithViewPager(binding.viewPager)
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


 }