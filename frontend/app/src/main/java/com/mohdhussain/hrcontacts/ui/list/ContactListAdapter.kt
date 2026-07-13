package com.mohdhussain.hrcontacts.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.ItemContactBinding
import com.mohdhussain.hrcontacts.databinding.ItemContactHeaderBinding

enum class SelectionState { NONE, PARTIAL, ALL }

sealed class ListItem {
    data class Header(
        val company: String,
        val count: Int,
        val selectionState: SelectionState = SelectionState.NONE
    ) : ListItem()
    data class ContactRow(
        val id: Long,
        val name: String,
        val company: String,
        val mobile: String,
        val emails: List<String>,
        val verified: Boolean,
        val isSelected: Boolean
    ) : ListItem()
}

class ContactListAdapter(
    private val onContactClick: (Long) -> Unit,
    private val onContactLongClick: (Long) -> Unit,
    private val onHeaderCheckboxClick: (String) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    var isSelectionMode = false

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTACT = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ListItem.Header -> VIEW_TYPE_HEADER
            is ListItem.ContactRow -> VIEW_TYPE_CONTACT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemContactHeaderBinding.inflate(inflater, parent, false)
            )
            else -> ContactViewHolder(
                ItemContactBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.ContactRow -> (holder as ContactViewHolder).bind(item)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemContactHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListItem.Header) {
            binding.tvCompanyName.text = "${item.company}  (${item.count})"
            binding.headerCheckbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            // A plain click listener (rather than OnCheckedChangeListener) sidesteps the
            // checkbox's own tri-state toggle cycling — checkedState below is always
            // reasserted from ViewModel state on the next bind, so it's the sole source of truth.
            binding.headerCheckbox.checkedState = when (item.selectionState) {
                SelectionState.ALL -> MaterialCheckBox.STATE_CHECKED
                SelectionState.PARTIAL -> MaterialCheckBox.STATE_INDETERMINATE
                SelectionState.NONE -> MaterialCheckBox.STATE_UNCHECKED
            }
            binding.headerCheckbox.setOnClickListener {
                onHeaderCheckboxClick(item.company)
            }
        }
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListItem.ContactRow) {
            binding.tvName.text = item.name
            binding.tvCompany.text = item.company
            binding.tvEmail.text = when {
                item.emails.isEmpty() -> ""
                item.emails.size == 1 -> item.emails[0]
                else -> "${item.emails[0]} +${item.emails.size - 1}"
            }
            binding.tvInitial.text = item.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

            if (item.verified) {
                binding.tvVerifiedBadge.text = binding.root.context.getString(R.string.verified)
                binding.tvVerifiedBadge.setBackgroundResource(R.drawable.bg_badge_verified)
                binding.tvVerifiedBadge.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.verified_badge_text)
                )
            } else {
                binding.tvVerifiedBadge.text = binding.root.context.getString(R.string.not_verified)
                binding.tvVerifiedBadge.setBackgroundResource(R.drawable.bg_badge_unverified)
                binding.tvVerifiedBadge.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.unverified_badge_text)
                )
            }

            binding.checkbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            binding.checkbox.setOnCheckedChangeListener(null)
            binding.checkbox.isChecked = item.isSelected

            binding.cardView.isChecked = item.isSelected

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    onContactLongClick(item.id)
                } else {
                    onContactClick(item.id)
                }
            }

            binding.root.setOnLongClickListener {
                onContactLongClick(item.id)
                true
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            when {
                oldItem is ListItem.Header && newItem is ListItem.Header ->
                    oldItem.company == newItem.company
                oldItem is ListItem.ContactRow && newItem is ListItem.ContactRow ->
                    oldItem.id == newItem.id
                else -> false
            }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem == newItem
    }
}
