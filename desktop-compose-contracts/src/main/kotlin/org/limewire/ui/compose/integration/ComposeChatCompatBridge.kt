package org.limewire.ui.compose.integration

import org.limewire.core.api.download.DownloadException
import org.limewire.friend.api.ChatState
import org.limewire.friend.api.Friend
import org.limewire.friend.api.FriendConnection
import org.limewire.friend.api.FriendConnectionEvent
import org.limewire.friend.api.FriendException
import org.limewire.friend.api.FriendPresence
import org.limewire.io.InvalidDataException
import java.io.File

enum class ComposeChatCompatMessageKind {
    TEXT,
    FILE_OFFER,
    STATUS
}

data class ComposeChatCompatFileOffer(
    val offerId: String,
    val fileName: String,
    val size: Long,
    val description: String,
    val urns: List<String>,
    val downloadState: org.limewire.core.api.download.DownloadState?,
    val localPath: String?
)

data class ComposeChatCompatMessage(
    val id: String,
    val friendId: String,
    val senderName: String,
    val body: String,
    val timestamp: Long,
    val incoming: Boolean,
    val outgoing: Boolean,
    val server: Boolean,
    val kind: ComposeChatCompatMessageKind,
    val fileOffer: ComposeChatCompatFileOffer?
)

data class ComposeChatRosterEntry(
    val id: String,
    val displayName: String,
    val status: String,
    val mode: FriendPresence.Mode?,
    val signedIn: Boolean,
    val unreadMessages: Boolean,
    val browseable: Boolean,
    val supportsOffTheRecord: Boolean,
    val supportsFileOffers: Boolean
)

interface ComposeChatCompatBridge {
    interface Listener {
        fun connectionChanged(event: FriendConnectionEvent)
        fun messageReceived(message: ComposeChatCompatMessage)
        fun chatStateChanged(friendId: String, state: ChatState)
        fun conversationReady(friendId: String)
        fun rosterChanged()
    }

    val currentConnection: FriendConnection?
    val lastConnectionEvent: FriendConnectionEvent?

    fun activate()
    fun deactivate()
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
    fun rosterSnapshot(): List<ComposeChatRosterEntry>
    fun friendById(friendId: String): Friend?
    fun markConversationViewed(friendId: String)
    @Throws(FriendException::class)
    fun sendMessage(friendId: String, text: String)
    fun closeConversation(friendId: String)
    fun supportsBrowse(friendId: String): Boolean
    @Throws(FriendException::class)
    fun setChatState(friendId: String, state: ChatState)
    fun supportsOffTheRecord(friendId: String): Boolean
    fun isOffTheRecord(friendId: String): Boolean
    @Throws(FriendException::class)
    fun toggleOffTheRecord(friendId: String)
    fun supportsFileOffers(friendId: String): Boolean
    fun offerFile(friendId: String, file: File)
    fun offerFolder(friendId: String, folder: File)
    @Throws(DownloadException::class, InvalidDataException::class)
    fun downloadFileOffer(messageId: String)
}
