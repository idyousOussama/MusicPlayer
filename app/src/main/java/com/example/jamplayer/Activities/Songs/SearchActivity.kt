package com.example.jamplayer.Activities.Songs

import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbum
import com.example.jamplayer.Activities.Songs.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbumSongsList
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity.showPlaylistmanager.playList
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity.showPlaylistmanager.playlistSongs
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.playLists
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import com.example.jamplayer.Activities.Vedios.PlayVideosActivity
import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity
import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity.showVideoAlbumManager.showAlbumsVideosList
import com.example.jamplayer.Activities.Vedios.ShowVideoAlbumsVideosActivity.showVideoAlbumManager.videosAlbumTitle
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.currentPosition
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.position
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.random
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.songsList

import com.example.jamplayer.Adapters.AlbumAdapter
import com.example.jamplayer.Adapters.AudiosAdapter
import com.example.jamplayer.Adapters.PlaylistAdapter
import com.example.jamplayer.Adapters.VedioAdapters.VideoAdapter
import com.example.jamplayer.Adapters.VedioAdapters.VideoAlbumsAdapter
import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Listeners.PlayListItemListener
import com.example.jamplayer.Listeners.VideoListeners.VideoItemListener
import com.example.jamplayer.Listeners.VideoListeners.VideosAlbumItemListener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoAlbum
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.mediaPlayer
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.selectedVideoList
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoCurrentPosition
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoMediaPlayer
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videoPosition
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.videosAlbums
import com.example.jamplayer.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    lateinit var binding : ActivitySearchBinding
    private val ioScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
        showIfVideosAlbumIsNull()
        setSearch()
        initbackBtn()
    }

    private fun showIfVideosAlbumIsNull() {
        if(videosAlbums.isEmpty()) {
            videosAlbums = getVideoAlbums()
        }
    }

    private fun initbackBtn() {
        binding.searchGoBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun setSearch() {
        binding.inputSearchBar.addTextChangedListener {
            val searchText = binding.inputSearchBar.text.toString()
            if (searchText.isNotEmpty()) {
                binding.searchScroll.visibility = View.GONE
                binding.searchProgress.visibility = View.VISIBLE
                executor.execute {
                    val filteredSongList = unHideSong.filter {
                        it.title.contains(searchText, ignoreCase = true) || it.album.contains(searchText, ignoreCase = true)
                    }
                    val filteredAlbumList = albumList.filter {
                        it.name!!.contains(searchText, ignoreCase = true)
                    }

                    val filteredPlayList = playLists.filter {
                        it.title!!.contains(searchText, ignoreCase = true)
                    }
                    val filtterVideoList = videosList.filter {
                        it.title.contains(searchText ,  ignoreCase = true)

                    }
                    val filtterVideoAlbumList = videosAlbums.filter {
                        it.name.contains(searchText ,  ignoreCase = true)

                    }

                    lifecycleScope.launch {
                        binding.searchProgress.visibility = View.GONE

                        if (filteredSongList.isEmpty() && filteredAlbumList.isEmpty() && filteredPlayList.isEmpty() && filtterVideoList.isEmpty() && filtterVideoAlbumList.isEmpty()) {
                            binding.searchScroll.visibility = View.GONE
                            binding.noResultLayout.visibility = View.VISIBLE
                        } else {
                            binding.noResultLayout.visibility = View.GONE
                            binding.searchScroll.visibility = View.VISIBLE

                            handleFilteredList(
                                filteredAlbumList,
                                binding.searchAlbumText,
                                binding.searchAlbumLayout,
                                getString(R.string.Albums_text)
                            ) { setFilteredAlbumsList(filteredAlbumList as ArrayList) }

                            handleFilteredList(
                                filteredSongList,
                                binding.searchSongsText,
                                binding.searchSongsLayout,
                                    getString(R.string.Songs_text)
                            ) { setFilteredSongsList(filteredSongList as ArrayList) }

                            handleFilteredList(
                                filteredPlayList,
                                binding.searchPlayListText,
                                binding.searchPlayListLayout,
                                getString(R.string.play_lists_text)
                            ) { setFilteredPlaylist(filteredPlayList as ArrayList) }
                            handleFilteredList(
                                filtterVideoList,
                                binding.searchVideoText,
                                binding.searchVideoLayout,
                                getString(R.string.videos_text)
                            ) { setFilteredVideosList(filtterVideoList as ArrayList) }
                            handleFilteredList(
                                filtterVideoAlbumList,
                                binding.searchVideoAlbumText,
                                binding.searchVideoAlbumsLayout,
                                getString(R.string.videos_Albums_text)
                            ) { setFilteredVideosAlbumsList(filtterVideoAlbumList as ArrayList) }
                        }
                    }
                }
            } else {
                binding.searchScroll.visibility = View.GONE
                binding.searchProgress.visibility = View.GONE
            }
        }

    }
    private fun handleFilteredList(
        list: List<Any>,
        textView: TextView,
        linarLayout: LinearLayout,
        label: String,
        updateList: (ArrayList<Any>) -> Unit
    ) {
        if (list.isNotEmpty()) {
            updateList(ArrayList(list))
            textView.visibility = View.VISIBLE
            linarLayout.visibility = View.VISIBLE
            textView.text = "$label (${list.size})"
        } else {
            textView.visibility = View.GONE
            linarLayout.visibility = View.GONE
        }
    }
    private fun setFilteredAlbumsList(filteredAlbums :ArrayList<Album>) {
        val albumsAdapter = AlbumAdapter(1)

        binding.searchAlbumRV.apply {
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
            adapter = albumsAdapter

        }
            albumsAdapter.setAlbumsList(filteredAlbums)


        albumsAdapter.setListner(object : AlbumMusicLisntener {
            override fun onAlbumItemClicked(album: Album, albumSongs: ArrayList<MusicFile>) {
                selectedAlbum = album
                selectedAlbumSongsList = albumSongs
                val albumIntent = Intent(baseContext, ShowAlbumActivity::class.java)
                startActivity(albumIntent)
            }
        })
    }
    private fun setFilteredSongsList(filteredSongs: ArrayList<MusicFile>) {

        val songsAdapter = AudiosAdapter(1)

            songsAdapter.setMusicFile(filteredSongs)
        binding.searchSongRV.apply {
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
            adapter = songsAdapter

        }

        songsAdapter.setListner(object : MusicFileItemsListener{
            override fun onItemClickListner(mFile: MusicFile, position: Int) {
                val currentMediaPlayer = BaseApplication.PlayingMusicManager.mediaPlayer
                currentMediaPlayer?.let {
                    if (it.isPlaying) {
                        addPlayingTime()
                        mediaPlayer!!.pause()
                        mediaPlayer!!.release()
                    } else if (!it.isPlaying && mediaPlayer != null) {
                        addPlayingTime()
                        mediaPlayer!!.release()
                    }

                }
                if(mFile != null){
                    curretSong = mFile
                    val songList : ArrayList<MusicFile> =ArrayList()
                    songList.add(mFile)
                    setSongsListForPlaying(0, songList,false)
                }
            }
        })
    }
    private fun setFilteredVideosList(filteredVideos: ArrayList<Video>) {

        val videoAdapter = VideoAdapter(0)

        videoAdapter.setVideosList(filteredVideos)
        binding.searchVideoRV.apply {
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

        videoAdapter.setVideoListener(object : VideoItemListener{
            override fun onVideoItemClicked(videoList: ArrayList<Video>, position: Int) {
                selectedVideoList = filteredVideos
                videoPosition = position
                navigateToNewActivity(PlayVideosActivity::class.java)
            }
        })
    }
    private fun setFilteredVideosAlbumsList(filteredVideosAlbums: ArrayList<VideoAlbum>) {
        val videoAlbumAdapter = VideoAlbumsAdapter(1)
        videoAlbumAdapter.setVideoAlbumsList(filteredVideosAlbums)
        binding.searchVideoAlbumRV.apply {
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
            adapter = videoAlbumAdapter

        }

        videoAlbumAdapter.setAlbumItemListener(object : VideosAlbumItemListener {
            override fun onVideosAlbumItemClicked(title: String, VideosList: ArrayList<Video>) {
                showAlbumsVideosList = VideosList
                videosAlbumTitle = title
                navigateToNewActivity(ShowVideoAlbumsVideosActivity::class.java)
            }

        })
    }

    private fun setSongsListForPlaying(positionz: Int, songList : ArrayList<MusicFile>, isRandom: Boolean) {
        songsList = songList
        random = isRandom
        position =  positionz
        val intent = Intent(baseContext, PlayingActivity :: class.java)
        startActivity(intent)
    }



    private fun navigateToNewActivity(newActivty: Class<*>) {
val newActivtyIntent = Intent(baseContext,newActivty)
        startActivity(newActivtyIntent)
    }


    private fun setFilteredPlaylist(filteredPlaylist: ArrayList<PlayList>) {
        val playListAdapter =PlaylistAdapter(1)
        playListAdapter.setPlayLists(filteredPlaylist)
        binding.searchPlayListRV.apply {
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
            adapter = playListAdapter

        }

        playListAdapter.setPlayListItemListener(object : PlayListItemListener{
            override fun onPlayListItemClicked(
                selectedPlayList: PlayList,
                playListSongs: ArrayList<MusicFile>
            ) {
                if(mediaPlayer!!.isPlaying){
                    addPlayingTime()
                } else if(videoMediaPlayer!!.isPlaying) {
                    addPlayingVideoTime()
                }
                playlistSongs = playListSongs
                playList = selectedPlayList
                navigateToNewActivty(ShowPlaylistActivity::class.java)            }
        })
    }
    private fun navigateToNewActivty(newActivity: Class<ShowPlaylistActivity>) {
val newActivityInntent = Intent(baseContext , newActivity)
        startActivity(newActivityInntent)
    }


    private fun addPlayingVideoTime() {
        ioScope.launch{
            jamViewModel.setVideoPlayingTime(videoCurrentPosition)

            videoMediaPlayer?.release()
            videoMediaPlayer = null


        }    }

    private fun addPlayingTime() {
        ioScope.launch{
            jamViewModel.setPlayingTime(currentPosition)

                BaseApplication.PlayingMusicManager.mediaPlayer?.release()
            BaseApplication.PlayingMusicManager.mediaPlayer = null


        }
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
    private fun getVideoAlbums(): ArrayList<VideoAlbum> {
        val albums = mutableMapOf<String, VideoAlbum>()
        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        )

        val sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC"

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        ) ?: run {
            Log.e("VideoAlbums", "Query returned null")
            return ArrayList()
        }

        cursor.use {
            val bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            if (bucketIdColumn == null || bucketNameColumn == null) {
                Log.e("VideoAlbums", "Required columns are missing in MediaStore")
                return ArrayList()
            }

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdColumn) ?: continue
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown Album"

                val album = albums[bucketId] ?: VideoAlbum(bucketId, bucketName, 0)
                albums[bucketId] = album.copy(videoCount = album.videoCount + 1)
            }
        }

        return ArrayList(albums.values)
    }

}


