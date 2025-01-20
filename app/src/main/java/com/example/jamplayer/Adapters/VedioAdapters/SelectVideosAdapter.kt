package com.example.jamplayer.Adapters.VedioAdapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings

import com.example.jamplayer.Listeners.VideoListeners.SelectVideosListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.R

class SelectVideosAdapter : RecyclerView.Adapter<SelectVideosAdapter.HiddenVideoVH>() {

    var videosList : ArrayList<Video> = ArrayList()
    lateinit var selecteVideosListener: SelectVideosListener

    fun setVideoListener (selecteVideosListener: SelectVideosListener){
        this.selecteVideosListener = selecteVideosListener
    }


    fun setVideoList (hiddenVideosList : ArrayList<Video>){
        this.videosList = hiddenVideosList
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HiddenVideoVH {
        if(settings!!.itemType == "small"){
            val view = LayoutInflater.from(p0.context).inflate(R.layout.hidden_small_song_custom_item,p0,false)
            return HiddenVideoVH(view)
        }else{
            val view = LayoutInflater.from(p0.context).inflate(R.layout.hidden_big_song_custom_item,p0,false)
            return HiddenVideoVH(view)
        }
    }
    override fun getItemCount(): Int {
        return videosList.size
    }
    override fun onBindViewHolder(p0: HiddenVideoVH, p1: Int) {
        val hiddenVideo = videosList.get(p1)
        p0.setVideo(hiddenVideo.path.toUri(),hiddenVideo.title, hiddenVideo.isChecked)
        p0.checkBox.setOnClickListener {
            if (p0.checkBox.isChecked){
                selecteVideosListener.onItemCheckChanged(hiddenVideo,true)
                hiddenVideo.isChecked = true
            }else{
                selecteVideosListener.onItemCheckChanged(hiddenVideo,false)
                hiddenVideo.isChecked = false
            }
        }
        p0.itemView.setOnClickListener{
            p0.checkBox.isChecked = !p0.checkBox.isChecked
            if (p0.checkBox.isChecked){
                selecteVideosListener.onItemCheckChanged(hiddenVideo,true)
                hiddenVideo.isChecked = true
            }else{
                selecteVideosListener.onItemCheckChanged(hiddenVideo,false)
                hiddenVideo.isChecked = false
            }
        }
        p0.itemView.setOnLongClickListener {
            selecteVideosListener.onHiddenSongLongClikcked(hiddenVideo)
            true
        }
    }
    class HiddenVideoVH(itemView: View) : ViewHolder(itemView) {
        private val videoImage = itemView.findViewById<ImageView>(R.id.hidden_song_custom_image)
        private val videoTitle = itemView.findViewById<TextView>(R.id.hidden_song_title)
        val checkBox  = itemView.findViewById<CheckBox>(R.id.hidden_check_box)


        fun setVideo (videoUri : Uri, title : String, isChecked : Boolean) {
            videoTitle.setText(title)

            Glide.with(itemView.context)
                .load(videoUri)
                .thumbnail(0.2f)
                .into(videoImage)

            if(isChecked){
                checkBox.isChecked = true
            }else{
                checkBox.isChecked = false
            }

        }
    }
}