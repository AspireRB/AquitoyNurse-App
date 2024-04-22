package com.aspire.aquitoy.nurse.ui.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aspire.aquitoy.nurse.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val requestsViewModel =
            ViewModelProvider(this).get(com.aspire.aquitoy.nurse.ui.requests.RequestsViewModel::class.java)

        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRequest
        requestsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}