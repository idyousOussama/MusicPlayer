package com.example.jamplayer.Fragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.example.jamplayer.Fragments.HiddenSongFragment.hiddenSongsManager.hiddenSongMediaPlayer

import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.MainActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Adapters.SelectSongsAdapter
import com.example.jamplayer.Listeners.SelecteSongsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.ACTION_TRACK_UPDATE
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.MusicPlayService
import com.example.jamplayer.databinding.FragmentHiddenSongBinding
import com.example.jamplayer.databinding.FragmentPlayListsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HiddenSongFragment : Fragment() {
    private var _binding: FragmentHiddenSongBinding?= null
    private val binding get() = _binding!!
    lateinit var playPauseBtn : ImageView
    object hiddenSongsManager {
        var  hiddenSongMediaPlayer : MediaPlayer? = null
    }
    var handler = Handler(Looper.getMainLooper())
    var hiddenCurrentPosition : Int = 0

    var checkedHiddenSongsList: ArrayList<MusicFile> = ArrayList()
    private lateinit var hiddenSongs: ArrayList<MusicFile>
    private lateinit var processDialog: Dialog
    private var counter = 0
    val hiddenSongsAdapter  = SelectSongsAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHiddenSongBinding.inflate(inflater, container, false)
        val view = binding.root
        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unHideSongs()
        searchingUnhiddenSongs()
        selectAllHiddenSongs()
        CoroutineScope(Dispatchers.Main).launch {
            hiddenSongs = jamViewModel.getHiddenSongs() as ArrayList<MusicFile>
            setHiddenSongs(hiddenSongs)
        }
    }
    private fun selectAllHiddenSongs() {
        binding.selectAllBox.setOnClickListener{
            if (binding.selectAllBox.isChecked){
                checkedHiddenSongsList.clear()
                for (item in hiddenSongs){
                    item.isChecked = true
                    checkedHiddenSongsList.add(item)
                }
                enableUnHideBtn()
            }else if (!binding.selectAllBox.isChecked){
                for (item in hiddenSongs){
                    item.isChecked = false
                    checkedHiddenSongsList.remove(item)
                }

                disableUnHideBtn()
            }
            setHiddenSongs(hiddenSongs)

        }
    }

    private fun unHideSongs() {
        binding.unHideSongsBtn.setOnClickListener {
            alertProcessingDialog()
            for(item in checkedHiddenSongsList){
                if(item != null){
                    jamViewModel.upDateSongsById(item.id, false)
                }
                counter++
                if (counter == checkedHiddenSongsList.size){
                    processDialog.dismiss()
                    movetToMainActivity()
                }
            }
        }
    }

    private fun movetToMainActivity() {
        val mainIntent = Intent(requireContext(), MainActivity::class.java)
        startActivity(mainIntent)
    }

    private fun setHiddenSongs(hiddenSongList: ArrayList<MusicFile>) {
        if (this.hiddenSongs.isNotEmpty()){
            hiddenSongsAdapter.setSongList(hiddenSongList)
            binding.hiddenSongList.apply {
                layoutManager = if (settings?.itemType == "small") {
                    LinearLayoutManager(context)
                }

                else{
                    if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        GridLayoutManager(context, 2)

                    }else {
                        GridLayoutManager(context, 4)

                    }
                }
                setHasFixedSize(true)
                adapter = hiddenSongsAdapter

            }        }
        getCheckedSongItem(hiddenSongsAdapter)
    }
    private fun getCheckedSongItem(hiddenSongsAdapter: SelectSongsAdapter) {
        hiddenSongsAdapter.setSongListener(object : SelecteSongsListener {
            override fun onItemCheckChanged(hiddenSong: MusicFile, toAdd: Boolean) {
                if(toAdd ){
                    checkedHiddenSongsList.add(hiddenSong)
                    if(!binding.unHideSongsBtn.isEnabled){
                        enableUnHideBtn()
                    }else if(checkedHiddenSongsList.size == hiddenSongs.size){
                        binding.selectAllBox.isChecked = true
                    }else if (!binding.unHideSongsBtn.isEnabled && checkedHiddenSongsList.size == hiddenSongs.size){
                        enableUnHideBtn()
                        binding.selectAllBox.isChecked = true
                    }
                }else{
                    if (checkedHiddenSongsList.isNotEmpty()){
                        binding.selectAllBox.isChecked = false
                        checkedHiddenSongsList.remove(hiddenSong)
                        if (checkedHiddenSongsList.isEmpty()){
                            disableUnHideBtn()
                        }
                    }
                }
            }
            override fun onHiddenSongLongClikcked(selectedSong: MusicFile) {
                if(mediaPlayer != null && mediaPlayer!!.isPlaying){
                    startService(MusicPlayService.Actions.PAUSE)
                }
                showPlayingHiddenSongDialog(selectedSong)
            }
        })
    }
    private fun startService(action: MusicPlayService.Actions) {
        Intent(requireContext(), MusicPlayService::class.java).also {
            it.action = action.name
            requireContext().startService(it)
        }
    }
    private fun showPlayingHiddenSongDialog(selectedHiddenSong: MusicFile) {
        val playHiddenSongView = LayoutInflater.from(requireContext()).inflate(R.layout.progress_song_dialog_custom_view, null)
        playPauseBtn = playHiddenSongView.findViewById<ImageView>(R.id.play_pause_hidden_song_Btn)
        val cancelBtn = playHiddenSongView.findViewById<ImageView>(R.id.dismess_play_hidden_song_dialog_Btn)
        playHiddenSongView.findViewById<TextView>(R.id.hidden_song_dilog_song_name).apply {
            text = selectedHiddenSong.title
        }
        val hiddenSongProgress = playHiddenSongView.findViewById<SeekBar>(R.id.hidden_song_dilog_progress_bar)
        playHiddenSongView .findViewById<TextView>(R.id.hidden_song_dilog_song_artist_name).text = selectedHiddenSong.artist
        val playHiddenSongDialog = Dialog(requireContext())

        playHiddenSongDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        playHiddenSongDialog.setContentView(playHiddenSongView)
        hiddenSongMediaPlayer = MediaPlayer.create(requireContext(), selectedHiddenSong.path.toUri())
        val durationInSeconds = hiddenSongMediaPlayer?.duration?.div(1000) ?: 0
        hiddenSongProgress.max = durationInSeconds

        hiddenSongMediaPlayer?.start()
        updateSeekBar(hiddenSongProgress,playPauseBtn)
        seekBarListener(hiddenSongProgress)
        cancelBtn.setOnClickListener {
            hiddenSongMediaPlayer?.release()
            hiddenSongMediaPlayer = null
            handler.removeCallbacksAndMessages(null)
            playHiddenSongDialog.dismiss()
        }

        playPauseBtn.setOnClickListener {
            hiddenSongMediaPlayer?.let { mediaPlayer ->
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    playPauseBtn.setImageResource(R.drawable.play)
                } else {
                    if(PlayingMusicManager.mediaPlayer != null && PlayingMusicManager.mediaPlayer!!.isPlaying){
                        startService(MusicPlayService.Actions.PAUSE)
                    }
                    mediaPlayer.start()
                    playPauseBtn.setImageResource(R.drawable.pause)
                }
            }
        }

        playHiddenSongDialog.setOnDismissListener {
            hiddenSongMediaPlayer?.release()
            hiddenSongMediaPlayer = null
            handler.removeCallbacksAndMessages(null)
        }

        playHiddenSongDialog.show()
    }
    private fun seekBarListener(hiddenSongProgress: SeekBar?) {
        hiddenSongProgress!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (hiddenSongMediaPlayer != null && fromUser) {
                    hiddenSongMediaPlayer?.seekTo(progress * 1000)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                seekBar?.thumb?.alpha =255
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
    private fun updateSeekBar(songSeekBar: SeekBar, playPauseBtn : ImageView) {
        handler.post(object : Runnable {
            override fun run() {
                hiddenSongMediaPlayer?.let { mediaPlayer ->
                    val currentPosition = mediaPlayer.currentPosition / 1000
                    songSeekBar.progress = currentPosition

                    if (currentPosition >= mediaPlayer.duration / 1000) {
                        songSeekBar.progress = 0
                        playPauseBtn.setImageResource(R.drawable.play)
                    }
                }
                handler.postDelayed(this, 1000)
            }
        })
    }
    private fun enableUnHideBtn() {
        binding.unHideSongsBtn.isEnabled = true
        binding.unHideSongsBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        @Suppress("DEPRECATION")
        binding.unHideSongsBtn.setTextColor(resources.getColor(R.color.white))
    }
    private fun disableUnHideBtn() {
        binding.unHideSongsBtn.isEnabled = false
        binding.unHideSongsBtn.setBackgroundResource(R.drawable.disable_btns_backround)
        @Suppress("DEPRECATION")
        binding.unHideSongsBtn.setTextColor(resources.getColor(R.color.disable_text_color))
    }
    fun alertProcessingDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.processing_dialog_custom,null)
        val processText =dialogView.findViewById<TextView>(R.id.dialog_process_text)
        processDialog = Dialog(requireContext())
        processDialog.setContentView(dialogView)
        processDialog.setCancelable(false)
        processText.setText(R.string.Unhiding_text)
        processDialog.create()
    }
    private fun searchingUnhiddenSongs(){
        binding.hiddenInputSearchBar.addTextChangedListener {
            val searchText = binding.hiddenInputSearchBar.text.toString()
            binding.selectAllBox.visibility = View.GONE
            if(searchText.isNotEmpty()){
                executor.execute {
                    val filteredHiddenSongList = hiddenSongs.filter {
                        it.title.contains(searchText, ignoreCase = true) || it.album.contains(searchText, ignoreCase = true)
                    }
                    lifecycleScope.launch {
                        setHiddenSongs(filteredHiddenSongList as ArrayList)
                    }
                }
            }else{
                setHiddenSongs(hiddenSongs)
                binding.selectAllBox.visibility = View.VISIBLE
            }
        }
    }
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("hidden_data_key")
            when(data){
                "pause"->{
                    playPauseBtn.setImageResource(R.drawable.play)
                }
            }
        }
    }
    override fun  onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_TRACK_UPDATE)

        requireContext().registerReceiver(broadcastReceiver, filter)
    }
}