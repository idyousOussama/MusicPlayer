package com.example.jamplayer.Adapters.VedioAdapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.Vedios.PlayVideosActivity.Companion.pipStatus
import com.example.jamplayer.Listeners.VideoListeners.VideoItemListener
import com.example.jamplayer.Moduls.Video
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.currentVideo

class VideoAdapter(var requestNumber  : Int) : RecyclerView.Adapter<VideoAdapter.VideoCustomVH>() {
var videoList : ArrayList<Video> = ArrayList()
    lateinit var videoItemListener : VideoItemListener
    var lastVideoClicked : Video? = null
fun setVideosList (videoList : ArrayList<Video>){
    this.videoList = videoList
}
    fun setVideoListener(videoItemListener : VideoItemListener) {
        this.videoItemListener = videoItemListener
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VideoCustomVH {
        if(settings!!.itemType == "small" || requestNumber == 1) {
            val view  = LayoutInflater.from(p0.context).inflate(R.layout.landscape_small_video_item_view,p0,false)
            return VideoCustomVH(view)
        }
        else{
            val view  = LayoutInflater.from(p0.context).inflate(R.layout.big_video_item_view,p0,false)
            return VideoCustomVH(view)
        }
    }
    override fun getItemCount(): Int {
     return videoList.size
    }

    override fun onBindViewHolder(p0: VideoCustomVH, p1: Int) {
        val video = videoList[p1]
        p0.setVideo(video.title, Uri.parse(video.path), video.duration, video.isSelected, requestNumber)
        p0.itemView.setOnClickListener {
            pipStatus = 1
            currentVideo?.let { lastVideo ->
                val previousPosition = videoList.indexOf(lastVideo)
                lastVideo.isSelected = false
                notifyItemChanged(previousPosition)
            }
                video.isSelected = true
                lastVideoClicked = video
                notifyItemChanged(p1)
            videoItemListener.onVideoItemClicked(videoList, p1)
        }
    }
    class VideoCustomVH(itemView: View) : ViewHolder(itemView){
val vedioTitle = itemView.findViewById<TextView>(R.id.video_title)
val vedioThumbnail = itemView.findViewById<ImageView>(R.id.video_thumbnail)
val vedioDuration = itemView.findViewById<TextView>(R.id.video_duration)
    fun setVideo (title :  String , videoUri : Uri , duration : Long ,isSelected : Boolean, requestNumber: Int) {
        vedioTitle.setText(title)
         Glide.with(itemView.context)
            .load(videoUri)
            .thumbnail(0.2f)
            .into(vedioThumbnail)
        vedioDuration.setText(itemView.context.getString(R.string.duration_text)+" "+ ":"+" "+formatTime(duration/1000))
   if (isSelected && requestNumber == 1) {
       vedioTitle.setTextColor(itemView.context.resources.getColor(R.color.Primary))
   }else if(!isSelected && requestNumber == 1){
       vedioTitle.setTextColor(itemView.context.resources.getColor(R.color.Background))
   }
   else {
       vedioTitle.setTextColor(itemView.context.resources.getColor(R.color.Background))
   }
    }
        private fun formatTime(playingTime: Long): String {
            val hours = (playingTime / 3600).toString()
            val minutes = ((playingTime % 3600) / 60).toString().padStart(2, '0') // Add leading zero
            val seconds = (playingTime % 60).toString().padStart(2, '0') // Add leading zero
            return if (playingTime >= 3600) "$hours:$minutes:$seconds" else "$minutes:$seconds"
        }
    }


}