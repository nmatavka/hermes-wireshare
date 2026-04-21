package org.limewire.core.impl.download;


import org.limewire.core.api.download.DownloadListManager;
import org.limewire.core.api.download.ResultDownloader;
import org.limewire.core.impl.download.listener.ItunesDownloadListener;
import org.limewire.core.impl.download.listener.ItunesDownloadListenerFactory;
import org.limewire.core.impl.download.listener.TorrentDownloadListener;
import org.limewire.core.impl.download.listener.TorrentDownloadListenerFactory;
import org.limewire.listener.EventListener;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.limegroup.gnutella.downloader.DownloadStateEvent;

public class CoreGlueDownloadModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(DownloadListManager.class).to(CoreDownloadListManager.class);
        bind(ResultDownloader.class).to(CoreDownloadListManager.class);
        install(new FactoryModuleBuilder().build(ItunesDownloadListenerFactory.class));
        install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<EventListener<DownloadStateEvent>>() {
                }, TorrentDownloadListener.class)
                .build(TorrentDownloadListenerFactory.class));
        install(new FactoryModuleBuilder().build(CoreDownloadItem.Factory.class));
    }
}
