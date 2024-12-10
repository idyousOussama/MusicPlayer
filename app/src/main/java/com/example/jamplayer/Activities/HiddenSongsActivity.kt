package com.example.jamplayer.Activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.executor
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Adapters.HiddenSongsAdapter
import com.example.jamplayer.Listeners.HiddenSongsListener
import com.example.jamplayer.Moduls.MusicFile
import com.example.jamplayer.R
import com.example.jamplayer.databinding.ActivityHiddenSongsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HiddenSongsActivity : AppCompatActivity() {
    lateinit var binding : ActivityHiddenSongsBinding
 var checkedHiddenSongsList: ArrayList<MusicFile> = ArrayList()
    private lateinit var hiddenSongs: ArrayList<MusicFile>
    private lateinit var processDialog: Dialog
    private var counter = 0
    val hiddenSongsAdapter  = HiddenSongsAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding = ActivityHiddenSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.Main).launch {
            hiddenSongs = jamViewModel.getHiddenSongs() as ArrayList<MusicFile>
            setHiddenSongs(hiddenSongs)
        }
        unHideSongs()
        searchingUnhiddenSongs()
        selectAllHiddenSongs()
    }

    private fun selectAllHiddenSongs() {
binding.selectAllBox.setOnClickListener{
    if (binding.selectAllBox.isChecked){
        checkedHiddenSongsList.clear()
        for (item in hiddenSongs){
            item.isChecked = true
            checkedHiddenSongsList.add(item)
        }
        enableUnHideBtn()
     }else if (!binding.selectAllBox.isChecked){
        for (item in hiddenSongs){
            item.isChecked = false
            checkedHiddenSongsList.remove(item)
        }

        disableUnHideBtn()
    }
    setHiddenSongs(hiddenSongs)

}
    }

    private fun unHideSongs() {
        binding.unHideSongsBtn.setOnClickListener {
            alertProcessingDialog()
            for(item in checkedHiddenSongsList){
                if(item != null){
                    jamViewModel.upDateSongsById(item.id, false)
                }
                counter++
                if (counter == checkedHiddenSongsList.size){
                    processDialog.dismiss()
                    movetToMainActivity()
                }
            }
        }
    }

    private fun movetToMainActivity() {
        val mainIntent = Intent(baseContext,MainActivity::class.java)
        startActivity(mainIntent)
    }

    private fun setHiddenSongs(hiddenSongList: ArrayList<MusicFile>) {
            if (this.hiddenSongs.isNotEmpty()){
                hiddenSongsAdapter.setHiddenList(hiddenSongList)
                binding.hiddenSongList.layoutManager = LinearLayoutManager(baseContext)
                binding.hiddenSongList.setHasFixedSize(true)
                binding.hiddenSongList.adapter = hiddenSongsAdapter
            }
        getCheckedSongItem(hiddenSongsAdapter)
    }
    private fun getCheckedSongItem(hiddenSongsAdapter: HiddenSongsAdapter) {
        hiddenSongsAdapter.setHiddenSongListener(object : HiddenSongsListener {
            override fun onItemCheckChanged(hiddenSong: MusicFile, toAdd: Boolean) {
if(toAdd ){
  checkedHiddenSongsList.add(hiddenSong)
    if(!binding.unHideSongsBtn.isEnabled){
        enableUnHideBtn()
    }else if(checkedHiddenSongsList.size == hiddenSongs.size){
        binding.selectAllBox.isChecked = true
     }else if (!binding.unHideSongsBtn.isEnabled && checkedHiddenSongsList.size == hiddenSongs.size){
        enableUnHideBtn()
        binding.selectAllBox.isChecked = true
    }
}else{
    if (checkedHiddenSongsList.isNotEmpty()){
        binding.selectAllBox.isChecked = false
        checkedHiddenSongsList.remove(hiddenSong)
        if (checkedHiddenSongsList.isEmpty()){
            disableUnHideBtn()
        }
    }

}



            }
        })
    }

    private fun enableUnHideBtn() {
        binding.unHideSongsBtn.isEnabled = true
        binding.unHideSongsBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        @Suppress("DEPRECATION")
        binding.unHideSongsBtn.setTextColor(resources.getColor(R.color.white))
    }

    private fun disableUnHideBtn() {
        binding.unHideSongsBtn.isEnabled = false
        binding.unHideSongsBtn.setBackgroundResource(R.drawable.disable_btns_backround)
        @Suppress("DEPRECATION")
        binding.unHideSongsBtn.setTextColor(resources.getColor(R.color.disable_text_color))
    }

    fun alertProcessingDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.processing_dialog_custom,null)
        val processText =dialogView.findViewById<TextView>(R.id.dialog_process_text)
        processDialog = Dialog(this)
        processDialog.setContentView(dialogView)
        processDialog.setCancelable(false)
        processText.setText(R.string.Unhiding_text)
        processDialog.create()
    }

private fun searchingUnhiddenSongs(){
binding.hiddenInputSearchBar.addTextChangedListener {
    val searchText = binding.hiddenInputSearchBar.text.toString()
    binding.selectAllBox.visibility = View.GONE
if(searchText.isNotEmpty()){
executor.execute {
    val filteredHiddenSongList = hiddenSongs.filter {
        it.title.contains(searchText, ignoreCase = true) || it.album.contains(searchText, ignoreCase = true)
    }
    lifecycleScope.launch {
        setHiddenSongs(filteredHiddenSongList as ArrayList)
    }
}
}else{
    setHiddenSongs(hiddenSongs)
    binding.selectAllBox.visibility = View.VISIBLE
}
}
    }
}