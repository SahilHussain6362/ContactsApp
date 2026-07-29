package com.mohdhussain.hrcontacts.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/**
 * Decides whether the user goes to their records or to sign-in. The decision itself is unchanged —
 * only what is on screen while it happens.
 */
class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            HrContactsTheme {
                SplashScreen()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val destination = if (AuthRepository.getInstance(requireContext()).isLoggedIn()) {
            R.id.contactListFragment
        } else {
            R.id.welcomeFragment
        }
        findNavController().navigate(
            destination,
            null,
            navOptions { popUpTo(R.id.splashFragment) { inclusive = true } }
        )
    }
}
