package com.example.jamplayer.Activities.Vedios

import android.app.AppOpsManager
import android.app.Dialog
import android.app.PictureInPictureParams
import android.app.RecoverableSecurityException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jamplayer.Activities.Songs.MainActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Adapters.VedioAdapters.VideoAdapter
import com.example.jamplayer.Listeners.VideoListeners.VideoItemListener
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.R

import com.example.jamplayer.Services.BaseApplication.playingVideoManager.ACTION_VIDEO_UPDATE
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.currentVideo
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.handler
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.isChangeToListene
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.selectedVideoList
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoCurrentPosition
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoMediaPlayer
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPlayMode
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPosition
import com.example.jamplayer.Services.VideoPlayService
import com.example.jamplayer.databinding.ActivityPlayVideosBinding
import com.example.jamplayer.databinding.ConfirmActionDialogBinding
import com.example.jamplayer.databinding.PlayVideoModeBottomSheetCustomViewBinding
import com.example.jamplayer.databinding.VideoDetailsBottomSheetViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Suppress("DEPRECATION")
class PlayVideosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayVideosBinding
    var locked = false
    val  EDIT_VIDEO_REQ_CODE = 6546
    lateinit var intentLauncher : ActivityResultLauncher<IntentSenderRequest>
    val videoAdapter  : VideoAdapter by lazy {
        VideoAdapter(1)
    }

    var isTracking = false
    var isRotate = false
    var runnable: Runnable? = null
    companion object {
        var pipStatus = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideosBinding.inflate(layoutInflater)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        setContentView(binding.root)
        intentSenderLunchers()

        if (selectedVideoList.isNotEmpty()) {
            playSelectedVideo()
        } else {
            Toast.makeText(this, "No videos available to play", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        initButtons()
        handleVideoController()
        setupSeekBarListener()
        initGoBackBtn()
        videoListener()
    }

    private fun videoListener() {
        binding.selectedVidio.setOnCompletionListener {
            saveCurrentVideoPlayTime()
            when(videoPlayMode) {
                "playOne" -> {
                    playSelectedVideo()
                }
                "playAll" -> {
                    sendVideoServiceActions(VideoPlayService.VideoActions.NEXT)
                }
                "pauseAfterPlay" -> {
                    sendVideoServiceActions(VideoPlayService.VideoActions.STOP)


                }
            }
        }    }

    private fun intentSenderLunchers() {
        intentLauncher  = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
            if(it.resultCode == RESULT_OK){
                Toast.makeText(baseContext , "video delete  successfully" , Toast.LENGTH_SHORT).show()
                navigateToNewActivityForResult(MainActivity::class.java)

            }else {
                Toast.makeText(baseContext , "video couldn't be deleted" , Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToNewActivityForResult(newActivity: Class<*>) {
        val newActivityIntent = Intent(this,newActivity)
        newActivityIntent.putExtra("VideoIsDeleted",true)
        startActivity(newActivityIntent)
    }
    private fun initGoBackBtn() {
        binding.playingActivityBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupSeekBarListener() {
        binding.playingVideoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (videoMediaPlayer != null && fromUser) {
                    videoMediaPlayer?.seekTo(progress * 1000)
                    binding.pastDuration.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                binding.playingVideoSeekBar.thumb.alpha = 0
                videoMediaPlayer?.pause()
                isTracking = true
                toggleUIVisibility(false)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.playingVideoSeekBar.thumb.alpha = 1
                binding.playingVideoSeekBar.thumb = resources.getDrawable(R.drawable.seek_bar_thumb, null)
                videoMediaPlayer?.start()
                binding.playingActivityPlayPauseBtn.setImageResource(R.drawable.white_pause)
                isTracking = false
                toggleUIVisibility(true)
            }
        })
    }

    private fun handleVideoController() {
        binding.playingVideoActivity.setOnClickListener {
            if (binding.playingVideoToolbar.isVisible ) {
                if (!locked){
                    hideAndUnhideVieoControl(false)
                    hideStatusBar()
                }else {
                    binding.unlockVedioLayout.visibility =View.GONE
                    binding.playingVideoProgressLayout.visibility = View.GONE
                    hideStatusBar()

                }
                stopHandlerTask()
            } else {
                if (!locked) {
                    showStatusBar()
                    hideAndUnhideVieoControl(true)
                }else{
                    binding.unlockVedioLayout.visibility = View.VISIBLE
                    binding.playingVideoProgressLayout.visibility = View.VISIBLE

                    showStatusBar()

                }
            }
            runnable = Runnable {
                if (binding.playingVideoToolbar.isVisible && !isTracking) {
                    if (!locked) {
                        hideAndUnhideVieoControl(false)
                    }else {
                        binding.unlockVedioLayout.visibility =View.GONE
                        binding.playingVideoSeekBar.visibility = View.GONE
                    }
                    hideStatusBar()
                }
            }
            handler.postDelayed(runnable!!, 5000)
        }
    }

    private fun stopHandlerTask() {
        runnable?.let { handler.removeCallbacks(it) }
    }



    private fun initButtons() {

        binding.playingActivityPlayPauseBtn.setOnClickListener {
            videoMediaPlayer?.let { player ->
                if (player.isPlaying) {
                    sendVideoServiceActions(VideoPlayService.VideoActions.PAUSE)

                } else {
                    sendVideoServiceActions(VideoPlayService.VideoActions.PLAY)

                }
            }
        }
        binding.lockVedioLayout.setOnClickListener {
            lockVideo()
        }

        binding.unlockVedioLayout.setOnClickListener {
            unlockVideo()
        }
        binding.playingActivityPreviousBtn.setOnClickListener {
            sendVideoServiceActions(VideoPlayService.VideoActions.PREV)

        }
        binding.playingActivityNextBtn.setOnClickListener {
            sendVideoServiceActions(VideoPlayService.VideoActions.NEXT)


        }

        binding.vediosPlayListLayout.setOnClickListener {
            showVideoListDialog()
        }
        binding.playingVedioDetailsBtn.setOnClickListener {
            showVideoBottomSheet()
        }
        binding.changeVedioToSongLayout.setOnClickListener {
            isChangeToListene = true
            currentVideo!!.currentMediaPlayerProgress = videoMediaPlayer!!.currentPosition / 1000
            sendVideoServiceActions(VideoPlayService.VideoActions.PAUSE)
            finish()
            sendVideoServiceActions(VideoPlayService.VideoActions.CREATE)


        }
        binding.rotateVedioLayout.setOnClickListener {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                isRotate = true
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                isRotate = false

            }
        }
    }
    private fun showVideoBottomSheet() {
        val detailsView = layoutInflater.inflate(R.layout.video_details_bottom_sheet_view,null)
        val detailsViewBinding = VideoDetailsBottomSheetViewBinding.bind(detailsView)
        Glide.with(baseContext)
            .load(currentVideo!!.path.toUri())
            .thumbnail(0.2f)
            .into(detailsViewBinding.videoDetailsBottomSheetCustomImage)
        detailsViewBinding.videoDetailsBottomSheetCustomTitle.text = currentVideo!!.title
        detailsViewBinding.videoDetailsBottomSheetCustomDuration.text = formatTime((currentVideo!!.duration /1000).toInt())
        val videoDetailsBottomSheetDialog = BottomSheetDialog(this,R.style.BottomSheetDialog)
        videoDetailsBottomSheetDialog.setContentView(detailsView)
        detailsViewBinding.videoShareLayout.setOnClickListener {
            videoDetailsBottomSheetDialog.dismiss()
            shareVideo(currentVideo!!)
        }
        detailsViewBinding.videoHideLayout.setOnClickListener {
            videoDetailsBottomSheetDialog.dismiss()
            showCofirmationDialog("Hide")
            videoDetailsBottomSheetDialog.dismiss()
        }
        detailsViewBinding.deleteLayout.setOnClickListener {
            videoDetailsBottomSheetDialog.dismiss()
            showCofirmationDialog("Delete")
            videoDetailsBottomSheetDialog.dismiss()
        }
        detailsViewBinding.videoPlayModeLayout.setOnClickListener {
            videoDetailsBottomSheetDialog.dismiss()
            videoDetailsBottomSheetDialog.dismiss()
showPlayModeBottomSheet()
        }
        detailsViewBinding.videoDetailsBottomSheetCustomEditVideoBtn.setOnClickListener {
            videoDetailsBottomSheetDialog.dismiss()
            navigateToNewActivityResult(EditVideoTagsActivity::class.java , EDIT_VIDEO_REQ_CODE)
        }
        detailsViewBinding.videoFloatingWindoowLayout.setOnClickListener {
            videoDetailsBottomSheetDialog.dismiss()
val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE,android.os.Process.myUid(),packageName) == AppOpsManager.MODE_ALLOWED
        } else {
false
        }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(status){
             this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
             // here can  add the conde ere are want to empliment
                 binding.playingVideoToolbar.visibility = View.GONE
                 binding.conrolBtnsLayout.visibility = View.GONE
                 binding.playingVideoProgressLayout.visibility = View.GONE
                 binding.playingVedioBottomVedioFeaturesLayout.visibility = View.GONE
                    pipStatus = 0
         }else {
val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS",Uri.parse("package: $packageName"))
      startActivity(intent)
         }

     }else {
         Toast.makeText(baseContext,"device does not support this features",Toast.LENGTH_SHORT).show()
            }
        }
        videoDetailsBottomSheetDialog.show()
    }

    private fun navigateToNewActivityResult(newActivity: Class<*> , codeReq  : Int) {
var intent = Intent(this,newActivity)
        startActivityForResult(intent,codeReq)
    }

    private fun showPlayModeBottomSheet() {
val  playModeView = layoutInflater.inflate(R.layout.play_video_mode_bottom_sheet_custom_view,null)
   val playModeBinding = PlayVideoModeBottomSheetCustomViewBinding.bind(playModeView)
        val videoPlayModeSheet = BottomSheetDialog(this,R.style.BottomSheetDialog)
        videoPlayModeSheet.setContentView(playModeBinding.root)
        when(videoPlayMode) {
            "playOne" -> {
                playModeBinding.playOneText.setTextColor(resources.getColor(R.color.Primary))
                playModeBinding.loopOneCheckImage.visibility = View.VISIBLE
            }
            "playAll" ->{
                playModeBinding.playAllText.setTextColor(resources.getColor(R.color.Primary))
                playModeBinding.loopAllCheckImage.visibility = View.VISIBLE
            }
            "pauseAfterPlay" ->{
                playModeBinding.pauseAfterPlayText.setTextColor(resources.getColor(R.color.Primary))
                playModeBinding.pauseAfterPlayCheckImage.visibility = View.VISIBLE
            }
        }
        playModeBinding.loopOneLayout.setOnClickListener {
            videoPlayMode = "playOne"
            videoPlayModeSheet.dismiss()        }
        playModeBinding.loopAllLayout.setOnClickListener {
            videoPlayMode = "playAll"
            videoPlayModeSheet.dismiss()
        }
        playModeBinding.pauseAfterPlayLayout.setOnClickListener {
            videoPlayMode = "pauseAfterPlay"
            videoPlayModeSheet.dismiss()
        }
        videoPlayModeSheet.show()
    }


    private fun showCofirmationDialog(processType: String) {
        val confermationDialogView = layoutInflater.inflate(R.layout.confirm_action_dialog,null)
        val confirmationDialogBinding = ConfirmActionDialogBinding.bind(confermationDialogView)
        confirmationDialogBinding.confermDiologActionNegativeBtn.text = getString(R.string.cancel_text)
        when(processType){
            "Hide" -> {
                confirmationDialogBinding.confermDiologActionTitle.setText(getString(R.string.hide_video_text))
                confirmationDialogBinding.confermDiologActionPositiveBtn.setText(getString(R.string.hide_text))
                confirmationDialogBinding.confermDiologActionMessage.setText(getString(R.string.hide_video_message))

            }
            "Delete" -> {
                confirmationDialogBinding.confermDiologActionTitle.setText(getString(R.string.delete_video_text))
                confirmationDialogBinding.confermDiologActionPositiveBtn.setText(getString(R.string.delete_text))
                confirmationDialogBinding.confermDiologActionMessage.setText(getString(R.string.delete_video_message))
            }
        }
        val confirmationDialog   = Dialog(this)
        confirmationDialog.setContentView(confermationDialogView)
        confirmationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confirmationDialog.setCancelable(false)
        confirmationDialogBinding.confermDiologActionNegativeBtn.setOnClickListener {
            confirmationDialog.dismiss()
        }
        confirmationDialogBinding.confermDiologActionPositiveBtn.setOnClickListener {
            when(processType) {
                "Hide"-> {
                    hideCurrentVideo(confirmationDialog)
                }
                "Delete"->{
                    videoMediaPlayer!!.pause()
                    deleteVideoFromInternalStorage(currentVideo!!.path.toUri())
                    confirmationDialog.dismiss()
                }
            }
        }
        confirmationDialog.show()
    }
    private fun deleteVideoFromInternalStorage(videoFileName:Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try{
                contentResolver.delete(videoFileName,null,null)
            }catch(ex : SecurityException){
                val intentSender  = when{
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(contentResolver, listOf(videoFileName)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->{
                        val recoverableSecurityException = ex as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let {sender ->
                    intentLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }
    private fun hideCurrentVideo(confirmationDialog: Dialog) {
        CoroutineScope(Dispatchers.Main).launch {
            jamViewModel.HideAndUnhieVideo(true,currentVideo!!.id)
            videoMediaPlayer!!.pause()
            navigateToNewActivityForResult(MainActivity::class.java)
            confirmationDialog.dismiss()
            finish()
        }
    }
    private fun shareVideo(currentVideo: Video) {
        val file = File(currentVideo.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",  // Replace with your actual provider authority in manifest
            file
        )

        // Create an Intent to share the video
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Check if there's an app available to handle the share intent
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(shareIntent, "Share video via"))
        } else {
            Toast.makeText(this, "No app available to share the video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun lockVideo() {
        binding.playingVedioBottomVedioFeaturesLayout.visibility = View.GONE
        binding.unlockVedioLayout.visibility = View.VISIBLE
        binding.conrolBtnsLayout.visibility = View.GONE
        binding.playingVideoToolbar.visibility = View.GONE
        binding.playingVideoSeekBar.thumb.alpha = 0
        binding.playingVideoSeekBar.isEnabled = false

        locked = true
    }
    private fun unlockVideo () {
        binding.unlockVedioLayout.visibility = View.GONE
        binding.playingVedioBottomVedioFeaturesLayout.visibility = View.VISIBLE
        binding.conrolBtnsLayout.visibility = View.VISIBLE
        binding.playingVideoToolbar.visibility = View.VISIBLE
        binding.playingVideoSeekBar.visibility = View.VISIBLE
        binding.playingVideoSeekBar.thumb.alpha = 1
        binding.playingVideoSeekBar.thumb = resources.getDrawable(R.drawable.seek_bar_thumb, null)

        binding.playingVideoSeekBar.isEnabled = true
        locked = false
    }
    private fun showVideoListDialog() {
        val videosListView = layoutInflater.inflate(R.layout.landscape_solo_list_bottom_sheet_custom_view, null)
        val videoRV = videosListView.findViewById<RecyclerView>(R.id.landscape_solo_lists_bottom_sheet_playLists_RV)
        videosListView.findViewById<TextView>(R.id.solo_lists_bottom_sheet_title).text = getString(R.string.videos_list)
        val bottomSheet = BottomSheetDialog(this@PlayVideosActivity, R.style.BottomSheetDialog)
        bottomSheet.setContentView(videosListView)

        // Ensure videoRV is not null before passin it to initVideosList
        if (videoRV != null) {
            initVideosList(videoRV, bottomSheet)
        }
        bottomSheet.show()
    }

    private fun initVideosList(playListRV: RecyclerView?, bottomSheet: BottomSheetDialog) {
        // Ensure videoAdapter is not null before accessing it
        videoAdapter.setVideosList(selectedVideoList)

        playListRV?.apply {
            layoutManager = LinearLayoutManager(baseContext)
            setHasFixedSize(true)
            adapter = videoAdapter
            videoAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    // Ensure videoPosition is valid before calling smoothScrollToPosition
                    if (videoAdapter.itemCount > 0 && videoPosition in 0 until videoAdapter!!.itemCount) {
                        smoothScrollToPosition(videoPosition)
                        Toast.makeText(baseContext, "Scrolled to position: $videoPosition", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        setVideoItemListener(bottomSheet)
    }


    private fun setVideoItemListener(videosListSheetDialog: BottomSheetDialog) {
        videoAdapter.setVideoListener(object : VideoItemListener {
            override fun onVideoItemClicked(videoList: ArrayList<Video>, position: Int) {
                videoPosition = position
                playSelectedVideo()
                videosListSheetDialog.dismiss()
            }
        })
    }

    private fun playSelectedVideo() {
          currentVideo = selectedVideoList.getOrNull(videoPosition)
        currentVideo?.let { video ->
            binding.selectedVidio.setVideoURI(currentVideo!!.path.toUri())
            videoMediaPlayer = MediaPlayer.create(this , currentVideo!!.path.toUri())
            binding.selectedVidio.setOnPreparedListener { mediaPlayer ->
                if(currentVideo!!.currentMediaPlayerProgress == 0){
                    videoMediaPlayer = mediaPlayer
                }else{
                    mediaPlayer?.seekTo(currentVideo!!.currentMediaPlayerProgress * 1000)
                    videoMediaPlayer = mediaPlayer
                }
                videoMediaPlayer?.let { player ->
                    val durationInSeconds = player.duration / 1000
                    binding.playingVideoSeekBar.max = durationInSeconds
                    binding.originalTextDuration.text = formatTime(durationInSeconds)
                    binding.selectedVideoTitle.text = video.title
                  sendVideoServiceActions(VideoPlayService.VideoActions.PLAY)
                }
            }
        }
    }
    private fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }

    private fun showStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE // Show the status bar
        decorView.systemUiVisibility = uiOptions
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    private fun toggleUIVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.INVISIBLE
        binding.playingVideoToolbar.visibility = visibility
        binding.conrolBtnsLayout.visibility = visibility
        binding.originalTextDuration.visibility = visibility
        binding.pastDuration.visibility = visibility
    }

    private fun hideAndUnhideVieoControl(visible: Boolean) {
        if (visible) {
            binding.playingVideoToolbar.visibility = View.VISIBLE
            binding.conrolBtnsLayout.visibility = View.VISIBLE
            binding.playingVideoProgressLayout.visibility = View.VISIBLE
            binding.playingVedioBottomVedioFeaturesLayout.visibility = View.VISIBLE
        }else{
            binding.playingVideoToolbar.visibility = View.INVISIBLE
            binding.conrolBtnsLayout.visibility = View.INVISIBLE
            binding.playingVideoProgressLayout.visibility = View.INVISIBLE
            binding.playingVedioBottomVedioFeaturesLayout.visibility = View.INVISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val params = binding.selectedVidio.layoutParams
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.selectedVidio.setLayoutParams(params)
            showVideoFeaturesTitles()

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val params = binding.selectedVidio.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.selectedVidio.setLayoutParams(params);
            hideVideoFeaturesTitles()
        }
    }

    private fun showVideoFeaturesTitles() {
        binding.playVideoListenText.visibility = View.VISIBLE
        binding.playVideoRotateText.visibility = View.VISIBLE
        binding.playVideoLockText.visibility = View.VISIBLE
        binding.playVideoQueueText.visibility = View.VISIBLE
    }
    private fun hideVideoFeaturesTitles() {
        binding.playVideoListenText.visibility = View.GONE
        binding.playVideoRotateText.visibility = View.GONE
        binding.playVideoLockText.visibility = View.GONE
        binding.playVideoQueueText.visibility = View.GONE
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if(pipStatus != 0){
            finish()
            val intent = Intent(this,PlayVideosActivity::class.java)
            startActivity(intent)
            playSelectedVideo()
        }
    }
    val videoBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("video_data_key")
            when(data){
                "pause"->{
                    binding.playingActivityPlayPauseBtn.setImageResource(R.drawable.white_play)
                }
                "prev" ->{
                    playSelectedVideo()
                    binding.playingActivityPlayPauseBtn.setImageResource(R.drawable.white_pause)
                }
                "play" ->{
                    binding.playingActivityPlayPauseBtn.setImageResource(R.drawable.white_pause)
                }

                "next" ->{
                    playSelectedVideo()
                    binding.playingActivityPlayPauseBtn.setImageResource(R.drawable.white_pause)

                }
                "stop" -> {
                    binding.playingVideoSeekBar.progress = 0
                    binding.pastDuration.text = "00:00"
                    binding.playingActivityPlayPauseBtn.setImageResource(R.drawable.white_play)                }
            "upDateProgress" -> {
                binding.playingVideoSeekBar.progress = videoCurrentPosition
                binding.pastDuration.text = formatTime(videoCurrentPosition)
            }
            }
        }
    }

    override fun  onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_VIDEO_UPDATE)
        registerReceiver(videoBroadcastReceiver, filter)
    }
    private fun sendVideoServiceActions(action: VideoPlayService.VideoActions) {
        Intent(this,VideoPlayService::class.java).also {
            it.action = action.name
            startService(it)
        }
    }
    private fun saveCurrentVideoPlayTime() {
        if(videoMediaPlayer != null){
            CoroutineScope(Dispatchers.Main).launch{
                jamViewModel.setVideoPlayingTime(videoMediaPlayer!!.currentPosition.div(1000))
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == EDIT_VIDEO_REQ_CODE && resultCode == RESULT_OK && data!!.data != null) {
        val newVideoTitle = data.getStringExtra("newVideoTitle")
        binding.selectedVideoTitle.setText(newVideoTitle)
    sendVideoServiceActions(VideoPlayService.VideoActions.PLAY)

    }
    }
}