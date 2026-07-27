package com.mohdhussain.hrcontacts.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.SheetFilterBinding

/**
 * Filter sheet that slides up from the bottom over a dimmed list (the dim scrim and
 * the slide-in transition both come from [BottomSheetDialogFragment]).
 *
 * It shares [ContactListViewModel] with the host fragment — hence the
 * `requireParentFragment()` store owner and the `childFragmentManager` in [show] —
 * so it can read the company list and publish the chosen criteria back without
 * needing a result callback.
 */
class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SheetFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireParentFragment(),
            ContactListViewModelFactory(requireContext())
        )[ContactListViewModel::class.java]

        viewModel.companies.observe(viewLifecycleOwner) { companies ->
            // Index 0 is the "no company filter" option; the rest are real companies.
            binding.etCompany.setSimpleItems(
                (listOf(getString(R.string.filter_any_company)) + companies).toTypedArray()
            )
        }

        // Prefill from the applied filter only on first open — on rotation the views
        // restore themselves, and overwriting them would drop in-progress edits.
        if (savedInstanceState == null) {
            val current = viewModel.filter.value ?: ContactFilter()
            binding.etCompany.setText(current.company.ifBlank { getString(R.string.filter_any_company) }, false)
            binding.switchHasPhone.isChecked = current.hasPhone
            binding.switchHasEmail.isChecked = current.hasEmail
            binding.switchVerified.isChecked = current.verifiedOnly
        }

        binding.btnApply.setOnClickListener {
            viewModel.applyFilter(readFilter())
            dismiss()
        }

        binding.btnClearAll.setOnClickListener {
            viewModel.clearFilter()
            dismiss()
        }
    }

    private fun readFilter(): ContactFilter {
        val anyCompany = getString(R.string.filter_any_company)
        val company = binding.etCompany.text?.toString()?.trim().orEmpty()
        return ContactFilter(
            company = if (company == anyCompany) "" else company,
            hasPhone = binding.switchHasPhone.isChecked,
            hasEmail = binding.switchHasEmail.isChecked,
            verifiedOnly = binding.switchVerified.isChecked
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheet"
    }
}
