package org.limewire.ui.swing.friends.settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;

import org.limewire.core.api.xmpp.XMPPResourceFactory;
import org.limewire.friend.api.FriendConnectionFactory;
import org.limewire.friend.api.Network;
import org.limewire.friend.api.PasswordManager;
import org.limewire.io.UnresolvedIpPort;
import org.limewire.io.UnresolvedIpPortImpl;
import org.limewire.ui.compose.FriendLoginDraft;
import org.limewire.ui.compose.FriendLoginOption;
import org.limewire.ui.compose.integration.ComposeFriendLoginStore;
import org.limewire.ui.swing.settings.SwingUiSettings;

public final class ComposeFriendAccountCompat {

    private ComposeFriendAccountCompat() {
    }

    public static ComposeFriendLoginStore createLoginStore(
            FriendConnectionFactory friendConnectionFactory,
            PasswordManager passwordManager,
            XMPPResourceFactory xmppResourceFactory) {
        FriendAccountConfigurationManager manager =
                new ComposeFriendAccountConfigurationManager(passwordManager, xmppResourceFactory.getResource());
        return new LegacyComposeFriendLoginStore(friendConnectionFactory, manager);
    }

    private static final class LegacyComposeFriendLoginStore implements ComposeFriendLoginStore {
        private final FriendConnectionFactory friendConnectionFactory;
        private final FriendAccountConfigurationManager friendAccountConfigurationManager;

        LegacyComposeFriendLoginStore(
                FriendConnectionFactory friendConnectionFactory,
                FriendAccountConfigurationManager friendAccountConfigurationManager) {
            this.friendConnectionFactory = friendConnectionFactory;
            this.friendAccountConfigurationManager = friendAccountConfigurationManager;
        }

        @Override
        public List<FriendLoginOption> loginOptions() {
            List<FriendLoginOption> options = new ArrayList<FriendLoginOption>();
            for (FriendAccountConfiguration configuration : friendAccountConfigurationManager.getConfigurations()) {
                options.add(new FriendLoginOption(configuration.getLabel()));
            }
            return options;
        }

        @Override
        public FriendLoginDraft preferredLoginDraft() {
            FriendAccountConfiguration config = friendAccountConfigurationManager.getAutoLoginConfig();
            if (config == null) {
                List<FriendAccountConfiguration> configs = friendAccountConfigurationManager.getConfigurations();
                if (configs.isEmpty()) {
                    return null;
                }
                config = configs.get(0);
            }
            return loginDraftFor(config.getLabel());
        }

        @Override
        public FriendLoginDraft loginDraftFor(String label) {
            FriendAccountConfiguration config = configFor(label);
            if (config == null) {
                return null;
            }
            FriendAccountConfiguration autoLoginConfig = friendAccountConfigurationManager.getAutoLoginConfig();
            return new FriendLoginDraft(
                    config.getLabel(),
                    nullToEmpty(config.getServiceName()),
                    nullToEmpty(config.getUserInputLocalID()),
                    nullToEmpty(config.getPassword()),
                    config == autoLoginConfig);
        }

        @Override
        public void saveLoginConfiguration(FriendLoginDraft draft) {
            FriendAccountConfiguration config = applyLoginDraft(draft);
            if (config == null) {
                return;
            }
            if (draft.getAutoLogin()) {
                friendAccountConfigurationManager.setAutoLoginConfig(config);
            } else {
                friendAccountConfigurationManager.setAutoLoginConfig(null);
            }
        }

        @Override
        public void submitLogin(FriendLoginDraft draft) {
            FriendAccountConfiguration config = applyLoginDraft(draft);
            if (config == null) {
                return;
            }
            if (draft.getAutoLogin()) {
                friendAccountConfigurationManager.setAutoLoginConfig(config);
            } else {
                friendAccountConfigurationManager.setAutoLoginConfig(null);
            }
            friendConnectionFactory.login(config);
        }

        private FriendAccountConfiguration configFor(String label) {
            for (FriendAccountConfiguration config : friendAccountConfigurationManager.getConfigurations()) {
                if (config.getLabel().equals(label)) {
                    return config;
                }
            }
            return null;
        }

        private FriendAccountConfiguration applyLoginDraft(FriendLoginDraft draft) {
            FriendAccountConfiguration config = configFor(draft.getConfigLabel());
            if (config == null) {
                return null;
            }
            config.setUsername(draft.getUsername().trim());
            config.setPassword(draft.getPassword());
            if (JABBER_LABEL.equals(config.getLabel())) {
                config.setServiceName(draft.getServiceName().trim());
            }
            return config;
        }
    }

