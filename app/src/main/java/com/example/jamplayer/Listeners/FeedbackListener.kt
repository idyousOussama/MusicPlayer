package com.example.jamplayer.Listeners

interface FeedbackListener {
    fun OnFeedbackInputChanged(isEmpty : Boolean , feedbackText : String)
fun onAdsBoxCheched (boxText : String , isChecked : Boolean)
}