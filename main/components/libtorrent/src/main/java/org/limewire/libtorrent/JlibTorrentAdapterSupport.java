package org.limewire.libtorrent;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.limewire.bittorrent.ProxySettingType;
import org.limewire.bittorrent.TorrentFileEntry;
import org.limewire.bittorrent.TorrentInfo;
import org.limewire.bittorrent.TorrentManagerSettings;
import org.limewire.bittorrent.TorrentParams;
import org.limewire.bittorrent.TorrentPieceState;
import org.limewire.bittorrent.TorrentPiecesInfo;
import org.limewire.bittorrent.TorrentTracker;
import org.limewire.bittorrent.TorrentState;
import org.limewire.util.URIUtils;

import com.frostwire.jlibtorrent.AddTorrentParams;
import com.frostwire.jlibtorrent.AnnounceEntry;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.InfoHash;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.Sha1Hash;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.swig.add_torrent_params;
import com.frostwire.jlibtorrent.swig.byte_vector;
import com.frostwire.jlibtorrent.swig.error_code;
import com.frostwire.jlibtorrent.swig.info_hash_t;
import com.frostwire.jlibtorrent.swig.peer_info;
import com.frostwire.jlibtorrent.swig.peer_info_vector;
import com.frostwire.jlibtorrent.swig.peer_source_flags_t;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.frostwire.jlibtorrent.swig.torrent_flags_t;

final class JlibTorrentAdapterSupport {

    private JlibTorrentAdapterSupport() {
    }

    static SettingsPack createSettings(TorrentManagerSettings settings, boolean dhtEnabled,
            boolean upnpEnabled, boolean natpmpEnabled, boolean lsdEnabled) {
        SettingsPack pack = new SettingsPack();
        pack.downloadRateLimit(settings.getMaxDownloadBandwidth());
        pack.uploadRateLimit(settings.getMaxUploadBandwidth());
        pack.activeDownloads(settings.getActiveDownloadsLimit());
        pack.activeSeeds(settings.getActiveSeedsLimit());
        pack.activeLimit(settings.getActiveLimit());
        pack.setInteger(settings_pack.int_types.alert_queue_size.swigValue(), 4096);
        pack.setInteger(settings_pack.int_types.max_retry_port_bind.swigValue(),
                Math.max(0, settings.getListenEndPort() - settings.getListenStartPort()));
        pack.setEnableDht(dhtEnabled);
        pack.setBoolean(settings_pack.bool_types.enable_upnp.swigValue(), upnpEnabled);
        pack.setBoolean(settings_pack.bool_types.enable_natpmp.swigValue(), natpmpEnabled);
        pack.setEnableLsd(lsdEnabled);

        String listenHost = settings.getListenInterface();
        if (listenHost == null || listenHost.trim().isEmpty()) {
            pack.listenInterfaces("0.0.0.0:" + settings.getListenStartPort() + ",[::]:"
                    + settings.getListenStartPort());
        } else {
            pack.listenInterfaces(listenHost + ":" + settings.getListenStartPort());
        }

        return pack;
    }

    static AddTorrentParams createAddTorrentParams(TorrentParams params) throws IOException {
        AddTorrentParams addParams = restoreResumeData(params.getFastResumeFile());

        addParams.savePath(params.getDownloadFolder().getAbsolutePath());

        File torrentFile = params.getTorrentFile();
        if (torrentFile != null && torrentFile.exists()) {
            com.frostwire.jlibtorrent.TorrentInfo torrentInfo =
                    new com.frostwire.jlibtorrent.TorrentInfo(torrentFile);
            addParams.torrentInfo(torrentInfo);
        } else {
            info_hash_t infoHashes = new info_hash_t();
            infoHashes.setV1(new Sha1Hash(params.getSha1()).swig());
            addParams.setInfoHashes(new InfoHash(infoHashes));
            if (params.getName() != null) {
                addParams.name(params.getName());
            }
        }

        List<URI> trackers = params.getTrackers();
        if (trackers != null && !trackers.isEmpty()) {
            List<String> trackerUrls = new ArrayList<String>(trackers.size());
            List<Integer> trackerTiers = new ArrayList<Integer>(trackers.size());
            int tier = 0;
            for (URI tracker : trackers) {
                if (tracker != null) {
                    trackerUrls.add(tracker.toASCIIString());
                    trackerTiers.add(tier++);
                }
            }
            if (!trackerUrls.isEmpty()) {
                addParams.trackers(trackerUrls);
                addParams.trackerTiers(trackerTiers);
                addParams.flags(addParams.flags().or_(TorrentFlags.OVERRIDE_TRACKERS));
            }
        }

        return addParams;
    }

