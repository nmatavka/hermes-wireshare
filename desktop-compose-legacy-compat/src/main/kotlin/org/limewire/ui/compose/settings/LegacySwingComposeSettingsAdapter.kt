package org.limewire.ui.compose.settings

import com.google.inject.Provider
import org.limewire.bittorrent.TorrentManager
import org.limewire.bittorrent.TorrentManagerSettings
import org.limewire.collection.AutoCompleteDictionary
import org.limewire.core.api.daap.DaapManager
import org.limewire.core.api.file.CategoryManager
import org.limewire.core.api.library.LibraryManager
import org.limewire.core.api.library.SharedFileListManager
import org.limewire.core.api.network.NetworkManager
import org.limewire.core.api.spam.SpamManager
import org.limewire.ui.compose.integration.ComposeFriendLoginStore
import org.limewire.ui.compose.integration.ComposeSettingsService

class LegacySwingComposeSettingsAdapter(
    friendLoginStore: ComposeFriendLoginStore? = null,
    searchHistory: AutoCompleteDictionary? = null,
    spamManager: SpamManager? = null,
    daapManager: DaapManager? = null,
    categoryManager: CategoryManager? = null,
    libraryManager: LibraryManager? = null,
    sharedFileListManager: SharedFileListManager? = null,
    networkManager: NetworkManager? = null,
    connectionManager: org.limewire.core.api.connection.GnutellaConnectionManager? = null,
    torrentManager: Provider<TorrentManager>? = null,
    torrentManagerSettings: TorrentManagerSettings? = null
) : ComposeSettingsService by LegacySwingComposeSettingsBackend(
    friendLoginStore = friendLoginStore,
    searchHistory = searchHistory,
    spamManager = spamManager,
    daapManager = daapManager,
    categoryManager = categoryManager,
    libraryManager = libraryManager,
    sharedFileListManager = sharedFileListManager,
    networkManager = networkManager,
    connectionManager = connectionManager,
    torrentManager = torrentManager,
    torrentManagerSettings = torrentManagerSettings
)
