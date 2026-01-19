package joseta.events;

import joseta.annotations.EventModule;
import joseta.annotations.types.Event;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.database.entities.Guild;
import joseta.database.entities.Reminder;
import joseta.database.entities.Reminder_;
import joseta.database.helper.MessageDatabase;
import joseta.database.helper.UserDatabase;
import joseta.generated.EventType;
import joseta.utils.Log;
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
        Log.info("Left guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
        
        Guild guildDatabase = Database.get(Guild.class, event.getGuild().getIdLong());
        if (guildDatabase != null) {
            Log.info("Removing database entries for guild: {} (ID: {})", event.getGuild().getName(), event.getGuild().getIdLong());
            Database.delete(guildDatabase);
            
            Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
            if (config != null) Database.delete(config);
            
            MessageDatabase.deleteGuildMessages(event.getGuild().getIdLong());
            UserDatabase.deleteGuildUsers(event.getGuild().getIdLong());
            
            try (Session session = Database.getSession()) {
                Transaction tx = session.beginTransaction();
                Database.queryDelete(Reminder.class, (cb, rt) -> cb.equal(rt.get(Reminder_.guildId), event.getGuild().getIdLong()), session);
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
