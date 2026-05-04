package org.limewire.ui.swing.friends.chat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.limewire.concurrent.FutureEvent;
import org.limewire.concurrent.ListeningFuture;
import org.limewire.core.api.download.DownloadException;
import org.limewire.core.api.download.DownloadItem;
import org.limewire.core.api.download.DownloadState;
import org.limewire.core.api.download.ResultDownloader;
import org.limewire.core.api.friend.FileMetaDataConverter;
import org.limewire.core.api.library.LibraryManager;
import org.limewire.core.api.library.LocalFileItem;
import org.limewire.core.api.library.LocalFileList;
import org.limewire.core.api.search.SearchResult;
import org.limewire.friend.api.ChatState;
import org.limewire.friend.api.FileMetaData;
import org.limewire.friend.api.FileOffer;
import org.limewire.friend.api.FileOfferEvent;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendConnection;
import org.limewire.friend.api.FriendConnectionEvent;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.FriendPresenceEvent;
import org.limewire.friend.api.MessageWriter;
import org.limewire.friend.api.feature.FeatureTransport;
import org.limewire.friend.api.feature.FileOfferFeature;
import org.limewire.friend.impl.feature.NoSave;
import org.limewire.friend.impl.feature.NoSaveFeature;
import org.limewire.friend.impl.feature.NoSaveStatus;
import org.limewire.io.InvalidDataException;
import org.limewire.listener.EventBean;
import org.limewire.listener.EventBroadcaster;
import org.limewire.listener.EventListener;
import org.limewire.listener.ListenerSupport;
import org.limewire.listener.SwingEDTEvent;
import org.limewire.ui.swing.util.I18n;

import com.google.inject.Inject;

import ca.odell.glazedlists.EventList;

/**
 * Compose-facing bridge for the package-private Swing chat model. This exposes
 * friend and message state without forcing the Compose desktop app to open the
 * legacy chat window.
 */
public class ComposeChatBridge {

    public enum MessageKind {
        TEXT,
        FILE_OFFER,
        STATUS
    }

    public interface Listener {
        void connectionChanged(FriendConnectionEvent event);
        void messageReceived(ComposeChatMessage message);
        void chatStateChanged(String friendId, ChatState state);
        void conversationReady(ChatFriend friend);
    }

    public static final class ComposeFileOffer {
        private final String offerId;
        private final String fileName;
        private final long size;
        private final String description;
        private final List<String> urns;
        private final DownloadState downloadState;
        private final String localPath;

        ComposeFileOffer(String offerId,
                String fileName,
                long size,
                String description,
                List<String> urns,
                DownloadState downloadState,
                String localPath) {
            this.offerId = offerId;
            this.fileName = fileName;
            this.size = size;
            this.description = description;
            this.urns = urns;
            this.downloadState = downloadState;
            this.localPath = localPath;
        }

        public String getOfferId() {
            return offerId;
        }

        public String getFileName() {
            return fileName;
        }

        public long getSize() {
            return size;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getUrns() {
            return urns;
        }

        public DownloadState getDownloadState() {
            return downloadState;
        }

        public String getLocalPath() {
            return localPath;
        }
    }

    public static final class ComposeChatMessage {
        private final String id;
        private final String friendId;
        private final String senderName;
        private final String body;
        private final long timestamp;
        private final Message.Type type;
        private final MessageKind kind;
        private final ComposeFileOffer fileOffer;

        ComposeChatMessage(Message message) {
            this.id = textMessageId(message);
            this.friendId = message.getFriendID();
            this.senderName = message.getSenderName();
            this.body = message.toString();
            this.timestamp = message.getMessageTimeMillis();
            this.type = message.getType();
            this.kind = message instanceof NoSaveStatusMessage ? MessageKind.STATUS : MessageKind.TEXT;
            this.fileOffer = null;
        }

