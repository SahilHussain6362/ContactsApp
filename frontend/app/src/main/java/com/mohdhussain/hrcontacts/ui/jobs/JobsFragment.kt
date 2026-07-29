package com.mohdhussain.hrcontacts.ui.jobs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/** Host for [JobsScreen]. No ViewModel: the screen is static until there is a jobs feature behind it. */
class JobsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            HrContactsTheme {
                JobsScreen()
            }
        }
    }
}
