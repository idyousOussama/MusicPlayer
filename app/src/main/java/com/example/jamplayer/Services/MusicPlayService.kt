package com.example.jamplayer.Services


import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.jamplayer.Activities.HiddenSongsActivity.hiddenSongsManager.hiddenSongMediaPlayer
import com.example.jamplayer.Activities.MainActivity
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.ACTION_TRACK_UPDATE
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.ACTION_TRACK_HIDDEN_SONG_UPDATE
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.audioFocusChangeListener
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.audioManager
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentSongUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.File

class MusicPlayService : Service() {
    val SongList = PlayingMusicManager.songsList

    lateinit var  remoteViews :RemoteViews

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent!!.action){
        Actions.PLAY.name -> play()
        Actions.PAUSE.name -> pause()
        Actions.NEXT.name -> next()
        Actions.PREV.name -> previous()
        Actions.REPEAT.name -> repeat()
        Actions.REPEAT.name -> random()
        Actions.CANCEL.name -> cancelNotification()
        Actions.LIKE.name -> LikeAndDisLikeSong()
        Actions.MOVE_TO_MAIN_ACTIVITY.name -> moveToMainActivity()
    }
        return START_STICKY
    }
    private fun moveToMainActivity() {
        val mainIntent =Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // Add this flag
        }
        startActivity(mainIntent)
    }
    private fun random(){
        try{
            addPlayingTime()
            if (requestAudioFocus()){
                curretSong = getRandomMusic()
                position = SongList.indexOf(curretSong)
                mediaPlayer!!.release()
                mediaPlayer = MediaPlayer.create(this,curretSong!!.path.toUri())
                mediaPlayer!!.start()
                sendIntentAction("random")
                createNotification()
                CoroutineScope(Dispatchers.IO).launch {
                    jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                }
            }
        }catch(ex : Exception){
            sendIntentAction("pause")
            pause()
        }
    }
    private fun repeat() {
        try{
            addPlayingTime()
            if (requestAudioFocus()){
                mediaPlayer!!.release()
                mediaPlayer = MediaPlayer.create(this, songsList[position].path.toUri())
              mediaPlayer!!.start()
                CoroutineScope(Dispatchers.IO).launch {
                    jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                }
            }
        }catch(ex :Exception){
            sendIntentAction("pause")
            pause()
        }

   }
    private fun LikeAndDisLikeSong() {
        try{
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
        }catch(ex : Exception) {
            sendIntentAction("pause")
            pause()
        }
    }
    private fun cancelNotification() {
        try {
            addPlayingTime()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            stopForeground(true)

            notificationManager.cancel(1)
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                sendIntentAction("pause")
            }
        }catch (ex : Exception) {
            sendIntentAction("pause")
            pause()
        }

    }
    private fun previous(){
        try{
            addPlayingTime()
            if (requestAudioFocus()){
                if(position != 0){
                    mediaPlayer!!.release()
                    mediaPlayer = MediaPlayer.create(this, songsList[position-1].path.toUri())
                    curretSong = songsList[position-1]
                    position--
                    mediaPlayer!!.start()
                    CoroutineScope(Dispatchers.IO).launch {
                        jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                    }
                }else{
                    mediaPlayer!!.release()
                    mediaPlayer = MediaPlayer.create(this, songsList[0].path.toUri())
                    position = songsList.size
                    curretSong = songsList[0]
                    mediaPlayer!!.start()
                    CoroutineScope(Dispatchers.IO).launch {
                        jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                    }
                }
                sendIntentAction("prev")
                createNotification()
            }
        }catch (ex : Exception){
            sendIntentAction("pause")
            pause()
        }
    }
    private fun next(){
        try {
            addPlayingTime()
            if (requestAudioFocus()){
                if(position != songsList.size-1){
                    mediaPlayer!!.release()
                    mediaPlayer = MediaPlayer.create(this, songsList[position+1].path.toUri())
                    curretSong = songsList[position+1]
                    mediaPlayer!!.start()
                    position++
                    CoroutineScope(Dispatchers.IO).launch {
                        jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                    }
                }else{
                    mediaPlayer!!.release()
                    mediaPlayer = MediaPlayer.create(this, songsList[songsList.size-1].path.toUri())
                    position = songsList.size-1
                    curretSong = songsList[songsList.size-1]
                    mediaPlayer!!.start()
                    CoroutineScope(Dispatchers.IO).launch {
                        jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                    }
                }
                sendIntentAction("next")
                createNotification()
            }
        }catch (ex : Exception){
            sendIntentAction("pause")
            pause()

        }
        }
    private fun play() {
        try {
            if (requestAudioFocus()) {
                // Check if mediaPlayer exists and is not already playing
                if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                    // Pause hidden song player if it's playing
                    if (hiddenSongMediaPlayer != null && hiddenSongMediaPlayer!!.isPlaying) {
                        hiddenSongMediaPlayer!!.pause()
                        sendHiddenSongIntentAction("pause")
                    }
                    mediaPlayer!!.start()
                    sendIntentAction("play")
                }
                // If mediaPlayer is null, initialize it
                else if (mediaPlayer == null) {
                    if (checkSongIsNull(curretSong!!)) {
                        next()
                    // Skip to the next song if the current song file is invalid
                    } else {
                        mediaPlayer = MediaPlayer.create(baseContext, curretSong!!.path.toUri())
                        mediaPlayer!!.start()
                        sendIntentAction("create")
                        // Update played song info asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                            jamViewModel.upDateNumPlayedSongById(curretSong!!.id)
                        }
                    }
                }
                // Create or update the notification
                createNotification()
            }
        } catch (ex: Exception) {
            // Handle exceptions gracefully
            sendIntentAction("pause")
            pause()
        }
    }
      private fun pause(){
          try {
              if(mediaPlayer != null && mediaPlayer!!.isPlaying){
                  mediaPlayer!!.pause()
                  sendIntentAction("pause")
              }
              createNotification()
          }catch (ex : Exception) {
              createNotification()
              sendIntentAction("pause")

          }

    }
    enum class Actions {
        PLAY,PAUSE,PREV,NEXT,CANCEL,LIKE,REPEAT,RANDOM,MOVE_TO_MAIN_ACTIVITY
    }
    private fun createNotification() {
        try {
            if(settings!!.FSNIsEnable){
                remoteViews = RemoteViews(packageName, R.layout.sing_notification_custom)
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
                remoteViews.setOnClickPendingIntent(R.id.fourground_notification_remote_view,remoteViewPendingIntent())

                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority for pre-Oreo
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setColor(resources.getColor(R.color.Primary))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
                // Start foreground service with the notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1,notification)
                startForeground(1, notification)
            }
        }catch (ex : Exception) {

        }
    }
    private fun remoteViewPendingIntent(): PendingIntent? {
        val intent = Intent(this,MusicPlayService::class.java).apply {
            action = Actions.MOVE_TO_MAIN_ACTIVITY.name
        }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    }
    private fun upDateLikedSongPendingIntent(): PendingIntent? {
  val intent = Intent(this,MusicPlayService::class.java).apply {
      action = Actions.LIKE.name
  }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    }
    private fun cancelNotificationPendingIntent(): PendingIntent {
        // Intent for the MusicPlayService with the CANCEL action
        val intent = Intent(this, MusicPlayService::class.java).apply {
            action = Actions.CANCEL.name
        }
        // Return the PendingIntent for the service
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
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
    private fun sendHiddenSongIntentAction(action:String){
        val intent = Intent(ACTION_TRACK_UPDATE)
        intent.putExtra("hidden_data_key", action)
        sendBroadcast(intent)
    }
    private fun getRandomMusic() : MusicFile {
        return songsList.random()
    }
    private fun addPlayingTime(){
CoroutineScope(Dispatchers.Main).launch{
    jamViewModel.setPlayingTime(mediaPlayer!!.currentPosition.div(1000))
}
}
    override fun onDestroy() {
        super.onDestroy()
     //   unregisterReceiver(mediaScannerReceiver)
        releaseAudioFocus()
        cancelNotification()
    }
    private fun releaseAudioFocus() {
        audioManager.abandonAudioFocus(audioFocusChangeListener)
    }
    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener, // Listener الذي سيتم استدعاؤه عند حدوث تغييرات
            AudioManager.STREAM_MUSIC, // نوع تدفق الصوت (الموسيقى)
            AudioManager.AUDIOFOCUS_GAIN // نوع التحكم (كامل أثناء التشغيل)
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    private fun checkSongIsNull(songItem: MusicFile): Boolean {
        return try {
            val file = File(songItem.path)
            !file.exists()
        } catch (e: Exception) {
            true
        }
    }


}
