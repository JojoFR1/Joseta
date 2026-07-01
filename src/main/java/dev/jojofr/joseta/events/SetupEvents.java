package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.annotations.types.EventPriority;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.ConfigurationDao;
import dev.jojofr.joseta.database.daos.GuildDao;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.ReminderDao;
import dev.jojofr.joseta.database.entities.ConfigurationEntity;
import dev.jojofr.joseta.database.entities.GuildEntity;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

@EventModule
public class SetupEvents {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onGuildReady(GuildReadyEvent event) {
        Log.info("Connected to guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
        
        GuildEntity guildEntity = Database.withHandle(handle -> handle.attach(GuildDao.class).getById(event.getGuild().getIdLong()));
        if (guildEntity == null) {
            Log.info("New guild detected. Creating database entries for guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
            
            Database.useHandle(handle -> handle.attach(GuildDao.class).upsert(new GuildEntity(event.getGuild())));
            
            ConfigurationEntity config = new ConfigurationEntity(event.getGuild().getIdLong());
            Database.useHandle(handle -> handle.attach(ConfigurationDao.class).upsert(config));
            
            MessageDatabase.populateNewGuild(event.getGuild()).exceptionally(throwable -> {
                Log.err("Failed to populate new guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong(), throwable);
                return null;
            });
        }
        
        BotCache.putGuildConfiguration(event.getGuild().getIdLong(), Database.withHandle(handle -> handle.attach(ConfigurationDao.class).getByGuildId(event.getGuild().getIdLong())));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onGuildLeave(GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        Log.info("Left guild: {} (ID: {})", event.getGuild().getName(), guildId);
        
        Database.useHandle(handle -> handle.attach(GuildDao.class).delete(guildId));
        BotCache.removeGuildConfiguration(guildId);
    }
    
    @EventHandler
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Database.useHandle(handle -> handle.attach(MessageDao.class).clearMarkovContent(event.getUser().getIdLong(), event.getGuild().getIdLong()));
        Database.useHandle(handle -> handle.attach(ReminderDao.class).deleteByUserId(event.getUser().getIdLong(), event.getGuild().getIdLong()));
    }
}
