package com.aspire.aquitoy.nurse.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aspire.aquitoy.nurse.databinding.ActivityLoginBinding
import com.aspire.aquitoy.nurse.ui.FragmentsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUIState()
    }

    private fun initUIState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.isLoading.collect {
                    binding.pbLoading.isVisible = it
                }
            }
        }
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            navigateToIntroduction()
        }

        binding.btnLogin.setOnClickListener{
            loginViewModel.login(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
            ) { navigateToFragment() }
        }
    }

    private fun navigateToIntroduction() {
        finish()
    }

    private fun navigateToFragment() {
        startActivity(Intent(this, FragmentsActivity::class.java))
    }
}