package com.example.jamplayer.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbum
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbumSongsList
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication
import com.example.jamplayer.databinding.ActivityShowAlbumBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ShowAlbumActivity : AppCompatActivity() {
    private lateinit var binding :ActivityShowAlbumBinding
    object ShowAlbumDetailsManagerObj{
        var selectedAlbum :Album? = null
        var selectedAlbumSongsList :ArrayList<MusicFile> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      binding = ActivityShowAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
getSelectedAlbum()
        setAlbumSongList()
        moveBack()

    }
    private fun moveBack() {
        binding.showAlbumGoBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun getSelectedAlbum() {

    if(selectedAlbum != null && selectedAlbumSongsList.isNotEmpty()){
if(selectedAlbumSongsList.get(0).musicImage != null){
    binding.showAlbumImage.setImageBitmap(selectedAlbumSongsList.get(0).musicImage)
}else{
    binding.showAlbumImage.setImageResource(R.drawable.songs_list_place_holder)
}
    binding.showAlbumName.setText(selectedAlbum!!.name)
         if(selectedAlbumSongsList.isNotEmpty()){
             if(selectedAlbumSongsList.size == 1 ){
                 binding.showAlbumSongNum.setText(selectedAlbumSongsList.size.toString() + " " + "Song")
             }else{
                 binding.showAlbumSongNum.setText(selectedAlbumSongsList.size.toString() + " " + "Songs")
             }
         }
    }

    }



    private fun setAlbumSongList() {
        if(selectedAlbumSongsList.isNotEmpty()){
          binding.albumNotFoundSongWraning.visibility = View.GONE
            binding.albumSongRV.visibility = View.VISIBLE
            val mFileAdapter = AudiosAdapter(1)
            mFileAdapter.setMusicFile(selectedAlbumSongsList)
            binding.albumSongRV.apply {
                if(settings!!.itemType == "small"){
                    layoutManager = LinearLayoutManager(baseContext)
                }else{
                    layoutManager = GridLayoutManager(baseContext,2)
                }
                setHasFixedSize(true)
                adapter = mFileAdapter
            }
            mFileAdapter.setListner(object : MusicFileItemsListener{
                override fun onItemClickListner(mFile: MusicFile, position: Int) {
                    if(selectedAlbumSongsList.isNotEmpty()){
                        songsList = selectedAlbumSongsList
                        setSongsListForPlaying(position,selectedAlbumSongsList,false)
                    }
                }

            })

        }else{
            binding.albumNotFoundSongWraning.visibility = View.VISIBLE
            binding.albumSongRV.visibility = View.GONE
        }



        initBtns(selectedAlbumSongsList)
    }


    private fun initBtns(albumSongsList: ArrayList<MusicFile>) {
        binding.showAlbumPlayBln.setOnClickListener{
            if(albumSongsList.isNotEmpty()){
                setSongsListForPlaying(0,albumSongsList,false)
            }
        }
        binding.showAlbumShuffleBtn.setOnClickListener{
            if(albumSongsList.isNotEmpty()){
                val songsListItem = albumSongsList.random()
                setSongsListForPlaying(albumSongsList.indexOf(songsListItem),albumSongsList,true)
            }
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


    private fun addPlayingTime(){
        CoroutineScope(Dispatchers.Main).launch{
            jamViewModel.setPlayingTime(currentPosition)
            BaseApplication.PlayingMusicManager.mediaPlayer?.release()
            BaseApplication.PlayingMusicManager.mediaPlayer = null
        }
    }
}