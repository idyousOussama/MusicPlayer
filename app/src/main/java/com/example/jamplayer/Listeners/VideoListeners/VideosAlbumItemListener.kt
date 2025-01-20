package com.example.jamplayer.Listeners.VideoListeners

import com.example.jamplayer.Moduls.Video

interface VideosAlbumItemListener {
    fun onVideosAlbumItemClicked(title : String , VideosList : ArrayList<Video>)
}