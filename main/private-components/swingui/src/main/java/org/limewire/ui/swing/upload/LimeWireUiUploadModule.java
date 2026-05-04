package org.limewire.ui.swing.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.limewire.core.api.upload.UploadItem;
import org.limewire.core.api.upload.UploadState;
import org.limewire.inject.FactoryModules;
import org.limewire.ui.swing.upload.table.FinishedUploadSelected;
import org.limewire.ui.swing.upload.table.UploadPopupMenu;
import org.limewire.ui.swing.upload.table.UploadPopupMenuFactory;
import org.limewire.ui.swing.upload.table.UploadTable;
import org.limewire.ui.swing.upload.table.UploadTableFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Module to configure Guice bindings for the Uploads UI classes.
 */
public class LimeWireUiUploadModule extends AbstractModule {

    @Override
    protected void configure() {
        install(FactoryModules.newFactory(UploadPopupMenuFactory.class, UploadPopupMenu.class));
        install(FactoryModules.newFactory(UploadTableFactory.class, UploadTable.class));
    }
    
    /**
     * Returns a list of selected upload files that are finished.  This is 
     * bound as a Provider method.  The Provider should only be accessed on 
     * the UI thread.
     */
    @Provides
    @FinishedUploadSelected
    List<File> selectedFiles(UploadMediator uploadMediator) {
        List<File> files = new ArrayList<File>();
        
        List<UploadItem> uploadItems = uploadMediator.getSelectedUploads();
        for (UploadItem item : uploadItems) {
            if (item.getState() == UploadState.DONE) {
                files.addAll(item.getCompleteFiles());
            }
        }
        
        return files;
    }
}
