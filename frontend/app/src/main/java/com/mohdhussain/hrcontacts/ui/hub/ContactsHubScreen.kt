package com.mohdhussain.hrcontacts.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.components.HrCard
import com.mohdhussain.hrcontacts.ui.components.HrTopAppBar
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * The "Your Contacts" tab: two folders rather than a list, since this tab exists to separate two
 * collections that used to be chips on the profile page. Opening a folder pushes the records list
 * again, scoped to that collection — same cards, same search and filter, just fewer contacts in it.
 */
@Composable
fun ContactsHubScreen(
    yourContactsCount: Int,
    bookmarksCount: Int,
    onYourContactsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // Horizontal only — the tab bar below this screen owns the system navigation bar area.
        // See the comment in activity_main.xml.
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = { HrTopAppBar(title = stringResource(R.string.contacts_hub_title)) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            FolderCard(
                icon = painterResource(R.drawable.ic_folder),
                title = stringResource(R.string.contacts_hub_your_contacts),
                body = stringResource(R.string.contacts_hub_your_contacts_body),
                count = yourContactsCount,
                onClick = onYourContactsClick
            )
            FolderCard(
                icon = painterResource(R.drawable.ic_bookmark),
                title = stringResource(R.string.contacts_hub_bookmarks),
                body = stringResource(R.string.contacts_hub_bookmarks_body),
                count = bookmarksCount,
                onClick = onBookmarksClick
            )
        }
    }
}

@Composable
private fun FolderCard(
    icon: Painter,
    title: String,
    body: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HrCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Sizes.AvatarMedium)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(Sizes.Icon)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.contacts_hub_folder_count, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Sizes.IconSmall)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ContactsHubScreenPreview() {
    PreviewSurface {
        ContactsHubScreen(
            yourContactsCount = 5,
            bookmarksCount = 2,
            onYourContactsClick = {},
            onBookmarksClick = {}
        )
    }
}
