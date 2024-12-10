package com.example.jamplayer.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamplayer.Activities.HiddenSongsActivity

import com.example.jamplayer.Activities.PlayingActivity
import com.example.jamplayer.Activities.MainActivity
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.curretSong
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.allAudios
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.MusicFile

import com.example.jamplayer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SongsFragment : Fragment() {

    val songsAdapter = AudiosAdapter()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
val view = inflater.inflate(R.layout.fragment_songs, container, false)

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    val musisRV = view.findViewById<RecyclerView>(R.id.songsList)
    val hidenSongText = view.findViewById<TextView>(R.id.ViewHidenSongs)
        val filteredSongsList = allAudios.filter { !it.isShort } as ArrayList<MusicFile>
  CoroutineScope(Dispatchers.Main).launch {
      var  hiddenSongsNum =   jamViewModel.getHiddenSongsNum()

      if(hiddenSongsNum != 0){
          hidenSongText.setText("View " + hiddenSongsNum+ " Hidden songs")
          hidenSongText.visibility = View.VISIBLE
      }else{
          hidenSongText.visibility = View.GONE
      }
  }
        hidenSongText.setOnClickListener {
            val hiddenSongsIntent = Intent(view.context,HiddenSongsActivity::class.java)
            startActivity(hiddenSongsIntent)
        }
        setSongsList(musisRV,view, filteredSongsList)
        songsAdapter.setListner(object:MusicFileItemsListener{
            override fun onItemClickListner(mFile: MusicFile, position: Int) {
                var currentMediaPlayer = PlayingActivity.PlayingMusicManager.mediaPlayer
                if(currentMediaPlayer != null && currentMediaPlayer.isPlaying){
                    PlayingActivity.PlayingMusicManager.mediaPlayer?.release()
                    PlayingActivity.PlayingMusicManager.mediaPlayer = null
                }
                songsList = allAudios
                if(filteredSongsList.isNotEmpty()){
                    PlayingActivity.PlayingMusicManager.songsList = filteredSongsList
                    val intent = Intent(view.context,PlayingActivity :: class.java)
                    intent.putExtra("position" ,position)
                    curretSong = mFile
                        startActivity(intent)
                }
            }
        })
    }
    private fun setSongsList(
        musisRV: RecyclerView,
        v: View,
        phoneAudiosList: ArrayList<MusicFile>,
    ) {
        songsAdapter.setMusicFile(phoneAudiosList)
        musisRV.layoutManager = LinearLayoutManager(v.context)
        musisRV.setHasFixedSize(true)
         musisRV.adapter = songsAdapter
    }
}
