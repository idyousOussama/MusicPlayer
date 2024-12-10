package com.example.jamplayer.Listeners

import androidx.lifecycle.LiveData
import com.example.jamplayer.Moduls.MusicFile

interface JamRoomListener {
    fun getAllSongsListener (songsList : List<MusicFile>)
    fun getSongsByTitleListener (songsList : List<MusicFile>)
}