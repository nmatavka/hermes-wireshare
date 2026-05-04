package org.limewire.libtorrent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.limewire.bittorrent.ProxySettingType;
import org.limewire.bittorrent.TorrentAlert;
import org.limewire.bittorrent.TorrentException;
import org.limewire.bittorrent.TorrentFileEntry;
import org.limewire.bittorrent.TorrentInfo;
import org.limewire.bittorrent.TorrentManagerSettings;
import org.limewire.bittorrent.TorrentParams;
import org.limewire.bittorrent.TorrentPiecesInfo;
import org.limewire.bittorrent.TorrentStatus;
import org.limewire.inject.LazySingleton;
import org.limewire.libtorrent.callback.AlertCallback;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.util.ExceptionUtils;

import com.frostwire.jlibtorrent.AddTorrentParams;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.ErrorCode;
import com.frostwire.jlibtorrent.Pair;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionHandle;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.Sha1Hash;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataFailedAlert;
import com.frostwire.jlibtorrent.alerts.ScrapeFailedAlert;
import com.frostwire.jlibtorrent.alerts.ScrapeReplyAlert;
import com.frostwire.jlibtorrent.alerts.StateUpdateAlert;
import com.frostwire.jlibtorrent.swig.error_code;
import com.frostwire.jlibtorrent.swig.settings_pack;

/**
 * Compatibility wrapper that preserves the old libtorrent-facing API while
 * delegating to the local jlibtorrent project.
 */
@LazySingleton
class LibTorrentWrapper {

    private static final Log LOG = LogFactory.getLog(LibTorrentWrapper.class);
    private static final long RESUME_SAVE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(15);

    private final SessionManager sessionManager = new SessionManager(false);
    private final LinkedBlockingQueue<LibTorrentAlert> pendingAlerts =
            new LinkedBlockingQueue<LibTorrentAlert>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final AtomicBoolean dhtEnabled = new AtomicBoolean(false);
    private final AtomicBoolean upnpEnabled = new AtomicBoolean(false);
    private final AtomicBoolean natpmpEnabled = new AtomicBoolean(false);
    private final AtomicBoolean lsdEnabled = new AtomicBoolean(false);
    private final AtomicReference<TorrentManagerSettings> torrentSettings =
            new AtomicReference<TorrentManagerSettings>();
    private final AtomicReference<LibTorrentProxySetting> peerProxy =
            new AtomicReference<LibTorrentProxySetting>(LibTorrentProxySetting.nullProxy());
    private final AtomicReference<LibTorrentProxySetting> dhtProxy =
            new AtomicReference<LibTorrentProxySetting>(LibTorrentProxySetting.nullProxy());
    private final AtomicReference<LibTorrentProxySetting> trackerProxy =
            new AtomicReference<LibTorrentProxySetting>(LibTorrentProxySetting.nullProxy());
    private final AtomicReference<LibTorrentProxySetting> webSeedProxy =
            new AtomicReference<LibTorrentProxySetting>(LibTorrentProxySetting.nullProxy());
    private final AtomicReference<IpFilterCallback> ipFilter =
            new AtomicReference<IpFilterCallback>();

    private final AlertListener bridgeListener = new AlertListener() {
        @Override
        public int[] types() {
            return null;
        }

        @Override
        public void alert(Alert<?> alert) {
            bridgeAlert(alert);
        }
    };

    LibTorrentWrapper() {
        sessionManager.addListener(bridgeListener);
    }

    void initialize(TorrentManagerSettings settings) {
        torrentSettings.set(settings);

        try {
            SessionParams sessionParams = buildSessionParams(settings);
            sessionManager.start(sessionParams);
            loaded.set(sessionManager.isRunning());
        } catch (Throwable e) {
            loaded.set(false);
            LOG.error("Failure loading the jlibtorrent libraries.", e);
            if (settings.isReportingLibraryLoadFailture()) {
                ExceptionUtils.reportOrReturn(e);
            }
        }
    }

    public boolean isLoaded() {
        return loaded.get() && sessionManager.isRunning();
    }

