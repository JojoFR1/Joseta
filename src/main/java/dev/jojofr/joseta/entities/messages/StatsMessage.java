package dev.jojofr.joseta.entities.messages;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StatsMessage {
    public final ConfigurationEntity config;
    
    public boolean isGlobal = false;
    public int currentPage = 0;
    private int lastMessagePage = 0;
    private int lastVoicePage = 0;
    public char leaderboardType = 'm';
    public Color color;
    
    public long guildId;
    public long userId;
    public long botId;
    
    public UserEntity dbUser;
    
    public int messageCount = 0;
    public final Instant timestamp;
    
    public StatsMessage(long guildId, User user, long botId) {
        this.guildId = guildId;
        this.userId = user.getIdLong();
        this.botId = botId;
        
        this.config = BotCache.getConfiguration(guildId);
        this.timestamp = Instant.now();
    }
    
    public static CompletableFuture<StatsMessage> createAsync(long guildId, User user, long botId) {
        StatsMessage message = new StatsMessage(guildId, user, botId);
        
        CompletableFuture<Color> colorFuture = user.retrieveProfile().submit().thenApply(User.Profile::getAccentColor);
        
        CompletionStage<Void> dbFuture = Database.useHandleAsync(handle -> {
            UserDao userDao = handle.attach(UserDao.class);
            message.dbUser = userDao.getById(message.userId, guildId);
            message.lastVoicePage = userDao.getMemberCountWithVoiceInGuild(guildId) / 10;
            
            MessageDao messageDao = handle.attach(MessageDao.class);
            message.messageCount = messageDao.getMemberMessageCount(message.userId, guildId);
            
            message.lastMessagePage = messageDao.getAmountOfMembersWithMessages(guildId) / 10;
        });
        
        return colorFuture.thenCombine(dbFuture, (color, ignored) -> {
            message.color = color;
            return message;
        });
    }
    
    public String getVoiceTime() {
        if (dbUser == null) return "Os";
        return TimeUtils.formatTime(dbUser.timeVoice / 1000);
    }
    
    public double getSuccessRate(int success, int failures) {
        if (success + failures == 0) return 0;
        return (double) success / (success + failures) * 100;
    }
    
    public void previousPage() {
        if (currentPage > 0) currentPage--;
    }
    
    public void nextPage() {
        if (currentPage < lastMessagePage) currentPage++;
    }
    
    public int getLastPage() {
        return leaderboardType == 'm' ? lastMessagePage : lastVoicePage;
    }
}
