package org.limewire.ui.compose.integration

import org.limewire.ui.desktop.util.I18n
import org.limewire.ui.desktop.util.LanguageUtils
import java.util.Locale

class SwingComposeLocalizationService : ComposeLocalizationService {
    override fun translate(text: String, vararg args: Any?): String = I18n.tr(text, *args)

    override fun availableLocales(): List<Locale> = LanguageUtils.getLocales(null).toList()

    override fun currentLocale(): Locale = LanguageUtils.getCurrentLocale()

    override fun applyLocale(locale: Locale) {
        LanguageUtils.setLocale(locale)
    }
}
