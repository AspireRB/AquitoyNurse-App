package com.aspire.aquitoy.nurse.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aspire.aquitoy.nurse.databinding.FragmentProfileBinding
import com.aspire.aquitoy.nurse.ui.introduction.IntroductionActivity
import com.aspire.aquitoy.nurse.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings

@AndroidEntryPoint
@WithFragmentBindings
class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textProfile
        profileViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        initUI()
        return root
    }

    private fun initUI() {
        initListeners()
    }

    private fun initListeners() {
        binding.btnLogout.setOnClickListener {
            profileViewModel.logout { navigateToIntroduction() }
        }
    }

    private fun navigateToIntroduction() {
        startActivity(Intent(requireContext(), IntroductionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}