        ComposeChatMessage(TrackedFileOffer fileOfferMessage) {
            this.id = fileOfferMessage.messageId;
            this.friendId = fileOfferMessage.friendId;
            this.senderName = fileOfferMessage.senderName;
            this.body = fileOfferMessage.fileOffer.getName();
            this.timestamp = fileOfferMessage.timestamp;
            this.type = fileOfferMessage.type;
            this.kind = MessageKind.FILE_OFFER;
            this.fileOffer = new ComposeFileOffer(
                    fileOfferMessage.fileOffer.getId(),
                    fileOfferMessage.fileOffer.getName(),
                    fileOfferMessage.fileOffer.getSize(),
                    fileOfferMessage.fileOffer.getDescription(),
                    new ArrayList<String>(fileOfferMessage.fileOffer.getUrns()),
                    fileOfferMessage.downloadState,
                    fileOfferMessage.localPath);
        }

        ComposeChatMessage(String id, String friendId, String senderName, String body, long timestamp, Message.Type type, MessageKind kind) {
            this.id = id;
            this.friendId = friendId;
            this.senderName = senderName;
            this.body = body;
            this.timestamp = timestamp;
            this.type = type;
            this.kind = kind;
            this.fileOffer = null;
        }

        public String getId() {
            return id;
        }

        public String getFriendId() {
            return friendId;
        }

        public String getSenderName() {
            return senderName;
        }

        public String getBody() {
            return body;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public MessageKind getKind() {
            return kind;
        }

        public ComposeFileOffer getFileOffer() {
            return fileOffer;
        }

        public boolean isIncoming() {
            return type == Message.Type.RECEIVED;
        }

        public boolean isOutgoing() {
            return type == Message.Type.SENT;
        }

        public boolean isServer() {
            return type == Message.Type.SERVER;
        }
    }

    private final ChatModel chatModel;
    private final EventBroadcaster<ChatMessageEvent> chatMessageBroadcaster;
    private final EventBroadcaster<ChatStateEvent> chatStateBroadcaster;
    private final ListenerSupport<ChatMessageEvent> chatMessageSupport;
    private final ListenerSupport<ChatStateEvent> chatStateSupport;
    private final ListenerSupport<FriendConnectionEvent> connectionSupport;
    private final ListenerSupport<FileOfferEvent> fileOfferSupport;
    private final EventBean<FriendConnectionEvent> connectionEventBean;
    private final ResultDownloader resultDownloader;
    private final FileMetaDataConverter fileMetaDataConverter;
    private final LibraryManager libraryManager;

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private final Map<String, MessageWriter> writers = new ConcurrentHashMap<String, MessageWriter>();
    private final Map<String, TrackedFileOffer> trackedFileOffers = new ConcurrentHashMap<String, TrackedFileOffer>();
    private final Map<String, NoSave> noSaveStates = new ConcurrentHashMap<String, NoSave>();

    private final IncomingListener incomingListener = new IncomingListener() {
        @Override
        public void incomingChat(ChatFriend chatFriend, MessageWriter messageWriter) {
            chatFriend.startChat();
            writers.put(chatFriend.getID(), messageWriter);
            for (Listener listener : listeners) {
                listener.conversationReady(chatFriend);
            }
        }
    };

    private final EventListener<ChatMessageEvent> messageListener = new EventListener<ChatMessageEvent>() {
        @Override
        @SwingEDTEvent
        public void handleEvent(ChatMessageEvent event) {
            handleMessage(event.getData());
        }
    };

    private final EventListener<ChatStateEvent> stateListener = new EventListener<ChatStateEvent>() {
        @Override
        @SwingEDTEvent
        public void handleEvent(ChatStateEvent event) {
            for (Listener listener : listeners) {
                listener.chatStateChanged(event.getSource(), event.getType());
            }
        }
    };

    private final EventListener<FriendConnectionEvent> connectionListener = new EventListener<FriendConnectionEvent>() {
        @Override
        @SwingEDTEvent
        public void handleEvent(FriendConnectionEvent event) {
            switch (event.getType()) {
            case CONNECTED:
                onConnected(event.getSource());
                break;
            case DISCONNECTED:
                onDisconnected();
                break;
            case CONNECT_FAILED:
                writers.clear();
                break;
            case CONNECTING:
                break;
            }
            for (Listener listener : listeners) {
                listener.connectionChanged(event);
            }
        }
    };

    private final EventListener<FileOfferEvent> fileOfferListener = new EventListener<FileOfferEvent>() {
        @Override
        @SwingEDTEvent
        public void handleEvent(FileOfferEvent event) {
            if (event.getType() != FileOfferEvent.Type.OFFER) {
                return;
            }
            handleIncomingFileOffer(event.getData());
        }
    };

