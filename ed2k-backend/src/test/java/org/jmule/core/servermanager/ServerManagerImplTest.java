package org.jmule.core.servermanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.file.Files;

import org.limewire.ed2k.backend.WireShareEd2kPaths;
import org.jmule.core.networkmanager.InternalNetworkManager;
import org.junit.Test;

public class ServerManagerImplTest {

    @Test
    public void autoConnectAdvancesToNextServerAfterDisconnectWhileConnecting() throws Exception {
        ServerManagerImpl manager = new ServerManagerImpl();
        Server first = new Server("145.239.2.134", 4661);
        Server second = new Server("45.82.80.155", 5687);
        first.setStatus(Server.ServerStatus.CONNECTING);

        final String[] connectedAddress = new String[1];
        final int[] connectedPort = new int[1];
        InternalNetworkManager networkManager = (InternalNetworkManager) java.lang.reflect.Proxy.newProxyInstance(
            InternalNetworkManager.class.getClassLoader(),
            new Class[] { InternalNetworkManager.class },
            (proxy, method, args) -> {
                if ("connectToServer".equals(method.getName())) {
                    connectedAddress[0] = (String) args[0];
                    connectedPort[0] = ((Integer) args[1]).intValue();
                }
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                }
                if (returnType == int.class) {
                    return 0;
                }
                if (returnType == long.class) {
                    return 0L;
                }
                if (returnType == float.class) {
                    return 0f;
                }
                if (returnType == double.class) {
                    return 0d;
                }
                return null;
            }
        );

        setField(manager, "server_list", new ConcurrentLinkedQueue<Server>(Arrays.asList(first, second)));
        setField(manager, "connected_server", first);
        setField(manager, "auto_connect_started", Boolean.TRUE);
        setField(manager, "candidate_servers", new ArrayList<Server>(Arrays.asList(second)));
        setField(manager, "_network_manager", networkManager);

        manager.serverDisconnected(first.getAddress(), first.getPort());

        assertSame(second, manager.getConnectedServer());
        assertEquals(ServerManager.Status.CONNECTING, manager.getStatus());
        assertEquals(Server.ServerStatus.CONNECTING, second.getStatus());
        assertEquals(second.getAddress(), connectedAddress[0]);
        assertEquals(second.getPort(), connectedPort[0]);
    }

    @Test
    public void autoConnectPrefersRememberedLastConnectedServer() throws Exception {
        File root = Files.createTempDirectory("ed2k-server-priority").toFile();
        try {
            WireShareEd2kPaths.configure(new File(root, "incoming"), new File(root, "incomplete"), new File(root, "state"));
            WireShareEd2kPaths.rememberLastConnectedServer("45.82.80.155", 5687);

            ServerManagerImpl manager = new ServerManagerImpl();
            Server first = new Server("145.239.2.134", 4661);
            Server remembered = new Server("45.82.80.155", 5687);

            final String[] connectedAddress = new String[1];
            final int[] connectedPort = new int[1];
            InternalNetworkManager networkManager = (InternalNetworkManager) java.lang.reflect.Proxy.newProxyInstance(
                InternalNetworkManager.class.getClassLoader(),
                new Class[] { InternalNetworkManager.class },
                (proxy, method, args) -> {
                    if ("connectToServer".equals(method.getName())) {
                        connectedAddress[0] = (String) args[0];
                        connectedPort[0] = ((Integer) args[1]).intValue();
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    if (returnType == long.class) {
                        return 0L;
                    }
                    if (returnType == float.class) {
                        return 0f;
                    }
                    if (returnType == double.class) {
                        return 0d;
                    }
                    return null;
                }
            );

            setField(manager, "server_list", new ConcurrentLinkedQueue<Server>(Arrays.asList(first, remembered)));
            setField(manager, "_network_manager", networkManager);

            manager.connect();

            assertSame(remembered, manager.getConnectedServer());
            assertEquals(ServerManager.Status.CONNECTING, manager.getStatus());
            assertEquals(remembered.getAddress(), connectedAddress[0]);
            assertEquals(remembered.getPort(), connectedPort[0]);
        } finally {
            deleteRecursively(root);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = ServerManagerImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
