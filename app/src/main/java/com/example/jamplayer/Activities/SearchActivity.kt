package com.example.jamplayer.Activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbum
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbumSongsList
import com.example.jamplayer.Activities.ShowPlaylistActivity.showPlaylistmanager.playList
import com.example.jamplayer.Activities.ShowPlaylistActivity.showPlaylistmanager.playlistSongs
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.playLists
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Adapters.AlbumAdapter
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Adapters.PlaylistAdapter
import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Listeners.PlayListItemListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Services.BaseApplication
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
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
        userIsActive = true
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
                    val filteredPlayList = playLists.filter {
                        it.title!!.contains(searchText, ignoreCase = true) || it.title!!.contains(searchText, ignoreCase = true)
                    }
                    // Launch a coroutine to update the UI
                    lifecycleScope.launch {
                        binding.searchProgress.visibility = View.GONE

                        if (filteredSongList.isEmpty() && filteredAlbumList.isEmpty() && filteredPlayList.isEmpty()) {
                            binding.searchScroll.visibility = View.GONE
                            binding.noResultLayout.visibility = View.VISIBLE
                        } else {
                            binding.noResultLayout.visibility = View.GONE
                            binding.searchScroll.visibility = View.VISIBLE

                            handleFilteredList(
                                filteredAlbumList,
                                binding.searchAlbumText,
                                binding.searchAlbumLayout,
                                "Albums"
                            ) { setFilteredAlbumsList(filteredAlbumList as ArrayList) }

                            handleFilteredList(
                                filteredSongList,
                                binding.searchSongsText,
                                binding.searchSongsLayout,
                                "Songs"
                            ) { setFilteredSongsList(filteredSongList as ArrayList) }

                            handleFilteredList(
                                filteredPlayList,
                                binding.searchPlayListText,
                                binding.searchPlayListLayout,
                                "Playlists"
                            ) { setFilteredPlaylist(filteredPlayList as ArrayList) }
                        }
                    }
                }


            } else {
                binding.searchScroll.visibility = View.GONE
                binding.searchProgress.visibility = View.GONE
            }
        }

    }
    private fun handleFilteredList(
        list: List<Any>,
        textView: TextView,
        linarLayout: LinearLayout,
        label: String,
        updateList: (ArrayList<Any>) -> Unit
    ) {
        if (list.isNotEmpty()) {
            updateList(ArrayList(list))
            textView.visibility = View.VISIBLE
            linarLayout.visibility = View.VISIBLE
            textView.text = "$label (${list.size})"
        } else {
            textView.visibility = View.GONE
            linarLayout.visibility = View.GONE
        }
    }
    private fun setFilteredAlbumsList(filteredAlbums :ArrayList<Album>) {
        val albumsAdapter = AlbumAdapter(1)

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

            songsAdapter.setMusicFile(filteredSongs)
            binding.searchSongRV.apply {
                if (settings!!.itemType == "small"){
                    layoutManager = LinearLayoutManager(baseContext)
                }else  {
                    layoutManager = GridLayoutManager(baseContext,2)
                }
                setHasFixedSize(true)
                adapter = songsAdapter

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
    private fun setFilteredPlaylist(filteredPlaylist: ArrayList<PlayList>) {
        val playListAdapter =PlaylistAdapter(1)
        playListAdapter.setPlayLists(filteredPlaylist)
            binding.searchPlayListRV.apply {
                if (settings!!.itemType == "small"){
                    layoutManager = LinearLayoutManager(baseContext)
                }else  {
                    layoutManager = GridLayoutManager(baseContext,2)
                }
                setHasFixedSize(true)
                adapter = playListAdapter

            }

        playListAdapter.setPlayListItemListener(object : PlayListItemListener{
            override fun onPlayListItemClicked(
                selectedPlayList: PlayList,
                playListSongs: ArrayList<MusicFile>
            ) {
                playlistSongs = playListSongs
                playList = selectedPlayList
                navigateToNewActivty(ShowPlaylistActivity::class.java)            }
        })
    }
    private fun navigateToNewActivty(newActivity: Class<ShowPlaylistActivity>) {
val newActivityInntent = Intent(baseContext , newActivity)
        startActivity(newActivityInntent)
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
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}


