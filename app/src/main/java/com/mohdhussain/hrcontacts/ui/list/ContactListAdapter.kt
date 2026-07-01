package com.mohdhussain.hrcontacts.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mohdhussain.hrcontacts.databinding.ItemContactBinding
import com.mohdhussain.hrcontacts.databinding.ItemContactHeaderBinding

sealed class ListItem {
    data class Header(val company: String, val count: Int, val isAllSelected: Boolean = false) : ListItem()
    data class ContactRow(
        val id: Long,
        val name: String,
        val company: String,
        val mobile: String,
        val email: String,
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
            binding.headerCheckbox.setOnCheckedChangeListener(null)
            binding.headerCheckbox.isChecked = item.isAllSelected
            if (isSelectionMode) {
                binding.headerCheckbox.setOnCheckedChangeListener { _, _ ->
                    onHeaderCheckboxClick(item.company)
                }
            }
        }
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListItem.ContactRow) {
            binding.tvName.text = item.name
            binding.tvCompany.text = item.company
            binding.tvEmail.text = item.email
            binding.tvInitial.text = item.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

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
