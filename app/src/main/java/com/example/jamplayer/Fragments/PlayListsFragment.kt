package com.example.jamplayer.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity.showPlaylistmanager.playList
import com.example.jamplayer.Activities.Songs.ShowPlaylistActivity.showPlaylistmanager.playlistSongs
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.playLists
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.settings

import com.example.jamplayer.Adapters.PlaylistAdapter
import com.example.jamplayer.Listeners.PlayListItemListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.Moduls.PlayList
import com.example.jamplayer.R
import com.example.jamplayer.databinding.FragmentPlayListsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayListsFragment : Fragment() {
    private var _binding: FragmentPlayListsBinding?= null
    private val binding get() = _binding!!
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    private val playListsAdapter = PlaylistAdapter(1  )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayListsBinding.inflate(inflater, container, false)
        val view = binding.root
    return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialPlaylists(view)

handleCreateNewPlaylistBtn()
    binding.playListRefreshSwipe.setOnRefreshListener {
        binding.playListCreateNewPlayBtn.visibility = View.GONE
        intialPlaylists(view)
    }

    }

    private fun handleCreateNewPlaylistBtn() {
        binding.playListCreateNewPlayBtn.setOnClickListener {
            showCreateNewPlaylistDialog()
        }
    }

    private fun showCreateNewPlaylistDialog() {
        var titleText  = ""
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.create_playlist_dialog_content_view,null)
        val newPlayListTitle = dialogView.findViewById<EditText>(R.id.create_new_playlist_title_input)
        val newPlayListPositiveBtn = dialogView.findViewById<TextView>(R.id.create_playList_positive_Btn)
        val newPlayListNegativeBtn = dialogView.findViewById<TextView>(R.id.create_playList_negative_Btn)
        val createNewPlaylistDialog = Dialog(requireContext())
        createNewPlaylistDialog.setContentView(dialogView)
        createNewPlaylistDialog.setCancelable(false)
        createNewPlaylistDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        newPlayListNegativeBtn.setOnClickListener {
            createNewPlaylistDialog.dismiss()
        }
        newPlayListTitle.addTextChangedListener {
             titleText = newPlayListTitle.text.toString()
if(titleText.isNotEmpty()){
    initCreateNewPlayListBtn(true , newPlayListPositiveBtn)
}else{
    initCreateNewPlayListBtn(false , newPlayListPositiveBtn)
}
        }
        newPlayListPositiveBtn.setOnClickListener {

            createNewPlayList(titleText,createNewPlaylistDialog)
        }
        createNewPlaylistDialog.show()
    }

    private fun createNewPlayList(titleText: String , dialog : Dialog) {
        CoroutineScope(Dispatchers.Main).launch {
            playList = jamViewModel.getPlaylistByTitle(titleText)
if(playList == null){
    Toast.makeText(requireContext() , R.string.adding_new_play_list_text , Toast.LENGTH_SHORT).show()
    val newPlaylistSongList : ArrayList<String> = ArrayList()
    val newPlayList =PlayList(0,titleText,null,newPlaylistSongList)
    jamViewModel.insertNewPalyList(newPlayList)
    Toast.makeText(requireContext(),R.string.newPlaylist_added, Toast.LENGTH_SHORT).show()
   playList = jamViewModel.getPlaylistByTitle(titleText)
    playListsAdapter.setNewPlayList(playList!!)
    dialog.dismiss()
}else {
    Toast.makeText(requireContext(),R.string.this_PlayList_added, Toast.LENGTH_SHORT).show()
}
        }
    }
    private fun initCreateNewPlayListBtn(noEmpty: Boolean, positiveBtn: TextView) {
if(noEmpty) {
    positiveBtn.isEnabled = true
    positiveBtn.setTextColor(resources.getColor(R.color.Primary))
}else{
    positiveBtn.isEnabled = false
    positiveBtn.setTextColor(resources.getColor(R.color.gray))
}
    }
    private fun intialPlaylists(view: View) {
        ioScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    if (binding.playListRefreshSwipe.isRefreshing){
                        playLists = jamViewModel.getAllPlayLists() as ArrayList
                        updatePlayListList(playLists , view.context)

                    }else {
                        updatePlayListList(playLists , view.context)

                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(view.context, "Failed to load songs: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun updatePlayListList(playLists: ArrayList<PlayList> , context : Context) {
        if (playLists.isEmpty()) {
            binding.playListRV.visibility = View.GONE
        } else {
            binding.playListRV.visibility = View.VISIBLE
            playListsAdapter.setPlayLists(playLists)
            setupPalyLisRecyclerView(context)
        }
    }
    private fun setupPalyLisRecyclerView(context :Context) {
        binding.playListRV.apply {
            layoutManager = if (settings?.itemType == "small") {
                LinearLayoutManager(context)
            }

            else{
                if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    GridLayoutManager(context, 2)

                }else {
                    GridLayoutManager(context, 4)

                }
            }
            setHasFixedSize(true)
            adapter = playListsAdapter

        }
        binding.playListCreateNewPlayBtn.visibility = View.VISIBLE

        binding.playListRefreshSwipe.setRefreshing(false)

        playListsAdapter.setPlayListItemListener(object : PlayListItemListener{
            override fun onPlayListItemClicked(
                selectedPlayList: PlayList,
                playListSongs: ArrayList<MusicFile>
            ) {
                playlistSongs = playListSongs
                playList = selectedPlayList
                navigateToNewActivty(ShowPlaylistActivity::class.java,context)
            }
        })
    }

    private fun navigateToNewActivty(newActivity: Class<*> , context :Context) {
val playListIntent = Intent(context,newActivity)
        startActivity(playListIntent)
    }


}