package com.example.jamplayer.Activities.Songs

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.firebaseDB
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.user

import com.example.jamplayer.Adapters.MyFeedbacksAdapter
import com.example.jamplayer.Listeners.MyFeedbackItemsListener
import com.example.jamplayer.Moduls.FeedbackMessage
import com.example.jamplayer.Moduls.MyFeedback
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityUserFeedBackBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class UserFeedBackActivity : AppCompatActivity() {
   private lateinit var binding : ActivityUserFeedBackBinding
val feedbacksAdapter = MyFeedbacksAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding =ActivityUserFeedBackBinding.inflate(layoutInflater)
    setContentView(binding.root)
        userIsActive = true
        checkUserNetworck()
        refreshPage()
        initbackBtn()
    }
    private fun initbackBtn() {
        binding.myFeedBackGoBackBtn.setOnClickListener {
            finish()
        }
    }
    private fun refreshPage() {
        binding.swipe.setOnRefreshListener(OnRefreshListener {

               feedbacksAdapter.clearFeedbackList()
               binding.feedbacksListLayout.visibility = View.GONE
               checkUserNetworck()


        })

    }

    private fun checkUserNetworck() {
        val connMgr =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isWifiConn = false
        var isMobileConn = false
        for (network in connMgr.allNetworks) {
            val networkInfo = connMgr.getNetworkInfo(network)
            if (networkInfo!!.type == ConnectivityManager.TYPE_WIFI) {
                isWifiConn = isWifiConn or networkInfo!!.isConnected
            }
            if (networkInfo!!.type == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn = isMobileConn or networkInfo!!.isConnected
            }
        }
        if (isWifiConn == false && isMobileConn == false){

            setFeedbackWraning(R.drawable.no_internet,R.string.no_connection_message_text)

        }else{
            binding.myFeedbackProgress.visibility = View.VISIBLE
            binding.wraningLayout.visibility = View.GONE
            getFeedbacksIds()
            setFeedbackListener()
        }
    }

    private fun  setFeedbackListener() {
        feedbacksAdapter.setFeedbackListenr(object : MyFeedbackItemsListener{
            override fun onMyFeedbackItemClick(feedback: MyFeedback) {
                val feedbackIntent = Intent(baseContext, ShowFeedbackConversationActivity::class.java)
                feedbackIntent. putExtra("feedback" , feedback)
                startActivity(feedbackIntent)
                finish()
            }

        })
    }

    private fun getFeedbacksIds() {
        val userFeedbacksIds = firebaseDB.getReference("FeedbacksIds").child(user!!.userId)
        userFeedbacksIds.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    for (item in p0.children){
val feedbackId = item.getValue(String::class.java)
                        if(feedbackId != null){
                            getFeedbacks(feedbackId)
                        }
                    }
                }else{
                    setFeedbackWraning(R.drawable.no_feedback,R.string.no_feed_backs_message)

                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setFeedbackWraning(wraningImage: Int, wraningMessage: Int) {
        binding.swipe.setRefreshing(false)
        binding.myFeedbackProgress.visibility = View.GONE
        binding.feedbacksListLayout.visibility = View.GONE
        binding.myFeedbacksWraningImage.setImageResource(wraningImage)
        binding.myFeedbacksWraningMessage.setText(wraningMessage)
        binding.wraningLayout.visibility = View.VISIBLE

    }

    private fun getFeedbacks(feedBackId: String?) {
        val feedbacksRef = firebaseDB.getReference("FeedbacksRooms").child(user!!.userId + "support0766501026")
         feedbacksRef.child(feedBackId!!).limitToLast(1).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        for(messageItem in p0.children ){
                            val message = messageItem.getValue(FeedbackMessage::class.java)
                            if(message != null){
                                getNewMessages(message,feedBackId)
                            }
                        }
                        }
                }
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
    private fun getNewMessages(message: FeedbackMessage, feedBackId: String) {
      var newMessageCounter = 0
        val userNewMessagesRef = firebaseDB.getReference("NewMessages").child(user!!.userId + "support0766501026")
        userNewMessagesRef.child(feedBackId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
if (p0.exists()){
    for(item in p0.children){
        newMessageCounter++
    }
}
    setList(newMessageCounter,message, feedBackId)

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun setList(newMessageCounter: Int,message: FeedbackMessage, feedBackId: String) {
        val feedback = MyFeedback(feedBackId,message,newMessageCounter)
        feedbacksAdapter.setFeedbackItem(feedback)
        setFeedbacksList()
    }

    private fun setFeedbacksList() {
    if(feedbacksAdapter.myFeedbacksList.isNotEmpty()){
        binding.swipe.setRefreshing(false)
        binding.myFeedbackRV.apply {
            layoutManager = LinearLayoutManager(this@UserFeedBackActivity)
            setHasFixedSize(true)
            adapter = feedbacksAdapter
        }
        binding.myFeedbackProgress.visibility = View.GONE
        binding.feedbacksListLayout.visibility = View.VISIBLE
    }else{
        setFeedbackWraning(R.drawable.no_feedback,R.string.no_feed_backs_message)
    }

    }
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
}