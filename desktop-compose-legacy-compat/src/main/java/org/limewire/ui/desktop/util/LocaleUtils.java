package org.limewire.ui.desktop.util;

import java.awt.Font;
import java.util.Locale;

import org.limewire.core.settings.ApplicationSettings;
import org.limewire.util.StringUtils;

public class LocaleUtils {
    
    private LocaleUtils() {}
    
    /** Returns the current locale in use. */
    public static Locale getCurrentLocale() {
        return new Locale(ApplicationSettings.LANGUAGE.get(),
                ApplicationSettings.COUNTRY.get(),
                ApplicationSettings.LOCALE_VARIANT.get());        
    }

    /** Sets the locale based on whats in the preferences. */
    public static void setLocaleFromPreferences() {
        if (ApplicationSettings.LANGUAGE.get().equals("")) {
            ApplicationSettings.LANGUAGE.set("en");
        }
        
        Locale locale = new Locale(ApplicationSettings.LANGUAGE.get(),
                ApplicationSettings.COUNTRY.get(),
                ApplicationSettings.LOCALE_VARIANT.get());
        Locale.setDefault(locale);
        StringUtils.setLocale(locale);
        I18n.setLocale(locale);
    }

    /**
     * Validates the locale, determining if the current locale's resources can
     * be displayed using the current fonts. If not, then the locale is reset to
     * English.
     * 
     * This prevents the UI from appearing as all boxes.
     */
    public static void validateLocaleAndFonts() {
        String displayName = getCurrentLocale().getDisplayName();
        Font fallback = new Font("Dialog", Font.PLAIN, 12);
        if (fallback.canDisplayUpTo(displayName) != -1) {
            ApplicationSettings.LANGUAGE.set("en");
            ApplicationSettings.COUNTRY.set("");
            ApplicationSettings.LOCALE_VARIANT.set("");
            setLocaleFromPreferences();
        }
    }
}
