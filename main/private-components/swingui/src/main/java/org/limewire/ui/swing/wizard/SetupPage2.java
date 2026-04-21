package org.limewire.ui.swing.wizard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Resource;
import org.limewire.core.api.library.LibraryData;
import org.limewire.core.settings.FilterSettings;
import org.limewire.core.settings.InstallSettings;
import org.limewire.core.settings.SharingSettings;
import org.limewire.ui.swing.components.SegmentLayout;
import org.limewire.ui.swing.util.GuiUtils;
import org.limewire.ui.swing.util.I18n;
import org.limewire.util.OSUtils;
import org.limewire.util.Version;
import org.limewire.util.VersionFormatException;

public class SetupPage2 extends WizardPage {
    private final JCheckBox shareDownloadedFilesCheckBox;
    private final JCheckBox sharePartialFilesCheckBox;
    private final LibraryData libraryData;

    @Resource private Icon p2pSharedListIcon;
    @Resource private Icon sharingMyFilesIcon;
    @Resource private Icon sharingArrowIcon;

    public SetupPage2(SetupComponentDecorator decorator,
            LibraryData libraryData) {
        
        super(decorator);
        
        this.libraryData = libraryData;

        GuiUtils.assignResources(this);

        setOpaque(false);
        setLayout(new SegmentLayout());

        shareDownloadedFilesCheckBox = createAndDecorateCheckBox(true);
        sharePartialFilesCheckBox = createAndDecorateCheckBox(true);
        
        boolean newInstall = InstallSettings.PREVIOUS_RAN_VERSIONS.get().size() == 0;
        boolean fourUpgrade = isFourUpgrade();
        
        JPanel autoSharingPanel = createAutoSharingPanel(newInstall);
        autoSharingPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        add(autoSharingPanel);
        
        add(createModifyInfoPanel());
       
        if (!newInstall) {
            JPanel oldVersionInfoPanel = createOldVersionInfoPanel(fourUpgrade);
            
            // The old version panel may be null if there are no shared files
            //  in the old version.
            if (oldVersionInfoPanel != null) {
                oldVersionInfoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
                add(oldVersionInfoPanel);
            }
        }

        initSettings();
    }
    
    private boolean isFourUpgrade() {
        boolean has4 = false;
        boolean hasGreater = false;
        for (String previousVersion : InstallSettings.PREVIOUS_RAN_VERSIONS.get()) {
            try {
                Version version = new Version(previousVersion);
                if (version.getMajor() > 4) {
                    hasGreater = true;
                } else if (version.getMajor() == 4) {
                    has4 = true;
                }
            } catch (VersionFormatException e) {
                // do nothing
            }
        }
        return has4 && !hasGreater;
    }

    private void initSettings() {
        shareDownloadedFilesCheckBox.setSelected(SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue());
        sharePartialFilesCheckBox.setSelected(SharingSettings.ALLOW_PARTIAL_SHARING.getValue());
    }

    @Override
    public void applySettings() {
        // Auto-Sharing downloaded files Setting
        SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.setValue(shareDownloadedFilesCheckBox.isSelected());
        SharingSettings.ALLOW_PARTIAL_SHARING.setValue(sharePartialFilesCheckBox.isSelected());
        if (InstallSettings.SPAM_VER.get() < 1) {
        	InstallSettings.SPAM_VER.set(1);
        	FilterSettings.BANNED_WORDS.revertToDefault();
        }
   }

    @Override
    public String getFooter() {
        return OSUtils.isMacOSX() ? I18n.tr("All settings can be changed later from WireShare > Preferences") 
                : I18n.tr("All settings can be changed later in Tools > Options");
    }

    @Override
    public String getLine1() {
        return I18n.tr("Learn about sharing.");
    }

    @Override
    public String getLine2() {
        return "";
    }

