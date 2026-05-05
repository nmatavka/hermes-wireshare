package org.limewire.xmpp.client.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.AbstractRosterListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.debugger.slf4j.SLF4JDebuggerFactory;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.limewire.concurrent.ListeningExecutorService;
import org.limewire.concurrent.ListeningFuture;
import org.limewire.friend.api.Friend;
import org.limewire.friend.api.FriendConnection;
import org.limewire.friend.api.FriendConnectionConfiguration;
import org.limewire.friend.api.FriendConnectionEvent;
import org.limewire.friend.api.FriendException;
import org.limewire.friend.api.FriendPresence;
import org.limewire.friend.api.FriendPresenceEvent;
import org.limewire.friend.api.FriendRequestEvent;
import org.limewire.friend.api.RosterEvent;
import org.limewire.friend.api.feature.AddressFeature;
import org.limewire.friend.api.feature.AuthTokenFeature;
import org.limewire.friend.api.feature.ConnectBackRequestFeature;
import org.limewire.friend.api.feature.FeatureEvent;
import org.limewire.friend.api.feature.FeatureRegistry;
import org.limewire.friend.api.feature.FileOfferFeature;
import org.limewire.friend.api.feature.LibraryChangedNotifierFeature;
import org.limewire.friend.impl.feature.LimewireFeatureInitializer;
import org.limewire.friend.impl.feature.NoSaveFeature;
import org.limewire.friend.impl.util.PresenceUtils;
import org.limewire.listener.AsynchronousEventBroadcaster;
import org.limewire.listener.AsynchronousEventMulticaster;
import org.limewire.listener.EventBroadcaster;
import org.limewire.listener.EventListener;
import org.limewire.listener.EventListenerList;
import org.limewire.listener.EventMulticaster;
import org.limewire.listener.EventRebroadcaster;
import org.limewire.listener.ListenerSupport;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.net.address.AddressFactory;
import org.limewire.xmpp.activity.XmppActivityEvent;
import org.limewire.xmpp.api.client.JabberSettings;
import org.limewire.xmpp.client.impl.features.NoSaveFeatureInitializer;
import org.limewire.xmpp.client.impl.messages.address.AddressIQ;
import org.limewire.xmpp.client.impl.messages.address.AddressIQListener;
import org.limewire.xmpp.client.impl.messages.address.AddressIQListenerFactory;
import org.limewire.xmpp.client.impl.messages.address.AddressIQProvider;
import org.limewire.xmpp.client.impl.messages.authtoken.AuthTokenIQ;
import org.limewire.xmpp.client.impl.messages.authtoken.AuthTokenIQListener;
import org.limewire.xmpp.client.impl.messages.authtoken.AuthTokenIQListenerFactory;
import org.limewire.xmpp.client.impl.messages.authtoken.AuthTokenIQProvider;
import org.limewire.xmpp.client.impl.messages.connectrequest.ConnectBackRequestIQ;
import org.limewire.xmpp.client.impl.messages.connectrequest.ConnectBackRequestIQListener;
import org.limewire.xmpp.client.impl.messages.connectrequest.ConnectBackRequestIQListenerFactory;
import org.limewire.xmpp.client.impl.messages.connectrequest.ConnectBackRequestIQProvider;
import org.limewire.xmpp.client.impl.messages.discoinfo.DiscoInfoListener;
import org.limewire.xmpp.client.impl.messages.filetransfer.FileTransferIQ;
import org.limewire.xmpp.client.impl.messages.filetransfer.FileTransferIQListener;
import org.limewire.xmpp.client.impl.messages.filetransfer.FileTransferIQListenerFactory;
import org.limewire.xmpp.client.impl.messages.library.LibraryChangedIQ;
import org.limewire.xmpp.client.impl.messages.library.LibraryChangedIQListener;
import org.limewire.xmpp.client.impl.messages.library.LibraryChangedIQListenerFactory;
import org.limewire.xmpp.client.impl.messages.nosave.NoSaveIQ;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Implements a {@link FriendConnection} using XMPP.
 */
