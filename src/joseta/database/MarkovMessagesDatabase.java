package joseta.database;

import joseta.*;
import joseta.database.entry.*;

import arc.files.*;
import arc.struct.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.regex.*;
import java.util.stream.*;

public class MarkovMessagesDatabase {
    private static final String dbFileName =  "resources/database/markov_messages.db";
    private static Connection conn;

    private static final Pattern NO_URL_PATTERN = Pattern.compile("(https?://\\S+|www\\.\\S+[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\S*)");
    private static final Pattern CLEAN_COPY_PATTERN = Pattern.compile("[^a-z0-9.?!,;\\-()~\"'&$€£ \\]\\[àáâãäåæçèéêëìíîïñòóôõöùúûüýÿ ]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SPACED_PATTERN = Pattern.compile("\\s+");

    public static void initialize() {
        Fi dbFile = new Fi(dbFileName);
        try {
            if (!dbFile.exists()) {
                dbFile.write().close();
                
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
        stmt.close();
    }

    private static void populateNewTable() {
        int count = 0;
        JosetaBot.logger.debug("Populating the Markov Messages Database...");
        for (Guild guild : JosetaBot.bot.getGuilds()) {
            ConfigEntry config = ConfigDatabase.getConfig(guild.getIdLong());

            for (TextChannel channel : guild.getTextChannels()) {
                if (config.markovChannelBlackList.contains(channel.getIdLong())) continue;
                if (channel.isNSFW() || config.markovCategoryBlackList.contains(channel.getParentCategoryIdLong())) continue;

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
                long authorId = message.getAuthor().getIdLong();
                String content = message.getContentRaw();
                String timestamp = message.getTimeCreated().toString();

                addNewMessage(message, guild, channel, authorId, content, timestamp);
                count++;
            }
        } catch (Exception e) {
            JosetaBot.logger.error("Could not populate the Messages database.", e);
        }

        return count;
    }

    public static void addNewMessage(Message message, Guild guild, GuildMessageChannel channel, long authorId, String content, String timestamp) {
        long id = message.getIdLong();
        long guildId = guild.getIdLong();
        long channelId = channel.getIdLong();

        ConfigEntry config = ConfigDatabase.getConfig(guildId);
        
        if (message.getAuthor().isBot() || message.getAuthor().isSystem()) return;
        if (config.markovChannelBlackList.contains(channelId)) return;
        if (channel instanceof TextChannel textChannel &&
            (textChannel.isNSFW() || config.markovCategoryBlackList.contains(textChannel.getParentCategoryIdLong()))) return;

        
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO messages "
                                                           + "(id, guildId, channelId, authorId, content, timestamp)"
                                                           + "VALUES (?, ?, ?, ?, ?, ?)"))
        {
            pstmt.setLong(1, id);
            pstmt.setLong(2, guildId);
            pstmt.setLong(3, channelId);
            pstmt.setLong(4, authorId);
            pstmt.setString(5, cleanMessage(content));
            pstmt.setString(6, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not add a new message.", e);
        }
    }

    public static void updateMessage(long id, long guildId, long channelId, String content) {
        if (getMessageEntry(id, guildId, channelId) == null) return; // Check if message exists
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE messages SET content = ? "
                                                           + "WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setString(1, cleanMessage(content));
            pstmt.setLong(2, id);
            pstmt.setLong(3, guildId);
            pstmt.setLong(4, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not update a message.", e);
        }
    }
    
    private static String cleanMessage(String string) {
        String lower = string.toLowerCase().replace('\n', ' ').trim();
        String noUrl = NO_URL_PATTERN.matcher(lower).replaceAll("");
        String cleanCopy = CLEAN_COPY_PATTERN.matcher(noUrl).replaceAll("").replace('.', ' ');
        String spaced = SPACED_PATTERN.matcher(cleanCopy).replaceAll(" ");

        return spaced;
    }

    public static void deleteMessage(long id, long guildId, long channelId) {
        if (getMessageEntry(id, guildId, channelId) == null) return; // Check if message exists
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM messages WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setLong(1, id);
            pstmt.setLong(2, guildId);
            pstmt.setLong(3, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not delete a message.", e);
        }
    }

    public static void deleteChannelMessages(long guildId, long channelId) {
        // Remove all messages from the deleted channel
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM messages WHERE channelId = ? AND guildId = ?")) {
            pstmt.setLong(1, channelId);
            pstmt.setLong(2, guildId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not delete messages from a deleted channel.", e);
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
                   Instant.parse(rs.getString("timestamp"))
                );
            }
            pstmt.close();
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
                    Instant.parse(rs.getString("timestamp"))
                ));
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not retrieve message entries.", e);
        }
        return entries;
    }
}