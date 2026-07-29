package com.mohdhussain.hrcontacts.ui.profile

import com.mohdhussain.hrcontacts.data.remote.dto.MAX_TEMPLATES_PER_TYPE

/**
 * The shape of the templates list, as [ProfileViewModel] builds it.
 *
 * These declarations used to live in `TemplateListAdapter.kt`. They moved here unchanged — same
 * package, same names — because the ViewModel depends on them and the adapter does not survive the
 * move to Compose.
 */

/** Which of the two prefill flows a template belongs to. */
enum class TemplateType { EMAIL, WHATSAPP }

sealed class TemplateListItem {
    /** One per type, always present — it carries the section's add button and the "n of 3" count. */
    data class Header(val type: TemplateType, val count: Int) : TemplateListItem()

    data class Row(
        val type: TemplateType,
        val id: String,
        /** The email heading. Null for WhatsApp, where the message is the only text there is. */
        val title: String?,
        val preview: String
    ) : TemplateListItem()
}

/** How many templates of one type the user may store. Mirrors the server's own cap. */
const val TEMPLATES_PER_TYPE_LIMIT = MAX_TEMPLATES_PER_TYPE
