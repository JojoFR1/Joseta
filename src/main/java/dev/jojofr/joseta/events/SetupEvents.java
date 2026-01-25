package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.Event;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.database.entities.Guild;
import dev.jojofr.joseta.database.entities.Reminder;
import dev.jojofr.joseta.database.entities.Sanction;
import dev.jojofr.joseta.database.entities.*;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.database.helper.UserDatabase;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
    
    @Event(type = EventType.GUILD_LEAVE)
    public void onGuildLeave(GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        Log.info("Left guild: {} (ID: {})", event.getGuild().getName(), guildId);
        
        Guild guildDatabase = Database.get(Guild.class, guildId);
        if (guildDatabase != null) {
            Log.info("Removing database entries for guild: {} (ID: {})", event.getGuild().getName(), guildId);
            Database.delete(guildDatabase);
            
            Configuration config = Database.get(Configuration.class, guildId);
            if (config != null) Database.delete(config);
            
            MessageDatabase.deleteGuildMessages(guildId);
            UserDatabase.deleteGuildUsers(guildId);
            
            try (Session session = Database.getSession()) {
                Transaction tx = session.beginTransaction();
                Database.queryDelete(Reminder.class, (cb, rt) -> cb.equal(rt.get(Reminder_.guildId), guildId), session);
                Database.queryDelete(Sanction.class, (cb, rt) -> cb.equal(rt.get(Sanction_.id).get(Sanction_.SanctionId_.guildId), guildId), session);
                tx.commit();
            }
        }
    }
    
    @Event(type = EventType.GUILD_MEMBER_REMOVE)
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        MessageDatabase.deleteUserMarkovMessages(event.getGuild().getIdLong());
        
        try (Session session = Database.getSession()) {
            Transaction tx = session.beginTransaction();
            Database.queryDelete(Reminder.class, (cb, rt) -> cb.equal(rt.get(Reminder_.userId), event.getUser().getIdLong()), session);
            tx.commit();
        }
    }
}