    public void add_torrent(TorrentParams params) {
        try {
            AddTorrentParams addParams = JlibTorrentAdapterSupport.createAddTorrentParams(params);
            ErrorCode ec = new ErrorCode(new error_code());
            TorrentHandle handle = sessionHandle().addTorrent(addParams, ec);
            if (ec.isError() && (handle == null || !handle.isValid())) {
                throw new TorrentException(ec.message(), ec.value());
            }
        } catch (IOException e) {
            throw new TorrentException(e.getMessage(), TorrentException.LOAD_EXCEPTION);
        }
    }

    public void freeze_and_save_all_fast_resume_data(AlertCallback alertCallback) {
        Set<String> pendingSha1s = new HashSet<String>();
        for (TorrentHandle handle : sessionManager.getTorrentHandles()) {
            if (handle != null && handle.isValid()) {
                String sha1 = safeSha1(handle);
                if (sha1 != null) {
                    pendingSha1s.add(sha1);
                }
                handle.saveResumeData(TorrentHandle.SAVE_INFO_DICT);
            }
        }

        long deadline = System.currentTimeMillis() + RESUME_SAVE_TIMEOUT_MILLIS;
        while (!pendingSha1s.isEmpty() && System.currentTimeMillis() < deadline) {
            try {
                LibTorrentAlert alert = pendingAlerts.poll(250, TimeUnit.MILLISECONDS);
                if (alert == null) {
                    continue;
                }
                alertCallback.callback(alert);
                if (alert.getCategory() == TorrentAlert.storage_notification
                        && alert.getSha1() != null) {
                    pendingSha1s.remove(alert.getSha1());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void get_alerts(AlertCallback alertCallback) {
        if (!isLoaded()) {
            return;
        }

        sessionManager.postTorrentUpdates();
        dispatchPendingAlerts(alertCallback, 50);
    }

    public void set_ip_filter(IpFilterCallback ipFilterCallback) {
        this.ipFilter.set(ipFilterCallback);
        LOG.warn("Per-peer IP callback filtering is not available in jlibtorrent; ignoring filter");
    }

    public void pause_torrent(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.pause();
        }
    }

    public void resume_torrent(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.resume();
        }
    }

    public void scrape_tracker(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.scrapeTracker();
        }
    }

    public void force_reannounce(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.forceReannounce();
        }
    }

