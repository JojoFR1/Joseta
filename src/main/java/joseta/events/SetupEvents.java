package joseta.events;

import joseta.annotations.EventModule;
import joseta.annotations.types.Event;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.database.entities.Guild;
import joseta.database.helper.MessageDatabase;
import joseta.generated.EventType;
import joseta.utils.Log;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;

@EventModule
public class SetupEvents {
    
    @Event(type = EventType.GUILD_READY)
    public void onGuildAvailable(GuildReadyEvent event) {
        Log.info("Connected to guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
        
        Guild guildDatabase = Database.get(Guild.class, event.getGuild().getIdLong());
        if (guildDatabase == null) {
            Log.info("New guild detected. Creating database entries for guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
            
            guildDatabase = new Guild(event.getGuild());
            Database.create(guildDatabase);
            
            Configuration config = new Configuration(event.getGuild().getIdLong());
            Database.create(config);
            
            MessageDatabase.populateNewGuild(event.getGuild());
        }
    }
}
