package com.example.jamplayer.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import com.example.jamplayer.Activities.ShowAlbumActivity
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbum
import com.example.jamplayer.Activities.ShowAlbumActivity.ShowAlbumDetailsManagerObj.selectedAlbumSongsList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Adapters.AlbumAdapter
import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.databinding.FragmentAlbumsFragmentsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlbumsFragments : Fragment() {
    val albumAdapter by lazy {  AlbumAdapter(1)}

    private var _binding: FragmentAlbumsFragmentsBinding?= null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlbumsFragmentsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialAlbums(view)
        binding.albumsRefreshSwipe.setOnRefreshListener {
            intialAlbums(view)
        }
    }
    private fun intialAlbums(view: View) {
        executor.execute{
            if (albumList.isNotEmpty()) {
                binding.albumsScroll.visibility = View.VISIBLE
                binding.albumNotFoundWraning.visibility = View.GONE
            if(binding.albumsRefreshSwipe.isRefreshing){
                CoroutineScope(Dispatchers.Main).launch {
                    albumList = jamViewModel.getAllAlbums()  as ArrayList
                    albumAdapter.setAlbumsList(albumList)
                    setUpList(view.context)
                }
            }else{
                albumAdapter.setAlbumsList(albumList)
                setUpList(view.context)

            }
            } else {
                binding.albumsScroll.visibility = View.GONE
binding.albumNotFoundWraning.visibility = View.VISIBLE            }

            setAlbumListener(albumAdapter,view.context)
        }

    }

    private fun setUpList(context : Context) {
        binding.albumsList.apply {
            if(settings!!.itemType == "small"){
                layoutManager = LinearLayoutManager(context)
            }else {
                layoutManager = GridLayoutManager(context,2)
            }
            setHasFixedSize(true)
            adapter = albumAdapter
        }
        binding.albumsRefreshSwipe.setRefreshing(false)

    }

    private fun setAlbumListener(
        albumAdapter: AlbumAdapter,
        context: Context,
    ) {
        albumAdapter.setListner(object : AlbumMusicLisntener{
            override fun onAlbumItemClicked(album: Album, albumSongs: ArrayList<MusicFile>) {
              selectedAlbum = album
                selectedAlbumSongsList = albumSongs
                val albumIntent = Intent(context,ShowAlbumActivity::class.java)
                startActivity(albumIntent)
            }
        })

    }

}
