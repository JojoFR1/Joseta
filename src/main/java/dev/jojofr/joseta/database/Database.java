package dev.jojofr.joseta.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jojofr.joseta.database.daos.MarkovBlacklistDao;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.utils.Log;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.async.JdbiExecutor;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.ExtensionCallback;
import org.jdbi.v3.core.extension.ExtensionConsumer;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Types;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {
    private static HikariDataSource dataSource;
    private static Jdbi jdbi;
    
    private static ExecutorService executorService;
    private static JdbiExecutor executor;
    
    public static boolean initialize(String user, String password, String host, String port, String database) {
        if (user == null || user.isBlank()) {
            Log.err("Database user is not provided.");
            return false;
        }
        if (password == null || password.isBlank()) Log.warn("Database password is not provided.");
        
        if (host == null || host.isBlank()) Log.warn("Database host is not provided. Using default host 'localhost'.");
        if (port == null || port.isBlank()) Log.warn("Database port is not provided. Using default port 5432.");
        if (database == null || database.isBlank()) {
            Log.err("Database name is not provided.");
            return false;
        }
        
        String url = "jdbc:postgresql://"
                    + (host != null && !host.isBlank() ? host : "localhost") + ":"
                    + (port != null && !port.isBlank() ? port : "5432") + "/"
                    + database;
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setPoolName("JosetaHikariPool");
        
        try {
            dataSource = new HikariDataSource(config);
            
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:database")
                .validateMigrationNaming(true)
                .loggers("slf4j")
                .load()
                .migrate();
            
            jdbi = Jdbi.create(dataSource);
            jdbi.installPlugin(new SqlObjectPlugin());
            jdbi.installPlugin(new PostgresPlugin());
            
            jdbi.registerColumnMapper(SanctionEntity.SanctionType.class, (rs, col, ctx) -> {
                String value = rs.getString(col);
                if (value == null) return null;
                
                return switch (value.charAt(0)) {
                    case 'W' -> SanctionEntity.SanctionType.WARN;
                    case 'T' -> SanctionEntity.SanctionType.TIMEOUT;
                    case 'K' -> SanctionEntity.SanctionType.KICK;
                    case 'B' -> SanctionEntity.SanctionType.BAN;
                    default -> throw new IllegalArgumentException("Unknown SanctionType code: " + value);
                };
            });
            jdbi.registerColumnMapper(MarkovBlacklistDao.EntityType.class, (rs, col, ctx) -> {
                String value = rs.getString(col);
                if (value == null) return null;
                
                return MarkovBlacklistDao.EntityType.valueOf(value);
            });
            jdbi.registerArgument(new AbstractArgumentFactory<SanctionEntity.SanctionType>(Types.CHAR) {
                @Override
                protected Argument build(SanctionEntity.SanctionType value, ConfigRegistry config) {
                    return (position, statement, ctx) -> statement.setString(position, String.valueOf(value.code));
                }
            });
            jdbi.registerArgument(new AbstractArgumentFactory<MarkovBlacklistDao.EntityType>(Types.VARCHAR) {
                @Override
                protected Argument build(MarkovBlacklistDao.EntityType value, ConfigRegistry config) {
                    return (position, statement, ctx) -> statement.setString(position, value.name());
                }
            });
            
            executorService = Executors.newFixedThreadPool(8);
            executor = JdbiExecutor.create(jdbi, executorService);
            
            return true;
        } catch (Exception e) {
            Log.err("Database initialization failed.", e);
            return false;
        }
    }
    
    public static void close() {
        if (executorService != null) executorService.close();
        if (dataSource != null) dataSource.close();
    }
    
    public static Jdbi getJdbi() {
        if (jdbi == null) throw new IllegalStateException("The database is not initialized. Call Database.initialize(...) first.");
        return jdbi;
    }
    
    public static JdbiExecutor getExecutor() {
        if (executor == null) throw new IllegalStateException("The database is not initialized. Call Database.initialize(...) first.");
        return executor;
    }
    
    public static <R, E> R withExtension(Class<E> extensionType, ExtensionCallback<R, E, RuntimeException> callback) {
        return getJdbi().withExtension(extensionType, callback);
    }
    
    public static <R, E> CompletionStage<R> withExtensionAsync(Class<E> extensionType, ExtensionCallback<R, E, RuntimeException> callback) {
        return getExecutor().withExtension(extensionType, callback);
    }
    
    public static <E> void useExtension(Class<E> extensionType, ExtensionConsumer<E, RuntimeException> callback) {
        getJdbi().useExtension(extensionType, callback);
    }
    
    public static <E> CompletionStage<Void> useExtensionAsync(Class<E> extensionType, ExtensionConsumer<E, RuntimeException> callback) {
        return getExecutor().useExtension(extensionType, callback);
    }
    
    public static <R> R withHandle(HandleCallback<R, RuntimeException> callback) {
        return getJdbi().withHandle(callback);
    }
    
    public static <R> CompletionStage<R> withHandleAsync(HandleCallback<R, RuntimeException> callback) {
        return getExecutor().withHandle(callback);
    }
    
    public static void useHandle(HandleConsumer<RuntimeException> callback) {
        getJdbi().useHandle(callback);
    }
    
    public static CompletionStage<Void> useHandleAsync(HandleConsumer<RuntimeException> callback) {
        return getExecutor().useHandle(callback);
    }
    
    public static <R> R inTransaction(HandleCallback<R, RuntimeException> callback) {
        return getJdbi().inTransaction(callback);
    }
    
    public static <R> CompletionStage<R> inTransactionAsync(HandleCallback<R, RuntimeException> callback) {
        return getExecutor().inTransaction(callback);
    }
    
    public static void useTransaction(HandleConsumer<RuntimeException> callback) {
        getJdbi().useTransaction(callback);
    }
    
    public static CompletionStage<Void> useTransactionAsync(HandleConsumer<RuntimeException> callback) {
        return getExecutor().useTransaction(callback);
    }
}
