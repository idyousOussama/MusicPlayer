package com.example.jamplayer.Activities.Vedios

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.playingVideoManager.currentVideo
import com.example.jamplayer.databinding.ActivityEditVideoTagsBinding
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
        setUpCurrentVideo()
        handelVideoTitleChanges()
        saveVideochanges()
    }
    private fun saveVideochanges() {
        binding.saveVideoChangesBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
            jamViewModel.upDateVideoTitleById(newTitle, currentVideo!!.id)
           Toast.makeText(baseContext,"video title is Updated . ", Toast.LENGTH_SHORT).show()
val resultIntent = Intent(baseContext,PlayVideosActivity::class.java)
                    resultIntent.putExtra("newVideoTitle",newTitle)
        setResult(RESULT_OK,resultIntent)
        finish()
            }
        }
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
}