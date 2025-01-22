package com.example.jamplayer.Activities.Songs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.SelectPlaylistSongsActivity.selectPlayListSongsManager.playListSelectSongs
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity.showPlaylistmanager.playList
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity.showPlaylistmanager.playlistSongs
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Adapters.SelectSongsAdapter
import com.example.jamplayer.Listeners.SelecteSongsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivitySelectPlaylistSongsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectPlaylistSongsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySelectPlaylistSongsBinding
    var checkedPlaylistSongsList: ArrayList<String> = ArrayList()
    private lateinit var processDialog: Dialog
    var playListSongs : ArrayList<MusicFile> = unHideSong
    object selectPlayListSongsManager{
        var playListSelectSongs : PlayList? = null
    }
    val hiddenSongsAdapter  = SelectSongsAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivitySelectPlaylistSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
         initSelectSongs()
        setSongs(playListSongs)
        saveSelectedSongs()
        searchingSongs()
        selectAllSongs()
       initbackBtn()
    }
    private fun initSelectSongs() {
        playListSelectSongs?.let { selectedSongs ->
            if (selectedSongs.playlistSong.isNotEmpty()) {
                playListSongs = playListSongs.filter { item ->
                    selectedSongs.playlistSong.none { playListSng ->
                        item.id == playListSng
                    }
                } as ArrayList // Cast back to ArrayList if necessary
            }
        }
    }

    private fun initbackBtn() {
        binding.selectSongsGoBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun selectAllSongs() {
        binding.selectAllBox.setOnClickListener{
            if (binding.selectAllBox.isChecked){
                checkedPlaylistSongsList.clear()
                for (item in playListSongs){
                    item.isChecked = true
                    checkedPlaylistSongsList.add(item.id)
                }
                enableSaveBtn()
            }else if (!binding.selectAllBox.isChecked){
                for (item in playListSongs){
                    item.isChecked = false
                    checkedPlaylistSongsList.remove(item.id)
                }
                disableSaveBtn()
            }
            setSongs(playListSongs)
        }
    }
    private fun saveSelectedSongs() {
        binding.selectSongsSaveBtn.setOnClickListener {
            alertProcessingDialog()
            CoroutineScope(Dispatchers.Main).launch {
                // Check if playListSelectSongs is null
                if (playListSelectSongs == null) {
                    Toast.makeText(this@SelectPlaylistSongsActivity, "Playlist is not available", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val updatedSongs = playListSelectSongs!!.playlistSong

                // Check if playlistSong is null
                if (updatedSongs == null) {
                    Toast.makeText(this@SelectPlaylistSongsActivity, "No songs found in the playlist", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                updatedSongs.addAll(checkedPlaylistSongsList)
                jamViewModel.upDatePlaylistSongsList(playListSelectSongs!!.id, updatedSongs)
                playlistSongs!!.clear()
                 for (it in updatedSongs) {
                     playlistSongs!!.add(jamViewModel.getSongsById(it))
                 }
                playList = playListSelectSongs
                navigateToNewActivity(ShowPlaylistActivity::class.java)
            }
        }
    }

    private fun navigateToNewActivity(newActivity: Class<ShowPlaylistActivity>) {
val newActivityIntent = Intent(baseContext,newActivity)
 startActivity(newActivityIntent)
        finish()
    }

    private fun setSongs(hiddenSongList: ArrayList<MusicFile>) {
        if (this.playListSongs.isNotEmpty()){
            hiddenSongsAdapter.setSongList(hiddenSongList)
            binding.selectSongsSongList.apply {
                if(settings!!.itemType == "small") {
                    layoutManager = LinearLayoutManager(baseContext)
                }else{
                    layoutManager = GridLayoutManager(baseContext,2)
                }
                setHasFixedSize(true)
                adapter = hiddenSongsAdapter
            }
        }
        getCheckedSongItem(hiddenSongsAdapter)
    }
    private fun getCheckedSongItem(hiddenSongsAdapter: SelectSongsAdapter) {
        hiddenSongsAdapter.setSongListener(object : SelecteSongsListener {
            override fun onItemCheckChanged(hiddenSong: MusicFile, toAdd: Boolean) {
                if(toAdd ){
                    checkedPlaylistSongsList.add(hiddenSong.id)
                    if(!binding.selectSongsSaveBtn.isEnabled){
                        enableSaveBtn()
                    }else if(checkedPlaylistSongsList.size == playListSongs.size){
                        binding.selectAllBox.isChecked = true
                    }else if (!binding.selectSongsSaveBtn.isEnabled && checkedPlaylistSongsList.size == playListSongs.size){
                        enableSaveBtn()
                        binding.selectAllBox.isChecked = true
                    }
                }else{
                    if (checkedPlaylistSongsList.isNotEmpty()){
                        binding.selectAllBox.isChecked = false
                        checkedPlaylistSongsList.remove(hiddenSong.id)
                        if (checkedPlaylistSongsList.isEmpty()){
                            disableSaveBtn()
                        }
                    }

                }

            }

            override fun onHiddenSongLongClikcked(selectedSong: MusicFile) {

            }
        })
    }

    private fun enableSaveBtn() {
        binding.selectSongsSaveBtn.isEnabled = true
        binding.selectSongsSaveBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        @Suppress("DEPRECATION")
        binding.selectSongsSaveBtn.setTextColor(resources.getColor(R.color.white))
    }

    private fun disableSaveBtn() {
        binding.selectSongsSaveBtn.isEnabled = false
        binding.selectSongsSaveBtn.setBackgroundResource(R.drawable.disable_btns_backround)
        @Suppress("DEPRECATION")
        binding.selectSongsSaveBtn.setTextColor(resources.getColor(R.color.disable_text_color))
    }

    fun alertProcessingDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.processing_dialog_custom,null)
        val processText =dialogView.findViewById<TextView>(R.id.dialog_process_text)
        processDialog = Dialog(this)
        processDialog.setContentView(dialogView)
        processDialog.setCancelable(false)
        processText.setText(R.string.Unhiding_text)
        processDialog.create()
    }

    private fun searchingSongs(){
        binding.selectSongsSearchBar.addTextChangedListener {
            val searchText = binding.selectSongsSearchBar.text.toString()
            binding.selectAllBox.visibility = View.GONE
            if(searchText.isNotEmpty()){
                executor.execute {
                    val filteredHiddenSongList = playListSongs.filter {
                        it.title.contains(searchText, ignoreCase = true) || it.album.contains(searchText, ignoreCase = true)
                    }
                    lifecycleScope.launch {
                        setSongs(filteredHiddenSongList as ArrayList)
                    }
                }
            }else{
                setSongs(playListSongs)
                binding.selectAllBox.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}