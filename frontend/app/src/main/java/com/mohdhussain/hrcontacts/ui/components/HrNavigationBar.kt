package com.mohdhussain.hrcontacts.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes

/**
 * One tab. [destinationId] is the navigation graph destination this tab selects — the same identity
 * the old `menu_bottom_nav.xml` used, where item ids deliberately matched destination ids. Keeping
 * that means the Activity's tab logic did not have to change to swap the View for this.
 *
 * [isAction] marks the centre "Add" slot: it is never lit as selected and [destinationId] there is
 * the `nav_action_add_contact` sentinel from ids.xml rather than a real nav_graph destination — the
 * click still comes back through the same `onSelect`, MainActivity just reacts to it differently.
 */
data class NavBarItem(
    val destinationId: Int,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val isAction: Boolean = false
)

/**
 * The bottom tab bar.
 *
 * Pads itself for the system navigation bar, which is what it takes for the app to sit correctly
 * behind the gesture bar now that the window is edge-to-edge — the View version had no inset handling
 * and, on Android 15 where edge-to-edge is not optional, would have ended up underneath it.
 */
@Composable
fun HrNavigationBar(
    items: List<NavBarItem>,
    selectedDestinationId: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = !item.isAction && item.destinationId == selectedDestinationId
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item.destinationId) },
                icon = {
                    if (item.isAction) {
                        // A filled circle rather than the plain glyph every other tab uses, so the
                        // one action in a row of destinations reads as something to press, not
                        // something to switch to.
                        Box(
                            modifier = Modifier
                                .size(Sizes.AvatarSmall)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(Sizes.Icon)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null,
                            modifier = Modifier.size(Sizes.Icon)
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = if (item.isAction) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun HrNavigationBarPreview() {
    PreviewSurface {
        HrNavigationBar(
            items = listOf(
                NavBarItem(R.id.contactListFragment, R.drawable.ic_contacts, R.string.nav_home),
                NavBarItem(R.id.jobsFragment, R.drawable.ic_work, R.string.nav_jobs),
                NavBarItem(R.id.nav_action_add_contact, R.drawable.ic_add, R.string.nav_add, isAction = true),
                NavBarItem(R.id.contactsHubFragment, R.drawable.ic_folder, R.string.nav_contacts_hub),
                NavBarItem(R.id.profileFragment, R.drawable.ic_account, R.string.nav_profile)
            ),
            selectedDestinationId = R.id.contactListFragment,
            onSelect = {}
        )
    }
}
