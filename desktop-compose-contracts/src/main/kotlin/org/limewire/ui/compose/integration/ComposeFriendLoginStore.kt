package org.limewire.ui.compose.integration

import org.limewire.ui.compose.FriendLoginDraft
import org.limewire.ui.compose.FriendLoginOption

interface ComposeFriendLoginStore {
    fun loginOptions(): List<FriendLoginOption>
    fun preferredLoginDraft(): FriendLoginDraft?
    fun loginDraftFor(label: String): FriendLoginDraft?
    fun saveLoginConfiguration(draft: FriendLoginDraft)
    fun submitLogin(draft: FriendLoginDraft)
}
