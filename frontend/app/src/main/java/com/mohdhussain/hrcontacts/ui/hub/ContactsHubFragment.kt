package com.mohdhussain.hrcontacts.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.list.ContactListScope
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/** Host for [ContactsHubScreen]. Pushes [com.mohdhussain.hrcontacts.ui.list.ContactListFragment] scoped to whichever folder was tapped. */
class ContactsHubFragment : Fragment() {

    private lateinit var viewModel: ContactsHubViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            ContactsHubViewModelFactory(requireContext())
        )[ContactsHubViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                val counts by viewModel.counts.observeAsState(initial = 0 to 0)

                HrContactsTheme {
                    ContactsHubScreen(
                        yourContactsCount = counts.first,
                        bookmarksCount = counts.second,
                        onYourContactsClick = { openFolder(ContactListScope.MINE) },
                        onBookmarksClick = { openFolder(ContactListScope.BOOKMARKED) }
                    )
                }
            }
        }
    }

    private fun openFolder(scope: ContactListScope) {
        findNavController().navigate(
            R.id.contactsFilteredFragment,
            bundleOf("scope" to scope.name)
        )
    }
}
