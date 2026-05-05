package org.limewire.ui.compose.integration

import com.google.inject.AbstractModule
import com.google.inject.ConfigurationException
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import org.limewire.core.api.xmpp.XMPPResourceFactory
import org.limewire.friend.api.FriendRequestEvent
import org.limewire.friend.api.FriendConnectionFactory
import org.limewire.friend.api.PasswordManager
import org.limewire.listener.EventBroadcaster
import org.limewire.listener.EventMulticaster
import org.limewire.listener.EventMulticasterImpl
import org.limewire.listener.ListenerSupport
import org.limewire.ui.swing.friends.chat.ChatMessageEvent
import org.limewire.ui.swing.friends.chat.ChatStateEvent
import org.limewire.ui.swing.friends.chat.ComposeChatBridge
import org.limewire.ui.swing.friends.chat.SwingComposeChatCompatBridge
import org.limewire.ui.swing.friends.settings.ComposeFriendAccountCompat
import org.limewire.ui.swing.plugin.SwingUiPlugin
import java.awt.Component

fun legacyComposeSwingCompatModule(): AbstractModule {
    return object : AbstractModule() {
        override fun configure() {
            bind(SwingUiPlugin::class.java)
                .annotatedWith(Names.named("MojitoArcsPlugin"))
                .to(ComposeMojitoArcsPlugin::class.java)
        }
    }
}

fun createLegacyFriendLoginStore(injector: Injector): ComposeFriendLoginStore {
    return ComposeFriendAccountCompat.createLoginStore(
        injector.getInstance(FriendConnectionFactory::class.java),
        injector.getInstance(PasswordManager::class.java),
        injector.getInstance(XMPPResourceFactory::class.java)
    )
}

fun createLegacyChatCompatBridge(injector: Injector): ComposeChatCompatBridge {
    val composeFriendsInjector = createComposeFriendsInjector(injector)
    return SwingComposeChatCompatBridge(composeFriendsInjector.getInstance(ComposeChatBridge::class.java))
}

fun findOptionalMojitoVisualizerPlugin(injector: Injector): ComposeMojitoVisualizerPlugin? {
    val plugin = injector.findOptionalInstance(
        Key.get(SwingUiPlugin::class.java, Names.named("MojitoArcsPlugin"))
    ) ?: return null
    return SwingComposeMojitoVisualizerPlugin(plugin)
}

private fun createComposeFriendsInjector(injector: Injector): Injector {
    return injector.createChildInjector(object : AbstractModule() {
        override fun configure() {
            val chatMessageListenerManager = EventMulticasterImpl<ChatMessageEvent>()
            bind(object : TypeLiteral<EventBroadcaster<ChatMessageEvent>>() {})
                .toInstance(chatMessageListenerManager)
            bind(object : TypeLiteral<ListenerSupport<ChatMessageEvent>>() {})
                .toInstance(chatMessageListenerManager)
            bind(object : TypeLiteral<EventMulticaster<ChatMessageEvent>>() {})
                .toInstance(chatMessageListenerManager)

            val chatStateListenerManager = EventMulticasterImpl<ChatStateEvent>()
            bind(object : TypeLiteral<EventBroadcaster<ChatStateEvent>>() {})
                .toInstance(chatStateListenerManager)
            bind(object : TypeLiteral<ListenerSupport<ChatStateEvent>>() {})
                .toInstance(chatStateListenerManager)
            bind(object : TypeLiteral<EventMulticaster<ChatStateEvent>>() {})
                .toInstance(chatStateListenerManager)
        }
    })
}

private fun <T> Injector.findOptionalInstance(key: Key<T>): T? {
    return try {
        getInstance(key)
    } catch (_: ConfigurationException) {
        null
    }
}

private class SwingComposeMojitoVisualizerPlugin(
    private val plugin: SwingUiPlugin
) : ComposeMojitoVisualizerPlugin {
    override fun openSession(): ComposeMojitoVisualizerSession? {
        val component = plugin.pluginComponent ?: return null
        plugin.startPlugin()
        return object : ComposeMojitoVisualizerSession {
            override val title: String = plugin.pluginName

            override fun component(): Component = component

            override fun close() {
                plugin.stopPlugin()
            }
        }
    }
}
