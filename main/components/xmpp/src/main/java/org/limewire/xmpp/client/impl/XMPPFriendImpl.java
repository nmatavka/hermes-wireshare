package org.limewire.xmpp.client.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.GuardedBy;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.limewire.concurrent.ThreadExecutor;
import org.limewire.friend.api.ChatState;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.IncomingChatListener;
import org.limewire.friend.api.MessageReader;
import org.limewire.friend.api.MessageWriter;
import org.limewire.friend.api.Network;
import org.limewire.friend.api.PresenceEvent;
import org.limewire.friend.api.feature.Feature;
import org.limewire.friend.api.feature.FeatureRegistry;
import org.limewire.friend.impl.AbstractFriend;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.util.DebugRunnable;
import org.limewire.util.StringUtils;

public class XMPPFriendImpl extends AbstractFriend {

    private static final Log LOG = LogFactory.getLog(XMPPFriendImpl.class);

    private final String id;
    private final FeatureRegistry featureRegistry;
    private final String idNoService;
    private final AtomicReference<RosterEntry> rosterEntry;
    private final XMPPConnection connection;
    private final Network network;

    private final Object presenceLock;

    @GuardedBy("presenceLock")
    private final Map<String, FriendPresence> presences;

    @GuardedBy("presenceLock")
    private String activePresenceJid;

    private final Object chatListenerLock;

    @GuardedBy("chatListenerLock")
    private volatile IncomingChatListenerAdapter listenerAdapter;

    private final Object chatSessionLock = new Object();

    @GuardedBy("chatSessionLock")
    private ChatSession activeChatSession;

    XMPPFriendImpl(String id, RosterEntry rosterEntry, Network network,
                   XMPPConnection connection, FeatureRegistry featureRegistry) {
        this.id = id;
        this.featureRegistry = featureRegistry;
        this.idNoService = stripService(id, network.getNetworkName());
        this.network = network;
        this.rosterEntry = new AtomicReference<RosterEntry>(rosterEntry);
        this.presences = new HashMap<String, FriendPresence>();
        this.activePresenceJid = null;
        this.connection = connection;
        this.presenceLock = new Object();
        this.chatListenerLock = new Object();
    }

    private static String stripService(String id, String service) {
        int idx = id.lastIndexOf("@" + service);
        if (idx == -1) {
            return id;
        }
        return id.substring(0, idx);
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        String name = rosterEntry.get().getName();
        if (name != null) {
            String service = network.getNetworkName();
            int idx = name.lastIndexOf("@" + service);
            if (idx == -1) {
                return name;
            }
            return name.substring(0, idx);
        }
        return null;
    }

    @Override
    public String getRenderName() {
        String visualName = getName();
        if (visualName == null) {
            return idNoService;
        }
        return visualName;
    }

    @Override
    public String getFirstName() {
        String visualName = getName();
        if (visualName == null) {
            return idNoService;
        }
        String[] subStrings = visualName.split(" ");
        return subStrings[0];
    }

    void setRosterEntry(RosterEntry rosterEntry) {
        this.rosterEntry.set(rosterEntry);
    }

