package com.example.jamplayer.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.settings
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {
lateinit var binding : ActivityNotificationSettingsBinding
var currentFNS  = true
var currentSecN  = true
    var newFNS = true
    var newSecN = true
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    userIsActive = true
setCurrentNotificationsSettings()
initNotificationSettingsBtns()
    upDateNotificationSetting()
    initbackBtn()
    }
    private fun initbackBtn() {
        binding.notificationSettingsBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun upDateNotificationSetting() {
        binding.saveNotificationBtn.setOnClickListener{
            disableSaveBtn()

            if(currentFNS != newFNS && currentSecN != newSecN){
               // upDate the Fourground notification Service Settings
                jamViewModel.upDateFNSSetting(newFNS)
                // upDate the Secound Notification Settings
                jamViewModel.upDateSecNSettings(newSecN)
                settings!!.FSNIsEnable = newFNS
                settings!!.SecNIsEnable = newSecN
            }else if (currentFNS != newFNS && currentSecN == newSecN){
                jamViewModel.upDateFNSSetting(newFNS)
                settings!!.FSNIsEnable = newFNS
            }else if (currentFNS == newFNS && currentSecN != newSecN){
                jamViewModel.upDateSecNSettings(newSecN)
                settings!!.SecNIsEnable = newSecN
            }
            val intent = Intent(baseContext,SettingsActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
    private fun disableSaveBtn() {
        binding.saveNotificationBtn.apply {
            setText("...")
            isEnabled = false
        }
    }
    private fun initNotificationSettingsBtns() {
        binding.forgroundNotificationBtn.setOnClickListener {
            newFNS = binding.forgroundNotificationBtn.isChecked
            initSaveBtn()
        }
        binding.secondaryNotificationBtn.setOnClickListener {
            newSecN = binding.secondaryNotificationBtn.isChecked
            initSaveBtn()
        }
    }

    private fun setCurrentNotificationsSettings() {
      currentFNS = settings!!.FSNIsEnable
        currentSecN = settings!!.SecNIsEnable
        if(currentFNS){
        binding.forgroundNotificationBtn.isChecked  = true
        }else{
            binding.forgroundNotificationBtn.isChecked  = false
        }
        if(currentSecN){
            binding.secondaryNotificationBtn.isChecked = true
        }else{
            binding.secondaryNotificationBtn.isChecked = false
        }
    }
    private fun initSaveBtn(){
        if(currentFNS == newFNS && currentSecN == newSecN) {
            binding.saveNotificationBtn.visibility = View.GONE
        }else{
            binding.saveNotificationBtn.visibility = View.VISIBLE
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}