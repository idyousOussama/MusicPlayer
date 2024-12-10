package com.example.jamplayer.Adapters
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Listeners.HiddenSongsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HiddenSongsAdapter : RecyclerView.Adapter<HiddenSongsAdapter.HiddenSongVH>() {

    var hiddenSongsList : ArrayList<MusicFile> = ArrayList()
 lateinit var hiddenSongsListener: HiddenSongsListener

 fun setHiddenSongListener (hiddenSongsListener: HiddenSongsListener){
     this.hiddenSongsListener = hiddenSongsListener
 }




    fun setHiddenList (hiddenSongsList : ArrayList<MusicFile>){
        this.hiddenSongsList = hiddenSongsList
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HiddenSongVH {
val view = LayoutInflater.from(p0.context).inflate(R.layout.hidden_song_custom_item,p0,false)
 return HiddenSongVH(view)

    }

    override fun getItemCount(): Int {
        return hiddenSongsList.size
    }


    override fun onBindViewHolder(p0: HiddenSongVH, p1: Int) {
val hiddenSong = hiddenSongsList.get(p1)

        p0.setHiddenSong(hiddenSong.musicImage,hiddenSong.title, hiddenSong.isChecked)
    p0.checkBox.setOnClickListener {
        if (p0.checkBox.isChecked){
            hiddenSongsListener.onItemCheckChanged(hiddenSong,true)
            hiddenSong.isChecked = true
        }else{
            hiddenSongsListener.onItemCheckChanged(hiddenSong,false)
            hiddenSong.isChecked = false
        }
    }

        p0.itemView.setOnClickListener{
                p0.checkBox.isChecked = !p0.checkBox.isChecked
            if (p0.checkBox.isChecked){
                hiddenSongsListener.onItemCheckChanged(hiddenSong,true)
                hiddenSong.isChecked = true
            }else{
                hiddenSongsListener.onItemCheckChanged(hiddenSong,false)
                hiddenSong.isChecked = false
            }
                 }
    }
    class HiddenSongVH(itemView: View) : ViewHolder(itemView) {
private val songImage = itemView.findViewById<ImageView>(R.id.hidden_song_custom_image)
private val songTitle = itemView.findViewById<TextView>(R.id.hidden_song_title)
 val checkBox  = itemView.findViewById<CheckBox>(R.id.hidden_check_box)


        fun setHiddenSong (image : Bitmap?, title : String , isChecked : Boolean) {
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