    @Override
    public void setName(final String name) {
        Thread t = ThreadExecutor.newManagedThread(new DebugRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    XMPPFriendImpl.this.rosterEntry.get().setName(name);
                } catch (Exception e) {
                    LOG.debugf("set name failed", e);
                }
            }
        }), "set-name-thread-" + toString());
        t.start();
    }

    @Override
    public Map<String, FriendPresence> getPresences() {
        synchronized (presenceLock) {
            return Collections.unmodifiableMap(new HashMap<String, FriendPresence>(presences));
        }
    }

    @Override
    public boolean isSubscribed() {
        RosterPacket.ItemType type = rosterEntry.get().getType();
        return type == RosterPacket.ItemType.both || type == RosterPacket.ItemType.to;
    }

    void addPresense(FriendPresence presence) {
        if (LOG.isDebugEnabled()) {
            LOG.debugf("adding presence {0}", presence.getPresenceId());
        }
        synchronized (presenceLock) {
            presences.put(presence.getPresenceId(), presence);
        }
        firePresenceEvent(new PresenceEvent(presence, PresenceEvent.Type.PRESENCE_NEW));
    }

    void removePresense(FriendPresence presence) {
        if (LOG.isDebugEnabled()) {
            LOG.debugf("removing presence {0}", presence.getPresenceId());
        }
        Collection<Feature> features = presence.getFeatures();
        for (Feature feature : features) {
            LOG.debugf("removing feature {0} for {1}", feature, presence);
            featureRegistry.get(feature.getID()).removeFeature(presence);
        }

        synchronized (presenceLock) {
            presences.remove(presence.getPresenceId());
            if (presence.getPresenceId().equals(activePresenceJid)) {
                activePresenceJid = null;
            }
            if (!isSignedIn()) {
                removeChatListener();
                clearActiveChatSession();
            }
        }

        firePresenceEvent(new PresenceEvent(presence, PresenceEvent.Type.PRESENCE_UPDATE));
    }

    @Override
    public String toString() {
        return StringUtils.toString(this, id, getName());
    }

    void updatePresence(FriendPresence updatedPresence) {
        if (LOG.isDebugEnabled()) {
            LOG.debugf("updating presence {0}", updatedPresence.getPresenceId());
        }
        synchronized (presenceLock) {
            presences.put(updatedPresence.getPresenceId(), updatedPresence);
        }
        firePresenceEvent(new PresenceEvent(updatedPresence, PresenceEvent.Type.PRESENCE_UPDATE));
    }

    FriendPresence getPresence(String jid) {
        synchronized (presenceLock) {
            return presences.get(jid);
        }
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    private String getChatParticipantId() {
        synchronized (presenceLock) {
            return activePresenceJid == null ? id : activePresenceJid;
        }
    }

    private void setActivePresence(String presenceId) {
        synchronized (presenceLock) {
            activePresenceJid = presenceId;
        }
    }

    @Override
    public FriendPresence getActivePresence() {
        synchronized (presenceLock) {
            return presences.get(activePresenceJid);
        }
    }

    @Override
    public boolean hasActivePresence() {
        synchronized (presenceLock) {
            return activePresenceJid != null;
        }
    }

    @Override
    public boolean isSignedIn() {
        synchronized (presenceLock) {
            return !presences.isEmpty();
        }
    }

    @Override
    public MessageWriter createChat(final MessageReader reader) {
        try {
            Chat chat = ChatManager.getInstanceFor(connection).chatWith(getBareJid());
            if (LOG.isInfoEnabled()) {
                LOG.info("new chat with " + getChatParticipantId());
            }
            return openChatSession(chat, reader).writer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setChatListenerIfNecessary(final IncomingChatListener listener) {
        synchronized (chatListenerLock) {
            if (listenerAdapter == null) {
                listenerAdapter = new IncomingChatListenerAdapter(listener);
                ChatManager.getInstanceFor(connection).addIncomingListener(listenerAdapter);
            }
        }
    }

    @Override
    public void removeChatListener() {
        synchronized (chatListenerLock) {
            if (listenerAdapter != null) {
                ChatManager.getInstanceFor(connection).removeIncomingListener(listenerAdapter);
                listenerAdapter = null;
            }
        }
    }

    private EntityBareJid getBareJid() throws Exception {
        return JidCreate.entityBareFrom(id);
    }

    private ChatSession openChatSession(Chat chat, MessageReader reader) {
        synchronized (chatSessionLock) {
            if (activeChatSession != null) {
                ChatStateManager.getInstance(connection).removeChatStateListener(activeChatSession.chatStateListener);
            }
            DefaultMessageWriter writer = new DefaultMessageWriter(chat);
            DefaultChatStateListener chatStateListener = new DefaultChatStateListener(reader);
            ChatStateManager.getInstance(connection).addChatStateListener(chatStateListener);
            activeChatSession = new ChatSession(chat, writer, reader, chatStateListener);
            return activeChatSession;
        }
    }

    private ChatSession getOrCreateIncomingChatSession(IncomingChatListener listener, Chat chat) {
        synchronized (chatSessionLock) {
            if (activeChatSession != null) {
                return activeChatSession;
            }
            DefaultMessageWriter writer = new DefaultMessageWriter(chat);
            MessageReader reader = listener.incomingChat(writer);
            DefaultChatStateListener chatStateListener = new DefaultChatStateListener(reader);
            ChatStateManager.getInstance(connection).addChatStateListener(chatStateListener);
            activeChatSession = new ChatSession(chat, writer, reader, chatStateListener);
            return activeChatSession;
        }
    }

    private void clearActiveChatSession() {
        synchronized (chatSessionLock) {
            if (activeChatSession != null) {
                ChatStateManager.getInstance(connection).removeChatStateListener(activeChatSession.chatStateListener);
                activeChatSession = null;
            }
        }
    }

    private class IncomingChatListenerAdapter implements IncomingChatMessageListener {
        private final IncomingChatListener listener;

        IncomingChatListenerAdapter(IncomingChatListener listener) {
            this.listener = listener;
        }

        @Override
        public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
            if (!isForThisUser(from)) {
                return;
            }

            String messageFrom = message.getFrom() != null ? message.getFrom().toString() : from.toString();
            if (!messageFrom.equals(getChatParticipantId())) {
                setActivePresence(messageFrom);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("new incoming chat with " + getChatParticipantId());
            }

            ChatSession chatSession = getOrCreateIncomingChatSession(listener, chat);
            chatSession.writer.deliverMessage(chat, message, chatSession.reader);
        }

        private boolean isForThisUser(EntityBareJid incomingMessageJid) {
            return id.equals(incomingMessageJid.toString());
        }
    }

    private class DefaultMessageWriter implements MessageWriter {

        private final Chat chat;

        DefaultMessageWriter(Chat chat) {
            this.chat = chat;
        }

        @Override
        public void writeMessage(String message) throws FriendException {
            try {
                String participantId = getChatParticipantId();
                Message outgoing = connection.getStanzaFactory().buildMessageStanza()
                        .ofType(Message.Type.chat)
                        .setBody(message)
                        .build();
                if (id.equals(participantId)) {
                    chat.send(outgoing);
                } else {
                    outgoing.setTo(JidCreate.from(participantId));
                    connection.sendStanza(outgoing);
                }
            } catch (Exception e) {
                throw new FriendException(e);
            }
        }

        @Override
        public void setChatState(ChatState chatState) throws FriendException {
            try {
                ChatStateManager.getInstance(connection)
                        .setCurrentState(org.jivesoftware.smackx.chatstates.ChatState.valueOf(chatState.toString()), chat);
            } catch (Exception e) {
                throw new FriendException(e);
            }
        }

        void deliverMessage(Chat chat, Message message, MessageReader reader) {
            String msgFromJid = message.getFrom() != null ? message.getFrom().toString() : getChatParticipantId();
            if (!getChatParticipantId().equals(msgFromJid)) {
                setActivePresence(msgFromJid);
            }

            if (message.getType() == Message.Type.error) {
                String errorMsg = parseError(message);
                if (errorMsg != null) {
                    reader.error(errorMsg);
                }
            } else if (message.getBody() != null) {
                reader.readMessage(message.getBody());
            }
        }
    }

    private class DefaultChatStateListener implements ChatStateListener {

        private final MessageReader reader;

        DefaultChatStateListener(MessageReader reader) {
            this.reader = reader;
        }

        @Override
        public void stateChanged(Chat chat, org.jivesoftware.smackx.chatstates.ChatState state, Message message) {
            if (isSignedIn() && chat.getXmppAddressOfChatPartner().toString().equals(id)) {
                reader.newChatState(ChatState.valueOf(state.toString()));
            }
        }
    }

    private String parseError(Message errorMessage) {
        StanzaError error = errorMessage.getError();
        if (error != null) {
            String body = errorMessage.getBody();
            if (body != null) {
                return "Error sending message: '" + body + "' : " + error.toString();
            }
        }
        return null;
    }

    private static final class ChatSession {
        private final Chat chat;
        private final DefaultMessageWriter writer;
        private final MessageReader reader;
        private final DefaultChatStateListener chatStateListener;

        private ChatSession(Chat chat, DefaultMessageWriter writer, MessageReader reader,
                            DefaultChatStateListener chatStateListener) {
            this.chat = chat;
            this.writer = writer;
            this.reader = reader;
            this.chatStateListener = chatStateListener;
        }
    }
}
