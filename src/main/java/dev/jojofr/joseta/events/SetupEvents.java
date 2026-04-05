package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.*;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.database.helper.UserDatabase;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.hibernate.Session;
import org.hibernate.Transaction;

@EventModule
public class SetupEvents {
    
    @EventHandler(priority = EventHandler.EventPriority.HIGH)
    public void onGuildReady(GuildReadyEvent event) {
        Log.info("Connected to guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
        
        GuildEntity guildEntity = Database.get(GuildEntity.class, event.getGuild().getIdLong());
        if (guildEntity == null) {
            Log.info("New guild detected. Creating database entries for guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
            
            guildEntity = new GuildEntity(event.getGuild());
            Database.create(guildEntity);
            
            ConfigurationEntity config = new ConfigurationEntity(event.getGuild().getIdLong());
            Database.create(config);
            
            MessageDatabase.populateNewGuild(event.getGuild());
        }
        
        BotCache.putGuildConfiguration(event.getGuild().getIdLong(), Database.get(ConfigurationEntity.class, event.getGuild().getIdLong()));
    }
    
    @EventHandler(priority = EventHandler.EventPriority.HIGH)
    public void onGuildLeave(GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        Log.info("Left guild: {} (ID: {})", event.getGuild().getName(), guildId);
        
        GuildEntity guildEntity = Database.get(GuildEntity.class, guildId);
        if (guildEntity != null) {
            Log.info("Removing database entries for guild: {} (ID: {})", event.getGuild().getName(), guildId);
            Database.delete(guildEntity);
            
            ConfigurationEntity config = BotCache.getGuildConfiguration(guildId);
            if (config != null) {
                Database.delete(config);
                BotCache.removeGuildConfiguration(guildId);
            }
            
            MessageDatabase.deleteGuildMessages(guildId);
            UserDatabase.deleteGuildUsers(guildId);
            
            try (Session session = Database.getSession()) {
                Transaction tx = session.beginTransaction();
                Database.queryDelete(ReminderEntity.class, (cb, rt) -> cb.equal(rt.get(ReminderEntity_.guildId), guildId), session);
                Database.queryDelete(SanctionEntity.class, (cb, rt) -> cb.equal(rt.get(SanctionEntity_.id).get(SanctionEntity_.SanctionId_.guildId), guildId), session);
                tx.commit();
            }
        }
    }
    
    @EventHandler
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        MessageDatabase.deleteUserMarkovMessages(event.getGuild().getIdLong());
        
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(ReminderEntity.class, (cb, rt) -> cb.equal(rt.get(ReminderEntity_.userId), event.getUser().getIdLong()), session);
            tx.commit();
        }
    }
}