public class XMPPFriendConnectionImpl implements FriendConnection {

    private static final Log LOG = LogFactory.getLog(XMPPFriendConnectionImpl.class);

    private final FriendConnectionConfiguration configuration;
    private final EventBroadcaster<FriendRequestEvent> friendRequestBroadcaster;
    private final AsynchronousEventMulticaster<FriendConnectionEvent> connectionMulticaster;
    private final AddressFactory addressFactory;
    private final EventMulticaster<FeatureEvent> featureSupport;
    private final ListeningExecutorService executorService;
    private final List<ConnectionConfigurationFactory> connectionConfigurationFactories;
    private final AddressIQListenerFactory addressIQListenerFactory;
    private final AuthTokenIQListenerFactory authTokenIQListenerFactory;
    private final LibraryChangedIQListenerFactory libraryChangedIQListenerFactory;
    private volatile AddressIQListener addressIQListener;
    private volatile AuthTokenIQListener authTokenIQListener;
    private volatile LibraryChangedIQListener libraryChangedIQListener;
    private volatile ConnectBackRequestIQListener connectRequestIQListener;
    private volatile FileTransferIQListener fileTransferIQListener;
    private volatile SubscriptionListener subscriptionListener;

    private final EventListenerList<RosterEvent> rosterListeners;
    private final Map<String, XMPPFriendImpl> friends;
    private final SmackConnectionListener smackConnectionListener;
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);
    private final AtomicBoolean loggingIn = new AtomicBoolean(false);
    private final AtomicBoolean providersRegistered = new AtomicBoolean(false);

    private volatile XMPPTCPConnection connection;
    private volatile DiscoInfoListener discoInfoListener;

    private final ConnectBackRequestIQListenerFactory connectBackRequestIQListenerFactory;
    private final FileTransferIQListenerFactory fileTransferIQListenerFactory;

    private final ListenerSupport<FriendPresenceEvent> friendPresenceSupport;

    private final FeatureRegistry featureRegistry;
    private final IdleStatusMonitorFactory idleStatusMonitorFactory;
    private IdleStatusMonitor idleStatusMonitor;

    private volatile NoSaveFeatureInitializer noSaveFeatureInitializer;
    private final JabberSettings jabberSettings;
    private final ListenerSupport<XmppActivityEvent> xmppActivitySupport;
    private EventListener<XmppActivityEvent> xmppActivityListener;

    @Inject
    public XMPPFriendConnectionImpl(@Assisted FriendConnectionConfiguration configuration,
                                    @Assisted ListeningExecutorService executorService,
                                    AsynchronousEventBroadcaster<RosterEvent> rosterBroadcaster,
                                    EventBroadcaster<FriendRequestEvent> friendRequestBroadcaster,
                                    AsynchronousEventMulticaster<FriendConnectionEvent> connectionMulticaster,
                                    AddressFactory addressFactory,
                                    EventMulticaster<FeatureEvent> featureSupport,
                                    List<ConnectionConfigurationFactory> connectionConfigurationFactories,
                                    AddressIQListenerFactory addressIQListenerFactory,
                                    AuthTokenIQListenerFactory authTokenIQListenerFactory,
                                    ConnectBackRequestIQListenerFactory connectBackRequestIQListenerFactory,
                                    LibraryChangedIQListenerFactory libraryChangedIQListenerFactory,
                                    FileTransferIQListenerFactory fileTransferIQListenerFactory,
                                    ListenerSupport<FriendPresenceEvent> friendPresenceSupport,
                                    FeatureRegistry featureRegistry,
                                    IdleStatusMonitorFactory idleStatusMonitorFactory,
                                    JabberSettings jabberSettings,
                                    ListenerSupport<XmppActivityEvent> xmppActivitySupport) {
        this.configuration = configuration;
        this.friendRequestBroadcaster = friendRequestBroadcaster;
        this.connectionMulticaster = connectionMulticaster;
        this.addressFactory = addressFactory;
        this.featureSupport = featureSupport;
        this.executorService = executorService;
        this.connectionConfigurationFactories = connectionConfigurationFactories;
        this.addressIQListenerFactory = addressIQListenerFactory;
        this.authTokenIQListenerFactory = authTokenIQListenerFactory;
        this.libraryChangedIQListenerFactory = libraryChangedIQListenerFactory;
        this.connectBackRequestIQListenerFactory = connectBackRequestIQListenerFactory;
        this.fileTransferIQListenerFactory = fileTransferIQListenerFactory;
        this.friendPresenceSupport = friendPresenceSupport;
        this.featureRegistry = featureRegistry;
        this.idleStatusMonitorFactory = idleStatusMonitorFactory;
        this.jabberSettings = jabberSettings;
        this.xmppActivitySupport = xmppActivitySupport;
        rosterListeners = new EventListenerList<RosterEvent>();
        if (configuration.getRosterListener() != null) {
            rosterListeners.addListener(configuration.getRosterListener());
        }
        rosterListeners.addListener(new EventRebroadcaster<RosterEvent>(rosterBroadcaster));
        friends = new TreeMap<String, XMPPFriendImpl>(String.CASE_INSENSITIVE_ORDER);

        smackConnectionListener = new SmackConnectionListener();
    }

    @Override
    public String toString() {
        return org.limewire.util.StringUtils.toString(this, configuration, connection);
    }

    @Override
    public boolean supportsMode() {
        return true;
    }

    public ListeningFuture<Void> setMode(final FriendPresence.Mode mode) {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setModeImpl(mode);
                return null;
            }
        });
    }

    void setModeImpl(FriendPresence.Mode mode) throws FriendException {
        synchronized (this) {
            try {
                checkLoggedIn();
                connection.sendStanza(getPresenceForMode(mode));
            } catch (Exception e) {
                throw new FriendException(e);
            }
        }
    }

    private Presence getPresenceForMode(FriendPresence.Mode mode) {
        return connection.getStanzaFactory().buildPresenceStanza()
                .ofType(Presence.Type.available)
                .setMode(Presence.Mode.valueOf(mode.name()))
                .setStatus(jabberSettings.advertiseLimeWireStatus() ? "on WireShare" : null)
                .build();
    }

    public FriendConnectionConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public ListeningFuture<Void> login() {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                loginImpl();
                return null;
            }
        });
    }

    void loginImpl() throws FriendException {
        synchronized (this) {
            try {
                loggingIn.set(true);
                connectionMulticaster.broadcast(new FriendConnectionEvent(this, FriendConnectionEvent.Type.CONNECTING));
                configureDebugging();
                registerProviders();
                connect();
                LOG.infof("connected.");
                LOG.infof("logging in {0} with resource: {1} ...", configuration.getUserInputLocalID(),
                        configuration.getResource());
                connection.login(configuration.getUserInputLocalID(), configuration.getPassword(),
                        Resourcepart.from(configuration.getResource()));
                startSessionServices();
                LOG.infof("logged in.");
                loggedIn.set(true);
                loggingIn.set(false);
                connectionMulticaster.broadcast(new FriendConnectionEvent(this, FriendConnectionEvent.Type.CONNECTED));
            } catch (Exception e) {
                handleLoginError(e);
                throw new FriendException(e);
            }
        }
    }

    private void configureDebugging() {
        SmackConfiguration.DEBUG = configuration.isDebugEnabled();
        if (configuration.isDebugEnabled()) {
            SmackConfiguration.setDefaultSmackDebuggerFactory(SLF4JDebuggerFactory.INSTANCE);
        }
    }

    private synchronized void handleLoginError(Exception e) {
        loggingIn.set(false);
        connectionMulticaster.broadcast(new FriendConnectionEvent(this, FriendConnectionEvent.Type.CONNECT_FAILED, e));
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
        cleanupSessionServices();
        connection = null;
    }

    private void connect() throws FriendException {
        FriendException lastFailure = null;
        for (ConnectionConfigurationFactory factory : connectionConfigurationFactories) {
            try {
                connectUsingFactory(factory);
                return;
            } catch (FriendException e) {
                lastFailure = e;
                LOG.debug(e.getMessage(), e);
            }
        }
        if (lastFailure != null) {
            throw lastFailure;
        }
        throw new FriendException("unable to connect");
    }

    private void connectUsingFactory(ConnectionConfigurationFactory factory) throws FriendException {
        ConnectionConfigurationFactory.RequestContext requestContext = new ConnectionConfigurationFactory.RequestContext();
        while (factory.hasMore(configuration, requestContext)) {
            XMPPTCPConnectionConfiguration connectionConfig =
                    factory.getConnectionConfiguration(configuration, requestContext);
            XMPPTCPConnection candidate = new XMPPTCPConnection(connectionConfig);
            prepareConnection(candidate);
            String host = connectionConfig.getHost() != null ? connectionConfig.getHost().toString()
                    : connectionConfig.getXMPPServiceDomain().toString();
            int port = connectionConfig.getPort() != null ? connectionConfig.getPort().intValue() : 5222;
            LOG.infof("connecting to {0} at {1}:{2} ...", connectionConfig.getXMPPServiceDomain(), host, port);
            try {
                candidate.connect();
                connection = candidate;
                return;
            } catch (Exception e) {
                LOG.debug(e.getMessage(), e);
                candidate.disconnect();
                requestContext.incrementRequests();
            }
        }
        throw new FriendException("couldn't connect using " + factory);
    }

    private void prepareConnection(XMPPTCPConnection candidate) {
        connection = candidate;
        candidate.addConnectionListener(smackConnectionListener);
        Roster roster = Roster.getInstanceFor(candidate);
        roster.addRosterListener(new RosterListenerImpl());
        ChatStateManager.getInstance(candidate);

        discoInfoListener = new DiscoInfoListener(this, candidate, featureRegistry);
        discoInfoListener.addListeners(connectionMulticaster, friendPresenceSupport);

        addressIQListener = addressIQListenerFactory.create(this, addressFactory);
        candidate.addAsyncStanzaListener(addressIQListener, addressIQListener.getStanzaFilter());

        fileTransferIQListener = fileTransferIQListenerFactory.create(this);
        candidate.addAsyncStanzaListener(fileTransferIQListener, fileTransferIQListener.getStanzaFilter());

        authTokenIQListener = authTokenIQListenerFactory.create(this);
        candidate.addAsyncStanzaListener(authTokenIQListener, authTokenIQListener.getStanzaFilter());

        libraryChangedIQListener = libraryChangedIQListenerFactory.create(this);
        candidate.addAsyncStanzaListener(libraryChangedIQListener, libraryChangedIQListener.getStanzaFilter());

        connectRequestIQListener = connectBackRequestIQListenerFactory.create(this);
        candidate.addAsyncStanzaListener(connectRequestIQListener, connectRequestIQListener.getStanzaFilter());

        new LimewireFeatureInitializer().register(featureRegistry);

        noSaveFeatureInitializer = new NoSaveFeatureInitializer(candidate, this, rosterListeners, friendPresenceSupport);
        noSaveFeatureInitializer.register(featureRegistry);

        subscriptionListener = new SubscriptionListener(candidate, friendRequestBroadcaster);
        candidate.addAsyncStanzaListener(subscriptionListener, subscriptionListener.getStanzaFilter());

        for (URI feature : featureRegistry.getPublicFeatureUris()) {
            ServiceDiscoveryManager.getInstanceFor(candidate).addFeature(feature.toASCIIString());
        }
    }

    private void registerProviders() {
        if (!providersRegistered.compareAndSet(false, true)) {
            return;
        }

        synchronized (ProviderManager.class) {
            if (ProviderManager.getIQProvider(AddressIQ.ELEMENT, AddressIQ.NAMESPACE) == null) {
                ProviderManager.addIQProvider(AddressIQ.ELEMENT, AddressIQ.NAMESPACE, new AddressIQProvider(addressFactory));
            }
            if (ProviderManager.getIQProvider(FileTransferIQ.ELEMENT, FileTransferIQ.NAMESPACE) == null) {
                ProviderManager.addIQProvider(FileTransferIQ.ELEMENT, FileTransferIQ.NAMESPACE, FileTransferIQ.getIQProvider());
            }
            if (ProviderManager.getIQProvider(AuthTokenIQ.ELEMENT, AuthTokenIQ.NAMESPACE) == null) {
                ProviderManager.addIQProvider(AuthTokenIQ.ELEMENT, AuthTokenIQ.NAMESPACE, new AuthTokenIQProvider());
            }
            if (ProviderManager.getIQProvider(LibraryChangedIQ.ELEMENT, LibraryChangedIQ.NAMESPACE) == null) {
                ProviderManager.addIQProvider(LibraryChangedIQ.ELEMENT, LibraryChangedIQ.NAMESPACE, LibraryChangedIQ.getIQProvider());
            }
            if (ProviderManager.getIQProvider(ConnectBackRequestIQ.ELEMENT_NAME, ConnectBackRequestIQ.NAME_SPACE) == null) {
                ProviderManager.addIQProvider(ConnectBackRequestIQ.ELEMENT_NAME, ConnectBackRequestIQ.NAME_SPACE,
                        new ConnectBackRequestIQProvider());
            }
            if (ProviderManager.getIQProvider(NoSaveIQ.ELEMENT_NAME, NoSaveIQ.NAME_SPACE) == null) {
                ProviderManager.addIQProvider(NoSaveIQ.ELEMENT_NAME, NoSaveIQ.NAME_SPACE, NoSaveIQ.getIQProvider());
            }
        }
    }

    @Override
    public boolean isLoggingIn() {
        return loggingIn.get();
    }

    @Override
    public ListeningFuture<Void> logout() {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                logoutImpl(null);
                return null;
            }
        });
    }

    void logoutImpl(Exception error) {
        synchronized (this) {
            if (isLoggedIn()) {
                loggedIn.set(false);
                if (connection != null) {
                    LOG.infof("disconnecting from {0} at {1}:{2} ...", connection.getXMPPServiceDomain(),
                            connection.getHost(), connection.getPort());
                    cleanupSessionServices();
                    connection.disconnect();
                }
                synchronized (friends) {
                    friends.clear();
                }
                connection = null;
                LOG.info("disconnected.");
                connectionMulticaster.broadcast(new FriendConnectionEvent(XMPPFriendConnectionImpl.this,
                        FriendConnectionEvent.Type.DISCONNECTED, error));
                featureRegistry.deregisterInitializer(NoSaveFeature.ID);
            }
        }
    }

    private void startSessionServices() {
        if (xmppActivityListener == null) {
            xmppActivityListener = new XmppActivityEventListener();
        }
        xmppActivitySupport.addListener(xmppActivityListener);

        if (idleStatusMonitor == null) {
            idleStatusMonitor = idleStatusMonitorFactory.create();
        }
        idleStatusMonitor.start();
    }

    private void cleanupSessionServices() {
        if (discoInfoListener != null) {
            discoInfoListener.cleanup();
            discoInfoListener = null;
        }
        if (noSaveFeatureInitializer != null) {
            noSaveFeatureInitializer.cleanup();
            noSaveFeatureInitializer = null;
        }
        if (xmppActivityListener != null) {
            xmppActivitySupport.removeListener(xmppActivityListener);
        }
        if (idleStatusMonitor != null) {
            idleStatusMonitor.stop();
        }
    }

    public boolean isLoggedIn() {
        return loggedIn.get();
    }

    private void checkLoggedIn() throws FriendException {
        synchronized (this) {
            if (!isLoggedIn()) {
                throw new FriendException("not connected");
            }
        }
    }

    private class RosterListenerImpl extends AbstractRosterListener {

        @Override
        public void entriesAdded(Collection<Jid> addedIds) {
            try {
                synchronized (XMPPFriendConnectionImpl.this) {
                    checkLoggedIn();
                    synchronized (friends) {
                        Roster roster = Roster.getInstanceFor(connection);
                        Map<String, XMPPFriendImpl> newFriends = new HashMap<String, XMPPFriendImpl>();
                        for (Jid jid : addedIds) {
                            String id = jid.asBareJid().toString();
                            RosterEntry rosterEntry = roster.getEntry(jid.asBareJid());
                            XMPPFriendImpl friend = new XMPPFriendImpl(id, rosterEntry, configuration, connection,
                                    featureRegistry);
                            LOG.debugf("user {0} added", friend);
                            newFriends.put(id, friend);
                        }
                        friends.putAll(newFriends);
                        rosterListeners.broadcast(new RosterEvent(new ArrayList<Friend>(newFriends.values()),
                                RosterEvent.Type.FRIENDS_ADDED));
                    }
                }
            } catch (FriendException e) {
                LOG.debugf(e, "error getting roster");
            }
        }

        @Override
        public void entriesUpdated(Collection<Jid> updatedIds) {
            try {
                synchronized (XMPPFriendConnectionImpl.this) {
                    checkLoggedIn();
                    synchronized (friends) {
                        Roster roster = Roster.getInstanceFor(connection);
                        List<Friend> updatedFriends = new ArrayList<Friend>();
                        for (Jid jid : updatedIds) {
                            String id = jid.asBareJid().toString();
                            RosterEntry rosterEntry = roster.getEntry(jid.asBareJid());
                            XMPPFriendImpl friend = friends.get(id);
                            if (friend == null) {
                                friend = new XMPPFriendImpl(id, rosterEntry, configuration, connection, featureRegistry);
                                friends.put(id, friend);
                            } else {
                                friend.setRosterEntry(rosterEntry);
                            }
                            updatedFriends.add(friend);
                            LOG.debugf("user {0} updated", friend);
                        }
                        rosterListeners.broadcast(new RosterEvent(updatedFriends, RosterEvent.Type.FRIENDS_UPDATED));
                    }
                }
            } catch (FriendException e) {
                LOG.debugf(e, "error getting roster");
            }
        }

        @Override
        public void entriesDeleted(Collection<Jid> removedIds) {
            synchronized (friends) {
                List<Friend> deletedFriends = new ArrayList<Friend>();
                for (Jid jid : removedIds) {
                    String id = jid.asBareJid().toString();
                    XMPPFriendImpl friend = friends.remove(id);
                    if (friend != null) {
                        deletedFriends.add(friend);
                        LOG.debugf("user {0} removed", friend);
                    }
                }
                rosterListeners.broadcast(new RosterEvent(deletedFriends, RosterEvent.Type.FRIENDS_DELETED));
            }
        }

        @Override
        public void presenceChanged(final Presence presence) {
            String localJID;
            try {
                localJID = getLocalJid();
            } catch (FriendException e) {
                localJID = null;
            }
            String presenceFrom = presence.getFrom() != null ? presence.getFrom().toString() : null;
            if (presenceFrom == null || presenceFrom.equals(localJID)) {
                return;
            }

            XMPPFriendImpl friend = getFriend(presence);
            if (friend != null) {
                LOG.debugf("presence from {0} changed to {1}", presence.getFrom(), presence.getType());
                synchronized (friend) {
                    if (presence.getType() == Presence.Type.available) {
                        if (!friend.getPresences().containsKey(presenceFrom)) {
                            addNewPresence(friend, presence);
                        } else {
                            updatePresence(friend, presence);
                        }
                    } else if (presence.getType() == Presence.Type.unavailable) {
                        PresenceImpl p = (PresenceImpl) friend.getPresence(presenceFrom);
                        if (p != null) {
                            p.update(presence);
                            friend.removePresense(p);
                        }
                    }
                }
            } else {
                LOG.debugf("no friend for presence {0}", presence.getFrom());
            }
        }

        private XMPPFriendImpl getFriend(Presence presence) {
            synchronized (friends) {
                return friends.get(PresenceUtils.parseBareAddress(presence.getFrom().toString()));
            }
        }

        private void addNewPresence(final XMPPFriendImpl friend, final Presence presence) {
            final PresenceImpl presenceImpl = new PresenceImpl(presence, friend, featureSupport);
            presenceImpl.addTransport(AddressFeature.class, addressIQListener);
            presenceImpl.addTransport(AuthTokenFeature.class, authTokenIQListener);
            presenceImpl.addTransport(ConnectBackRequestFeature.class, connectRequestIQListener);
            presenceImpl.addTransport(LibraryChangedNotifierFeature.class, libraryChangedIQListener);
            presenceImpl.addTransport(FileOfferFeature.class, fileTransferIQListener);
            friend.addPresense(presenceImpl);
        }

        private void updatePresence(XMPPFriendImpl friend, Presence presence) {
            PresenceImpl currentPresence = (PresenceImpl) friend.getPresences().get(presence.getFrom().toString());
            currentPresence.update(presence);
            friend.updatePresence(currentPresence);
        }
    }

    @Override
    public boolean supportsAddRemoveFriend() {
        return true;
    }

    @Override
    public ListeningFuture<Void> addNewFriend(final String id, final String name) {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addFriendImpl(id, name);
                return null;
            }
        });
    }

    void addFriendImpl(String id, String name) throws FriendException {
        synchronized (this) {
            try {
                checkLoggedIn();
                Roster.getInstanceFor(connection).createItemAndRequestSubscription(JidCreate.bareFrom(id), name, null);
            } catch (Exception e) {
                throw new FriendException(e);
            }
        }
    }

    @Override
    public ListeningFuture<Void> removeFriend(final String id) {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                removeFriendImpl(id);
                return null;
            }
        });
    }

    private void removeFriendImpl(String id) throws FriendException {
        synchronized (this) {
            try {
                checkLoggedIn();
                Roster roster = Roster.getInstanceFor(connection);
                RosterEntry entry = roster.getEntry(JidCreate.bareFrom(id));
                if (entry != null) {
                    roster.removeEntry(entry);
                }
            } catch (Exception e) {
                throw new FriendException(e);
            }
        }
    }

    @Override
    public XMPPFriendImpl getFriend(String id) {
        id = PresenceUtils.parseBareAddress(id);
        synchronized (friends) {
            return friends.get(id);
        }
    }

    @Override
    public Collection<Friend> getFriends() {
        synchronized (friends) {
            return new ArrayList<Friend>(friends.values());
        }
    }

    public void sendPacket(Stanza stanza) throws FriendException {
        synchronized (this) {
            try {
                checkLoggedIn();
                connection.sendStanza(stanza);
            } catch (Exception e) {
                throw new FriendException(e);
            }
        }
    }

    public String getLocalJid() throws FriendException {
        synchronized (this) {
            checkLoggedIn();
            if (connection.getUser() == null) {
                throw new FriendException("not connected");
            }
            return connection.getUser().toString();
        }
    }

    private class XmppActivityEventListener implements EventListener<XmppActivityEvent> {

        @Override
        public void handleEvent(XmppActivityEvent event) {
            switch (event.getSource()) {
            case Idle:
                try {
                    setModeImpl(FriendPresence.Mode.xa);
                } catch (FriendException e) {
                    LOG.debugf(e, "couldn't set mode based on {0}", event);
                }
                break;
            case Active:
                try {
                    setModeImpl(jabberSettings.isDoNotDisturbSet()
                            ? FriendPresence.Mode.dnd : FriendPresence.Mode.available);
                } catch (FriendException e) {
                    LOG.debugf(e, "couldn't set mode based on {0}", event);
                }
                break;
            default:
                break;
            }
        }
    }

    private class SmackConnectionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            LOG.debug("smack connection closed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            LOG.debug("smack connection closed with error", e);
            logoutImpl(e);
        }
    }
}
