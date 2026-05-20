package org.limewire.core.impl.search.frostwire;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.limewire.core.api.URN;
import org.limewire.core.api.download.DownloadException;
import org.limewire.core.api.download.DownloadItem;
import org.limewire.core.api.download.DownloadListManager;
import org.limewire.core.api.search.Search;
import org.limewire.core.api.search.SearchResult;
import org.limewire.core.impl.download.DownloadItemFactory;
import org.limewire.core.impl.download.DownloadItemFactoryRegistry;
import org.limewire.core.impl.magnet.MagnetLinkImpl;
import org.limewire.inject.EagerSingleton;
import org.limewire.search.frostwire.WireShareFrostWireSearchResult;
import org.limewire.util.Base32;
import org.limewire.util.StringUtils;
import org.limewire.util.URIUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.limegroup.gnutella.browser.MagnetOptions;

@EagerSingleton
public final class FrostWireSearchDownloadItemFactory implements DownloadItemFactory {
    private final Provider<DownloadListManager> downloadListManager;

    @Inject
    public FrostWireSearchDownloadItemFactory(DownloadItemFactoryRegistry registry, Provider<DownloadListManager> downloadListManager) {
        this.downloadListManager = downloadListManager;
        registry.register(this);
    }

    @Override
    public DownloadItem create(Search search, List<? extends SearchResult> searchResults, File saveFile, boolean overwrite)
            throws DownloadException {
        WireShareFrostWireSearchResult result = firstFrostWireResult(searchResults);
        if (result == null) {
            return null;
        }

        if (result.isTorrentResult()) {
            return createTorrentDownload(result, overwrite);
        }

        if (result.isPreliminary()) {
            throw new DownloadException(
                    DownloadException.ErrorCode.DOWNLOAD_NOT_SUPPORTED,
                    null,
                    "This FrostWire provider result needs a resolver/helper before it can be downloaded.");
        }

        String downloadUrl = result.getDownloadUrl();
        if (downloadUrl == null || downloadUrl.trim().isEmpty()) {
            throw unsupported("This FrostWire provider result does not include a downloadable URL.");
        }
        MagnetOptions magnet = MagnetOptions.createMagnet(
                result.getFileNameWithoutExtension(),
                result.getFileName(),
                null,
                new String[] { downloadUrl.trim() });
        return downloadListManager.get().addDownload(new MagnetLinkImpl(magnet), saveFile, overwrite);
    }

    private static WireShareFrostWireSearchResult firstFrostWireResult(List<? extends SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return null;
        }
        for (SearchResult searchResult : searchResults) {
            if (searchResult instanceof WireShareFrostWireSearchResult) {
                return (WireShareFrostWireSearchResult) searchResult;
            }
        }
        return null;
    }

    private DownloadItem createTorrentDownload(WireShareFrostWireSearchResult result, boolean overwrite) throws DownloadException {
        String torrentUrl = result.getTorrentUrl();
        if (torrentUrl == null || torrentUrl.trim().isEmpty()) {
            throw unsupported("This FrostWire torrent result does not include a downloadable torrent or magnet link.");
        }
        String trimmed = torrentUrl.trim();
        if (trimmed.startsWith("magnet:")) {
            URN infoHash = urnFromInfoHash(firstNonBlank(result.getTorrentHash(), extractBtih(trimmed)));
            if (infoHash == null) {
                throw unsupported("This FrostWire magnet result does not include a usable BitTorrent info hash.");
            }
            return downloadListManager.get().addTorrentDownload(result.getFileNameWithoutExtension(), infoHash, extractTrackers(trimmed));
        }
        try {
            return downloadListManager.get().addTorrentDownload(new URI(trimmed), overwrite);
        } catch (URISyntaxException e) {
            throw unsupported("This FrostWire torrent result has an invalid torrent URL: " + e.getMessage());
        }
    }

    private static DownloadException unsupported(String message) {
        return new DownloadException(DownloadException.ErrorCode.DOWNLOAD_NOT_SUPPORTED, null, message);
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && first.trim().length() > 0 ? first.trim() : second;
    }

    private static URN urnFromInfoHash(String hash) {
        byte[] bytes = infoHashBytes(hash);
        if (bytes == null) {
            return null;
        }
        try {
            return com.limegroup.gnutella.URN.createSHA1UrnFromBytes(bytes);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static byte[] infoHashBytes(String hash) {
        if (hash == null) {
            return null;
        }
        String normalized = hash.trim();
        if (normalized.length() == 40 && normalized.matches("[0-9a-fA-F]+")) {
            return StringUtils.fromHexString(normalized);
        }
        if (normalized.length() == 32) {
            return Base32.decode(normalized);
        }
        return null;
    }

    private static String extractBtih(String magnet) {
        for (String part : magnetParts(magnet)) {
            int equals = part.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String key = part.substring(0, equals);
            if (!"xt".equalsIgnoreCase(key)) {
                continue;
            }
            String value = decode(part.substring(equals + 1));
            String prefix = "urn:btih:";
            int index = value.toLowerCase().indexOf(prefix);
            if (index >= 0) {
                return value.substring(index + prefix.length());
            }
        }
        return null;
    }

    private static List<URI> extractTrackers(String magnet) {
        List<URI> trackers = new ArrayList<URI>();
        for (String part : magnetParts(magnet)) {
            int equals = part.indexOf('=');
            if (equals <= 0 || !"tr".equalsIgnoreCase(part.substring(0, equals))) {
                continue;
            }
            String tracker = decode(part.substring(equals + 1));
            try {
                trackers.add(URIUtils.toURI(tracker));
            } catch (URISyntaxException ignored) {
            }
        }
        return trackers;
    }

    private static List<String> magnetParts(String magnet) {
        String query = magnet;
        int question = magnet.indexOf('?');
        if (question >= 0 && question + 1 < magnet.length()) {
            query = magnet.substring(question + 1);
        }
        List<String> parts = new ArrayList<String>();
        for (String part : query.split("&")) {
            parts.add(part);
        }
        return parts;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
