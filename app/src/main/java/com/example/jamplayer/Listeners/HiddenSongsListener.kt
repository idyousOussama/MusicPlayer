package com.example.jamplayer.Listeners

import androidx.recyclerview.widget.RecyclerView
import com.example.jamplayer.Moduls.MusicFile

interface HiddenSongsListener{
fun onItemCheckChanged(hiddenSong : MusicFile , toAdd : Boolean )
}