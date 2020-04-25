package org.limewire.ui.swing.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.limewire.core.settings.InstallSettings;
import org.limewire.setting.BooleanSetting;
import org.limewire.ui.swing.settings.StartupSettings;
import org.limewire.ui.swing.settings.SwingUiSettings;
import org.limewire.ui.swing.shell.LimeAssociationOption;
import org.limewire.ui.swing.shell.LimeAssociations;
import org.limewire.ui.swing.tray.TrayNotifier;
import org.limewire.ui.swing.util.BackgroundExecutorService;
import org.limewire.ui.swing.util.I18n;
import org.limewire.ui.swing.util.MacOSXUtils;
import org.limewire.ui.swing.util.WindowsUtils;
import org.limewire.util.CommonUtils;
import org.limewire.util.OSUtils;

import com.google.inject.Inject;

/**
 * System Option View.
 */
public class SystemOptionPanel extends OptionPanel {

    private final TrayNotifier trayNotifier;
    private final FileAssociationPanel fileAssociationPanel;
    private final StartupShutdownPanel startupShutdownPanel;
    private final SecurityPanel securityPanel;

    @Inject
    public SystemOptionPanel(TrayNotifier trayNotifier) {
        this.trayNotifier = trayNotifier;
        setLayout(new MigLayout("hidemode 3, insets 15, fillx, wrap"));

        setOpaque(false);

        fileAssociationPanel = new FileAssociationPanel();
        startupShutdownPanel = new StartupShutdownPanel();
        securityPanel = new SecurityPanel();
        
        add(fileAssociationPanel, "pushx, growx");
        add(startupShutdownPanel, "pushx, growx");
        add(securityPanel, "pushx, growx");
   }

    @Override
    void setOptionTabItem(OptionTabItem tab) {
        super.setOptionTabItem(tab);
        getFileAssociationPanel().setOptionTabItem(tab);
        getStartupShutdownPanel().setOptionTabItem(tab);
        getsecurityPanel().setOptionTabItem(tab);
        }

    private FileAssociationPanel getFileAssociationPanel() {
        return fileAssociationPanel;
    }
    
    private StartupShutdownPanel getStartupShutdownPanel() {
        return startupShutdownPanel;
    }
    
    private SecurityPanel getsecurityPanel() {
        return securityPanel;
    }
    
    @Override
    ApplyOptionResult applyOptions() {
        ApplyOptionResult result = null;
        
        result = fileAssociationPanel.applyOptions();
        if (result.isSuccessful())
            result.applyResult(startupShutdownPanel.applyOptions());
        if (result.isSuccessful())
            result.applyResult(securityPanel.applyOptions());
        return result;
    }

    @Override
    boolean hasChanged() {
        return fileAssociationPanel.hasChanged() || startupShutdownPanel.hasChanged() || securityPanel.hasChanged();
    }

    @Override
    public void initOptions() {
        fileAssociationPanel.initOptions();
        startupShutdownPanel.initOptions();
        securityPanel.initOptions();
    }

    private static class FileAssociationPanel extends OptionPanel {

        private JCheckBox magnetCheckBox;
        private JCheckBox torrentCheckBox;
        private JCheckBox warnCheckBox;

