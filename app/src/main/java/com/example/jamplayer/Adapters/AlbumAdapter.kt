package com.example.jamplayer.Adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Listeners.AlbumMusicLisntener
import com.example.jamplayer.Moduls.Album
import com.example.jamplayer.R

class AlbumAdapter: RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {
 var albumList : ArrayList<Album> = ArrayList()
    lateinit var lestner : AlbumMusicLisntener
    fun setAlbumsList (albumsList : ArrayList<Album>){
        this.albumList = albumsList
    }
 fun setListner(lestner : AlbumMusicLisntener){
     this.lestner = lestner
 }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AlbumViewHolder {
val view = LayoutInflater.from(p0.context).inflate(R.layout.album_custom_item,p0,false)

    return AlbumViewHolder(view)
    }

    override fun getItemCount(): Int {
return albumList.size

    }

    override fun onBindViewHolder(p0: AlbumViewHolder, p1: Int) {
val albumItem =  albumList.get(p1)
        val albumSonglist = albumItem.albumSongsList
        p0.setAlbum(albumItem.name!!,albumItem.songsNumber!!,albumSonglist[0].musicImage)
    p0.itemView.setOnClickListener {
        lestner.onAlbumItemClicked(p1)
    }
    }

    class AlbumViewHolder(itemView: View) : ViewHolder(itemView){
private val title = itemView.findViewById<TextView>(R.id.album_name)
private val songNum = itemView.findViewById<TextView>(R.id.album_songNum)
private val albumImage = itemView.findViewById<ImageView>(R.id.album_Image)
        fun setAlbum(tit: String, num: String, image: Bitmap?){
            title.setText(tit)
            songNum.setText(num)
            if(image != null){
                albumImage.setImageBitmap(image)
            }else{
                albumImage.setImageResource(R.drawable.small_place_holder_image )
            }
        }

    }

}