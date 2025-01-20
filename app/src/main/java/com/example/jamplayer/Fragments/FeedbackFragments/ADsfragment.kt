package com.example.jamplayer.Fragments.FeedbackFragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.jamplayer.Activities.Songs.FeedBackActivity.SubmitFeedbackBtnManager.subBtnListner
import com.example.jamplayer.Listeners.FeedbackListener
import com.example.jamplayer.Listeners.SubmitButtonListener
import com.example.jamplayer.databinding.FragmentADsfragmentBinding



class ADsfragment : Fragment() {
 private var feedBackListener : FeedbackListener? = null
    private var _binding: FragmentADsfragmentBinding? = null
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
        _binding = FragmentADsfragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.feedBackAdsExplainInpute.addTextChangedListener {
            val adsFeedbackText =  binding.feedBackAdsExplainInpute.text.toString()
                feedBackListener!!.OnFeedbackInputChanged(adsFeedbackText.isEmpty(),adsFeedbackText)

        }
binding.AdsCheckBoxNum1.setOnClickListener{
    setCheeckBoxListener(binding.AdsCheckBoxNum1.text.toString(),binding.AdsCheckBoxNum1.isChecked)
}
binding.AdsCheckBoxNum2.setOnClickListener{
    setCheeckBoxListener(binding.AdsCheckBoxNum2.text.toString(),binding.AdsCheckBoxNum2.isChecked)
}
binding.AdsCheckBoxNum3.setOnClickListener{
    setCheeckBoxListener(binding.AdsCheckBoxNum3.text.toString(),binding.AdsCheckBoxNum3.isChecked)
}
binding.AdsCheckBoxNum4.setOnClickListener{
    setCheeckBoxListener(binding.AdsCheckBoxNum4.text.toString(),binding.AdsCheckBoxNum4.isChecked)

}
binding.AdsCheckBoxNum5.setOnClickListener{
    setCheeckBoxListener(binding.AdsCheckBoxNum5.text.toString(),binding.AdsCheckBoxNum5.isChecked)
}
binding.AdsCheckBoxNum6.setOnClickListener{
    setCheeckBoxListener(binding.AdsCheckBoxNum6.text.toString(),binding.AdsCheckBoxNum6.isChecked)
}
        subBtnListner =object : SubmitButtonListener {
            override fun onSubmitBtnClicked() {
                binding.feedBackAdsExplainInpute.setText("")
            }

        }
    }

    private fun setCheeckBoxListener(boxText: String, checked: Boolean) {
        feedBackListener!!.onAdsBoxCheched(boxText,checked)

    }


}