package com.example.jamplayer.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat.Actions
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.ACTION_TRACK_UPDATE
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.curretSong
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.position
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.songsList
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.Services.MusicPlayService
import com.example.jamplayer.databinding.ActivityPlayingBinding
import java.util.ArrayList

class PlayingActivity : AppCompatActivity() {

  lateinit var binding: ActivityPlayingBinding
  var  uri: Uri? = null
  var currentPosition : Int = 0
  var repeate = false
  var random = false
  private lateinit var audioManager: AudioManager
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                PlayingMusicManager.mediaPlayer?.pause()
                stratService(MusicPlayService.Actions.PAUSE.name)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                stratService(MusicPlayService.Actions.PAUSE.name)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                PlayingMusicManager.mediaPlayer?.setVolume(0.5f, 0.5f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                stratService(MusicPlayService.Actions.PLAY.name)
                PlayingMusicManager.mediaPlayer?.setVolume(1.0f, 1.0f)
            }
        }
    }
    object PlayingMusicManager {
        var position = -1
        var mediaPlayer: MediaPlayer? = null
        var songsList: ArrayList<MusicFile> = ArrayList()
        var curretSong : MusicFile? = null
        const val ACTION_TRACK_UPDATE = "com.example.ACTION_TRACK_UPDATE"
    }
    var handler = Handler(Looper.getMainLooper()) // Ensure correct Looper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        binding.musicProgress.thumb?.alpha = 0
        getIntentMethod()
        musicControl()
        binding.goBackBtn.setOnClickListener {
            finish()
        }
        // Set SeekBar listener
        binding.musicProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer?.seekTo(progress * 1000)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBar?.thumb?.alpha =255
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optionally handle when the user stops dragging the SeekBar
                seekBar?.thumb?.alpha = 0
            }
        })
        updateSeekBar()
    }




    private fun musicControl() {
        binding.palyPauseBtn.setOnClickListener {
            stratService(MusicPlayService.Actions.PLAY.name)

        }
        binding.previousBtn.setOnClickListener {
            stratService(MusicPlayService.Actions.PREV.name)

        }
        binding.nextBtn.setOnClickListener {
            stratService(MusicPlayService.Actions.NEXT.name)

        }
        binding.repeatBtn.setOnClickListener {
            if(repeate){
                repeate = false
                binding.repeatBtn.setImageResource(R.drawable.repeat)
            }else{
                binding.repeatBtn.setImageResource(R.drawable.fill_repeat)
                repeate = true
                if(random){
                    random = false
                }
                binding.randomBtn.setImageResource(R.drawable.random)
            }
        }
        binding.randomBtn.setOnClickListener {
            if(random){
                random = false
                binding.randomBtn.setImageResource(R.drawable.random)
            }else{
                random = true
                binding.randomBtn.setImageResource(R.drawable.fill_random)
                if(repeate){
                    repeate = false
                    binding.repeatBtn.setImageResource(R.drawable.repeat)
                }
            }
        }
        binding.songLike.setOnClickListener {
            stratService(MusicPlayService.Actions.LIKE.name)
        }
    }


    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    currentPosition = it.currentPosition / 1000
                    binding.musicProgress.progress = currentPosition
                    binding.pastDuration.text = formatTime(currentPosition)

                }
                if(formatTime(currentPosition) == formatTime(mediaPlayer?.duration?.div(1000) ?: 0) && !repeate && !random){
              stratService(MusicPlayService.Actions.NEXT.name)
                }else if (formatTime(currentPosition) == formatTime(mediaPlayer?.duration?.div(1000) ?: 0) && repeate){
                    stratService(MusicPlayService.Actions.REPEAT.name)
                }else if (formatTime(currentPosition) == formatTime(mediaPlayer?.duration?.div(1000) ?: 0) && random){
                 stratService(MusicPlayService.Actions.RANDOM.name)
                }
                handler.postDelayed(this, 1000) // Update every second
            }
        })
    }
    private fun createNewMusic(posi: Int) {
        uri = songsList[posi].path.toUri()
        playNewMusic(posi)
    }
    private fun formatTime(currentPosition: Int): String {
        val minutes = (currentPosition / 60).toString()
        val seconds = (currentPosition % 60).toString().padStart(2, '0') // Add leading zero
        return "$minutes:$seconds"
    }
    private fun getIntentMethod() {
        position = intent.getIntExtra("position", -1)
        random = intent.getBooleanExtra("isRandom",false)

        if(random){
            binding.randomBtn.setImageResource(R.drawable.fill_random)
            random = true
        }
        Toast.makeText(baseContext,songsList.size.toString() , Toast.LENGTH_SHORT).show()
        if (position != -1 && songsList.isNotEmpty()) {
            binding.palyPauseBtn.setImageResource(R.drawable.pause)

            // Get the selected song URI
            uri = songsList[position].path.toUri()
            Toast.makeText(baseContext, songsList[position].album , Toast.LENGTH_SHORT).show()
            playNewMusic(position)

        }
    }
    private fun playNewMusic(posi: Int) {
        if (requestAudioFocus()) {
            // لديك التحكم في التركيز الصوتي، يمكنك تشغيل الموسيقى الآن
            mediaPlayer = MediaPlayer.create(this, uri)
           // mediaPlayer!!.start()
           // curretSong = songsList[posi]
             stratService(MusicPlayService.Actions.PLAY.name)
        setSongData(curretSong!!)

            // Set SeekBar max and total duration

        }

    }

    private fun stratService (action: String) {
        Intent(this,MusicPlayService::class.java).also {
            it.action = action
            startService(it)
        }
    }

    private fun setSongData(song: MusicFile){
        if (song.musicImage != null){
            binding.musicPlayingImage.setImageBitmap(song.musicImage)
            Palette.from(song.musicImage!!).generate { palette ->
                val vibrantColor = palette?.getVibrantColor(Color.GREEN)
                binding.musicTitle.setTextColor(vibrantColor ?: Color.GREEN)
            }

        }else{
binding.musicPlayingImage.setImageResource(R.drawable.small_place_holder_image)
        }
        binding.musicProgress.max = mediaPlayer?.duration?.div(1000) ?: 0
        binding.originalDuration.text = formatTime(mediaPlayer?.duration?.div(1000) ?: 0)
        binding.musicTitle.setText(song.title)
        if(song.isLiked == true){
            binding.songLike.setImageResource(R.drawable.full_heart)
        }else{
            binding.songLike.setImageResource(R.drawable.empty_heart)
        }
    }
    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener, // Listener الذي سيتم استدعاؤه عند حدوث تغييرات
            AudioManager.STREAM_MUSIC, // نوع تدفق الصوت (الموسيقى)
            AudioManager.AUDIOFOCUS_GAIN // نوع التحكم (كامل أثناء التشغيل)
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    private fun releaseAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
    override fun onDestroy() {
        super.onDestroy()
        releaseAudioFocus()
    }
    private fun requestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(this,

                arrayOf(Manifest.permission.POST_NOTIFICATIONS),100)
        }

    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("data_key")
            when(data){
                "pause"->{
                    Toast.makeText(baseContext,"pause",Toast.LENGTH_SHORT).show()
                    binding.palyPauseBtn.setImageResource(R.drawable.play)
                }
                "prev" ->{
                    Toast.makeText(baseContext,"prev",Toast.LENGTH_SHORT).show()
                    setSongData(curretSong!!)
                }
                "play" ->{
                    Toast.makeText(baseContext,"play",Toast.LENGTH_SHORT).show()
                    binding.palyPauseBtn.setImageResource(R.drawable.pause)
                }
                "next" ->{
                    Toast.makeText(baseContext,"next",Toast.LENGTH_SHORT).show()
                    setSongData(curretSong!!)
                }
                "dislike" ->{
                    binding.songLike.setImageResource(R.drawable.empty_heart)
                }
                "like" ->{
                    binding.songLike.setImageResource(R.drawable.full_heart)
                }
                "random" ->{
                    setSongData(curretSong!!)
                }
            }
        }
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_TRACK_UPDATE)
        registerReceiver(broadcastReceiver, filter)
    }
}






