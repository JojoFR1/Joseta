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
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.audit.TargetType;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;

@EventModule
public class SanctionEvents {

    @EventHandler(type = EventType.GUILD_AUDIT_LOG_ENTRY_CREATE, priority = EventHandler.EventPriority.DISABLED)
    public void onGuildAuditLogEntryCreate(GuildAuditLogEntryCreateEvent event) {
        AuditLogEntry entry = event.getEntry();
        Log.debug("New audit log entry in guild {} ({}): action={}, targetType={}, targetId={}, userId={}, reason={}", event.getGuild().getName(), event.getGuild().getIdLong(), entry.getType(), entry.getTargetType(), entry.getTargetIdLong(), entry.getUserIdLong(), entry.getReason());
        
        // Check for timeout start/end time
        if (entry.getType() == ActionType.MEMBER_UPDATE && entry.getTargetType() == TargetType.MEMBER) {
            // Check if the audit log entry is for a timeout start or end
            if (entry.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT) != null) {
                Log.debug("Audit log entry is for a timeout update, ignoring as it will be handled by the GuildMemberUpdateTimeOutEvent.");
                return;
            }
        }
    }
    
    @EventHandler(type = EventType.GUILD_MEMBER_UPDATE_TIME_OUT, priority = EventHandler.EventPriority.DISABLED)
    public void onGuildMemberUpdate(GuildMemberUpdateTimeOutEvent event) {
        // event.getGuild().retrieveAuditLogs().type(ActionType.).queue(
        //     logs -> {
        //         if (logs.isEmpty()) throw new RuntimeException("No audit logs found for timeout event.");
        //
        //         AuditLogEntry log = logs.getFirst();
        //         if (log.getTargetIdLong() != event.getMember().getIdLong()) throw new RuntimeException("The latest timeout audit log entry does not match the timed out user.");
        //         String reason = log.getReason() == null ? "Aucun motif fourni." : log.getReason();
        //
        //         SanctionDatabase.addSanction(SanctionEntity.SanctionType.TIMEOUT, event.getMember().getUser(), log.getUserIdLong(), event.getGuild().getIdLong(), reason, (event.getNewTimeOutEnd().toEpochMilli() - System.currentTimeMillis()) / 1000);
        //     },
        //     failure -> Log.warn("Failed to retrieve audit logs for timeout event in guild: {} ({}), for user: {} ({}).", failure, event.getGuild().getName(), event.getGuild().getIdLong(), event.getMember().getUser().getAsTag(), event.getMember().getUser().getIdLong())
        // );
    }
    
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
