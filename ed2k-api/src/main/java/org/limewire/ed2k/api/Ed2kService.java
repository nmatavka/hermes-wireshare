package org.limewire.ed2k.api;

import java.io.File;
import java.util.List;

import org.limewire.core.api.download.DownloadItem;
import org.limewire.core.api.search.GroupedSearchResult;
import org.limewire.core.api.upload.UploadItem;

public interface Ed2kService {

    boolean isRunning();

    void addListener(Ed2kListener listener);

    void removeListener(Ed2kListener listener);

    Ed2kOpenLinkResult openEd2kLink(String link) throws Exception;

    void addEd2kDownload(String link) throws Exception;

    void addSearchResultDownload(GroupedSearchResult result, File saveFile, boolean overwrite) throws Exception;

    Ed2kSearchSession startSearch(String query);

    Ed2kSearchSession repeatSearch(String sessionId, boolean clearExisting);

    void stopSearch(String sessionId);

    Ed2kSearchSession getSearchSession(String sessionId);

    List<DownloadItem> getDownloads();

    List<UploadItem> getUploads();

    Ed2kStatus getStatus();

    List<Ed2kServerRecord> getServers();

    void connectServer(String address, int port) throws Exception;

    void connectAnyServer() throws Exception;

    void disconnectServer() throws Exception;

    void importServers(File file) throws Exception;

    void importKadNodes(File file) throws Exception;

    void bootstrapKad(String host, int port) throws Exception;

    void connectKad();

    void disconnectKad();

    void requestMoreSources(DownloadItem item) throws Exception;
}
