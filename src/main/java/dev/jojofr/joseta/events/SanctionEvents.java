package dev.jojofr.joseta.events;

import dev.jojofr.joseta.annotations.EventModule;
import dev.jojofr.joseta.annotations.types.EventHandler;
import dev.jojofr.joseta.commands.ModerationCommands;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.SanctionDao;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.database.helper.SanctionDatabase;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.audit.TargetType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;

import java.time.Instant;
import java.time.OffsetDateTime;

@EventModule
public class SanctionEvents {
    
    @EventHandler
    public void onGuildAuditLogEntryCreate(GuildAuditLogEntryCreateEvent event) {
        AuditLogEntry entry = event.getEntry();
        if (entry.getType() != ActionType.MEMBER_UPDATE || entry.getTargetType() != TargetType.MEMBER || entry.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT) == null) return;
        
        String timeOutEndDate = entry.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT).getNewValue();
        if (timeOutEndDate == null) {
            SanctionEntity sanction = Database.withHandle(handle ->
                handle.attach(SanctionDao.class).getLatestByUserIdAndByType(entry.getGuild().getIdLong(), entry.getTargetIdLong(), SanctionEntity.SanctionType.TIMEOUT));
            Database.useHandle(handle -> handle.attach(SanctionDao.class).upsert(sanction.setExpired(true)));
            return;
        }
        
        User user = event.getGuild().getMemberById(entry.getTargetIdLong()).getUser();
        String reason = entry.getReason() == null ? "Aucun motif fourni." : entry.getReason();
        long moderatorId = ModerationCommands.pendingSanctions.getOrDefault(user.getIdLong() + ":timeout", entry.getUserIdLong());
        long timeOutEnd = OffsetDateTime.parse(timeOutEndDate).toInstant().getEpochSecond();
        
        user.openPrivateChannel().queue(
            channel -> channel.sendMessage("Vous avez été mis en timeout sur le serveur **`" + event.getGuild().getName() + "`** par <@" +  moderatorId +
                "> pour la raison suivante : " + reason + ".\nCette sanction expirera dans: <t:" + timeOutEnd +
                ":R>.\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
            ).queue()
        );
        
        SanctionDatabase.addSanction(SanctionEntity.SanctionType.TIMEOUT, user, moderatorId, event.getGuild().getIdLong(), reason, timeOutEnd - Instant.now().getEpochSecond());
        ModerationCommands.pendingSanctions.remove(user.getIdLong() + ":timeout");
    }
    
    // TODO handle kick
    // @EventHandler
    // public void onMemberLeave(GuildMemberRemoveEvent event) {
    //     event.getGuild().retrieveAuditLogs().type(ActionType.KICK).limit(1).queue(
    //         entries -> {
    //             if (entries.isEmpty()) return;
    //             AuditLogEntry entry = entries.getFirst();
    //
    //             if (entry.getTargetIdLong() != event.getUser().getIdLong()) return;
    //
    //             String reason = entry.getReason() == null ? "Aucun motif fourni." : entry.getReason();
    //             long moderatorId = ModerationCommands.pendingSanctions.getOrDefault(event.getUser().getIdLong() + ":kick", entry.getUserIdLong());
    //
    //             event.getUser().openPrivateChannel().queue(
    //                 channel -> channel.sendMessage(
    //                     "Vous avez été expulsé du serveur **`" + event.getGuild().getName() + "`** par <@" + moderatorId +
    //                         "> pour la raison suivante : " + reason + ".\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
    //                 ).queue()
    //             );
    //
    //             SanctionDatabase.addSanction(SanctionEntity.SanctionType.KICK, event.getUser(), moderatorId, event.getGuild().getIdLong(), reason, -1);
    //             ModerationCommands.pendingSanctions.remove(event.getUser().getIdLong() + ":kick");
    //         }
    //     );
    // }
    
    @EventHandler
    public void onGuildBan(GuildBanEvent event) {
        event.getGuild().retrieveAuditLogs().type(ActionType.BAN).limit(1).queue(
            entries -> {
                if (entries.isEmpty()) return;
                AuditLogEntry entry = entries.getFirst();
                
                if (entry.getTargetIdLong() != event.getUser().getIdLong()) return;
                
                String reason = entry.getReason() == null ? "Aucun motif fourni." : entry.getReason();
                long moderatorId = ModerationCommands.pendingSanctions.getOrDefault(event.getUser().getIdLong() + ":ban", entry.getUserIdLong());
                
                event.getUser().openPrivateChannel().queue(
                    channel -> channel.sendMessage(
                        "Vous avez été banni sur le serveur **`" + event.getGuild().getName() + "`** par <@" + moderatorId +
                            "> pour la raison suivante : " + reason + ".\nCette sanction n'expirera pas automatiquement." +
                            "\n\n-# ***Ceci est un message automatique. Toutes contestations doivent se faire avec le modérateur responsable.***"
                    ).queue()
                );
                
                SanctionDatabase.addSanction(SanctionEntity.SanctionType.BAN, event.getUser(), moderatorId, event.getGuild().getIdLong(), reason, -1);
                ModerationCommands.pendingSanctions.remove(event.getUser().getIdLong() + ":ban");
            }
        );
    }
    
    @EventHandler
    public void onGuildUnban(GuildUnbanEvent event) {
        // A user can't have 2 bans active at the same time.
        SanctionEntity sanction = Database.withHandle(handle ->
            handle.attach(SanctionDao.class).getLatestByUserIdAndByType(event.getGuild().getIdLong(), event.getUser().getIdLong(), SanctionEntity.SanctionType.BAN));
        Database.useHandle(handle -> handle.attach(SanctionDao.class).upsert(sanction.setExpired(true)));
    }
}
