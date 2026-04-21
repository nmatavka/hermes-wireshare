package org.limewire.ui.swing.downloads.table;

import org.limewire.ui.swing.downloads.table.renderer.DownloadMessageRendererEditor;
import org.limewire.ui.swing.downloads.table.renderer.DownloadMessageRendererEditorFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LimeWireUiDownloadsTableModule extends AbstractModule {

    @Override
    protected void configure() {        
        install(new FactoryModuleBuilder().build(DownloadTableFactory.class));
        install(new FactoryModuleBuilder().build(DownloadTableMenuFactory.class));
        install(new FactoryModuleBuilder().build(DownloadPopupHandlerFactory.class));
        install(new FactoryModuleBuilder().build(DownloadMessageRendererEditorFactory.class));
    }

}
