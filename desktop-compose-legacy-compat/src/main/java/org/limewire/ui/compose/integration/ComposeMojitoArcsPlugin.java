package org.limewire.ui.compose.integration;

import javax.swing.JComponent;

import org.limewire.mojito.Context;
import org.limewire.mojito.visual.ArcsVisualizer;

import com.google.inject.Inject;
import com.limegroup.gnutella.dht.DHTManager;

public final class ComposeMojitoArcsPlugin implements ComposeMojitoVisualizerPlugin {

    private final DHTManager dhtManager;

    private volatile ArcsVisualizer arcsVisualizer;

    @Inject
    public ComposeMojitoArcsPlugin(DHTManager dhtManager) {
        this.dhtManager = dhtManager;
    }

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

    public void startPlugin() {
        if (arcsVisualizer != null) {
            arcsVisualizer.startArcs();
        }
    }

    public void stopPlugin() {
        if (arcsVisualizer != null) {
            arcsVisualizer.stopArcs();
        }
    }

    public String getPluginName() {
        return "Mojito Arcs Visualizer";
    }

    @Override
    public ComposeMojitoVisualizerSession openSession() {
        JComponent component = getPluginComponent();
        if (component == null) {
            return null;
        }
        startPlugin();
        return new ComposeMojitoVisualizerSession() {
            @Override
            public String getTitle() {
                return getPluginName();
            }

            @Override
            public java.awt.Component component() {
                return component;
            }

            @Override
            public void close() {
                stopPlugin();
            }
        };
    }
}
