package com.example.jamplayer.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
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
        Toast.makeText(baseContext,settings!!.playingTime.toString(),Toast.LENGTH_SHORT).show()
               binding.playingTimeTextBar.setText(formatTime(settings!!.playingTime.toInt()))
       binding.songsPlayingTime.setText(formatTime(settings!!.playingTime.toInt()))
    }
    private fun formatTime(playingTime: Int): String {
        val hours = (playingTime / 3600).toString()
        val minutes = ((playingTime % 3600) / 60).toString().padStart(2, '0') // Add leading zero
        val seconds = (playingTime % 60).toString().padStart(2, '0') // Add leading zero

        return if (playingTime >= 3600) "$hours:$minutes:$seconds" else "$minutes:$seconds"
    }

}