    private boolean active;

    @Inject
    public ComposeChatBridge(ChatModel chatModel,
            EventBroadcaster<ChatMessageEvent> chatMessageBroadcaster,
            EventBroadcaster<ChatStateEvent> chatStateBroadcaster,
            ListenerSupport<ChatMessageEvent> chatMessageSupport,
            ListenerSupport<ChatStateEvent> chatStateSupport,
            ListenerSupport<FriendConnectionEvent> connectionSupport,
            ListenerSupport<FileOfferEvent> fileOfferSupport,
            EventBean<FriendConnectionEvent> connectionEventBean,
            ResultDownloader resultDownloader,
            FileMetaDataConverter fileMetaDataConverter,
            LibraryManager libraryManager) {
        this.chatModel = chatModel;
        this.chatMessageBroadcaster = chatMessageBroadcaster;
        this.chatStateBroadcaster = chatStateBroadcaster;
        this.chatMessageSupport = chatMessageSupport;
        this.chatStateSupport = chatStateSupport;
        this.connectionSupport = connectionSupport;
        this.fileOfferSupport = fileOfferSupport;
        this.connectionEventBean = connectionEventBean;
        this.resultDownloader = resultDownloader;
        this.fileMetaDataConverter = fileMetaDataConverter;
        this.libraryManager = libraryManager;
    }

    public void activate() {
        if (active) {
            return;
        }
        active = true;
        connectionSupport.addListener(connectionListener);
        chatMessageSupport.addListener(messageListener);
        chatStateSupport.addListener(stateListener);
        fileOfferSupport.addListener(fileOfferListener);

        FriendConnectionEvent lastEvent = connectionEventBean.getLastEvent();
        if (lastEvent != null && lastEvent.getType() == FriendConnectionEvent.Type.CONNECTED) {
            onConnected(lastEvent.getSource());
        }
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        connectionSupport.removeListener(connectionListener);
        chatMessageSupport.removeListener(messageListener);
        chatStateSupport.removeListener(stateListener);
        fileOfferSupport.removeListener(fileOfferListener);
        onDisconnected();
    }

    public EventList<ChatFriend> getFriends() {
        return chatModel.getChatFriendList();
    }

    public ChatFriend getFriend(String friendId) {
        return chatModel.getChatFriend(friendId);
    }

