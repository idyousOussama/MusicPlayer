package com.example.jamplayer.Adapters.VedioAdapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.videosList
import com.example.jamplayer.Listeners.VideoListeners.VideosAlbumItemListener
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.Moduls.VideoAlbum
import com.example.jamplayer.R

class VideoAlbumsAdapter (var requestCode : Int) : RecyclerView.Adapter<VideoAlbumsAdapter.VideoAlbumsVH>() {
var videosAlbumsList: ArrayList<VideoAlbum> = ArrayList()
    var albumItemListner : VideosAlbumItemListener? = null
    fun setVideoAlbumsList(videoAlbumsList: ArrayList<VideoAlbum>){
        this.videosAlbumsList   = videoAlbumsList
    }
fun setAlbumItemListener (albumItemListener : VideosAlbumItemListener){
    this.albumItemListner = albumItemListener
}
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VideoAlbumsVH {
        if(settings!!.itemType == "samll" || requestCode == 1) {
            val view  = LayoutInflater.from(p0.context).inflate(R.layout.small_video_album_custom_view,p0,false)
            return VideoAlbumsVH(view)
        }else {
            val view  = LayoutInflater.from(p0.context).inflate(R.layout.video_album_custom_view,p0,false)
            return VideoAlbumsVH(view)
        }
    }
    override fun getItemCount(): Int {
return videosAlbumsList.size    }

    override fun onBindViewHolder(p0: VideoAlbumsVH, p1: Int) {
        val videoAlbum = videosAlbumsList[p1]
        val filteredVideosList = videosList?.filter { it.album == videoAlbum.name } ?: emptyList()
        val firstVideoUri = filteredVideosList.getOrNull(0)?.path?.toUri()
        val secondVideoUri = filteredVideosList.getOrNull(1)?.path?.toUri()
        p0.setVideoAlbum(
            videoAlbum.name,
            filteredVideosList.size.toString(),
            firstVideoUri,
            secondVideoUri
        )
        p0.itemView.setOnClickListener {
            albumItemListner!!.onVideosAlbumItemClicked(videoAlbum.name , filteredVideosList as ArrayList<Video>)
        }
    }

    class VideoAlbumsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumTitle = itemView.findViewById<TextView>(R.id.video_album_title)
        private val albumItemNum = itemView.findViewById<TextView>(R.id.video_album_item_num)
        private val albumItemImg1 = itemView.findViewById<ImageView>(R.id.video_album_img1)
        private val albumItemImg2 = itemView.findViewById<ImageView>(R.id.video_album_img2)
        private val albumItemIcon = itemView.findViewById<ImageView>(R.id.video_album_icon)

        fun setVideoAlbum(title: String, count: String, img1: Uri?, img2: Uri?) {
            albumTitle.text = title
            albumItemNum.text = count

            // Handle first image
            if (img1 != null) {
                albumItemImg1.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(img1)
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.playing_song_place_holder)
                    .error(R.drawable.playing_song_place_holder)
                    .into(albumItemImg1)
            } else {
                albumItemImg1.visibility = View.GONE
            }

            // Handle second image
            if (img2 != null) {
                albumItemImg2.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(img2)
                    .thumbnail(0.2f)
                    .placeholder(R.drawable.playing_song_place_holder)
                    .error(R.drawable.playing_song_place_holder)
                    .into(albumItemImg2)
            } else {
                albumItemImg2.visibility = View.INVISIBLE
            }
            when(title){
                "Instagram" -> {
                    albumItemIcon.setImageResource(R.drawable.instagram_icon)
                }
                "Facebook" -> {
                    albumItemIcon.setImageResource(R.drawable.facebook_icon)
                }
                "WhatsApp" -> {
                    albumItemIcon.setImageResource(R.drawable.whatsap_icon)
                }
                "Camera" -> {
                    albumItemIcon.setImageResource(R.drawable.camera_icon)
                }
                else -> {
                    albumItemIcon.setImageResource(R.drawable.folder_icon_img)

                }
            }
        }
    }
}