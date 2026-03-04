package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.helper.SanctionDatabase;
import dev.jojofr.joseta.generated.EventType;
import dev.jojofr.joseta.utils.Log;
import dev.jojofr.joseta.utils.TimeParser;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;

import java.time.Instant;

@EventModule
public class SanctionEvents {

    @EventHandler(type = EventType.GUILD_BAN)
    public void onGuildBan(GuildBanEvent event) {
        event.getGuild().retrieveAuditLogs().type(ActionType.BAN).queue(
            logs -> {
                if (logs.isEmpty()) throw new RuntimeException("No audit logs found for ban event.");
                
                AuditLogEntry log = logs.getFirst();
                if (log.getTargetIdLong() != event.getUser().getIdLong()) throw new RuntimeException("The latest ban audit log entry does not match the banned user.");
                String reason = log.getReason() == null ? "Aucune raison fournie" : log.getReason();
                
                SanctionDatabase.addSanction(SanctionEntity.SanctionType.BAN, event.getUser(), log.getUserIdLong(), event.getGuild().getIdLong(), reason, TimeParser.parse("inf"));
            },
            failure -> {
                String reason = "Aucune raison fournie";
                
                SanctionDatabase.addSanction(SanctionEntity.SanctionType.BAN, event.getUser(), 0, event.getGuild().getIdLong(), reason, TimeParser.parse("inf"));
            }
        );
        
        // event.getUser().openPrivateChannel().queue(
        //     channel -> channel.sendMessage(
        //         "Vous avez été banni sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() +
        //             " pour la raison suivante : " + reason + ".\nCette sanction expirera dans: <t:" + (Instant.now().getEpochSecond() + timeSeconds) +
        //             ":R>.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
        //     ).queue(null, null).queue());
        // );
    }
    
    @EventHandler(type = EventType.GUILD_MEMBER_UPDATE_TIME_OUT)
    public void onGuildMemberTimeout(GuildMemberUpdateTimeOutEvent event) {
        Log.info("hi");
    }
}
