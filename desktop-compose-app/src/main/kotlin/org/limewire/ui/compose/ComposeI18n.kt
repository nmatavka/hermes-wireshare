package org.limewire.ui.compose

import org.limewire.ui.compose.integration.ComposeLocalizationService
import java.text.MessageFormat
import java.util.Locale

private object FallbackLocalizationService : ComposeLocalizationService {
    override fun translate(text: String, vararg args: Any?): String {
        return if (args.isEmpty()) {
            text
        } else {
            MessageFormat.format(text, *args)
        }
    }

    override fun availableLocales(): List<Locale> = listOf(Locale.getDefault())

    override fun currentLocale(): Locale = Locale.getDefault()

    override fun applyLocale(locale: Locale) {
    }
}

object ComposeLocalization {
    @Volatile
    var service: ComposeLocalizationService = FallbackLocalizationService
}

fun tr(text: String, vararg args: Any?): String = ComposeLocalization.service.translate(text, *args)
