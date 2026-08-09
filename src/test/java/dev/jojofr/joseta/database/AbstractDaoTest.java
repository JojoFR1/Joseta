package dev.jojofr.joseta.database;

import dev.jojofr.joseta.database.daos.GuildDao;
import dev.jojofr.joseta.database.daos.MarkovBlacklistDao;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.GuildEntity;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.entities.UserEntity;
import org.assertj.core.api.SoftAssertions;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.sql.Types;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDaoTest<E, D> {
    private static volatile boolean databaseReady = false;
    static Jdbi jdbi;
    
    protected abstract Class<D> getDaoClass();
    protected abstract E buildDefault();
    protected abstract E buildFull();
    protected abstract E buildUpdated();
    
    protected abstract void upsert(D dao, E entity);
    protected abstract E fetch(D dao);
    
    protected Set<String> excludedFields() { return Set.of("id"); }
    protected Set<String> excludedDefaultFields() { return excludedFields(); }
    protected Set<String> excludedUpdatedFields() { return excludedFields(); }
    
    protected D getDao() { return jdbi.onDemand(getDaoClass()); }
    
    @BeforeAll
    void setup() throws IllegalAccessException {
        assertNoFieldsEqualsDefault(buildDefault(), buildFull());
        setupDatabase();
    }
    
    private synchronized static void setupDatabase() {
        if (databaseReady) return;
        
        SharedPostgresContainer postgres = SharedPostgresContainer.getInstance();
        
        Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:database")
            .validateMigrationNaming(true)
            .loggers("slf4j")
            .load()
            .migrate();
        
        jdbi = Jdbi.create(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
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
        
        jdbi.useExtension(GuildDao.class, dao -> dao.upsert(new GuildEntity(1L, "Guild", "url", 100L)));
        jdbi.useExtension(GuildDao.class, dao -> dao.upsert(new GuildEntity(2L, "dluiG", "lru", 200L)));
        jdbi.useExtension(UserDao.class, dao -> dao.upsert(new UserEntity(2L, 1L, "User", "url", Instant.now())));
        jdbi.useExtension(UserDao.class, dao -> dao.upsert(new UserEntity(3L, 1L, "User", "url", Instant.now())));
        databaseReady = true;
    }
    
    @Test
    void upsertPersistsEveryFieldOnInsert() {
        D dao = jdbi.onDemand(getDaoClass());
        
        E original = buildFull();
        upsert(dao, original);
        
        E fetched = fetch(dao);
        
        assertThat(fetched).usingRecursiveComparison().isEqualTo(original);
    }
    
    @Test
    void upsertPersistsEveryFieldOnUpdate() throws IllegalAccessException {
        D dao = jdbi.onDemand(getDaoClass());
        
        E original = buildFull();
        E updated = buildUpdated();
        assertEveryFieldChanged(original, updated);
        
        upsert(dao, original);
        upsert(dao, updated);
        
        E fetched = fetch(dao);
        
        assertThat(fetched).usingRecursiveComparison().isEqualTo(updated);
    }
    
    private void assertNoFieldsEqualsDefault(E base, E built) throws IllegalAccessException {
        SoftAssertions softly = new SoftAssertions();
        
        for (Field field : base.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (excludedFields().contains(field.getName())) continue;
            if (excludedDefaultFields().contains(field.getName())) continue;
            
            softly.assertThat(field.get(built))
                .as("Field '%s' still has its default value", field.getName())
                .isNotEqualTo(field.get(base));
        }
        
        softly.assertAll();
    }
    
    private void assertEveryFieldChanged(E original, E updated) throws IllegalAccessException {
        SoftAssertions softly = new SoftAssertions();
        
        for (Field field : original.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (excludedFields().contains(field.getName())) continue;
            if (excludedUpdatedFields().contains(field.getName())) continue;
            
            softly.assertThat(field.get(updated))
                .as("Field '%s' was not changed in the updated fixture", field.getName())
                .isNotEqualTo(field.get(original)).isNotNull();
        }
        
        softly.assertAll();
    }
}
