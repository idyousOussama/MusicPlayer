package com.example.jamplayer.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.SelectPlaylistSongsActivity.selectPlayListSongsManager.playListSelectSongs
import com.example.jamplayer.Activities.ShowPlaylistActivity.showPlaylistmanager.playList
import com.example.jamplayer.Activities.ShowPlaylistActivity.showPlaylistmanager.playlistSongs
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Moduls.Settings
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityShowPlaylistBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ShowPlaylistActivity : AppCompatActivity() {
   private lateinit var binding : ActivityShowPlaylistBinding
   private val songsAdapter = AudiosAdapter(1)
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    object showPlaylistmanager{
       var playlistSongs : ArrayList<MusicFile>? = ArrayList()
       var playList : PlayList? = null
   }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initShowPlaylistActivity()
   handlePlaySongListBtn()
    moveToAddSongs()
        moveBack()
    }
    private fun moveBack() {
        binding.showPlaylistGoBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun moveToAddSongs() {
        if (playList!!.title != getString(R.string.likedSongs_text) && playList!!.title != getString(R.string.mostPlayedSongs_text)){
            binding.showPlayListAddSongToPlayListBtn.visibility = View.VISIBLE
        }
        binding.showPlayListAddSongToPlayListBtn.setOnClickListener {
            if(playList != null) {
                playListSelectSongs = playList
                finish()
            navigateToNewActivity(SelectPlaylistSongsActivity::class.java)
            }else{
                runOnUiThread {
                    Toast.makeText(baseContext,"playList not found." , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToNewActivity(newActivity: Class<SelectPlaylistSongsActivity>) {
val newActivityIntent = Intent(baseContext , newActivity)
        startActivity(newActivityIntent)
    }

    private fun handlePlaySongListBtn() {
        binding.showPlayListPlayBln.setOnClickListener{
            if(playlistSongs!!.isNotEmpty()){
                setSongsListForPlaying(0,playlistSongs!!,false)
            }
        }
        binding.showPlayListShuffleBtn.setOnClickListener{
            if(playlistSongs!!.isNotEmpty()){
                val songsListItem = playlistSongs!!.random()
                setSongsListForPlaying(playlistSongs!!.indexOf(songsListItem),playlistSongs!!,true)
            }
        }
    }
    private fun initShowPlaylistActivity() {
        binding.showPlayListName.setText(playList!!.title)
            setUpPlaylistSongsList()
    }
    private fun setUpPlaylistSongsList() {
        if(playlistSongs!!.isNotEmpty()){
            binding.playlistNotFoundSongWraning.visibility = View.GONE
            binding.playListSongRV.visibility = View.VISIBLE
            if(playlistSongs!!.get(0).musicImage != null) {
                binding.showPlayListImage.setImageBitmap(playlistSongs!!.get(0).musicImage)
            }else{
                binding.showPlayListImage.setImageResource(R.drawable.songs_list_place_holder)
            }
            songsAdapter.setMusicFile(playlistSongs!!)
            if(playlistSongs!!.size == 1) {
                binding.showPlayListSongNum.setText(playlistSongs!!.size.toString() +" " + getString(R.string.song_text))
            }else{
                binding.showPlayListSongNum.setText(playlistSongs!!.size.toString()+" " + getString(R.string.songs_text))

            }
            setupRecyclerView()
        } else {
            binding.playListSongRV.visibility = View.GONE
            binding.playlistNotFoundSongWraning.visibility = View.VISIBLE
            binding.showPlayListSongNum.visibility = View.GONE
        }
    }
    private fun setupRecyclerView() {
        binding.playListSongRV.apply {
            layoutManager = if (settings?.itemType == "small") {
                LinearLayoutManager(context)
            } else {
                GridLayoutManager(context, 2)
            }
            setHasFixedSize(true)
            adapter = songsAdapter
        }
        songsAdapter.setListner(object : MusicFileItemsListener {
            override fun onItemClickListner(mFile: MusicFile, position: Int) {
                handleSongClick(mFile, position)
            }
        })
    }
    private fun handleSongClick(mFile: MusicFile, position: Int) {
        val currentMediaPlayer = BaseApplication.PlayingMusicManager.mediaPlayer
        currentMediaPlayer?.let {
            if (it.isPlaying) addPlayingTime()
        }
        curretSong = mFile
        random = false
        PlayingMusicManager.position = position
        songsList = playlistSongs!!

        if (songsList.isNotEmpty()) {
            val intent = Intent(baseContext, PlayingActivity::class.java)
            startActivity(intent)
        }
    }
    private fun addPlayingTime() {
        ioScope.launch {
            jamViewModel.setPlayingTime(currentPosition)
            userIsActive = true
            BaseApplication.PlayingMusicManager.mediaPlayer?.release()
            BaseApplication.PlayingMusicManager.mediaPlayer = null
        }
    }
    private fun setSongsListForPlaying(positionz: Int, albumSongsList : ArrayList<MusicFile>, isRandom: Boolean) {
        var currentMediaPlayer =  BaseApplication.PlayingMusicManager.mediaPlayer
        if(currentMediaPlayer != null){
            addPlayingTime()
        }
        songsList = albumSongsList
        curretSong = albumSongsList.get(positionz)
        random = isRandom
        position = positionz
        val intent = Intent(baseContext,PlayingActivity :: class.java)
        startActivity(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}