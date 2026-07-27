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
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.databinding.FragmentProfileBinding
import com.mohdhussain.hrcontacts.ui.list.ContactListAdapter
import com.mohdhussain.hrcontacts.ui.list.ListItem
import kotlinx.coroutines.launch

/**
 * The user's own page: who they are signed in as, and the two collections that belong to them —
 * contacts they bookmarked and contacts they added. Deliberately reuses [ContactListAdapter] so a
 * contact row looks and behaves identically here and on the records page.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: ContactListAdapter

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
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupChips() {
        binding.collectionChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.setCollection(
                if (R.id.chipAdded in checkedIds) Collection.ADDED else Collection.BOOKMARKED
            )
        }
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
            val hasItems = items.any { it is ListItem.ContactRow }
            binding.recyclerView.isVisible = hasItems
            binding.emptyView.isVisible = !hasItems
        }

        viewModel.collection.observe(viewLifecycleOwner) { collection ->
            binding.emptyView.setText(
                when (collection) {
                    Collection.BOOKMARKED -> R.string.profile_no_bookmarks
                    Collection.ADDED -> R.string.profile_no_added
                }
            )
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
            }
        }
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
