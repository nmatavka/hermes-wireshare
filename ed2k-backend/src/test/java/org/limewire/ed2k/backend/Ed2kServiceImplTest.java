package org.limewire.ed2k.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jmule.core.downloadmanager.DownloadManagerException;
import org.jmule.core.edonkey.ED2KFileLink;
import org.junit.Test;
import org.limewire.core.api.download.DownloadException;

public class Ed2kServiceImplTest {

    @Test
    public void duplicateAddFailuresAreDetected() {
        assertTrue(Ed2kServiceImpl.isDuplicateDownloadFailure(new DownloadManagerException("Download HASH already exists")));
        assertFalse(Ed2kServiceImpl.isDuplicateDownloadFailure(new DownloadManagerException("Download HASH not found")));
        assertTrue(Ed2kServiceImpl.isAlreadyStartedDownloadFailure(new DownloadManagerException("Download HASH is already started")));
        assertFalse(Ed2kServiceImpl.isAlreadyStartedDownloadFailure(new DownloadManagerException("Download HASH already exists")));
    }

    @Test
    public void duplicateAddFailureMapsToAlreadyDownloadingException() throws Exception {
        ED2KFileLink fileLink = new ED2KFileLink("ed2k://|file|example.bin|12345|3F688EBEA9A6BAE791E6870843DFE673|/");
        File requestedSaveFile = new File("/tmp/example.bin");

        DownloadException failure = Ed2kServiceImpl.duplicateDownloadException(
            fileLink,
            requestedSaveFile,
            new DownloadManagerException("Download 3F688EBEA9A6BAE791E6870843DFE673 already exists")
        );

        assertEquals(DownloadException.ErrorCode.FILE_ALREADY_DOWNLOADING, failure.getErrorCode());
        assertNotNull(failure.getFile());
        assertEquals(requestedSaveFile.getAbsoluteFile(), failure.getFile());
    }
}
