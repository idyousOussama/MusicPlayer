package com.example.jamplayer.Listeners

import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList

interface PlayListItemListener {
fun onPlayListItemClicked (selectedPlayList : PlayList , playListSongs: ArrayList<MusicFile>)
}