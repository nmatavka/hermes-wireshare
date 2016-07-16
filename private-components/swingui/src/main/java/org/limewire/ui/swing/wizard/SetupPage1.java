package org.limewire.ui.swing.wizard;

import javax.swing.JCheckBox;

import net.miginfocom.swing.MigLayout;

import org.limewire.core.settings.FilterSettings;
import org.limewire.core.settings.InstallSettings;
import org.limewire.ui.swing.settings.StartupSettings;
import org.limewire.ui.swing.settings.SwingUiSettings;
import org.limewire.ui.swing.shell.LimeAssociationOption;
import org.limewire.ui.swing.shell.LimeAssociations;
import org.limewire.ui.swing.util.BackgroundExecutorService;
import org.limewire.ui.swing.util.I18n;
import org.limewire.ui.swing.util.MacOSXUtils;
import org.limewire.ui.swing.util.WindowsUtils;
import org.limewire.util.OSUtils;

public class SetupPage1 extends WizardPage {

    private final JCheckBox associationMagFileTypeCheckBox;
    private final JCheckBox associationTorFileTypeCheckBox;
    private final JCheckBox launchAtStartupCheckBox;
    private final JCheckBox filterAdultContentCheckBox;
    
    public SetupPage1(SetupComponentDecorator decorator){
        super(decorator);
        
        setOpaque(false);
        setLayout(new MigLayout("insets 0 14 0 0, gap 0, nogrid"));       
   
        associationMagFileTypeCheckBox = createAndDecorateCheckBox(true);
        associationTorFileTypeCheckBox = createAndDecorateCheckBox(true);
        launchAtStartupCheckBox = createAndDecorateCheckBox(true);
        filterAdultContentCheckBox = createAndDecorateCheckBox(true);

        //File Associations
        addFileAssociations();        
        initSettings();
    }
    
    /**
     * Adds header for file association and any appropriate checkboxes and text
     */
    private void addFileAssociations() {
        if (LimeAssociations.isMagnetAssociationSupported() 
                || LimeAssociations.isTorrentAssociationSupported()
                || shouldShowStartOnStartupWindow()) {
             add(createAndDecorateHeader(I18n.tr("File Associations and Startup")), "gaptop 20, span, wrap");

             if (LimeAssociations.isMagnetAssociationSupported() 
                     || LimeAssociations.isTorrentAssociationSupported()) {
                 add(associationMagFileTypeCheckBox, "gaptop 5, gapleft 26");
                 add(createAndDecorateMultiLine(I18n.tr("Associate magnet links with WireShare"), associationMagFileTypeCheckBox), "gaptop 5, gapleft 5, wrap");
                 add(associationTorFileTypeCheckBox, "gaptop 5, gapleft 26");
                 add(createAndDecorateMultiLine(I18n.tr("Associate torrent files with WireShare"), associationTorFileTypeCheckBox), "gaptop 5, gapleft 5, wrap");
             }
             
             if (shouldShowStartOnStartupWindow()) {
                 add(launchAtStartupCheckBox, "gaptop 5, gapleft 26");
                 add(createAndDecorateMultiLine(I18n.tr("Launch WireShare at system startup"), launchAtStartupCheckBox), "gaptop 5, gapleft 5, wrap");
             }
        }
        add(createAndDecorateHeader(I18n.tr("Search Filter Settings")), "gaptop 20, span, wrap");
        add(filterAdultContentCheckBox, "gaptop 5, gapleft 26");
        add(createAndDecorateMultiLine(I18n.tr("Filter adult content from my search results."), filterAdultContentCheckBox), "gaptop 5, gapleft 5, wrap");

    }
    
    private void initSettings() {
        associationMagFileTypeCheckBox.setSelected(SwingUiSettings.HANDLE_MAGNETS.getValue());
        associationTorFileTypeCheckBox.setSelected(SwingUiSettings.HANDLE_TORRENTS.getValue());
        launchAtStartupCheckBox.setSelected(StartupSettings.RUN_ON_STARTUP.getValue());
        filterAdultContentCheckBox.setSelected(FilterSettings.FILTER_ADULT.getValue());
    }

    @Override
    public String getLine1() {
        return I18n.tr("Please take a minute to configure these options before moving on.");
    }
    
    @Override
    public String getLine2() {
        return "";
    }
    
    @Override
    public String getFooter() {
        return OSUtils.isMacOSX() ? I18n.tr("All settings can be changed later from WireShare > Preferences") :
            I18n.tr("All settings can be changed later in Tools > Options");
    }
    
    @Override
    public void applySettings() {
        // File Associations
        SwingUiSettings.HANDLE_MAGNETS.setValue(associationMagFileTypeCheckBox.isSelected());
        LimeAssociationOption magnetAssociationOption = LimeAssociations.getMagnetAssociation();
        if (magnetAssociationOption != null) {
            magnetAssociationOption.setEnabled(associationMagFileTypeCheckBox.isSelected());
        }

        SwingUiSettings.HANDLE_TORRENTS.setValue(associationTorFileTypeCheckBox.isSelected());
        LimeAssociationOption torrentAssociationOption = LimeAssociations.getTorrentAssociation();
        if (torrentAssociationOption != null) {
            torrentAssociationOption.setEnabled(associationTorFileTypeCheckBox.isSelected());
        }

        // launch at startup
        if (shouldShowStartOnStartupWindow()) {
            if (OSUtils.isMacOSX())
                MacOSXUtils.setLoginStatus(launchAtStartupCheckBox.isSelected());
            else if (WindowsUtils.isLoginStatusAvailable())
                BackgroundExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        WindowsUtils.setLoginStatus(launchAtStartupCheckBox.isSelected());
                    }
                });

            StartupSettings.RUN_ON_STARTUP.setValue(launchAtStartupCheckBox.isSelected());
        } else
            StartupSettings.RUN_ON_STARTUP.setValue(false);
        
        // Adult Filter Setting
        FilterSettings.FILTER_ADULT.setValue(filterAdultContentCheckBox.isSelected());
 }
    
    /**
     * Determines if the Start On Startup option is available.
     */
    private boolean shouldShowStartOnStartupWindow() {
        return OSUtils.isMacOSX() || OSUtils.isGoodWindows();
    }
}
