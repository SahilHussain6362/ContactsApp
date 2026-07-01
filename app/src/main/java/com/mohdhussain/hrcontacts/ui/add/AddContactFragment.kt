package com.mohdhussain.hrcontacts.ui.add

import android.os.Bundle
import android.util.Patterns
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.FragmentAddContactBinding
import kotlinx.coroutines.launch

class AddContactFragment : Fragment() {

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddContactViewModel

    private val contactId: Long by lazy {
        arguments?.getLong("contactId") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            AddContactViewModelFactory(requireContext())
        )[AddContactViewModel::class.java]

        val isEditing = contactId != -1L

        binding.toolbar.title = if (isEditing) getString(R.string.edit_contact) else getString(R.string.add_contact)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        if (isEditing) {
            viewModel.loadContact(contactId)
            viewModel.editContact.observe(viewLifecycleOwner) { contact ->
                contact ?: return@observe
                binding.etName.setText(contact.name)
                binding.etCompany.setText(contact.company)
                binding.etMobile.setText(contact.mobile)
                binding.etEmail.setText(contact.email)
                binding.etLinkedin.setText(contact.linkedinProfile)
            }
        }

        binding.btnSave.setOnClickListener { onSaveClicked() }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.saveResult.collect {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun onSaveClicked() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val company = binding.etCompany.text?.toString()?.trim() ?: ""
        val mobile = binding.etMobile.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val linkedin = binding.etLinkedin.text?.toString()?.trim() ?: ""

        var valid = true

        // Name is optional — silently defaults to "Anonymous" in the ViewModel
        binding.nameLayout.error = null

        if (company.isEmpty()) {
            binding.companyLayout.error = getString(R.string.company_required)
            valid = false
        } else binding.companyLayout.error = null

        val mobileRegex = Regex("^\\+?[0-9]{7,15}$")
        val mobileValid = mobile.isEmpty() || mobile.matches(mobileRegex)
        val emailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

        if (!mobileValid) {
            binding.mobileLayout.error = getString(R.string.mobile_invalid)
            valid = false
        } else binding.mobileLayout.error = null

        if (!emailValid) {
            binding.emailLayout.error = getString(R.string.email_invalid)
            valid = false
        } else binding.emailLayout.error = null

        if (mobileValid && emailValid && mobile.isEmpty() && email.isEmpty()) {
            binding.mobileLayout.error = getString(R.string.mobile_or_email_required)
            binding.emailLayout.error = getString(R.string.mobile_or_email_required)
            valid = false
        }

        if (valid) {
            viewModel.save(name, company, mobile, email, linkedin)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
