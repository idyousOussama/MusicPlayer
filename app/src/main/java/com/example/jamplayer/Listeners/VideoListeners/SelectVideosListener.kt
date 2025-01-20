package com.example.jamplayer.Listeners.VideoListeners

import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.Video

interface SelectVideosListener {
    fun onItemCheckChanged(selectedVideo : Video, toAdd : Boolean)
    fun onHiddenSongLongClikcked(selectedVideo : Video)
}