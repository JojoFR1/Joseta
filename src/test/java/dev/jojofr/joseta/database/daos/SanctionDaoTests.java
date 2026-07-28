package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.AbstractDaoTest;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Testcontainers
class SanctionDaoTests extends AbstractDaoTest<SanctionEntity, SanctionDao> {
    
    @Override
    protected Class<SanctionDao> getDaoClass() { return SanctionDao.class; }
    
    @Override
    protected SanctionEntity buildDefault() { return new SanctionEntity(1L, 1, SanctionEntity.SanctionType.TIMEOUT, 2L, 3L, "Reason", 3600L); }
    
    @Override
    protected SanctionEntity buildFull() {
        SanctionEntity sanction = buildDefault();
        sanction.type = SanctionEntity.SanctionType.BAN;
        sanction.reason = "Full Reason";
        sanction.createdAt = Instant.now().minusSeconds(500).truncatedTo(ChronoUnit.MICROS);
        sanction.expiresAt = sanction.createdAt.plusSeconds(1000).truncatedTo(ChronoUnit.MICROS);
        sanction.isExpired = true;
        sanction.isPermanent = true;
        
        return sanction;
    }
    
    @Override
    protected SanctionEntity buildUpdated() {
        SanctionEntity updatedSanction = buildDefault();
        updatedSanction.type = SanctionEntity.SanctionType.KICK;
        updatedSanction.reason = "Updated Reason";
        updatedSanction.createdAt = Instant.now().minusSeconds(250).truncatedTo(ChronoUnit.MICROS);
        updatedSanction.expiresAt = updatedSanction.createdAt.plusSeconds(500).truncatedTo(ChronoUnit.MICROS);
        updatedSanction.isExpired = false;
        updatedSanction.isPermanent = false;
        
        return updatedSanction;
    }
    
    @Override
    protected void upsert(SanctionDao dao, SanctionEntity entity) { dao.upsert(entity); }
    @Override
    protected SanctionEntity fetch(SanctionDao dao) { return dao.getByUserId(1L, 2L, 0, 1).getFirst(); }
    
    @Override
    protected Set<String> excludedFields() {
        return Set.of("guildId", "sanctionNumber", "userId", "moderatorId");
    }
}