    public void addListener(Listener listener) {
        listeners.addIfAbsent(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public FriendConnectionEvent getLastConnectionEvent() {
        return connectionEventBean.getLastEvent();
    }

    public FriendConnection getCurrentConnection() {
        FriendConnectionEvent event = connectionEventBean.getLastEvent();
        return event != null ? event.getSource() : null;
    }

    public MessageWriter ensureConversation(ChatFriend chatFriend) {
        MessageWriter writer = writers.get(chatFriend.getID());
        if (writer != null) {
            return writer;
        }

        chatFriend.startChat();
        MessageWriter createdWriter = new MessageWriterImpl(
                chatFriend,
                chatFriend.createChat(new MessageReaderImpl(chatFriend, chatMessageBroadcaster, chatStateBroadcaster)),
                chatMessageBroadcaster);
        writers.put(chatFriend.getID(), createdWriter);
        return createdWriter;
    }

    public void closeConversation(ChatFriend chatFriend) {
        writers.remove(chatFriend.getID());
        chatFriend.stopChat();
        chatModel.removeFriendIfNecessary(chatFriend);
    }

    public boolean supportsBrowse(String friendId) {
        ChatFriend friend = chatModel.getChatFriend(friendId);
        return friend != null && friend.isSignedInToLimewire();
    }

    public void setChatState(String friendId, ChatState state) throws FriendException {
        ChatFriend chatFriend = chatModel.getChatFriend(friendId);
        if (chatFriend == null || state == null) {
            return;
        }
        ensureConversation(chatFriend).setChatState(state);
    }

    public boolean supportsOffTheRecord(String friendId) {
        return getNoSaveStatus(friendId) != null;
    }

    public boolean isOffTheRecord(String friendId) {
        NoSaveStatus status = getNoSaveStatus(friendId);
        return status != null && status.getStatus() == NoSave.ENABLED;
    }

    public void toggleOffTheRecord(String friendId) throws FriendException {
        NoSaveStatus status = getNoSaveStatus(friendId);
        if (status == null) {
            return;
        }
        NoSave current = status.getStatus();
        status.toggleStatus();
        NoSave updated = current == NoSave.ENABLED ? NoSave.DISABLED : NoSave.ENABLED;
        noSaveStates.put(friendId, updated);
        emitMessage(new ComposeChatMessage(
                "status:" + friendId + ":" + System.currentTimeMillis(),
                friendId,
                "chat server",
                noSaveMessage(updated),
                System.currentTimeMillis(),
                Message.Type.SERVER,
                MessageKind.STATUS));
    }

    public boolean supportsFileOffers(String friendId) {
        ChatFriend friend = chatModel.getChatFriend(friendId);
        return friend != null && !findFileOfferCapablePresences(friend.getFriend()).isEmpty();
    }

    public void offerFile(String friendId, File file) {
        ChatFriend chatFriend = chatModel.getChatFriend(friendId);
        if (chatFriend == null || file == null) {
            return;
        }
        LocalFileList fileList = libraryManager.getLibraryManagedList();
        if (!fileList.isFileAllowed(file)) {
            emitStatusMessage(friendId, I18n.tr("WireShare could not share that file."));
            return;
        }
        offerFile(chatFriend, fileList.addFile(file));
    }

    public void offerFolder(String friendId, File folder) {
        ChatFriend chatFriend = chatModel.getChatFriend(friendId);
        if (chatFriend == null || folder == null) {
            return;
        }
        final LocalFileList fileList = libraryManager.getLibraryManagedList();
        if (!fileList.isDirectoryAllowed(folder)) {
            emitStatusMessage(friendId, I18n.tr("WireShare could not share that folder."));
            return;
        }
        offerFolder(chatFriend, fileList.addFolder(folder, new java.io.FileFilter() {
            @Override
            public boolean accept(File candidate) {
                return candidate.isDirectory() || fileList.isFileAllowed(candidate);
            }
        }));
    }

    public void downloadFileOffer(String messageId) throws DownloadException, InvalidDataException {
        TrackedFileOffer tracked = trackedFileOffers.get(messageId);
        if (tracked == null || tracked.presence == null) {
            return;
        }
        SearchResult searchResult = fileMetaDataConverter.create(tracked.presence, tracked.fileOffer);
        DownloadItem download = resultDownloader.addDownload(null, Collections.singletonList(searchResult));
        trackDownload(tracked, download);
    }

    private void onConnected(FriendConnection connection) {
        chatModel.registerListeners();
        chatModel.addIncomingListener(incomingListener);
        populateExistingFriends(connection);
    }

    private void onDisconnected() {
        writers.clear();
        trackedFileOffers.clear();
        noSaveStates.clear();
        chatModel.removeIncomingListener(incomingListener);
        chatModel.unregisterListeners();
    }

    private void populateExistingFriends(FriendConnection connection) {
        if (connection == null) {
            return;
        }

        Collection<Friend> friends = connection.getFriends();
        if (friends == null) {
            return;
        }

        for (Friend friend : friends) {
            for (FriendPresence presence : friend.getPresences().values()) {
                chatModel.handlePresenceEvent(new FriendPresenceEvent(presence, FriendPresenceEvent.Type.ADDED));
            }
            NoSaveStatus status = getNoSaveStatus(friend.getId());
            if (status != null) {
                noSaveStates.put(friend.getId(), status.getStatus());
            }
        }
    }

    private void handleMessage(Message message) {
        if (message instanceof MessageFileOffer) {
            TrackedFileOffer tracked = trackFileOffer((MessageFileOffer) message);
            emitMessage(new ComposeChatMessage(tracked));
            return;
        }

        if (message instanceof NoSaveStatusMessage) {
            noSaveStates.put(message.getFriendID(), ((NoSaveStatusMessage) message).getStatus());
        }

        emitMessage(new ComposeChatMessage(message));
    }

    private void emitMessage(ComposeChatMessage message) {
        for (Listener listener : listeners) {
            listener.messageReceived(message);
        }
    }

    private void emitStatusMessage(String friendId, String body) {
        emitMessage(new ComposeChatMessage(
                "status:" + friendId + ":" + System.currentTimeMillis(),
                friendId,
                "chat server",
                body,
                System.currentTimeMillis(),
                Message.Type.SERVER,
                MessageKind.STATUS));
    }

    private void handleIncomingFileOffer(FileOffer offer) {
        FriendPresence presence = findPresenceById(offer.getFromJID());
        Friend friend = presence != null ? presence.getFriend() : findFriendByPresenceId(offer.getFromJID());
        if (friend == null) {
            return;
        }

        ChatFriend chatFriend = chatModel.getChatFriend(friend.getId());
        if (chatFriend != null) {
            chatFriend.startChat();
            for (Listener listener : listeners) {
                listener.conversationReady(chatFriend);
            }
        }

        TrackedFileOffer tracked = new TrackedFileOffer(
                fileOfferMessageId(friend.getId(), offer.getFile(), Message.Type.RECEIVED),
                friend.getId(),
                friend.getRenderName(),
                System.currentTimeMillis(),
                Message.Type.RECEIVED,
                offer.getFile(),
                presence);
        trackedFileOffers.put(tracked.messageId, tracked);
        emitMessage(new ComposeChatMessage(tracked));
    }

    private void offerFolder(final ChatFriend chatFriend, ListeningFuture<List<ListeningFuture<LocalFileItem>>> future) {
        future.addFutureListener(new EventListener<FutureEvent<List<ListeningFuture<LocalFileItem>>>>() {
            @Override
            public void handleEvent(FutureEvent<List<ListeningFuture<LocalFileItem>>> event) {
                if (event.getResult() == null) {
                    return;
                }
                for (ListeningFuture<LocalFileItem> childFuture : event.getResult()) {
                    offerFile(chatFriend, childFuture);
                }
            }
        });
    }

    private void offerFile(final ChatFriend chatFriend, ListeningFuture<LocalFileItem> future) {
        future.addFutureListener(new EventListener<FutureEvent<LocalFileItem>>() {
            @Override
            @SwingEDTEvent
            public void handleEvent(FutureEvent<LocalFileItem> event) {
                LocalFileItem localFileItem = event.getResult();
                if (localFileItem == null) {
                    return;
                }
                performOutgoingFileOffer(chatFriend, localFileItem);
            }
        });
    }

    private void performOutgoingFileOffer(ChatFriend chatFriend, LocalFileItem localFileItem) {
        FileMetaData metadata = localFileItem.toMetadata();
        Friend friend = chatFriend.getFriend();
        if (!friend.isSignedIn()) {
            emitStatusMessage(chatFriend.getID(), I18n.tr("File offer not sent because friend signed off."));
            return;
        }

        List<FriendPresence> presences = findFileOfferCapablePresences(friend);
        if (presences.isEmpty()) {
            emitStatusMessage(chatFriend.getID(), I18n.tr("This friend cannot receive file offers right now."));
            return;
        }

        boolean sentFileOffer = false;
        try {
            ensureConversation(chatFriend).setChatState(ChatState.active);
        } catch (FriendException ignored) {
        }

        for (FriendPresence presence : presences) {
            try {
                FeatureTransport<FileMetaData> fileOfferTransport = presence.getTransport(FileOfferFeature.class);
                if (fileOfferTransport != null) {
                    fileOfferTransport.sendFeature(presence, metadata);
                    sentFileOffer = true;
                }
            } catch (FriendException ignored) {
            }
        }

        if (!sentFileOffer) {
            emitStatusMessage(chatFriend.getID(), I18n.tr("WireShare could not send that file offer."));
            return;
        }

        TrackedFileOffer tracked = new TrackedFileOffer(
                fileOfferMessageId(chatFriend.getID(), metadata, Message.Type.SENT),
                chatFriend.getID(),
                I18n.tr("me"),
                System.currentTimeMillis(),
                Message.Type.SENT,
                metadata,
                null);
        trackedFileOffers.put(tracked.messageId, tracked);
        emitMessage(new ComposeChatMessage(tracked));
    }

    private void trackDownload(final TrackedFileOffer tracked, DownloadItem download) {
        updateTrackedDownloadState(tracked, download);
        emitMessage(new ComposeChatMessage(tracked));
        download.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || "state".equals(evt.getPropertyName())) {
                    updateTrackedDownloadState(tracked, download);
                    emitMessage(new ComposeChatMessage(tracked));
                }
            }
        });
    }

