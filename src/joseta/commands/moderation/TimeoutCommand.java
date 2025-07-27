package joseta.commands.moderation;

import joseta.commands.*;
import joseta.database.helper.*;

import arc.util.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.time.*;
import java.util.concurrent.*;

public class TimeoutCommand extends ModCommand {

    public TimeoutCommand() {
        super("timeout", "Exclue des salons le membre.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            .addOptions(
                new OptionData(OptionType.USER, "user", "Le membre a mute.", true),
                new OptionData(OptionType.STRING, "reason", "La raison du mute."),
                new OptionData(OptionType.STRING, "time", "La durée du mute (s, m, h, d, w).")
            );
    }
    
    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        member.timeoutFor(time, TimeUnit.SECONDS).reason(reason).queue(
            success -> {
                event.reply("Le membre a bien été mute").setEphemeral(true).queue();

                member.getUser().openPrivateChannel().queue(
                        channel -> channel.sendMessage("Vous avez été exclue sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() + " pour la raison suivante : " + reason + ".\nCette sanction expirera dans: <t:" + (Instant.now().getEpochSecond() + time) + ":R>.\n\n-# ***Ceci est un message automatique. Toutes constestations doivent se faire avec le modérateur reponsable.***").queue()
                );

                SanctionDatabaseHelper.addSanction('T', member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, time);
            },
            failure -> {
                Log.err("Error while executing a command ('mute').", failure);
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
            }
        );
    }
}
