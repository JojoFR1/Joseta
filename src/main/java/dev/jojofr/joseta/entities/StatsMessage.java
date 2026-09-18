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
    
    public final Instant timestamp;
    
    public StatsMessage(long guildId, User user, long botId) {
        this.guildId = guildId;
        this.userId = user.getIdLong();
        this.botId = botId;
        
        color = user.retrieveProfile().complete().getAccentColor();
        
        ConfigurationEntity config = BotCache.getConfiguration(guildId);
        Database.useHandle(handle -> {
            this.dbUser = handle.attach(UserDao.class).getById(userId, guildId);
            
            MessageDao messageDao = handle.attach(MessageDao.class);
            messageCount = messageDao.getMemberMessageCount(userId, guildId);
            
            if (config.countingChannelId != null) {
                countingMessages = messageDao.getMemberCountingMessageCount(userId, guildId, config.countingChannelId, botId, "[0-9]+");
                chainBreaks = messageDao.getMemberChainBreakCount(userId, guildId, config.countingChannelId, botId);
            }
            if (config.countingSpecialChannelId != null) {
                countingSpecialMessages = messageDao.getMemberCountingMessageCount(userId, guildId, config.countingSpecialChannelId, botId, "[0-9A-Za-z]+");
                chainBreaksSpecial = messageDao.getMemberChainBreakCount(userId, guildId, config.countingSpecialChannelId, botId);
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
