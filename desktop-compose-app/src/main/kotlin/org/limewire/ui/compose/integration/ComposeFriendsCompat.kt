package org.limewire.ui.compose.integration

import org.limewire.core.settings.FriendSettings
import org.limewire.friend.api.ChatState
import org.limewire.friend.api.Friend
import org.limewire.friend.api.FriendConnection
import org.limewire.friend.api.FriendConnectionEvent
import org.limewire.friend.api.FriendPresence
import org.limewire.friend.api.FriendRequestEvent
import org.limewire.listener.ListenerSupport
import org.limewire.ui.compose.ComposePerformanceTracker
import org.limewire.ui.compose.ConversationFileOffer
import org.limewire.ui.compose.ConversationMessage
import org.limewire.ui.compose.ConversationMessageKind
import org.limewire.ui.compose.FriendRosterItem
import org.limewire.ui.compose.PendingFriendRequest
import org.limewire.ui.compose.runOnUi
import java.awt.EventQueue
import java.io.File
import java.util.LinkedHashMap
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class SwingComposeFriendService(
    private val friendLoginStore: ComposeFriendLoginStore,
    private val chatBridge: ComposeChatCompatBridge,
    private val friendRequestListeners: ListenerSupport<FriendRequestEvent>
) : ComposeFriendService {
    private val listeners = CopyOnWriteArrayList<ComposeFriendService.Listener>()
    private val rosterItems = LinkedHashMap<String, FriendRosterItem>()
    private val pendingFriendRequests = LinkedHashMap<Long, PendingFriendRequest>()
    private var nextFriendRequestId = 1L

    @Volatile
    private var rosterSyncScheduled = false

    @Volatile
    private var active = false

    private val friendRequestListener = object : org.limewire.listener.EventListener<FriendRequestEvent> {
        override fun handleEvent(event: FriendRequestEvent) {
            if (event.type != FriendRequestEvent.Type.REQUESTED) {
                return
            }
            val pendingRequest = synchronized(this@SwingComposeFriendService) {
                PendingFriendRequest(
                    id = nextFriendRequestId++,
                    username = event.data.friendUsername,
                    request = event.data
                ).also { pendingFriendRequests[it.id] = it }
            }
            runOnUi {
                listeners.forEach { it.friendRequestReceived(pendingRequest) }
            }
        }
    }

    private val chatListener = object : ComposeChatCompatBridge.Listener {
        override fun connectionChanged(event: FriendConnectionEvent) {
            if (event.type == FriendConnectionEvent.Type.DISCONNECTED) {
                clearState()
            } else if (event.type == FriendConnectionEvent.Type.CONNECTED) {
                scheduleRosterSync()
            }
            listeners.forEach { it.connectionChanged(event) }
        }

        override fun messageReceived(message: ComposeChatCompatMessage) {
            listeners.forEach {
                it.messageReceived(
                    ConversationMessage(
                        id = message.id,
                        friendId = message.friendId,
                        senderName = message.senderName,
                        body = message.body,
                        timestamp = message.timestamp,
                        incoming = message.incoming,
                        outgoing = message.outgoing,
                        server = message.server,
                        kind = when (message.kind) {
                            ComposeChatCompatMessageKind.FILE_OFFER -> ConversationMessageKind.FILE_OFFER
                            ComposeChatCompatMessageKind.STATUS -> ConversationMessageKind.STATUS
                            ComposeChatCompatMessageKind.TEXT -> ConversationMessageKind.TEXT
                        },
                        fileOffer = message.fileOffer?.let { offer ->
                            ConversationFileOffer(
                                offerId = offer.offerId,
                                fileName = offer.fileName,
                                size = offer.size,
                                description = offer.description,
                                urns = offer.urns,
                                downloadState = offer.downloadState,
                                localPath = offer.localPath
                            )
                        }
                    )
                )
            }
            scheduleRosterSync()
        }

        override fun chatStateChanged(friendId: String, state: ChatState) {
            listeners.forEach { it.chatStateChanged(friendId, state) }
        }

        override fun conversationReady(friendId: String) {
            scheduleRosterSync()
            listeners.forEach { it.conversationReady(friendId) }
        }

        override fun rosterChanged() {
            scheduleRosterSync()
        }
    }

    override fun activate() {
        if (active) {
            return
        }
        active = true
        chatBridge.addListener(chatListener)
        friendRequestListeners.addListener(friendRequestListener)
        chatBridge.activate()
        syncRoster()
    }

    override fun deactivate() {
        if (!active) {
            return
        }
        active = false
        chatBridge.removeListener(chatListener)
        friendRequestListeners.removeListener(friendRequestListener)
        chatBridge.deactivate()
        clearState()
    }

    override fun addListener(listener: ComposeFriendService.Listener) {
        listeners.addIfAbsent(listener)
    }

    override fun removeListener(listener: ComposeFriendService.Listener) {
        listeners.remove(listener)
    }

    override fun roster(): List<FriendRosterItem> = synchronized(this) { rosterItems.values.toList() }

    override fun rosterItem(friendId: String): FriendRosterItem? = synchronized(this) { rosterItems[friendId] }

    override fun friendById(friendId: String): Friend? = chatBridge.friendById(friendId)

    override fun currentConnection(): FriendConnection? = chatBridge.currentConnection

    override fun lastConnectionEvent(): FriendConnectionEvent? = chatBridge.lastConnectionEvent

    override fun supportsAddRemove(): Boolean {
        val connection = currentConnection() ?: return false
        return connection.isLoggedIn && connection.supportsAddRemoveFriend()
    }

    override fun supportsPresenceModes(): Boolean {
        val connection = currentConnection() ?: return false
        return connection.isLoggedIn && connection.supportsMode()
    }

    override fun isDoNotDisturbEnabled(): Boolean = FriendSettings.DO_NOT_DISTURB.getValue()

    override fun loginOptions() = friendLoginStore.loginOptions()

    override fun preferredLoginDraft() = friendLoginStore.preferredLoginDraft()

    override fun loginDraftFor(label: String) = friendLoginStore.loginDraftFor(label)

    override fun saveLoginConfiguration(draft: org.limewire.ui.compose.FriendLoginDraft) {
        friendLoginStore.saveLoginConfiguration(draft)
    }

    override fun submitLogin(draft: org.limewire.ui.compose.FriendLoginDraft) {
        friendLoginStore.submitLogin(draft)
    }

    override fun logout() {
        currentConnection()?.logout()
    }

    override fun setDoNotDisturb(enabled: Boolean) {
        val connection = currentConnection() ?: return
        if (!connection.isLoggedIn || !connection.supportsMode()) {
            return
        }
        connection.setMode(if (enabled) FriendPresence.Mode.dnd else FriendPresence.Mode.available)
        FriendSettings.DO_NOT_DISTURB.setValue(enabled)
        scheduleRosterSync()
    }

    override fun addFriend(username: String, nickname: String) {
        val connection = currentConnection() ?: return
        if (!connection.supportsAddRemoveFriend()) {
            return
        }
        val trimmedUser = username.trim()
        if (trimmedUser.isEmpty()) {
            return
        }
        val normalizedId = if (trimmedUser.contains('@')) trimmedUser else "$trimmedUser@${connection.configuration.serviceName}"
        val normalizedNickname = nickname.trim().ifEmpty { trimmedUser }
        connection.addNewFriend(normalizedId, normalizedNickname)
    }

    override fun removeFriend(friendId: String) {
        currentConnection()?.removeFriend(friendId)
    }

    override fun acceptFriendRequest(requestId: Long) {
        resolveFriendRequest(requestId, true)
    }

    override fun declineFriendRequest(requestId: Long) {
        resolveFriendRequest(requestId, false)
    }

    override fun markConversationViewed(friendId: String) {
        chatBridge.markConversationViewed(friendId)
        scheduleRosterSync()
    }

    override fun sendMessage(friendId: String, text: String) {
        chatBridge.sendMessage(friendId, text)
    }

    override fun setChatState(friendId: String, state: ChatState) {
        runCatching {
            chatBridge.setChatState(friendId, state)
        }
    }

    override fun closeConversation(friendId: String) {
        chatBridge.closeConversation(friendId)
        scheduleRosterSync()
    }

    override fun canBrowseFriendLibrary(friendId: String): Boolean = chatBridge.supportsBrowse(friendId)

    override fun supportsOffTheRecord(friendId: String): Boolean = chatBridge.supportsOffTheRecord(friendId)

    override fun isOffTheRecordEnabled(friendId: String): Boolean = chatBridge.isOffTheRecord(friendId)

    override fun toggleOffTheRecord(friendId: String) {
        chatBridge.toggleOffTheRecord(friendId)
        scheduleRosterSync()
    }

    override fun supportsFileOffers(friendId: String): Boolean = chatBridge.supportsFileOffers(friendId)

    override fun offerFile(friendId: String, file: File) {
        chatBridge.offerFile(friendId, file)
    }

    override fun offerFolder(friendId: String, folder: File) {
        chatBridge.offerFolder(friendId, folder)
    }

    override fun downloadFileOffer(messageId: String) {
        chatBridge.downloadFileOffer(messageId)
    }

    private fun scheduleRosterSync() {
        if (!active) {
            return
        }
        synchronized(this) {
            if (rosterSyncScheduled) {
                return
            }
            rosterSyncScheduled = true
        }
        EventQueue.invokeLater {
            synchronized(this) {
                rosterSyncScheduled = false
            }
            if (!active) {
                return@invokeLater
            }
            syncRoster()
        }
    }

    private fun syncRoster() {
        val rosterEntries = chatBridge.rosterSnapshot()
        runOnUi {
            ComposePerformanceTracker.measure("friends.syncRoster") {
                val rosterSnapshot = synchronized(this@SwingComposeFriendService) {
                    val nextSnapshot = rosterEntries
                        .sortedWith(
                            compareByDescending<ComposeChatRosterEntry> { it.signedIn }
                                .thenByDescending { it.unreadMessages }
                                .thenBy { it.displayName.lowercase(Locale.US) }
                        )
                        .map(::rosterItemFor)

                    if (nextSnapshot != rosterItems.values.toList()) {
                        rosterItems.clear()
                        nextSnapshot.forEach { rosterItems[it.id] = it }
                        nextSnapshot
                    } else {
                        null
                    }
                }
                rosterSnapshot?.let { snapshot ->
                    listeners.forEach { it.rosterChanged(snapshot) }
                }
            }
        }
    }

    private fun clearState() {
        synchronized(this) {
            rosterSyncScheduled = false
            rosterItems.clear()
            pendingFriendRequests.clear()
        }
        listeners.forEach { it.rosterChanged(emptyList()) }
    }

    private fun resolveFriendRequest(requestId: Long, accept: Boolean) {
        val pending = synchronized(this) { pendingFriendRequests.remove(requestId) } ?: return
        runOnComposeBackground {
            pending.request.decisionHandler.handleDecision(pending.request.friendUsername, accept)
        }
    }

    private fun rosterItemFor(entry: ComposeChatRosterEntry): FriendRosterItem {
        return FriendRosterItem(
            id = entry.id,
            displayName = entry.displayName,
            status = entry.status,
            mode = entry.mode,
            signedIn = entry.signedIn,
            unreadMessages = entry.unreadMessages,
            browseable = entry.browseable,
            supportsOffTheRecord = entry.supportsOffTheRecord,
            supportsFileOffers = entry.supportsFileOffers
        )
    }
}
