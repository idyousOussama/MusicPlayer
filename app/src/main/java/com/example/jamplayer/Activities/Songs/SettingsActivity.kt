package com.example.jamplayer.Activities.Songs

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.firebaseDB
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.user

import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivitySettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SettingsActivity : AppCompatActivity() {
    lateinit var binding : ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
initBtns()
 checkNewMessage()
        initbackBtn()
    }
    private fun initbackBtn() {
        binding.settingBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun checkNewMessage() {
        val userNewMessagesRef = firebaseDB.getReference("NewMessages").child(user!!.userId + "support0766501026")
        userNewMessagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    binding.feedbackRedDot.visibility = View.VISIBLE

                }else{
                    binding.feedbackRedDot.visibility = View.GONE


                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }



    private fun initBtns() {
        binding.palayingHidenSongLayout.setOnClickListener {
            sentIntent(HiddenFilesActivity::class.java)
        }
        binding.playingTimeLayout.setOnClickListener {
            sentIntent(PlayingTimeActivity::class.java)

        }
        binding.aboutLayout.setOnClickListener {
            alertDialog()
        }
        binding.settingThemeBtn.setOnClickListener {
            sentIntent(ThemesActivity::class.java)
        }
        binding.feedbackLayout.setOnClickListener {
            sentIntent(FeedBackActivity::class.java)
        }
        binding.notificationLayout.setOnClickListener {
            sentIntent(NotificationSettingsActivity::class.java)
        }
    }
    private fun alertDialog() {
        val dialogCustom = LayoutInflater.from(this).inflate(R.layout.about_custom_dialog,null)
         val aboutdialog = Dialog(this)
        aboutdialog.setContentView(dialogCustom)
        aboutdialog.setCancelable(true)
        aboutdialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        aboutdialog.show()
    }
    private fun sentIntent(intClass: Class<*>) {
val intent = Intent(this,intClass)
        startActivity(intent)
        finish()

    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}