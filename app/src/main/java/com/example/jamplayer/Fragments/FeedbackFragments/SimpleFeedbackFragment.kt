package com.example.jamplayer.Fragments.FeedbackFragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.example.jamplayer.Activities.FeedBackActivity.SubmitFeedbackBtnManager.subBtnListner
import com.example.jamplayer.Listeners.FeedbackListener
import com.example.jamplayer.Listeners.SubmitButtonListener
import com.example.jamplayer.R
import com.example.jamplayer.databinding.FragmentOthersBinding
import com.example.jamplayer.databinding.FragmentSimpleFeedbackBinding

class SimpleFeedbackFragment : Fragment() {
    private  var feedBackListener : FeedbackListener? = null
    private var _binding: FragmentSimpleFeedbackBinding? = null
    private val binding get() = _binding!!
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the activity implements the interface
        if (context is FeedbackListener) {
            feedBackListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSimpleFeedbackBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.simpleExplainInpute.addTextChangedListener{
            val  feedbackText = binding.simpleExplainInpute.text.toString()
            feedBackListener!!.OnFeedbackInputChanged(feedbackText.isEmpty(),feedbackText)
        }
        subBtnListner =object : SubmitButtonListener{
            override fun onSubmitBtnClicked() {
                binding.simpleExplainInpute.setText("")
            }

        }

    }







}