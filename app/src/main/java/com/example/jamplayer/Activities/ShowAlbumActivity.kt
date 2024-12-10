package com.example.jamplayer.Activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.curretSong
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.databinding.ActivityShowAlbumBinding

@Suppress("DEPRECATION")
class ShowAlbumActivity : AppCompatActivity() {
    lateinit var binding :ActivityShowAlbumBinding
    lateinit var album :Album
    private val musicImageCache = mutableMapOf<String, Bitmap?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      binding = ActivityShowAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
getAlbumIntent()
    }
    private fun getAlbumIntent() {
        val albumPosition = intent.getIntExtra("albumPosition",0)
      album = albumList[albumPosition]
        val albumImageBitmap = getAlbumImage(album.albumSongsList[0].path)
        if(albumImageBitmap!= null){
            binding.showAlbumImage.setImageBitmap(albumImageBitmap)
       }else{
            binding.showAlbumImage.setImageResource(R.drawable.small_place_holder_image)
        }
        binding.showAlbumName.setText(album.artist)
        binding.showAlbumSongNum.setText(album.songsNumber)
        setAlbumSongList(album.albumSongsList)
    }
    private fun setAlbumSongList(albumSongsList: ArrayList<MusicFile>) {
        val mFileAdapter = AudiosAdapter()
        mFileAdapter.setMusicFile(albumSongsList)
binding.albumSongRV.layoutManager = LinearLayoutManager(this)
        binding.albumSongRV.setHasFixedSize(true)
        binding.albumSongRV.adapter = mFileAdapter

     mFileAdapter.setListner(object : MusicFileItemsListener{
         override fun onItemClickListner(mFile: MusicFile, position: Int) {
             if(albumSongsList.isNotEmpty()){
                 songsList = albumSongsList
                 setSongsListForPlaying(position,albumSongsList,false)
             }
         }

     })
        initBtns(albumSongsList)
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

    private fun setSongsListForPlaying(position: Int, albumSongsList : ArrayList<MusicFile>, isRandom: Boolean) {
        var currentMediaPlayer = PlayingActivity.PlayingMusicManager.mediaPlayer
        if(currentMediaPlayer != null){
            PlayingActivity.PlayingMusicManager.mediaPlayer?.release()
            PlayingActivity.PlayingMusicManager.mediaPlayer = null
        }
        songsList = albumSongsList
        curretSong = albumSongsList.get(position)
        val intent = Intent(baseContext,PlayingActivity :: class.java)
        intent.putExtra("position" ,position)
        intent.putExtra("isRandom" , isRandom)
        startActivity(intent)
    }

    private fun getAlbumImage(uri: String): Bitmap? {
        if (musicImageCache.containsKey(uri)) return musicImageCache[uri]
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri)
            val imageData = retriever.embeddedPicture
            val bitmap = imageData?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            musicImageCache[uri] = bitmap
            bitmap
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

}