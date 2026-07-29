package com.mohdhussain.hrcontacts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohdhussain.hrcontacts.R
import com.mohdhussain.hrcontacts.ui.theme.LightDarkPreview
import com.mohdhussain.hrcontacts.ui.theme.LocalHrColors
import com.mohdhussain.hrcontacts.ui.theme.PreviewSurface
import com.mohdhussain.hrcontacts.ui.theme.Sizes
import com.mohdhussain.hrcontacts.ui.theme.Spacing

/**
 * A labelled card of contact information — the mobile number, the email addresses, the LinkedIn URL.
 *
 * The icon and the label sit on their own line above the values rather than beside them, which is
 * what gives a long email address or URL the full width of the card instead of whatever is left over
 * after three buttons.
 */
@Composable
fun DetailInfoCard(
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    HrCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Sizes.IconSmall)
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }
            content()
        }
    }
}

/**
 * One value inside a [DetailInfoCard], with its own trailing actions.
 *
 * Used once for a mobile number and once per email address, so a contact with three emails gets
 * three independent copy/send pairs rather than one that guesses which address was meant.
 */
@Composable
fun InfoValueRow(
    value: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.MinTouchTarget)
            .padding(top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(Spacing.sm))
        actions()
    }
}

/**
 * One of the headline actions on the contact detail screen: call, WhatsApp, email.
 *
 * Labelled on purpose. The old screen offered these as three bare 44dp icons in the corner of a
 * card, which left the user to work out which glyph did what — for actions this common, a word
 * costs one line and removes the guess.
 */
@Composable
fun QuickAction(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Column(
        modifier = modifier
            .widthIn(min = 72.dp)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.MinTouchTarget)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(Sizes.Icon)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}

/** The row of [QuickAction]s. Spread evenly so it stays balanced whether there are two or three. */
@Composable
fun QuickActionRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@LightDarkPreview
@Composable
private fun QuickActionRowPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            QuickActionRow {
                QuickAction(
                    icon = painterResource(R.drawable.ic_phone),
                    label = stringResource(R.string.action_call),
                    onClick = {}
                )
                QuickAction(
                    icon = painterResource(R.drawable.ic_whatsapp),
                    label = stringResource(R.string.action_whatsapp),
                    onClick = {},
                    tint = LocalHrColors.current.whatsapp
                )
                QuickAction(
                    icon = painterResource(R.drawable.ic_email),
                    label = stringResource(R.string.action_email),
                    onClick = {}
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun DetailInfoCardPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            DetailInfoCard(
                icon = painterResource(R.drawable.ic_phone),
                label = stringResource(R.string.mobile)
            ) {
                InfoValueRow(value = "+91 98765 43210") {
                    HrIconButton(
                        onClick = {},
                        icon = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.copy_mobile)
                    )
                }
            }
            DetailInfoCard(
                icon = painterResource(R.drawable.ic_email),
                label = stringResource(R.string.email)
            ) {
                InfoValueRow(value = "priya.sharma@acme.com") {
                    HrIconButton(
                        onClick = {},
                        icon = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.copy_email)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    HrIconButton(
                        onClick = {},
                        icon = painterResource(R.drawable.ic_email),
                        contentDescription = stringResource(R.string.send_email)
                    )
                }
                InfoValueRow(value = "priya@personal.example") {
                    HrIconButton(
                        onClick = {},
                        icon = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.copy_email)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    HrIconButton(
                        onClick = {},
                        icon = painterResource(R.drawable.ic_email),
                        contentDescription = stringResource(R.string.send_email)
                    )
                }
            }
        }
    }
}
