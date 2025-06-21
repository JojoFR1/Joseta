package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.database.*;
import joseta.database.entry.*;
import joseta.database.helper.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.sql.*;

public class UntimeoutCommand extends ModCommand {
    
    public UntimeoutCommand() {
        super("untimeout", "Retire l'exclusion des salons du membre.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
            .addOptions(new OptionData(OptionType.USER, "user", "Le membre a unmute.", true));
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {        
        member.removeTimeout().queue(
            success -> {
                event.reply("Le membre a bien été untimeout.").setEphemeral(true).queue();

                // A member can't have 2 mute actie at the same time.
                try {
                    Databases databases = Databases.getInstance();
                    // A user can't have 2 ban active at the same time.
                    SanctionEntry entry = SanctionDatabaseHelper.getLatestSanction(user.getIdLong(), event.getGuild().getIdLong(), 'T');
                    databases.getSanctionDao().delete(entry);
                } catch (SQLException e) {
                    JosetaBot.logger.error("Erreur lors de la récupération de la configuration du serveur {} : {}", event.getGuild().getId(), e);
                    event.getChannel().sendMessage("Une erreur est survenue lors de la récupération de la configuration du serveur. Veuillez contacter un administrateur.").queue();
                    return;
                }
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                JosetaBot.logger.error("Error while executing a command ('untimeout').", failure);
            }
        );

    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        if (!member.isTimedOut()) {
            event.reply("Ce membre n'est pas mute !").setEphemeral(true).queue();
            return false;
        }

        return super.check(event);
    }
}