package org.limewire.libtorrent;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.limewire.bittorrent.ProxySettingType;
import org.limewire.bittorrent.TorrentException;
import org.limewire.bittorrent.TorrentFileEntry;
import org.limewire.bittorrent.TorrentInfo;
import org.limewire.bittorrent.TorrentIpPort;
import org.limewire.bittorrent.TorrentManagerSettings;
import org.limewire.bittorrent.TorrentPiecesInfo;
import org.limewire.bittorrent.TorrentStatus;
import org.limewire.bittorrent.TorrentTracker;
import org.limewire.inject.LazySingleton;
import org.limewire.libtorrent.callback.AlertCallback;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.util.ExceptionUtils;

import com.frostwire.jlibtorrent.AddTorrentParams;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.AnnounceEntry;
import com.frostwire.jlibtorrent.ErrorCode;
import com.frostwire.jlibtorrent.PartialPieceInfo;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionHandle;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.Sha1Hash;
import com.frostwire.jlibtorrent.TcpEndpoint;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.Vectors;
import com.frostwire.jlibtorrent.WebSeedEntry;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert;
import com.frostwire.jlibtorrent.alerts.ScrapeFailedAlert;
import com.frostwire.jlibtorrent.alerts.ScrapeReplyAlert;
import com.frostwire.jlibtorrent.alerts.StateUpdateAlert;
import com.frostwire.jlibtorrent.swig.add_torrent_params;
import com.frostwire.jlibtorrent.swig.error_code;
import com.frostwire.jlibtorrent.swig.peer_info;
import com.frostwire.jlibtorrent.swig.peer_info_vector;
import com.frostwire.jlibtorrent.swig.resume_data_flags_t;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.frostwire.jlibtorrent.swig.torrent_flags_t;
import com.sun.jna.WString;

/**
 * Compatibility adapter that preserves the old wrapper surface while delegating
 * torrent work to jlibtorrent.
 */
@LazySingleton
class LibTorrentWrapper {

    private static final Log LOG = LogFactory.getLog(LibTorrentWrapper.class);

    private static final int SCRAPE_STATUS_SUCCESS = 0;
    private static final int SCRAPE_STATUS_TIMEOUT = 1;
    private static final int SCRAPE_STATUS_ERROR = 2;
    private static final long RESUME_SAVE_TIMEOUT_MILLIS = 10_000L;
    private static final long SCRAPE_TIMEOUT_MILLIS = 3_000L;

    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final Queue<LibTorrentAlert> pendingAlerts = new ConcurrentLinkedQueue<LibTorrentAlert>();
    private final ConcurrentMap<LibTorrentAlert, byte[]> pendingResumeData =
            new ConcurrentHashMap<LibTorrentAlert, byte[]>();

    private volatile SessionManager sessionManager;
    private volatile TorrentManagerSettings torrentSettings;
    private volatile LibTorrentProxySetting peerProxy = LibTorrentProxySetting.nullProxy();
    private volatile LibTorrentProxySetting trackerProxy = LibTorrentProxySetting.nullProxy();
    private volatile LibTorrentProxySetting webSeedProxy = LibTorrentProxySetting.nullProxy();
    private volatile LibTorrentProxySetting dhtProxy = LibTorrentProxySetting.nullProxy();
    private volatile boolean dhtEnabled;
    private volatile boolean upnpEnabled;
    private volatile boolean natpmpEnabled;
    private volatile boolean lsdEnabled;

    private final AlertListener sessionAlertListener = new AlertListener() {
        @Override
        public int[] types() {
            return null;
        }

        @Override
        public void alert(Alert<?> alert) {
            try {
                if (alert instanceof StateUpdateAlert) {
                    for (com.frostwire.jlibtorrent.TorrentStatus status : ((StateUpdateAlert) alert)
                            .status()) {
                        queueStatusAlert(status.infoHash().toHex(), "status updated");
                    }
                } else if (alert instanceof MetadataReceivedAlert) {
                    TorrentHandle handle = ((MetadataReceivedAlert) alert).handle();
                    queueStatusAlert(toSha1(handle), "metadata successfully received");
                } else if (alert instanceof SaveResumeDataAlert) {
                    queueResumeAlert((SaveResumeDataAlert) alert);
                }
            } catch (RuntimeException e) {
                LOG.warn("Unable to translate jlibtorrent alert", e);
            }
        }
    };

    /**
     * Initializes the torrent session, restoring prior session state if a
     * state file exists.
     */
    void initialize(TorrentManagerSettings torrentSettings) {
        this.torrentSettings = torrentSettings;

        try {
            dhtEnabled = false;
            upnpEnabled = false;
            natpmpEnabled = false;
            lsdEnabled = false;

            SettingsPack settingsPack = buildSettingsPack(torrentSettings);
            SessionParams sessionParams = buildSessionParams(torrentSettings, settingsPack);

            SessionManager manager = new SessionManager(LOG.isDebugEnabled());
            manager.addListener(sessionAlertListener);
            manager.start(sessionParams);

            sessionManager = manager;
            loaded.set(true);
        } catch (Throwable e) {
            LOG.error("Failure loading jlibtorrent.", e);
            sessionManager = null;
            loaded.set(false);
            if (torrentSettings.isReportingLibraryLoadFailture()) {
                ExceptionUtils.reportOrReturn(e);
            }
        }
    }

