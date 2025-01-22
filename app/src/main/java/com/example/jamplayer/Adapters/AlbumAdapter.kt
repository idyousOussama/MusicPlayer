package com.example.jamplayer.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.unHideSong

import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R


class AlbumAdapter(var requestCode: Int) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {
 var albumList : ArrayList<Album> = ArrayList()
    lateinit var lestner : AlbumMusicLisntener
    fun setAlbumsList (albumsList : ArrayList<Album>){
        this.albumList = albumsList
    }
 fun setListner(lestner : AlbumMusicLisntener){
     this.lestner = lestner
 }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AlbumViewHolder {
        if(requestCode == 1 ){
            if (settings!!.itemType == "small"){
                val view = LayoutInflater.from(p0.context).inflate(R.layout.small_album_custom_item,p0,false)
                return AlbumViewHolder(view)
            }else {
                val view = LayoutInflater.from(p0.context).inflate(R.layout.big_album_custom_item,p0,false)
                return AlbumViewHolder(view)
            }

        }else{
            if (settings!!.itemType == "small"){
                val view = LayoutInflater.from(p0.context).inflate(R.layout.theme_small_albums_custom_item,p0,false)
                return AlbumViewHolder(view)
            }else {
                val view = LayoutInflater.from(p0.context).inflate(R.layout.theme_big_albums_custom_item,p0,false)
                return AlbumViewHolder(view)
            }

        }

    }
    override fun getItemCount(): Int {
        if(requestCode == 1){
            return albumList.size
        }else{
            if(albumList.size >= 6){
                return 6
            }else{
                return albumList.size
            }
        }
    }
    override fun onBindViewHolder(p0: AlbumViewHolder, p1: Int) {
val albumItem =  albumList.get(p1)
        val albumSonglist = unHideSong.filter { it.album == albumItem.name || it.album == albumItem.artist} as ArrayList
        if(albumSonglist.isNotEmpty()){
            p0.setAlbum(albumItem.name!!,albumSonglist.size.toString(),albumSonglist[0],albumSonglist,requestCode) }

    p0.itemView.setOnClickListener {
        if(requestCode == 1){
            lestner.onAlbumItemClicked(albumItem,albumSonglist)
        }
    }
    }
    class AlbumViewHolder(itemView: View) : ViewHolder(itemView){
private val title = itemView.findViewById<TextView>(R.id.album_name)
private val songNum = itemView.findViewById<TextView>(R.id.album_songNum)
private val albumImage = itemView.findViewById<ImageView>(R.id.album_Image)
        private  val albumSongsNum1 = itemView.findViewById<TextView>(R.id.album_song_num1)
        private  val albumSongsNum2 = itemView.findViewById<TextView>(R.id.album_song_num2)
        fun setAlbum(tit: String, num: String, song: MusicFile? , albumSongsList : ArrayList<MusicFile> , requestCode :Int){
            title.setText(tit)
            songNum.setText(num)
            if(song != null && song.musicImage != null){
                albumImage.setImageBitmap(song.musicImage)
            }else{
                albumImage.setImageResource(R.drawable.songs_list_place_holder)
            }
            if(requestCode == 1) {
                if(albumSongsList.isEmpty()){
                    albumSongsNum1.visibility = View.GONE
                    albumSongsNum2 .visibility = View.GONE
                    songNum.visibility = View.VISIBLE
                }else if (albumSongsList.size == 1){
                    albumSongsNum1.visibility = View.VISIBLE
                    albumSongsNum2 .visibility = View.GONE
                    albumSongsNum1.text =albumSongsList.get(0).title
                }else if (albumSongsList.size > 1) {
                    albumSongsNum1.visibility = View.VISIBLE
                    albumSongsNum2 .visibility = View.VISIBLE
                    albumSongsNum1.text =albumSongsList.get(0).title
                    albumSongsNum2.text =albumSongsList.get(1).title
                }
            }


        }

    }

}