    private void updateTrackedDownloadState(TrackedFileOffer tracked, DownloadItem download) {
        tracked.downloadState = download.getState();
        File localFile = download.getLaunchableFile();
        if (localFile == null) {
            localFile = download.getDownloadingFile();
        }
        tracked.localPath = localFile != null ? localFile.getAbsolutePath() : null;
    }

    private TrackedFileOffer trackFileOffer(MessageFileOffer message) {
        String messageId = fileOfferMessageId(message.getFriendID(), message.getFileOffer(), message.getType());
        TrackedFileOffer existing = trackedFileOffers.get(messageId);
        if (existing != null) {
            return existing;
        }
        TrackedFileOffer created = new TrackedFileOffer(
                messageId,
                message.getFriendID(),
                message.getSenderName(),
                message.getMessageTimeMillis(),
                message.getType(),
                message.getFileOffer(),
                message.getPresence());
        trackedFileOffers.put(messageId, created);
        return created;
    }

    private NoSaveStatus getNoSaveStatus(String friendId) {
        ChatFriend chatFriend = chatModel.getChatFriend(friendId);
        if (chatFriend == null) {
            return null;
        }
        for (FriendPresence presence : chatFriend.getFriend().getPresences().values()) {
            if (presence.hasFeatures(NoSaveFeature.ID)) {
                NoSaveFeature feature = (NoSaveFeature) presence.getFeature(NoSaveFeature.ID);
                return feature != null ? feature.getFeature() : null;
            }
        }
        return null;
    }

