package org.limewire.ed2k.backend;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.limewire.core.api.Category;
import org.limewire.core.api.FilePropertyKey;
import org.limewire.core.api.URN;
import org.limewire.core.api.download.DownloadException;
import org.limewire.core.api.download.DownloadItem;
import org.limewire.core.api.download.DownloadPiecesInfo;
import org.limewire.core.api.download.DownloadPropertyKey;
import org.limewire.core.api.download.DownloadState;
import org.limewire.core.api.endpoint.RemoteHost;
import org.limewire.core.api.file.CategoryManager;
import org.limewire.core.api.library.PropertiableFile;
import org.limewire.core.api.library.LocalFileItem;
import org.limewire.core.api.library.SharedFileList;
import org.limewire.core.api.library.SharedFileListManager;
import org.limewire.core.api.search.GroupedSearchResult;
import org.limewire.core.api.search.SearchResult;
import org.limewire.core.api.transfer.SourceInfo;
import org.limewire.core.api.upload.UploadItem;
import org.limewire.core.api.upload.UploadState;
import org.limewire.core.settings.SharingSettings;
import org.limewire.ed2k.api.Ed2kDownloadItem;
import org.limewire.ed2k.api.Ed2kGroupedSearchResultView;
import org.limewire.ed2k.api.Ed2kLinkTargetType;
import org.limewire.ed2k.api.Ed2kListener;
import org.limewire.ed2k.api.Ed2kOpenLinkResult;
import org.limewire.ed2k.api.Ed2kSearchResultView;
import org.limewire.ed2k.api.Ed2kSearchSession;
import org.limewire.ed2k.api.Ed2kServerRecord;
import org.limewire.ed2k.api.Ed2kService;
import org.limewire.ed2k.api.Ed2kStatus;
import org.limewire.ed2k.api.Ed2kUploadItem;
import org.limewire.ed2k.jed2k.EmuleLink;
import org.limewire.ed2k.jed2k.KadNodesDatImporter;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendPresence;
import org.limewire.inject.EagerSingleton;
import org.limewire.io.Address;
import org.limewire.lifecycle.Service;
import org.limewire.lifecycle.ServiceRegistry;
import org.limewire.lifecycle.ServiceStage;
import org.limewire.util.CommonUtils;
import org.limewire.util.FileUtils;
import org.jmule.core.JMuleCore;
import org.jmule.core.JMuleCoreException;
import org.jmule.core.JMuleCoreFactory;
import org.jmule.core.configmanager.ConfigurationManager;
import org.jmule.core.configmanager.ConfigurationManagerException;
import org.jmule.core.downloadmanager.DownloadManager;
import org.jmule.core.downloadmanager.DownloadManagerException;
import org.jmule.core.downloadmanager.DownloadSession;
import org.jmule.core.edonkey.ED2KFileLink;
import org.jmule.core.edonkey.ED2KLinkMalformedException;
import org.jmule.core.edonkey.FileHash;
import org.jmule.core.downloadmanager.InternalDownloadManager;
import org.jmule.core.jkad.ContactAddress;
import org.jmule.core.jkad.IPAddress;
import org.jmule.core.jkad.Int128;
import org.jmule.core.jkad.JKadListener;
import org.jmule.core.jkad.JKadManager;
import org.jmule.core.jkad.indexer.Source;
import org.jmule.core.jkad.routingtable.KadContact;
import org.jmule.core.jkad.utils.Convert;
import org.jmule.core.peermanager.InternalPeerManager;
import org.jmule.core.peermanager.Peer;
import org.jmule.core.peermanager.Peer.PeerSource;
import org.jmule.core.peermanager.PeerManagerException;
import org.jmule.core.searchmanager.SearchManager;
import org.jmule.core.searchmanager.SearchQuery;
import org.jmule.core.searchmanager.SearchQueryType;
import org.jmule.core.searchmanager.SearchResultItem;
import org.jmule.core.searchmanager.SearchResultListener;
import org.jmule.core.servermanager.Server;
import org.jmule.core.servermanager.ServerManager;
import org.jmule.core.servermanager.ServerManagerListener;
import org.jmule.core.sharingmanager.CompletedFile;
import org.jmule.core.sharingmanager.SharingManager;
import org.jmule.core.uploadmanager.UploadManager;
import org.jmule.core.uploadmanager.InternalUploadManager;
import org.jmule.core.uploadmanager.UploadSession;

import com.google.inject.Inject;

@EagerSingleton
class Ed2kServiceImpl implements Ed2kService {

    private static final long SYNC_INTERVAL_MS = 1500L;
    private static final long STARTUP_AUTO_CONNECT_DELAY_MS = 5000L;
    private static final long SEARCH_RESUBMIT_INTERVAL_MS = 5000L;
    private static final long JMULE_INFINITY_ETA_SECONDS = 31536000L;

