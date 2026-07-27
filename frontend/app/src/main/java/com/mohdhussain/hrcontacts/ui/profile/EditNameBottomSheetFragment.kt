package com.mohdhussain.hrcontacts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.SheetEditNameBinding

/**
 * Renames the signed-in user. Shares [ProfileViewModel] with the host fragment — hence the
 * `requireParentFragment()` store owner and the `childFragmentManager` in `show` — so the header
 * updates from the same state this sheet writes to, with no result callback in between.
 */
class EditNameBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SheetEditNameBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetEditNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireParentFragment(),
            ProfileViewModelFactory(requireContext())
        )[ProfileViewModel::class.java]

        val user = viewModel.user.value
        binding.tvEmailReadOnly.text = user?.email.orEmpty()

        // Prefill only on first open — on rotation the field restores itself, and overwriting it
        // would discard an in-progress edit.
        if (savedInstanceState == null) {
            binding.etName.setText(user?.name.orEmpty())
            binding.etName.setSelection(binding.etName.text?.length ?: 0)
        }

        binding.btnSaveName.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.profile_name_required)
            return
        }
        binding.nameLayout.error = null
        viewModel.updateName(name)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditNameBottomSheet"
    }
}
