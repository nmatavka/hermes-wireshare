package org.limewire.search.frostwire;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.limewire.core.api.Category;
import org.limewire.core.api.FilePropertyKey;
import org.limewire.core.api.URN;
import org.limewire.core.api.endpoint.RemoteHost;
import org.limewire.core.api.search.SearchResult;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.IncomingChatListener;
import org.limewire.friend.api.MessageReader;
import org.limewire.friend.api.MessageWriter;
import org.limewire.friend.api.Network;
import org.limewire.friend.api.PresenceEvent;
import org.limewire.friend.api.feature.Feature;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.friend.api.feature.ReferrerFeature;
import org.limewire.listener.EventListener;
import org.limewire.search.frostwire.licenses.Licenses;
import org.limewire.search.frostwire.search.CompositeFileSearchResult;
import org.limewire.search.frostwire.search.FileSearchResult;
import org.limewire.search.frostwire.search.HttpSearchResult;
import org.limewire.search.frostwire.search.StreamableSearchResult;
import org.limewire.search.frostwire.search.torrent.TorrentSearchResult;
import org.limewire.util.Base32;

public final class WireShareFrostWireSearchResult implements SearchResult {
    private final WireShareFrostWireSearchProvider provider;
    private final FileSearchResult delegate;
    private final URN urn;
    private final RemoteHost source;

    WireShareFrostWireSearchResult(WireShareFrostWireSearchProvider provider, FileSearchResult delegate) {
        this.provider = provider;
        this.delegate = delegate;
        this.urn = createUrn(delegate);
        this.source = new FrostWireRemoteHost(sourceName(), detailsUri());
    }

    public WireShareFrostWireSearchProvider getProvider() {
        return provider;
    }

    public String getTorrentUrl() {
        if (delegate instanceof TorrentSearchResult) {
            return ((TorrentSearchResult) delegate).getTorrentUrl();
        }
        if (delegate instanceof CompositeFileSearchResult) {
            return ((CompositeFileSearchResult) delegate).getTorrentUrl().orElse(null);
        }
        return null;
    }

    public String getTorrentHash() {
        if (delegate instanceof TorrentSearchResult) {
            return ((TorrentSearchResult) delegate).getHash();
        }
        if (delegate instanceof CompositeFileSearchResult) {
            return ((CompositeFileSearchResult) delegate).getTorrentHash().orElse(null);
        }
        return null;
    }

    public String getReferrerUrl() {
        if (delegate instanceof TorrentSearchResult) {
            return ((TorrentSearchResult) delegate).getReferrerUrl();
        }
        if (delegate instanceof CompositeFileSearchResult) {
            return ((CompositeFileSearchResult) delegate).getReferrerUrl().orElse(null);
        }
        return delegate.getDetailsUrl();
    }

    public String getDownloadUrl() {
        if (delegate instanceof HttpSearchResult) {
            return ((HttpSearchResult) delegate).getDownloadUrl();
        }
        if (delegate instanceof StreamableSearchResult) {
            return ((StreamableSearchResult) delegate).getStreamUrl();
        }
        if (delegate instanceof CompositeFileSearchResult) {
            CompositeFileSearchResult composite = (CompositeFileSearchResult) delegate;
            return composite.getStreamUrl().orElse(null);
        }
        return null;
    }

    public boolean isTorrentResult() {
        return getTorrentUrl() != null;
    }

    public boolean isPreliminary() {
        return delegate.isPreliminary();
    }

    public String sourceName() {
        String source = delegate.getSource();
        if (source == null || source.trim().isEmpty()) {
            return provider.displayName();
        }
        return source.trim();
    }

    @Override
    public String getFileExtension() {
        return FilenameUtils.getExtension(getFileName());
    }

    @Override
    public Object getProperty(FilePropertyKey key) {
        switch (key) {
        case FILE_SIZE:
            return getSize();
        case NAME:
        case TITLE:
            return delegate.getDisplayName();
        case REFERRER:
            return detailsUri();
        case USERAGENT:
            return "FrostWire:" + sourceName();
        case DATE_CREATED:
            return delegate.getCreationTime();
        default:
            return null;
        }
    }

