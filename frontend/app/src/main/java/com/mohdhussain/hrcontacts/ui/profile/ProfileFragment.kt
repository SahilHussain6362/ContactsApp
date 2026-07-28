package com.mohdhussain.hrcontacts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.remote.dto.MAX_TEMPLATES_PER_TYPE
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.databinding.FragmentProfileBinding
import com.mohdhussain.hrcontacts.ui.list.ContactListAdapter
import com.mohdhussain.hrcontacts.ui.list.ListItem
import kotlinx.coroutines.launch

/**
 * The user's own page: who they are signed in as, and what belongs to them — contacts they
 * bookmarked, contacts they added, and the message templates they send with. Deliberately reuses
 * [ContactListAdapter] so a contact row looks and behaves identically here and on the records page.
 *
 * The chip group swaps which adapter the single RecyclerView holds, since templates are not
 * contacts and share nothing with a contact row beyond the space they occupy.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: ContactListAdapter
    private lateinit var templateAdapter: TemplateListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(requireContext())
        )[ProfileViewModel::class.java]

        setupRecyclerView()
        setupChips()
        setupToolbar()
        setupEditProfile()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ContactListAdapter(
            onContactClick = { contactId ->
                findNavController().navigate(
                    R.id.action_profile_to_detail,
                    bundleOf("contactId" to contactId)
                )
            },
            // Multi-select belongs to the records page; here a long press is just a tap.
            onContactLongClick = { contactId ->
                findNavController().navigate(
                    R.id.action_profile_to_detail,
                    bundleOf("contactId" to contactId)
                )
            },
            onHeaderCheckboxClick = { },
            onBookmarkClick = { contactId -> viewModel.toggleBookmark(contactId) }
        )
        templateAdapter = TemplateListAdapter(
            onAddClick = { type -> addTemplate(type) },
            onEditClick = { type, id ->
                showTemplateSheet(EditTemplateBottomSheetFragment.newInstance(type, id))
            },
            onDeleteClick = { type, id -> confirmDeleteTemplate(type, id) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupChips() {
        binding.collectionChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.setCollection(
                when {
                    R.id.chipAdded in checkedIds -> Collection.ADDED
                    R.id.chipTemplates in checkedIds -> Collection.TEMPLATES
                    else -> Collection.BOOKMARKED
                }
            )
        }
    }

    /**
     * The cap is checked here rather than by greying out the add button: the point of the tap is to
     * learn why nothing happened, and only a message can say that one template has to go first.
     */
    private fun addTemplate(type: TemplateType) {
        if (!viewModel.hasRoomFor(type)) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.templates_limit_title)
                .setMessage(
                    getString(
                        when (type) {
                            TemplateType.EMAIL -> R.string.templates_limit_email
                            TemplateType.WHATSAPP -> R.string.templates_limit_whatsapp
                        },
                        MAX_TEMPLATES_PER_TYPE
                    )
                )
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }
        showTemplateSheet(EditTemplateBottomSheetFragment.newInstance(type))
    }

    private fun showTemplateSheet(sheet: EditTemplateBottomSheetFragment) {
        if (childFragmentManager.findFragmentByTag(EditTemplateBottomSheetFragment.TAG) == null) {
            sheet.show(childFragmentManager, EditTemplateBottomSheetFragment.TAG)
        }
    }

    private fun confirmDeleteTemplate(type: TemplateType, id: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.templates_delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteTemplate(type, id) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                confirmLogout()
                true
            } else {
                false
            }
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.logout) { _, _ ->
                AuthRepository.getInstance(requireContext()).logout()
                findNavController().navigate(R.id.action_global_to_welcome)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupEditProfile() {
        binding.btnEditProfile.setOnClickListener {
            if (childFragmentManager.findFragmentByTag(EditNameBottomSheetFragment.TAG) == null) {
                EditNameBottomSheetFragment().show(
                    childFragmentManager,
                    EditNameBottomSheetFragment.TAG
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            val displayName = user?.name?.takeIf { it.isNotBlank() }
                ?: user?.email?.substringBefore('@')
                ?: ""
            binding.tvName.text = displayName
            binding.tvEmail.text = user?.email.orEmpty()
            binding.tvInitial.text =
                displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            binding.tvProvider.isVisible = user?.provider?.isNotBlank() == true
            binding.tvProvider.text = user?.provider?.let {
                getString(R.string.profile_signed_in_with, it.lowercase().replaceFirstChar(Char::uppercase))
            }
        }

        viewModel.counts.observe(viewLifecycleOwner) { (bookmarks, added) ->
            binding.tvCounts.text = getString(R.string.profile_counts, bookmarks, added)
        }

        viewModel.listItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            renderList()
        }

        viewModel.templateItems.observe(viewLifecycleOwner) { items ->
            templateAdapter.submitList(items)
            renderList()
        }

        viewModel.collection.observe(viewLifecycleOwner) { collection ->
            binding.emptyView.setText(
                when (collection) {
                    Collection.BOOKMARKED -> R.string.profile_no_bookmarks
                    Collection.ADDED -> R.string.profile_no_added
                    // Never shown: the template sections carry their own empty text.
                    Collection.TEMPLATES -> R.string.profile_no_bookmarks
                }
            )
            renderList()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.nameSaved.collect {
                        Snackbar.make(binding.root, R.string.profile_name_updated, Snackbar.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.errors.collect {
                        Snackbar.make(binding.root, R.string.profile_update_failed, Snackbar.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.templateSaved.collect {
                        Snackbar.make(binding.root, R.string.templates_saved, Snackbar.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.templateDeleted.collect {
                        Snackbar.make(binding.root, R.string.templates_deleted, Snackbar.LENGTH_SHORT).show()
                    }
                }
                launch {
                    // The server's own wording — a rejected write explains itself.
                    viewModel.templateErrors.collect { message ->
                        Snackbar.make(
                            binding.root,
                            message.ifBlank { getString(R.string.profile_update_failed) },
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    /**
     * Points the RecyclerView at whichever adapter the selected chip calls for and decides whether
     * the empty text takes over. Called from all three observers because any of them can change the
     * answer, and re-reading the current state is cheaper than tracking who changed what.
     */
    private fun renderList() {
        val showTemplates = viewModel.collection.value == Collection.TEMPLATES
        val target = if (showTemplates) templateAdapter else adapter
        // Reassigning an adapter drops the scroll position, so only do it on an actual swap.
        if (binding.recyclerView.adapter !== target) {
            binding.recyclerView.adapter = target
        }
        // Templates always have their two section headers, so that list is never empty; a contact
        // collection with nothing in it hands the screen over to the empty text.
        val hasItems = showTemplates ||
            viewModel.listItems.value?.any { it is ListItem.ContactRow } == true
        binding.recyclerView.isVisible = hasItems
        binding.emptyView.isVisible = !hasItems
    }

    override fun onResume() {
        super.onResume()
        // Bookmarks live on the user record, so returning to this page re-reads it: a bookmark
        // added from the records page in this session is already local, but one made on another
        // device only lands here on a refresh.
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
