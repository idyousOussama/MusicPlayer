package com.example.jamplayer.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jamplayer.R

class AdsProblemsAdapter : RecyclerView.Adapter<AdsProblemsAdapter.AdsProblemItemVH>() {
var adsProblemsList : ArrayList<String> = ArrayList()
fun setAdsProblemList (adsProblemsList : ArrayList<String>){
    this.adsProblemsList = adsProblemsList
}
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AdsProblemItemVH {
val view = LayoutInflater.from(p0.context).inflate(R.layout.ads_problem_custom_item , p0,false)
    return AdsProblemItemVH(view)
    }

    override fun getItemCount(): Int {
      return adsProblemsList.size
    }

    override fun onBindViewHolder(p0: AdsProblemItemVH, p1: Int) {
val adsProblem = adsProblemsList.get(p1)
        p0.problemText.setText(adsProblem)
    }

    class AdsProblemItemVH(itemView: View) : ViewHolder(itemView) {
val problemText =
    itemView.findViewById<TextView>(R.id.feedback_ads_problem_title)

    }

}