    @Override
    public Category getCategory() {
        if (isTorrentResult() || "torrent".equalsIgnoreCase(getFileExtension())) {
            return Category.TORRENT;
        }
        String ext = getFileExtension().toLowerCase();
        if (isOneOf(ext, "mp3", "m4a", "aac", "flac", "ogg", "opus", "wav", "wma", "aiff")) {
            return Category.AUDIO;
        }
        if (isOneOf(ext, "mp4", "m4v", "mkv", "mov", "avi", "webm", "wmv", "flv", "mpeg", "mpg", "3gp")) {
            return Category.VIDEO;
        }
        if (isOneOf(ext, "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif", "tif", "tiff")) {
            return Category.IMAGE;
        }
        if (isOneOf(ext, "pdf", "txt", "md", "doc", "docx", "epub", "mobi", "rtf", "odt")) {
            return Category.DOCUMENT;
        }
        if (isOneOf(ext, "exe", "msi", "dmg", "pkg", "app", "apk", "jar", "sh", "bat", "command")) {
            return Category.PROGRAM;
        }
        return Category.OTHER;
    }

    @Override
    public long getSize() {
        return Math.max(0, delegate.getSize());
    }

    @Override
    public RemoteHost getSource() {
        return source;
    }

    @Override
    public URN getUrn() {
        return urn;
    }

    @Override
    public boolean isSpam() {
        return false;
    }

    @Override
    public String getFileName() {
        String filename = delegate.getFilename();
        if (filename == null || filename.trim().isEmpty()) {
            filename = delegate.getDisplayName();
        }
        if (filename == null || filename.trim().isEmpty()) {
            filename = sourceName();
        }
        return filename.trim();
    }

    @Override
    public String getFileNameWithoutExtension() {
        return FilenameUtils.getBaseName(getFileName());
    }

    @Override
    public String getMagnetURL() {
        String torrentUrl = getTorrentUrl();
        if (torrentUrl != null && torrentUrl.startsWith("magnet:")) {
            return torrentUrl;
        }
        String downloadUrl = getDownloadUrl();
        return downloadUrl != null ? downloadUrl : torrentUrl;
    }

    @Override
    public float getRelevance(String query) {
        String name = getFileName().toLowerCase();
        String normalizedQuery = query == null ? "" : query.toLowerCase();
        return normalizedQuery.isEmpty() || name.contains(normalizedQuery) ? 1.0f : 0.5f;
    }

    @Override
    public boolean isLicensed() {
        return delegate.getLicense() != null && !Licenses.UNKNOWN.equals(delegate.getLicense());
    }

