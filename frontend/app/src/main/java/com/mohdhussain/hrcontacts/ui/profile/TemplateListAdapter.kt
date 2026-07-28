package com.mohdhussain.hrcontacts.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.remote.dto.MAX_TEMPLATES_PER_TYPE
import com.mohdhussain.hrcontacts.databinding.ItemTemplateBinding
import com.mohdhussain.hrcontacts.databinding.ItemTemplateHeaderBinding

/** Which of the two prefill flows a template belongs to. */
enum class TemplateType { EMAIL, WHATSAPP }

sealed class TemplateListItem {
    /** One per type, always present — it carries the section's add button and the "n of 3" count. */
    data class Header(val type: TemplateType, val count: Int) : TemplateListItem()

    data class Row(
        val type: TemplateType,
        val id: String,
        /** The email heading. Null for WhatsApp, where the message is the only text there is. */
        val title: String?,
        val preview: String
    ) : TemplateListItem()
}

/**
 * The templates list on the profile page: two headed sections, each with up to
 * [MAX_TEMPLATES_PER_TYPE] rows.
 *
 * The add button is left tappable at the cap rather than disabled — the host answers the tap with
 * the warning explaining that one has to go first, which is more use than a dead control.
 */
class TemplateListAdapter(
    private val onAddClick: (TemplateType) -> Unit,
    private val onEditClick: (TemplateType, String) -> Unit,
    private val onDeleteClick: (TemplateType, String) -> Unit
) : ListAdapter<TemplateListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TEMPLATE = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is TemplateListItem.Header -> VIEW_TYPE_HEADER
            is TemplateListItem.Row -> VIEW_TYPE_TEMPLATE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemTemplateHeaderBinding.inflate(inflater, parent, false)
            )
            else -> TemplateViewHolder(
                ItemTemplateBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TemplateListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TemplateListItem.Row -> (holder as TemplateViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemTemplateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TemplateListItem.Header) {
            val context = binding.root.context
            binding.tvTemplateSectionTitle.setText(
                when (item.type) {
                    TemplateType.EMAIL -> R.string.templates_email_title
                    TemplateType.WHATSAPP -> R.string.templates_whatsapp_title
                }
            )
            binding.tvTemplateSectionCount.text =
                context.getString(R.string.templates_count, item.count, MAX_TEMPLATES_PER_TYPE)
            binding.tvTemplateSectionEmpty.isVisible = item.count == 0
            binding.tvTemplateSectionEmpty.setText(
                when (item.type) {
                    TemplateType.EMAIL -> R.string.templates_none_email
                    TemplateType.WHATSAPP -> R.string.templates_none_whatsapp
                }
            )
            binding.btnAddTemplate.setOnClickListener { onAddClick(item.type) }
        }
    }

    inner class TemplateViewHolder(private val binding: ItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TemplateListItem.Row) {
            binding.tvTemplateTitle.isVisible = item.title != null
            binding.tvTemplateTitle.text = item.title.orEmpty()
            binding.tvTemplatePreview.text = item.preview
            // A WhatsApp row has no heading above it, so let the message itself use the space.
            binding.tvTemplatePreview.maxLines = if (item.title == null) 3 else 2
            binding.btnEditTemplate.setOnClickListener { onEditClick(item.type, item.id) }
            binding.btnDeleteTemplate.setOnClickListener { onDeleteClick(item.type, item.id) }
            binding.root.setOnClickListener { onEditClick(item.type, item.id) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TemplateListItem>() {
        override fun areItemsTheSame(oldItem: TemplateListItem, newItem: TemplateListItem): Boolean =
            when {
                oldItem is TemplateListItem.Header && newItem is TemplateListItem.Header ->
                    oldItem.type == newItem.type
                oldItem is TemplateListItem.Row && newItem is TemplateListItem.Row ->
                    oldItem.id == newItem.id
                else -> false
            }

        override fun areContentsTheSame(oldItem: TemplateListItem, newItem: TemplateListItem): Boolean =
            oldItem == newItem
    }
}
