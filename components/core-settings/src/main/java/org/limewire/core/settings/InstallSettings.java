package org.limewire.core.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.IntSetting;
import org.limewire.setting.SettingsFactory;
import org.limewire.setting.StringSetSetting;
import org.limewire.setting.StringSetting;

/**
 * Handles installation preferences.
 */
public final class InstallSettings extends LimeWireSettings {

    private static final InstallSettings INSTANCE =
        new InstallSettings();
    private static final SettingsFactory FACTORY =
        INSTANCE.getFactory();

    public static InstallSettings instance() {
        return INSTANCE;
    }

    private InstallSettings() {
        super("installation.props", "WireShare installs file");
    }
    
    /**
     * Current version of spam words.
     */
    public static final IntSetting SPAM_VER =
        FACTORY.createIntSetting("SPAM_VER", 0);
    
    /**
     * The default Security Level.
     */
    public static final IntSetting SECURITY_LEVEL =
        FACTORY.createIntSetting("SECURITY_LEVEL", 3);
    
    /**
     * Whether or not to update the security file.
     */
    public static final BooleanSetting SECURITY_UPDATE =
        FACTORY.createBooleanSetting("SECURITY_UPDATE", true);
        
    /**
     * The Security Version.
     */
    public static final StringSetting SECURITY_VERSION =
        FACTORY.createStringSetting("SECURITY_VERSION", "0.0.0");
    
    /**
     * The Security Version.
     */
    public static final StringSetting NO_REMIND_VERSION =
        FACTORY.createStringSetting("NO_REMIND_VERSION", "0.0.0");
    
    /**
     * Whether or not the 'Start on startup' question has been asked.
     */
    public static final BooleanSetting START_STARTUP =
        FACTORY.createBooleanSetting("START_STARTUP", false);
        
    /**
     * Whether or not the 'Choose your language' question has been asked.
     */
    public static final BooleanSetting LANGUAGE_CHOICE =
        FACTORY.createBooleanSetting("LANGUAGE_CHOICE", false);
        
    /**
     * Whether or not the firewall warning question has been asked.
     */
    public static final BooleanSetting FIREWALL_WARNING =
        FACTORY.createBooleanSetting("FIREWALL_WARNING", false);
    
    /** Whether the association option has been asked. */
    public static final BooleanSetting EXTENSION_OPTION =
        FACTORY.createBooleanSetting("EXTENSION_OPTION", false);
    
    /** Whether the setup wizard has been completed on 5. */
    public static final BooleanSetting UPGRADED_TO_5 =
        FACTORY.createBooleanSetting("UPGRADED_TO_5", false);
        
    /**
     * Stores the value of the last known version of limewire that has been run. Will be null on a clean install until the program is run and a value is set for it.
     * This setting starts with versions > 5.2.2 
     */
    public static final StringSetting LAST_VERSION_RUN = FACTORY.createStringSetting("LAST_VERSION_RUN", "");
    
    /**
     * Stores the java version that was used to run the last known version of limewire. It msut be read early enough, or it will be overwritten with the current value.
     */
    public static final StringSetting LAST_JAVA_VERSION_RUN = FACTORY.createStringSetting("LAST_JAVA_VERSION_RUN", "");
    
    /**
     * Stores an array of all the known versions of limewire that have been run.
     * This setting starts with versions > 5.2.2
     */
    public static final StringSetSetting PREVIOUS_RAN_VERSIONS = FACTORY.createStringSetSetting("PREVIOUS_RAN_VERSIONS", "");
    
}
