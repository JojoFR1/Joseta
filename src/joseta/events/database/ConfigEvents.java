package joseta.events.database;

import joseta.*;
import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.events.guild.*;

import java.sql.*;

public class ConfigEvents {
    
    public static void executeGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        try {
            if (Databases.getInstance().getConfigDao().queryForId(guildId) != null) return; // Guild already has a config.
            Databases.getInstance().getConfigDao().create(new ConfigEntry(guildId));
            JosetaBot.logger.info("Added new config for guild ID: " + guildId);
        } catch (SQLException e) {
            JosetaBot.logger.error("Could not create a new config for guild ID: " + guildId, e);
        } 
    }
}