    private static AddTorrentParams restoreResumeData(File fastResumeFile) throws IOException {
        if (fastResumeFile == null || !fastResumeFile.exists() || !fastResumeFile.isFile()) {
            return AddTorrentParams.createInstance();
        }

        byte[] resumeBytes = Files.readAllBytes(fastResumeFile.toPath());
        byte_vector buffer = com.frostwire.jlibtorrent.Vectors.bytes2byte_vector(resumeBytes);
        error_code ec = new error_code();
        add_torrent_params nativeParams = add_torrent_params.read_resume_data(buffer, ec);
        if (ec.value() != 0) {
            throw new IOException("Unable to read fast resume data: " + ec.message());
        }
        return new AddTorrentParams(nativeParams);
    }

    static LibTorrentStatus toStatus(com.frostwire.jlibtorrent.TorrentStatus status) {
        LibTorrentStatus value = new LibTorrentStatus();
        value.total_done = status.totalDone();
        value.total_wanted_done = status.totalWantedDone();
        value.total_wanted = status.totalWanted();
        value.total_download = status.totalDownload();
        value.total_upload = status.totalUpload();
        value.total_payload_download = status.totalPayloadDownload();
        value.total_payload_upload = status.totalPayloadUpload();
        value.all_time_payload_download = status.allTimeDownload();
        value.all_time_payload_upload = status.allTimeUpload();
        value.download_rate = status.downloadRate();
        value.upload_rate = status.uploadRate();
        value.download_payload_rate = status.downloadPayloadRate();
        value.upload_payload_rate = status.uploadPayloadRate();
        value.num_peers = status.numPeers();
        value.num_uploads = status.numUploads();
        value.num_seeds = status.numSeeds();
        value.num_connections = status.numConnections();
        value.state = toLegacyState(status.state());
        value.progress = status.progress();
        value.paused = status.flags().and_(TorrentFlags.PAUSED).nonZero() ? 1 : 0;
        value.finished = status.isFinished() ? 1 : 0;
        value.valid = 1;
        value.auto_managed = status.flags().and_(TorrentFlags.AUTO_MANAGED).nonZero() ? 1 : 0;
        value.seeding_time = (int) status.seedingDuration();
        value.active_time = (int) status.activeDuration();
        value.error = status.errorCode().value() != 0 ? status.errorCode().message() : null;
        value.current_tracker = emptyToNull(status.currentTracker());
        value.num_complete = status.numComplete();
        value.num_incomplete = status.numIncomplete();
        value.total_failed_bytes = status.totalFailedBytes();
        return value;
    }

    static TorrentInfo toTorrentInfo(TorrentHandle handle) {
        com.frostwire.jlibtorrent.TorrentInfo info = handle.torrentFile();
        if (info == null) {
            return null;
        }

        FileStorage files = info.files();
        List<TorrentFileEntry> fileEntries = toFileEntries(handle, info);
        List<TorrentTracker> trackers = new ArrayList<TorrentTracker>();
        for (AnnounceEntry tracker : info.trackers()) {
            trackers.add(new LibTorrentAnnounceEntry(tracker.url(), tracker.tier()));
        }

        return new TorrentInfoImpl(info.name(), info.pieceLength(), trackers,
                Collections.<String>emptyList(), fileEntries);
    }

    static LibTorrentFileEntry[] toFileEntries(TorrentHandle handle) {
        com.frostwire.jlibtorrent.TorrentInfo info = handle.torrentFile();
        if (info == null) {
            return new LibTorrentFileEntry[0];
        }
        List<TorrentFileEntry> fileEntries = toFileEntries(handle, info);
        LibTorrentFileEntry[] array = new LibTorrentFileEntry[fileEntries.size()];
        for (int i = 0; i < fileEntries.size(); i++) {
            array[i] = (LibTorrentFileEntry) fileEntries.get(i);
        }
        return array;
    }

    private static List<TorrentFileEntry> toFileEntries(TorrentHandle handle,
            com.frostwire.jlibtorrent.TorrentInfo info) {
        FileStorage files = info.files();
        long[] progress = safeFileProgress(handle);
        Priority[] priorities = safeFilePriorities(handle, files.numFiles());

        List<TorrentFileEntry> result = new ArrayList<TorrentFileEntry>(files.numFiles());
        for (int i = 0; i < files.numFiles(); i++) {
            long totalDone = i < progress.length ? progress[i] : 0;
            int priority = i < priorities.length ? priorities[i].swig() : Priority.NORMAL.swig();
            result.add(new LibTorrentFileEntry(i, files.filePath(i), files.fileSize(i), totalDone,
                    priority));
        }
        return result;
    }

    private static long[] safeFileProgress(TorrentHandle handle) {
        try {
            return handle.fileProgress();
        } catch (RuntimeException ignored) {
            return new long[0];
        }
    }

