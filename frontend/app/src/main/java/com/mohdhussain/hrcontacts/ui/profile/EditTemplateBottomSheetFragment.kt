package com.mohdhussain.hrcontacts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/**
 * Writes one message template — a new one when [ARG_TEMPLATE_ID] is absent, otherwise a replacement
 * for that template.
 *
 * Shares [ProfileViewModel] with the host fragment — hence the `requireParentFragment()` store owner
 * and the `childFragmentManager` in `show` — so the list behind it redraws from the same state this
 * sheet writes to.
 */
class EditTemplateBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ProfileViewModel

    private val type: TemplateType by lazy {
        TemplateType.valueOf(requireArguments().getString(ARG_TYPE) ?: TemplateType.EMAIL.name)
    }

    /** Null while adding. */
    private val templateId: String? by lazy { arguments?.getString(ARG_TEMPLATE_ID) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            requireParentFragment(),
            ProfileViewModelFactory(requireContext())
        )[ProfileViewModel::class.java]

        val id = templateId
        val isEditing = id != null

        // Read once, outside the composition, for the same reason as the other sheets: the content
        // owns its fields from then on so a rotation does not discard an in-progress edit.
        var initialHeading = ""
        var initialBody = ""
        if (id != null) {
            when (type) {
                TemplateType.EMAIL -> viewModel.emailTemplate(id)?.let {
                    initialHeading = it.heading
                    initialBody = it.body
                }
                TemplateType.WHATSAPP -> viewModel.whatsappTemplate(id)?.let {
                    initialBody = it.message
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                HrContactsTheme {
                    TemplateSheetContent(
                        type = type,
                        isEditing = isEditing,
                        initialHeading = initialHeading,
                        initialBody = initialBody,
                        onSave = { heading, body ->
                            when (type) {
                                TemplateType.EMAIL ->
                                    viewModel.saveEmailTemplate(id, heading, body)
                                TemplateType.WHATSAPP ->
                                    viewModel.saveWhatsappTemplate(id, body)
                            }
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "EditTemplateBottomSheet"

        private const val ARG_TYPE = "templateType"
        private const val ARG_TEMPLATE_ID = "templateId"

        fun newInstance(type: TemplateType, templateId: String? = null) =
            EditTemplateBottomSheetFragment().apply {
                arguments = bundleOf(ARG_TYPE to type.name, ARG_TEMPLATE_ID to templateId)
            }
    }
}
