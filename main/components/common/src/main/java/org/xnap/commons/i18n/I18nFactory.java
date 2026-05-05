package org.xnap.commons.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal factory compatible with the legacy gettext-commons entry points used
 * by the Swing UI.
 */
public final class I18nFactory {

    public static final int DEFAULT = 0;
    public static final int FALLBACK = 1;
    public static final int READ_PROPERTIES = 2;
    public static final int NO_CACHE = 4;
    public static final String DEFAULT_BASE_NAME = "Messages";
    public static final String PROPS_FILENAME = "i18n.properties";

    private static final Map<String, I18n> CACHE = new ConcurrentHashMap<String, I18n>();

    private I18nFactory() {
    }

    public static I18n getI18n(Class clazz) {
        return getI18n(clazz, DEFAULT_BASE_NAME);
    }

    public static I18n getI18n(Class clazz, Locale locale) {
        return getI18n(clazz, DEFAULT_BASE_NAME, locale);
    }

    public static I18n getI18n(Class clazz, Locale locale, int flags) {
        return getI18n(clazz, DEFAULT_BASE_NAME, locale, flags);
    }

    public static I18n getI18n(Class clazz, String baseName) {
        return getI18n(clazz, baseName, Locale.getDefault());
    }

    public static I18n getI18n(Class clazz, String baseName, Locale locale) {
        return getI18n(clazz, baseName, locale, DEFAULT);
    }

    public static I18n getI18n(Class clazz, String baseName, Locale locale, int flags) {
        ClassLoader classLoader = clazz != null ? clazz.getClassLoader() : Thread.currentThread().getContextClassLoader();
        return getI18n(baseName, baseName, classLoader, locale, flags);
    }

    public static I18n getI18n(String key, String baseName, ClassLoader classLoader, Locale locale, int flags) {
        Locale resolvedLocale = locale != null ? locale : Locale.getDefault();
        ClassLoader resolvedLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        String cacheKey = baseName + "|" + resolvedLocale.toLanguageTag() + "|" + System.identityHashCode(resolvedLoader);

        if ((flags & NO_CACHE) != 0) {
            return new I18n(baseName, resolvedLocale, resolvedLoader);
        }

        return CACHE.computeIfAbsent(cacheKey, ignored -> new I18n(baseName, resolvedLocale, resolvedLoader));
    }
}
