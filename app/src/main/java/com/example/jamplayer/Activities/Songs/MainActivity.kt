 package com.example.jamplayer.Activities.Songs

 import com.example.jamplayer.AppDatabase.ViewModels.SongViewModel.JamViewModel
 import com.example.jamplayer.Fragments.VideosFragment
 import android.content.ContentResolver
 import android.content.Context
 import android.content.Intent
 import android.content.res.Configuration
 import android.database.ContentObserver
 import android.graphics.Bitmap
 import android.graphics.BitmapFactory
 import android.media.MediaMetadataRetriever
 import android.net.Uri
 import android.os.Bundle
 import android.os.Handler
 import android.os.Looper
 import android.provider.MediaStore
 import android.util.Log
 import android.view.View
 import android.widget.Toast
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.app.ActivityCompat
 import androidx.core.content.ContextCompat
 import androidx.core.net.toUri
 import androidx.lifecycle.ViewModelProvider
 import com.bumptech.glide.disklrucache.DiskLruCache.Value
 import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers
 import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.firebaseDB
 import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
 import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.user
 import com.example.jamplayer.Adapters.AudiosAdapter
 import com.example.jamplayer.Adapters.MainViewPagerAdapter

 import com.example.jamplayer.Fragments.AlbumsFragments
 import com.example.jamplayer.Fragments.PlayListsFragment
 import com.example.jamplayer.Fragments.SongsFragment
 import com.example.jamplayer.Moduls.Album
 import com.example.jamplayer.Moduls.MusicFile
 import com.example.jamplayer.Moduls.VideoAlbum

 import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
 import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videosAlbums
 import com.example.jamplayer.databinding.ActivityMainBinding
 import com.google.firebase.database.DataSnapshot
 import com.google.firebase.database.DatabaseError
 import com.google.firebase.database.ValueEventListener


 class MainActivity : AppCompatActivity() {
     private lateinit var binding: ActivityMainBinding
     private val musicImageCache = mutableMapOf<String, Bitmap?>()
companion object {
var  mainScreenIsRotate  = false
}

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)


         userIsActive = true
         jamViewModel = ViewModelProvider(this).get(JamViewModel::class.java)
         initBtns()

         initViewPager()
         checkNewMessage()
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
         viewPagerAdapter.addFragment(VideosFragment(),"Videos")
         viewPagerAdapter.addFragment(SongsFragment(),"Songs")
         viewPagerAdapter.addFragment(AlbumsFragments(),"Albums")
         viewPagerAdapter.addFragment(PlayListsFragment(),"Playlists")
         binding.viewPager.adapter = viewPagerAdapter
         binding.tablayout.setupWithViewPager(binding.viewPager)
         val videoIsDeleted = intent.getBooleanExtra("VideoIsDeleted",false)
         if(!videoIsDeleted){
             binding.viewPager.currentItem = 1
         }else{
             binding.viewPager.currentItem = 0
         }
     }
     override fun onDestroy() {
         super.onDestroy()
         userIsActive = false
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