    private static final class ComposeFriendAccountConfigurationManager implements FriendAccountConfigurationManager {
        private final PasswordManager passwordManager;
        private final String resource;
        private final Map<String, FriendAccountConfiguration> configs = new LinkedHashMap<String, FriendAccountConfiguration>();

        private volatile boolean loaded;
        private volatile FriendAccountConfiguration autoLoginConfig;

        ComposeFriendAccountConfigurationManager(PasswordManager passwordManager, String resource) {
            this.passwordManager = passwordManager;
            this.resource = resource;
        }

        @Override
        public FriendAccountConfiguration getConfig(String label) {
            return rawConfigs().get(label);
        }

        @Override
        public List<String> getLabels() {
            List<String> labels = new ArrayList<String>();
            for (FriendAccountConfiguration configuration : getConfigurations()) {
                labels.add(configuration.getLabel());
            }
            return labels;
        }

        @Override
        public List<FriendAccountConfiguration> getConfigurations() {
            List<FriendAccountConfiguration> configurations =
                    new ArrayList<FriendAccountConfiguration>(rawConfigs().values());
            configurations.sort((left, right) ->
                    left.getLabel().toLowerCase(Locale.US).compareTo(right.getLabel().toLowerCase(Locale.US)));
            return configurations;
        }

        @Override
        public FriendAccountConfiguration getAutoLoginConfig() {
            ensureLoaded();
            return autoLoginConfig;
        }

        @Override
        public void setAutoLoginConfig(FriendAccountConfiguration config) {
            if (autoLoginConfig != null) {
                passwordManager.removePassword(autoLoginConfig.getUserInputLocalID());
                SwingUiSettings.XMPP_AUTO_LOGIN.set("");
                SwingUiSettings.USER_DEFINED_JABBER_SERVICENAME.set("");
                autoLoginConfig = null;
            }

            if (config == null) {
                return;
            }

            try {
                if (config.storePassword()) {
                    passwordManager.storePassword(config.getUserInputLocalID(), config.getPassword());
                }
                SwingUiSettings.XMPP_AUTO_LOGIN.set(config.getLabel() + "," + config.getUserInputLocalID());
                if (JABBER_LABEL.equals(config.getLabel())) {
                    SwingUiSettings.USER_DEFINED_JABBER_SERVICENAME.set(config.getServiceName());
                }
                autoLoginConfig = config;
            } catch (Exception ignored) {
            }
        }

        private Map<String, FriendAccountConfiguration> rawConfigs() {
            ensureLoaded();
            return configs;
        }

        private void ensureLoaded() {
            if (loaded) {
                return;
            }
            synchronized (this) {
                if (loaded) {
                    return;
                }
                loadWellKnownServers();
                loadCustomServer();
                loadAutoLoginAccount();
                loaded = true;
            }
        }

        private void loadCustomServer() {
            String custom = SwingUiSettings.USER_DEFINED_JABBER_SERVICENAME.get();
            if (custom == null || custom.isBlank()) {
                custom = DEFAULT_JABBER_SERVICE;
            }
            ComposeFriendAccountConfiguration configuration = new ComposeFriendAccountConfiguration(
                    false,
                    custom,
                    JABBER_LABEL,
                    resource,
                    Collections.<UnresolvedIpPort>emptyList(),
                    false);
            configs.put(configuration.getLabel(), configuration);
        }

        private void loadAutoLoginAccount() {
            String autoLogin = SwingUiSettings.XMPP_AUTO_LOGIN.get();
            if (autoLogin == null || autoLogin.isBlank()) {
                return;
            }

            int comma = autoLogin.indexOf(',');
            if (comma <= 0 || comma >= autoLogin.length() - 1) {
                return;
            }

            String label = autoLogin.substring(0, comma);
            String username = autoLogin.substring(comma + 1);
            FriendAccountConfiguration configuration = configs.get(label);
            if (configuration == null) {
                return;
            }

            try {
                configuration.setUsername(username);
                if (configuration.storePassword()) {
                    configuration.setPassword(passwordManager.loadPassword(username));
                }
                autoLoginConfig = configuration;
            } catch (IllegalArgumentException ignored) {
            } catch (IOException ignored) {
            }
        }

