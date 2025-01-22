package com.example.jamplayer.Activities.Songs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityPlayingTimeBinding

class PlayingTimeActivity : AppCompatActivity() {
    lateinit var binding : ActivityPlayingTimeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayingTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true

        setPalyingTime()
        initbackBtn()
    }
    private fun initbackBtn() {
        binding.playingTimeBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun setPalyingTime() {
               binding.playingTimeTextBar.setText(formatTime((settings!!.playingTime + settings!!.videoPlayingTime).toInt()))
               binding.videosPlayingTime.setText(formatTime(settings!!.videoPlayingTime.toInt()))
               binding.songsPlayingTime.setText(formatTime(settings!!.playingTime.toInt()))
    }
    private fun formatTime(playingTime: Int): String {
        val hours = (playingTime / 3600).toString()
        val minutes = ((playingTime % 3600) / 60).toString().padStart(2, '0') // Add leading zero
        val seconds = (playingTime % 60).toString().padStart(2, '0') // Add leading zero
        return if (playingTime >= 3600) "$hours:$minutes:$seconds" else "$minutes:$seconds"
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}