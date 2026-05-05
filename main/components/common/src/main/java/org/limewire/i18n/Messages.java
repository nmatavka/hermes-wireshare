package org.limewire.i18n;

import java.util.ListResourceBundle;

/**
 * Root bundle required by the legacy gettext runtime. Locale-specific bundles
 * provide translations; the root bundle intentionally stays empty so English
 * text falls back to the source strings.
 */
public class Messages extends ListResourceBundle {

    @Override
    protected Object[][] getContents() {
        return new Object[0][];
    }
}
