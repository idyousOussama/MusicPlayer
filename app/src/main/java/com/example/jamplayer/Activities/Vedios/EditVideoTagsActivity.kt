package com.example.jamplayer.Activities.Vedios

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.jamplayer.Activities.Songs.MainActivity
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.currentVideo
import com.example.jamplayer.databinding.ActivityEditVideoTagsBinding
import com.example.jamplayer.databinding.ConfirmActionDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditVideoTagsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEditVideoTagsBinding
private var videoOldTitle  = ""
    var newTitle = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditVideoTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGoBackBtn()
        setUpCurrentVideo()
        handelVideoTitleChanges()
        initSaveVideoChangesBtn()
        onBackPressonBachPressed()
    }

    private fun initGoBackBtn() {
        binding.videoEditTagBackBtn.setOnClickListener {
         if(!shekAboutNewChanges()) {
             finish()
         }else {
             showConfiramtionExitDialog()
         }

        }
    }

    private fun showConfiramtionExitDialog() {
        val confiramtionDialogView = layoutInflater.inflate(R.layout.confirm_action_dialog,null)
        val confiramtionDialogBinding = ConfirmActionDialogBinding.bind(confiramtionDialogView)
        val confiramtionDialog = Dialog(this)
        confiramtionDialogBinding.confermDiologActionTitle.text = getString(R.string.save_video_chages)
        confiramtionDialogBinding.confermDiologActionMessage.text = getString(R.string.confiramtion_video_changes_message)
        confiramtionDialogBinding.confermDiologActionPositiveBtn.text = getString(R.string.save_text)
        confiramtionDialogBinding.confermDiologActionNegativeBtn.text = getString(R.string.exit_text)
        confiramtionDialog.setContentView(confiramtionDialogView)
        confiramtionDialog.setCancelable(false)
        confiramtionDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confiramtionDialogBinding.confermDiologActionPositiveBtn.setOnClickListener {
            saveVideochanges()
            confiramtionDialog.cancel()
        }
        confiramtionDialogBinding.confermDiologActionNegativeBtn.setOnClickListener {
            confiramtionDialog.cancel()
            finish()
        }
        confiramtionDialog.show()

    }

    private fun shekAboutNewChanges(): Boolean {
if(videoOldTitle == newTitle) {
    return  false
}else {
    return true
}
    }

    private fun initSaveVideoChangesBtn() {
        binding.saveVideoChangesBtn.setOnClickListener {
            saveVideochanges()
        }
    }

    private fun saveVideochanges() {
        CoroutineScope(Dispatchers.Main).launch {
            jamViewModel.upDateVideoTitleById(newTitle, currentVideo!!.id)
            Toast.makeText(baseContext,"video title is Updated . ", Toast.LENGTH_SHORT).show()
            val resultIntent = Intent(baseContext,PlayVideosActivity::class.java)
            resultIntent.putExtra("newVideoTitle",newTitle)
            setResult(RESULT_OK,resultIntent)
            navigateToNewActivity(MainActivity::class.java)
        }

    }

    private fun navigateToNewActivity(newActivity: Class<*>) {
        val newActivityIntent = Intent(baseContext,newActivity)
        newActivityIntent.putExtra("VideoIsDeleted" , true)
        startActivity(newActivityIntent)
    }

     private fun handelVideoTitleChanges() {
        binding.videoEditTagsNameInput.addTextChangedListener {
            newTitle = binding.videoEditTagsNameInput.text.toString()
        if(title == videoOldTitle || title.isEmpty() ) {
            binding.saveVideoChangesBtn.visibility = View.GONE
        }else {
            binding.saveVideoChangesBtn.visibility = View.VISIBLE
        }

        }

    }
    private fun setUpCurrentVideo() {
        videoOldTitle = currentVideo!!.title
        binding.videoEditTagsNameInput.setText(currentVideo!!.title)
        Glide.with(baseContext)
            .load(currentVideo!!.path)
            .thumbnail(0.2f)
            .into(binding.videoEditTagsVideoThumbnail)
        binding.videoEditTagsPathText.text  = currentVideo!!.filePath
    }
    private fun onBackPressonBachPressed() {
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(!shekAboutNewChanges()) {
                finish()
            }else {
                showConfiramtionExitDialog()
            }
            }
        })
    }
}