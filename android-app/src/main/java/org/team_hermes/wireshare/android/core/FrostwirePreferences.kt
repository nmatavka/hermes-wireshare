package org.team_hermes.wireshare.android.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object WireSharePreferences {

    lateinit var dataStore: DataStore<Preferences>
        private set

    fun initialize(context: Context) {
        dataStore = DataStoreManager.dataStore(context.applicationContext, "frostwire_prefs")
    }
}
