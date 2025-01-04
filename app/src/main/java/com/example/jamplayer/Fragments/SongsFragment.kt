package com.example.jamplayer.Fragments


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.HiddenSongsActivity
import com.example.jamplayer.Activities.PlayingActivity
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Services.BaseApplication
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.FragmentSongsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class SongsFragment : Fragment() {
    private val songsAdapter = AudiosAdapter(1)
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialSongs(view)
        removeMessedSongs(view.context)
        binding.songRefreshSwipe.setOnRefreshListener{
            refreshSongs(view)
            removeMessedSongs(view.context)
        }
    }
    private fun setupRecyclerView() {
        binding.songsList.apply {
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
    private fun refreshSongs(view: View) {

        ioScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    unHideSong = jamViewModel.getunhiddenSongs() as ArrayList<MusicFile>
                    updateSongsList(unHideSong)
                    updateHiddenSongsInfo(view.context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(view.context, "Failed to load songs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.songRefreshSwipe.setRefreshing(false)
                }
            }
        }
    }
    private fun updateSongsList(songs: ArrayList<MusicFile>) {
        if (songs.isEmpty()) {
            binding.songsNotFound.visibility = View.VISIBLE
            binding.songsList.visibility = View.GONE
        } else {
            binding.songsNotFound.visibility = View.GONE
            binding.songsList.visibility = View.VISIBLE
            songsAdapter.setMusicFile(songs)
            setupRecyclerView()

        }
    }

    private fun updateHiddenSongsInfo(context: Context) {
        ioScope.launch {
            val hiddenSongsNum = jamViewModel.getHiddenSongsNum()
            withContext(Dispatchers.Main) {
                if (hiddenSongsNum > 0) {
                    binding.ViewHidenSongs.apply {
                        text = "View $hiddenSongsNum Hidden Songs"
                        visibility = View.VISIBLE
                        setOnClickListener {
                            val hiddenSongsIntent = Intent(context, HiddenSongsActivity::class.java)
                            startActivity(hiddenSongsIntent)
                        }
                    }
                } else {
                    binding.ViewHidenSongs.visibility = View.GONE
                }
            }
        }
    }

    private fun handleSongClick(mFile: MusicFile, position: Int) {
        val currentMediaPlayer = BaseApplication.PlayingMusicManager.mediaPlayer
        currentMediaPlayer?.let {
            if (it.isPlaying) addPlayingTime()
        }

        curretSong = mFile
        random = false
        PlayingMusicManager.position = position
        songsList = unHideSong

        if (songsList.isNotEmpty()) {
            val intent = Intent(requireContext(), PlayingActivity::class.java)
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
    private fun removeMessedSongs(context: Context) {
        ioScope.launch {
            val allSongs = jamViewModel.getAllMusic()
            val messedSongsList = allSongs.filter {checkSongIsNull(it, context) }
            messedSongsList.forEach { song ->
                jamViewModel.removeSongById(song.id)
            }
            withContext(Dispatchers.Main) {
                if(messedSongsList.isNotEmpty()){
                    Toast.makeText(
                        context,
                        "Removed ${messedSongsList.size} invalid songs.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                refreshSongs(requireView())
            }
        }
    }
    private fun checkSongIsNull(songItem: MusicFile, context: Context): Boolean {
        return try {
            val file = File(songItem.path)
            !file.exists()
        } catch (e: Exception) {
            true
        }
    }

    private fun intialSongs(view: View) {
        ioScope.launch {
            try {
                updateSongsList(unHideSong)
                withContext(Dispatchers.Main) {
                    updateHiddenSongsInfo(view.context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(view.context, "Failed to load songs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
