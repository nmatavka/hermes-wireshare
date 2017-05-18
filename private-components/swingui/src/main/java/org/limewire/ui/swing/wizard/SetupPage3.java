package org.limewire.ui.swing.wizard;

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

import org.limewire.core.settings.FilterSettings;
import org.limewire.core.settings.InstallSettings;
import org.limewire.io.Expand;
import org.limewire.ui.swing.options.OptionPanel;
import org.limewire.ui.swing.options.OptionPanel.ApplyOptionResult;
import org.limewire.ui.swing.settings.StartupSettings;
import org.limewire.ui.swing.settings.SwingUiSettings;
import org.limewire.ui.swing.shell.LimeAssociationOption;
import org.limewire.ui.swing.shell.LimeAssociations;
import org.limewire.ui.swing.util.BackgroundExecutorService;
import org.limewire.ui.swing.util.I18n;
import org.limewire.ui.swing.util.MacOSXUtils;
import org.limewire.ui.swing.util.WindowsUtils;
import org.limewire.util.CommonUtils;
import org.limewire.util.OSUtils;

public class SetupPage3 extends WizardPage {

    private final JRadioButton Strong;
    private final JRadioButton Lite;
    private final JRadioButton StrongNJ;
    private final JRadioButton LiteNJ;        
    private final JRadioButton None;        
    
    private int SecurityLevel;
    
    public SetupPage3(SetupComponentDecorator decorator){
        super(decorator);
        
        setOpaque(false);
        setLayout(new MigLayout("insets 100 200 0 0, gap 0, nogrid"));       

        Strong = createAndDecorateLargeRadioButton(I18n.tr("Strong Security - Full Japanese Block."));
        Strong.setContentAreaFilled(false);
        StrongNJ = createAndDecorateLargeRadioButton(I18n.tr("Strong Security."));
        StrongNJ.setContentAreaFilled(false);
        Lite = createAndDecorateLargeRadioButton(I18n.tr("Light Security - Full Japanese Block."));
        Lite.setContentAreaFilled(false);
        LiteNJ = createAndDecorateLargeRadioButton(I18n.tr("Light Security."));
        LiteNJ.setContentAreaFilled(false);
        None = createAndDecorateLargeRadioButton(I18n.tr("None."));
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
        initOptions();
    }
    @Override
    public String getLine1() {
        return I18n.tr("Please select your desired security level.");
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
    public void  applySettings() {
    	String url = "http://wireshare.sourceforge.net/WSSecurityUpdates/";
    	String Hostiles = CommonUtils.getUserSettingsDir() + "\\hostiles.zip";
    	boolean Success = true;
        switch (SecurityLevel) {
        case 4:
        	Success = getHostiles(url + "HostilesFull.zip", Hostiles);
        	break;
        case 3:
        	Success = getHostiles(url + "HostilesNJ.zip", Hostiles);
        	break;
        case 2:
        	Success = getHostiles(url + "HostilesLight.zip", Hostiles);
        	break;
        case 1:
        	Success = getHostiles(url + "HostilesLightNJ.zip", Hostiles);
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
    }

    private void initOptions() {
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
    
    private boolean getHostiles(String strURL, String localFilename){
    	try {
			downloadFromUrl(strURL, localFilename);
			Extract(localFilename,CommonUtils.getUserSettingsDir());
			return true;
		} catch (IOException e) {
			return false;
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
