package com.example.jamplayer.Moduls

import java.io.Serializable

data class FeedbackMessage(var messageId :String, var sender : String, var messageType : String?, var messageText : String, var selectedFeedbackList :ArrayList<String>? , var sentDate: String):Serializable{
    constructor() : this("","","","",null,"")
 }
