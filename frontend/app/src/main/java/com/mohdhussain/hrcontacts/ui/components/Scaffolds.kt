package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes

/**
 * The standard top bar: title, an optional back arrow, and actions.
 *
 * Sits on the page background rather than a raised surface, so it reads as part of the screen until
 * the content scrolls under it — at which point [scrollBehavior] tints it. That is the Material 3
 * way of showing "there is more above" without a permanent shadow line.
 */
@Composable
fun HrTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.navigate_back),
                        modifier = Modifier.size(Sizes.Icon)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior
    )
}

/**
 * The contextual bar shown while contacts are multi-selected.
 *
 * Replaces the AppCompat `ActionMode` the list used to start. That bar carried its own styling and
 * floated over the screen looking like it came from a different app; this one is the same top bar in
 * a different state, tinted with the primary container so the mode change is unmistakable.
 */
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.selected_count, selectedCount),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.selection_clear),
                    modifier = Modifier.size(Sizes.Icon)
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@LightDarkPreview
@Composable
private fun TopAppBarPreview() {
    PreviewSurface {
        HrTopAppBar(title = "HR Contacts")
    }
}

@LightDarkPreview
@Composable
private fun TopAppBarWithBackPreview() {
    PreviewSurface {
        HrTopAppBar(title = "Edit Contact", onNavigateBack = {})
    }
}

@LightDarkPreview
@Composable
private fun SelectionTopAppBarPreview() {
    PreviewSurface {
        SelectionTopAppBar(selectedCount = 3, onClose = {})
    }
}
