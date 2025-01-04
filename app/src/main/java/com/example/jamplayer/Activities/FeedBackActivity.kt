package com.example.jamplayer.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.jamplayer.Activities.FeedBackActivity.SubmitFeedbackBtnManager.subBtnListner
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.firebaseDB
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.user
import com.example.jamplayer.Fragments.FeedbackFragments.ADsfragment
import com.example.jamplayer.Fragments.FeedbackFragments.AppSlowFragement
import com.example.jamplayer.Fragments.FeedbackFragments.FileCantPlayFragment
import com.example.jamplayer.Fragments.FeedbackFragments.FileNotFoundFragment
import com.example.jamplayer.Fragments.FeedbackFragments.MusicStopFragment
import com.example.jamplayer.Fragments.FeedbackFragments.OthersFragment
import com.example.jamplayer.Fragments.FeedbackFragments.SimpleFeedbackFragment
import com.example.jamplayer.Listeners.FeedbackListener
import com.example.jamplayer.Listeners.SubmitButtonListener
import com.example.jamplayer.Moduls.FeedbackMessage
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityFeedBackBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedBackActivity : AppCompatActivity() ,FeedbackListener {
    lateinit var binding : ActivityFeedBackBinding
     var lastClickedBtn : TextView? = null
    var feedBackType = "simple"
    lateinit var  feedback : FeedbackMessage
object SubmitFeedbackBtnManager{
    var subBtnListner : SubmitButtonListener? = null
}
    private lateinit var feedbackText : String
    private var adsCheckBoxesList : ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding =ActivityFeedBackBinding.inflate(layoutInflater)

        setContentView(binding.root)
        userIsActive = true
        addSimpleFeedFragment()
        initFeedbacksBtns()
        submitFeedback()
        checkNewMessage()
        initbackBtn()
    }

    private fun initbackBtn() {
        binding.feedBackGoBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun checkNewMessage() {
        val userNewMessagesRef = firebaseDB.getReference("NewMessages").child(user!!.userId + "support0766501026")
        userNewMessagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    binding.addFeedbackRedDot.visibility = View.VISIBLE

                }else{
                    binding.addFeedbackRedDot.visibility = View.GONE


                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }


    private fun submitFeedback() {
binding.feedBackSubmitBtn.setOnClickListener {
    disableSubBtn()
    val userRoom = firebaseDB.getReference("FeedbacksRooms").child(user!!.userId + "support0766501026")
    val supportNewMessagesRef = firebaseDB.getReference("NewMessages").child( "support0766501026" + user!!.userId  )
    val userFeedbacksIds = firebaseDB.getReference("FeedbacksIds")
    val supportRoom = firebaseDB.getReference("FeedbacksRooms").child("support0766501026" + user!!.userId  )
    val feedbackId = userFeedbacksIds.push().key.toString()
    val userFeedbackId = userRoom.push().key.toString()
    userFeedbacksIds.child(user!!.userId).child(userFeedbackId).setValue(userFeedbackId)
    // timestamp (current time)
    val currentTimestamp = System.currentTimeMillis()
    // Format and display the date
    val formattedDate = formatTimestamp(currentTimestamp)
    if(feedBackType.equals("ads")){
        feedback = FeedbackMessage(feedbackId,user!!.userId,feedBackType,feedbackText,adsCheckBoxesList,formattedDate)
    }else{
        feedback = FeedbackMessage(feedbackId,user!!.userId,feedBackType,feedbackText,null,formattedDate)
    }

    supportRoom.child(userFeedbackId).child(feedbackId).setValue(feedback).addOnCompleteListener { task ->
        if(task.isSuccessful){

            userRoom.child(userFeedbackId).child(feedbackId).setValue(feedback)
       // set the Support NewMessage
            supportNewMessagesRef.child(userFeedbackId).setValue(feedback)
        val intent = Intent(baseContext,UserFeedBackActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    subBtnListner!!.onSubmitBtnClicked()

}
    }

    private fun initFeedbacksBtns() {
        binding.feedBackADsBtn.setOnClickListener {
            manageFeedbackTypesBtn("ads", ADsfragment(), binding.feedBackADsBtn)
        }
        binding.feedBackFileNotFoundBtn.setOnClickListener {
            manageFeedbackTypesBtn("fileNotFound", FileNotFoundFragment(), binding.feedBackFileNotFoundBtn)
        }
        binding.feedBackFileCantPlayBtn.setOnClickListener {
            manageFeedbackTypesBtn("fileCan'tPlay", FileCantPlayFragment(), binding.feedBackFileCantPlayBtn)
        }
        binding.feedBackMusicStopBtn.setOnClickListener {
            manageFeedbackTypesBtn("musicStop", MusicStopFragment(), binding.feedBackMusicStopBtn)
        }
        binding.feedBackAppSlowBtn.setOnClickListener {
            manageFeedbackTypesBtn("appSlow", AppSlowFragement(),binding.feedBackAppSlowBtn)
        }
    binding.feedBackOthersBtn.setOnClickListener {
        manageFeedbackTypesBtn("others", OthersFragment(), binding.feedBackOthersBtn)
    }
        binding.toFeedBackBtn.setOnClickListener {
            val intent = Intent(this , UserFeedBackActivity::class.java)
        startActivity(intent)
        }
    }

    private fun manageFeedbackTypesBtn(btn: String, fragment: Fragment, feedBackAppSlowBtn: TextView) {
if(feedBackType != btn){
    disableSubBtn()
    feedBackType = btn
    feedbackText = ""
    replaceFragment(fragment)
    changeClikcedBtn(feedBackAppSlowBtn)

}
    }

    private fun changeClikcedBtn(clickedBtn: TextView) {
clickedBtn.setBackgroundResource(R.drawable.enable_btns_backround)
if(lastClickedBtn != null){
    lastClickedBtn!!.setBackgroundResource(R.drawable.disable_btns_backround)
}
 lastClickedBtn = clickedBtn
    }

    private fun replaceFragment(replacedFragment: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(binding.feedbackContainerFragment.id ,replacedFragment)
        ft.commit()
    }

    private fun addSimpleFeedFragment() {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val simpleFeedBackFragment = SimpleFeedbackFragment()
        ft.add(binding.feedbackContainerFragment.id ,simpleFeedBackFragment)
        ft.commit()
    }



    override fun OnFeedbackInputChanged(isEmpty: Boolean, feedbackText : String) {
 if(isEmpty){
     this.feedbackText = ""
    disableSubBtn()
 }else{
     enableSubBtn()
     this.feedbackText = feedbackText

 }
 }

    private fun enableSubBtn() {
        binding.feedBackSubmitBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        binding.feedBackSubmitBtn.setTextColor(resources.getColor(R.color.white))
        binding.feedBackSubmitBtn.isEnabled = true
    }
    private fun disableSubBtn() {
        binding.feedBackSubmitBtn.isEnabled = false
        binding.feedBackSubmitBtn.setBackgroundResource(R.drawable.disable_btns_backround)
        binding.feedBackSubmitBtn.setTextColor(resources.getColor(R.color.disable_text_color))
    }
    override fun onAdsBoxCheched(boxText: String, isChecked: Boolean) {
if(isChecked){
    adsCheckBoxesList.add(boxText)
}else{
    if(adsCheckBoxesList.isNotEmpty()){
        adsCheckBoxesList.remove(boxText)
    }
}

    }
    fun formatTimestamp(timestamp: Long): String {
        // Use "MMM dd, yyyy hh:mm a" for the desired format
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }

}