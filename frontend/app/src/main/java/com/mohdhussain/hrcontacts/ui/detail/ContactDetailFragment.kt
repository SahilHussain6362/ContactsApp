package com.mohdhussain.hrcontacts.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.remote.dto.EmailTemplateDto
import com.mohdhussain.hrcontacts.data.remote.dto.WhatsappTemplateDto
import com.mohdhussain.hrcontacts.data.remote.dto.label
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme
import com.mohdhussain.hrcontacts.util.ClipboardUtils

/**
 * Host for [ContactDetailScreen].
 *
 * Owns everything that leaves the app: the dialler, WhatsApp, the mail composer, LinkedIn and the
 * clipboard. All of that logic — including how a template is attached to an intent — is carried over
 * unchanged; the screen only decides which template was chosen and hands the index back.
 */
class ContactDetailFragment : Fragment() {

    private lateinit var viewModel: ContactDetailViewModel

    private val contactId: Long by lazy {
        arguments?.getLong("contactId") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            ContactDetailViewModelFactory(requireContext())
        )[ContactDetailViewModel::class.java]

        viewModel.loadContact(contactId)

        return ComposeView(requireContext()).apply {
            setContent {
                val contact by viewModel.contact.observeAsState()

                HrContactsTheme {
                    val current = contact
                    if (current == null) {
                        ContactDetailLoading(onBack = { findNavController().popBackStack() })
                    } else {
                        // Read on every recomposition rather than held: every template write
                        // refreshes the cached profile, so a template added a moment ago on the
                        // profile page is already here.
                        val emailTemplates = viewModel.emailTemplates
                        val whatsappTemplates = viewModel.whatsappTemplates

                        ContactDetailScreen(
                            name = current.name,
                            company = current.company,
                            mobile = current.mobile,
                            emails = current.emails,
                            linkedinProfile = current.linkedinProfile,
                            verified = current.verified,
                            isPrivate = current.isPrivate,
                            bookmarked = current.bookmarked,
                            emailTemplateLabels = emailTemplates.map { it.heading },
                            whatsappTemplateLabels = whatsappTemplates.map { it.label() },
                            onBack = { findNavController().popBackStack() },
                            onToggleBookmark = viewModel::toggleBookmark,
                            onEdit = {
                                findNavController().navigate(
                                    R.id.action_detail_to_edit,
                                    bundleOf("contactId" to contactId)
                                )
                            },
                            onDelete = {
                                viewModel.deleteContact { findNavController().popBackStack() }
                            },
                            onCall = { dial(current.mobile) },
                            onCopyMobile = {
                                ClipboardUtils.copyToClipboard(requireContext(), "Mobile", current.mobile)
                            },
                            onCopyEmail = { email ->
                                ClipboardUtils.copyToClipboard(requireContext(), "Email", email)
                            },
                            onCopyLinkedin = {
                                ClipboardUtils.copyToClipboard(
                                    requireContext(),
                                    "LinkedIn",
                                    current.linkedinProfile
                                )
                            },
                            onSendEmail = { email, templateIndex ->
                                sendEmail(email, templateIndex?.let(emailTemplates::getOrNull))
                            },
                            onSendWhatsapp = { templateIndex ->
                                sendWhatsapp(
                                    current.mobile,
                                    templateIndex?.let(whatsappTemplates::getOrNull)
                                )
                            },
                            onOpenLinkedin = { openLinkedin(current.linkedinProfile) }
                        )
                    }
                }
            }
        }
    }

    private fun dial(mobile: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile")))
    }

    private fun sendEmail(email: String, template: EmailTemplateDto?) {
        // Gmail ignores EXTRA_SUBJECT/EXTRA_TEXT when the recipient rides in the mailto: URI itself
        // (mailto:$email) — it only honours them when the address instead comes through EXTRA_EMAIL
        // and the URI is left as the bare "mailto:" scheme.
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            template?.let {
                putExtra(Intent.EXTRA_SUBJECT, it.heading)
                putExtra(Intent.EXTRA_TEXT, it.body)
            }
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show()
        }
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
            Toast.makeText(requireContext(), R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openLinkedin(profile: String) {
        val uri = Uri.parse(profile)
        val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.linkedin.android")
        }
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}