    private URI detailsUri() {
        String url = delegate.getDetailsUrl();
        if (url == null || url.trim().isEmpty()) {
            url = getDownloadUrl();
        }
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        try {
            return new URI(url.trim());
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    private static boolean isOneOf(String value, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static URN createUrn(FileSearchResult result) {
        String hash = torrentHash(result);
        if (hash != null) {
            byte[] bytes = infoHashBytes(hash);
            if (bytes != null) {
                try {
                    return com.limegroup.gnutella.URN.createSHA1UrnFromBytes(bytes);
                } catch (IOException ignored) {
                }
            }
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            String stable = result.getSource() + "|" + result.getDetailsUrl() + "|" + result.getFilename() + "|" + result.getSize();
            return com.limegroup.gnutella.URN.createSHA1UrnFromBytes(digest.digest(stable.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("Could not create stable FrostWire search URN", e);
        }
    }

    private static String torrentHash(FileSearchResult result) {
        if (result instanceof TorrentSearchResult) {
            return ((TorrentSearchResult) result).getHash();
        }
        if (result instanceof CompositeFileSearchResult) {
            return ((CompositeFileSearchResult) result).getTorrentHash().orElse(null);
        }
        return null;
    }

    private static byte[] infoHashBytes(String hash) {
        if (hash == null) {
            return null;
        }
        String normalized = hash.trim();
        if (normalized.length() == 40 && normalized.matches("[0-9a-fA-F]+")) {
            return org.limewire.util.StringUtils.fromHexString(normalized);
        }
        if (normalized.length() == 32) {
            return Base32.decode(normalized);
        }
        return null;
    }

    private static final class FrostWireRemoteHost implements RemoteHost {
        private final FriendPresence presence;

        FrostWireRemoteHost(String sourceName, URI referrer) {
            this.presence = new FrostWirePresence(sourceName, referrer);
        }

        @Override
        public boolean isBrowseHostEnabled() {
            return false;
        }

        @Override
        public boolean isChatEnabled() {
            return false;
        }

        @Override
        public boolean isSharingEnabled() {
            return false;
        }

        @Override
        public FriendPresence getFriendPresence() {
            return presence;
        }
    }

    private static final class FrostWirePresence implements FriendPresence {
        private final Friend friend;
        private final URI referrer;

        FrostWirePresence(String sourceName, URI referrer) {
            this.friend = new FrostWireFriend(sourceName);
            this.referrer = referrer;
        }

        @Override
        public Friend getFriend() {
            return friend;
        }

        @Override
        public String getPresenceId() {
            return friend.getId();
        }

        @Override
        public Collection<Feature> getFeatures() {
            return Collections.emptyList();
        }

        @Override
        public Feature getFeature(URI id) {
            if (referrer != null && ReferrerFeature.ID.equals(id)) {
                return new ReferrerFeature(referrer);
            }
            return null;
        }

        @Override
        public boolean hasFeatures(URI... ids) {
            if (ids == null) {
                return false;
            }
            for (URI id : ids) {
                if (getFeature(id) == null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void addFeature(Feature feature) {
        }

        @Override
        public void removeFeature(URI id) {
        }

        @Override
        public <F extends Feature<D>, D> FeatureTransport<D> getTransport(Class<F> feature) {
            return null;
        }

        @Override
        public <D, F extends Feature<D>> void addTransport(Class<F> clazz, FeatureTransport<D> transport) {
        }

        @Override
        public Type getType() {
            return Type.available;
        }

        @Override
        public String getStatus() {
            return "FrostWire search";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public Mode getMode() {
            return Mode.available;
        }
    }

    private static final class FrostWireFriend implements Friend {
        private final String sourceName;
        private final Network network = new FrostWireNetwork();

        FrostWireFriend(String sourceName) {
            this.sourceName = sourceName == null || sourceName.trim().isEmpty() ? "FrostWire Search" : sourceName.trim();
        }

        @Override
        public String getId() {
            return "frostwire-search:" + sourceName;
        }

        @Override
        public String getName() {
            return sourceName;
        }

        @Override
        public String getRenderName() {
            return sourceName;
        }

        @Override
        public String getFirstName() {
            return sourceName;
        }

        @Override
        public void setName(String name) {
        }

        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public Network getNetwork() {
            return network;
        }

        @Override
        public void addPresenceListener(EventListener<PresenceEvent> presenceListener) {
        }

        @Override
        public MessageWriter createChat(MessageReader reader) {
            return null;
        }

        @Override
        public void setChatListenerIfNecessary(IncomingChatListener listener) {
        }

        @Override
        public void removeChatListener() {
        }

        @Override
        public FriendPresence getActivePresence() {
            return null;
        }

        @Override
        public boolean hasActivePresence() {
            return false;
        }

        @Override
        public boolean isSignedIn() {
            return false;
        }

        @Override
        public Map<String, FriendPresence> getPresences() {
            return Collections.emptyMap();
        }

        @Override
        public boolean isSubscribed() {
            return false;
        }
    }

    private static final class FrostWireNetwork implements Network {
        @Override
        public String getCanonicalizedLocalID() {
            return "frostwire-search";
        }

        @Override
        public String getNetworkName() {
            return "FrostWire Search";
        }

        @Override
        public Type getType() {
            return Type.WEBSEARCH;
        }
    }
}
