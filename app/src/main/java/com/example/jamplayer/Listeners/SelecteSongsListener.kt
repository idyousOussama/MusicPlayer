package com.example.jamplayer.Listeners

import com.example.jamplayer.Moduls.MusicFile

interface SelecteSongsListener{
fun onItemCheckChanged(selectedSong : MusicFile , toAdd : Boolean)
fun onHiddenSongLongClikcked(selectedSong : MusicFile)
}