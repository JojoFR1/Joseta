package joseta.commands.moderation;

import joseta.commands.*;
import joseta.database.helper.*;

import arc.util.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.time.*;

public class KickCommand extends ModCommand {
    
    public KickCommand() {
        super("kick", "Exclue du serveur le membre .");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
            .addOptions(
                new OptionData(OptionType.USER, "user", "Le membre a bannir.", true),
                new OptionData(OptionType.STRING, "reason", "La raison de l'exclusion.")
            );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        member.kick().reason(reason).queue(
            success -> {
                event.reply("Le membre a bien été expulsé.").queue();

                member.getUser().openPrivateChannel().queue(
                        channel -> channel.sendMessage("Vous avez été expulsé sur le serveur **`" + event.getGuild().getName() + "`** par " + event.getUser().getAsMention() + " pour la raison suivante : " + reason + ".\n\n-# ***Ceci est un message automatique. Toutes constestations doivent se faire avec le modérateur reponsable.***").queue()
                );

                SanctionDatabaseHelper.addSanction('K', member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, -1);        
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                Log.err("Error while executing a command ('kick').", failure);
            }
        );
    }
}
