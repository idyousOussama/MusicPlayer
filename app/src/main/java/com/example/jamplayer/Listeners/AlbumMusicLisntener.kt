package com.example.jamplayer.Listeners

import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
interface AlbumMusicLisntener{
    fun onAlbumItemClicked(album : Album , albumSongs : ArrayList<MusicFile>)
}