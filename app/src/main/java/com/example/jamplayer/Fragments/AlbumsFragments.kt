package com.example.jamplayer.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import com.example.jamplayer.Activities.ShowAlbumActivity
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Adapters.AlbumAdapter
import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.R


class AlbumsFragments : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_albums_fragments, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val albumRV = view.findViewById<RecyclerView>(R.id.albumsList)


        // Fetch albums in a background thread
        executor.execute{
            val albumAdapter = AlbumAdapter()
            if (albumList.isNotEmpty()) {
                albumAdapter.setAlbumsList(albumList)
                albumRV.layoutManager = LinearLayoutManager(view.context)
                albumRV.setHasFixedSize(true)
                albumRV.adapter = albumAdapter

            } else {
            }
            setAlbumListener(albumAdapter,view.context)
        }

    }

    private fun setAlbumListener(
        albumAdapter: AlbumAdapter,
        context: Context,

    ) {
        albumAdapter.setListner(object : AlbumMusicLisntener{
            override fun onAlbumItemClicked(position: Int) {
                val albumIntent = Intent(context,ShowAlbumActivity::class.java)
                albumIntent.putExtra("albumPosition",position)
                startActivity(albumIntent)
            }

        })

    }

}