    private static Priority[] safeFilePriorities(TorrentHandle handle, int numFiles) {
        try {
            return handle.filePriorities();
        } catch (RuntimeException ignored) {
            return Priority.array(Priority.NORMAL, numFiles);
        }
    }

    static LibTorrentPeer[] toPeers(TorrentHandle handle) {
        peer_info_vector peers = new peer_info_vector();
        handle.swig().get_peer_info(peers);
        int size = (int) peers.size();
        List<LibTorrentPeer> values = new ArrayList<LibTorrentPeer>(size);
        for (int i = 0; i < size; i++) {
            peer_info peer = peers.get(i);
            values.add(new LibTorrentPeer(peer.get_flags(),
                    new Sha1Hash(peer.getPid()).toHex(),
                    new com.frostwire.jlibtorrent.TcpEndpoint(peer.getIp()).toString(),
                    peer.get_source(),
                    peer.getUp_speed(),
                    peer.getDown_speed(),
                    peer.getPayload_up_speed(),
                    peer.getPayload_down_speed(),
                    peer.getProgress(),
                    "",
                    com.frostwire.jlibtorrent.Vectors.byte_vector2utf8(peer.get_client())));
        }
        return values.toArray(new LibTorrentPeer[values.size()]);
    }

    static LibTorrentAnnounceEntry[] toTrackers(TorrentHandle handle) {
        List<AnnounceEntry> trackers = handle.trackers();
        LibTorrentAnnounceEntry[] values = new LibTorrentAnnounceEntry[trackers.size()];
        for (int i = 0; i < trackers.size(); i++) {
            AnnounceEntry tracker = trackers.get(i);
            values[i] = new LibTorrentAnnounceEntry(tracker.url(), tracker.tier());
        }
        return values;
    }

    static TorrentPiecesInfo toPiecesInfo(TorrentHandle handle) {
        com.frostwire.jlibtorrent.TorrentStatus status =
                handle.status(TorrentHandle.QUERY_PIECES);
        int numPieces = status.numPieces();
        char[] states = new char[numPieces];
        for (int i = 0; i < numPieces; i++) {
            states[i] = status.pieces().getBit(i) ? 'x' : 'U';
        }

        for (com.frostwire.jlibtorrent.PartialPieceInfo piece : handle.getDownloadQueue()) {
            int pieceIndex = piece.pieceIndex();
            if (pieceIndex >= 0 && pieceIndex < states.length) {
                states[pieceIndex] = piece.requested() > 0 ? 'a'
                        : (piece.writing() > 0 || piece.finished() > 0 ? 'p' : states[pieceIndex]);
            }
        }

        return new LibTorrentPiecesInfo(new String(states), status.numPieces());
    }

    static TorrentState toTorrentState(com.frostwire.jlibtorrent.TorrentStatus.State state) {
        switch (state) {
        case CHECKING_FILES:
            return TorrentState.CHECKING_FILES;
        case DOWNLOADING_METADATA:
            return TorrentState.DOWNLOADING_METADATA;
        case DOWNLOADING:
            return TorrentState.DOWNLOADING;
        case FINISHED:
            return TorrentState.FINISHED;
        case SEEDING:
            return TorrentState.SEEDING;
        case CHECKING_RESUME_DATA:
        case UNKNOWN:
        default:
            return TorrentState.QUEUED_FOR_CHECKING;
        }
    }

    private static int toLegacyState(com.frostwire.jlibtorrent.TorrentStatus.State state) {
        switch (toTorrentState(state)) {
        case QUEUED_FOR_CHECKING:
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
        case ALLOCATING:
        default:
            return LibTorrentState.QUEUED_FOR_CHECKING.getId();
        }
    }

    static URI toUri(String value) {
        try {
            return URIUtils.toURI(value);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    static int toProxyType(ProxySettingType type) {
        if (type == null) {
            return settings_pack.proxy_type_t.none.swigValue();
        }

        switch (type) {
        case SOCKS4:
            return settings_pack.proxy_type_t.socks4.swigValue();
        case SOCKS5:
            return settings_pack.proxy_type_t.socks5.swigValue();
        case SOCKS5_PW:
            return settings_pack.proxy_type_t.socks5_pw.swigValue();
        case HTTP:
            return settings_pack.proxy_type_t.http.swigValue();
        case HTTP_PW:
            return settings_pack.proxy_type_t.http_pw.swigValue();
        default:
            return settings_pack.proxy_type_t.none.swigValue();
        }
    }

    static boolean hasSource(byte source, peer_source_flags_t flag) {
        return (source & flag.to_int()) != 0;
    }

    private static String emptyToNull(String value) {
        return value == null || value.length() == 0 ? null : value;
    }
}
