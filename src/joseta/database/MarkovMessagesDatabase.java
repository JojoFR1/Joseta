package joseta.database;

import joseta.*;

import arc.struct.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;

import java.io.*;
import java.sql.*;
import java.util.stream.*;

public class MarkovMessagesDatabase extends ListenerAdapter {
    private static final String dbFileName =  "resources/database/markov_messages.db";
    private static Connection conn;

    public static void initialize() {
        File dbFile = new File(dbFileName);
        try {
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);

                initializeTable();
                populateNewTable();
            } else conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
            
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not initialize the SQL table.", e);
        } catch (IOException e) {
            JosetaBot.logger.error("Could not create the 'markov_messages.db' file.", e);
        }
    }

    private static void initializeTable() throws SQLException {
        String messageTable = "CREATE TABLE messages ("
                            + "id BIGINT PRIMARY KEY,"
                            + "guildId BIGINT,"
                            + "channelId BIGINT,"
                            + "authorId BIGINT,"
                            + "content TEXT,"
                            + "timestamp TEXT"
                            + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(messageTable);
    }

    private static void populateNewTable() {
        int count = 0;
        JosetaBot.logger.debug("Populating the Markov Messages Database...");
        for (Guild guild : JosetaBot.bot.getGuilds()) {
            for (TextChannel channel : guild.getTextChannels()) {
                for (ThreadChannel thread : channel.getThreadChannels()) count += addChannelMessageHistory(thread, guild);
                count += addChannelMessageHistory(channel, guild);
            }

            JosetaBot.logger.debug("Populated Markov Messages Database with "+ count +" messages for guild: " + guild.getName() + " (" + guild.getId() + ")");
            count = 0;
        }
    }

    private static int addChannelMessageHistory(GuildMessageChannel channel, Guild guild) {
        int count = 0;        
        try {
            for (Message message : channel.getIterableHistory().takeAsync(10000).thenApply(list -> list.stream().collect(Collectors.toList())).get()) {
                if (message.getAuthor().isBot() || message.getAuthor().isSystem()) continue;
                // if (channelBlackList.contains(channel.getIdLong())) continue;
                

                long id = message.getIdLong();
                long guildId = guild.getIdLong();
                long channelId = channel.getIdLong();
                long authorId = message.getAuthor().getIdLong();
                String content = message.getContentRaw();
                String timestamp = message.getTimeCreated().toString();

                addNewMessage(id, guildId, channelId, authorId, content, timestamp);
                count++;
            }
        } catch (Exception e) {
            JosetaBot.logger.error("Could not populate the Messages database.", e);
        }

        return count;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return; // Ignore DMs
        
        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long authorId = event.getAuthor().getIdLong();
        String content = event.getMessage().getContentRaw();
        String timestamp = event.getMessage().getTimeCreated().toString();

        addNewMessage(id, guildId, channelId, authorId, content, timestamp);
    }

    private static void addNewMessage(long id, long guildId, long channelId, long authorId, String content, String timestamp) {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO messages "
                                                           + "(id, guildId, channelId, authorId, content, timestamp)"
                                                           + "VALUES (?, ?, ?, ?, ?, ?)"))
        {
            pstmt.setLong(1, id);
            pstmt.setLong(2, guildId);
            pstmt.setLong(3, channelId);
            pstmt.setLong(4, authorId);
            pstmt.setString(5, content);
            pstmt.setString(6, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not add a new message.", e);
        }
    }

    public static void updateMessage(long id, long guildId, long channelId, String content) {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE messages SET content = ? "
                                                           + "WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setString(1, content);
            pstmt.setLong(2, id);
            pstmt.setLong(3, guildId);
            pstmt.setLong(4, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not update a message.", e);
        }
    }

    public static void deleteMessage(long id, long guildId, long channelId) {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM messages WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setLong(1, id);
            pstmt.setLong(2, guildId);
            pstmt.setLong(3, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not delete a message.", e);
        }
    }

    public static MessageEntry getMessageEntry(long id, long guildId, long channelId) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM messages WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setLong(1, id);
            pstmt.setLong(2, guildId);
            pstmt.setLong(3, channelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new MessageEntry(
                    rs.getLong("id"),
                    rs.getLong("guildId"),
                    rs.getLong("channelId"),
                    rs.getLong("authorId"),
                    rs.getString("content"),
                    rs.getString("timestamp")
                );
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not retrieve a message.", e);
        }
        return null;
    }

    public static Seq<MessageEntry> getMessageEntries(long guildId) {
        Seq<MessageEntry> entries = Seq.with();
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM messages WHERE guildId = ?")) {
            pstmt.setLong(1, guildId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                entries.add(new MessageEntry(
                    rs.getLong("id"),
                    rs.getLong("guildId"),
                    rs.getLong("channelId"),
                    rs.getLong("authorId"),
                    rs.getString("content"),
                    rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not retrieve message entries.", e);
        }
        return entries;
    }

    public static class MessageEntry {
        public final long id;
        public final long guildId;
        public final long channelId;
        public final long authorId;
        public final String content;
        public final String timestamp;

        public MessageEntry(long id, long guildId, long channelId, long authorId, String content, String timestamp) {
            this.id = id;
            this.guildId = guildId;
            this.channelId = channelId;
            this.authorId = authorId;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}