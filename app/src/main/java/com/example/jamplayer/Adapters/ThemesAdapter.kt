package com.example.jamplayer.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.albumList
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.unHideSong

import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R

class ThemesAdapter : RecyclerView.Adapter<ThemesAdapter.ThemesViewHolder>() {
    private var themeViewList: ArrayList<Int> = ArrayList()  // ArrayList of layout resource IDs

    // Set the theme views (layout resource IDs)
    fun setThemeViews(themeViewList: ArrayList<Int>) {
        this.themeViewList = themeViewList
        notifyDataSetChanged()  // Notify adapter that the data has changed
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ThemesViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.activity_theme_views_container_custom, p0, false)
        return ThemesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return themeViewList.size
    }

    override fun onBindViewHolder(p0: ThemesViewHolder, p1: Int) {
        val layoutResourceId = themeViewList[p1]  // Get the layout resource ID at position p1
        p0.addLayout(layoutResourceId)
    }

    // ViewHolder class
    class ThemesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fragmentContainer: RelativeLayout = itemView.findViewById(R.id.included)

        // Add layout to the fragment container dynamically
        fun addLayout(view: Int) {
             if(view == R.layout.songs_theme_custom_fragment){
                 val inflatedView = LayoutInflater.from(itemView.context).inflate(view, fragmentContainer, false)
                 fragmentContainer.addView(inflatedView)
                 setSongList(inflatedView.findViewById<RecyclerView>(R.id.theme_songs_RV), itemView.context)
             }else if(view == R.layout.albums_theme_custom_fragment){
                 val inflatedView = LayoutInflater.from(itemView.context).inflate(view, fragmentContainer, false)
                 fragmentContainer.addView(inflatedView)
                 setAlbumList(inflatedView.findViewById<RecyclerView>(R.id.theme_album_RV), itemView.context)

             }else if(view == R.layout.show_album_theme_custom){
                 val inflatedView = LayoutInflater.from(itemView.context).inflate(view, fragmentContainer, false)
                 fragmentContainer.addView(inflatedView)
val randomAlbum = albumList.random()
val rendomAlbumSong: ArrayList<MusicFile> = unHideSong.filter { it.album == randomAlbum.name } as ArrayList
                 setThemeShowAlbum(inflatedView.findViewById(R.id.theme_album_songRV),rendomAlbumSong,itemView.context)
if(rendomAlbumSong.isNotEmpty()){
    if(rendomAlbumSong.get(0).musicImage != null){
        inflatedView.findViewById<ImageView>(R.id.theme_show_album_Image).setImageBitmap(rendomAlbumSong[0].musicImage)
    }else{
        inflatedView.findViewById<ImageView>(R.id.theme_show_album_Image).setImageResource(R.drawable.small_place_holder_image)
    }
}else{
    inflatedView.findViewById<ImageView>(R.id.theme_show_album_Image).setImageResource(R.drawable.small_place_holder_image)
}

                 inflatedView.findViewById<TextView>(R.id.theme_show_album_name).setText(randomAlbum.name)
                 if(rendomAlbumSong.size <= 1){
                     inflatedView.findViewById<TextView>(R.id.theme_show_album_songNum).setText(rendomAlbumSong.size.toString() + "Song")
                 }else{
                     inflatedView.findViewById<TextView>(R.id.theme_show_album_songNum).setText(rendomAlbumSong.size.toString() + "Songs")

                 }

             }

        }
        private fun setThemeShowAlbum(rv : RecyclerView, albumSongList : ArrayList<MusicFile>,context: Context?) {
val themeShowAlbumAdapter = AudiosAdapter(0)
            themeShowAlbumAdapter.setMusicFile(albumSongList)
            rv.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = themeShowAlbumAdapter
            }
        }

        private fun setAlbumList(rv: RecyclerView?, context: Context?) {
            val themeAlbumAdapter = AlbumAdapter(0)
            themeAlbumAdapter.setAlbumsList(albumList)
            rv?.apply {
                if(settings!!.itemType == "small"){
                    layoutManager = LinearLayoutManager(context)
                }else {
                    layoutManager = GridLayoutManager(context,2)
                }
                setHasFixedSize(true)
                adapter = themeAlbumAdapter
            }
        }
        private fun setSongList(rv: RecyclerView?, context : Context) {
                val themeSongAdapter = AudiosAdapter(0)
                themeSongAdapter.setMusicFile(unHideSong)
                rv?.apply {
                    if(settings!!.itemType == "small"){
                        layoutManager = LinearLayoutManager(context)
                    }else {
                        layoutManager = GridLayoutManager(context,2)
                    }
                    setHasFixedSize(true)
                    adapter = themeSongAdapter
                }
        }

    }

}
