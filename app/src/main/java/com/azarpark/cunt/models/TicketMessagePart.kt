package com.azarpark.cunt.models

import android.content.Context
import android.view.ViewGroup
import com.azarpark.cunt.utils.Assistant

class TicketMessagePart(val title: String? = null, val body: String? = null) {
    fun render(context: Context, parent: ViewGroup, attachToParent: Boolean, replacements: Map<String, String>): Boolean {
        var b = body
        if (replacements.isNotEmpty()) {
            replacements.forEach {
                b = b?.replace(it.key, it.value) ?: b
            }
        }
        return Assistant.inflateHTML(title, b, context, parent, attachToParent);
    }

    override fun toString(): String {
        return "TicketMessagePart{" +
                "title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}'
    }
}