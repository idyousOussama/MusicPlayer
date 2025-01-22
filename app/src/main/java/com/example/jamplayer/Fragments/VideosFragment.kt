package com.example.jamplayer.Fragments

import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.HIDDEN_FILE_FRAGMENT_RES_CODE
import com.example.jamplayer.Activities.Songs.HiddenFilesActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import com.example.jamplayer.Activities.Vedios.PlayVideosActivity

import com.example.jamplayer.Activities.Vedios.VideoAlbums
import com.example.jamplayer.Adapters.VedioAdapters.VideoAdapter
import com.example.jamplayer.Listeners.VideoListeners.VideoItemListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoTable
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.selectedVideoList
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPosition
import com.example.jamplayer.Services.ShowNewFileService
import com.example.jamplayer.databinding.FragmentVideosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideosFragment : Fragment() {
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
private var isNotRotate = true
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    private val videoAdapter by lazy {
        VideoAdapter(0)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVideos()
        setVideoItemListener()
        goToShowVideosAlbums()
        setOnRefreshPage()
    }

    private fun setOnRefreshPage() {
        binding.videosRefreshSwipe.setOnRefreshListener {
            startScannVideosAction(ShowNewFileService.ShowNewFileAction.SCANNINGVIDEOS)
            initVideos()
        }
    }

    private fun initVideos() {

        ioScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    videosList = jamViewModel.getAllVideos() as ArrayList<Video>
                    setVideosList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to load songs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.videosRefreshSwipe.setRefreshing(false)
                }
            }
        }
    }
    private fun setVideosList() {
        CoroutineScope(Dispatchers.Main).launch{
          val unHidevideosList = videosList.filter { !it.isHide } as ArrayList<Video>
            if (unHidevideosList.size == 1) {
                binding.vediosNumber.text =
                    unHidevideosList.size.toString() + " " + requireContext().getString(R.string.Video_text)
            } else if (unHidevideosList.isEmpty()) {
                binding.vediosNumber.visibility = View.GONE
            } else {
                binding.vediosNumber.text =
                    unHidevideosList.size.toString() + " " + requireContext().getString(R.string.Videos_text)
            }
            updateVideoList(unHidevideosList)
        }

    }

    private fun updateHiddenVideosInfo() {
        val hiddenVideos = videosList.filter { it.isHide } as ArrayList<Video>
        if(hiddenVideos.isNotEmpty()) {
            binding.showHiddenVideos.text = requireContext().getString(R.string.show_text) + " "+ hiddenVideos.size.toString() + " "+ requireContext().getString(R.string.Hidden_video_text)
        }else{
            binding.showHiddenVideos.visibility = View.GONE
        }
        binding.showHiddenVideos.setOnClickListener {
            val intent = Intent(requireContext(),HiddenFilesActivity::class.java)
            intent.putExtra(HIDDEN_FILE_FRAGMENT_RES_CODE , "hiddenVideos")
            startActivity(intent )
        }
    }
    private fun updateVideoList(videos: ArrayList<Video>) {
        binding.videosRefreshSwipe.setRefreshing(false)
        if (videos.isEmpty()) {
            binding.viedosListRV.visibility = View.GONE
        } else {
            binding.viedosListRV.visibility = View.VISIBLE
            videoAdapter.setVideosList(videos)
            setUpRecyclerVideo()
        }
        updateHiddenVideosInfo()
    }

    private fun goToShowVideosAlbums() {
        binding.videoFoldersIcon.setOnClickListener {
            navigateToNewActivity(VideoAlbums::class.java)
        }
    }

    private fun setVideoItemListener() {
        videoAdapter.setVideoListener(object : VideoItemListener {
            override fun onVideoItemClicked(videoList: ArrayList<Video>, position: Int) {
                selectedVideoList = videoList
                videoPosition = position
                navigateToNewActivity(PlayVideosActivity::class.java)
            }
        })
    }
    private fun navigateToNewActivity(newActivity: Class<*>) {
        val newActivityIntent = Intent(requireContext(),newActivity)
        startActivity(newActivityIntent)
    }


    private fun setUpRecyclerVideo() {

        binding.viedosListRV.apply {
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
            adapter = videoAdapter

        }


    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
isNotRotate = false
    }
    private fun startScannVideosAction(scannFilesAction: ShowNewFileService.ShowNewFileAction) {
        Intent(requireContext(), ShowNewFileService::class.java).also {
            it.action = scannFilesAction.name
            requireContext().startService(it)
        }
    }
}