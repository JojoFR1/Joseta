package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.AbstractDaoTest;
import dev.jojofr.joseta.database.entities.GuildEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class GuildDaoTests extends AbstractDaoTest<GuildEntity, GuildDao> {
    
    @Test
    void nextSanctionNumberIncrements() {
        GuildDao dao = getDao();
        upsert(dao, buildDefault());
        
        int sanctionNumber = dao.nextSanctionNumber(1L);
        int nextSanctionNumber = sanctionNumber + 1;
        
        assertThat(dao.nextSanctionNumber(1L)).isEqualTo(nextSanctionNumber);
    }
    
    @Override
    protected Class<GuildDao> getDaoClass() { return GuildDao.class; }
    
    @Override
    protected GuildEntity buildDefault() { return new GuildEntity(1L, "Guild", "url", 999L); }
    
    @Override
    protected GuildEntity buildFull() {
        GuildEntity guild = buildDefault();
        guild.name = "Full Guild";
        guild.iconUrl = "full_url";
        guild.ownerId = 777L;
        
        return guild;
    }
    
    @Override
    protected GuildEntity buildUpdated() {
        GuildEntity updatedGuild = buildDefault();
        updatedGuild.name = "Updated Guild";
        updatedGuild.iconUrl = "updated_url";
        updatedGuild.ownerId = 888L;
        updatedGuild.lastSanctionNumber = 43;
        
        return updatedGuild;
    }
    
    @Override
    protected void upsert(GuildDao dao, GuildEntity entity) { dao.upsert(entity); }
    @Override
    protected GuildEntity fetch(GuildDao dao) { return dao.getById(1L); }
    
    
    @Override
    protected Set<String> excludedDefaultFields() {
        return Set.of("lastSanctionNumber");
    }
}
