package org.limewire.ui.compose.integration;

import javax.swing.JComponent;

import org.limewire.mojito.Context;
import org.limewire.mojito.visual.ArcsVisualizer;
import org.limewire.ui.swing.plugin.SwingUiPlugin;

import com.google.inject.Inject;
import com.limegroup.gnutella.dht.DHTManager;

public final class ComposeMojitoArcsPlugin implements SwingUiPlugin {

    private final DHTManager dhtManager;

    private volatile ArcsVisualizer arcsVisualizer;

    @Inject
    public ComposeMojitoArcsPlugin(DHTManager dhtManager) {
        this.dhtManager = dhtManager;
    }

    @Override
    public JComponent getPluginComponent() {
        if (arcsVisualizer != null) {
            arcsVisualizer.stopArcs();
            arcsVisualizer = null;
        }

        Context context = (Context) dhtManager.getMojitoDHT();
        if (context == null) {
            return null;
        }

        arcsVisualizer = new ArcsVisualizer(context, context.getLocalNodeID());
        return arcsVisualizer;
    }

    @Override
    public void startPlugin() {
        if (arcsVisualizer != null) {
            arcsVisualizer.startArcs();
        }
    }

    @Override
    public void stopPlugin() {
        if (arcsVisualizer != null) {
            arcsVisualizer.stopArcs();
        }
    }

    @Override
    public String getPluginName() {
        return "Mojito Arcs Visualizer";
    }
}
