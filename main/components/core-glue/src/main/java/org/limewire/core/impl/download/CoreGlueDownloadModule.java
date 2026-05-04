package org.limewire.core.impl.download;


import org.limewire.core.api.download.DownloadListManager;
import org.limewire.core.api.download.ResultDownloader;
import org.limewire.inject.FactoryModules;
import org.limewire.core.impl.download.listener.ItunesDownloadListener;
import org.limewire.core.impl.download.listener.ItunesDownloadListenerFactory;
import org.limewire.core.impl.download.listener.TorrentDownloadListener;
import org.limewire.core.impl.download.listener.TorrentDownloadListenerFactory;

import com.google.inject.AbstractModule;

public class CoreGlueDownloadModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(DownloadListManager.class).to(CoreDownloadListManager.class);
        bind(ResultDownloader.class).to(CoreDownloadListManager.class);
        install(FactoryModules.newFactory(ItunesDownloadListenerFactory.class, ItunesDownloadListener.class));
        install(FactoryModules.newFactory(TorrentDownloadListenerFactory.class, TorrentDownloadListener.class));
        install(FactoryModules.newFactory(CoreDownloadItem.Factory.class, CoreDownloadItem.class));
    }
}
