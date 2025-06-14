package joseta.events.database;

import joseta.*;
import joseta.database.*;

import net.dv8tion.jda.api.events.guild.*;

public class ConfigEvents {
    
    public static void executeGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (ConfigDatabase.getConfig(guildId) != null) return; // Guild already has a config.
     
        ConfigDatabase.addNewConfig(guildId);
        JosetaBot.logger.info("Added new config for guild ID: " + guildId);
    }

}
