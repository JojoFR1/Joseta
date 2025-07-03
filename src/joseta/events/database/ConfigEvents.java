package joseta.events.database;

import joseta.database.*;
import joseta.database.entry.*;

import arc.util.*;

import net.dv8tion.jda.api.events.guild.*;

public class ConfigEvents {
    
    public static void executeGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (Databases.getInstance().get(ConfigEntry.class, guildId) != null) return; // Guild already has a config.
        
        Databases.getInstance().create(new ConfigEntry(guildId));
        Log.info("Added new config for guild @ (@)", event.getGuild().getName(), guildId);
    }
}
