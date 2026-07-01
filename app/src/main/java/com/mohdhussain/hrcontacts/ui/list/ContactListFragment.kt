package com.mohdhussain.hrcontacts.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.databinding.FragmentContactListBinding

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactListViewModel
    private lateinit var adapter: ContactListAdapter
    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ContactListViewModelFactory(requireContext())
        )[ContactListViewModel::class.java]

        setupAdapter()
        setupRecyclerView()
        setupSearchView()
        setupFilterChips()
        setupFab()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = ContactListAdapter(
            onContactClick = { contactId ->
                findNavController().navigate(
                    R.id.action_list_to_detail,
                    bundleOf("contactId" to contactId)
                )
            },
            onContactLongClick = { contactId ->
                if (viewModel.isSelectionMode.value == true) {
                    viewModel.toggleSelection(contactId)
                } else {
                    viewModel.enterSelectionMode(contactId)
                }
            },
            onHeaderCheckboxClick = { company ->
                viewModel.selectAllFromCompany(company)
            }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString() ?: "")
        }
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val scope = when {
                R.id.chipByName in checkedIds -> SearchScope.NAME
                R.id.chipByCompany in checkedIds -> SearchScope.COMPANY
                else -> SearchScope.ALL
            }
            viewModel.setSearchScope(scope)
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            findNavController().navigate(
                R.id.action_list_to_add,
                bundleOf("contactId" to -1L)
            )
        }
    }

    private fun observeViewModel() {
        viewModel.listItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            val hasContacts = items.any { it is ListItem.ContactRow }
            binding.recyclerView.isVisible = hasContacts
            binding.emptyView.isVisible = !hasContacts
        }

        viewModel.isSelectionMode.observe(viewLifecycleOwner) { inSelectionMode ->
            adapter.isSelectionMode = inSelectionMode
            if (inSelectionMode) {
                if (actionMode == null) {
                    actionMode = (requireActivity() as AppCompatActivity)
                        .startSupportActionMode(actionModeCallback)
                }
                binding.fab.hide()
            } else {
                actionMode?.finish()
                actionMode = null
                binding.fab.show()
            }
            adapter.notifyDataSetChanged()
        }

        viewModel.selectedIds.observe(viewLifecycleOwner) { selected ->
            val count = selected.size
            actionMode?.title = getString(R.string.selected_count, count)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_send_email -> {
                    sendBulkEmail()
                    true
                }
                R.id.action_select_all -> {
                    viewModel.selectAll()
                    true
                }
                R.id.action_delete -> {
                    confirmAndDelete()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            viewModel.clearSelection()
            actionMode = null
        }
    }

    private fun sendBulkEmail() {
        val emails = viewModel.getSelectedEmails()
        if (emails.isEmpty()) return

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(requireContext())
            .setMessage("Delete selected contacts?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSelected()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
