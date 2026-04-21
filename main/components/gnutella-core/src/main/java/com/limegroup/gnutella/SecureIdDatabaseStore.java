package com.limegroup.gnutella;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.limewire.collection.FixedsizeForgetfulHashMap;
import org.limewire.core.settings.SecuritySettings;
import org.limewire.inject.EagerSingleton;
import org.limewire.io.GUID;
import org.limewire.lifecycle.Asynchronous;
import org.limewire.lifecycle.Service;
import org.limewire.lifecycle.ServiceRegistry;
import org.limewire.lifecycle.ServiceStage;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;
import org.limewire.security.id.SecureIdStore;
import org.limewire.util.Base32;
import org.limewire.util.Clock;
import org.limewire.util.CommonUtils;

import com.google.inject.Inject;

@EagerSingleton
public class SecureIdDatabaseStore implements SecureIdStore, Service {
    
    private static final Log LOG = LogFactory.getLog(SecureIdDatabaseStore.class);
    
    private volatile DbStore store;
    private final Object storeLock = new Object();
    
    private final Map<GUID, byte[]> cache = new FixedsizeForgetfulHashMap<GUID, byte[]>(100);

    private final Clock clock;
    
    private volatile boolean resetDatabase;
    
    @Inject
    public SecureIdDatabaseStore(Clock clock) {
        this.clock = clock;
    }
    
    @Inject
    void register(ServiceRegistry serviceRegistry) {
        serviceRegistry.register(this).in(ServiceStage.VERY_LATE);
    }
        
    @Override
    public byte[] getLocalData() {
        String value = SecuritySettings.SECURE_IDENTITY.get();
        if (value.isEmpty()) {
            return null;
        }
        return Base32.decode(value);
    }
    
    @Override
    public void setLocalData(byte[] value) {
        SecuritySettings.SECURE_IDENTITY.set(Base32.encode(value));
        resetDatabase = true;
    }
    
    @Override
    public String getServiceName() {
        return "id db store";
    }

    @Override
    public void initialize() {
    }
    
    @Override
    @Asynchronous
    public void start() {
        try {
            store = openStore(resetDatabase);
            long aYearAgo = clock.now() - TimeUnit.DAYS.toMillis(365);
            synchronized (storeLock) {
                if (store != null) {
                    store.deleteOlderThan(aYearAgo);
                }
            }
        } catch (SQLException e) {
            LOG.warn("Unable to initialize secure id database store, continuing with in-memory cache only.", e);
            store = null;
        }
    }
    
    @Override
    public void stop() {
        synchronized (storeLock) {
            if (store != null) {
                store.stop();
                store = null;
            }
        }
    }

    @Override
    public byte[] get(GUID key) {
        synchronized (storeLock) {
            byte[] value = cache.get(key);
            if (value != null) {
                return value;
            }
            if (store == null) {
                return null;
            }
            value = store.get(key);
            if (value != null) {
                // only store non-null values in memory cache to avoid valid
                // items from being expelled, we might want to change this based
                // on usage
                cache.put(key, value);
            }
            return value;
        }
    }

    @Override
    public void put(GUID key, byte[] value) {
        synchronized (storeLock) {
            if (store == null) {
                cache.put(key, value);
                return;
            }
            boolean stored = store.put(key, value);
            if (stored) {
                cache.put(key, value);
            }
        }
    }

    private DbStore openStore(boolean dropDb) throws SQLException {
        try {
            return new DbStore(dropDb);
        } catch (SQLException firstFailure) {
            if (isClearlyStaleLock(firstFailure) && deleteSecureIdLockFile()) {
                LOG.warn("Deleted stale secure id database lock file and retrying startup.");
                return new DbStore(dropDb);
            }
            throw firstFailure;
        }
    }

    private boolean isClearlyStaleLock(SQLException failure) {
        String message = failure.getMessage();
        return message != null
                && message.contains("The database is already in use by another process")
                && message.contains("locked=false")
                && message.contains("valid=false");
    }

    private boolean deleteSecureIdLockFile() {
        File lockFile = new File(CommonUtils.getUserSettingsDir(), "secure-ids.lck");
        return !lockFile.exists() || lockFile.delete();
    }
    
    class DbStore {
        
        private final Connection connection;

        private final PreparedStatement getStatement;
        
        private final PreparedStatement putStatement;

        private final PreparedStatement deleteStatement;

        private final PreparedStatement updateStatement;
        
        public DbStore(boolean dropDb) throws SQLException {
            try {
                Class.forName("org.hsqldb.jdbcDriver");
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(e1);
            }
            File dbFile = new File(CommonUtils.getUserSettingsDir(), "secure-ids");
            String connectionUrl = "jdbc:hsqldb:file:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(connectionUrl, "sa", "");
            Statement statement = connection.createStatement();
            try {
                if (dropDb) {
                    statement.execute("drop table ids if exists");
                }
                statement.execute("create cached table ids (guid binary(16) primary key, timestamp bigint, data varbinary(250))");
            } catch (SQLException se) {
                LOG.debug("table already exists", se);
            }
            getStatement = connection.prepareStatement("select data from ids where guid = ?");
            updateStatement = connection.prepareStatement("update ids set timestamp = ? where guid = ?");
            putStatement = connection.prepareStatement("insert into ids values (?, ?, ?)");
            deleteStatement = connection.prepareStatement("delete from ids where timestamp < ?");
        }
        
        public synchronized byte[] get(GUID key) {
            try {
                getStatement.setBytes(1, key.bytes());
                ResultSet resultSet = getStatement.executeQuery();
                while (resultSet.next()) {
                    byte[] value = resultSet.getBytes(1);
                    if (value != null) {
                        updateStatement.setLong(1, clock.now());
                        updateStatement.setBytes(2, key.bytes());
                        updateStatement.execute();
                    }
                    return value;
                }
            } catch (SQLException e) {
                LOG.debug("error getting value", e);
            }
            return null;
        }

        public synchronized boolean put(GUID key, byte[] value) {
            try {
                putStatement.setBytes(1, key.bytes());
                putStatement.setLong(2, clock.now());
                putStatement.setBytes(3, value);
                putStatement.execute();
                return true;
            } catch (SQLException e) {
                LOG.debug("error putting value", e);
            }
            return false;
        }
        
        public synchronized void stop() {
            try {
                Statement statement = connection.createStatement();
                statement.execute("SHUTDOWN");
            } catch (SQLException e) {
                LOG.debug("error shutting down", e);
            }
        }
        
        public synchronized void deleteOlderThan(long timestamp) {
            try {
                deleteStatement.setLong(1, timestamp);
                deleteStatement.execute();
            } catch (SQLException e) {
                LOG.debug("error deleting old entries", e);
            }
        }

    }
}
