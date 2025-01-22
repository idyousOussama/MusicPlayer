package com.example.jamplayer.Activities.Vedios

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings

import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity.showVideoAlbumManager.showAlbumsVideosList
import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity.showVideoAlbumManager.videosAlbumTitle
import com.example.jamplayer.Adapters.VedioAdapters.VideoAdapter
import com.example.jamplayer.Listeners.VideoListeners.VideoItemListener
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.selectedVideoList
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPosition
import com.example.jamplayer.databinding.ActivityShowVideoAlbumsVideosBinding

class ShowVideoAlbumsVideosActivity : AppCompatActivity() {
    lateinit var binding : ActivityShowVideoAlbumsVideosBinding
private val VideoAdapter = VideoAdapter(0)
    object showVideoAlbumManager {
        var videosAlbumTitle  : String = ""
        var showAlbumsVideosList : ArrayList<Video> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityShowVideoAlbumsVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVideosList()
        initBackBtn()
setVideoItemListener()
    }
    private fun setVideoItemListener() {
        VideoAdapter.setVideoListener(object : VideoItemListener{
            override fun onVideoItemClicked(videoList: ArrayList<Video>, position: Int) {
                selectedVideoList = videoList
                videoPosition = position
                navigateToNewActivity(PlayVideosActivity::class.java)
            }

        })
    }
     private fun initBackBtn() {
         binding.showVideoAlbumsBackBtn.setOnClickListener {
             finish()
         }
     }
    private fun navigateToNewActivity(newActivity: Class<*>) {
val newActivityIntent  = Intent(this@ShowVideoAlbumsVideosActivity , newActivity)
        startActivity(newActivityIntent)
    }
    private fun initVideosList() {
        binding.showVideoAlbumsAlbumTitle.text =    videosAlbumTitle
        VideoAdapter.setVideosList(showAlbumsVideosList)
initializRecyclerView()
    }

    private fun initializRecyclerView() {
        binding.showVideoAlbumsVideosRV.apply {
            if(settings!!.itemType == "small") {
                layoutManager = LinearLayoutManager(this@ShowVideoAlbumsVideosActivity)
            }else {
                layoutManager = GridLayoutManager(this@ShowVideoAlbumsVideosActivity,2)
            }
            setHasFixedSize(true)
            adapter = VideoAdapter
        }
    }
}