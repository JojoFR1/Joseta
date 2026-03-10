package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.helper.SanctionDatabase;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;

@EventModule
public class SanctionEvents {

    @EventHandler(type = EventType.GUILD_BAN)
    public void onGuildBan(GuildBanEvent event) {
        event.getGuild().retrieveAuditLogs().type(ActionType.BAN).queue(
            logs -> {
                if (logs.isEmpty()) throw new RuntimeException("No audit logs found for ban event.");
                
                AuditLogEntry log = logs.getFirst();
                if (log.getTargetIdLong() != event.getUser().getIdLong()) throw new RuntimeException("The latest ban audit log entry does not match the banned user.");
                String reason = log.getReason() == null ? "Aucun motif fourni." : log.getReason();
                
                event.getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage(
                        "Vous avez été banni sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() +
                            " pour la raison suivante : " + reason + ".\nCette sanction n'expirera pas automatiquement." +
                            "\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
                    ).queue()
                );
                
                SanctionDatabase.addSanction(SanctionEntity.SanctionType.BAN, event.getUser(), log.getUserIdLong(), event.getGuild().getIdLong(), reason, -1);
            },
            failure -> {
                Log.warn("Failed to retrieve audit logs for ban event in guild: {} ({}), for user: {} ({}).", failure, event.getGuild().getName(), event.getGuild().getIdLong(), event.getUser().getAsTag(), event.getUser().getIdLong());
                SanctionDatabase.addSanction(SanctionEntity.SanctionType.BAN, event.getUser(), 0, event.getGuild().getIdLong(), "Aucun motif fourni.", -1);
            }
        );
    }
    
    @EventHandler(type = EventType.GUILD_UNBAN)
    public void onGuildUnban(GuildUnbanEvent event) {
        // A user can't have 2 bans active at the same time.
        SanctionEntity sanction = SanctionDatabase.getLatest(event.getUser().getIdLong(), event.getGuild().getIdLong(), SanctionEntity.SanctionType.BAN);
        Database.update(sanction.setExpired(true));
    }
}
