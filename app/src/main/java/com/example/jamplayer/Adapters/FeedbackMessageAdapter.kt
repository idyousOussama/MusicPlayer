package com.example.jamplayer.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.user
import com.example.jamplayer.Moduls.FeedbackMessage
import com.example.jamplayer.R

class FeedbackMessageAdapter : RecyclerView.Adapter<FeedbackMessageAdapter.FeedbackMessageVH>() {

    private var feedbackMessagesList: ArrayList<FeedbackMessage> = ArrayList()

    fun setFeedbackMessageList(feedbackMessagesList: ArrayList<FeedbackMessage>) {
        this.feedbackMessagesList = feedbackMessagesList
    }

    fun addNewFeedbackMessage(feedbackMessage: FeedbackMessage) {
        feedbackMessagesList.add(feedbackMessage)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FeedbackMessageVH {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.feedback_message_custom_item, p0, false)
        return FeedbackMessageVH(view)
    }

    override fun getItemCount(): Int {
        return feedbackMessagesList.size
    }

    override fun onBindViewHolder(p0: FeedbackMessageVH, p1: Int) {
        val feedbackMessage = feedbackMessagesList.getOrNull(p1)
        if (feedbackMessage == null) {
            Log.e("FeedbackMessageAdapter", "Feedback message at position $p1 is null")
            return
        }

        if (user == null) {
            Log.e("FeedbackMessageAdapter", "User is null")
            return
        }

        val messageType = feedbackMessage.messageType ?: ""
        val sentDate = feedbackMessage.sentDate ?: "Unknown date"
        val messageText = feedbackMessage.messageText ?: "No message"

        if (feedbackMessage.sender == user!!.userId) {

            p0.getFeedback(
                R.drawable.user_holder_image,
                R.string.me_text,
                sentDate,
                messageText,
                messageType!!,
                feedbackMessage.selectedFeedbackList
            )
        } else {
            p0.getFeedback(
                R.drawable.app_logo,
                R.string.app_name,
                sentDate,
                messageText,
                messageType!!,
                feedbackMessage.selectedFeedbackList
            )
        }
    }

    class FeedbackMessageVH(itemView: View) : ViewHolder(itemView) {
        private val feedbackSenderImage =
            itemView.findViewById<ImageView>(R.id.feedback_message_items_sender_Img)
        private val feedbackSenderTitle =
            itemView.findViewById<TextView>(R.id.feedback_message_items_sender_title)
        private val feedbackMessageSentDate =
            itemView.findViewById<TextView>(R.id.feedback_message_items_sent_date)
        private val feedbackMessageText =
            itemView.findViewById<TextView>(R.id.feedback_message_items_message_text)
        private val feedbackFeedbackType =
            itemView.findViewById<TextView>(R.id.feedback_message_items_feedbackType)
        private val feedbackFeedbackAdsProblemList =
            itemView.findViewById<RecyclerView>(R.id.ads_problem_RV)

        fun getFeedback(
            image: Int,
            title: Int,
            sentDate: String,
            message: String,
            type: String,
            selectedFeedbackList: ArrayList<String>?
        ) {
            feedbackSenderImage.setImageResource(image)
            feedbackSenderTitle.setText(title)
            feedbackMessageSentDate.text = sentDate
            feedbackMessageText.text = message
            if (type.isNotEmpty()) {
                if(type == "ads"){
                  if(selectedFeedbackList!!.isNotEmpty()){
                      setAdsProblemsLis(feedbackFeedbackAdsProblemList , selectedFeedbackList)
                      feedbackFeedbackType.text = type
                  }else{
                      feedbackFeedbackType.text = type
                      feedbackFeedbackType.visibility = View.VISIBLE
                  }
                }else{
                    feedbackFeedbackType.text = type
                    feedbackFeedbackType.visibility = View.VISIBLE
                }

            } else {
                feedbackFeedbackType.visibility = View.GONE
            }
        }

        private fun setAdsProblemsLis(Rv: RecyclerView?, selectedFeedbackList: ArrayList<String>) {
val adsProblemsAdapter = AdsProblemsAdapter()
adsProblemsAdapter.setAdsProblemList(selectedFeedbackList)
            Rv?.apply {
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
                setHasFixedSize(true)
                adapter = adsProblemsAdapter

            }
        }
    }
}
