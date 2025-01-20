package com.example.jamplayer.Listeners.VideoListeners

import com.example.jamplayer.Moduls.Video

interface VideoItemListener {
    fun onVideoItemClicked(videoList  : ArrayList<Video> , position : Int)
}