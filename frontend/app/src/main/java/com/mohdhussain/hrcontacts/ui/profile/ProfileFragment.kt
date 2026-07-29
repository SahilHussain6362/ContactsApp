package com.mohdhussain.hrcontacts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme

/**
 * Host for [ProfileScreen].
 *
 * The five one-shot event streams the ViewModel publishes — name saved, profile update failed,
 * template saved, template deleted, template rejected — are collected here and shown on the screen's
 * snackbar host. They used to be five `Snackbar.make` calls in a `repeatOnLifecycle` block; they are
 * now five `LaunchedEffect`s, which are scoped to the composition and so stop collecting when the
 * screen leaves without any extra bookkeeping.
 */
class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(requireContext())
        )[ProfileViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                val user by viewModel.user.observeAsState()
                val templateItems by viewModel.templateItems.observeAsState(initial = emptyList())

                val snackbarHostState = remember { SnackbarHostState() }

                val nameUpdated = stringResource(R.string.profile_name_updated)
                val updateFailed = stringResource(R.string.profile_update_failed)
                val templateSaved = stringResource(R.string.templates_saved)
                val templateDeleted = stringResource(R.string.templates_deleted)

                LaunchedEffect(Unit) {
                    viewModel.nameSaved.collect { snackbarHostState.showSnackbar(nameUpdated) }
                }
                LaunchedEffect(Unit) {
                    viewModel.errors.collect { snackbarHostState.showSnackbar(updateFailed) }
                }
                LaunchedEffect(Unit) {
                    viewModel.templateSaved.collect { snackbarHostState.showSnackbar(templateSaved) }
                }
                LaunchedEffect(Unit) {
                    viewModel.templateDeleted.collect {
                        snackbarHostState.showSnackbar(templateDeleted)
                    }
                }
                LaunchedEffect(Unit) {
                    // The server's own wording — a rejected write explains itself.
                    viewModel.templateErrors.collect { message ->
                        snackbarHostState.showSnackbar(message.ifBlank { updateFailed })
                    }
                }

                val displayName = user?.name?.takeIf { it.isNotBlank() }
                    ?: user?.email?.substringBefore('@')
                    ?: ""

                HrContactsTheme {
                    ProfileScreen(
                        displayName = displayName,
                        email = user?.email.orEmpty(),
                        provider = user?.provider,
                        templateItems = templateItems,
                        canAddTemplate = viewModel::hasRoomFor,
                        onEditProfile = ::showEditNameSheet,
                        onLogout = {
                            AuthRepository.getInstance(requireContext()).logout()
                            findNavController().navigate(R.id.action_global_to_welcome)
                        },
                        onAddTemplate = { type ->
                            showTemplateSheet(EditTemplateBottomSheetFragment.newInstance(type))
                        },
                        onEditTemplate = { type, id ->
                            showTemplateSheet(EditTemplateBottomSheetFragment.newInstance(type, id))
                        },
                        onDeleteTemplate = viewModel::deleteTemplate,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    private fun showTemplateSheet(sheet: EditTemplateBottomSheetFragment) {
        if (childFragmentManager.findFragmentByTag(EditTemplateBottomSheetFragment.TAG) == null) {
            sheet.show(childFragmentManager, EditTemplateBottomSheetFragment.TAG)
        }
    }

    private fun showEditNameSheet() {
        if (childFragmentManager.findFragmentByTag(EditNameBottomSheetFragment.TAG) == null) {
            EditNameBottomSheetFragment().show(
                childFragmentManager,
                EditNameBottomSheetFragment.TAG
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Templates live on the user record, so returning to this page re-reads it: a template
        // saved from this session is already local, but one saved on another device only lands
        // here on a refresh.
        viewModel.refresh()
    }
}
