package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.database.helper.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

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

                SanctionDatabaseHelper.addSanction('K', member, event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, -1);        
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                JosetaBot.logger.error("Error while executing a command ('kick').", failure);
            }
        );
    }
}
