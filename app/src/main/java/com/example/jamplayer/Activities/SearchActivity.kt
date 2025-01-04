package com.example.jamplayer.Activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbum
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbumSongsList
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Adapters.AlbumAdapter
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Services.BaseApplication
import com.example.jamplayer.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    lateinit var binding : ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSearch()
        initbackBtn()
    }
    private fun initbackBtn() {
        binding.searchGoBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun setSearch() {
        binding.inputSearchBar.addTextChangedListener {
            val searchText = binding.inputSearchBar.text.toString()

            if (searchText.isNotEmpty() && searchText.isNotEmpty()) {
                binding.searchScroll.visibility = View.GONE
                binding.searchProgress.visibility = View.VISIBLE
                executor.execute {
                    val filteredSongList = unHideSong.filter {
                        it.title.contains(searchText, ignoreCase = true) || it.album.contains(searchText, ignoreCase = true)
                    }
                    val filteredAlbumList = albumList.filter {
                        it.name!!.contains(searchText, ignoreCase = true) || it.name!!.contains(searchText, ignoreCase = true)
                    }
                    // Launch a coroutine to update the UI
                    lifecycleScope.launch {
                        if(filteredSongList.isEmpty() && filteredAlbumList.isEmpty() ){
                            binding.searchScroll.visibility = View.GONE
                            binding.searchProgress.visibility =View.GONE
                              binding.noResultLayout.visibility = View.VISIBLE
                        }else if (filteredSongList.isEmpty() && filteredAlbumList.isNotEmpty() ) {
                            setFilteredAlbumsList(filteredAlbumList as ArrayList)
                            binding.searchProgress.visibility = View.GONE
                            binding.noResultLayout.visibility =  View.GONE
                            binding.noResultLayout.visibility = View.GONE
                            binding.searchScroll.visibility = View.VISIBLE
                            binding.searchSongsText.visibility = View.GONE
                            binding.searchAlbumText.visibility = View.VISIBLE
                            binding.searchAlbumText.setText("Albums" + "("+filteredAlbumList.size.toString()+")")

                        }else if (filteredSongList.isNotEmpty() && filteredAlbumList.isEmpty()){
                            setFilteredSongsList(filteredSongList as ArrayList)
                            binding.searchProgress.visibility = View.GONE
                            binding.noResultLayout.visibility = View.GONE
                            binding.searchScroll.visibility = View.VISIBLE
                            binding.searchSongsText.visibility = View.VISIBLE
                            binding.searchAlbumText.visibility = View.GONE
                            binding.noResultLayout.visibility =  View.GONE
                            binding.searchAlbumText.setText("Songs" + "("+ filteredSongList.size.toString()+")")

                        }else{
                            setFilteredSongsList(filteredSongList as ArrayList)
                            setFilteredAlbumsList(filteredAlbumList as ArrayList)
                            binding.searchProgress.visibility = View.GONE
                            binding.searchScroll.visibility = View.VISIBLE
                            binding.searchSongsText.visibility = View.VISIBLE
                            binding.searchAlbumText.visibility = View.VISIBLE
                            binding.noResultLayout.visibility =  View.GONE
                            binding.searchAlbumText.setText("Songs" + "("+ filteredSongList.size.toString()+")")
                            binding.searchAlbumText.setText("Albums" + "("+filteredAlbumList.size.toString()+")")


                        }
                    }
                }


            } else {
                binding.searchScroll.visibility = View.GONE
                binding.searchProgress.visibility = View.GONE
            }
        }

    }
    private fun setFilteredAlbumsList(filteredAlbums :ArrayList<Album>) {
        val albumsAdapter = AlbumAdapter(1)
        if(filteredAlbums != null){
            binding.searchAlbumRV.apply {
                if(settings!!.itemType == "small"){
                    layoutManager = LinearLayoutManager(baseContext)
                }else{
                    layoutManager = GridLayoutManager(baseContext,2)
                }
                setHasFixedSize(true)
                adapter = albumsAdapter
            }
            albumsAdapter.setAlbumsList(filteredAlbums)

            binding.searchAlbumText.text = "Albums(${filteredAlbums.size})"
            binding.searchAlbumText.visibility = View.VISIBLE
            binding.searchAlbumRV.visibility = View.VISIBLE
            binding.searchProgress.visibility = View.GONE
        }else{
            binding.searchAlbumText.visibility = View.GONE
            binding.searchAlbumRV.visibility = View.GONE
            binding.searchProgress.visibility = View.GONE
        }
        albumsAdapter.setListner(object : AlbumMusicLisntener {
            override fun onAlbumItemClicked(album: Album, albumSongs: ArrayList<MusicFile>) {
                selectedAlbum = album
                selectedAlbumSongsList = albumSongs
                val albumIntent = Intent(baseContext,ShowAlbumActivity::class.java)
                startActivity(albumIntent)
            }
        })
    }
    private fun setFilteredSongsList(filteredSongs: ArrayList<MusicFile>) {

        val songsAdapter = AudiosAdapter(1)
        if (filteredSongs.isNotEmpty()){
            songsAdapter.setMusicFile(filteredSongs)
            binding.searchRV.apply {
                if (settings!!.itemType == "small"){
                    layoutManager = LinearLayoutManager(baseContext)
                }else  {
                    layoutManager = GridLayoutManager(baseContext,2)
                }
                setHasFixedSize(true)
                adapter = songsAdapter

            }
            binding.searchSongsText.text = "Songs(${filteredSongs.size})"
            binding.searchSongsText.visibility = View.VISIBLE
            binding.searchRV.visibility = View.VISIBLE
            binding.searchProgress.visibility = View.GONE
        }else{
            binding.searchSongsText.visibility = View.GONE
            binding.searchRV.visibility = View.GONE
            binding.searchProgress.visibility = View.GONE
        }
        songsAdapter.setListner(object : MusicFileItemsListener{
            override fun onItemClickListner(mFile: MusicFile, position: Int) {
                if(mFile != null){
                    curretSong = mFile
                    val songList : ArrayList<MusicFile> =ArrayList()
                    songList.add(mFile)
                    setSongsListForPlaying(0, songList,false)
                }
            }
        })
    }
    private fun setSongsListForPlaying(positionz: Int, songList : ArrayList<MusicFile>, isRandom: Boolean) {
        var currentMediaPlayer = BaseApplication.PlayingMusicManager.mediaPlayer
        if(currentMediaPlayer != null){
            addPlayingTime(currentMediaPlayer)
        }
        songsList = songList
        random = isRandom
        position =  positionz
        val intent = Intent(baseContext,PlayingActivity :: class.java)
        startActivity(intent)
    }
    private fun addPlayingTime(currentMediaPlayer: MediaPlayer) {
        CoroutineScope(Dispatchers.Main).launch{
            jamViewModel.setPlayingTime(currentPosition)

                BaseApplication.PlayingMusicManager.mediaPlayer?.release()
            BaseApplication.PlayingMusicManager.mediaPlayer = null


        }
    }
}


