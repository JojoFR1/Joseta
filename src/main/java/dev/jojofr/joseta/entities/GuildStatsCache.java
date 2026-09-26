package dev.jojofr.joseta.entities;

import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.LeaderboardEntry;

import java.time.Instant;
import java.util.List;

public class GuildStatsCache {
    public final long guildId;
    public final int totalMessages;
    public final long totalVoiceTime;
    private final List<LeaderboardEntry> messageLeaderboard;
    private final List<LeaderboardEntry> voiceLeaderboard;
    
    public final Instant timestamp;
    
    public GuildStatsCache(long guildId, int totalMessages, long totalVoiceTime, List<LeaderboardEntry> messageLeaderboard, List<LeaderboardEntry> voiceLeaderboard) {
        this.guildId = guildId;
        this.totalMessages = totalMessages;
        this.totalVoiceTime = totalVoiceTime;
        this.messageLeaderboard = messageLeaderboard;
        this.voiceLeaderboard = voiceLeaderboard;
        this.timestamp = Instant.now();
    }
    
    public List<LeaderboardEntry> getMessageLeaderboard(int start, int end) {
        if (messageLeaderboard.size() < 10) return messageLeaderboard;
        if (end > messageLeaderboard.size()) {
            int offset = messageLeaderboard.size();
            int limit = 50;
            if (offset + limit < end)
                limit = (end - offset);
            
            if (limit % 50 != 0) limit += 50 - (limit % 50);
            
            int _limit = limit;
            List<LeaderboardEntry> updatedLeaderboard = Database.withHandle(handle -> handle.attach(MessageDao.class).getMessageLeaderboard(guildId, _limit, offset));
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
}
