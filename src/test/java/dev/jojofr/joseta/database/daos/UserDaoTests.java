package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.AbstractDaoTest;
import dev.jojofr.joseta.database.entities.UserEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Testcontainers
class UserDaoTests extends AbstractDaoTest<UserEntity, UserDao> {
    
    @Override
    protected Class<UserDao> getDaoClass() { return UserDao.class; }
    
    @Override
    protected UserEntity buildDefault() { return new UserEntity(99L, 1L, "User", "url", Instant.now()); }
    
    @Override
    protected UserEntity buildFull() {
        UserEntity user = buildDefault();
        user.name = "Full User";
        user.avatarUrl = "full_url";
        user.creationDate = Instant.now().minusSeconds(3600).truncatedTo(ChronoUnit.MICROS);
        user.sanctionCount = 10;
        
        return user;
    }
    
    @Override
    protected UserEntity buildUpdated() {
        UserEntity updatedUser = buildDefault();
        updatedUser.name = "Updated User";
        updatedUser.avatarUrl = "updated_url";
        updatedUser.creationDate = Instant.now().minusSeconds(1800).truncatedTo(ChronoUnit.MICROS);
        updatedUser.sanctionCount = 20;
        
        return updatedUser;
    }
    
    @Override
    protected void upsert(UserDao dao, UserEntity entity) { dao.upsert(entity); }
    @Override
    protected UserEntity fetch(UserDao dao) { return dao.getById(99L, 1L); }
    
    @Override
    protected Set<String> excludedFields() { return Set.of("id", "guildId"); }
}