        private void loadWellKnownServers() {
            List<ComposeFriendAccountConfiguration> configurations = List.of(
                    new ComposeFriendAccountConfiguration(
                            true,
                            "gmail.com",
                            "Gmail",
                            resource,
                            List.of(
                                    new UnresolvedIpPortImpl("talk.1.google.com", 5222),
                                    new UnresolvedIpPortImpl("talk1.1.google.com", 5222),
                                    new UnresolvedIpPortImpl("talk2.1.google.com", 5222),
                                    new UnresolvedIpPortImpl("talk3.1.google.com", 5222),
                                    new UnresolvedIpPortImpl("talk4.1.google.com", 5222))),
                    new ComposeFriendAccountConfiguration(
                            false,
                            "livejournal.com",
                            "LiveJournal",
                            resource,
                            List.of(new UnresolvedIpPortImpl("xmpp.services.livejournal.com", 5222))));

            for (ComposeFriendAccountConfiguration configuration : configurations) {
                configs.put(configuration.getLabel(), configuration);
            }
        }
    }

    private static final class ComposeFriendAccountConfiguration implements FriendAccountConfiguration {
        private static final Icon SMALL_PLACEHOLDER_ICON = new PlaceholderIcon(16, 16);
        private static final Icon LARGE_PLACEHOLDER_ICON = new PlaceholderIcon(28, 28);

        private final boolean requireDomain;
        private final String resource;
        private final List<UnresolvedIpPort> defaultServers;
        private final boolean modifyUser;
        private final Icon icon;
        private final Icon largeIcon;
        private final Network.Type type;
        private final Map<String, Object> attributes = Collections.synchronizedMap(new LinkedHashMap<String, Object>());

        private volatile String serviceName;
        private volatile String label;
        private volatile String username = "";
        private volatile String canonicalId = "";
        private volatile String password = "";

        ComposeFriendAccountConfiguration(
                boolean requireDomain,
                String serviceName,
                String label,
                String resource,
                List<UnresolvedIpPort> defaultServers) {
            this(requireDomain, serviceName, label, resource, defaultServers, true, SMALL_PLACEHOLDER_ICON,
                    LARGE_PLACEHOLDER_ICON, Network.Type.XMPP);
        }

        ComposeFriendAccountConfiguration(
                boolean requireDomain,
                String serviceName,
                String label,
                String resource,
                List<UnresolvedIpPort> defaultServers,
                boolean modifyUser) {
            this(requireDomain, serviceName, label, resource, defaultServers, modifyUser, SMALL_PLACEHOLDER_ICON,
                    LARGE_PLACEHOLDER_ICON, Network.Type.XMPP);
        }

        ComposeFriendAccountConfiguration(
                boolean requireDomain,
                String serviceName,
                String label,
                String resource,
                List<UnresolvedIpPort> defaultServers,
                boolean modifyUser,
                Icon icon,
                Icon largeIcon,
                Network.Type type) {
            this.requireDomain = requireDomain;
            this.serviceName = serviceName;
            this.label = label;
            this.resource = resource;
            this.defaultServers = defaultServers;
            this.modifyUser = modifyUser;
            this.icon = icon;
            this.largeIcon = largeIcon;
            this.type = type;
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public String getServiceName() {
            return serviceName;
        }

        @Override
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public Icon getLargeIcon() {
            return largeIcon;
        }

        @Override
        public String getUserInputLocalID() {
            return username;
        }

        @Override
        public void setUsername(String username) {
            setCanonicalIdFromUsername(username);
            String updated = username;
            if (modifyUser) {
                int at = updated.indexOf('@');
                if (requireDomain && at == -1) {
                    updated = updated + "@" + getServiceName();
                } else if (!requireDomain && at > -1) {
                    updated = updated.substring(0, at);
                }
            }
            this.username = updated;
        }

        private void setCanonicalIdFromUsername(String username) {
            if (username.indexOf('@') > -1) {
                canonicalId = username.toLowerCase(Locale.US);
            } else {
                canonicalId = (username + "@" + getServiceName()).toLowerCase(Locale.US);
            }
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public boolean storePassword() {
            return true;
        }

        @Override
        public String getResource() {
            return resource;
        }

        @Override
        public String getCanonicalizedLocalID() {
            return canonicalId;
        }

        @Override
        public String getNetworkName() {
            return serviceName;
        }

        @Override
        public Network.Type getType() {
            return type;
        }

        @Override
        public org.limewire.listener.EventListener<org.limewire.friend.api.RosterEvent> getRosterListener() {
            return null;
        }

        @Override
        public List<UnresolvedIpPort> getDefaultServers() {
            return defaultServers;
        }

        @Override
        public void setAttribute(String key, Object property) {
            attributes.put(key, property);
        }

        @Override
        public Object getAttribute(String key) {
            return attributes.get(key);
        }
    }

    private static final class PlaceholderIcon implements Icon {
        private final int width;
        private final int height;

        private PlaceholderIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static final String JABBER_LABEL = "Jabber";
    private static final String DEFAULT_JABBER_SERVICE = "jabber.org";
}
