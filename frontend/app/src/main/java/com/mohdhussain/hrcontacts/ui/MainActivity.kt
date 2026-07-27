package com.mohdhussain.hrcontacts.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
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
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id != item.itemId) {
                navigateToTab(item.itemId)
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in TOP_LEVEL_DESTINATIONS
            binding.bottomNav.isVisible = isTopLevel
            if (isTopLevel) {
                // setChecked, not setSelectedItemId: the latter fires the listener again and
                // would re-navigate to the destination we are already arriving at.
                binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
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

    companion object {
        private val TOP_LEVEL_DESTINATIONS = setOf(R.id.contactListFragment, R.id.profileFragment)
    }
}
