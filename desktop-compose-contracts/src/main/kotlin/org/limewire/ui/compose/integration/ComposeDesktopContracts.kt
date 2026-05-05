package org.limewire.ui.compose.integration

import org.limewire.ui.compose.ConnectionLayoutPreferences
import org.limewire.ui.compose.DownloadLayoutPreferences
import org.limewire.ui.compose.FileAssociationPromptState
import org.limewire.ui.compose.FriendsPaneLayoutPreferences
import org.limewire.ui.compose.LibraryLayoutPreferences
import org.limewire.ui.compose.LibraryPaneLayoutPreferences
import org.limewire.ui.compose.PreferencesDraft
import org.limewire.ui.compose.SaveDirectoryValidationResult
import org.limewire.ui.compose.SearchLayoutPreferences
import org.limewire.ui.compose.SearchPaneLayoutPreferences
import org.limewire.ui.compose.SettingsApplyResult
import org.limewire.ui.compose.TorrentEngineHealthState
import org.limewire.ui.compose.TransferPaneLayoutPreferences
import org.limewire.ui.compose.TrayBehaviorPreferences
import org.limewire.ui.compose.UploadLayoutPreferences
import org.limewire.ui.compose.WindowPlacementPreferences
import java.awt.Component
import java.io.File
import java.util.Locale

interface ComposeLocalizationService {
    fun translate(text: String, vararg args: Any?): String
    fun availableLocales(): List<Locale>
    fun currentLocale(): Locale
    fun applyLocale(locale: Locale)
}

interface ComposeSettingsService {
    fun loadPreferences(): PreferencesDraft
    fun applyPreferences(draft: PreferencesDraft): SettingsApplyResult
    fun validateSaveDirectory(path: String): SaveDirectoryValidationResult
    fun torrentEngineHealthState(): TorrentEngineHealthState
    fun torrentPromptBeforeDownloadingEnabled(): Boolean
    fun setTorrentPromptBeforeDownloadingEnabled(enabled: Boolean)
    fun clearDownloadsWhenFinished(): Boolean
    fun setClearDownloadsWhenFinished(enabled: Boolean)
    fun clearUploadsWhenFinished(): Boolean
    fun setClearUploadsWhenFinished(enabled: Boolean)
    fun showUploadsInTray(): Boolean
    fun setShowUploadsInTray(enabled: Boolean)
    fun resolveConnectionHostnamesEnabled(): Boolean
    fun setResolveConnectionHostnamesEnabled(enabled: Boolean)
    fun shouldShowSetupAssociationsPage(): Boolean
    fun validateCurrentSaveDirectory(): String?
    fun startupFileAssociationPrompt(): FileAssociationPromptState?
    fun resolveStartupFileAssociationPrompt(reassociate: Boolean, warnOnChange: Boolean)
    fun playerVolume(): Float
    fun setPlayerVolume(value: Float)
    fun playerEnabled(): Boolean
    fun clearSearchHistory()
    fun recordSearchHistoryEntry(query: String)
    fun resetSpamFilter()
    fun resetWarnings()
    fun confirmBlockUsersEnabled(): Boolean
    fun setConfirmBlockUsersEnabled(enabled: Boolean)
    fun confirmRemoveFileInfoSharingEnabled(): Boolean
    fun setConfirmRemoveFileInfoSharingEnabled(enabled: Boolean)
    fun defaultBlockedExtensions(): List<String>
    fun loadWindowPlacementPreferences(): WindowPlacementPreferences
    fun saveWindowPlacementPreferences(preferences: WindowPlacementPreferences)
    fun loadTrayBehaviorPreferences(): TrayBehaviorPreferences
    fun saveTrayBehaviorPreferences(preferences: TrayBehaviorPreferences)
    fun loadSearchLayoutPreferences(): SearchLayoutPreferences
    fun saveSearchLayoutPreferences(preferences: SearchLayoutPreferences)
    fun loadLibraryLayoutPreferences(): LibraryLayoutPreferences
    fun saveLibraryLayoutPreferences(preferences: LibraryLayoutPreferences)
    fun loadDownloadLayoutPreferences(): DownloadLayoutPreferences
    fun saveDownloadLayoutPreferences(preferences: DownloadLayoutPreferences)
    fun loadUploadLayoutPreferences(): UploadLayoutPreferences
    fun saveUploadLayoutPreferences(preferences: UploadLayoutPreferences)
    fun loadSearchPaneLayoutPreferences(): SearchPaneLayoutPreferences
    fun saveSearchPaneLayoutPreferences(preferences: SearchPaneLayoutPreferences)
    fun loadLibraryPaneLayoutPreferences(): LibraryPaneLayoutPreferences
    fun saveLibraryPaneLayoutPreferences(preferences: LibraryPaneLayoutPreferences)
    fun showLibraryOverlayMessageEnabled(): Boolean
    fun setShowLibraryOverlayMessageEnabled(enabled: Boolean)
    fun showSharingOverlayMessageEnabled(): Boolean
    fun setShowSharingOverlayMessageEnabled(enabled: Boolean)
    fun warnSharingDocumentsWithWorldEnabled(): Boolean
    fun setWarnSharingDocumentsWithWorldEnabled(enabled: Boolean)
    fun removeDocumentsFromPublicLists()
    fun loadFriendsPaneLayoutPreferences(): FriendsPaneLayoutPreferences
    fun saveFriendsPaneLayoutPreferences(preferences: FriendsPaneLayoutPreferences)
    fun loadTransferPaneLayoutPreferences(): TransferPaneLayoutPreferences
    fun saveTransferPaneLayoutPreferences(preferences: TransferPaneLayoutPreferences)
    fun loadConnectionLayoutPreferences(): ConnectionLayoutPreferences
    fun saveConnectionLayoutPreferences(preferences: ConnectionLayoutPreferences)
}

interface ComposeConsoleSettings {
    fun patternLayout(): String
}

interface ComposeDiagnosticsSettings {
    fun logBugsLocally(): Boolean
    fun bugLogFile(): File
    fun maxBugFileSizeBytes(): Long
}

interface ComposeMojitoVisualizerSession : AutoCloseable {
    val title: String
    fun component(): Component?
}

interface ComposeMojitoVisualizerPlugin {
    fun openSession(): ComposeMojitoVisualizerSession?
}
