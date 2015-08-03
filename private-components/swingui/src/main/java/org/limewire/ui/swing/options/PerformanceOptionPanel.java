package org.limewire.ui.swing.options;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.limewire.core.api.connection.GnutellaConnectionManager;
import org.limewire.core.api.network.NetworkManager;
import org.limewire.core.settings.DHTSettings;
import org.limewire.core.settings.SearchSettings;
import org.limewire.core.settings.UltrapeerSettings;
import org.limewire.ui.swing.components.MultiLineLabel;
import org.limewire.ui.swing.util.BackgroundExecutorService;
import org.limewire.ui.swing.util.I18n;

import com.google.inject.Inject;

/**
 * Performance Option View.
 */
public class PerformanceOptionPanel extends OptionPanel {

    private final NetworkManager networkManager;
    private final GnutellaConnectionManager connectionManager;
    private final String firstMultiLineLabel = I18n.tr("In order to abate network fragmentation, WireShare may operate in Ultrapeer mode or launch the Mojito Distributed Hash Table, subject to connection quality.  If quality degradation occurs, these features may be disabled by checking one or both of the boxes below.");
    private final String secondMultiLineLabel = I18n.tr("WireShare uses a secure communications mode called Transport Layer Security.  If it becomes resource intensive, it can be disabled by checking the box below.");
    private final String thirdMultiLineLabel = I18n.tr("In order to optimise search query turnaround times, servers may send replies along the shortest path, rather than that of the original request.  This is called out-of-band searching.  Some connections may not handle this feature well, however; in this case, it may be disabled by checking the box below.");
    
    private JCheckBox disableUltraPeerCheckBox;
    //private JCheckBox forceUltraPeerCheckBox;
    private JCheckBox disableMojitoCheckBox;
    private JCheckBox disableTLS;
    private JCheckBox disableOutOfBandSearchCheckBox;
    
    @Inject
    public PerformanceOptionPanel(NetworkManager networkManager, GnutellaConnectionManager connectionManager) {
        this.networkManager = networkManager;
        this.connectionManager = connectionManager;
        
        setLayout(new MigLayout("insets 15 15 15 15, fillx, wrap", "", ""));
        setOpaque(false);
        
        add(getPerformancePanel(), "pushx, growx");
    }
    
    private JPanel getPerformancePanel() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(""));
        p.setLayout(new MigLayout("fillx"));
        p.setOpaque(false);
        
        disableUltraPeerCheckBox = new JCheckBox(I18n.tr("Disable Ultrapeer capabilities"));
        //forceUltraPeerCheckBox = new JCheckBox(I18n.tr("Force Ultrapeer Mode"));
        disableMojitoCheckBox = new JCheckBox(I18n.tr("Disable connecting to the Mojito DHT"));
        disableTLS = new JCheckBox(I18n.tr("Disable TLS capabilities"));
        disableOutOfBandSearchCheckBox = new JCheckBox(I18n.tr("Disable out-of-band searching"));

        disableUltraPeerCheckBox.setOpaque(false);
        //forceUltraPeerCheckBox.setOpaque(false);
        disableMojitoCheckBox.setOpaque(false);
        disableTLS.setOpaque(false);
        disableOutOfBandSearchCheckBox.setOpaque(false);
        
        p.add(new MultiLineLabel(firstMultiLineLabel, AdvancedOptionPanel.MULTI_LINE_LABEL_WIDTH), "span, growx, wrap");
        
        p.add(disableUltraPeerCheckBox, "gapleft 25, split, wrap");
        
        //p.add(forceUltraPeerCheckBox, "gapleft 25, split, wrap");
        
        p.add(disableMojitoCheckBox, "gapleft 25, split, wrap");
        
        p.add(new MultiLineLabel(secondMultiLineLabel, AdvancedOptionPanel.MULTI_LINE_LABEL_WIDTH), "span, growx, gaptop 18, wrap");
        
        p.add(disableTLS, "gapleft 25, split, wrap");
        
        p.add(new MultiLineLabel(thirdMultiLineLabel, AdvancedOptionPanel.MULTI_LINE_LABEL_WIDTH), "span, growx, gaptop 18, wrap");
        
        p.add(disableOutOfBandSearchCheckBox, "gapleft 25, split, wrap");
        
        return p;
    }
    
    @Override
    ApplyOptionResult applyOptions() {
        
        boolean upChanged = UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue() != disableUltraPeerCheckBox.isSelected();
        //boolean forceChanged = UltrapeerSettings.FORCE_ULTRAPEER_MODE.getValue() != forceUltraPeerCheckBox.isSelected();
        boolean tlsServerChanged = disableTLS.isSelected() != !networkManager.isIncomingTLSEnabled();
        boolean isSupernode = connectionManager.isUltrapeer();
        
        UltrapeerSettings.DISABLE_ULTRAPEER_MODE.setValue(disableUltraPeerCheckBox.isSelected());
        //UltrapeerSettings.FORCE_ULTRAPEER_MODE.setValue(forceUltraPeerCheckBox.isSelected());
        DHTSettings.DISABLE_DHT_USER.setValue(disableMojitoCheckBox.isSelected());
        
        networkManager.setIncomingTLSEnabled(!disableTLS.isSelected());
        networkManager.setOutgoingTLSEnabled(!disableTLS.isSelected());
        
        SearchSettings.OOB_ENABLED.setValue(!disableOutOfBandSearchCheckBox.isSelected());
        
        if((tlsServerChanged || (upChanged && disableUltraPeerCheckBox.isSelected()) && isSupernode)) {
            if(tlsServerChanged && !disableTLS.isSelected()) {
                BackgroundExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        networkManager.validateTLS();
                        connectionManager.restart();
                    }
                });
            }
        }
        return new ApplyOptionResult(false, true);
    }

    @Override
    boolean hasChanged() {
        return UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue() != disableUltraPeerCheckBox.isSelected() 
        //|| UltrapeerSettings.FORCE_ULTRAPEER_MODE.getValue() != forceUltraPeerCheckBox.isSelected() 
        || DHTSettings.DISABLE_DHT_USER.getValue() != disableMojitoCheckBox.isSelected()
        || (!networkManager.isIncomingTLSEnabled() && !networkManager.isOutgoingTLSEnabled()) != disableTLS.isSelected()
        || SearchSettings.OOB_ENABLED.getValue() == disableOutOfBandSearchCheckBox.isSelected();
    }

    @Override
    public void initOptions() {
        disableUltraPeerCheckBox.setSelected(UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue());
        //forceUltraPeerCheckBox.setSelected(UltrapeerSettings.FORCE_ULTRAPEER_MODE.getValue());
        disableMojitoCheckBox.setSelected(DHTSettings.DISABLE_DHT_USER.getValue());
        disableTLS.setSelected(!networkManager.isIncomingTLSEnabled() || !networkManager.isOutgoingTLSEnabled());
        disableOutOfBandSearchCheckBox.setSelected(!SearchSettings.OOB_ENABLED.getValue());
    }

}