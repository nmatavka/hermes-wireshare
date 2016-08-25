package org.limewire.ui.swing.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.limewire.core.settings.InstallSettings;
import org.limewire.io.Expand;
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

        private JRadioButton Strong;
        private JRadioButton Lite;
        private JRadioButton StrongNJ;
        private JRadioButton LiteNJ;        
        private JRadioButton None;        
        
        private int SecurityLevel;
        
        public SecurityPanel() {
            super(I18n.tr("Security Level"));

            Strong = new JRadioButton(I18n.tr("Strong Security - Full Japanese Block."));
            Strong.setContentAreaFilled(false);
            StrongNJ = new JRadioButton(I18n.tr("Strong Security."));
            StrongNJ.setContentAreaFilled(false);
            Lite = new JRadioButton(I18n.tr("Light Security - Full Japanese Block."));
            Lite.setContentAreaFilled(false);
            LiteNJ = new JRadioButton(I18n.tr("Light Security."));
            LiteNJ.setContentAreaFilled(false);
            None = new JRadioButton(I18n.tr("None."));
            None.setContentAreaFilled(false);
            
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(Strong);
            buttonGroup.add(StrongNJ);
            buttonGroup.add(Lite);
            buttonGroup.add(LiteNJ);
            buttonGroup.add(None);

            Strong.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 4;
                }          
             });
            StrongNJ.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 3;
                }          
             });
            Lite.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 2;
                }          
             });
            LiteNJ.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 1;
                }          
             });
            None.addActionListener(new ActionListener( ) {
                public void actionPerformed(ActionEvent ae) {           
                 SecurityLevel = 0;
                }          
             });
            
            add(Strong,"gapleft 5, wrap");
            add(StrongNJ,"gapleft 5, wrap");
            add(Lite,"gapleft 5, wrap");
            add(LiteNJ,"gapleft 5, wrap");
            add(None,"gapleft 5");
            
        }

        @Override
        ApplyOptionResult applyOptions() {
        	if (hasChanged()) {
	        	String url = "http://wireshare.sourceforge.net/WSSecurityUpdates/";
	        	String Hostiles = CommonUtils.getUserSettingsDir() + "\\hostiles.zip";
	        	boolean Success = true;
	            switch (SecurityLevel) {
	            case 4:
	            	try {
						downloadFromUrl(url + "HostilesFull.zip", Hostiles);
						Extract(Hostiles,CommonUtils.getUserSettingsDir());
					} catch (IOException e) {
						Success = false;
					}
	            	break;
	            case 3:
	            	try {
						downloadFromUrl(url + "HostilesNJ.zip", Hostiles);
						Extract(Hostiles,CommonUtils.getUserSettingsDir());
					} catch (IOException e) {
						Success = false;
					}
	            	break;
	            case 2:
	            	try {
						downloadFromUrl(url + "HostilesLight.zip", Hostiles);
						Extract(Hostiles,CommonUtils.getUserSettingsDir());
					} catch (IOException e) {
						Success = false;
					}
	            	break;
	            case 1:
	            	try {
						downloadFromUrl(url + "HostilesLightNJ.zip", Hostiles);
						Extract(Hostiles,CommonUtils.getUserSettingsDir());
					} catch (IOException e) {
						Success = false;
					}
	            	break;
	            case 0:
	            	File hostiles = new File(Hostiles);
	            	hostiles.delete();
	            	Success = true;
	            }
	            if (Success) {
	            	try {
						URL vurl = new URL("http://wireshare.sourceforge.net/WSSecurityUpdates/version");
				        URLConnection con = vurl.openConnection();
				        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				        String version;
				        version=in.readLine();
				        in.close();
				        InstallSettings.SECURITY_VERSION.set(version);
	            	} catch (MalformedURLException e) {
					} catch (IOException e) {
					}
	            	InstallSettings.SECURITY_LEVEL.setValue(SecurityLevel);
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
        		Strong.setSelected(true);
        		break;
        	case 3: 
        		StrongNJ.setSelected(true);
        		break;
        	case 2:
        		Lite.setSelected(true);
        		break;
        	case 1:
        		LiteNJ.setSelected(true);
        		break;
        	case 0:
        		None.setSelected(true);
        		}
        }
        
        private void Extract(String zipFilePath, File Path) throws FileNotFoundException{
        	FileInputStream in = new FileInputStream(zipFilePath);
    	    try {
    			Expand.expandFile(in, Path, true, null);
    			in.close();
    			File zip = new File(zipFilePath);
    			zip.delete();
    		} catch (IOException e) {
    		}
        }
        
        private void downloadFromUrl(String strURL, String localFilename) throws IOException {
            InputStream is = null;
            FileOutputStream fos = null;
            URL url = new URL(strURL);
            try {
                URLConnection urlConn = url.openConnection();//connect

                is = urlConn.getInputStream();               //get connection inputstream
                fos = new FileOutputStream(localFilename);   //open outputstream to local file

                byte[] buffer = new byte[4096];              //declare 4KB buffer
                int len;

                //while we have available data, continue downloading and storing to local file
                while ((len = is.read(buffer)) > 0) {  
                    fos.write(buffer, 0, len);
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            }
        }
    }
}
