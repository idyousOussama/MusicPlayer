package com.example.jamplayer.Fragments


import android.content.Intent
import android.content.res.Configuration

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.MainActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import com.example.jamplayer.Adapters.VedioAdapters.SelectVideosAdapter

import com.example.jamplayer.Listeners.VideoListeners.SelectVideosListener

import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.R

import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.MusicPlayService

import com.example.jamplayer.databinding.FragmentHiddenVideosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HiddenVideosFragment : Fragment() {
    private var _binding: FragmentHiddenVideosBinding?= null
    private val binding get() = _binding!!

    var checkedHiddenVideosList: ArrayList<Video> = ArrayList()
    private lateinit var hiddenVideos: ArrayList<Video>
    private var counter = 0
    val hiddenVideosAdapter  = SelectVideosAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHiddenVideosBinding.inflate(inflater, container, false)
        val view = binding.root
    return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unHideVideos()
        searchingUnhiddenVideo()
        selectAllHiddenVideos()
        CoroutineScope(Dispatchers.Main).launch {
            hiddenVideos = videosList.filter { it.isHide } as ArrayList
            setHiddenVideos(hiddenVideos)
        }
    }
    private fun selectAllHiddenVideos() {
        binding.selectAllVideoBox.setOnClickListener{
            if (binding.selectAllVideoBox.isChecked){
                checkedHiddenVideosList.clear()
                for (item in hiddenVideos){
                    item.isChecked = true
                    checkedHiddenVideosList.add(item)
                }
                enableUnHideBtn()
            }else if (!binding.selectAllVideoBox.isChecked){
                for (item in hiddenVideos){
                    item.isChecked = false
                    checkedHiddenVideosList.remove(item)
                }
                disableUnHideBtn()
            }
            setHiddenVideos(hiddenVideos)

        }
    }
    private fun unHideVideos() {
        binding.unHideVidesBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                for(item in checkedHiddenVideosList){
                    if(item != null){
                        jamViewModel.upDateVideosHideById(item.id, false)
                    }
                    counter++
                    if (counter == checkedHiddenVideosList.size){
                        movetToMainActivity()
                    }
                }
            }
        }
    }
    private fun movetToMainActivity() {
        val mainIntent = Intent(requireContext(), MainActivity::class.java)
        mainIntent.putExtra("VideoIsDeleted",true)
        startActivity(mainIntent)
    }

    private fun setHiddenVideos(hiddenVideoList: ArrayList<Video>) {
        if (this.hiddenVideos.isNotEmpty()){
            hiddenVideosAdapter.setVideoList(hiddenVideoList)
            binding.hiddenVideoList.apply {
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
                adapter = hiddenVideosAdapter

            }
        }
        getCheckedSongItem(hiddenVideosAdapter)
    }
    private fun getCheckedSongItem(hiddenVideosAdapter: SelectVideosAdapter) {
        hiddenVideosAdapter.setVideoListener(object : SelectVideosListener {
            override fun onItemCheckChanged(hiddenVideo: Video, toAdd: Boolean) {
                if(toAdd ){
                    checkedHiddenVideosList.add(hiddenVideo)
                    if(!binding.unHideVidesBtn.isEnabled){
                        enableUnHideBtn()
                    }else if(checkedHiddenVideosList.size == hiddenVideos.size){
                        binding.selectAllVideoBox.isChecked = true
                    }else if (!binding.unHideVidesBtn.isEnabled && checkedHiddenVideosList.size == hiddenVideos.size){
                        enableUnHideBtn()
                        binding.selectAllVideoBox.isChecked = true
                    }
                }else{
                    if (checkedHiddenVideosList.isNotEmpty()){
                        binding.selectAllVideoBox.isChecked = false
                        checkedHiddenVideosList.remove(hiddenVideo)
                        if (checkedHiddenVideosList.isEmpty()){
                            disableUnHideBtn()
                        }
                    }
                }
            }
            override fun onHiddenSongLongClikcked(selectedVideo: Video) {
                if(mediaPlayer != null && mediaPlayer!!.isPlaying){
                    startService(MusicPlayService.Actions.PAUSE)
                }
            }
        })
    }
    private fun startService(action: MusicPlayService.Actions) {
        Intent(requireContext(), MusicPlayService::class.java).also {
            it.action = action.name
            requireContext().startService(it)
        }
    }
    private fun enableUnHideBtn() {
        binding.unHideVidesBtn.isEnabled = true
        binding.unHideVidesBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        @Suppress("DEPRECATION")
        binding.unHideVidesBtn.setTextColor(resources.getColor(R.color.white))
    }
    private fun disableUnHideBtn() {
        binding.unHideVidesBtn.isEnabled = false
        binding.unHideVidesBtn.setBackgroundResource(R.drawable.disable_btns_backround)
        @Suppress("DEPRECATION")
        binding.unHideVidesBtn.setTextColor(resources.getColor(R.color.disable_text_color))
    }

    private fun searchingUnhiddenVideo(){
        binding.hiddenVideoInputSearchBar.addTextChangedListener {
            val searchText = binding.hiddenVideoInputSearchBar.text.toString()
            binding.selectAllVideoBox.visibility = View.GONE
            if(searchText.isNotEmpty()){
                executor.execute {
                    val filteredHiddenVideosList = hiddenVideos.filter {
                        it.title.contains(searchText, ignoreCase = true)
                    }
                    lifecycleScope.launch {
                        setHiddenVideos(filteredHiddenVideosList as ArrayList)
                    }
                }
            }else{
                setHiddenVideos(hiddenVideos)
                binding.selectAllVideoBox.visibility = View.VISIBLE
            }
        }
    }

}