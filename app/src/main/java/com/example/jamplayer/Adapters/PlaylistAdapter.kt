package com.example.jamplayer.Adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.unHideSong
import com.example.jamplayer.Listeners.PlayListItemListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.R

class PlaylistAdapter(var playListItemTypeNum : Int) : RecyclerView.Adapter<PlaylistAdapter.PlaylistVH>() {
        private var playLists : ArrayList<PlayList> = ArrayList()
    private var playListItemListener : PlayListItemListener? = null
    fun setPlayLists(playLists : ArrayList<PlayList> ){
        this.playLists = playLists
    }

    fun setNewPlayList(playList : PlayList){
        playLists.add(playList)
        notifyDataSetChanged()
    }
     fun setPlayListItemListener(playListItemListener : PlayListItemListener) {
         this.playListItemListener = playListItemListener
     }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PlaylistVH {
        if(playListItemTypeNum == 1){
            if(settings!!.itemType == "small"){
                val view = LayoutInflater.from(p0.context).inflate(R.layout.small_playlist_item_custom_view,p0,false)
                return PlaylistVH(view)
            }else{
                val view = LayoutInflater.from(p0.context).inflate(R.layout.big_playlist_item_custom_view,p0,false)
                return PlaylistVH(view)
            }
        }else{
            val view = LayoutInflater.from(p0.context).inflate(R.layout.small_playlist_item_custom_view,p0,false)
            return PlaylistVH(view)
        }
    }

    override fun getItemCount(): Int {
return playLists.size    }

    override fun onBindViewHolder(p0: PlaylistVH, p1: Int) {
        val playList = playLists[p1]
        val context = p0.itemView.context

        // Retrieve playlist titles from resources
        val likedListTitle = context.getString(R.string.likedSongs_text)
        val mostPlayedTitle = context.getString(R.string.mostPlayedSongs_text)
        val recentlyPlayedTitle = context.getString(R.string.RecentlyPlayedSongs_text)

// Get filtered songs based on playlist type
        val playListSongs: ArrayList<MusicFile> = when (playList.title) {
            likedListTitle -> unHideSong.filter { it.isLiked }.toCollection(ArrayList())
            mostPlayedTitle -> unHideSong.filter { it.playedNumber > 10 }.toCollection(ArrayList())
            recentlyPlayedTitle -> unHideSong.filter { it.isPlayedRecently }.toCollection(ArrayList())
            else -> {
                val result = ArrayList<MusicFile>() // Initialize the list for the else branch
                for (item in playList.playlistSong) {
                    val matchingSongs = unHideSong.filter { it.id == item } // Filter songs matching the ID
                    result.addAll(matchingSongs) // Add all matching songs to the result list
                }
                result // Return the result list
            }
        }


        // Initialize the playlist item
        val musicImage = playListSongs.firstOrNull()?.musicImage
        val songCount = playListSongs.size
        p0.initPlaylistItem(musicImage, playList.icon, playList.title, songCount,playListSongs)

        // Set click listener
        p0.itemView.setOnClickListener {
            playListItemListener?.onPlayListItemClicked(playList, playListSongs)
        }
    }
    class PlaylistVH(itemView: View) :ViewHolder(itemView){
        private val playListImage = itemView.findViewById<ImageView>(R.id.small_playList_item_image)
        private val playListIcon = itemView.findViewById<ImageView>(R.id.small_playList_item_icon)
        private val playListTitle = itemView.findViewById<TextView>(R.id.small_playList_item_title)
        private val playListNumSongs = itemView.findViewById<TextView>(R.id.small_playList_item_songsNum)
        private  val playlistSongsNum1 = itemView.findViewById<TextView>(R.id.playlist_song_num1)
        private  val playlistSongsNum2 = itemView.findViewById<TextView>(R.id.playlist_song_num2)
    fun initPlaylistItem (image : Bitmap?, icon : Int? , title : String , numSongs : Int , playlistSongsList :ArrayList<MusicFile> ) {
        if(image != null){
            playListImage.setImageBitmap(image)
        }else{
            playListImage.setImageResource(R.drawable.songs_list_place_holder)
        }
    if(icon != null){
        playListIcon.setImageResource(icon)
    }else{
        playListIcon.visibility = View.GONE
    }
        if (numSongs == 0){
            playListNumSongs.visibility = View.GONE
        }else{
            playListNumSongs.visibility = View.VISIBLE
            playListNumSongs.text = numSongs.toString()
        }
        playListTitle.setText(title)
        if(playlistSongsList.isEmpty()){
            playlistSongsNum1.visibility = View.GONE
            playlistSongsNum2 .visibility = View.GONE
        }else if (playlistSongsList.size == 1){
            playlistSongsNum1.visibility = View.VISIBLE
            playlistSongsNum2 .visibility = View.GONE
            playlistSongsNum1.text =playlistSongsList.get(0).title
        }else if (playlistSongsList.size > 1) {
            playlistSongsNum1.visibility = View.VISIBLE
            playlistSongsNum2 .visibility = View.VISIBLE
            playlistSongsNum1.text =playlistSongsList.get(0).title
            playlistSongsNum2.text =playlistSongsList.get(1).title
        }

    }
    }
}