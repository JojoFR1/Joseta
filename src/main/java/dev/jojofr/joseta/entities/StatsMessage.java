package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.TimeUtils;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;

public class StatsMessage {
    public final ConfigurationEntity config;
    public boolean isGlobal = false;
    public Color color;
    
    public long guildId;
    public long userId;
    public long botId;
    
    public UserEntity dbUser;
    
    public int messageCount = 0;
    public int countingMessages = 0;
    public int chainBreaks = 0;
    public int countingSpecialMessages = 0;
    public int chainBreaksSpecial = 0;
    public int countingSpecialMessagesLegacy = 0;
    public int chainBreaksSpecialLegacy = 0;
    
    public final Instant timestamp;
    
    public StatsMessage(long guildId, User user, long botId) {
        this.guildId = guildId;
        this.userId = user.getIdLong();
        this.botId = botId;
        
        this.color = user.retrieveProfile().complete().getAccentColor();
        
        this.config = BotCache.getConfiguration(guildId);
        Database.useHandle(handle -> {
            this.dbUser = handle.attach(UserDao.class).getById(userId, guildId);
            
            MessageDao messageDao = handle.attach(MessageDao.class);
            messageCount = messageDao.getMemberMessageCount(userId, guildId);
            
            if (this.config.countingChannelId != null) {
                countingMessages = messageDao.getMemberCountingMessageCount(userId, guildId, this.config.countingChannelId, botId, "[0-9]+");
                chainBreaks = messageDao.getMemberChainBreakCount(userId, guildId, this.config.countingChannelId, botId);
            }
            if (this.config.countingSpecialChannelId != null) {
                countingSpecialMessages = messageDao.getMemberCountingMessageCount(userId, guildId, this.config.countingSpecialChannelId, botId, "[0-9A-Za-z]+");
                chainBreaksSpecial = messageDao.getMemberChainBreakCount(userId, guildId, this.config.countingSpecialChannelId, botId);
            }
            if (guildId == 1219005659194851389L) { // Main server
                countingSpecialMessagesLegacy = messageDao.getMemberCountingMessageCount(userId, guildId, 1534307776963022848L, botId, "[0-9A-Za-z]+"); // Counting channel
                chainBreaksSpecialLegacy = messageDao.getMemberChainBreakCount(userId, guildId, 1534307776963022848L, botId);
            }
        });
        
        this.timestamp = Instant.now();
    }
    
    public String getVoiceTime() {
        if (dbUser == null) return "Os";
        return TimeUtils.formatTime(dbUser.timeVoice / 1000);
    }
    
    public double getSuccessRate(int success, int failures) {
        if (success + failures == 0) return 0;
        return (double) success / (success + failures) * 100;
    }
}
