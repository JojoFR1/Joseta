package joseta.events.database;

import joseta.database.*;
import joseta.database.entry.*;

import arc.util.*;

import net.dv8tion.jda.api.events.guild.*;

import java.sql.*;

public class ConfigEvents {
    
    public static void executeGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        try {
            if (Databases.getInstance().getConfigDao().queryForId(guildId) != null) return; // Guild already has a config.
            Databases.getInstance().getConfigDao().create(new ConfigEntry(guildId));
            Log.info("Added new config for guild @ (@)", event.getGuild().getName(), guildId);
        } catch (SQLException e) {
            Log.err("Could not create a new config for guild @ (@): @", event.getGuild().getName(), guildId, e.getMessage());
        } 
    }
}
