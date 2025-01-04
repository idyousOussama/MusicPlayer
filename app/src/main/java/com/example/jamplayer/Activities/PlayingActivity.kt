package com.example.jamplayer.Activities

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamplayer.Activities.SelectPlaylistSongsActivity.selectPlayListSongsManager.playListSelectSongs
import com.example.jamplayer.Activities.ShowPlaylistActivity.showPlaylistmanager.playList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.playLists
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Adapters.PlaylistAdapter
import com.example.jamplayer.Listeners.PlayListItemListener
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.ACTION_TRACK_UPDATE
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.repeate
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.Services.MusicPlayService
import com.example.jamplayer.databinding.ActivityPlayingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PlayingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayingBinding
    var handler = Handler(Looper.getMainLooper())
    val playListsAdapter : PlaylistAdapter  by lazy {
        PlaylistAdapter(0)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
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
        showSongDetailsBottomSheet()
    }

    private fun showSongDetailsBottomSheet() {
        binding.showSongDetailsBtn.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.song_details_bottom_sheet_custom,null)
       val song_title = bottomSheetView.findViewById<TextView>(R.id.song_details_bottom_sheet_custom_title)
       val song_artist = bottomSheetView.findViewById<TextView>(R.id.song_details_bottom_sheet_custom_artest_name)
       val song_image = bottomSheetView.findViewById<ImageView>(R.id.song_details_bottom_sheet_custom_image)
 val shareBtn = bottomSheetView.findViewById<LinearLayout>(R.id.share_layout)
  val editTagBtn =  bottomSheetView.findViewById<ImageView>(R.id.song_details_bottom_sheet_custom_edit_song_btn)
  val hideCurrentSong =  bottomSheetView.findViewById<LinearLayout>(R.id.hide_layout)
  val deleteCurrentSong =  bottomSheetView.findViewById<LinearLayout>(R.id.delete_layout)
      val addToPlaylistBtn = bottomSheetView.findViewById<LinearLayout>(R.id.add_to_playlist_layout)
        val bottomSheet = BottomSheetDialog(this,R.style.BottomSheetDialog)
        bottomSheet.setContentView(bottomSheetView)
       song_title.setText(curretSong!!.title)
        song_artist.setText(curretSong!!.artist)
        if (curretSong!!.musicImage != null){
            song_image.setImageBitmap(curretSong!!.musicImage)
        }else{
            song_image.setImageResource(R.drawable.small_place_holder_image)
        }
     shareBtn.setOnClickListener {
         val file = File(curretSong!!.path) // Replace with the actual file path
         val uri = FileProvider.getUriForFile(baseContext, "${packageName}.provider", file)

         val shareIntent = Intent(Intent.ACTION_SEND).apply {
             type = "audio/*"
             putExtra(Intent.EXTRA_STREAM, uri)
             addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
         }
         startActivity(Intent.createChooser(shareIntent, "Share Song via"))
     }
        editTagBtn.setOnClickListener {
            val EditTagIntent = Intent(this@PlayingActivity , EditTagsActivity::class.java)
            startActivity(EditTagIntent)
        }
        hideCurrentSong.setOnClickListener {
            showWraningDialog(R.string.hide_cureentSong_title , R.string.hideSong_wraning_message,"Hide")

        }
        deleteCurrentSong.setOnClickListener {
            showWraningDialog(R.string.delete_cureentSong_title , R.string.delete_song_wraning_message,"Delete")

        }
        addToPlaylistBtn.setOnClickListener {
            bottomSheet.dismiss()
            showPlayListsBottomSheets()
        }
         bottomSheet.show()
    }

   private fun showPlayListsBottomSheets() {
        val addToPlaylistView = LayoutInflater.from(baseContext).inflate(R.layout.list_bottom_sheet_view_custom,null)
        val playListRV = addToPlaylistView.findViewById<RecyclerView>(R.id.lists_bottom_sheet_playLists_RV)
        val cearteNewPlayList = addToPlaylistView.findViewById<LinearLayout>(R.id.lists_bottom_sheet_create_new_playlist_layout)
        addToPlaylistView.findViewById<TextView>(R.id.lists_bottom_sheet_title).text = getString(R.string.add_to_play_list_text)
        val playListsDialog = BottomSheetDialog(this,R.style.BottomSheetDialog)
        playListsDialog.setContentView(addToPlaylistView)
        getPlayLists(playListRV)
        setPlaylistListener(playListsDialog)
        cearteNewPlayList.setOnClickListener {
            playListsDialog.dismiss()
            showCreateNewPlaylistDialog()
        }
        playListsDialog.show()
    }
    private fun showCreateNewPlaylistDialog() {
        var titleText = ""
        // Inflate the custom dialog view
        val dialogView = LayoutInflater.from(this@PlayingActivity)
            .inflate(R.layout.create_playlist_dialog_content_view, null)

        // Find views in the custom dialog layout
        val newPlayListTitle = dialogView.findViewById<EditText>(R.id.create_new_playlist_title_input)
        val newPlayListPositiveBtn = dialogView.findViewById<TextView>(R.id.create_playList_positive_Btn)
        val newPlayListNegativeBtn = dialogView.findViewById<TextView>(R.id.create_playList_negative_Btn)

        // Use the activity context instead of baseContext
        val createNewPlaylistDialog = Dialog(this@PlayingActivity)
        createNewPlaylistDialog.setContentView(dialogView)
        createNewPlaylistDialog.setCancelable(false)
        createNewPlaylistDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Negative button click listener
        newPlayListNegativeBtn.setOnClickListener {
            createNewPlaylistDialog.dismiss()
        }

        // Text change listener for enabling/disabling the positive button
        newPlayListTitle.addTextChangedListener {
            titleText = newPlayListTitle.text.toString()
            if (titleText.isNotEmpty()) {
                initCreateNewPlayListBtn(true, newPlayListPositiveBtn)
            } else {
                initCreateNewPlayListBtn(false, newPlayListPositiveBtn)
            }
        }
        // Positive button click listener
        newPlayListPositiveBtn.setOnClickListener {
            createNewPlayList(titleText, createNewPlaylistDialog)
        }
        // Show the dialog
        if (!isFinishing && !isDestroyed) {
            createNewPlaylistDialog.show()
        }
    }
    private fun createNewPlayList(titleText: String , dialog : Dialog) {
        CoroutineScope(Dispatchers.Main).launch {
            playList = jamViewModel.getPlaylistByTitle(titleText)
            if(playList == null){
                Toast.makeText(baseContext , R.string.adding_new_play_list_text , Toast.LENGTH_SHORT).show()
                val newPlaylistSongList : ArrayList<Int> = ArrayList()
                newPlaylistSongList.add(curretSong!!.id)
                val newPlayList =PlayList(0,titleText,null,newPlaylistSongList)
                jamViewModel.insertNewPalyList(newPlayList)
                Toast.makeText(baseContext,R.string.newPlaylist_added, Toast.LENGTH_SHORT).show()
                playList = jamViewModel.getPlaylistByTitle(titleText)
                dialog.dismiss()
            }else {
                Toast.makeText(baseContext,R.string.this_PlayList_added, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initCreateNewPlayListBtn(noEmpty: Boolean, positiveBtn: TextView) {
        if(noEmpty) {
            positiveBtn.isEnabled = true
            positiveBtn.setTextColor(resources.getColor(R.color.Primary))
        }else{
            positiveBtn.isEnabled = false
            positiveBtn.setTextColor(resources.getColor(R.color.gray))
        }
    }

    private fun setPlaylistListener(playListsDialog: Dialog) {
        playListsAdapter.setPlayListItemListener(object : PlayListItemListener{
            override fun onPlayListItemClicked(
                selectedPlayList: PlayList,
                playListSongs: ArrayList<MusicFile>
            ) {
                CoroutineScope(Dispatchers.Main).launch {
                    val updatedSongs = selectedPlayList.playlistSong
                    // Check if playlistSong is null
                    updatedSongs.add(curretSong!!.id)
                    jamViewModel.upDatePlaylistSongsList(selectedPlayList.id, updatedSongs)
                 runOnUiThread {
                     Toast.makeText(baseContext , R.string.this_PlayList_added , Toast.LENGTH_SHORT).show()
                 }
                    playListsDialog.dismiss()
                }
            }

        })
    }

    private fun getPlayLists(playListRV: RecyclerView?) {
        val likedListTitle = getString(R.string.likedSongs_text)
        val mostPlayedTitle = getString(R.string.mostPlayedSongs_text)
        val recentlyPlayedTitle =getString(R.string.RecentlyPlayedSongs_text)
        val filteredPlayLists = ArrayList(playLists) // Create a mutable copy
        filteredPlayLists.removeIf { playList ->
            playList.title == likedListTitle ||
                    playList.title == mostPlayedTitle ||
                    playList.title == recentlyPlayedTitle ||
                    playList.playlistSong.any { playListSng ->
                        curretSong!!.id == playListSng
                    }
        }
        playListsAdapter.setPlayLists(filteredPlayLists)
        playListRV?.apply {
            layoutManager = LinearLayoutManager(baseContext)
                setHasFixedSize(true)
            adapter = playListsAdapter

        }
    }

    private fun showWraningDialog(hideCureentsongTitle: Int, hidesongWraningMessage: Int, action: String) {
val wraningDialogView = LayoutInflater.from(this@PlayingActivity).inflate(R.layout.confirm_action_dialog,null)
        val wraningTitle = wraningDialogView.findViewById<TextView>(R.id.conferm_diolog_action_title)
        val wraningMessage = wraningDialogView.findViewById<TextView>(R.id.conferm_diolog_action_message)
        val positiveBtn = wraningDialogView.findViewById<TextView>(R.id.conferm_diolog_action_positiveBtn)
        val negativeBtn =  wraningDialogView.findViewById<TextView>(R.id.conferm_diolog_action_negativeBtn)
        val wraningDialog = Dialog(this)
        wraningDialog.setContentView(wraningDialogView)
        wraningTitle.setText(hideCureentsongTitle)
        wraningMessage.setText(hidesongWraningMessage)
        positiveBtn.setText(action)
        positiveBtn.setOnClickListener {
            if(action == "Hide"){
                jamViewModel.unHideCurrentSong(curretSong!!.id)
                if(mediaPlayer!!.isPlaying){
                    mediaPlayer!!.release()
                    mediaPlayer = null
                    songsList.remove(curretSong)
                    runOnUiThread{
                        Toast.makeText(baseContext , curretSong!!.title + " " + "Is Hidden",Toast.LENGTH_SHORT).show()
                    }
                    val intent = Intent(this , MainActivity::class.java)
                    startActivity(intent)

                }
            }else if (action == "Delete"){
                CoroutineScope(Dispatchers.Main).launch{
                    if(mediaPlayer!!.isPlaying){
                        mediaPlayer!!.release()
                        mediaPlayer = null
                        jamViewModel.deleteCurrentSongById(curretSong!!.id)
                        val intent = Intent(baseContext , MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
        negativeBtn.setOnClickListener {
            wraningDialog.dismiss()
        }
        wraningDialog.setCancelable(false)
        wraningDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        wraningDialog.show()
    }
    private fun musicControl() {
        binding.palyPauseBtn.setOnClickListener {
            if(mediaPlayer != null && mediaPlayer!!.isPlaying){
                stratService(MusicPlayService.Actions.PAUSE)
            }else if(mediaPlayer != null && !mediaPlayer!!.isPlaying){
                    stratService(MusicPlayService.Actions.PLAY)
            }
        }
        binding.previousBtn.setOnClickListener {
            stratService(MusicPlayService.Actions.PREV)
        }
        binding.nextBtn.setOnClickListener {
            stratService(MusicPlayService.Actions.NEXT)
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
                stratService(MusicPlayService.Actions.RANDOM)
                random = true
                binding.randomBtn.setImageResource(R.drawable.fill_random)
                if(repeate){
                    repeate = false
                    binding.repeatBtn.setImageResource(R.drawable.repeat)
                }
            }
        }
        binding.songLike.setOnClickListener {
            stratService(MusicPlayService.Actions.LIKE)
        }
    }

    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
             try{
                 mediaPlayer?.let {
                     currentPosition = it.currentPosition / 1000
                     binding.musicProgress.progress = currentPosition
                     binding.pastDuration.text = formatTime(currentPosition)
                 }
                 if(formatTime(currentPosition) == formatTime(mediaPlayer?.duration?.div(1000) ?: 0) && !repeate && !random){
                     stratService(MusicPlayService.Actions.NEXT)

                 }else if (formatTime(currentPosition) == formatTime(mediaPlayer?.duration?.div(1000) ?: 0) && repeate){
                     stratService(MusicPlayService.Actions.REPEAT)

                 }else if (formatTime(currentPosition) == formatTime(mediaPlayer?.duration?.div(1000) ?: 0) && random){
                     stratService(MusicPlayService.Actions.RANDOM)
                 }
                 handler.postDelayed(this, 500) // Update every second
             }catch(ex :Exception ){
                 stratService(MusicPlayService.Actions.PAUSE)
             }

            }
        })
    }

    private fun formatTime(playingTime: Int): String {
        val hours = (playingTime / 3600).toString()
        val minutes = ((playingTime % 3600) / 60).toString().padStart(2, '0') // Add leading zero
        val seconds = (playingTime % 60).toString().padStart(2, '0') // Add leading zero
        return if (playingTime >= 3600) "$hours:$minutes:$seconds" else "$minutes:$seconds"
    }
    private fun getIntentMethod() {
        if(random){
            binding.randomBtn.setImageResource(R.drawable.fill_random)
            random = true
        }
        if(curretSong!!.isLiked){
            binding.songLike.setImageResource(R.drawable.full_heart)
        }
        Toast.makeText(baseContext,songsList.size.toString() , Toast.LENGTH_SHORT).show()
        if (position != -1 && songsList.isNotEmpty()) {
            stratService(MusicPlayService.Actions.PLAY)
        }
    }


    private fun stratService(action: MusicPlayService.Actions) {
        Intent(this,MusicPlayService::class.java).also {
            it.action = action.name
            startService(it)
        }
    }
    private fun setSongData(song: MusicFile) {
        song.musicImage?.let { image ->
            binding.musicPlayingImage.setImageBitmap(image)
            Palette.from(image).generate { palette ->
                val vibrantColor = palette?.getVibrantColor(getColorResource(R.color.Primary))
                binding.musicTitle.setTextColor(vibrantColor ?: getColorResource(R.color.Primary))
                binding.musicArtestName.setTextColor(vibrantColor ?: getColorResource(R.color.Primary))

            }
        } ?: run {
            // Set default image and title color if no music image is available
            binding.musicPlayingImage.setImageResource(R.drawable.small_place_holder_image)
        binding.musicArtestName.setTextColor(getColorResource(R.color.Primary))
            binding.musicTitle.setTextColor(getColorResource(R.color.Primary))

        }

        // Update song title
        binding.musicTitle.text = song.title
        binding.musicArtestName.text = song.artist

        // Update song duration and progress
        val durationInSeconds = mediaPlayer?.duration?.div(1000) ?: 0
        binding.musicProgress.max = durationInSeconds
        binding.originalDuration.text = formatOriginalTime(durationInSeconds)
    }

    // Helper function to retrieve color resources safely
    private fun getColorResource(colorId: Int): Int {
        return resources.getColor(colorId, theme)
    }

    // Helper function to format time in MM:SS
    private fun formatOriginalTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
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
                    binding.palyPauseBtn.setImageResource(R.drawable.play)
                }
                "prev" ->{
                    setSongData(curretSong!!)
                    binding.palyPauseBtn.setImageResource(R.drawable.pause)
                }
                "play" ->{
                    setSongData(curretSong!!)
                    binding.palyPauseBtn.setImageResource(R.drawable.pause)
                }
                "create" ->{
                    setSongData(curretSong!!)
                    binding.palyPauseBtn.setImageResource(R.drawable.pause)
                }
                "next" ->{
                    setSongData(curretSong!!)
                    binding.palyPauseBtn.setImageResource(R.drawable.pause)
                }
                "random" ->{
                    setSongData(curretSong!!)
                }
                "like" ->{
                    binding.songLike.setImageResource(R.drawable.full_heart)
                }
                "dislike" ->{
                    binding.songLike.setImageResource(R.drawable.empty_heart)
                }

            }
        }
    }
    override fun  onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_TRACK_UPDATE)
        registerReceiver(broadcastReceiver, filter)
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}









