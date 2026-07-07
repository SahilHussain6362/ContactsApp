package com.mohdhussain.hrcontacts.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForgotPasswordViewModel
    private var registeredEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ForgotPasswordViewModelFactory(requireContext())
        )[ForgotPasswordViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.tvBackToLogin.setOnClickListener { findNavController().popBackStack() }

        binding.btnSendOtp.setOnClickListener { onSendOtpClicked() }
        binding.btnResendOtp.setOnClickListener { viewModel.sendOtp(registeredEmail) }
        binding.btnResetPassword.setOnClickListener { onResetPasswordClicked() }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSendOtp.isEnabled = !loading
            binding.btnResetPassword.isEnabled = !loading
            binding.btnResendOtp.isEnabled = !loading
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sendOtpResult.collect { result ->
                    result.onSuccess {
                        showStage2()
                    }.onFailure { error ->
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetResult.collect { result ->
                    result.onSuccess {
                        findNavController().navigate(R.id.action_global_to_contact_list)
                    }.onFailure { error ->
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun onSendOtpClicked() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.email_invalid)
            return
        }
        binding.emailLayout.error = null
        registeredEmail = email
        viewModel.sendOtp(email)
    }

    private fun showStage2() {
        binding.stage1Group.visibility = View.GONE
        binding.stage2Group.visibility = View.VISIBLE
        binding.tvOtpSentTo.text = getString(R.string.otp_sent_to, registeredEmail)
    }

    private fun onResetPasswordClicked() {
        val otp = binding.etOtp.text?.toString()?.trim().orEmpty()
        val newPassword = binding.etNewPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        var valid = true

        if (otp.isEmpty()) {
            binding.otpLayout.error = getString(R.string.otp_required)
            valid = false
        } else {
            binding.otpLayout.error = null
        }

        if (newPassword.length < 8) {
            binding.newPasswordLayout.error = getString(R.string.password_too_short)
            valid = false
        } else {
            binding.newPasswordLayout.error = null
        }

        if (confirmPassword != newPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.passwords_dont_match)
            valid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        if (valid) {
            viewModel.resetPassword(registeredEmail, otp, newPassword)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
