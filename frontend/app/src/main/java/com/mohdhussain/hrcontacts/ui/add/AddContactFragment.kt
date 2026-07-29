package com.mohdhussain.hrcontacts.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme
import kotlinx.coroutines.launch

/**
 * Host for [AddContactScreen]. One screen serves both adding and editing, decided by whether a
 * `contactId` came in — exactly as before.
 */
class AddContactFragment : Fragment() {

    private lateinit var viewModel: AddContactViewModel

    private val contactId: Long by lazy {
        arguments?.getLong("contactId") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            AddContactViewModelFactory(requireContext())
        )[AddContactViewModel::class.java]

        val isEditing = contactId != -1L
        if (isEditing) {
            viewModel.loadContact(contactId)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val editContact by viewModel.editContact.observeAsState()

                // A new contact starts from blank values immediately; an existing one waits for the
                // database read, which is what puts the screen into its loading state.
                val initialValues = when {
                    !isEditing -> ContactFormValues()
                    else -> editContact?.let { contact ->
                        ContactFormValues(
                            name = contact.name,
                            company = contact.company,
                            mobile = contact.mobile,
                            emails = contact.emails.ifEmpty { listOf("") },
                            linkedin = contact.linkedinProfile,
                            isPrivate = contact.isPrivate
                        )
                    }
                }

                HrContactsTheme {
                    AddContactScreen(
                        isEditing = isEditing,
                        initialValues = initialValues,
                        onBack = { findNavController().popBackStack() },
                        onSave = { values ->
                            viewModel.save(
                                values.name,
                                values.company,
                                values.mobile,
                                values.emails,
                                values.linkedin,
                                values.isPrivate
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect {
                    findNavController().popBackStack()
                }
            }
        }
    }
}
