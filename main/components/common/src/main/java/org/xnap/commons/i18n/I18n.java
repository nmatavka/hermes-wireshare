package org.xnap.commons.i18n;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Small compatibility layer for the legacy gettext-commons API used by the
 * Swing UI. It keeps the same package surface while relying on the generated
 * {@link ResourceBundle} classes that already live in this repo.
 */
public class I18n {

    private static final char CONTEXT_SEPARATOR = '\u0004';

    private String baseName;
    private Locale locale;
    private ClassLoader classLoader;
    private Locale sourceCodeLocale = Locale.ENGLISH;
    private ResourceBundle resources;

    public I18n(ResourceBundle resources) {
        this.resources = resources != null ? resources : emptyBundle();
        this.locale = this.resources.getLocale();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public I18n(String baseName, Locale locale, ClassLoader classLoader) {
        setResources(baseName, locale, classLoader);
    }

    public ResourceBundle getResources() {
        return resources;
    }

    public Locale getLocale() {
        return locale != null ? locale : Locale.getDefault();
    }

    public synchronized void setResources(ResourceBundle resources) {
        this.resources = resources != null ? resources : emptyBundle();
        this.locale = this.resources.getLocale();
    }

    public synchronized void setResources(String baseName, Locale locale, ClassLoader classLoader) {
        this.baseName = baseName;
        this.locale = locale != null ? locale : Locale.getDefault();
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        this.resources = loadBundle(baseName, this.locale, this.classLoader);
    }

    public static String marktr(String sourceText) {
        return sourceText;
    }

    public synchronized boolean setLocale(Locale locale) {
        if (baseName == null) {
            this.locale = locale;
            return false;
        }

        setResources(baseName, locale, classLoader);
        return true;
    }

    public void setSourceCodeLocale(Locale sourceCodeLocale) {
        this.sourceCodeLocale = sourceCodeLocale != null ? sourceCodeLocale : Locale.ENGLISH;
    }

    public final String tr(String text) {
        return lookup(text);
    }

    public final String tr(String text, Object[] args) {
        return format(lookup(text), args);
    }

    public final String tr(String text, Object arg) {
        return tr(text, new Object[] { arg });
    }

    public final String tr(String text, Object arg1, Object arg2) {
        return tr(text, new Object[] { arg1, arg2 });
    }

    public final String tr(String text, Object arg1, Object arg2, Object arg3) {
        return tr(text, new Object[] { arg1, arg2, arg3 });
    }

    public final String tr(String text, Object arg1, Object arg2, Object arg3, Object arg4) {
        return tr(text, new Object[] { arg1, arg2, arg3, arg4 });
    }

    public final String trn(String singularText, String pluralText, long number) {
        return number == 1 ? lookup(singularText) : lookup(pluralText);
    }

    public final String trn(String singularText, String pluralText, long number, Object[] args) {
        return format(number == 1 ? lookup(singularText) : lookup(pluralText), args);
    }

    public final String trn(String singularText, String pluralText, long number, Object arg) {
        return trn(singularText, pluralText, number, new Object[] { arg });
    }

    public final String trn(String singularText, String pluralText, long number, Object arg1, Object arg2) {
        return trn(singularText, pluralText, number, new Object[] { arg1, arg2 });
    }

    public final String trn(String singularText, String pluralText, long number, Object arg1, Object arg2, Object arg3) {
        return trn(singularText, pluralText, number, new Object[] { arg1, arg2, arg3 });
    }

    public final String trn(String singularText, String pluralText, long number, Object arg1, Object arg2, Object arg3, Object arg4) {
        return trn(singularText, pluralText, number, new Object[] { arg1, arg2, arg3, arg4 });
    }

    public final String trc(String comment, String text) {
        return lookupWithContext(comment, text);
    }

    public final String trnc(String context, String singularText, String pluralText, long number) {
        return number == 1
            ? lookupWithContext(context, singularText)
            : lookupWithContext(context, pluralText);
    }

    public final String trnc(String context, String singularText, String pluralText, long number, Object[] args) {
        String pattern = number == 1
            ? lookupWithContext(context, singularText)
            : lookupWithContext(context, pluralText);
        return format(pattern, args);
    }

    public final String trnc(String context, String singularText, String pluralText, long number, Object arg) {
        return trnc(context, singularText, pluralText, number, new Object[] { arg });
    }

    public final String trnc(String context, String singularText, String pluralText, long number, Object arg1, Object arg2) {
        return trnc(context, singularText, pluralText, number, new Object[] { arg1, arg2 });
    }

    public final String trnc(String context, String singularText, String pluralText, long number, Object arg1, Object arg2, Object arg3) {
        return trnc(context, singularText, pluralText, number, new Object[] { arg1, arg2, arg3 });
    }

    public final String trnc(String context, String singularText, String pluralText, long number, Object arg1, Object arg2, Object arg3, Object arg4) {
        return trnc(context, singularText, pluralText, number, new Object[] { arg1, arg2, arg3, arg4 });
    }

    private String lookup(String sourceText) {
        if (sourceText == null) {
            return null;
        }

        try {
            return resources.getString(sourceText);
        } catch (MissingResourceException ignored) {
            return sourceText;
        }
    }

    private String lookupWithContext(String context, String sourceText) {
        if (context == null || context.isEmpty()) {
            return lookup(sourceText);
        }

        String contextKey = context + CONTEXT_SEPARATOR + sourceText;
        try {
            return resources.getString(contextKey);
        } catch (MissingResourceException ignored) {
            return lookup(sourceText);
        }
    }

    private String format(String pattern, Object[] args) {
        if (args == null || args.length == 0) {
            return pattern;
        }

        MessageFormat format = new MessageFormat(pattern, sourceCodeLocale != null ? sourceCodeLocale : getLocale());
        format.setLocale(getLocale());
        return format.format(args);
    }

    private static ResourceBundle loadBundle(String baseName, Locale locale, ClassLoader classLoader) {
        try {
            return ResourceBundle.getBundle(baseName, locale != null ? locale : Locale.getDefault(), classLoader);
        } catch (MissingResourceException ignored) {
            return emptyBundle();
        }
    }

    private static ResourceBundle emptyBundle() {
        return new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[0][];
            }
        };
    }
}
