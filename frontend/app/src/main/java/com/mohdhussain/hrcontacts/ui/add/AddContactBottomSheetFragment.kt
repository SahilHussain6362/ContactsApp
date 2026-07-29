package com.mohdhussain.hrcontacts.ui.add

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme
import kotlinx.coroutines.launch

/**
 * Quick "add a contact" entry point: the centre nav bar button and the records list's own empty
 * state both open this rather than the full-screen [AddContactFragment], which stays reserved for
 * editing an existing contact from the detail screen.
 *
 * Always create-mode — there is no `contactId` to read — so this owns a fresh [AddContactViewModel]
 * rather than sharing one with a parent fragment the way the other sheets do.
 */
class AddContactBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: AddContactViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The form is long enough that the sheet's default half-height peek would hide the save
        // button under the keyboard; starting expanded is what a full add-contact form needs.
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            // A dialog's window defaults to adjustPan, which shifts the entire sheet upward when
            // the keyboard opens instead of resizing its content — on a form this tall that pans
            // fields (even ones in the top half) up past the top of the screen. adjustResize lets
            // the Compose content shrink instead, which is what its own imePadding() expects.
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            AddContactViewModelFactory(requireContext())
        )[AddContactViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                HrContactsTheme {
                    AddContactScreen(
                        isEditing = false,
                        initialValues = ContactFormValues(),
                        onBack = { dismiss() },
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
                viewModel.saveResult.collect { dismiss() }
            }
        }
    }

    companion object {
        const val TAG = "AddContactBottomSheet"
    }
}
