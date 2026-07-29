package com.mohdhussain.hrcontacts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/**
 * Renames the signed-in user. Shares [ProfileViewModel] with the host fragment — hence the
 * `requireParentFragment()` store owner and the `childFragmentManager` in `show` — so the header
 * updates from the same state this sheet writes to, with no result callback in between.
 */
class EditNameBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            requireParentFragment(),
            ProfileViewModelFactory(requireContext())
        )[ProfileViewModel::class.java]

        // Seeded once, outside the composition: EditNameSheetContent then owns the field, which is
        // what keeps an in-progress edit through a rotation.
        val user = viewModel.user.value

        return ComposeView(requireContext()).apply {
            setContent {
                HrContactsTheme {
                    EditNameSheetContent(
                        initialName = user?.name.orEmpty(),
                        email = user?.email.orEmpty(),
                        onSave = { name ->
                            viewModel.updateName(name)
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "EditNameBottomSheet"
    }
}
