package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.interaction.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.daos.UserDao;
import dev.jojofr.joseta.database.entities.UserEntity;
import dev.jojofr.joseta.utils.Parser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@InteractionModule
public class StatsCommand {
    
    @SlashCommandInteraction(name = "stats", description = "Affiche les statistiques de l'utilisateur.")
    public void stats(SlashCommandInteractionEvent event) {
        Database.useHandle(handle -> {
            UserEntity dbUser = handle.attach(UserDao.class).getById(event.getUser().getIdLong(), event.getGuild().getIdLong());
            int messageCount = handle.attach(MessageDao.class).getMemberMessageCount(event.getUser().getIdLong(), event.getGuild().getIdLong());
            
            Member member = event.getMember();
            
            event.reply("Nombre de messages envoyés : " + Parser.formatNumber(messageCount)
                + "\nTemps passé en vocal : " + (dbUser == null ? "0s" : Parser.formatTime(dbUser.timeVoice / 1000))
                + "\nNombre de sanctions : " + (dbUser == null ? 0 : dbUser.sanctionCount)
                + "\nA rejoint le serveur le : <t:" + (member == null ? 0 : member.getTimeJoined().toEpochSecond()) + ":F> (<t:" + (member == null ? 0 : member.getTimeJoined().toEpochSecond()) + ":R>)"
                + "\nA créé son compte Discord le : <t:" + (member == null ? 0 : member.getTimeCreated().toEpochSecond()) + ":F> (<t:" + (member == null ? 0 : member.getTimeCreated().toEpochSecond()) + ":R>)"
            ).setEphemeral(true).queue();
        });
    }
}
