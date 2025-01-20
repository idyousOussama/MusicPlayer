package com.example.jamplayer.Adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Listeners.MusicFileItemsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R

class AudiosAdapter(var requestCode: Int) : RecyclerView.Adapter<AudiosAdapter.AudioViewHolder>() {
private  var musicsList :ArrayList<MusicFile> = ArrayList()
    private lateinit var fileMusicLestner : MusicFileItemsListener

fun setListner (fileMusicLestner : MusicFileItemsListener){
    this.fileMusicLestner = fileMusicLestner
}

fun setMusicFile(musicsList :ArrayList<MusicFile>){
this.musicsList = musicsList
}



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AudioViewHolder {
        if (requestCode == 1){
            if(settings!!.itemType == "small"){
                val view = LayoutInflater.from(p0.context).inflate(R.layout.small_songs_custom_item,p0,false)
                return AudioViewHolder(view)
            }else {
                val view = LayoutInflater.from(p0.context).inflate(R.layout.big_songs_custom_item,p0,false)
                return AudioViewHolder(view)
            }

        }else{
            if(settings!!.itemType == "small"){
                val view = LayoutInflater.from(p0.context).inflate(R.layout.theme_small_songs_custom_item,p0,false)
                return AudioViewHolder(view)
            }else{
                val view = LayoutInflater.from(p0.context).inflate(R.layout.theme_big_songs_custom_item,p0,false)
                return AudioViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        if(requestCode == 1){
            return musicsList.size
        }else{
            if(musicsList.size >=  8){
                return  8
            }else{
                return musicsList.size

            }
        }
    }

    override fun onBindViewHolder(p0: AudioViewHolder, p1: Int) {
       val musicFile = musicsList.get(p1)
            p0.setMusicFile(musicFile.title,musicFile.artist,musicFile.musicImage)

        p0.itemView.setOnClickListener {
            if(requestCode == 1){
                fileMusicLestner.onItemClickListner(musicFile,p1)

            }
        }
    }


    class AudioViewHolder(itemView: View) : ViewHolder(itemView){
private val title = itemView.findViewById<TextView>(R.id.custom_music_title)
private val artist = itemView.findViewById<TextView>(R.id.custom_music_artist)
private val image = itemView.findViewById<ImageView>(R.id.music_custom_image)
fun setMusicFile(mTitle:String, mArtist:String, mImage: Bitmap?){
    title.setText(mTitle)
    artist.setText(mArtist)
    if(mImage != null){
        image.setImageBitmap(mImage)
    }else{
        image.setImageResource(R.drawable.small_place_holder_image)
    }
}

    }

}