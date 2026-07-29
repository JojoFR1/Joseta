package dev.jojofr.joseta.database.daos;

import dev.jojofr.joseta.database.AbstractDaoTest;
import dev.jojofr.joseta.database.entities.GuildEntity;
import dev.jojofr.joseta.database.entities.MessageEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MessageDaoTests extends AbstractDaoTest<MessageEntity, MessageDao> {
    
    @Override
    protected Class<MessageDao> getDaoClass() { return MessageDao.class; }
    
    @Override
    protected MessageEntity buildDefault() { return new MessageEntity(11111L, 1L, 2L, 3L, "Hello World", "Hello World", Instant.now()); }
    
    @Override
    protected MessageEntity buildFull() {
        MessageEntity message = buildDefault();
        message.guildId = 2L;
        message.channelId = 3L;
        message.authorId = 4L;
        message.content = "Full Content";
        message.markovContent = "Full Markov Content";
        // Has to be different, and now is too close
        message.createdAt = Instant.now().minusSeconds(3600).truncatedTo(ChronoUnit.MICROS);
        
        return message;
    }
    
    @Override
    protected MessageEntity buildUpdated() {
        MessageEntity updatedMessage = buildDefault();
        updatedMessage.guildId = 1L;
        updatedMessage.channelId = 5L;
        updatedMessage.authorId = 6L;
        updatedMessage.content = "Updated Content";
        updatedMessage.markovContent = "Updated Markov Content";
        updatedMessage.createdAt = Instant.now().minusSeconds(1800).truncatedTo(ChronoUnit.MICROS);
        
        return updatedMessage;
    }
    
    @Override
    protected void upsert(MessageDao dao, MessageEntity entity) { dao.upsert(entity); }
    @Override
    protected MessageEntity fetch(MessageDao dao) { return dao.getById(11111L); }
}
