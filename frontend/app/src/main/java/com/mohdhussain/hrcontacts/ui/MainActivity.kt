package com.mohdhussain.hrcontacts.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.data.auth.AuthEventBus
import com.mohdhussain.hrcontacts.data.repository.AuthRepository
import com.mohdhussain.hrcontacts.databinding.ActivityMainBinding
import com.mohdhussain.hrcontacts.ui.add.AddContactBottomSheetFragment
import com.mohdhussain.hrcontacts.ui.components.HrNavigationBar
import com.mohdhussain.hrcontacts.ui.components.NavBarItem
import com.mohdhussain.hrcontacts.ui.theme.HrContactsTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    /**
     * Which tab is lit. Held as Compose state so the destination listener can set it without going
     * through the tab bar's own click path — the equivalent of the old `setChecked` rather than
     * `setSelectedItemId`, which would have fired the listener again and re-navigated to the
     * destination being arrived at.
     */
    private val selectedDestinationId = mutableIntStateOf(R.id.contactListFragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Mandatory from Android 15 (targetSdk 35) and previously unhandled: the window now draws
        // behind the system bars, and the tab bar pads itself for them.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        setupBottomNav()

        // A 401 from any API call (e.g. an expired JWT) forces the user back to the
        // welcome screen, since there's no refresh token to silently recover the session.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthEventBus.unauthorized.collect {
                    AuthRepository.getInstance(applicationContext).logout()
                    navController.navigate(R.id.action_global_to_welcome)
                }
            }
        }
    }

    /**
     * Tab switching is wired by hand rather than with `setupWithNavController`, because that
     * helper pops back to the graph's start destination — which here is the splash screen, so
     * every tab tap would bounce through the auth check. Instead the records list acts as the
     * base of the stack and the profile page sits directly on top of it.
     */
    private fun setupBottomNav() {
        binding.bottomNav.setContent {
            HrContactsTheme {
                HrNavigationBar(
                    items = NAV_ITEMS,
                    selectedDestinationId = selectedDestinationId.intValue,
                    onSelect = { destinationId ->
                        if (destinationId == R.id.nav_action_add_contact) {
                            showAddContactSheet()
                        } else if (navController.currentDestination?.id != destinationId) {
                            navigateToTab(destinationId)
                        }
                    }
                )
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in TOP_LEVEL_DESTINATIONS
            binding.bottomNav.isVisible = isTopLevel
            if (isTopLevel) {
                selectedDestinationId.intValue = destination.id
            }
        }
    }

    private fun navigateToTab(destinationId: Int) {
        if (destinationId == R.id.contactListFragment) {
            // The list is always somewhere below; unwind to it rather than stacking a second copy.
            if (!navController.popBackStack(R.id.contactListFragment, false)) {
                navController.navigate(destinationId)
            }
            return
        }
        navController.navigate(
            destinationId,
            null,
            navOptions {
                launchSingleTop = true
                popUpTo(R.id.contactListFragment) { inclusive = false }
            }
        )
    }

    /**
     * The centre nav bar button, shown over whichever tab happens to be open — hence
     * `supportFragmentManager` rather than a host fragment's `childFragmentManager`, since the bar
     * itself belongs to this Activity, not to any one tab.
     */
    private fun showAddContactSheet() {
        if (supportFragmentManager.findFragmentByTag(AddContactBottomSheetFragment.TAG) == null) {
            AddContactBottomSheetFragment().show(
                supportFragmentManager,
                AddContactBottomSheetFragment.TAG
            )
        }
    }

    companion object {
        // contactsFilteredFragment (the Your Contacts / Bookmarks folders) is deliberately absent:
        // it is reached by pushing on top of the Hub tab, not a tab itself, so it hides the bottom
        // nav and relies on its own back arrow — same treatment as the detail and add/edit screens.
        private val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.contactListFragment,
            R.id.jobsFragment,
            R.id.contactsHubFragment,
            R.id.profileFragment
        )

        /** Order is tab order. Ids are nav_graph destinations, as they were in the old menu — except
         * the centre item, whose id is the `nav_action_add_contact` sentinel from ids.xml. */
        private val NAV_ITEMS = listOf(
            NavBarItem(R.id.contactListFragment, R.drawable.ic_contacts, R.string.nav_home),
            NavBarItem(R.id.jobsFragment, R.drawable.ic_work, R.string.nav_jobs),
            NavBarItem(
                R.id.nav_action_add_contact,
                R.drawable.ic_add,
                R.string.nav_add,
                isAction = true
            ),
            NavBarItem(R.id.contactsHubFragment, R.drawable.ic_folder, R.string.nav_contacts_hub),
            NavBarItem(R.id.profileFragment, R.drawable.ic_account, R.string.nav_profile)
        )
    }
}
