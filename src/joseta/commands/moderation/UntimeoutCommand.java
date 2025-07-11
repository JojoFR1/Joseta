package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.database.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

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
                event.reply("Le membre a bien été unmute.").setEphemeral(true).queue();

                // A member can't have 2 mute actie at the same time.
                ModLogDatabase.removeSanction(ModLogDatabase.getLatestSanction(user.getIdLong(), event.getGuild().getIdLong(), SanctionType.MUTE));
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                JosetaBot.logger.error("Error while executing a command ('unmute').", failure);
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