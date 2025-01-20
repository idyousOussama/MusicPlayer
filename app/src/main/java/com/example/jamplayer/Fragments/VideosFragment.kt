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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.HIDDEN_FILE_FRAGMENT_RES_CODE
import com.example.jamplayer.Activities.Songs.HiddenFilesActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import com.example.jamplayer.Activities.Vedios.PlayVideosActivity

import com.example.jamplayer.Activities.Vedios.VideoAlbums
import com.example.jamplayer.Adapters.VedioAdapters.VideoAdapter
import com.example.jamplayer.Listeners.VideoListeners.VideoItemListener
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoTable
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.selectedVideoList
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPosition
import com.example.jamplayer.databinding.FragmentVideosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VideosFragment : Fragment() {
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
private var isNotRotate = true
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
        fetchAllVideos()
        setVideoItemListener()
binding.videoFoldersIcon.setOnClickListener {
navigateToNewActivity(VideoAlbums::class.java)
}


    }
    private fun getHiddenVideos() {
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
    private fun fetchAllVideos() {
        videosList.clear()
        val videoList = ArrayList<Video>()
        val newAddedVieos : ArrayList<Video> = ArrayList()
        val videoUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
       CoroutineScope(Dispatchers.Main).launch{
           val projection = arrayOf(
               MediaStore.Video.Media._ID,
               MediaStore.Video.Media.DATA,
               MediaStore.Video.Media.TITLE,
               MediaStore.Video.Media.DURATION,
               MediaStore.Video.Media.DISPLAY_NAME,
               MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
               MediaStore.Video.Media.BUCKET_ID,
           )
           requireContext().contentResolver.query(
               videoUri, projection, null, null,
               MediaStore.Video.Media.DATE_ADDED + " DESC"
           )?.use { cursor ->
               while (cursor.moveToNext()) {
                   val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                   val title =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                           ?: "Unknown Title"
                   val duration =
                       cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                   val album =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                           ?: "Unknown Album"
                   val displayName =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                           ?: "Unknown"
                   val path =
                       cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                           ?: "Unknown"
                     val dataUri =
                       ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                   var savedVideo = jamViewModel.getVideoById(id.toString())
                   if(savedVideo != null) {
                       videosList.add(
                           Video(
                               id.toString(),
                               savedVideo.title,
                               dataUri.toString(),
                               displayName,
                               duration,
                               album,path,
                               false,
                               0,
                               savedVideo.isHide,false)
                       )
                   }else {
                       if(duration > 6000 ){
                           savedVideo = VideoTable(id.toString(),title,false)
                           jamViewModel.insertNewVideo(VideoTable(id.toString(),title,false))
                       }else {
                           savedVideo = VideoTable(id.toString(),title,true)
                           jamViewModel.insertNewVideo(VideoTable(id.toString(),title,true))
                       }
                       videosList.add(
                           Video(
                               id.toString(),
                               title,
                               dataUri.toString(),
                               displayName,
                               duration,
                               album,path,
                               false,
                               0,savedVideo.isHide,false)
                       )
                       newAddedVieos.add( Video(
                           id.toString(),
                           title,
                           dataUri.toString(),
                           displayName,
                           duration,
                           album,path,
                           false,
                           0,savedVideo.isHide,false))
                   }
               }
           }
           if (videosList.size == 1) {
               binding.vediosNumber.text =
                   videoList.size.toString() + " " + requireContext().getString(R.string.Video_text)
           } else if (videoList.isEmpty()) {
               binding.vediosNumber.visibility = View.GONE
           } else {
               binding.vediosNumber.text =
                   videoList.size.toString() + " " + requireContext().getString(R.string.Videos_text)
           }
           videoAdapter.setVideosList(videosList.filter { !it.isHide} as ArrayList<Video>)

           setUpRecyclerVideo()

       }
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

        getHiddenVideos()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
isNotRotate = false
    }
}