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
import com.mohdhussain.hrcontacts.databinding.ItemEmailInputBinding
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

        binding.btnAddEmail.setOnClickListener { addEmailRow() }

        if (isEditing) {
            viewModel.loadContact(contactId)
            viewModel.editContact.observe(viewLifecycleOwner) { contact ->
                contact ?: return@observe
                binding.etName.setText(contact.name)
                binding.etCompany.setText(contact.company)
                binding.etMobile.setText(contact.mobile)
                binding.etLinkedin.setText(contact.linkedinProfile)
                binding.switchVerified.isChecked = contact.verified
                binding.emailsContainer.removeAllViews()
                if (contact.emails.isEmpty()) {
                    addEmailRow()
                } else {
                    contact.emails.forEach { addEmailRow(it) }
                }
            }
        } else {
            addEmailRow()
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

    private fun addEmailRow(initialValue: String = "") {
        val rowBinding = ItemEmailInputBinding.inflate(layoutInflater, binding.emailsContainer, true)
        rowBinding.etEmailRow.setText(initialValue)
        rowBinding.btnRemoveEmail.setOnClickListener {
            binding.emailsContainer.removeView(rowBinding.root)
        }
    }

    private fun collectEmailRows(): List<ItemEmailInputBinding> =
        (0 until binding.emailsContainer.childCount).map { index ->
            ItemEmailInputBinding.bind(binding.emailsContainer.getChildAt(index))
        }

    private fun onSaveClicked() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val company = binding.etCompany.text?.toString()?.trim() ?: ""
        val mobile = binding.etMobile.text?.toString()?.trim() ?: ""
        val linkedin = binding.etLinkedin.text?.toString()?.trim() ?: ""

        val emailRows = collectEmailRows()
        val enteredEmails = emailRows.map { it.etEmailRow.text?.toString()?.trim() ?: "" }

        var valid = true

        // Name is optional — silently defaults to "Anonymous" in the ViewModel
        binding.nameLayout.error = null

        if (company.isEmpty()) {
            binding.companyLayout.error = getString(R.string.company_required)
            valid = false
        } else binding.companyLayout.error = null

        val mobileRegex = Regex("^\\+?[0-9]{7,15}$")
        val mobileValid = mobile.isEmpty() || mobile.matches(mobileRegex)

        if (!mobileValid) {
            binding.mobileLayout.error = getString(R.string.mobile_invalid)
            valid = false
        } else binding.mobileLayout.error = null

        var allEmailsValid = true
        emailRows.forEachIndexed { index, rowBinding ->
            val email = enteredEmails[index]
            val emailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
            if (!emailValid) {
                rowBinding.emailInputLayout.error = getString(R.string.email_invalid)
                allEmailsValid = false
                valid = false
            } else {
                rowBinding.emailInputLayout.error = null
            }
        }

        val validEmails = enteredEmails.filter { it.isNotEmpty() }

        if (mobileValid && allEmailsValid && mobile.isEmpty() && validEmails.isEmpty()) {
            binding.mobileLayout.error = getString(R.string.mobile_or_email_required)
            binding.tvEmailsError.text = getString(R.string.mobile_or_email_required)
            binding.tvEmailsError.visibility = View.VISIBLE
            valid = false
        } else {
            binding.tvEmailsError.visibility = View.GONE
        }

        if (valid) {
            viewModel.save(name, company, mobile, validEmails, linkedin, binding.switchVerified.isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
