package com.example.jamplayer.Adapters
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Listeners.SelecteSongsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R

class SelectSongsAdapter : RecyclerView.Adapter<SelectSongsAdapter.HiddenSongVH>() {

    var songsList : ArrayList<MusicFile> = ArrayList()
 lateinit var selecteSongsListener: SelecteSongsListener

 fun setSongListener (selecteSongsListener: SelecteSongsListener){
     this.selecteSongsListener = selecteSongsListener
 }




    fun setSongList (hiddenSongsList : ArrayList<MusicFile>){
        this.songsList = hiddenSongsList
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HiddenSongVH {
        if(settings!!.itemType == "small"){
            val view = LayoutInflater.from(p0.context).inflate(R.layout.hidden_small_song_custom_item,p0,false)
            return HiddenSongVH(view)
        }else{
            val view = LayoutInflater.from(p0.context).inflate(R.layout.hidden_big_song_custom_item,p0,false)
            return HiddenSongVH(view)
        }
    }
    override fun getItemCount(): Int {
        return songsList.size
    }
    override fun onBindViewHolder(p0: HiddenSongVH, p1: Int) {
val hiddenSong = songsList.get(p1)
        p0.setSong(hiddenSong.musicImage,hiddenSong.title, hiddenSong.isChecked)
    p0.checkBox.setOnClickListener {
        if (p0.checkBox.isChecked){
            selecteSongsListener.onItemCheckChanged(hiddenSong,true)
            hiddenSong.isChecked = true
        }else{
            selecteSongsListener.onItemCheckChanged(hiddenSong,false)
            hiddenSong.isChecked = false
        }
    }
        p0.itemView.setOnClickListener{
                p0.checkBox.isChecked = !p0.checkBox.isChecked
            if (p0.checkBox.isChecked){
                selecteSongsListener.onItemCheckChanged(hiddenSong,true)
                hiddenSong.isChecked = true
            }else{
                selecteSongsListener.onItemCheckChanged(hiddenSong,false)
                hiddenSong.isChecked = false
            }
                 }
        p0.itemView.setOnLongClickListener {
            selecteSongsListener.onHiddenSongLongClikcked(hiddenSong)
            true
        }
    }
    class HiddenSongVH(itemView: View) : ViewHolder(itemView) {
private val songImage = itemView.findViewById<ImageView>(R.id.hidden_song_custom_image)
private val songTitle = itemView.findViewById<TextView>(R.id.hidden_song_title)
 val checkBox  = itemView.findViewById<CheckBox>(R.id.hidden_check_box)


        fun setSong (image : Bitmap?, title : String , isChecked : Boolean) {
            songTitle.setText(title)

            if(image != null){
                songImage.setImageBitmap(image)
            }else{
                songImage.setImageResource(R.drawable.small_place_holder_image)
            }

            if(isChecked){
                checkBox.isChecked = true
            }else{
                checkBox.isChecked = false
            }

        }
    }
    }
