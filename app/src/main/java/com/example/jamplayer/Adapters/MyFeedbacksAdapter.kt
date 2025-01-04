package com.example.jamplayer.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.Listeners.MyFeedbackItemsListener
import com.example.jamplayer.Moduls.MyFeedback
import com.example.jamplayer.R

class MyFeedbacksAdapter : RecyclerView.Adapter<MyFeedbacksAdapter.MyFeedbacksCustomVH>() {

    var myFeedbacksList : ArrayList<MyFeedback>  = ArrayList()
lateinit var  myFeedbackListener : MyFeedbackItemsListener
fun setFeedbackListenr (myFeedbackListener : MyFeedbackItemsListener){
    this.myFeedbackListener = myFeedbackListener
}

fun clearFeedbackList(){
   if(myFeedbacksList.isNotEmpty()){
       myFeedbacksList.clear()
   }
}
fun setFeedbackItem(item : MyFeedback){
        myFeedbacksList.add(item)
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyFeedbacksCustomVH {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.my_feedback_custom_item,p0,false)
    return MyFeedbacksCustomVH(view)
    }

    override fun getItemCount(): Int {
     return myFeedbacksList.size    }

    override fun onBindViewHolder(p0: MyFeedbacksCustomVH, p1: Int) {
val feedback = myFeedbacksList.get(p1)

p0.setFeedback(feedback.lastMessage!!.messageText,"${p0.itemView.context.getString(R.string.upDatedOn_text)} ${feedback.lastMessage!!.sentDate}",feedback.newMessagesNum)
    p0.itemView.setOnClickListener{
        myFeedbackListener.onMyFeedbackItemClick(feedback)
    }
    }
    class MyFeedbacksCustomVH(itemView: View) : ViewHolder(itemView) {
val lastMessage = itemView.findViewById<TextView>(R.id.feedback_conver_last_message)
 val upDatedDate = itemView.findViewById<TextView>(R.id.feedback_conver_last_message_date)
 val newMessageNum = itemView.findViewById<TextView>(R.id.feedback_newMessageMessage_Num_text)
        fun setFeedback (message : String , date : String ,num : Int){
            lastMessage.setText(message)
            upDatedDate.setText(date)
            if(num == 0){
                newMessageNum.visibility = View.GONE
            }else{
                newMessageNum.visibility = View.VISIBLE
                newMessageNum.setText(num.toString())
            }

        }


    }
}