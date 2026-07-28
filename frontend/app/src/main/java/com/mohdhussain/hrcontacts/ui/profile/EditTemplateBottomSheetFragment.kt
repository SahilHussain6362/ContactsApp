package com.mohdhussain.hrcontacts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.SheetEditTemplateBinding

/**
 * Writes one message template — a new one when [ARG_TEMPLATE_ID] is absent, otherwise a replacement
 * for that template.
 *
 * One sheet serves both types: the heading field is simply gone for WhatsApp, which has no subject
 * line to prefill. Shares [ProfileViewModel] with the host fragment — hence the
 * `requireParentFragment()` store owner and the `childFragmentManager` in `show` — so the list
 * behind it redraws from the same state this sheet writes to.
 */
class EditTemplateBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: SheetEditTemplateBinding? = null
    private val binding get() = _binding!!

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
        _binding = SheetEditTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireParentFragment(),
            ProfileViewModelFactory(requireContext())
        )[ProfileViewModel::class.java]

        val isEditing = templateId != null
        binding.tvTemplateSheetTitle.setText(
            when (type) {
                TemplateType.EMAIL ->
                    if (isEditing) R.string.templates_edit_email else R.string.templates_new_email
                TemplateType.WHATSAPP ->
                    if (isEditing) R.string.templates_edit_whatsapp else R.string.templates_new_whatsapp
            }
        )
        binding.tvTemplateSheetHint.setText(
            when (type) {
                TemplateType.EMAIL -> R.string.templates_hint_email
                TemplateType.WHATSAPP -> R.string.templates_hint_whatsapp
            }
        )

        binding.headingLayout.isVisible = type == TemplateType.EMAIL
        binding.bodyLayout.hint = getString(
            when (type) {
                TemplateType.EMAIL -> R.string.templates_body
                TemplateType.WHATSAPP -> R.string.templates_message
            }
        )

        // Prefill only on first open — on rotation the fields restore themselves, and overwriting
        // them would discard an in-progress edit.
        if (savedInstanceState == null) {
            prefill()
        }

        binding.btnSaveTemplate.setOnClickListener { save() }
    }

    private fun prefill() {
        val id = templateId ?: return
        when (type) {
            TemplateType.EMAIL -> viewModel.emailTemplate(id)?.let {
                binding.etHeading.setText(it.heading)
                binding.etBody.setText(it.body)
            }
            TemplateType.WHATSAPP -> viewModel.whatsappTemplate(id)?.let {
                binding.etBody.setText(it.message)
            }
        }
    }

    private fun save() {
        val body = binding.etBody.text?.toString()?.trim().orEmpty()
        if (body.isEmpty()) {
            binding.bodyLayout.error = getString(
                when (type) {
                    TemplateType.EMAIL -> R.string.templates_body_required
                    TemplateType.WHATSAPP -> R.string.templates_message_required
                }
            )
            return
        }
        binding.bodyLayout.error = null

        when (type) {
            TemplateType.EMAIL -> {
                val heading = binding.etHeading.text?.toString()?.trim().orEmpty()
                if (heading.isEmpty()) {
                    binding.headingLayout.error = getString(R.string.templates_heading_required)
                    return
                }
                binding.headingLayout.error = null
                viewModel.saveEmailTemplate(templateId, heading, body)
            }
            TemplateType.WHATSAPP -> viewModel.saveWhatsappTemplate(templateId, body)
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
