package org.limewire.ui.swing.downloads.table;

import org.limewire.inject.FactoryModules;
import org.limewire.ui.swing.downloads.table.renderer.DownloadMessageRendererEditor;
import org.limewire.ui.swing.downloads.table.renderer.DownloadMessageRendererEditorFactory;

import com.google.inject.AbstractModule;

public class LimeWireUiDownloadsTableModule extends AbstractModule {

    @Override
    protected void configure() {        

        install(FactoryModules.newFactory(DownloadTableFactory.class, DownloadTable.class));
        install(FactoryModules.newFactory(DownloadTableMenuFactory.class, DownloadTableMenu.class));
        install(FactoryModules.newFactory(DownloadPopupHandlerFactory.class, DownloadPopupHandler.class));
        install(FactoryModules.newFactory(DownloadMessageRendererEditorFactory.class, DownloadMessageRendererEditor.class));
    }

}
