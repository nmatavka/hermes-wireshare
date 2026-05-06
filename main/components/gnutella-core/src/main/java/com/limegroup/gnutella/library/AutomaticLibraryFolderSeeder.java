package com.limegroup.gnutella.library;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.limewire.core.api.Category;
import org.limewire.core.settings.SharingSettings;
import org.limewire.inject.EagerSingleton;
import org.limewire.listener.EventListener;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.util.FileUtils;

import com.google.inject.Inject;
import com.limegroup.gnutella.DownloadManager;

/**
 * Restores automatic seeding of the built-in save/share roots on startup.
 */
@EagerSingleton
class AutomaticLibraryFolderSeeder {

    private static final Log LOG = LogFactory.getLog(AutomaticLibraryFolderSeeder.class);

    private final Library library;
    private final FileCollectionManager collectionManager;
    private final DownloadManager downloadManager;

    @Inject
    AutomaticLibraryFolderSeeder(Library library, FileCollectionManager collectionManager,
            DownloadManager downloadManager) {
        this.library = library;
        this.collectionManager = collectionManager;
        this.downloadManager = downloadManager;
    }

    @Inject
    void register() {
        library.addManagedListStatusListener(new EventListener<LibraryStatusEvent>() {
            @Override
            public void handleEvent(LibraryStatusEvent event) {
                if (event.getType() == LibraryStatusEvent.Type.LOAD_FINISHING) {
                    seedDefaultRoots();
                }
            }
        });
    }

    private void seedDefaultRoots() {
        seedManagedSaveRoots();
        seedPublicSharedRoot();
        ensureDirectoryExists(FileUtils.canonicalize(SharingSettings.INCOMPLETE_DIRECTORY.get()));
        downloadManager.getIncompleteFileManager().registerAllIncompleteFiles();
    }

    private void seedManagedSaveRoots() {
        for (File saveRoot : getDistinctSaveRoots()) {
            ensureDirectoryExists(saveRoot);

            File[] entries = saveRoot.listFiles();
            if (entries == null) {
                continue;
            }

            for (File entry : entries) {
                if (entry.isFile()) {
                    library.add(entry);
                }
            }
        }
    }

    private void seedPublicSharedRoot() {
        File sharedRoot = FileUtils.canonicalize(SharingSettings.DEFAULT_SHARE_DIR);
        ensureDirectoryExists(sharedRoot);

        final SharedFileCollection publicCollection =
                collectionManager.getCollectionById(LibraryFileData.DEFAULT_SHARED_COLLECTION_ID);
        if (publicCollection == null || !publicCollection.isDirectoryAllowed(sharedRoot)) {
            return;
        }

        publicCollection.addFolder(sharedRoot, new FileFilter() {
            @Override
            public boolean accept(File candidate) {
                return candidate.isDirectory() || publicCollection.isFileAllowed(candidate);
            }
        });
    }

    private Set<File> getDistinctSaveRoots() {
        Set<File> roots = new LinkedHashSet<File>();
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory()));
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory(Category.AUDIO)));
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory(Category.VIDEO)));
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory(Category.IMAGE)));
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory(Category.DOCUMENT)));
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory(Category.PROGRAM)));
        roots.add(FileUtils.canonicalize(SharingSettings.getSaveDirectory(Category.OTHER)));
        roots.remove(null);
        return roots;
    }

    private void ensureDirectoryExists(File directory) {
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            LOG.debugf("Unable to create auto-seeded directory {0}", directory);
        }
    }
}