    /**
     * Returns true if the jlibtorrent session was loaded successfully.
     */
    public boolean isLoaded() {
        return loaded.get() && sessionManager != null;
    }

    public void add_torrent(String sha1, String trackerURI, String torrentPath, String savePath,
            String fastResumePath) {
        SessionManager manager = requireLoadedSession();
        AddTorrentParams params = buildAddTorrentParams(sha1, trackerURI, torrentPath, savePath,
                fastResumePath);

        ErrorCode ec = new ErrorCode(new error_code());
        TorrentHandle handle = new SessionHandle(manager.swig()).addTorrent(params, ec);
        if (ec.isError()) {
            throw new TorrentException(ec.message(), ec.value());
        }
        if (handle == null || !handle.isValid()) {
            throw new TorrentException("Unable to add torrent " + sha1, TorrentException.LOAD_EXCEPTION);
        }
    }

    public void freeze_and_save_all_fast_resume_data(final AlertCallback alertCallback) {
        SessionManager manager = sessionManager;
        if (manager == null) {
            return;
        }

        final List<TorrentHandle> handles = validHandles();
        final List<String> sha1s = new ArrayList<String>(handles.size());
        for (TorrentHandle handle : handles) {
            com.frostwire.jlibtorrent.TorrentStatus status = handle.status(true);
            if (status != null && status.hasMetadata()) {
                sha1s.add(toSha1(handle));
            }
        }

        if (sha1s.isEmpty()) {
            return;
        }

        final CountDownLatch latch = new CountDownLatch(sha1s.size());
        AlertListener listener = new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                if (!(alert instanceof SaveResumeDataAlert)) {
                    return;
                }

                SaveResumeDataAlert saveAlert = (SaveResumeDataAlert) alert;
                String sha1 = toSha1(saveAlert.handle());
                if (!sha1s.contains(sha1)) {
                    return;
                }

                byte[] resumeData = buildResumeDataBytes(saveAlert);
                if (resumeData != null) {
                    LibTorrentAlert synthetic = createAlert(sha1, LibTorrentAlert.storage_notification,
                            "resume data saved", resumeData);
                    alertCallback.callback(synthetic);
                }
                latch.countDown();
            }
        };

        manager.addListener(listener);
        try {
            resume_data_flags_t resumeFlags = TorrentHandle.FLUSH_DISK_CACHE
                    .or_(TorrentHandle.SAVE_INFO_DICT);
            for (TorrentHandle handle : handles) {
                com.frostwire.jlibtorrent.TorrentStatus status = handle.status(true);
                if (status != null && status.hasMetadata()) {
                    handle.pause();
                    handle.saveResumeData(resumeFlags);
                }
            }
            latch.await(RESUME_SAVE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            manager.removeListener(listener);
        }
    }

    public void get_alerts(AlertCallback alertCallback) {
        LibTorrentAlert alert;
        while ((alert = pendingAlerts.poll()) != null) {
            alertCallback.callback(alert);
        }
    }

    public void set_ip_filter(IpFilterCallback ipFilterCallback) {
        LOG.debug("Torrent IP filtering is no longer backed by the legacy native callback bridge.");
    }

    public void pause_torrent(String id) {
        TorrentHandle handle = requireHandle(id);
        handle.pause();
    }

    public void resume_torrent(String id) {
        TorrentHandle handle = requireHandle(id);
        handle.resume();
    }

    public void scrape_tracker(String id) {
        TorrentHandle handle = requireHandle(id);
        handle.scrapeTracker();
    }

    public void force_reannounce(String id) {
        TorrentHandle handle = requireHandle(id);
        handle.forceReannounce();
    }

    public void get_torrent_status(String id, TorrentStatus status) {
        if (!(status instanceof LibTorrentStatus)) {
            throw new IllegalArgumentException("Expected LibTorrentStatus.");
        }

        LibTorrentStatus target = (LibTorrentStatus) status;
        TorrentHandle handle = findHandle(id);
        if (handle == null || !handle.isValid()) {
            target.valid = 0;
            target.error = "torrent not found";
            return;
        }

        populateStatus(handle, target);
    }

    public void remove_torrent(String id) {
        SessionManager manager = requireLoadedSession();
        TorrentHandle handle = requireHandle(id);
        manager.remove(handle);
    }

    public LibTorrentPeer[] get_peers(String id) {
        TorrentHandle handle = requireHandle(id);
        peer_info_vector peerInfoVector = new peer_info_vector();
        handle.swig().get_peer_info(peerInfoVector);

        int size = (int) peerInfoVector.size();
        LibTorrentPeer[] peers = new LibTorrentPeer[size];
        for (int i = 0; i < size; i++) {
            peers[i] = toLegacyPeer(peerInfoVector.get(i));
        }
        return peers;
    }

    public void signal_fast_resume_data_request(String id) {
        TorrentHandle handle = requireHandle(id);
        if (handle.torrentFile() != null) {
            handle.saveResumeData(TorrentHandle.SAVE_INFO_DICT);
        }
    }

    public void clear_error_and_retry(String id) {
        TorrentHandle handle = requireHandle(id);
        handle.resume();
    }

    public void move_torrent(String id, String absolutePath) {
        TorrentHandle handle = requireHandle(id);
        handle.moveStorage(absolutePath);
    }

    public void abort_torrents() {
        SessionManager manager = sessionManager;
        sessionManager = null;
        loaded.set(false);
        pendingAlerts.clear();
        pendingResumeData.clear();
        if (manager != null) {
            manager.removeListener(sessionAlertListener);
            manager.stop();
        }
    }

    public void free_torrent_status(LibTorrentStatus status) {
        // No-op. jlibtorrent objects are JVM managed.
    }

    public void update_settings(TorrentManagerSettings torrentSettings) {
        this.torrentSettings = torrentSettings;
        applyRuntimeSettings();
    }

    public void start_dht() {
        start_dht(null);
    }

    public void start_dht(File dhtStateFile) {
        dhtEnabled = true;
        SessionManager manager = requireLoadedSession();
        manager.startDht();

        SessionHandle sessionHandle = new SessionHandle(manager.swig());
        if (dhtStateFile != null && dhtStateFile.exists()) {
            LOG.debugf("Using saved session state from {0} on next initialization only.", dhtStateFile);
        }
        for (TorrentIpPort ipPort : torrentSettings.getBootStrapDHTRouters()) {
            sessionHandle.addDhtNode(new com.frostwire.jlibtorrent.Pair<String, Integer>(
                    ipPort.getAddress(), ipPort.getPort()));
        }
    }

    public void stop_dht() {
        dhtEnabled = false;
        SessionManager manager = requireLoadedSession();
        manager.stopDht();
    }

    public void save_dht_state(File dhtStateFile) {
        SessionManager manager = requireLoadedSession();
        if (dhtStateFile != null) {
            try {
                java.nio.file.Files.createDirectories(dhtStateFile.toPath().getParent());
                byte[] sessionState = manager.saveState();
                if (sessionState != null) {
                    java.nio.file.Files.write(dhtStateFile.toPath(), sessionState);
                }
            } catch (IOException e) {
                throw new TorrentException("Unable to save DHT state: " + e.getMessage(),
                        TorrentException.LOAD_EXCEPTION);
            }
        }
    }

    public void add_dht_router(String address, int port) {
        SessionHandle handle = new SessionHandle(requireLoadedSession().swig());
        handle.addDhtNode(new com.frostwire.jlibtorrent.Pair<String, Integer>(address, port));
    }

    public void add_dht_node(String address, int port) {
        add_dht_router(address, port);
    }

    public void start_upnp() {
        upnpEnabled = true;
        applyRuntimeSettings();
        reopenMappedPorts();
    }

    public void stop_upnp() {
        upnpEnabled = false;
        applyRuntimeSettings();
    }

    public void start_lsd() {
        lsdEnabled = true;
        applyRuntimeSettings();
    }

    public void stop_lsd() {
        lsdEnabled = false;
        applyRuntimeSettings();
    }

    public void start_natpmp() {
        natpmpEnabled = true;
        applyRuntimeSettings();
        reopenMappedPorts();
    }

    public void stop_natpmp() {
        natpmpEnabled = false;
        applyRuntimeSettings();
    }

    public void set_seed_ratio(String id, float seed_ratio) {
        LOG.debugf("Per-torrent seed ratio limits are no-op under the jlibtorrent adapter: {0}",
                id);
    }

    public void set_upload_limit(String id, int limit) {
        TorrentHandle handle = requireHandle(id);
        handle.setUploadLimit(limit);
    }

    public int get_upload_limit(String id) {
        TorrentHandle handle = requireHandle(id);
        return handle.getUploadLimit();
    }

    public void set_download_limit(String id, int limit) {
        TorrentHandle handle = requireHandle(id);
        handle.setDownloadLimit(limit);
    }

    public int get_download_limit(String id) {
        TorrentHandle handle = requireHandle(id);
        return handle.getDownloadLimit();
    }

    public void set_file_priorities(String id, int[] priorities) {
        TorrentHandle handle = requireHandle(id);
        Priority[] mapped = new Priority[priorities.length];
        for (int i = 0; i < priorities.length; i++) {
            mapped[i] = Priority.fromSwig(priorities[i]);
        }
        handle.prioritizeFiles(mapped);
    }

    public int get_num_files(String id) {
        TorrentHandle handle = requireHandle(id);
        com.frostwire.jlibtorrent.TorrentInfo info = handle.torrentFile();
        return info == null ? 0 : info.files().numFiles();
    }

    public LibTorrentFileEntry[] get_files(String id) {
        TorrentHandle handle = requireHandle(id);
        com.frostwire.jlibtorrent.TorrentInfo info = handle.torrentFile();
        return info == null ? new LibTorrentFileEntry[0] : buildFileEntries(handle, info);
    }

    public void set_auto_managed_torrent(String sha1, boolean auto_managed) {
        TorrentHandle handle = requireHandle(sha1);
        if (auto_managed) {
            handle.setFlags(TorrentFlags.AUTO_MANAGED);
        } else {
            handle.unsetFlags(TorrentFlags.AUTO_MANAGED);
        }
    }

    public void set_file_priority(String sha1, int index, int priority) {
        TorrentHandle handle = requireHandle(sha1);
        handle.filePriority(index, Priority.fromSwig(priority));
    }

    public boolean has_metadata(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null && handle.isValid() && handle.status(true).hasMetadata();
    }

    public boolean is_valid(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null && handle.isValid();
    }

    public TorrentInfo get_torrent_info(String id) {
        TorrentHandle handle = requireHandle(id);
        com.frostwire.jlibtorrent.TorrentInfo info = handle.torrentFile();
        if (info == null) {
            return null;
        }

        List<TorrentTracker> trackers = new ArrayList<TorrentTracker>();
        for (AnnounceEntry tracker : info.trackers()) {
            trackers.add(toLegacyTracker(tracker));
        }

        List<String> seeds = new ArrayList<String>();
        for (WebSeedEntry seed : info.webSeeds()) {
            seeds.add(seed.url());
        }

        return new TorrentInfoImpl(info.name(), info.pieceLength(), trackers, seeds,
                buildFileEntries(handle, info));
    }

    public void free_torrent_info(TorrentInfo info) {
        // No-op. jlibtorrent objects are JVM managed.
    }

    public void save_fast_resume_data(LibTorrentAlert alert, String filePath) {
        byte[] resumeData = pendingResumeData.remove(alert);
        if (resumeData == null || filePath == null) {
            return;
        }

        try {
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                java.nio.file.Files.createDirectories(path.getParent());
            }
            java.nio.file.Files.write(path, resumeData);
        } catch (IOException e) {
            throw new TorrentException("Unable to save fast resume data: " + e.getMessage(),
                    TorrentException.LOAD_EXCEPTION);
        }
    }

    public void set_peer_proxy(LibTorrentProxySetting proxySetting) {
        peerProxy = sanitizePeerStyleProxy(proxySetting);
        applyRuntimeSettings();
    }

    public void set_dht_proxy(LibTorrentProxySetting proxySetting) {
        dhtProxy = sanitizePeerStyleProxy(proxySetting);
        applyRuntimeSettings();
    }

    public void set_tracker_proxy(LibTorrentProxySetting proxySetting) {
        trackerProxy = sanitizeTrackerStyleProxy(proxySetting);
        applyRuntimeSettings();
    }

    public void set_web_seed_proxy(LibTorrentProxySetting proxySetting) {
        webSeedProxy = sanitizeTrackerStyleProxy(proxySetting);
        applyRuntimeSettings();
    }

    public TorrentPiecesInfo get_pieces_status(String sha1) {
        TorrentHandle handle = requireHandle(sha1);
        com.frostwire.jlibtorrent.TorrentStatus status = handle.status(TorrentHandle.QUERY_PIECES);
        int numPieces = status.numPieces();
        if (numPieces <= 0 && handle.torrentFile() != null) {
            numPieces = handle.torrentFile().files().numPieces();
        }
        if (numPieces <= 0) {
            LibTorrentPiecesInfoContainer container = new LibTorrentPiecesInfoContainer();
            container.stateInfo = "";
            container.numPiecesCompleted = 0;
            return new LibTorrentPiecesInfo(container);
        }

        char[] states = new char[numPieces];
        java.util.Arrays.fill(states, 'U');

        int completedPieces = 0;
        if (status.pieces() != null && !status.pieces().isEmpty()) {
            int size = Math.min(numPieces, status.pieces().size());
            for (int i = 0; i < size; i++) {
                if (status.pieces().getBit(i)) {
                    states[i] = 'x';
                    completedPieces++;
                }
            }
        }

        for (PartialPieceInfo pieceInfo : handle.getDownloadQueue()) {
            int pieceIndex = pieceInfo.pieceIndex();
            if (pieceIndex >= 0 && pieceIndex < states.length && states[pieceIndex] != 'x') {
                states[pieceIndex] = (pieceInfo.requested() > 0 || pieceInfo.writing() > 0) ? 'a'
                        : 'p';
            }
        }

        int[] availability = handle.pieceAvailability();
        for (int i = 0; i < Math.min(availability.length, states.length); i++) {
            if (states[i] == 'U' && availability[i] > 0) {
                states[i] = '0';
            }
        }

        LibTorrentPiecesInfoContainer container = new LibTorrentPiecesInfoContainer();
        container.stateInfo = new String(states);
        container.numPiecesCompleted = completedPieces;
        return new LibTorrentPiecesInfo(container);
    }

    public void add_tracker(String sha1, String url, int tier) {
        TorrentHandle handle = requireHandle(sha1);
        AnnounceEntry tracker = new AnnounceEntry(url);
        tracker.tier((short) tier);
        handle.addTracker(tracker);
    }

    public void remove_tracker(String sha1, String url, int tier) {
        TorrentHandle handle = requireHandle(sha1);
        List<AnnounceEntry> updated = new ArrayList<AnnounceEntry>();
        for (AnnounceEntry tracker : handle.trackers()) {
            if (!url.equals(tracker.url()) || tier != tracker.tier()) {
                updated.add(tracker);
            }
        }
        handle.replaceTrackers(updated);
    }

    public LibTorrentAnnounceEntry[] get_trackers(String sha1) {
        TorrentHandle handle = requireHandle(sha1);
        List<AnnounceEntry> trackers = handle.trackers();
        LibTorrentAnnounceEntry[] result = new LibTorrentAnnounceEntry[trackers.size()];
        for (int i = 0; i < trackers.size(); i++) {
            result[i] = toLegacyTracker(trackers.get(i));
        }
        return result;
    }

    public void queue_tracker_scrape_request(final String sha1String, final String trackerUri,
            final TrackerScrapeRequestCallback callback) {
        final TorrentHandle handle = findHandle(sha1String);
        if (handle == null || !handle.isValid()) {
            callback.callback(SCRAPE_STATUS_ERROR, 0, 0, 0);
            return;
        }

        final SessionManager manager = requireLoadedSession();
        final AtomicBoolean done = new AtomicBoolean(false);
        final AlertListener listener = new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                if (done.get()) {
                    return;
                }

                if (alert instanceof ScrapeReplyAlert) {
                    ScrapeReplyAlert replyAlert = (ScrapeReplyAlert) alert;
                    if (matchesScrape(replyAlert.handle(), replyAlert.trackerUrl(), sha1String, trackerUri)
                            && done.compareAndSet(false, true)) {
                        manager.removeListener(this);
                        callback.callback(SCRAPE_STATUS_SUCCESS, replyAlert.getComplete(),
                                replyAlert.getIncomplete(), -1);
                    }
                } else if (alert instanceof ScrapeFailedAlert) {
                    ScrapeFailedAlert failedAlert = (ScrapeFailedAlert) alert;
                    if (matchesScrape(failedAlert.handle(), failedAlert.trackerUrl(), sha1String,
                            trackerUri) && done.compareAndSet(false, true)) {
                        manager.removeListener(this);
                        callback.callback(SCRAPE_STATUS_ERROR, 0, 0, 0);
                    }
                }
            }
        };

        manager.addListener(listener);
        handle.scrapeTracker();

        Thread timeoutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SCRAPE_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (done.compareAndSet(false, true)) {
                    manager.removeListener(listener);
                    callback.callback(SCRAPE_STATUS_TIMEOUT, 0, 0, 0);
                }
            }
        }, "jlibtorrent-scrape-timeout");
        timeoutThread.setDaemon(true);
        timeoutThread.start();
    }

    private SessionManager requireLoadedSession() {
        SessionManager manager = sessionManager;
        if (manager == null || !loaded.get()) {
            throw new TorrentException("Torrent session is not loaded.", TorrentException.LOAD_EXCEPTION);
        }
        return manager;
    }

    private TorrentHandle requireHandle(String sha1) {
        TorrentHandle handle = findHandle(sha1);
        if (handle == null || !handle.isValid()) {
            throw new TorrentException("Unknown torrent: " + sha1, TorrentException.LOAD_EXCEPTION);
        }
        return handle;
    }

    private TorrentHandle findHandle(String sha1) {
        SessionManager manager = sessionManager;
        if (manager == null || sha1 == null) {
            return null;
        }
        return manager.find(new Sha1Hash(sha1));
    }

    private List<TorrentHandle> validHandles() {
        SessionManager manager = sessionManager;
        if (manager == null) {
            return Collections.emptyList();
        }

        TorrentHandle[] handles = manager.getTorrentHandles();
        List<TorrentHandle> validHandles = new ArrayList<TorrentHandle>(handles.length);
        for (TorrentHandle handle : handles) {
            if (handle != null && handle.isValid()) {
                validHandles.add(handle);
            }
        }
        return validHandles;
    }

    private AddTorrentParams buildAddTorrentParams(String sha1, String trackerURI, String torrentPath,
            String savePath, String fastResumePath) {
        AddTorrentParams params = readResumeData(fastResumePath);

        if (torrentPath != null) {
            if (params == null) {
                params = new AddTorrentParams();
            }
            params.torrentInfo(new com.frostwire.jlibtorrent.TorrentInfo(new File(torrentPath)));
        } else if (params == null) {
            params = AddTorrentParams.parseMagnetUri(buildMagnetUri(sha1, trackerURI));
        }

        if (params.name() == null || params.name().length() == 0) {
            params.name(sha1);
        }

        if (trackerURI != null && params.trackers().isEmpty()) {
            params.trackers(Collections.singletonList(trackerURI));
            params.trackerTiers(Collections.singletonList(0));
        }

        params.savePath(savePath);
        params.flags(defaultAddTorrentFlags(params.flags()));
        return params;
    }

    private AddTorrentParams readResumeData(String fastResumePath) {
        if (fastResumePath == null) {
            return null;
        }

        Path resumePath = Path.of(fastResumePath);
        if (!java.nio.file.Files.isRegularFile(resumePath)) {
            return null;
        }

        try {
            byte[] data = java.nio.file.Files.readAllBytes(resumePath);
            error_code ec = new error_code();
            add_torrent_params params = add_torrent_params.read_resume_data(
                    Vectors.bytes2byte_vector(data), ec);
            if (ec.value() != 0) {
                LOG.warnf("Unable to read resume data from {0}: {1}", fastResumePath, ec.message());
                return null;
            }
            return new AddTorrentParams(params);
        } catch (IOException e) {
            LOG.warnf("Unable to load resume data from {0}: {1}", fastResumePath, e.getMessage());
            return null;
        }
    }

    private torrent_flags_t defaultAddTorrentFlags(torrent_flags_t existingFlags) {
        torrent_flags_t flags = existingFlags == null ? new torrent_flags_t() : existingFlags;
        flags = flags.or_(TorrentFlags.PAUSED);
        flags = flags.or_(TorrentFlags.UPDATE_SUBSCRIBE);
        flags = flags.and_(TorrentFlags.AUTO_MANAGED.inv());
        return flags;
    }

    private String buildMagnetUri(String sha1, String trackerURI) {
        StringBuilder builder = new StringBuilder("magnet:?xt=urn:btih:");
        builder.append(sha1);
        builder.append("&dn=").append(encodeComponent(sha1));
        if (trackerURI != null && trackerURI.length() > 0) {
            builder.append("&tr=").append(encodeComponent(trackerURI));
        }
        return builder.toString();
    }

    private String encodeComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void populateStatus(TorrentHandle handle, LibTorrentStatus target) {
        com.frostwire.jlibtorrent.TorrentStatus status = handle.status(true);
        target.total_done = status.totalDone();
        target.total_wanted_done = status.totalWantedDone();
        target.total_wanted = status.totalWanted();
        target.total_download = status.totalDownload();
        target.total_upload = status.totalUpload();
        target.total_payload_download = status.totalPayloadDownload();
        target.total_payload_upload = status.totalPayloadUpload();
        target.all_time_payload_download = status.allTimeDownload();
        target.all_time_payload_upload = status.allTimeUpload();
        target.download_rate = status.downloadRate();
        target.upload_rate = status.uploadRate();
        target.download_payload_rate = status.downloadPayloadRate();
        target.upload_payload_rate = status.uploadPayloadRate();
        target.num_peers = status.numPeers();
        target.num_uploads = status.numUploads();
        target.num_seeds = status.numSeeds();
        target.num_connections = status.numConnections();
        target.state = toLegacyStateId(status);
        target.progress = status.progress();
        target.paused = handle.flags().and_(TorrentFlags.PAUSED).nonZero() ? 1 : 0;
        target.finished = status.isFinished() ? 1 : 0;
        target.valid = handle.isValid() ? 1 : 0;
        target.auto_managed = handle.flags().and_(TorrentFlags.AUTO_MANAGED).nonZero() ? 1 : 0;
        target.seeding_time = saturatingInt(status.seedingDuration());
        target.active_time = saturatingInt(status.activeDuration());
        target.error = status.errorCode().isError() ? status.errorCode().message() : "";
        target.current_tracker = status.currentTracker();
        target.num_complete = status.numComplete();
        target.num_incomplete = status.numIncomplete();
        target.total_failed_bytes = status.totalFailedBytes();
    }

    private int toLegacyStateId(com.frostwire.jlibtorrent.TorrentStatus status) {
        switch (status.state()) {
        case CHECKING_RESUME_DATA:
            return LibTorrentState.QUEUED_FOR_CHECKING.getId();
        case CHECKING_FILES:
            return LibTorrentState.CHECKING_FILES.getId();
        case DOWNLOADING_METADATA:
            return LibTorrentState.DOWNLOADING_METADATA.getId();
        case DOWNLOADING:
            return LibTorrentState.DOWNLOADING.getId();
        case FINISHED:
            return LibTorrentState.FINISHED.getId();
        case SEEDING:
            return LibTorrentState.SEEDING.getId();
        default:
            return LibTorrentState.DOWNLOADING.getId();
        }
    }

    private int saturatingInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private LibTorrentFileEntry[] buildFileEntries(TorrentHandle handle,
            com.frostwire.jlibtorrent.TorrentInfo info) {
        int fileCount = info.files().numFiles();
        long[] fileProgress = handle.fileProgress(TorrentHandle.PIECE_GRANULARITY);
        Priority[] priorities = handle.filePriorities();

        LibTorrentFileEntry[] entries = new LibTorrentFileEntry[fileCount];
        for (int i = 0; i < fileCount; i++) {
            LibTorrentFileEntry entry = new LibTorrentFileEntry();
            entry.index = i;
            entry.path = new WString(info.files().filePath(i));
            entry.size = info.files().fileSize(i);
            entry.total_done = i < fileProgress.length ? fileProgress[i] : 0L;
            entry.priority = i < priorities.length ? priorities[i].swig() : Priority.NORMAL.swig();
            entries[i] = entry;
        }
        return entries;
    }

    private LibTorrentPeer toLegacyPeer(peer_info peerInfo) {
        LibTorrentPeer peer = new LibTorrentPeer();
        peer.status_flags = peerInfo.getFlags() == null ? 0 : peerInfo.getFlags().to_int();
        peer.peer_id = peerInfo.getPid() == null ? "" : new Sha1Hash(peerInfo.getPid()).toHex();
        peer.ip = new TcpEndpoint(peerInfo.getIp()).address().toString();
        peer.source = peerInfo.getSource() == null ? 0 : peerInfo.getSource().to_int();
        peer.up_speed = peerInfo.getUp_speed();
        peer.down_speed = peerInfo.getDown_speed();
        peer.payload_up_speed = peerInfo.getPayload_up_speed();
        peer.payload_down_speed = peerInfo.getPayload_down_speed();
        peer.progress = peerInfo.getProgress();
        peer.country = "";
        peer.clientName = new WString(Vectors.byte_vector2utf8(peerInfo.get_client()));
        return peer;
    }

    private LibTorrentAnnounceEntry toLegacyTracker(AnnounceEntry tracker) {
        LibTorrentAnnounceEntry entry = new LibTorrentAnnounceEntry();
        entry.uri = tracker.url();
        entry.tier = tracker.tier();
        return entry;
    }

    private void queueStatusAlert(String sha1, String message) {
        if (sha1 == null || sha1.length() == 0) {
            return;
        }
        pendingAlerts.add(createAlert(sha1, LibTorrentAlert.status_notification, message, null));
    }

    private void queueResumeAlert(SaveResumeDataAlert alert) {
        String sha1 = toSha1(alert.handle());
        byte[] resumeData = buildResumeDataBytes(alert);
        if (sha1 != null && resumeData != null) {
            pendingAlerts.add(createAlert(sha1, LibTorrentAlert.storage_notification,
                    "resume data saved", resumeData));
        }
    }

    private byte[] buildResumeDataBytes(SaveResumeDataAlert alert) {
        try {
            return AddTorrentParams.writeResumeData(alert.params()).bencode();
        } catch (RuntimeException e) {
            LOG.warn("Unable to serialize resume data", e);
            return null;
        }
    }

    private LibTorrentAlert createAlert(String sha1, int category, String message, byte[] resumeData) {
        LibTorrentAlert alert = new LibTorrentAlert();
        alert.sha1 = sha1;
        alert.category = category;
        alert.message = message;
        alert.has_data = resumeData != null ? 1 : 0;
        if (resumeData != null) {
            pendingResumeData.put(alert, resumeData);
        }
        return alert;
    }

    private String toSha1(TorrentHandle handle) {
        return handle != null && handle.isValid() ? handle.infoHash().toHex() : null;
    }

    private SessionParams buildSessionParams(TorrentManagerSettings settings, SettingsPack settingsPack) {
        File dhtStateFile = settings.getDHTStateFile();
        if (dhtStateFile != null && dhtStateFile.exists() && dhtStateFile.isFile()) {
            try {
                SessionParams sessionParams = new SessionParams(dhtStateFile);
                sessionParams.setSettings(settingsPack);
                return sessionParams;
            } catch (IllegalArgumentException e) {
                LOG.warnf("Unable to restore session state from {0}: {1}", dhtStateFile,
                        e.getMessage());
            }
        }
        return new SessionParams(settingsPack);
    }

    private SettingsPack buildSettingsPack(TorrentManagerSettings settings) {
        SettingsPack settingsPack = new SettingsPack();
        settingsPack.uploadRateLimit(settings.getMaxUploadBandwidth());
        settingsPack.downloadRateLimit(settings.getMaxDownloadBandwidth());
        settingsPack.activeDownloads(settings.getActiveDownloadsLimit());
        settingsPack.activeSeeds(settings.getActiveSeedsLimit());
        settingsPack.activeLimit(settings.getActiveLimit());
        settingsPack.setBoolean(settings_pack.bool_types.enable_dht.swigValue(), dhtEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_upnp.swigValue(), upnpEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_natpmp.swigValue(), natpmpEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.enable_lsd.swigValue(), lsdEnabled);
        settingsPack.listenInterfaces(buildListenInterfaces(settings));
        settingsPack.setInteger(settings_pack.int_types.max_retry_port_bind.swigValue(),
                Math.max(0, settings.getListenEndPort() - settings.getListenStartPort()));
        if (!settings.getBootStrapDHTRouters().isEmpty()) {
            settingsPack.setDhtBootstrapNodes(buildBootstrapNodeList(settings));
        }
        applyProxySettings(settingsPack);
        return settingsPack;
    }

    private void applyRuntimeSettings() {
        SessionManager manager = sessionManager;
        TorrentManagerSettings settings = torrentSettings;
        if (manager == null || settings == null) {
            return;
        }
        manager.applySettings(buildSettingsPack(settings));
    }

    private String buildListenInterfaces(TorrentManagerSettings settings) {
        String listenInterface = settings.getListenInterface();
        int listenPort = settings.getListenStartPort();
        if (listenInterface == null || listenInterface.length() == 0) {
            return "0.0.0.0:" + listenPort + ",[::]:" + listenPort;
        }
        if (listenInterface.indexOf(':') != -1 && !listenInterface.startsWith("[")) {
            return "[" + listenInterface + "]:" + listenPort;
        }
        return listenInterface + ":" + listenPort;
    }

    private String buildBootstrapNodeList(TorrentManagerSettings settings) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (TorrentIpPort ipPort : settings.getBootStrapDHTRouters()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(ipPort.getAddress()).append(':').append(ipPort.getPort());
            first = false;
        }
        return builder.toString();
    }

    private void applyProxySettings(SettingsPack settingsPack) {
        LibTorrentProxySetting effectiveProxy = chooseEffectiveProxy();
        boolean peerEnabled = hasProxy(peerProxy) || hasProxy(dhtProxy);
        boolean trackerEnabled = hasProxy(trackerProxy) || hasProxy(webSeedProxy);

        if (!hasProxy(effectiveProxy)) {
            settingsPack.setInteger(settings_pack.int_types.proxy_type.swigValue(),
                    settings_pack.proxy_type_t.none.swigValue());
            settingsPack.setString(settings_pack.string_types.proxy_hostname.swigValue(), "");
            settingsPack.setInteger(settings_pack.int_types.proxy_port.swigValue(), 0);
            settingsPack.setString(settings_pack.string_types.proxy_username.swigValue(), "");
            settingsPack.setString(settings_pack.string_types.proxy_password.swigValue(), "");
            settingsPack.setBoolean(settings_pack.bool_types.proxy_peer_connections.swigValue(),
                    false);
            settingsPack.setBoolean(settings_pack.bool_types.proxy_tracker_connections.swigValue(),
                    false);
            settingsPack.setBoolean(settings_pack.bool_types.proxy_hostnames.swigValue(), false);
            return;
        }

        settingsPack.setInteger(settings_pack.int_types.proxy_type.swigValue(),
                toProxyType(effectiveProxy).swigValue());
        settingsPack.setString(settings_pack.string_types.proxy_hostname.swigValue(),
                effectiveProxy.getHostname());
        settingsPack.setInteger(settings_pack.int_types.proxy_port.swigValue(),
                effectiveProxy.getPort());
        settingsPack.setString(settings_pack.string_types.proxy_username.swigValue(),
                nullToEmpty(effectiveProxy.getUsername()));
        settingsPack.setString(settings_pack.string_types.proxy_password.swigValue(),
                nullToEmpty(effectiveProxy.getPassword()));
        settingsPack.setBoolean(settings_pack.bool_types.proxy_peer_connections.swigValue(),
                peerEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.proxy_tracker_connections.swigValue(),
                trackerEnabled);
        settingsPack.setBoolean(settings_pack.bool_types.proxy_hostnames.swigValue(), true);
    }

    private LibTorrentProxySetting chooseEffectiveProxy() {
        LibTorrentProxySetting[] candidates = new LibTorrentProxySetting[] { trackerProxy, webSeedProxy,
                peerProxy, dhtProxy };

        LibTorrentProxySetting effective = null;
        for (LibTorrentProxySetting candidate : candidates) {
            if (!hasProxy(candidate)) {
                continue;
            }
            if (effective == null) {
                effective = candidate;
            } else if (!sameProxy(effective, candidate)) {
                LOG.warnf("Conflicting torrent proxy settings detected; using {0} and ignoring {1}",
                        describeProxy(effective), describeProxy(candidate));
            }
        }

        return effective == null ? LibTorrentProxySetting.nullProxy() : effective;
    }

    private boolean hasProxy(LibTorrentProxySetting proxy) {
        return proxy != null && proxy.getType() != null && proxy.getHostname() != null
                && proxy.getHostname().length() > 0 && proxy.getPort() > 0;
    }

    private boolean sameProxy(LibTorrentProxySetting left, LibTorrentProxySetting right) {
        return Objects.equals(left.getType(), right.getType())
                && Objects.equals(left.getHostname(), right.getHostname())
                && left.getPort() == right.getPort()
                && Objects.equals(left.getUsername(), right.getUsername())
                && Objects.equals(left.getPassword(), right.getPassword());
    }

    private String describeProxy(LibTorrentProxySetting proxy) {
        return proxy.getType() + "://" + proxy.getHostname() + ":" + proxy.getPort();
    }

    private LibTorrentProxySetting sanitizePeerStyleProxy(LibTorrentProxySetting proxy) {
        if (proxy == null || proxy.getType() == null) {
            return LibTorrentProxySetting.nullProxy();
        }
        if (proxy.getType() == ProxySettingType.HTTP || proxy.getType() == ProxySettingType.HTTP_PW) {
            return LibTorrentProxySetting.nullProxy();
        }
        return proxy;
    }

    private LibTorrentProxySetting sanitizeTrackerStyleProxy(LibTorrentProxySetting proxy) {
        return proxy == null ? LibTorrentProxySetting.nullProxy() : proxy;
    }

    private settings_pack.proxy_type_t toProxyType(LibTorrentProxySetting proxy) {
        switch (proxy.getType()) {
        case SOCKS4:
            return settings_pack.proxy_type_t.socks4;
        case SOCKS5:
            return settings_pack.proxy_type_t.socks5;
        case SOCKS5_PW:
            return settings_pack.proxy_type_t.socks5_pw;
        case HTTP:
            return settings_pack.proxy_type_t.http;
        case HTTP_PW:
            return settings_pack.proxy_type_t.http_pw;
        default:
            return settings_pack.proxy_type_t.none;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void reopenMappedPorts() {
        SessionManager manager = sessionManager;
        if (manager != null) {
            new SessionHandle(manager.swig()).reopenNetworkSockets(SessionHandle.REOPEN_MAP_PORTS);
        }
    }

    private boolean matchesScrape(TorrentHandle handle, String alertTrackerUri, String expectedSha1,
            String expectedTrackerUri) {
        return handle != null && handle.isValid() && Objects.equals(expectedSha1, toSha1(handle))
                && Objects.equals(expectedTrackerUri, alertTrackerUri);
    }
}
