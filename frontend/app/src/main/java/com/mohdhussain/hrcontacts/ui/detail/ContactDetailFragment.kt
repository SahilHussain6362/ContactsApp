package com.mohdhussain.hrcontacts.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import com.mohdhussain.hrcontacts.databinding.ItemEmailDetailRowBinding
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
            binding.tvInitial.text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

            if (contact.emails.isNotEmpty()) {
                binding.emailCard.visibility = android.view.View.VISIBLE
                bindEmailRows(contact.emails)
            } else {
                binding.emailCard.visibility = android.view.View.GONE
            }

            if (contact.linkedinProfile.isNotEmpty()) {
                binding.linkedinCard.visibility = android.view.View.VISIBLE
                binding.tvLinkedin.text = contact.linkedinProfile
            } else {
                binding.linkedinCard.visibility = android.view.View.GONE
            }

            binding.btnCopyMobile.setOnClickListener {
                ClipboardUtils.copyToClipboard(requireContext(), "Mobile", contact.mobile)
                Snackbar.make(binding.root, R.string.mobile_copied, Snackbar.LENGTH_SHORT).show()
            }

            binding.btnCall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.mobile}")))
            }

            binding.btnWhatsapp.setOnClickListener {
                val cleaned = contact.mobile.replace(Regex("[^0-9+]"), "")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleaned")).apply {
                    setPackage("com.whatsapp")
                }
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(binding.root, R.string.whatsapp_not_installed, Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.btnOpenLinkedin.setOnClickListener {
                val uri = Uri.parse(contact.linkedinProfile)
                val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.linkedin.android")
                }
                try {
                    startActivity(appIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }

            binding.btnCopyLinkedin.setOnClickListener {
                ClipboardUtils.copyToClipboard(requireContext(), "LinkedIn", contact.linkedinProfile)
                Snackbar.make(binding.root, R.string.linkedin_copied, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindEmailRows(emails: List<String>) {
        binding.emailsContainer.removeAllViews()
        emails.forEach { email ->
            val rowBinding = ItemEmailDetailRowBinding.inflate(layoutInflater, binding.emailsContainer, true)
            rowBinding.tvEmailRow.text = email
            rowBinding.btnCopyEmailRow.setOnClickListener {
                ClipboardUtils.copyToClipboard(requireContext(), "Email", email)
                Snackbar.make(binding.root, R.string.email_copied, Snackbar.LENGTH_SHORT).show()
            }
            rowBinding.btnSendEmailRow.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(binding.root, R.string.no_email_app, Snackbar.LENGTH_SHORT).show()
                }
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
