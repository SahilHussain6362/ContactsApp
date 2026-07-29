package com.mohdhussain.hrcontacts.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mohdhussain.hrcontacts.BuildConfig
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    private lateinit var viewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            WelcomeViewModelFactory(requireContext())
        )[WelcomeViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                val loading by viewModel.loading.observeAsState(initial = false)
                HrContactsTheme {
                    WelcomeScreen(
                        loading = loading,
                        onGoogleSignIn = ::launchGoogleSignIn
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { result ->
                    result.onSuccess {
                        findNavController().navigate(R.id.action_global_to_contact_list)
                    }.onFailure { error ->
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(requireContext())
                val result = credentialManager.getCredential(request = request, context = requireActivity())
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    Toast.makeText(requireContext(), R.string.google_sign_in_failed, Toast.LENGTH_LONG).show()
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google sign-in failed", e)
                Toast.makeText(requireContext(), R.string.google_sign_in_failed, Toast.LENGTH_LONG).show()
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Google sign-in failed", e)
                Toast.makeText(requireContext(), R.string.google_sign_in_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "WelcomeFragment"
    }
}