    /**
     * Adds header for Auto-Sharing, checkbox and associated text.
     */
    private JPanel createAutoSharingPanel(boolean newInstall) {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        
        JPanel autoSharingPanel = new JPanel(new MigLayout("insets 0, gap 0, nogrid"));

        autoSharingPanel.add(createAndDecorateHeader(I18n.tr("Files in your Public Shared list are shared with the world.")),
                "alignx center, wrap");

        if (newInstall) {
            autoSharingPanel.add(shareDownloadedFilesCheckBox);
            autoSharingPanel.add(createAndDecorateMultiLine(I18n.tr("Add files I download from peer-to-peer users to my Public Shared list."), shareDownloadedFilesCheckBox));
            autoSharingPanel.add(createAndDecorateHyperlink("http://www.gnutellaforums.com/"), "wrap");
            autoSharingPanel.add(sharePartialFilesCheckBox);
            autoSharingPanel.add(createAndDecorateMultiLine(I18n.tr("Enable partial file sharing."), sharePartialFilesCheckBox));
            autoSharingPanel.add(createAndDecorateHyperlink("http://www.gnutellaforums.com/"), "wrap");
        } else {
        	if (SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue()) {
               autoSharingPanel.add(createAndDecorateSubHeading(I18n.tr("WireShare will add files you download from peer-to-peer users into your Public Shared list.")), "alignx center");
               autoSharingPanel.add(createAndDecorateHyperlink("http://www.gnutellaforums.com/"), "wrap");
        	}
            if (SharingSettings.ALLOW_PARTIAL_SHARING.getValue()) {
               autoSharingPanel.add(createAndDecorateSubHeading(I18n.tr("WireShare will will share partially downloaded files from peer-to-peer users.")), "alignx center");
               autoSharingPanel.add(createAndDecorateHyperlink("http://www.gnutellaforums.com/"), "wrap");
            }
        }

        outerPanel.add(autoSharingPanel, new GridBagConstraints());
        return outerPanel;
    }

    private JPanel createModifyInfoPanel() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        
        JPanel modifyInfoPanel = new JPanel(new MigLayout("fill, insets 0, gap 0, nogrid"));
        modifyInfoPanel.add(createAndDecorateHeader(I18n.tr("To see or modify files in your Public Shared list, go to")),
                "alignx center, wrap");

        JLabel myFiles = new JLabel(I18n.tr("My Files"), sharingMyFilesIcon, SwingConstants.CENTER);
        myFiles.setVerticalTextPosition(SwingConstants.BOTTOM);
        myFiles.setHorizontalTextPosition(SwingConstants.CENTER);

        modifyInfoPanel.add(myFiles, "alignx center");
        modifyInfoPanel.add(new JLabel(sharingArrowIcon), "aligny top, gaptop 17");
        modifyInfoPanel.add(new JLabel(I18n.tr("Public Shared"), p2pSharedListIcon, SwingConstants.RIGHT), "aligny top, gaptop 15");

        outerPanel.add(modifyInfoPanel, new GridBagConstraints());
        
        return outerPanel;
    }

    private int peekNumPublicSharedFiles() {
        int numPublicFiles = libraryData.peekPublicSharedListCount();
        
        return numPublicFiles;
    }
    
    private JPanel createOldVersionInfoPanel(boolean fourUpgrade) {
        
        JPanel outerPanel = new JPanel(new GridBagLayout());
        
        JLabel label;
        
        if(fourUpgrade) {
            label = createAndDecorateHeader(I18n.tr("Shared files from your old version will continue to be shared with the world."));
        } else {
            int numSharedFiles = peekNumPublicSharedFiles();

            if (numSharedFiles > 0) {
                label = createAndDecorateHeader(I18n.tr("{0} shared files from your previous version will continue to be shared with the world.", 
                            numSharedFiles));
            } 
            else {
                return null;
            }
                
        }
        
        outerPanel.add(label, new GridBagConstraints());
        return outerPanel;
    }
    

}
