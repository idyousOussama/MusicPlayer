package com.example.jamplayer.Activities.Songs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.jamplayer.Fragments.FeedbackFragments.SimpleFeedbackFragment
import com.example.jamplayer.Fragments.HiddenSongFragment
import com.example.jamplayer.Fragments.HiddenVideosFragment
import com.example.jamplayer.Fragments.SongsFragment
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityHiddenFilesBinding


const val HIDDEN_FILE_FRAGMENT_RES_CODE = "fragmentRequetCode"
class HiddenFilesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHiddenFilesBinding
private var HiddenVideosFragemntIsAttached = false
private var HiddenSongsFragemntIsAttached = false
   private val songFragment  = HiddenSongFragment()
 private val videoFragment  = HiddenVideosFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding = ActivityHiddenFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
        initbackBtn()
         addHiddenFileFile ()
        hnadelChangersBtns()
    }

    private fun hnadelChangersBtns() {
        binding.hiddenSongLayoutBtn.setOnClickListener {
             if (!HiddenSongsFragemntIsAttached) {
                 replaceFirstFragment(songFragment)
                 upDateHiddenSongsBtnLayout()
                 HiddenSongsFragemntIsAttached = true
                 HiddenVideosFragemntIsAttached = false
             }
        }
        binding.hiddenVideoLayoutBtn.setOnClickListener {
            if (!HiddenVideosFragemntIsAttached) {
                replaceFirstFragment(videoFragment)
                upDateHiddenVideosBtnLayout()
                HiddenSongsFragemntIsAttached = false
                HiddenVideosFragemntIsAttached = true
            }
        }
    }

    private fun addHiddenFileFile() {
        val requestCodeIntent  = intent
val fragmentRequetCode = requestCodeIntent.getStringExtra(HIDDEN_FILE_FRAGMENT_RES_CODE)
        when( fragmentRequetCode) {
      "hiddenSongs" -> {
          upDateHiddenSongsBtnLayout()
          replaceFirstFragment(songFragment)
          HiddenVideosFragemntIsAttached = false
          HiddenSongsFragemntIsAttached = true
      }
            "hiddenVideos" -> {
                upDateHiddenVideosBtnLayout()
                replaceFirstFragment(videoFragment)
                HiddenVideosFragemntIsAttached = true
                HiddenSongsFragemntIsAttached = false
            }
        }



    }

    private fun upDateHiddenVideosBtnLayout() {
       binding.hiddenVideoLayoutBtn.setBackgroundResource(R.drawable.enable_btns_backround)
       binding.hiddenVideosBtnTitle.setTextColor(resources.getColor(R.color.white))
      binding.hiddenVideosBtnIcon .setImageResource(R.drawable.white_video_icon)


        binding.hiddenSongLayoutBtn.background = null
        binding.hiddenSongsBtnTitle.setTextColor(resources.getColor(R.color.deep_gray))
        binding.hiddenSongsBtnIcon.setImageResource(R.drawable.gray_music_icon)
    }

    private fun upDateHiddenSongsBtnLayout() {
        binding.hiddenSongLayoutBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        binding.hiddenSongsBtnTitle.setTextColor(resources.getColor(R.color.white))
        binding.hiddenSongsBtnIcon.setImageResource(R.drawable.white_music_icon)

        binding.hiddenVideoLayoutBtn.background = null
        binding.hiddenVideosBtnTitle.setTextColor(resources.getColor(R.color.deep_gray))
        binding.hiddenVideosBtnIcon.setImageResource(R.drawable.gray_video_icon)
   }
    private fun replaceFirstFragment(firstFragment: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(binding.hiddenFileFragmentsContainer.id ,firstFragment)
        ft.commit()
    }

    private fun initbackBtn() {
        binding.hiddenGoBackBtn.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}