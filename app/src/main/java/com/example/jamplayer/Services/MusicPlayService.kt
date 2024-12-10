package com.example.jamplayer.Services


import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.ACTION_TRACK_UPDATE
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.curretSong
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.position
import com.example.jamplayer.Activities.PlayingActivity.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicPlayService : Service() {
    val SongList = PlayingMusicManager.songsList
    lateinit var  remoteViews :RemoteViews

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent!!.action){
        Actions.PLAY.name -> start()
        Actions.PAUSE.name -> stop()
        Actions.NEXT.name -> next()
        Actions.PREV.name -> previous()
        Actions.REPEAT.name -> repeat()
        Actions.REPEAT.name -> random()
        Actions.CANCEL.name -> cancelNotification()
        Actions.LIKE.name -> LikeAndDisLikeSong()
    }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun random(){
         curretSong = getRandomMusic()
         position = SongList.indexOf(curretSong)
        mediaPlayer!!.release()
        mediaPlayer = MediaPlayer.create(this,curretSong!!.path.toUri())
        sendIntentAction("random")
        createNotification()
    }
    private fun repeat() {
mediaPlayer!!.release()
 mediaPlayer =MediaPlayer.create(this, songsList[position].path.toUri())
    }
    private fun LikeAndDisLikeSong() {
        if (curretSong?.isLiked == true){
            curretSong?.isLiked = false
            CoroutineScope(Dispatchers.Main).launch{
                jamViewModel.upDateLikedSong(false, curretSong!!.id)
                sendIntentAction("dislike")
                remoteViews.setImageViewResource(R.id.noti_heart_btn,R.drawable.empty_heart)
            }
        }else{
            curretSong?.isLiked = true
            CoroutineScope(Dispatchers.Main).launch{
                jamViewModel.upDateLikedSong(true, curretSong!!.id)
                sendIntentAction("like")
                remoteViews.setImageViewResource(R.id.noti_heart_btn,R.drawable.full_heart)
            }
        }
createNotification()
    }
    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1)

    }
    private fun previous(){
        if(position != 0){
            mediaPlayer!!.release()
            mediaPlayer = MediaPlayer.create(this, songsList[position-1].path.toUri())
            curretSong = songsList[position-1]
            position--
            mediaPlayer!!.start()
        }else{
            mediaPlayer!!.release()
            mediaPlayer = MediaPlayer.create(this, songsList[0].path.toUri())
            position = songsList.size
            curretSong = songsList[0]
            mediaPlayer!!.start()
        }
        sendIntentAction("prev")
        createNotification()
    }
        private fun next(){
            if(position != songsList.size-1){
                mediaPlayer!!.release()
                mediaPlayer = MediaPlayer.create(this, songsList[position+1].path.toUri())
                 curretSong = songsList[position+1]
                mediaPlayer!!.start()
                position++
            }else{
                mediaPlayer!!.release()
                mediaPlayer = MediaPlayer.create(this, songsList[songsList.size-1].path.toUri())
                position = songsList.size-1
                curretSong = songsList[songsList.size-1]
                mediaPlayer!!.start()
            }
            sendIntentAction("next")
            createNotification()
    }
    private fun start() {
        if(mediaPlayer != null && mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
            sendIntentAction("pause")

        }else if(mediaPlayer != null && !mediaPlayer!!.isPlaying){
                mediaPlayer!!.start()
            sendIntentAction("play")
        }
        createNotification()
    }
      private fun stop(){
        if (mediaPlayer != null ) {
            mediaPlayer?.pause()
            sendIntentAction("pause")
            createNotification()
        }

    }
    enum class Actions {
        PLAY,PAUSE,PREV,NEXT,CANCEL,LIKE,REPEAT,RANDOM
    }

    private fun createNotification() {
        remoteViews = RemoteViews(packageName, R.layout.sing_notification_custom)
        // Set data dynamically (e.g., song title, artist name, and album art)
        remoteViews.setTextViewText(R.id.noti_custom_music_title, PlayingMusicManager.curretSong?.title ?: "Unknown Title")
        remoteViews.setTextViewText(R.id.noti_custom_music_artist, PlayingMusicManager.curretSong?.artist ?: "Unknown Artist")
        if (curretSong!!.musicImage != null){
            remoteViews.setImageViewBitmap(R.id.noti_music_custom_image, curretSong!!.musicImage  ) // Replace with actual album art
        }else{
            remoteViews.setImageViewResource(R.id.noti_music_custom_image,R.drawable.small_place_holder_image)
        }
        val isPlaying = mediaPlayer?.isPlaying
       val isLiked = curretSong?.isLiked
        if(isLiked == true){
            remoteViews.setImageViewResource(R.id.noti_heart_btn,R.drawable.full_heart)

        } else
        {
            remoteViews.setImageViewResource(R.id.noti_heart_btn,R.drawable.empty_heart)
        }
        if (isPlaying == true) remoteViews.setImageViewResource(R.id.noti_paly_pause_btn,R.drawable.pause)else  remoteViews.setImageViewResource(R.id.noti_paly_pause_btn,R.drawable.play)
        val playPausePendingIntent = if (isPlaying == true) pausePendingIntent() else playPendingIntent()

        remoteViews.setOnClickPendingIntent(R.id.noti_previous_btn, prevPendingIntent())
        remoteViews.setOnClickPendingIntent(R.id.noti_paly_pause_btn,playPausePendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.noti_next_btn, nextPendingIntent())
        remoteViews.setOnClickPendingIntent(R.id.noti_cancel_btn, cancelNotificationPendingIntent())
        remoteViews.setOnClickPendingIntent(R.id.noti_heart_btn, upDateLikedSongPendingIntent())

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setColor(resources.getColor(R.color.Primary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        // Start foreground service with the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1,notification)
    }
    private fun upDateLikedSongPendingIntent(): PendingIntent? {
  val intent = Intent(this,MusicPlayService::class.java).apply {
      action = Actions.LIKE.name
  }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    }

    private fun cancelNotificationPendingIntent(): PendingIntent? {
val intent = Intent(this,MusicPlayService::class.java).apply {
    action = Actions.CANCEL.name
}
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pausePendingIntent(): PendingIntent {
         var  intent = Intent(this,MusicPlayService::class.java).apply {
                action = Actions.PAUSE.name
            }
        return PendingIntent.getService(this,1,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    }

    private fun  playPendingIntent () : PendingIntent {
    val intent = Intent(this , MusicPlayService::class.java).apply {
        action = Actions.PLAY.name
    }
    return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

 }
    private fun  nextPendingIntent () : PendingIntent {
        val intent = Intent(this , MusicPlayService::class.java).apply {
            action = Actions.NEXT.name
        }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    }
    private fun  prevPendingIntent () : PendingIntent {
        val intent = Intent(this , MusicPlayService::class.java).apply {
            action = Actions.PREV.name
        }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun sendIntentAction(action:String){
        val intent = Intent(ACTION_TRACK_UPDATE)
        intent.putExtra("data_key", action)
        sendBroadcast(intent)
    }
    private fun scheduleProgressUpdates() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val remoteViews = RemoteViews(packageName, R.layout.sing_notification_custom)

                    val notification = NotificationCompat.Builder(this@MusicPlayService, CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_logo)
                        .setCustomContentView(remoteViews)
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())

                        .build()
                    notificationManager.notify(1, notification)

                    if (it.isPlaying) {
                        handler.postDelayed(this, 1000) // Update every second
                    }
                }
            }
        }
        handler.post(runnable)
    }
    private fun getRandomMusic() : MusicFile {
        return songsList.random()
    }

}
