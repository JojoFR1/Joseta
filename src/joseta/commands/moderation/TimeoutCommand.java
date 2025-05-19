package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.database.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.concurrent.*;

public class TimeoutCommand extends ModCommand {

    public TimeoutCommand() {
        super("timeout", "Exclue des salons le membre.",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
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

                ModLogDatabase.log(SanctionType.MUTE, member.getIdLong(), event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, time);
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                JosetaBot.logger.error("Error while executing a command ('mute').", failure);
            }
        );
    }
}