    public void get_torrent_status(String id, TorrentStatus status) {
        LibTorrentStatus target = (LibTorrentStatus) status;
        LibTorrentStatus source = new LibTorrentStatus();
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            source = JlibTorrentAdapterSupport.toStatus(handle.status());
        } else {
            source.valid = 0;
        }
        copyStatus(source, target);
    }

    public void remove_torrent(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            sessionHandle().removeTorrent(handle);
        }
    }

    public LibTorrentPeer[] get_peers(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null ? JlibTorrentAdapterSupport.toPeers(handle) : new LibTorrentPeer[0];
    }

    public void signal_fast_resume_data_request(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.saveResumeData(TorrentHandle.SAVE_INFO_DICT);
        }
    }

    public void clear_error_and_retry(String id) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.swig().clear_error();
            handle.resume();
        }
    }

    public void move_torrent(String id, String absolutePath) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.moveStorage(absolutePath);
        }
    }

    public void abort_torrents() {
        sessionManager.stop();
        loaded.set(false);
    }

    public void free_torrent_status(LibTorrentStatus status) {
        // no-op, compatibility hook retained for callers
    }

    public void update_settings(TorrentManagerSettings settings) {
        torrentSettings.set(settings);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(settings));
        }
    }

    public void start_dht() {
        start_dht(null);
    }

    public void start_dht(File dhtStateFile) {
        dhtEnabled.set(true);
        if (isLoaded()) {
            sessionManager.startDht();
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void stop_dht() {
        dhtEnabled.set(false);
        if (isLoaded()) {
            sessionManager.stopDht();
        }
    }

    public void save_dht_state(File dhtStateFile) {
        if (!isLoaded() || dhtStateFile == null) {
            return;
        }

        try {
            File parent = dhtStateFile.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            byte[] sessionState = sessionManager.saveState();
            if (sessionState != null) {
                Files.write(dhtStateFile.toPath(), sessionState);
            }
        } catch (IOException e) {
            throw new TorrentException(e.getMessage(), TorrentException.LOAD_EXCEPTION);
        }
    }

    public void add_dht_router(String address, int port) {
        add_dht_node(address, port);
    }

    public void add_dht_node(String address, int port) {
        if (isLoaded()) {
            sessionHandle().addDhtNode(new Pair<String, Integer>(address, Integer.valueOf(port)));
        }
    }

    public void start_upnp() {
        upnpEnabled.set(true);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void stop_upnp() {
        upnpEnabled.set(false);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void start_lsd() {
        lsdEnabled.set(true);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void stop_lsd() {
        lsdEnabled.set(false);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void start_natpmp() {
        natpmpEnabled.set(true);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void stop_natpmp() {
        natpmpEnabled.set(false);
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    public void set_seed_ratio(String id, float seedRatio) {
        // jlibtorrent doesn't expose a per-torrent seed-ratio target through the Java API.
    }

    public void set_upload_limit(String id, int limit) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.setUploadLimit(limit);
        }
    }

    public int get_upload_limit(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null ? handle.getUploadLimit() : 0;
    }

    public void set_download_limit(String id, int limit) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.setDownloadLimit(limit);
        }
    }

    public int get_download_limit(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null ? handle.getDownloadLimit() : 0;
    }

    public void set_file_priorities(String id, int[] priorities) {
        TorrentHandle handle = findHandle(id);
        if (handle != null) {
            handle.prioritizeFiles(toPriorities(priorities));
        }
    }

    public int get_num_files(String id) {
        return get_files(id).length;
    }

    public LibTorrentFileEntry[] get_files(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null ? JlibTorrentAdapterSupport.toFileEntries(handle)
                : new LibTorrentFileEntry[0];
    }

    public void set_auto_managed_torrent(String sha1, boolean autoManaged) {
        TorrentHandle handle = findHandle(sha1);
        if (handle == null) {
            return;
        }

        if (autoManaged) {
            handle.setFlags(TorrentFlags.AUTO_MANAGED);
        } else {
            handle.unsetFlags(TorrentFlags.AUTO_MANAGED);
        }
    }

    public void set_file_priority(String sha1, int index, int priority) {
        TorrentHandle handle = findHandle(sha1);
        if (handle == null) {
            return;
        }

        Priority[] current = handle.filePriorities();
        if (index < 0 || index >= current.length) {
            return;
        }
        current[index] = toPriority(priority);
        handle.prioritizeFiles(current);
    }

    public boolean has_metadata(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null && handle.torrentFile() != null;
    }

    public boolean is_valid(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null && handle.isValid();
    }

    public TorrentInfo get_torrent_info(String id) {
        TorrentHandle handle = findHandle(id);
        return handle != null ? JlibTorrentAdapterSupport.toTorrentInfo(handle) : null;
    }

    public void free_torrent_info(LibTorrentInfo info) {
        // no-op, compatibility hook retained for callers
    }

    public void save_fast_resume_data(LibTorrentAlert alert, String filePath) {
        if (alert == null || !alert.hasResumeData() || filePath == null) {
            return;
        }

        try {
            File target = new File(filePath);
            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.write(target.toPath(), AddTorrentParams.writeResumeData(alert.getResumeData())
                    .bencode());
        } catch (IOException e) {
            throw new TorrentException(e.getMessage(), TorrentException.LOAD_EXCEPTION);
        }
    }

    public void set_peer_proxy(LibTorrentProxySetting proxySetting) {
        peerProxy.set(normalizeProxy(proxySetting));
        applyProxySettingsIfRunning();
    }

    public void set_dht_proxy(LibTorrentProxySetting proxySetting) {
        dhtProxy.set(normalizeProxy(proxySetting));
        applyProxySettingsIfRunning();
    }

    public void set_tracker_proxy(LibTorrentProxySetting proxySetting) {
        trackerProxy.set(normalizeProxy(proxySetting));
        applyProxySettingsIfRunning();
    }

    public void set_web_seed_proxy(LibTorrentProxySetting proxySetting) {
        webSeedProxy.set(normalizeProxy(proxySetting));
        applyProxySettingsIfRunning();
    }

    public TorrentPiecesInfo get_pieces_status(String sha1) {
        TorrentHandle handle = findHandle(sha1);
        return handle != null ? JlibTorrentAdapterSupport.toPiecesInfo(handle)
                : new LibTorrentPiecesInfo("", 0);
    }

    public void add_tracker(String sha1, String url, int tier) {
        TorrentHandle handle = findHandle(sha1);
        if (handle != null) {
            com.frostwire.jlibtorrent.AnnounceEntry entry =
                    new com.frostwire.jlibtorrent.AnnounceEntry(url);
            entry.tier((short) tier);
            handle.addTracker(entry);
        }
    }

    public void remove_tracker(String sha1, String url, int tier) {
        TorrentHandle handle = findHandle(sha1);
        if (handle == null) {
            return;
        }

        List<com.frostwire.jlibtorrent.AnnounceEntry> trackers = handle.trackers();
        List<com.frostwire.jlibtorrent.AnnounceEntry> updated =
                new ArrayList<com.frostwire.jlibtorrent.AnnounceEntry>(trackers.size());
        for (com.frostwire.jlibtorrent.AnnounceEntry tracker : trackers) {
            boolean sameTier = tracker.tier() == tier;
            boolean sameUrl = url == null ? tracker.url() == null : url.equals(tracker.url());
            if (!(sameTier && sameUrl)) {
                updated.add(tracker);
            }
        }
        handle.replaceTrackers(updated);
    }

    public LibTorrentAnnounceEntry[] get_trackers(String sha1) {
        TorrentHandle handle = findHandle(sha1);
        return handle != null ? JlibTorrentAdapterSupport.toTrackers(handle)
                : new LibTorrentAnnounceEntry[0];
    }

    public void queue_tracker_scrape_request(final String sha1String, final String trackerUri,
            final TrackerScrapeRequestCallback callback) {
        final TorrentHandle handle = findHandle(sha1String);
        if (handle == null || callback == null) {
            if (callback != null) {
                callback.callback(2, 0, 0, 0);
            }
            return;
        }

        final AlertListener listener = new AlertListener() {
            @Override
            public int[] types() {
                return new int[] {
                        com.frostwire.jlibtorrent.alerts.AlertType.SCRAPE_REPLY.swig(),
                        com.frostwire.jlibtorrent.alerts.AlertType.SCRAPE_FAILED.swig()
                };
            }

            @Override
            public void alert(Alert<?> alert) {
                if (!(alert instanceof com.frostwire.jlibtorrent.alerts.TrackerAlert<?>)) {
                    return;
                }

                com.frostwire.jlibtorrent.alerts.TrackerAlert<?> trackerAlert =
                        (com.frostwire.jlibtorrent.alerts.TrackerAlert<?>) alert;
                String alertSha1 = safeSha1(trackerAlert.handle());
                if (!sha1String.equals(alertSha1)) {
                    return;
                }

                if (trackerUri != null && !trackerUri.equals(trackerAlert.trackerUrl())) {
                    return;
                }

                sessionManager.removeListener(this);
                if (alert instanceof ScrapeReplyAlert) {
                    ScrapeReplyAlert replyAlert = (ScrapeReplyAlert) alert;
                    callback.callback(0, replyAlert.getComplete(), replyAlert.getIncomplete(), -1);
                } else if (alert instanceof ScrapeFailedAlert) {
                    callback.callback(2, 0, 0, 0);
                }
            }
        };

        sessionManager.addListener(listener);
        handle.scrapeTracker();
    }

    private void bridgeAlert(Alert<?> alert) {
        try {
            if (alert instanceof StateUpdateAlert) {
                StateUpdateAlert stateUpdate = (StateUpdateAlert) alert;
                for (com.frostwire.jlibtorrent.TorrentStatus status : stateUpdate.status()) {
                    pendingAlerts.offer(new LibTorrentAlert(TorrentAlert.status_notification,
                            safeSha1(status.infoHash()), "state update"));
                }
                return;
            }

            if (alert instanceof MetadataReceivedAlert) {
                MetadataReceivedAlert metadataAlert = (MetadataReceivedAlert) alert;
                pendingAlerts.offer(new LibTorrentAlert(TorrentAlert.status_notification,
                        safeSha1(metadataAlert.handle()), "metadata successfully received"));
                return;
            }

            if (alert instanceof SaveResumeDataAlert) {
                SaveResumeDataAlert resumeAlert = (SaveResumeDataAlert) alert;
                pendingAlerts.offer(new LibTorrentAlert(TorrentAlert.storage_notification,
                        safeSha1(resumeAlert.handle()), resumeAlert.message(),
                        resumeAlert.params()));
                return;
            }

            if (alert instanceof SaveResumeDataFailedAlert) {
                SaveResumeDataFailedAlert failedAlert = (SaveResumeDataFailedAlert) alert;
                pendingAlerts.offer(new LibTorrentAlert(TorrentAlert.storage_notification,
                        safeSha1(failedAlert.handle()), failedAlert.message()));
                return;
            }

            if (alert instanceof com.frostwire.jlibtorrent.alerts.TorrentAlert<?>) {
                com.frostwire.jlibtorrent.alerts.TorrentAlert<?> torrentAlert =
                        (com.frostwire.jlibtorrent.alerts.TorrentAlert<?>) alert;
                pendingAlerts.offer(new LibTorrentAlert(toLegacyCategory(alert),
                        safeSha1(torrentAlert.handle()), alert.message()));
            }
        } catch (Throwable t) {
            LOG.debug("Unable to bridge jlibtorrent alert", t);
        }
    }

    private void dispatchPendingAlerts(AlertCallback alertCallback, long waitMillis) {
        try {
            LibTorrentAlert first = pendingAlerts.poll(waitMillis, TimeUnit.MILLISECONDS);
            if (first != null) {
                alertCallback.callback(first);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        LibTorrentAlert next;
        while ((next = pendingAlerts.poll()) != null) {
            alertCallback.callback(next);
        }
    }

    private SessionParams buildSessionParams(TorrentManagerSettings settings) {
        SettingsPack settingsPack = buildSettingsPack(settings);
        File stateFile = settings.getDHTStateFile();
        if (stateFile != null && stateFile.exists() && stateFile.isFile()) {
            try {
                SessionParams params = new SessionParams(stateFile);
                params.setSettings(settingsPack);
                return params;
            } catch (Throwable t) {
                LOG.warnf(t, "Unable to load session state from {0}", stateFile);
            }
        }
        return new SessionParams(settingsPack);
    }

    private SettingsPack buildSettingsPack(TorrentManagerSettings settings) {
        SettingsPack pack = JlibTorrentAdapterSupport.createSettings(settings, dhtEnabled.get(),
                upnpEnabled.get(), natpmpEnabled.get(), lsdEnabled.get());
        applyProxySettings(pack);
        if (ipFilter.get() != null) {
            pack.setBoolean(settings_pack.bool_types.apply_ip_filter_to_trackers.swigValue(),
                    false);
        }
        return pack;
    }

    private void applyProxySettings(SettingsPack pack) {
        LibTorrentProxySetting effective = effectiveProxy();
        boolean peerProxyEnabled = hasProxy(peerProxy.get());
        boolean trackerProxyEnabled = hasProxy(trackerProxy.get()) || hasProxy(webSeedProxy.get());

        pack.setInteger(settings_pack.int_types.proxy_type.swigValue(),
                JlibTorrentAdapterSupport.toProxyType(effective.getType()));
        pack.setInteger(settings_pack.int_types.proxy_port.swigValue(), effective.getPort());
        pack.setString(settings_pack.string_types.proxy_hostname.swigValue(),
                emptyString(effective.getHostname()));
        pack.setString(settings_pack.string_types.proxy_username.swigValue(),
                emptyString(effective.getUsername()));
        pack.setString(settings_pack.string_types.proxy_password.swigValue(),
                emptyString(effective.getPassword()));
        pack.setBoolean(settings_pack.bool_types.proxy_hostnames.swigValue(), hasProxy(effective));
        pack.setBoolean(settings_pack.bool_types.proxy_peer_connections.swigValue(),
                peerProxyEnabled || hasProxy(dhtProxy.get()));
        pack.setBoolean(settings_pack.bool_types.proxy_tracker_connections.swigValue(),
                trackerProxyEnabled);
    }

    private void applyProxySettingsIfRunning() {
        if (isLoaded()) {
            sessionManager.applySettings(buildSettingsPack(torrentSettings.get()));
        }
    }

    private LibTorrentProxySetting effectiveProxy() {
        LibTorrentProxySetting[] candidates = {
                peerProxy.get(), trackerProxy.get(), webSeedProxy.get(), dhtProxy.get()
        };
        for (LibTorrentProxySetting candidate : candidates) {
            if (hasProxy(candidate)) {
                return candidate;
            }
        }
        return LibTorrentProxySetting.nullProxy();
    }

    private boolean hasProxy(LibTorrentProxySetting setting) {
        return setting != null && setting.getType() != null;
    }

    private LibTorrentProxySetting normalizeProxy(LibTorrentProxySetting proxySetting) {
        return proxySetting != null ? proxySetting : LibTorrentProxySetting.nullProxy();
    }

    private TorrentHandle findHandle(String sha1) {
        if (!isLoaded() || sha1 == null || sha1.length() == 0) {
            return null;
        }

        try {
            TorrentHandle handle = sessionManager.find(new Sha1Hash(sha1));
            return handle != null && handle.isValid() ? handle : null;
        } catch (IllegalArgumentException e) {
            LOG.warnf(e, "Invalid torrent sha1: {0}", sha1);
            return null;
        }
    }

    private SessionHandle sessionHandle() {
        return new SessionHandle(sessionManager.swig());
    }

    private String safeSha1(TorrentHandle handle) {
        return handle != null && handle.isValid() ? safeSha1(handle.infoHash()) : null;
    }

    private String safeSha1(Sha1Hash sha1) {
        return sha1 != null ? sha1.toHex() : null;
    }

    private String emptyString(String value) {
        return value == null ? "" : value;
    }

    private int toLegacyCategory(Alert<?> alert) {
        return alert.category() != null ? alert.category().to_int() : TorrentAlert.status_notification;
    }

    private Priority[] toPriorities(int[] priorities) {
        Priority[] values = new Priority[priorities.length];
        for (int i = 0; i < priorities.length; i++) {
            values[i] = toPriority(priorities[i]);
        }
        return values;
    }

    private Priority toPriority(int priority) {
        int normalized = Math.max(Priority.IGNORE.swig(), Math.min(Priority.SEVEN.swig(), priority));
        return Priority.fromSwig(normalized);
    }

    private void copyStatus(LibTorrentStatus source, LibTorrentStatus target) {
        target.total_done = source.total_done;
        target.total_wanted_done = source.total_wanted_done;
        target.total_wanted = source.total_wanted;
        target.total_download = source.total_download;
        target.total_upload = source.total_upload;
        target.total_payload_download = source.total_payload_download;
        target.total_payload_upload = source.total_payload_upload;
        target.all_time_payload_download = source.all_time_payload_download;
        target.all_time_payload_upload = source.all_time_payload_upload;
        target.download_rate = source.download_rate;
        target.upload_rate = source.upload_rate;
        target.download_payload_rate = source.download_payload_rate;
        target.upload_payload_rate = source.upload_payload_rate;
        target.num_peers = source.num_peers;
        target.num_uploads = source.num_uploads;
        target.num_seeds = source.num_seeds;
        target.num_connections = source.num_connections;
        target.state = source.state;
        target.progress = source.progress;
        target.paused = source.paused;
        target.finished = source.finished;
        target.valid = source.valid;
        target.auto_managed = source.auto_managed;
        target.seeding_time = source.seeding_time;
        target.active_time = source.active_time;
        target.error = source.error;
        target.current_tracker = source.current_tracker;
        target.num_complete = source.num_complete;
        target.num_incomplete = source.num_incomplete;
        target.total_failed_bytes = source.total_failed_bytes;
    }
}
