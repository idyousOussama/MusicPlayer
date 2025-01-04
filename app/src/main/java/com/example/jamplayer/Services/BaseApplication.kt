package com.example.jamplayer.Services

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.audioFocusChangeListener
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.audioManager
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import java.util.ArrayList


const val CHANNEL_ID = "channel_id"
const val  CHANNEL_NAME = "channel_name"

class BaseApplication : Application() {
    object PlayingMusicManager {
        var mediaPlayer: MediaPlayer? = null
        var songsList: ArrayList<MusicFile> = ArrayList()
        var curretSong : MusicFile? = null
        var repeate = false
        var random = false
        var position = -1
        var currentSongUri: Uri? = null
        var userIsActive = false
        var currentPosition : Int = 0
          lateinit var audioManager: AudioManager
        const val ACTION_TRACK_UPDATE = "com.example.ACTION_TRACK_UPDATE"
        const val ACTION_TRACK_HIDDEN_SONG_UPDATE = "com.example.hidden_song.ACTION_TRACK_UPDATE"
        var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    }
    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
if(!userIsActive){
    stratService(MusicPlayService.Actions.PAUSE)
}

                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    stratService(MusicPlayService.Actions.PAUSE)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    BaseApplication.PlayingMusicManager.mediaPlayer?.setVolume(0.5f, 0.5f)
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    stratService(MusicPlayService.Actions.PLAY)
                    BaseApplication.PlayingMusicManager.mediaPlayer?.setVolume(1.0f, 1.0f)
                }
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notification for music playback control"
                setSound(null, null) // تعطيل الصوت
                enableVibration(false)

            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


    }
    private fun stratService(action: MusicPlayService.Actions) {
        Intent(this,MusicPlayService::class.java).also {
            it.action = action.name
            startService(it)
        }
    }
}