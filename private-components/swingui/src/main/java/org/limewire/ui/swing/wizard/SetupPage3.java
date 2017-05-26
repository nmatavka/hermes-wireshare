package org.limewire.ui.swing.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;
import org.limewire.core.settings.InstallSettings;

import javax.swing.ButtonGroup;

import org.limewire.ui.swing.util.I18n;
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
        return I18n.tr("NOTE: Changes to security level settings will not take effect until after WireShare is restarted.");
    }
    
    @Override
    public String getFooter() {
        return OSUtils.isMacOSX() ? I18n.tr("All settings can be changed later from WireShare > Preferences") :
            I18n.tr("All settings can be changed later in Tools > Options");
    }
    
    @Override
    public void  applySettings() {
        if (hasChanged()) {
        	InstallSettings.SECURITY_LEVEL.setValue(SecurityLevel);
        	if (SecurityLevel == 0) {
	        	InstallSettings.SECURITY_UPDATE.setValue(false);
	        	File hostiles = new File(CommonUtils.getUserSettingsDir(), "hostiles.txt");
	        	if (hostiles.exists()) hostiles.delete();
	        } else {
	        	InstallSettings.SECURITY_UPDATE.setValue(true);
	        }
        }
    }
    
    boolean hasChanged() {
        return InstallSettings.SECURITY_LEVEL.getValue() != SecurityLevel;
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
}
