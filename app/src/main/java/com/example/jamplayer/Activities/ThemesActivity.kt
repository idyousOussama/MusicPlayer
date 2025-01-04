package com.example.jamplayer.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorLong
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.Adapters.ThemesAdapter
import com.example.jamplayer.AppDatabase.ViewModels.JamViewModel
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityThemesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThemesActivity : AppCompatActivity() {
    lateinit var binding : ActivityThemesBinding
lateinit var themesAdapter : ThemesAdapter
lateinit var itemSize : String
var originalItemSize = settings!!.itemType
var currentItemSize = settings!!.itemType
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     binding = ActivityThemesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
        getSettings()
        setThemeViews()
        initBtns()
        onBackPressonBachPressed()
    }
    private fun getSettings() {
        itemSize = settings!!.itemType
        if (itemSize == "small"){
            initSmallBtn()
        }else if(itemSize == "big"){
            initBigBtn()
        }
    }
    private fun initBtns() {
        binding.smallBtn.setOnClickListener {
            initSmallBtn()
        settings!!.itemType = "small"
            currentItemSize = "small"
                    showSaveBtn()
                }
        binding.bigBtn.setOnClickListener {
            initBigBtn()
            settings!!.itemType = "big"
            currentItemSize = "big"
            showSaveBtn()

        }
binding.saveThemeBtn.setOnClickListener {
    saveChanges()
}
    }

    private fun saveChanges() {
        CoroutineScope(Dispatchers.Main).launch{
            jamViewModel.upDateItemSize(currentItemSize)
            val intent = Intent(this@ThemesActivity,SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showSaveBtn() {
        if(currentItemSize != originalItemSize){
            binding.saveThemeBtn.visibility = View.VISIBLE
        }else{
            binding.saveThemeBtn.visibility = View.GONE
        }
    }

    private fun initBigBtn() {
        binding.bigText.visibility = View.VISIBLE
        binding.bigBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        binding.bigItemsIcon.setImageResource(R.drawable.white_big_items)

        binding.smallItemsIcon.setImageResource(R.drawable.black_small_items)
        binding.smallBtn.setBackgroundColor(resources.getColor(R.color.white))
        binding.smallText.visibility = View.GONE
        setThemeViews()
    }
    private fun onBackPressonBachPressed() {
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(currentItemSize != originalItemSize){
                    showConfermDialog()
                }else{
                    finish()
                }
            }
        })
    }
    private fun showConfermDialog() {
        val dialogCustom = LayoutInflater.from(this).inflate(R.layout.confirm_action_dialog,null)
        val positiveBtn = dialogCustom.findViewById<TextView>(R.id.conferm_diolog_action_positiveBtn)
        val negativeBtn = dialogCustom.findViewById<TextView>(R.id.conferm_diolog_action_negativeBtn)
        val dialogTitle = dialogCustom.findViewById<TextView>(R.id.conferm_diolog_action_title)
        val dialogMessage = dialogCustom.findViewById<TextView>(R.id.conferm_diolog_action_message)
        val confermDialog = Dialog(this)
        confermDialog.setContentView(dialogCustom)
        confermDialog.setCancelable(true)
        dialogTitle.setText(R.string.go_back_text)
        positiveBtn.setText(R.string.save_text)
        negativeBtn.setText(R.string.cancel_text)
        dialogMessage.setText(R.string.dialog_action_changes_message)
        positiveBtn.setOnClickListener {
            saveChanges()
        }
        negativeBtn.setOnClickListener {
            finish()
        }
        confermDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confermDialog.show()
    }

    private fun initSmallBtn() {
        binding.smallItemsIcon.setImageResource(R.drawable.white_small_items)
        binding.smallBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        binding.smallText.visibility = View.VISIBLE
        binding.bigText.visibility = View.GONE
        binding.bigBtn.setBackgroundColor(resources.getColor(R.color.white))
        binding.bigItemsIcon.setImageResource(R.drawable.black_big_items)
        setThemeViews()
    }

    private fun setThemeViews() {
val list : ArrayList<Int> = ArrayList()
        themesAdapter = ThemesAdapter()
        list.add(R.layout.songs_theme_custom_fragment)
        list.add(R.layout.albums_theme_custom_fragment)
        list.add(R.layout.show_album_theme_custom)
        binding.themeLayoutImagesList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            setHasFixedSize(true)
            adapter = themesAdapter
        }
        themesAdapter.setThemeViews(list)

    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}