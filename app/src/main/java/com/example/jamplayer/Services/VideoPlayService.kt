package com.example.jamplayer.Services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import com.example.jamplayer.Activities.Vedios.PlayVideosActivity
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.ACTION_VIDEO_UPDATE
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.currentVideo
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.handler
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.isChangeToListene
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.selectedVideoList
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoCurrentPosition
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoMediaPlayer
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPlayMode
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoPlayService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            VideoActions.PLAY.name -> playVideo()
            VideoActions.MOVE_TO_PLAYING_VIDEO_ACTIVITY.name -> moveToPlayingVideoActivity()
            VideoActions.PAUSE.name -> pauseVideo()
            VideoActions.STOP.name -> stopVideo()
            VideoActions.PREV.name -> priveousVideo()
            VideoActions.NEXT.name -> nextVideo()
            VideoActions.CREATE.name -> createVideoBackground()
        }
        updateVideoSeekBar()
        return START_STICKY

    }
    private fun createVideoBackground() {
        if(videoMediaPlayer != null) {
            videoMediaPlayer =  MediaPlayer.create(this, currentVideo!!.path.toUri())
            videoMediaPlayer!!.seekTo(currentVideo!!.currentMediaPlayerProgress)
            videoMediaPlayer!!.start()
        }
    createVideoNotification()
    }
    private fun cancelVideoNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            stopForeground(true)
            notificationManager.cancel(2)

        }catch (ex : Exception) {
            pauseVideo()
        }

    }

    private fun moveToPlayingVideoActivity() {
        cancelVideoNotification()
        if (videoMediaPlayer != null && videoMediaPlayer!!.isPlaying()) {
            videoMediaPlayer!!.pause()
            val playingVideoIntent = Intent(this, PlayVideosActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            // Ensure currentVideo is not null before accessing its properties
            currentVideo?.let {
                it.currentMediaPlayerProgress = videoMediaPlayer!!.currentPosition / 1000
                startActivity(playingVideoIntent)
            }
        }else if (videoMediaPlayer != null && !videoMediaPlayer!!.isPlaying()){
            val playingVideoIntent = Intent(this, PlayVideosActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            // Ensure currentVideo is not null before accessing its properties
            currentVideo?.let {
                it.currentMediaPlayerProgress = videoMediaPlayer!!.currentPosition / 1000
                startActivity(playingVideoIntent)
            }}
    }
    enum class VideoActions {
        CREATE,PLAY, PAUSE, PREV, NEXT,MOVE_TO_PLAYING_VIDEO_ACTIVITY, CREATE_VIDEO_NOTIFICATION,STOP
    }
    private fun pauseVideo() {
        videoMediaPlayer!!.pause()
        sendVideoIntentAction("pause")
    }
    private fun priveousVideo() {
        saveCurrentVideoPlayTime()

        currentVideo!!.currentMediaPlayerProgress = 0
        if (videoPosition > 0) {
            currentVideo?.isSelected = false
            videoPosition--
            selectedVideoList[videoPosition].isSelected = true
            videoMediaPlayer?.pause()
            sendVideoIntentAction("prev")
        }
    }
    private fun playVideo() {
         if(videoMediaPlayer != null) {
             videoMediaPlayer?.start()
             sendVideoIntentAction("play")
         }
        cancelVideoNotification()
    }
    private fun nextVideo() {
        saveCurrentVideoPlayTime()
        currentVideo!!.currentMediaPlayerProgress = 0
        if (videoPosition < selectedVideoList.size - 1) {
           saveCurrentVideoPlayTime()
            currentVideo?.isSelected = false
            videoPosition++
            selectedVideoList[videoPosition].isSelected = true
            videoMediaPlayer?.pause()
            sendVideoIntentAction("next")

        } else {
            Toast.makeText(baseContext, "You are at the last video", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveCurrentVideoPlayTime() {
        if(videoMediaPlayer != null){
            CoroutineScope(Dispatchers.Main).launch{
                jamViewModel.setVideoPlayingTime(videoMediaPlayer!!.currentPosition.div(1000))
            }
        }
       }

    private fun stopVideo() {
        videoMediaPlayer!!.pause()
        videoMediaPlayer!!.seekTo(0)
        sendVideoIntentAction("stop")

    }
    private fun createVideoNotification() {
        // Ensure that settings and video are available
        settings?.let {
            if (it.FSNIsEnable && currentVideo != null) {
                val thumbnail = getVideoThumbnail(currentVideo!!.filePath.toUri())

                val videoNotificationRemoteView = RemoteViews(packageName, R.layout.play_video_notification_remote_view).apply {
                    setTextViewText(R.id.video_notification_video_title, currentVideo!!.title)
                    setImageViewBitmap(R.id.video_notification_video_img, thumbnail)
                    setOnClickPendingIntent(R.id.video_notification, notificationRemoteViewPendingIntent())
                }
                val notification = NotificationCompat.Builder(this, VIDEOCHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(videoNotificationRemoteView)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority for pre-Oreo
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setColor(resources.getColor(R.color.Primary))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                // Start foreground service with the notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
                startForeground(2, notification)
            }
        }
    }

    private fun notificationRemoteViewPendingIntent(): PendingIntent? {
        val intent = Intent(this, VideoPlayService::class.java).apply {
            action = VideoActions.MOVE_TO_PLAYING_VIDEO_ACTIVITY.name
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    fun getVideoThumbnail(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(baseContext, uri)
            // Get the frame at the first time unit (0 microseconds)
            return retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
    }
    private fun sendVideoIntentAction(action:String){
        val intent = Intent(ACTION_VIDEO_UPDATE)
        intent.putExtra("video_data_key", action)
        sendBroadcast(intent)
    }
    private fun updateVideoSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                videoMediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            videoCurrentPosition = player.currentPosition / 1000
                            currentVideo!!.currentMediaPlayerProgress = videoCurrentPosition
                            sendVideoIntentAction("upDateProgress")
                        if(formatTime(videoCurrentPosition) == formatTime(videoMediaPlayer?.duration?.div(1000) ?: 0) && isChangeToListene) {
                            when(videoPlayMode) {
                                "playOne" -> {
                                    saveCurrentVideoPlayTime()
                                    createVideoBackground()
                                }
                                "playAll" ->{
                                    saveCurrentVideoPlayTime()
                                    nextBackgroundVideo()
                                }
                                "pauseAfterPlay" ->{
                                    saveCurrentVideoPlayTime()
                                 sendVideoIntentAction("pause")
                                    currentVideo!!.currentMediaPlayerProgress = 0
                                    videoMediaPlayer!!.pause()
                                }
                            }

                        }

                        }
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                        handler.removeCallbacks(this)
                    }
                }
                handler.postDelayed(this, 500) // Update every half second
            }
        })
    }
    private fun nextBackgroundVideo() {
        currentVideo!!.currentMediaPlayerProgress = 0
        if (videoPosition < selectedVideoList.size - 1) {
            currentVideo?.isSelected = false
            videoPosition++
            selectedVideoList[videoPosition].isSelected = true
            currentVideo = selectedVideoList.get(videoPosition)
            createVideoBackground()
            createVideoNotification()
        } else {
            Toast.makeText(baseContext, "You are at the last video", Toast.LENGTH_SHORT).show()
        }    }

    private fun formatTime(playingTime: Int): String {
        val hours = (playingTime / 3600).toString()
        val minutes = ((playingTime % 3600) / 60).toString().padStart(2, '0') // Add leading zero
        val seconds = (playingTime % 60).toString().padStart(2, '0') // Add leading zero
        return if (playingTime >= 3600) "$hours:$minutes:$seconds" else "$minutes:$seconds"
    }
}