    private final CategoryManager categoryManager;
    private final SharedFileListManager sharedFileListManager;
    private final CopyOnWriteArrayList<Ed2kListener> listeners = new CopyOnWriteArrayList<Ed2kListener>();
    private final Map<String, Ed2kDownloadItemAdapter> downloadItems = new LinkedHashMap<String, Ed2kDownloadItemAdapter>();
    private final Map<String, Ed2kUploadItemAdapter> uploadItems = new LinkedHashMap<String, Ed2kUploadItemAdapter>();
    private final Map<String, Ed2kSearchSessionImpl> searchSessionsById = new ConcurrentHashMap<String, Ed2kSearchSessionImpl>();
    private final Map<String, List<Ed2kSearchSessionImpl>> searchSessionsByQuery = new ConcurrentHashMap<String, List<Ed2kSearchSessionImpl>>();
    private final SearchResultListener searchResultListener = new SearchResultListener() {
        @Override
        public void searchStarted(SearchQuery query) {
            forEachSearchSession(query, new SearchSessionAction() {
                @Override
                public void apply(Ed2kSearchSessionImpl session) {
                    session.setRunning(true);
                    session.setFailed(false, null);
                    fireSearchChanged(session.getId());
                    fireStatusChanged();
                }
            });
        }

        @Override
        public void resultArrived(org.jmule.core.searchmanager.SearchResult searchResult) {
            List<Ed2kSearchSessionImpl> sessions = sessionsForSearchResult(searchResult);
            if (sessions == null || sessions.isEmpty()) {
                return;
            }
            boolean changed = false;
            for (SearchResultItem item : searchResult.getSearchResultItemList()) {
                for (Ed2kSearchSessionImpl session : sessions) {
                    try {
                        changed |= session.addResult(item);
                    } catch (Throwable ignored) {
                    }
                }
            }
            if (changed) {
                for (Ed2kSearchSessionImpl session : sessions) {
                    fireSearchChanged(session.getId());
                }
            }
        }

        @Override
        public void searchCompleted(SearchQuery query) {
            forEachSearchSession(query, new SearchSessionAction() {
                @Override
                public void apply(Ed2kSearchSessionImpl session) {
                    session.setRunning(false);
                    fireSearchChanged(session.getId());
                    fireStatusChanged();
                }
            });
        }

        @Override
        public void searchFailed(SearchQuery query) {
            forEachSearchSession(query, new SearchSessionAction() {
                @Override
                public void apply(Ed2kSearchSessionImpl session) {
                    session.setRunning(false);
                    session.setFailed(true, "ED2K/Kad search failed.");
                    fireSearchChanged(session.getId());
                    fireStatusChanged();
                }
            });
        }
    };
    private final ServerManagerListener serverListener = new ServerManagerListener() {
        @Override public void connected(Server server) {
            if (server != null) {
                WireShareEd2kPaths.rememberLastConnectedServer(server.getAddress(), server.getPort());
            }
            lastServerStatusDetail = "Connected to " + describeServer(server);
            fireStatusChanged();
            retrySearchesAfterNetworkReady();
        }
        @Override public void disconnected(Server server) {
            lastServerStatusDetail = server == null ? "Disconnected from ED2K server." : "Disconnected from " + describeServer(server);
            fireStatusChanged();
        }
        @Override public void isConnecting(Server server) {
            lastServerStatusDetail = "Connecting to " + describeServer(server);
            fireStatusChanged();
        }
        @Override public void serverMessage(Server server, String message) {
            if (message != null && !message.trim().isEmpty()) {
                lastServerStatusDetail = message.trim();
            }
            fireStatusChanged();
        }
        @Override public void serverConnectingFailed(Server server, Throwable cause) {
            lastServerStatusDetail = cause != null && cause.getMessage() != null && !cause.getMessage().trim().isEmpty()
                ? cause.getMessage().trim()
                : "Unable to connect to " + describeServer(server) + ".";
            fireStatusChanged();
        }
        @Override public void serverAdded(Server server) { fireStatusChanged(); }
        @Override public void serverRemoved(Server server) { fireStatusChanged(); }
        @Override public void serverListCleared() { fireStatusChanged(); }
        @Override public void autoConnectStarted() {
            lastServerStatusDetail = "Trying available ED2K servers.";
            fireStatusChanged();
        }
        @Override public void autoConnectFailed() {
            lastServerStatusDetail = "Could not connect to any loaded ED2K server.";
            fireStatusChanged();
        }
    };
    private final JKadListener kadListener = new JKadListener() {
        @Override public void JKadIsConnecting() { fireStatusChanged(); }
        @Override public void JKadIsConnected() {
            fireStatusChanged();
            retrySearchesAfterNetworkReady();
        }
        @Override public void JKadIsDisconnected() { fireStatusChanged(); }
    };
    private final PropertyChangeListener sharedRootsListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || SharedFileListManager.SHARED_FILE_COUNT.equals(evt.getPropertyName())) {
                updateSharedRoots();
            }
        }
    };

    private volatile ScheduledExecutorService poller;
    private volatile JMuleCore core;
    private volatile boolean running;
    private volatile String lastServerStatusDetail;
    private volatile Ed2kStatus lastStatus = new Ed2kStatus(
        false,
        Ed2kStatus.ConnectionState.DISCONNECTED,
        Ed2kStatus.ConnectionState.DISCONNECTED,
        Ed2kStatus.KadBootstrapState.NOT_BOOTSTRAPPED,
        null,
        null,
        0,
        0,
        0,
        0,
        0
    );
    private volatile int nextSearchId = 1;

    @Inject
    Ed2kServiceImpl(CategoryManager categoryManager, SharedFileListManager sharedFileListManager) {
        this.categoryManager = categoryManager;
        this.sharedFileListManager = sharedFileListManager;
    }

    @Inject
    @SuppressWarnings("unused")
    private void register(ServiceRegistry registry) {
        registry.register(new Service() {
            @Override
            public String getServiceName() {
                return "ED2K/Kad";
            }

            @Override
            public void initialize() {
            }

            @Override
            public void start() {
                startBackend();
            }

            @Override
            public void stop() {
                stopBackend();
            }
        }).in(ServiceStage.VERY_LATE);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void addListener(Ed2kListener listener) {
        listeners.addIfAbsent(listener);
    }

    @Override
    public void removeListener(Ed2kListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Ed2kOpenLinkResult openEd2kLink(String link) throws Exception {
        ensureBackendStarted();
        EmuleLink emuleLink = EmuleLink.parse(link);
        switch (emuleLink.getType()) {
        case FILE:
            queueDownload(new ED2KFileLink(link), null, false);
            return new Ed2kOpenLinkResult(Ed2kLinkTargetType.FILE, true, "Added ED2K download.");
        case SERVER:
            connectServer(emuleLink.getStringValue(), (int) emuleLink.getNumberValue());
            return new Ed2kOpenLinkResult(Ed2kLinkTargetType.SERVER, false, "Connecting to ED2K server.");
        case SERVERLIST:
            importServers(downloadLinkedResource(emuleLink.getStringValue(), ".met"));
            return new Ed2kOpenLinkResult(Ed2kLinkTargetType.SERVERLIST, false, "Imported ED2K server list.");
        case NODESLIST:
            importKadNodes(downloadLinkedResource(emuleLink.getStringValue(), ".dat"));
            connectKad();
            return new Ed2kOpenLinkResult(Ed2kLinkTargetType.NODESLIST, false, "Imported Kad nodes and started bootstrap.");
        default:
            throw new IllegalArgumentException("Unsupported ED2K link type.");
        }
    }

    @Override
    public void addEd2kDownload(String link) throws Exception {
        ensureBackendStarted();
        EmuleLink emuleLink = EmuleLink.parse(link);
        if (emuleLink.getType() != Ed2kLinkTargetType.FILE) {
            throw new IllegalArgumentException("This ED2K link does not point to a downloadable file.");
        }
        queueDownload(new ED2KFileLink(link), null, false);
    }

    @Override
    public void addSearchResultDownload(GroupedSearchResult result, File saveFile, boolean overwrite) throws Exception {
        ensureBackendStarted();
        if (!(result instanceof Ed2kGroupedSearchResultView)) {
            throw new IllegalArgumentException("Result is not backed by ED2K.");
        }
        queueDownload(new ED2KFileLink(((Ed2kGroupedSearchResultView) result).getEd2kLink()), saveFile, overwrite);
    }

    @Override
    public Ed2kSearchSession startSearch(String query) {
        ensureBackendStartedForSearch();
        final String sessionId = "ed2k-search-" + (nextSearchId++);
        final Ed2kSearchSessionImpl session = new Ed2kSearchSessionImpl(sessionId, query, categoryManager);
        session.setRunning(true);
        searchSessionsById.put(sessionId, session);
        final String key = normalizeQuery(query);
        synchronized (searchSessionsByQuery) {
            List<Ed2kSearchSessionImpl> sessions = searchSessionsByQuery.get(key);
            if (sessions == null) {
                sessions = new ArrayList<Ed2kSearchSessionImpl>();
                searchSessionsByQuery.put(key, sessions);
            }
            sessions.add(session);
        }
        submitSearch(session, true);
        fireSearchChanged(sessionId);
        return session;
    }

    @Override
    public Ed2kSearchSession repeatSearch(String sessionId, boolean clearExisting) {
        Ed2kSearchSessionImpl session = searchSessionsById.get(sessionId);
        if (session == null) {
            return null;
        }
        if (clearExisting) {
            session.clear();
        }
        session.setRunning(true);
        session.setFailed(false, null);
        submitSearch(session, true);
        fireSearchChanged(sessionId);
        return session;
    }

    private void submitSearch(Ed2kSearchSessionImpl session, boolean force) {
        if (session == null) {
            return;
        }
        JMuleCore activeCore = core;
        if (activeCore == null) {
            session.setRunning(false);
            session.setFailed(true, "ED2K/Kad backend is not running.");
            return;
        }
        if (!force && !session.canSubmitAgain(System.currentTimeMillis(), SEARCH_RESUBMIT_INTERVAL_MS)) {
            return;
        }
        try {
            session.markSubmitted();
            session.setRunning(true);
            session.setFailed(false, null);
            activeCore.getSearchManager().search(new SearchQuery(session.getQuery(), SearchQueryType.SERVER_KAD));
            activeCore.getSearchManager().search(new SearchQuery(session.getQuery(), SearchQueryType.GLOBAL));
        } catch (RuntimeException failure) {
            session.setRunning(false);
            session.setFailed(true, failure.getMessage());
        }
    }

    private void retrySearchesAfterNetworkReady() {
        JMuleCore activeCore = core;
        if (!running || activeCore == null || searchSessionsById.isEmpty()) {
            return;
        }
        for (Ed2kSearchSessionImpl session : searchSessionsById.values()) {
            if (!session.shouldRetryWhenNetworkReady()) {
                continue;
            }
            submitSearch(session, false);
            fireSearchChanged(session.getId());
        }
    }

    @Override
    public void stopSearch(String sessionId) {
        Ed2kSearchSessionImpl session = searchSessionsById.remove(sessionId);
        if (session == null) {
            return;
        }
        final String key = normalizeQuery(session.getQuery());
        synchronized (searchSessionsByQuery) {
            List<Ed2kSearchSessionImpl> sessions = searchSessionsByQuery.get(key);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    searchSessionsByQuery.remove(key);
                    if (core != null) {
                        core.getSearchManager().removeSearch(new SearchQuery(session.getQuery(), SearchQueryType.SERVER_KAD));
                        core.getSearchManager().removeSearch(new SearchQuery(session.getQuery(), SearchQueryType.GLOBAL));
                    }
                }
            }
        }
        fireSearchChanged(sessionId);
    }

    @Override
    public Ed2kSearchSession getSearchSession(String sessionId) {
        return searchSessionsById.get(sessionId);
    }

    @Override
    public List<DownloadItem> getDownloads() {
        synchronized (downloadItems) {
            return new ArrayList<DownloadItem>(downloadItems.values());
        }
    }

    @Override
    public List<UploadItem> getUploads() {
        synchronized (uploadItems) {
            return new ArrayList<UploadItem>(uploadItems.values());
        }
    }

    @Override
    public void removeDownload(DownloadItem item) throws Exception {
        FileHash fileHash = fileHashFor(item);
        if (fileHash == null) {
            return;
        }
        String key = normalizeFileHash(fileHash);
        if (core != null && core.getDownloadManager().hasDownload(fileHash)) {
            core.getDownloadManager().cancelDownload(fileHash);
        }
        WireShareEd2kPaths.clearPreferredTarget(key);
        boolean removed;
        synchronized (downloadItems) {
            removed = downloadItems.remove(key) != null;
        }
        if (removed) {
            fireDownloadsChanged();
        }
        synchronizeTransfers();
    }

    @Override
    public void removeUpload(UploadItem item) throws Exception {
        FileHash fileHash = fileHashFor(item);
        if (fileHash == null) {
            return;
        }
        String key = normalizeFileHash(fileHash);
        if (core != null && core.getUploadManager().hasUpload(fileHash)) {
            ((InternalUploadManager) core.getUploadManager()).removeUpload(fileHash);
        }
        boolean removed;
        synchronized (uploadItems) {
            removed = uploadItems.remove(key) != null;
        }
        if (removed) {
            fireUploadsChanged();
        }
        synchronizeTransfers();
    }

    @Override
    public Ed2kStatus getStatus() {
        return lastStatus;
    }

    @Override
    public List<Ed2kServerRecord> getServers() {
        if (core == null) {
            return Collections.emptyList();
        }
        List<Ed2kServerRecord> records = new ArrayList<Ed2kServerRecord>();
        for (Server server : core.getServerManager().getServers()) {
            records.add(new Ed2kServerRecord(
                safeServerAddress(server),
                server.getPort(),
                safeServerName(server),
                safeServerDescription(server),
                safeServerUsers(server),
                safeServerFiles(server),
                server.isConnected()
            ));
        }
        Collections.sort(records, new Comparator<Ed2kServerRecord>() {
            @Override
            public int compare(Ed2kServerRecord left, Ed2kServerRecord right) {
                if (left.isConnected() != right.isConnected()) {
                    return left.isConnected() ? -1 : 1;
                }
                return left.getName().compareToIgnoreCase(right.getName());
            }
        });
        return records;
    }

    @Override
    public void connectServer(String address, int port) throws Exception {
        ensureRunning();
        ServerManager serverManager = core.getServerManager();
        Server server = serverManager.getServer(address, port);
        if (server == null) {
            server = serverManager.newServer(address, port);
        }
        lastServerStatusDetail = "Connecting to " + describeServer(server);
        serverManager.connect(server);
        fireStatusChanged();
    }

    @Override
    public void connectAnyServer() throws Exception {
        ensureRunning();
        ServerManager serverManager = core.getServerManager();
        if (connectLastKnownServer(serverManager)) {
            fireStatusChanged();
            return;
        }
        if (serverManager.getServersCount() == 0) {
            throw new JMuleCoreException("No ED2K servers are loaded. Import server.met first.");
        }
        lastServerStatusDetail = "Trying available ED2K servers.";
        serverManager.connect();
        fireStatusChanged();
    }

    @Override
    public void disconnectServer() throws Exception {
        ensureRunning();
        lastServerStatusDetail = "Disconnecting from ED2K server.";
        core.getServerManager().disconnect();
        fireStatusChanged();
    }

    @Override
    public void importServers(File file) throws Exception {
        ensureRunning();
        int before = core.getServerManager().getServersCount();
        core.getServerManager().importList(file.getAbsolutePath());
        int after = core.getServerManager().getServersCount();
        if (before == 0 && after == 0) {
            throw new JMuleCoreException("No ED2K servers were found in that file.");
        }
        lastServerStatusDetail = after == before
            ? "ED2K server list loaded, but all entries were already present."
            : "Loaded " + (after - before) + " ED2K server" + (after - before == 1 ? "" : "s") + ".";
        fireStatusChanged();
    }

    @Override
    public void importKadNodes(File file) throws Exception {
        ensureBackendStarted();
        List<KadContact> contacts = KadNodesDatImporter.parse(file);
        for (KadContact contact : contacts) {
            core.getJKadManager().getRoutingTable().addContact(contact);
        }
        fireStatusChanged();
    }

    @Override
    public void bootstrapKad(String host, int port) throws Exception {
        ensureBackendStarted();
        core.getJKadManager().connect(new ContactAddress(new IPAddress(InetAddress.getByName(host).getAddress()), port));
        fireStatusChanged();
    }

    @Override
    public void connectKad() {
        if (core == null) {
            return;
        }
        core.getJKadManager().connect();
        fireStatusChanged();
    }

    @Override
    public void disconnectKad() {
        if (core == null) {
            return;
        }
        core.getJKadManager().disconnect();
        fireStatusChanged();
    }

    @Override
    public void requestMoreSources(DownloadItem item) throws Exception {
        ensureRunning();
        FileHash fileHash = fileHashFor(item);
        if (fileHash == null) {
            throw new IllegalArgumentException("This download is not backed by ED2K.");
        }
        final DownloadSession downloadSession = core.getDownloadManager().getDownload(fileHash);
        if (downloadSession == null) {
            throw new IllegalArgumentException("The ED2K download session could not be found.");
        }
        if (!downloadSession.isStarted()) {
            core.getDownloadManager().startDownload(fileHash);
        }
        if (!core.getJKadManager().isConnected()) {
            fireStatusChanged();
            return;
        }

        byte[] hashBytes = fileHash.getHash().clone();
        Convert.updateSearchID(hashBytes);
        final Int128 searchId = new Int128(hashBytes);
        core.getJKadManager().getSearch().searchSources(searchId, new org.jmule.core.jkad.search.SearchResultListener() {
            @Override
            public void searchStarted() {
            }

            @Override
            public void processNewResults(List<Source> result) {
                List<Peer> peers = new ArrayList<Peer>();
                InternalPeerManager peerManager = (InternalPeerManager) core.getPeerManager();
                for (Source source : result) {
                    String address = source.getAddress().toString();
                    int tcpPort = source.getTCPPort();
                    if (tcpPort <= 0 || hasDownloadPeer(downloadSession, address, tcpPort)) {
                        continue;
                    }
                    try {
                        Peer peer = peerManager.hasPeer(address, tcpPort)
                            ? peerManager.getPeer(address, tcpPort)
                            : peerManager.newPeer(address, tcpPort, PeerSource.KAD);
                        peers.add(peer);
                    } catch (PeerManagerException ignored) {
                    }
                }
                ((InternalDownloadManager) core.getDownloadManager()).addDownloadPeers(fileHash, peers);
                synchronizeTransfers();
            }

            @Override
            public void searchFinished() {
                synchronizeTransfers();
            }
        }, downloadSession.getSharedFile().length());
    }

    private synchronized void startBackend() {
        if (running) {
            return;
        }
        try {
            File saveDir = canonicalFile(SharingSettings.getSaveDirectory());
            File incompleteDir = canonicalFile(SharingSettings.INCOMPLETE_DIRECTORY.get());
            File stateRoot = canonicalFile(new File(CommonUtils.getUserSettingsDir(), "ed2k"));
            WireShareEd2kPaths.configure(saveDir, incompleteDir, stateRoot);
            core = JMuleCoreFactory.create();
            core.start();
            configureCore(core, saveDir, stateRoot);
            sharedFileListManager.addPropertyChangeListener(sharedRootsListener);
            core.getServerManager().addServerListListener(serverListener);
            core.getJKadManager().addJKadListener(kadListener);
            core.getSearchManager().addSeachResultListener(searchResultListener);
            running = true;
            startPoller();
            synchronizeTransfers();
            beginStartupAutoConnect();
            fireStatusChanged();
        } catch (Throwable failure) {
            running = false;
            failure.printStackTrace();
        }
    }

    private synchronized void stopBackend() {
        running = false;
        stopPoller();
        if (core == null) {
            return;
        }
        try {
            sharedFileListManager.removePropertyChangeListener(sharedRootsListener);
        } catch (Throwable ignored) {
        }
        try {
            core.getSearchManager().removeSearchResultListener(searchResultListener);
        } catch (Throwable ignored) {
        }
        try {
            core.getJKadManager().removeJKadListener(kadListener);
        } catch (Throwable ignored) {
        }
        try {
            core.getServerManager().removeServerListListener(serverListener);
        } catch (Throwable ignored) {
        }
        try {
            core.stop();
        } catch (JMuleCoreException failure) {
            failure.printStackTrace();
        }
        core = null;
    }

    private void configureCore(JMuleCore core, File saveDir, File stateRoot) throws ConfigurationManagerException {
        ConfigurationManager configurationManager = core.getConfigurationManager();
        configurationManager.setWorkingDir(stateRoot);
        configurationManager.setSharedFolders(currentSharedRoots(saveDir));
        core.getSharingManager().loadCompletedFiles();
    }

    private void beginStartupAutoConnect() {
        ScheduledExecutorService executor = poller;
        if (executor == null || executor.isShutdown()) {
            attemptStartupAutoConnect();
            return;
        }
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                attemptStartupAutoConnect();
            }
        }, STARTUP_AUTO_CONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void attemptStartupAutoConnect() {
        JMuleCore activeCore = core;
        if (!running || activeCore == null) {
            return;
        }

        try {
            ServerManager serverManager = activeCore.getServerManager();
            if (serverManager.getStatus() == ServerManager.Status.DISCONNECTED && !serverManager.isConnected()) {
                if (!connectLastKnownServer(serverManager) && serverManager.getServersCount() > 0) {
                    lastServerStatusDetail = "Trying available ED2K servers.";
                    serverManager.connect();
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            File nodesFile = new File(WireShareEd2kPaths.settingsFile("nodes.dat"));
            if (!activeCore.getJKadManager().isConnected()
                    && (activeCore.getJKadManager().getRoutingTable().getTotalContacts() > 0 || nodesFile.isFile())) {
                activeCore.getJKadManager().connect();
            }
        } catch (Throwable ignored) {
        }
    }

    private boolean connectLastKnownServer(ServerManager serverManager) throws Exception {
        String address = WireShareEd2kPaths.lastConnectedServerAddress();
        int port = WireShareEd2kPaths.lastConnectedServerPort();
        if (address == null || address.trim().isEmpty() || port <= 0) {
            return false;
        }
        Server server = serverManager.getServer(address.trim(), port);
        if (server == null) {
            server = serverManager.newServer(address.trim(), port);
        }
        lastServerStatusDetail = "Connecting to last ED2K server " + describeServer(server);
        serverManager.connect(server);
        return true;
    }

    private void updateSharedRoots() {
        JMuleCore activeCore = core;
        if (!running || activeCore == null) {
            return;
        }
        try {
            activeCore.getConfigurationManager().setSharedFolders(currentSharedRoots(canonicalFile(SharingSettings.getSaveDirectory())));
            fireStatusChanged();
        } catch (Throwable ignored) {
        }
    }

    private List<File> currentSharedRoots(File saveDir) {
        LinkedHashSet<File> roots = new LinkedHashSet<File>();
        roots.add(canonicalFile(SharingSettings.DEFAULT_SHARE_DIR));
        roots.add(saveDir);
        for (SharedFileList sharedFileList : snapshotSharedLists()) {
            if (!sharedFileList.isPublic()) {
                continue;
            }
            for (LocalFileItem item : snapshotSharedFiles(sharedFileList)) {
                File file = item.getFile();
                if (file == null) {
                    continue;
                }
                File root = file.isDirectory() ? file : file.getParentFile();
                if (root != null) {
                    roots.add(canonicalFile(root));
                }
            }
        }
        return new ArrayList<File>(roots);
    }

    @SuppressWarnings("unchecked")
    private List<SharedFileList> snapshotSharedLists() {
        return snapshotModel(sharedFileListManager);
    }

    @SuppressWarnings("unchecked")
    private List<LocalFileItem> snapshotSharedFiles(SharedFileList sharedFileList) {
        return snapshotModel(sharedFileList);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> snapshotModel(Object source) {
        try {
            Object model = source.getClass().getMethod("getModel").invoke(source);
            if (model instanceof Collection) {
                return new ArrayList<T>((Collection<T>) model);
            }
        } catch (Throwable ignored) {
        }
        return Collections.emptyList();
    }

    private void startPoller() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "wireShare-ed2k-sync");
                thread.setDaemon(true);
                return thread;
            }
        });
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                synchronizeTransfers();
            }
        }, SYNC_INTERVAL_MS, SYNC_INTERVAL_MS, TimeUnit.MILLISECONDS);
        poller = executor;
    }

    private void stopPoller() {
        ScheduledExecutorService executor = poller;
        poller = null;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void synchronizeTransfers() {
        if (core == null) {
            return;
        }
        boolean downloadsChanged = synchronizeDownloads();
        boolean uploadsChanged = synchronizeUploads();
        Ed2kStatus newStatus = buildStatus();
        boolean statusChanged = !statusEquals(lastStatus, newStatus);
        lastStatus = newStatus;
        if (downloadsChanged) {
            fireDownloadsChanged();
        }
        if (uploadsChanged) {
            fireUploadsChanged();
        }
        if (statusChanged) {
            fireStatusChanged();
        }
    }

    private boolean synchronizeDownloads() {
        Map<String, DownloadSession> sessions = new LinkedHashMap<String, DownloadSession>();
        for (DownloadSession session : core.getDownloadManager().getDownloads()) {
            sessions.put(session.getFileHash().toString(), session);
        }
        boolean changed = false;
        synchronized (downloadItems) {
            Set<String> existingKeys = new LinkedHashSet<String>(downloadItems.keySet());
            for (Map.Entry<String, DownloadSession> entry : sessions.entrySet()) {
                Ed2kDownloadItemAdapter item = downloadItems.get(entry.getKey());
                if (item == null) {
                    item = new Ed2kDownloadItemAdapter(entry.getValue(), core.getDownloadManager(), categoryManager);
                    downloadItems.put(entry.getKey(), item);
                    changed = true;
                } else if (item.refresh(entry.getValue())) {
                    changed = true;
                }
                existingKeys.remove(entry.getKey());
            }
            for (String removed : existingKeys) {
                downloadItems.remove(removed);
                changed = true;
            }
        }
        return changed;
    }

    private boolean synchronizeUploads() {
        Map<String, UploadSession> sessions = new LinkedHashMap<String, UploadSession>();
        for (UploadSession session : core.getUploadManager().getUploads()) {
            sessions.put(session.getFileHash().toString(), session);
        }
        boolean changed = false;
        synchronized (uploadItems) {
            Set<String> existingKeys = new LinkedHashSet<String>(uploadItems.keySet());
            for (Map.Entry<String, UploadSession> entry : sessions.entrySet()) {
                Ed2kUploadItemAdapter item = uploadItems.get(entry.getKey());
                if (item == null) {
                    item = new Ed2kUploadItemAdapter(entry.getValue(), categoryManager);
                    uploadItems.put(entry.getKey(), item);
                    changed = true;
                } else if (item.refresh(entry.getValue())) {
                    changed = true;
                }
                existingKeys.remove(entry.getKey());
            }
            for (String removed : existingKeys) {
                uploadItems.remove(removed);
                changed = true;
            }
        }
        return changed;
    }

    private Ed2kStatus buildStatus() {
        if (core == null) {
            return new Ed2kStatus(
                false,
                Ed2kStatus.ConnectionState.DISCONNECTED,
                Ed2kStatus.ConnectionState.DISCONNECTED,
                Ed2kStatus.KadBootstrapState.NOT_BOOTSTRAPPED,
                null,
                lastServerStatusDetail,
                0,
                0,
                0,
                0,
                0
            );
        }
        ServerManager serverManager = core.getServerManager();
        JKadManager kadManager = core.getJKadManager();
        SharingManager sharingManager = core.getSharingManager();
        Server connectedServer = serverManager.getConnectedServer();
        int kadContactCount = kadManager.getRoutingTable().getTotalContacts();
        return new Ed2kStatus(
            running,
            toServerState(serverManager.getStatus()),
            toKadState(kadManager),
            toKadBootstrapState(kadManager, kadContactCount),
            connectedServer == null ? null : connectedServer.getName(),
            connectedServer != null ? describeServer(connectedServer) : lastServerStatusDetail,
            serverManager.getServersCount(),
            core.getDownloadManager().getDownloadCount(),
            core.getUploadManager().getUploadCount(),
            sharingManager.getFileCount(),
            kadContactCount
        );
    }

    private void ensureRunning() throws JMuleCoreException {
        if (!running || core == null) {
            throw new JMuleCoreException("ED2K backend is not running.");
        }
    }

    private void ensureBackendStartedForSearch() {
        if (!running) {
            startBackend();
        }
    }

    private void ensureBackendStarted() throws JMuleCoreException {
        if (!running || core == null) {
            startBackend();
        }
        ensureRunning();
    }

    private void forEachSearchSession(SearchQuery query, SearchSessionAction action) {
        final String key = normalizeQuery(query.getQuery());
        List<Ed2kSearchSessionImpl> sessions = searchSessionsByQuery.get(key);
        if (sessions == null) {
            return;
        }
        for (Ed2kSearchSessionImpl session : sessions) {
            action.apply(session);
        }
    }

    private List<Ed2kSearchSessionImpl> sessionsForSearchResult(org.jmule.core.searchmanager.SearchResult searchResult) {
        if (searchResult == null || searchResult.getSearchResultItemList() == null) {
            return Collections.emptyList();
        }
        SearchQuery query = searchResult.getSearchQuery();
        if (query != null) {
            List<Ed2kSearchSessionImpl> sessions = searchSessionsByQuery.get(normalizeQuery(query.getQuery()));
            if (sessions != null && !sessions.isEmpty()) {
                return new ArrayList<Ed2kSearchSessionImpl>(sessions);
            }
        }
        List<Ed2kSearchSessionImpl> runningSessions = new ArrayList<Ed2kSearchSessionImpl>();
        for (Ed2kSearchSessionImpl session : searchSessionsById.values()) {
            if (session.isRunning()) {
                runningSessions.add(session);
            }
        }
        return runningSessions.size() == 1 ? runningSessions : Collections.<Ed2kSearchSessionImpl>emptyList();
    }

    private String normalizeQuery(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private void queueDownload(ED2KFileLink fileLink, File requestedSaveFile, boolean overwrite) throws Exception {
        ensureRunning();
        if (requestedSaveFile != null) {
            rememberPreferredSaveTarget(fileLink.getFileHash(), requestedSaveFile, fileLink.getFileName(), overwrite);
        }
        try {
            core.getDownloadManager().addDownload(fileLink);
        } catch (DownloadManagerException failure) {
            if (!isDuplicateDownloadFailure(failure)) {
                throw failure;
            }
            if (!resumeExistingDownloadIfStopped(fileLink.getFileHash())) {
                throw duplicateDownloadException(fileLink, requestedSaveFile, failure);
            }
            synchronizeTransfers();
            return;
        }
        core.getDownloadManager().startDownload(fileLink.getFileHash());
        synchronizeTransfers();
    }

    static boolean isDuplicateDownloadFailure(DownloadManagerException failure) {
        return hasFailureMessage(failure, "already exists");
    }

    static boolean isAlreadyStartedDownloadFailure(DownloadManagerException failure) {
        return hasFailureMessage(failure, "already started");
    }

    static DownloadException duplicateDownloadException(ED2KFileLink fileLink, File requestedSaveFile, DownloadManagerException failure) {
        File target = requestedSaveFile != null ? requestedSaveFile.getAbsoluteFile() : null;
        if (target == null) {
            target = WireShareEd2kPaths.completedFileTarget(
                normalizeFileHashValue(fileLink.getFileHash()),
                fileLink.getFileName()
            );
        }
        return new DownloadException(
            DownloadException.ErrorCode.FILE_ALREADY_DOWNLOADING,
            target,
            failure.getMessage()
        );
    }

    private boolean resumeExistingDownloadIfStopped(FileHash fileHash) throws DownloadManagerException {
        try {
            core.getDownloadManager().startDownload(fileHash);
            return true;
        } catch (DownloadManagerException failure) {
            if (isAlreadyStartedDownloadFailure(failure)) {
                return false;
            }
            throw failure;
        }
    }

    private static boolean hasFailureMessage(Throwable failure, String token) {
        return failure != null
            && failure.getMessage() != null
            && failure.getMessage().toLowerCase(Locale.US).contains(token);
    }

    private void rememberPreferredSaveTarget(FileHash fileHash, File requestedSaveFile, String fileName, boolean overwrite) throws DownloadException {
        File resolvedTarget = resolveRequestedSaveTarget(requestedSaveFile, fileName);
        if (resolvedTarget == null) {
            WireShareEd2kPaths.clearPreferredTarget(normalizeFileHash(fileHash));
            return;
        }
        File existingTarget = WireShareEd2kPaths.preferredTarget(normalizeFileHash(fileHash));
        boolean existingOverwrite = WireShareEd2kPaths.preferredTargetOverwrite(normalizeFileHash(fileHash));
        if (resolvedTarget.equals(existingTarget) && overwrite == existingOverwrite) {
            return;
        }
        resolvedTarget.getParentFile().mkdirs();
        if (resolvedTarget.exists() && !overwrite) {
            throw new DownloadException(
                DownloadException.ErrorCode.FILE_ALREADY_EXISTS,
                resolvedTarget,
                "An ED2K download already exists at the selected location."
            );
        }
        WireShareEd2kPaths.rememberPreferredTarget(normalizeFileHash(fileHash), resolvedTarget, overwrite);
    }

    private File resolveRequestedSaveTarget(File requestedSaveFile, String fileName) {
        if (requestedSaveFile == null) {
            return null;
        }
        File absolute = canonicalFile(requestedSaveFile);
        if (absolute.isDirectory() || requestedSaveFile.getName().indexOf('.') < 0 && !requestedSaveFile.exists()) {
            return canonicalFile(new File(absolute, fileName));
        }
        return absolute;
    }

    private String normalizeFileHash(FileHash fileHash) {
        return normalizeFileHashValue(fileHash);
    }

    private static String normalizeFileHashValue(FileHash fileHash) {
        return fileHash == null ? "" : fileHash.toString().toLowerCase(Locale.US);
    }

    private String safeServerAddress(Server server) {
        return server == null || server.getAddress() == null ? "" : server.getAddress();
    }

    private String safeServerName(Server server) {
        return server == null || server.getName() == null ? "" : server.getName();
    }

    private String safeServerDescription(Server server) {
        return server == null || server.getDesc() == null ? "" : server.getDesc();
    }

    private FileHash fileHashFor(DownloadItem item) throws ED2KLinkMalformedException {
        if (!(item instanceof Ed2kDownloadItem)) {
            return null;
        }
        return new ED2KFileLink(((Ed2kDownloadItem) item).getEd2kLink()).getFileHash();
    }

    private FileHash fileHashFor(UploadItem item) throws ED2KLinkMalformedException {
        if (!(item instanceof Ed2kUploadItem)) {
            return null;
        }
        return new ED2KFileLink(((Ed2kUploadItem) item).getEd2kLink()).getFileHash();
    }

    private File downloadLinkedResource(String address, String suffix) throws IOException {
        File tempFile = File.createTempFile("wireShare-ed2k-", suffix);
        tempFile.deleteOnExit();
        InputStream inputStream = null;
        try {
            if (address.startsWith("http://") || address.startsWith("https://") || address.startsWith("file:/")) {
                inputStream = new URL(address).openStream();
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(new File(address).toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return tempFile;
    }

    private boolean hasDownloadPeer(DownloadSession session, String address, int port) {
        for (Peer peer : session.getPeers()) {
            if (address.equals(peer.getIP()) && port == peer.getPort()) {
                return true;
            }
        }
        return false;
    }

    private File canonicalFile(File file) {
        try {
            return FileUtils.getCanonicalFile(file);
        } catch (java.io.IOException ignored) {
            return file.getAbsoluteFile();
        }
    }

    private int safeServerUsers(Server server) {
        try {
            return (int) server.getNumUsers();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private int safeServerFiles(Server server) {
        try {
            return (int) server.getNumFiles();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private Ed2kStatus.ConnectionState toServerState(ServerManager.Status status) {
        switch (status) {
        case CONNECTED:
            return Ed2kStatus.ConnectionState.CONNECTED;
        case CONNECTING:
            return Ed2kStatus.ConnectionState.CONNECTING;
        case DISCONNECTED:
        default:
            return Ed2kStatus.ConnectionState.DISCONNECTED;
        }
    }

    private Ed2kStatus.ConnectionState toKadState(JKadManager kadManager) {
        if (kadManager.isConnected()) {
            return Ed2kStatus.ConnectionState.CONNECTED;
        }
        if (kadManager.isConnecting()) {
            return Ed2kStatus.ConnectionState.CONNECTING;
        }
        return Ed2kStatus.ConnectionState.DISCONNECTED;
    }

    private Ed2kStatus.KadBootstrapState toKadBootstrapState(JKadManager kadManager, int kadContactCount) {
        if (kadManager.isConnected()) {
            return Ed2kStatus.KadBootstrapState.BOOTSTRAPPED;
        }
        if (kadManager.isConnecting() || kadContactCount > 0) {
            return Ed2kStatus.KadBootstrapState.BOOTSTRAPPING;
        }
        return Ed2kStatus.KadBootstrapState.NOT_BOOTSTRAPPED;
    }

    private boolean statusEquals(Ed2kStatus left, Ed2kStatus right) {
        return left.isRunning() == right.isRunning()
            && left.getServerState() == right.getServerState()
            && left.getKadState() == right.getKadState()
            && left.getKadBootstrapState() == right.getKadBootstrapState()
            && Objects.equals(left.getConnectedServerName(), right.getConnectedServerName())
            && Objects.equals(left.getServerStatusDetail(), right.getServerStatusDetail())
            && left.getServerCount() == right.getServerCount()
            && left.getDownloadCount() == right.getDownloadCount()
            && left.getUploadCount() == right.getUploadCount()
            && left.getSharedFileCount() == right.getSharedFileCount()
            && left.getKadContactCount() == right.getKadContactCount();
    }

    private String describeServer(Server server) {
        if (server == null) {
            return "ED2K server";
        }
        String name = server.getName();
        if (name != null && !name.trim().isEmpty() && !name.equals(server.getAddress())) {
            return name + " (" + server.getAddress() + ":" + server.getPort() + ")";
        }
        return server.getAddress() + ":" + server.getPort();
    }

    private void fireDownloadsChanged() {
        for (Ed2kListener listener : listeners) {
            listener.downloadsChanged();
        }
    }

    private void fireUploadsChanged() {
        for (Ed2kListener listener : listeners) {
            listener.uploadsChanged();
        }
    }

    private void fireStatusChanged() {
        for (Ed2kListener listener : listeners) {
            listener.statusChanged();
        }
    }

    private void fireSearchChanged(String sessionId) {
        for (Ed2kListener listener : listeners) {
            listener.searchChanged(sessionId);
        }
    }

    private interface SearchSessionAction {
        void apply(Ed2kSearchSessionImpl session);
    }

    private static final class Ed2kSearchSessionImpl implements Ed2kSearchSession {
        private final String id;
        private final String query;
        private final CategoryManager categoryManager;
        private final List<GroupedSearchResult> results = new ArrayList<GroupedSearchResult>();
        private final Map<String, Ed2kGroupedSearchResultAdapter> resultsByUrn = new LinkedHashMap<String, Ed2kGroupedSearchResultAdapter>();
        private boolean running;
        private boolean failed;
        private String failureMessage;
        private long lastSubmittedAt;

        private Ed2kSearchSessionImpl(String id, String query, CategoryManager categoryManager) {
            this.id = id;
            this.query = query;
            this.categoryManager = categoryManager;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getQuery() {
            return query;
        }

        @Override
        public synchronized boolean isRunning() {
            return running;
        }

        @Override
        public synchronized boolean isFailed() {
            return failed;
        }

        @Override
        public synchronized String getFailureMessage() {
            return failureMessage;
        }

        @Override
        public synchronized List<GroupedSearchResult> getResults() {
            return new ArrayList<GroupedSearchResult>(results);
        }

        private synchronized void setRunning(boolean running) {
            this.running = running;
        }

        private synchronized void setFailed(boolean failed, String message) {
            this.failed = failed;
            this.failureMessage = message;
        }

        private synchronized void markSubmitted() {
            lastSubmittedAt = System.currentTimeMillis();
        }

        private synchronized boolean canSubmitAgain(long now, long minimumDelayMs) {
            return lastSubmittedAt <= 0L || now - lastSubmittedAt >= minimumDelayMs;
        }

        private synchronized boolean shouldRetryWhenNetworkReady() {
            return (running || failed) && results.isEmpty();
        }

        private synchronized void clear() {
            results.clear();
            resultsByUrn.clear();
        }

        private synchronized boolean addResult(SearchResultItem item) {
            Ed2kSearchResultAdapter searchResult = Ed2kSearchResultAdapter.create(item, categoryManager);
            if (searchResult == null) {
                return false;
            }
            String key = searchResult.getUrn().toString();
            Ed2kGroupedSearchResultAdapter grouped = resultsByUrn.get(key);
            if (grouped == null) {
                grouped = new Ed2kGroupedSearchResultAdapter(searchResult);
                resultsByUrn.put(key, grouped);
                results.add(grouped);
                return true;
            }
            return grouped.addResult(searchResult);
        }
    }

    private static final class Ed2kSyntheticUrn implements URN {
        private final String value;

        private Ed2kSyntheticUrn(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(URN other) {
            return value.compareTo(other.toString());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof URN && value.equals(obj.toString());
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final class Ed2kRemoteHost implements RemoteHost {
        @Override public boolean isBrowseHostEnabled() { return false; }
        @Override public boolean isChatEnabled() { return false; }
        @Override public boolean isSharingEnabled() { return false; }
        @Override public FriendPresence getFriendPresence() { return null; }
    }

    private static final class EmptyDownloadPiecesInfo implements DownloadPiecesInfo {
        private final int pieceCount;
        private final long pieceSize;

        private EmptyDownloadPiecesInfo(int pieceCount, long pieceSize) {
            this.pieceCount = pieceCount;
            this.pieceSize = pieceSize;
        }

        @Override public PieceState getPieceState(int piece) { return PieceState.UNAVAILABLE; }
        @Override public int getNumPieces() { return pieceCount; }
        @Override public long getPieceSize() { return pieceSize; }
        @Override public int getNumPiecesCompleted() { return 0; }
    }

    private static final class Ed2kSearchResultAdapter implements Ed2kSearchResultView {
        private final String ed2kLink;
        private final String fileName;
        private final long size;
        private final Category category;
        private final URN urn;
        private final RemoteHost remoteHost;
        private final Map<FilePropertyKey, Object> properties = new LinkedHashMap<FilePropertyKey, Object>();

        private static Ed2kSearchResultAdapter create(SearchResultItem item, CategoryManager categoryManager) {
            if (item == null || item.getFileHash() == null) {
                return null;
            }
            return new Ed2kSearchResultAdapter(item, categoryManager);
        }

        private Ed2kSearchResultAdapter(SearchResultItem item, CategoryManager categoryManager) {
            FileHash hash = item.getFileHash();
            this.fileName = safeSearchFileName(item, hash);
            this.size = safeSearchFileSize(item);
            this.category = categoryManager.getCategoryForFilename(fileName);
            this.urn = new Ed2kSyntheticUrn("urn:ed2k:" + hash.toString().toLowerCase(Locale.US));
            this.ed2kLink = new ED2KFileLink(fileName, size, hash).getAsString();
            this.remoteHost = new Ed2kRemoteHost();
            properties.put(FilePropertyKey.NAME, fileName);
            properties.put(FilePropertyKey.FILE_SIZE, Long.valueOf(size));
            properties.put(FilePropertyKey.DESCRIPTION, "ED2K/Kad");
        }

        private static String safeSearchFileName(SearchResultItem item, FileHash hash) {
            String name = item.getFileName();
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
            return "ed2k-" + hash.toString().toLowerCase(Locale.US);
        }

        private static long safeSearchFileSize(SearchResultItem item) {
            try {
                return Math.max(0L, item.getFileSize());
            } catch (Throwable ignored) {
                return 0L;
            }
        }

        @Override public String getEd2kLink() { return ed2kLink; }
        @Override public String getFileExtension() { return extensionOf(fileName); }
        @Override public Object getProperty(FilePropertyKey key) { return properties.get(key); }
        @Override public Category getCategory() { return category; }
        @Override public long getSize() { return size; }
        @Override public RemoteHost getSource() { return remoteHost; }
        @Override public URN getUrn() { return urn; }
        @Override public boolean isSpam() { return false; }
        @Override public String getFileName() { return fileName; }
        @Override public String getFileNameWithoutExtension() { return FileUtils.getFilenameNoExtension(fileName); }
        @Override public String getMagnetURL() { return ed2kLink; }
        @Override public float getRelevance(String query) { return size <= 0 ? 0.1f : 1.0f; }
        @Override public boolean isLicensed() { return false; }
    }

    private static final class Ed2kGroupedSearchResultAdapter implements Ed2kGroupedSearchResultView {
        private final URN urn;
        private final String fileName;
        private final List<SearchResult> searchResults = new ArrayList<SearchResult>();
        private final Set<RemoteHost> sources = new LinkedHashSet<RemoteHost>();
        private final String ed2kLink;

        private Ed2kGroupedSearchResultAdapter(Ed2kSearchResultAdapter result) {
            this.urn = result.getUrn();
            this.fileName = result.getFileName();
            this.ed2kLink = result.getEd2kLink();
            addResult(result);
        }

        private boolean addResult(Ed2kSearchResultAdapter result) {
            int beforeCount = sources.size();
            searchResults.add(result);
            sources.add(result.getSource());
            return searchResults.size() == 1 || sources.size() != beforeCount;
        }

        @Override public String getEd2kLink() { return ed2kLink; }
        @Override public boolean isAnonymous() { return true; }
        @Override public String getFileName() { return fileName; }
        @Override public Collection<Friend> getFriends() { return Collections.emptyList(); }
        @Override public float getRelevance() { return Math.max(1.0f, searchResults.size()); }
        @Override public List<SearchResult> getSearchResults() { return Collections.unmodifiableList(searchResults); }
        @Override public Collection<RemoteHost> getSources() { return Collections.unmodifiableCollection(sources); }
        @Override public URN getUrn() { return urn; }
    }

    private final class Ed2kDownloadItemAdapter extends AbstractEd2kTransferItem implements Ed2kDownloadItem {
        private final DownloadManager downloadManager;
        private final CategoryManager categoryManager;
        private final Date startDate = new Date();
        private DownloadSession session;
        private String ed2kLink;
        private String fileName;
        private Category category;
        private File saveFile;
        private File downloadingFile;
        private URN urn;
        private DownloadState state;
        private int percentComplete;
        private long currentSize;
        private long totalSize;
        private long remainingTime;
        private float speed;
        private int sourceCount;

        private Ed2kDownloadItemAdapter(DownloadSession session, DownloadManager downloadManager, CategoryManager categoryManager) {
            this.downloadManager = downloadManager;
            this.categoryManager = categoryManager;
            refresh(session);
        }

        private boolean refresh(DownloadSession newSession) {
            this.session = newSession;
            final String newFileName = newSession.getSharingName();
            final Category newCategory = categoryManager.getCategoryForFilename(newFileName);
            final File newSaveFile = effectiveSaveFile(newSession);
            final File newDownloadingFile = safeDownloadingFile(newSession);
            final URN newUrn = new Ed2kSyntheticUrn("urn:ed2k:" + newSession.getFileHash().toString().toLowerCase(Locale.US));
            final DownloadState newState = mapDownloadState(newSession);
            final int newPercent = clampPercent((int) Math.round(newSession.getPercentCompleted()));
            final long newCurrentSize = newSession.getTransferredBytes();
            final long newTotalSize = newSession.getFileSize();
            final long newRemainingTime = normalizeEta(newSession.getETA());
            final float newSpeed = newSession.getSpeed();
            final int newSourceCount = newSession.getPeerCount();
            final String newEd2kLink = newSession.getED2KLink().getAsString();

            boolean changed = !Objects.equals(fileName, newFileName)
                || category != newCategory
                || !Objects.equals(saveFile, newSaveFile)
                || !Objects.equals(downloadingFile, newDownloadingFile)
                || !Objects.equals(urn, newUrn)
                || state != newState
                || percentComplete != newPercent
                || currentSize != newCurrentSize
                || totalSize != newTotalSize
                || remainingTime != newRemainingTime
                || Float.compare(speed, newSpeed) != 0
                || sourceCount != newSourceCount
                || !Objects.equals(ed2kLink, newEd2kLink);

            fileName = newFileName;
            category = newCategory;
            saveFile = newSaveFile;
            downloadingFile = newDownloadingFile;
            urn = newUrn;
            state = newState;
            percentComplete = newPercent;
            currentSize = newCurrentSize;
            totalSize = newTotalSize;
            remainingTime = newRemainingTime;
            speed = newSpeed;
            sourceCount = newSourceCount;
            ed2kLink = newEd2kLink;

            if (changed) {
                fireChange();
            }
            return changed;
        }

        @Override public String getEd2kLink() { return ed2kLink; }
        @Override public DownloadItemType getDownloadItemType() { return DownloadItemType.GNUTELLA; }
        @Override public DownloadState getState() { return state; }
        @Override public String getTitle() { return FileUtils.getFilenameNoExtension(fileName); }
        @Override public int getPercentComplete() { return percentComplete; }
        @Override public long getCurrentSize() { return currentSize; }
        @Override public long getAmountVerified() { return currentSize; }
        @Override public long getTotalSize() { return totalSize; }
        @Override public long getAmountLost() { return 0; }
        @Override public long getRemainingDownloadTime() { return remainingTime; }
        @Override public long getRemainingTimeInState() { return remainingTime; }
        @Override public void cancel() { safeCancel(); }
        @Override public void pause() { safeStop(); }
        @Override public void resume() { safeResume(); }
        @Override public int getDownloadSourceCount() { return sourceCount; }
        @Override public List<Address> getSources() { return Collections.emptyList(); }
        @Override public List<SourceInfo> getSourcesDetails() { return Collections.emptyList(); }
        @Override public DownloadPiecesInfo getPiecesInfo() { return new EmptyDownloadPiecesInfo((int) session.getPartCount(), 0L); }
        @Override public Category getCategory() { return category; }
        @Override public float getDownloadSpeed() { return speed; }
        @Override public Collection<org.limewire.core.api.endpoint.RemoteHost> getRemoteHosts() { return Collections.emptyList(); }
        @Override public int getRemoteQueuePosition() { return -1; }
        @Override public ErrorState getErrorState() { return ErrorState.NONE; }
        @Override public int getLocalQueuePriority() { return 0; }
        @Override public boolean isLaunchable() { return getLaunchableFile() != null; }
        @Override public File getDownloadingFile() { return downloadingFile; }
        @Override public File getLaunchableFile() { return state == DownloadState.DONE ? saveFile : null; }
        @Override public void setSaveFile(File saveFile, boolean overwrite) throws DownloadException {
            if (state == DownloadState.DONE) {
                throw new DownloadException(
                    DownloadException.ErrorCode.FILESYSTEM_ERROR,
                    saveFile,
                    "Completed ED2K downloads cannot be moved here after finishing."
                );
            }
            rememberPreferredSaveTarget(session.getFileHash(), saveFile, session.getSharingName(), overwrite);
            this.saveFile = effectiveSaveFile(session);
            fireChange();
        }
        @Override public File getSaveFile() { return saveFile; }
        @Override public boolean isTryAgainEnabled() { return true; }
        @Override public Date getStartDate() { return startDate; }
        @Override public boolean isRelocatable() { return state != DownloadState.DONE; }
        @Override public Collection<File> getCompleteFiles() { return saveFile == null ? Collections.<File>emptyList() : Collections.singletonList(saveFile); }
        @Override public Object getDownloadProperty(DownloadPropertyKey key) { return null; }
        @Override public String getFileName() { return fileName; }
        @Override public Object getProperty(FilePropertyKey key) {
            switch (key) {
            case NAME: return fileName;
            case FILE_SIZE: return Long.valueOf(totalSize);
            case LOCATION: return saveFile == null ? null : saveFile.getAbsolutePath();
            case DESCRIPTION: return "ED2K/Kad";
            default: return null;
            }
        }
        @Override public String getPropertyString(FilePropertyKey key) {
            Object value = getProperty(key);
            return value == null ? null : value.toString();
        }
        @Override public URN getUrn() { return urn; }

        private void safeCancel() {
            try {
                WireShareEd2kPaths.clearPreferredTarget(normalizeFileHash(session.getFileHash()));
                downloadManager.cancelDownload(session.getFileHash());
            } catch (Throwable ignored) {
            }
        }

        private void safeStop() {
            try {
                downloadManager.stopDownload(session.getFileHash());
            } catch (Throwable ignored) {
            }
        }

        private void safeResume() {
            try {
                downloadManager.startDownload(session.getFileHash());
            } catch (Throwable ignored) {
            }
        }
    }

    private static final class Ed2kUploadItemAdapter extends AbstractEd2kTransferItem implements Ed2kUploadItem {
        private UploadSession session;
        private String fileName;
        private Category category;
        private File file;
        private URN urn;
        private float speed;
        private long remainingTime;
        private long uploaded;
        private int peerCount;
        private String ed2kLink;

        private Ed2kUploadItemAdapter(UploadSession session, CategoryManager categoryManager) {
            refresh(session, categoryManager);
        }

        private boolean refresh(UploadSession newSession) {
            return refresh(newSession, null);
        }

        private boolean refresh(UploadSession newSession, CategoryManager categoryManager) {
            this.session = newSession;
            final String newFileName = newSession.getSharingName();
            final Category newCategory = categoryManager == null ? category : categoryManager.getCategoryForFilename(newFileName);
            final File newFile = newSession.getSharedFile() == null ? null : newSession.getSharedFile().getFile();
            final URN newUrn = new Ed2kSyntheticUrn("urn:ed2k:" + newSession.getFileHash().toString().toLowerCase(Locale.US));
            final float newSpeed = newSession.getSpeed();
            final long newRemaining = normalizeEta(newSession.getETA());
            final long newUploaded = newSession.getTransferredBytes();
            final int newPeerCount = newSession.getPeerCount();
            final String newEd2kLink = newSession.getED2KLink().getAsString();
            boolean changed = !Objects.equals(fileName, newFileName)
                || category != newCategory
                || !Objects.equals(file, newFile)
                || !Objects.equals(urn, newUrn)
                || Float.compare(speed, newSpeed) != 0
                || remainingTime != newRemaining
                || uploaded != newUploaded
                || peerCount != newPeerCount
                || !Objects.equals(ed2kLink, newEd2kLink);
            fileName = newFileName;
            if (newCategory != null) {
                category = newCategory;
            }
            file = newFile;
            urn = newUrn;
            speed = newSpeed;
            remainingTime = newRemaining;
            uploaded = newUploaded;
            peerCount = newPeerCount;
            ed2kLink = newEd2kLink;
            if (changed) {
                fireChange();
            }
            return changed;
        }

        @Override public String getEd2kLink() { return ed2kLink; }
        @Override public void cancel() { }
        @Override public UploadState getState() { return peerCount > 0 ? UploadState.UPLOADING : UploadState.QUEUED; }
        @Override public long getTotalAmountUploaded() { return uploaded; }
        @Override public String getFileName() { return fileName; }
        @Override public long getFileSize() { return session.getFileSize(); }
        @Override public Category getCategory() { return category; }
        @Override public RemoteHost getRemoteHost() { return new Ed2kRemoteHost(); }
        @Override public String getRenderName() { return "ED2K/Kad"; }
        @Override public int getQueuePosition() { return -1; }
        @Override public long getRemainingUploadTime() { return remainingTime; }
        @Override public float getUploadSpeed() { return speed; }
        @Override public Collection<File> getCompleteFiles() { return file == null ? Collections.<File>emptyList() : Collections.singletonList(file); }
        @Override public File getFile() { return file; }
        @Override public UploadItemType getUploadItemType() { return UploadItemType.GNUTELLA; }
        @Override public int getNumUploadConnections() { return peerCount; }
        @Override public BrowseType getBrowseType() { return BrowseType.NONE; }
        @Override public long getStartTime() { return 0L; }
        @Override public float getSeedRatio() { return -1f; }
        @Override public boolean isFinished() { return false; }
        @Override public boolean isStarted() { return peerCount > 0; }
        @Override public void pause() { }
        @Override public void resume() { }
        @Override public List<SourceInfo> getTransferDetails() { return Collections.emptyList(); }
        @Override public String getPropertyString(FilePropertyKey filePropertyKey) {
            Object value = getProperty(filePropertyKey);
            return value == null ? null : value.toString();
        }
        @Override public Object getProperty(FilePropertyKey key) {
            switch (key) {
            case NAME: return fileName;
            case FILE_SIZE: return Long.valueOf(getFileSize());
            case LOCATION: return file == null ? null : file.getAbsolutePath();
            case DESCRIPTION: return "ED2K/Kad";
            default: return null;
            }
        }
        @Override public URN getUrn() { return urn; }
    }

    private abstract static class AbstractEd2kTransferItem implements PropertiableFile {
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        protected void fireChange() {
            propertyChangeSupport.firePropertyChange("state", null, null);
        }
    }

    private static DownloadState mapDownloadState(DownloadSession session) {
        if (session.getPercentCompleted() >= 100d) {
            return DownloadState.DONE;
        }
        if (session.isStarted()) {
            return session.getPeerCount() > 0 ? DownloadState.DOWNLOADING : DownloadState.CONNECTING;
        }
        return session.getTransferredBytes() > 0 ? DownloadState.PAUSED : DownloadState.STALLED;
    }

    private static File safeCompletedFile(DownloadSession session) {
        try {
            if (session.getSharedFile() instanceof CompletedFile) {
                return session.getSharedFile().getFile();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private File effectiveSaveFile(DownloadSession session) {
        File completedFile = safeCompletedFile(session);
        if (completedFile != null) {
            return completedFile;
        }
        return WireShareEd2kPaths.completedFileTarget(normalizeFileHash(session.getFileHash()), session.getSharingName());
    }

    private static File safeDownloadingFile(DownloadSession session) {
        try {
            return session.getSharedFile() == null ? null : session.getSharedFile().getFile();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static long normalizeEta(long eta) {
        return eta < 0 || eta >= JMULE_INFINITY_ETA_SECONDS ? DownloadItem.UNKNOWN_TIME : eta;
    }

    private static String extensionOf(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot < 0 || lastDot == fileName.length() - 1 ? "" : fileName.substring(lastDot + 1);
    }
}
