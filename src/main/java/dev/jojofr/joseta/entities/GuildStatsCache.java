package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.LeaderboardEntry;
import dev.jojofr.joseta.utils.BotCache;

import java.time.Instant;
import java.util.List;

public class GuildStatsCache {
    public final long guildId;
    public final int totalMessages;
    public final long totalVoiceTime;
    private final List<LeaderboardEntry> messageLeaderboard;
    private final List<LeaderboardEntry> voiceLeaderboard;
    private final List<LeaderboardEntry> countingLeaderboard;
    private final List<LeaderboardEntry> countingSpecialLeaderboard;
    
    public final Instant timestamp;
    
    public GuildStatsCache(long guildId, int totalMessages, long totalVoiceTime, List<LeaderboardEntry> messageLeaderboard, List<LeaderboardEntry> voiceLeaderboard, List<LeaderboardEntry> countingLeaderboard, List<LeaderboardEntry> countingSpecialLeaderboard) {
        this.guildId = guildId;
        this.totalMessages = totalMessages;
        this.totalVoiceTime = totalVoiceTime;
        this.messageLeaderboard = messageLeaderboard;
        this.voiceLeaderboard = voiceLeaderboard;
        this.countingLeaderboard = countingLeaderboard;
        this.countingSpecialLeaderboard = countingSpecialLeaderboard;
        this.timestamp = Instant.now();
    }
    
    public List<LeaderboardEntry> getMessageLeaderboard(int start, int end) {
        if (messageLeaderboard.size() < 10) return messageLeaderboard;
        if (end > messageLeaderboard.size()) {
            ConfigurationEntity config = BotCache.getConfiguration(guildId);
            long[] excludedChannels = { config.countingChannelId, config.countingSpecialChannelId, 1534307776963022848L };
            
            int offset = messageLeaderboard.size();
            int limit = 50;
            if (offset + limit < end)
                limit = (end - offset);
            
            if (limit % 50 != 0) limit += 50 - (limit % 50);
            
            int _limit = limit;
            List<LeaderboardEntry> updatedLeaderboard = Database.withHandle(handle -> handle.attach(MessageDao.class).getMessageLeaderboard(guildId, _limit, offset, excludedChannels));
            messageLeaderboard.addAll(updatedLeaderboard);
        }
        return messageLeaderboard.subList(Math.max(0, start), Math.min(messageLeaderboard.size(), end));
    }
    
    public List<LeaderboardEntry> getVoiceLeaderboard(int start, int end) {
        if (voiceLeaderboard.size() < 10) return voiceLeaderboard;
        if (end > voiceLeaderboard.size()) {
            int offset = voiceLeaderboard.size();
            int limit = 50;
            if (offset + limit < end)
                limit = (end - offset);
            
            if (limit % 50 != 0) limit += 50 - (limit % 50);
            
            int _limit = limit;
            List<LeaderboardEntry> updatedLeaderboard = Database.withHandle(handle -> handle.attach(UserDao.class).getVoiceLeaderboard(guildId, _limit, offset));
            voiceLeaderboard.addAll(updatedLeaderboard);
        }
        return voiceLeaderboard.subList(Math.max(0, start), Math.min(voiceLeaderboard.size(), end));
    }
    
    public List<LeaderboardEntry> getCountingLeaderboard(int start, int end) {
        if (countingLeaderboard.size() < 10) return countingLeaderboard;
        if (end > countingLeaderboard.size()) {
            int offset = countingLeaderboard.size();
            int limit = 50;
            if (offset + limit < end)
                limit = (end - offset);
            
            if (limit % 50 != 0) limit += 50 - (limit % 50);
            
            int _limit = limit;
            List<LeaderboardEntry> updatedLeaderboard = Database.withHandle(handle -> handle.attach(UserDao.class).getCountingLeaderboard(guildId, _limit, offset));
            countingLeaderboard.addAll(updatedLeaderboard);
        }
        return countingLeaderboard.subList(Math.max(0, start), Math.min(countingLeaderboard.size(), end));
    }
    
    public List<LeaderboardEntry> getCountingSpecialLeaderboard(int start, int end) {
        if (countingSpecialLeaderboard.size() < 10) return countingSpecialLeaderboard;
        if (end > countingSpecialLeaderboard.size()) {
            int offset = countingSpecialLeaderboard.size();
            int limit = 50;
            if (offset + limit < end)
                limit = (end - offset);
            
            if (limit % 50 != 0) limit += 50 - (limit % 50);
            
            int _limit = limit;
            List<LeaderboardEntry> updatedLeaderboard = Database.withHandle(handle -> handle.attach(UserDao.class).getCountingSpecialLeaderboard(guildId, _limit, offset));
            countingSpecialLeaderboard.addAll(updatedLeaderboard);
        }
        return countingSpecialLeaderboard.subList(Math.max(0, start), Math.min(countingSpecialLeaderboard.size(), end));
    }
}