    private List<FriendPresence> findFileOfferCapablePresences(Friend friend) {
        if (friend == null) {
            return Collections.emptyList();
        }
        List<FriendPresence> matches = new ArrayList<FriendPresence>();
        FriendPresence activePresence = friend.getActivePresence();
        if (activePresence != null && activePresence.hasFeatures(FileOfferFeature.ID)) {
            matches.add(activePresence);
            return matches;
        }
        for (FriendPresence presence : friend.getPresences().values()) {
            if (presence.hasFeatures(FileOfferFeature.ID)) {
                matches.add(presence);
            }
        }
        return matches;
    }

    private FriendPresence findPresenceById(String presenceId) {
        FriendConnection connection = getCurrentConnection();
        if (connection == null || presenceId == null) {
            return null;
        }
        for (Friend friend : connection.getFriends()) {
            FriendPresence presence = friend.getPresences().get(presenceId);
            if (presence != null) {
                return presence;
            }
        }
        return null;
    }

    private Friend findFriendByPresenceId(String presenceId) {
        FriendPresence presence = findPresenceById(presenceId);
        return presence != null ? presence.getFriend() : null;
    }

    private static String textMessageId(Message message) {
        return "text:" + message.getFriendID() + ":" + message.getType().name() + ":" + message.getMessageTimeMillis() + ":" + message.toString().hashCode();
    }

    private static String fileOfferMessageId(String friendId, FileMetaData fileOffer, Message.Type type) {
        String offerId = fileOffer.getId() != null ? fileOffer.getId() : fileOffer.getName() + ":" + fileOffer.getSize();
        return "file-offer:" + friendId + ":" + type.name() + ":" + offerId;
    }

    private static String noSaveMessage(NoSave status) {
        return status == NoSave.ENABLED
                ? I18n.tr("Chat is now off the record")
                : I18n.tr("Chat is now on the record");
    }

    private static final class TrackedFileOffer {
        private final String messageId;
        private final String friendId;
        private final String senderName;
        private final long timestamp;
        private final Message.Type type;
        private final FileMetaData fileOffer;
        private final FriendPresence presence;
        private volatile DownloadState downloadState;
        private volatile String localPath;

        private TrackedFileOffer(String messageId,
                String friendId,
                String senderName,
                long timestamp,
                Message.Type type,
                FileMetaData fileOffer,
                FriendPresence presence) {
            this.messageId = messageId;
            this.friendId = friendId;
            this.senderName = senderName;
            this.timestamp = timestamp;
            this.type = type;
            this.fileOffer = fileOffer;
            this.presence = presence;
        }
    }
}
