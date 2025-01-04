package com.example.jamplayer.Activities


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.firebaseDB
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.user
import com.example.jamplayer.Adapters.FeedbackMessageAdapter
import com.example.jamplayer.Moduls.FeedbackMessage
import com.example.jamplayer.Moduls.MyFeedback
import com.example.jamplayer.R
import com.example.jamplayer.databinding.ActivityShowFeedbackConversationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ShowFeedbackConversationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityShowFeedbackConversationBinding
    val feedbackMessageAdapter = FeedbackMessageAdapter()
    private lateinit var myFeedback : MyFeedback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShowFeedbackConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)
getIntentFeedback()
        sendNewFeedbackMessage()
        initSubBtn()
        removeNewMessage()
        initbackBtn()
    }
    private fun initbackBtn() {
        binding.showFeedbackDetailsBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun removeNewMessage() {
        val supportNewMessagesRef = firebaseDB.getReference("NewMessages").child( user!!.userId + "support0766501026")
        supportNewMessagesRef.child(myFeedback.feedbackId).removeValue()
    }


    fun initSubBtn(){
    binding.feedbackMessageInput.addTextChangedListener {
        val messageText = binding.feedbackMessageInput.text.toString()
        if(messageText.isEmpty()){
            binding.feedbackDetailsSubBtn.isEnabled = false
            binding.feedbackDetailsSubBtn.setBackgroundResource(R.drawable.disable_btns_backround)
            binding.feedbackDetailsSubBtn.setTextColor(resources.getColor(R.color.disable_text_color))
        }else{
            binding.feedbackDetailsSubBtn.isEnabled = true
            binding.feedbackDetailsSubBtn.setBackgroundResource(R.drawable.enable_btns_backround)
            binding.feedbackDetailsSubBtn.setTextColor(resources.getColor(R.color.white))
        }
    }
}
    private fun sendNewFeedbackMessage() {
        binding.feedbackDetailsSubBtn.setOnClickListener {
            val messageText = binding.feedbackMessageInput.text.toString()
            if(messageText.isNotEmpty()){
                val supportNewMessagesRef = firebaseDB.getReference("NewMessages").child( "support0766501026" + user!!.userId  )
                val userRoom = firebaseDB.getReference("FeedbacksRooms").child(user!!.userId + "support0766501026")
                val userFeedbacksIds = firebaseDB.getReference("FeedbacksIds")
                val supportRoom = firebaseDB.getReference("FeedbacksRooms").child("support0766501026" + user!!.userId  )
                val feedbackId = userFeedbacksIds.push().key.toString()
                // timestamp (current time)
                val currentTimestamp = System.currentTimeMillis()
                // Format and display the date
                val formattedDate = formatTimestamp(currentTimestamp)
                val  feedback = FeedbackMessage(feedbackId,user!!.userId,null,messageText,null,formattedDate)
                supportRoom.child(myFeedback.feedbackId).child(feedbackId).setValue(feedback).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        userRoom.child(myFeedback.feedbackId).child(feedbackId).setValue(feedback)
                     // add newMessage to SupportRom
                        supportNewMessagesRef.child(myFeedback.feedbackId).child(feedbackId).setValue(feedback)
                        feedbackMessageAdapter.addNewFeedbackMessage(feedback)
                        binding.feedbackMessagesRv.smoothScrollToPosition(feedbackMessageAdapter.itemCount - 1)
                        binding.feedbackMessageInput.setText("")
                    }
                }
            }

        }
    }

    private fun getIntentFeedback() {
         myFeedback = intent.getSerializableExtra("feedback") as MyFeedback
    if(myFeedback != null){
        getFeedback(myFeedback)
    }
    }
    private fun getFeedback(feedback: MyFeedback) {
        val feedBackMessageList : ArrayList<FeedbackMessage> = ArrayList()
val feedbackRef =firebaseDB.getReference("FeedbacksRooms").child(user!!.userId + "support0766501026")
 feedbackRef.child(feedback.feedbackId).addListenerForSingleValueEvent(object : ValueEventListener{
     override fun onDataChange(p0: DataSnapshot) {
         if(p0.exists()){
             for (feedbackItem in p0.children){
                 val feedbackMessage = feedbackItem.getValue(FeedbackMessage::class.java)
                 if(feedbackMessage != null){
                     feedBackMessageList.add(feedbackMessage)
                 }
             }
             if(feedBackMessageList.isNotEmpty()){
                 setFeedbackList(feedBackMessageList)
             }
         }
     }
     override fun onCancelled(p0: DatabaseError) {
         TODO("Not yet implemented")
     }
 })
    }
    private fun setFeedbackList(feedBackMessagesList: ArrayList<FeedbackMessage>) {

        feedbackMessageAdapter.setFeedbackMessageList(feedBackMessagesList)
binding.myFeedbackMessagesProgress.visibility = View.GONE
        binding.feedbackMessagesRv.apply {
        layoutManager = LinearLayoutManager(baseContext)
        setHasFixedSize(true)
        adapter = feedbackMessageAdapter
    visibility = View.VISIBLE
    }
        binding.feedbackMessagesRv.smoothScrollToPosition(feedbackMessageAdapter.itemCount - 1)
    }
    fun formatTimestamp(timestamp: Long): String {
        // Use "MMM dd, yyyy hh:mm a" for the desired format
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}