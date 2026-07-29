package com.mohdhussain.hrcontacts.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/**
 * Filter sheet that slides up from the bottom over a dimmed list (the dim scrim and
 * the slide-in transition both come from [BottomSheetDialogFragment]).
 *
 * It shares [ContactListViewModel] with the host fragment — hence the
 * `requireParentFragment()` store owner and the `childFragmentManager` in `show` —
 * so it can read the company list and publish the chosen criteria back without
 * needing a result callback. That arrangement is unchanged; only the sheet's contents are Compose.
 */
class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ContactListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            requireParentFragment(),
            ContactListViewModelFactory(requireContext())
        )[ContactListViewModel::class.java]

        // Read once, outside the composition: this is the seed for the sheet's controls, and
        // FilterSheetContent deliberately ignores later changes so an in-progress edit survives.
        val initialFilter = viewModel.filter.value ?: ContactFilter()

        return ComposeView(requireContext()).apply {
            setContent {
                val companies by viewModel.companies.observeAsState(initial = emptyList())
                HrContactsTheme {
                    FilterSheetContent(
                        companies = companies,
                        initialFilter = initialFilter,
                        onApply = { filter ->
                            viewModel.applyFilter(filter)
                            dismiss()
                        },
                        onClearAll = {
                            viewModel.clearFilter()
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "FilterBottomSheet"
    }
}