        public FileAssociationPanel() {
            super(I18n.tr("File Associations"));
            setLayout(new MigLayout("insets 0, gap 0, hidemode 3"));
            setOpaque(false);

            magnetCheckBox = new JCheckBox(I18n.tr("\".magnet\" links"));
            magnetCheckBox.setContentAreaFilled(false);
            magnetCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateView();
                }
            });

            torrentCheckBox = new JCheckBox(I18n.tr("\".torrent\" files"));
            torrentCheckBox.setContentAreaFilled(false);
            torrentCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateView();
                }
            });
            warnCheckBox = new JCheckBox("<html>" + I18n.tr("Warn me when other programs want to open these types automatically") + "</html>");
            warnCheckBox.setContentAreaFilled(false);

            add(magnetCheckBox, "gapleft 5, gapbottom 5, wrap");
            add(torrentCheckBox, "gapleft 5, push");
            add(warnCheckBox);
        }

        @Override
        ApplyOptionResult applyOptions() {
            if (hasChanged(magnetCheckBox, SwingUiSettings.HANDLE_MAGNETS)) {
                applyOption(magnetCheckBox, SwingUiSettings.HANDLE_MAGNETS);
                LimeAssociationOption magnetAssociationOption = LimeAssociations
                        .getMagnetAssociation();
                if (magnetAssociationOption != null) {
                    magnetAssociationOption.setEnabled(magnetCheckBox.isSelected());
                }
            }

            if (hasChanged(torrentCheckBox, SwingUiSettings.HANDLE_TORRENTS)) {
                applyOption(torrentCheckBox, SwingUiSettings.HANDLE_TORRENTS);
                LimeAssociationOption torrentAssociationOption = LimeAssociations
                        .getTorrentAssociation();
                if (torrentAssociationOption != null) {
                    torrentAssociationOption.setEnabled(torrentCheckBox.isSelected());
                }
            }

            if (hasChanged(warnCheckBox, SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES)) {
                applyOption(warnCheckBox, SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES);
            }
            return new ApplyOptionResult(false, true);
        }

        private void applyOption(JCheckBox checkBox, BooleanSetting booleanSetting) {
            booleanSetting.setValue(checkBox.isSelected());
        }

        @Override
        boolean hasChanged() {
            return hasChanged(magnetCheckBox, SwingUiSettings.HANDLE_MAGNETS)
                    || hasChanged(torrentCheckBox, SwingUiSettings.HANDLE_TORRENTS)
                    || hasChanged(warnCheckBox, SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES);
        }

        private boolean hasChanged(JCheckBox checkBox, BooleanSetting booleanSetting) {
            return booleanSetting.getValue() != checkBox.isSelected();
        }

        @Override
        public void initOptions() {
            boolean selected = SwingUiSettings.HANDLE_MAGNETS.getValue() && LimeAssociations.isMagnetAssociationSupported() && LimeAssociations.getMagnetAssociation().isEnabled();
            initOption(magnetCheckBox, selected);
            if (selected) {
                magnetCheckBox.setEnabled(LimeAssociations.getMagnetAssociation().canDisassociate());
            }
            
            selected = SwingUiSettings.HANDLE_TORRENTS.getValue() && LimeAssociations.isTorrentAssociationSupported() && LimeAssociations.getTorrentAssociation().isEnabled();
            initOption(torrentCheckBox, selected);
            if (selected) {
                torrentCheckBox.setEnabled(LimeAssociations.getTorrentAssociation().canDisassociate());
            }

            selected = SwingUiSettings.WARN_FILE_ASSOCIATION_CHANGES.getValue();
            initOption(warnCheckBox, selected);
            updateView();
        }

        private void updateView() {
            boolean warnShouldBeVisible = magnetCheckBox.isSelected()
                    || torrentCheckBox.isSelected();
            warnCheckBox.setVisible(warnShouldBeVisible);

            boolean torrentShouldBeVisible = LimeAssociations.isTorrentAssociationSupported();
            torrentCheckBox.setVisible(torrentShouldBeVisible);

            boolean magnetShouldBeVisible = LimeAssociations.isMagnetAssociationSupported();
            magnetCheckBox.setVisible(magnetShouldBeVisible);

            setVisible(torrentShouldBeVisible || magnetShouldBeVisible);
        }

        private void initOption(JCheckBox checkBox, boolean value) {
            checkBox.setSelected(value);
        }
    }

    /**
     * When I press X is not shown for OSX, OSX automatically minimizes on an X
     * If Run at startup || minimize to try is selected, set System tray icon to
     * true
     */
    private class StartupShutdownPanel extends OptionPanel {

        private JCheckBox runAtStartupCheckBox;
        private JRadioButton minimizeButton;
        private JRadioButton exitButton;
        private boolean displaySystemTrayIcon = false;

        public StartupShutdownPanel() {
            super(I18n.tr("Startup and Shutdown"));
             
            runAtStartupCheckBox = new JCheckBox(I18n.tr("Start WireShare every time I log in"));
            runAtStartupCheckBox.setContentAreaFilled(false);
            minimizeButton = new JRadioButton(I18n.tr("Minimize to notifications area"));
            minimizeButton.setContentAreaFilled(false);
            exitButton = new JRadioButton(I18n.tr("Exit program"));
            exitButton.setContentAreaFilled(false);

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(minimizeButton);
            buttonGroup.add(exitButton);

            if (OSUtils.isWindows() || OSUtils.isMacOSX()) {
                add(runAtStartupCheckBox, "wrap");
            }
            if (trayNotifier.supportsSystemTray()) {
                add(new JLabel(I18n.tr("When I press the close button:")), "wrap");
                add(minimizeButton, "gapleft 10");
                add(exitButton);
            }
        }

        @Override
        ApplyOptionResult applyOptions() {
            if (OSUtils.isMacOSX()) {
                MacOSXUtils.setLoginStatus(runAtStartupCheckBox.isSelected());
            } else if (WindowsUtils.isLoginStatusAvailable()) {
                BackgroundExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        WindowsUtils.setLoginStatus(runAtStartupCheckBox.isSelected());
                    }
                });
            }
            StartupSettings.RUN_ON_STARTUP.setValue(runAtStartupCheckBox.isSelected());
            SwingUiSettings.MINIMIZE_TO_TRAY.setValue(minimizeButton.isSelected());
            if (SwingUiSettings.MINIMIZE_TO_TRAY.getValue()) {
                trayNotifier.showTrayIcon();
            } else {
                trayNotifier.hideTrayIcon();
            }
            return new ApplyOptionResult(false, true);
        }

        @Override
        boolean hasChanged() {
            return StartupSettings.RUN_ON_STARTUP.getValue() != runAtStartupCheckBox.isSelected()
                    || SwingUiSettings.MINIMIZE_TO_TRAY.getValue() != minimizeButton.isSelected()
                    || isIconDisplayed();
        }

        private boolean isIconDisplayed() {
            if ((runAtStartupCheckBox.isSelected() || minimizeButton.isSelected())
                    && OSUtils.supportsTray())
                return displaySystemTrayIcon != true;
            else
                return displaySystemTrayIcon != false;
        }

        @Override
        public void initOptions() {
            runAtStartupCheckBox.setSelected(StartupSettings.RUN_ON_STARTUP.getValue());
            minimizeButton.setSelected(SwingUiSettings.MINIMIZE_TO_TRAY.getValue());
            exitButton.setSelected(!SwingUiSettings.MINIMIZE_TO_TRAY.getValue());
        }
    }
    private class SecurityPanel extends OptionPanel {

        //private JRadioButton Strong;
        //private JRadioButton Light;
        private JRadioButton StrongNJ;
        private JRadioButton LightNJ;        
        private JRadioButton None;        
        
        private int SecurityLevel;
        
        public SecurityPanel() {
            super(I18n.tr("Security Level"));

            //Strong = new JRadioButton(I18n.tr("Strong Security."));
            //Strong.setContentAreaFilled(false);
            StrongNJ = new JRadioButton(I18n.tr("Strong Security."));
            StrongNJ.setContentAreaFilled(false);
            //Light = new JRadioButton(I18n.tr("Light Security - Full Japanese Block."));
            //Light.setContentAreaFilled(false);
            LightNJ = new JRadioButton(I18n.tr("Light Security."));
            LightNJ.setContentAreaFilled(false);
            None = new JRadioButton(I18n.tr("None."));
            None.setContentAreaFilled(false);
            
            ButtonGroup buttonGroup = new ButtonGroup();
            //buttonGroup.add(StrongNJ);
            buttonGroup.add(StrongNJ);
            //buttonGroup.add(Light);
            buttonGroup.add(LightNJ);
            buttonGroup.add(None);

            /*Strong.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 4;
                }          
             });*/
            StrongNJ.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 3;
                }          
             });
            /*Light.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 2;
                }          
             });*/
            LightNJ.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 1;
                }          
             });
            None.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 0;
                }          
             });
            
            //add(Strong,"gapleft 5, wrap");
            add(StrongNJ,"gapleft 5, wrap");
            //add(Light,"gapleft 5, wrap");
            add(LightNJ,"gapleft 5, wrap");
            add(None,"gapleft 5");
            
        }

        @Override
        ApplyOptionResult applyOptions() {
        	if (hasChanged()) {
	            InstallSettings.SECURITY_LEVEL.setValue(SecurityLevel);
	            if (SecurityLevel == 0) {
	            	InstallSettings.SECURITY_UPDATE.setValue(false);
	            	File hostiles = new File(CommonUtils.getUserSettingsDir(), "hostiles.txt");
	            	if (hostiles.exists()) hostiles.delete();
	            } else {
	            	InstallSettings.SECURITY_UPDATE.setValue(true);
	            }
	            return new ApplyOptionResult(true, true);
	        } else {
	        	return new ApplyOptionResult(false, true);
	        }
        }
        
        @Override
        boolean hasChanged() {
            return InstallSettings.SECURITY_LEVEL.getValue() != SecurityLevel;
        }

        @Override
        public void initOptions() {
        	SecurityLevel = InstallSettings.SECURITY_LEVEL.getValue();
        	switch (SecurityLevel) { 
        	case 4:
        		//Strong.setSelected(true);
        		//break;
        	case 3: 
        		StrongNJ.setSelected(true);
        		break;
        	case 2:
        		//Lite.setSelected(true);
        		//break;
        	case 1:
        		LightNJ.setSelected(true);
        		break;
        	case 0:
        		None.setSelected(true);
        	}
        }
    }
}
