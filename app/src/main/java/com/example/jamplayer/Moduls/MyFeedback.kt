package com.example.jamplayer.Moduls

import java.io.Serializable


 class MyFeedback (var feedbackId : String, var  lastMessage :FeedbackMessage?,var newMessagesNum : Int) : Serializable {
    constructor() : this("",null,0)
}