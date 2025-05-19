package joseta.database;

import joseta.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;

import java.io.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class MessagesDatabase extends ListenerAdapter {
    private static final String dbFileName = "resources/database/messages.db";
    private static Connection conn;

    public static void initialize() {
        File dbFile = new File(dbFileName);
        try {
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);

                initializeTable();
            } else conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
            
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not initialize the SQL table.", e);
        } catch (IOException e) {
            JosetaBot.logger.error("Could not create the 'messages.db' file.", e);
        }

        // for (Guild guild : JosetaBot.bot.getGuilds()) {
        //     for (TextChannel channel : guild.getTextChannels()) {
        //         System.out.println("Fetching messages from channel: " + channel.getName() + " in guild: " + guild.getName());
        //         try {
        //             for (Message message : channel.getIterableHistory().takeAsync(10000).thenApply(list -> list.stream().collect(Collectors.toList())).get()) {
        //                 long id = message.getIdLong();
        //                 long guildId = guild.getIdLong();
        //                 long channelId = channel.getIdLong();
        //                 long authorId = message.getAuthor().getIdLong();
        //                 String content = message.getContentRaw();
        //                 String timestamp = message.getTimeCreated().toString();
        //                 boolean edited = message.isEdited();

        //                 addNewMessage(id, guildId, channelId, authorId, content, timestamp);
        //             }
        //         } catch (InterruptedException e) {
        //             e.printStackTrace();
        //         } catch (ExecutionException e) {
        //             e.printStackTrace();
        //         }
        //     }
        // }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        long id = event.getMessageIdLong();

        // Check if message already exists in the database
        try (PreparedStatement pstmtCheck = conn.prepareStatement("SELECT 1 FROM messages WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmtCheck.setLong(1, id);
            pstmtCheck.setLong(2, event.getGuild().getIdLong());
            pstmtCheck.setLong(3, event.getChannel().getIdLong());
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next()) {
                    // Message already exists, so do not add it again.
                    return;
                }
            }
        } catch (SQLException e) {
            JosetaBot.logger.error("Failed to check for existing message " + id + ". Proceeding with add attempt.", e);
        }

        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        long authorId = event.getAuthor().getIdLong();
        String content = event.getMessage().getContentRaw();
        String timestamp = event.getMessage().getTimeCreated().toString();

        addNewMessage(id, guildId, channelId, authorId, content, timestamp);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        String content = event.getMessage().getContentRaw();
        boolean edited = true;

        updateMessage(id, guildId, channelId, content, edited);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        long id = event.getMessageIdLong();
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();

        deleteMessage(id, guildId, channelId);
    }

    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        if (event.getChannel().getType().isGuild()) {
            long guildId = event.getGuild().getIdLong();
            long channelId = event.getChannel().getIdLong();

            for (String id : event.getMessageIds()) {
                deleteMessage(Long.parseLong(id), guildId, channelId);
            }
        }
    }

    
    private static void initializeTable() throws SQLException {
        String messageTable = "CREATE TABLE messages ("
                            + "id BIGINT PRIMARY KEY,"
                            + "guildId BIGINT,"
                            + "channelId BIGINT,"
                            + "authorId BIGINT,"
                            + "content TEXT,"
                            + "timestamp TEXT,"
                            + "edited BOOLEAN DEFAULT FALSE"
                            + ")";


        Statement stmt = conn.createStatement();
        stmt.execute(messageTable);
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

    private static void updateMessage(long id, long guildId, long channelId, String content, boolean edited) {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE messages SET content = ?, edited = ? "
                                                           + "WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setString(1, content);
            pstmt.setBoolean(2, edited);
            pstmt.setLong(3, id);
            pstmt.setLong(4, guildId);
            pstmt.setLong(5, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not update a message.", e);
        }
    }

    private static void deleteMessage(long id, long guildId, long channelId) {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM messages WHERE id = ? AND guildId = ? AND channelId = ?")) {
            pstmt.setLong(1, id);
            pstmt.setLong(2, guildId);
            pstmt.setLong(3, channelId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not delete a message.", e);
        }
    }
}
