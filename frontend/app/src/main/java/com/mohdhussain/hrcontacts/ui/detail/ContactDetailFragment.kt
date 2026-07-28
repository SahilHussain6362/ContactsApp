package com.mohdhussain.hrcontacts.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.remote.dto.EmailTemplateDto
import com.mohdhussain.hrcontacts.data.remote.dto.WhatsappTemplateDto
import com.mohdhussain.hrcontacts.data.remote.dto.label
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
                R.id.action_bookmark -> {
                    viewModel.toggleBookmark()
                    true
                }
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

            binding.toolbar.menu.findItem(R.id.action_bookmark)?.apply {
                setIcon(
                    if (contact.bookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
                )
                setTitle(if (contact.bookmarked) R.string.bookmark_remove else R.string.bookmark_add)
            }

            if (contact.verified) {
                binding.tvVerifiedBadge.text = getString(R.string.verified)
                binding.tvVerifiedBadge.setBackgroundResource(R.drawable.bg_badge_verified)
                binding.tvVerifiedBadge.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.verified_badge_text)
                )
            } else {
                binding.tvVerifiedBadge.text = getString(R.string.not_verified)
                binding.tvVerifiedBadge.setBackgroundResource(R.drawable.bg_badge_unverified)
                binding.tvVerifiedBadge.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.unverified_badge_text)
                )
            }

            binding.tvPrivateBadge.visibility =
                if (contact.isPrivate) android.view.View.VISIBLE else android.view.View.GONE

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

            binding.btnWhatsapp.setOnClickListener { composeWhatsapp(contact.mobile) }

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
            rowBinding.btnSendEmailRow.setOnClickListener { composeEmail(email) }
        }
    }

    /**
     * Opens a mail composer, prefilled from one of the user's own templates.
     *
     * With a single template there is nothing to choose between, so it is applied straight away —
     * the composer is still editable, so this takes nothing away. More than one and the user picks.
     */
    private fun composeEmail(email: String) {
        val templates = viewModel.emailTemplates
        if (templates.size <= 1) {
            sendEmail(email, templates.firstOrNull())
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.templates_choose_email)
            .setItems(templates.map { it.heading }.toTypedArray()) { _, index ->
                sendEmail(email, templates[index])
            }
            .show()
    }

    private fun sendEmail(email: String, template: EmailTemplateDto?) {
        // mailto: carries the recipient; subject and body ride as extras, which every mail app
        // honours — encoding them into the URI itself is the part that is patchily supported.
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
            template?.let {
                putExtra(Intent.EXTRA_SUBJECT, it.heading)
                putExtra(Intent.EXTRA_TEXT, it.body)
            }
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.no_email_app, Snackbar.LENGTH_SHORT).show()
        }
    }

    /** The WhatsApp counterpart of [composeEmail]; a chat has no subject, so only a message. */
    private fun composeWhatsapp(mobile: String) {
        val templates = viewModel.whatsappTemplates
        if (templates.size <= 1) {
            sendWhatsapp(mobile, templates.firstOrNull())
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.templates_choose_whatsapp)
            .setItems(templates.map { it.label() }.toTypedArray()) { _, index ->
                sendWhatsapp(mobile, templates[index])
            }
            .show()
    }

    private fun sendWhatsapp(mobile: String, template: WhatsappTemplateDto?) {
        val cleaned = mobile.replace(Regex("[^0-9+]"), "")
        // wa.me takes the prefilled text as a percent-encoded query parameter.
        val url = buildString {
            append("https://wa.me/").append(cleaned)
            template?.let { append("?text=").append(Uri.encode(it.message)) }
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.whatsapp_not_installed, Snackbar.LENGTH_SHORT).show()
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
