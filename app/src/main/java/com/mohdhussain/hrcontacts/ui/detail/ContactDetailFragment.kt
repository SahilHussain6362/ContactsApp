package com.mohdhussain.hrcontacts.ui.detail

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.FragmentContactDetailBinding
import com.mohdhussain.hrcontacts.util.ClipboardUtils

class ContactDetailFragment : Fragment() {

    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactDetailViewModel

    private val contactId: Long by lazy {
        arguments?.getLong("contactId") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ContactDetailViewModelFactory(requireContext())
        )[ContactDetailViewModel::class.java]

        viewModel.loadContact(contactId)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    findNavController().navigate(
                        R.id.action_detail_to_edit,
                        bundleOf("contactId" to contactId)
                    )
                    true
                }
                R.id.action_delete -> {
                    confirmAndDelete()
                    true
                }
                else -> false
            }
        }

        viewModel.contact.observe(viewLifecycleOwner) { contact ->
            contact ?: return@observe
            binding.tvName.text = contact.name
            binding.tvCompany.text = contact.company
            binding.tvMobile.text = contact.mobile
            binding.tvEmail.text = contact.email
            binding.tvInitial.text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

            binding.btnCopyMobile.setOnClickListener {
                ClipboardUtils.copyToClipboard(requireContext(), "Mobile", contact.mobile)
                Snackbar.make(binding.root, R.string.mobile_copied, Snackbar.LENGTH_SHORT).show()
            }

            binding.btnCopyEmail.setOnClickListener {
                ClipboardUtils.copyToClipboard(requireContext(), "Email", contact.email)
                Snackbar.make(binding.root, R.string.email_copied, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(requireContext())
            .setMessage("Delete this contact?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteContact {
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
