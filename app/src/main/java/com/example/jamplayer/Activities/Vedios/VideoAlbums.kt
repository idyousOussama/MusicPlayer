package com.example.jamplayer.Activities.Vedios

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity.showVideoAlbumManager.showAlbumsVideosList
import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity.showVideoAlbumManager.videosAlbumTitle
import com.example.jamplayer.Adapters.VedioAdapters.VideoAlbumsAdapter
import com.example.jamplayer.Listeners.VideoListeners.VideosAlbumItemListener
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoAlbum
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videosAlbums
import com.example.jamplayer.databinding.ActivityVideoAlbumsBinding

class VideoAlbums : AppCompatActivity() {
    lateinit var binding : ActivityVideoAlbumsBinding
    val videoAlbumsAdapter = VideoAlbumsAdapter(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding = ActivityVideoAlbumsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videosAlbums =  getVideoAlbums()
        videoAlbumsAdapter.setVideoAlbumsList(videosAlbums)
        binding.videoAlbumsList.apply {
             if(settings!!.itemType == "small") {
                 layoutManager = LinearLayoutManager(baseContext)
             }else {
                 if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                     GridLayoutManager(context, 1)

                 }else {
                     GridLayoutManager(context, 2)

                 }
             }
            setHasFixedSize(true)
            adapter= videoAlbumsAdapter
        }
        setAlbumListener ()

    }

    private fun setAlbumListener() {
        videoAlbumsAdapter.setAlbumItemListener(object : VideosAlbumItemListener{
            override fun onVideosAlbumItemClicked(title: String, VideosList: ArrayList<Video>) {
                showAlbumsVideosList = VideosList
                videosAlbumTitle = title
                navigateToNewActivity(ShowVideoAlbumsVideosActivity::class.java)
            }
        })
    }

    private fun navigateToNewActivity(newActivity: Class<*>) {
val newActivityIntent = Intent(this@VideoAlbums , newActivity)
        startActivity(newActivityIntent)
    }

    private fun getVideoAlbums(): ArrayList<VideoAlbum> {
        val albums = mutableMapOf<String, VideoAlbum>()
        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        )

        val sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        ) ?: run {
            Log.e("VideoAlbums", "Query returned null")
            return ArrayList()
        }

        cursor.use {
            val bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            if (bucketIdColumn == null || bucketNameColumn == null) {
                Log.e("VideoAlbums", "Required columns are missing in MediaStore")
                return ArrayList()
            }

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdColumn) ?: continue
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown Album"

                val album = albums[bucketId] ?: VideoAlbum(bucketId, bucketName, 0)
                albums[bucketId] = album.copy(videoCount = album.videoCount + 1)
            }
        }

        return ArrayList(albums.values)
    }

}