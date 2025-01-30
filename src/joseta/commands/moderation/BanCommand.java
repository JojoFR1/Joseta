package joseta.commands.moderation;

import joseta.*;
import joseta.commands.*;
import joseta.utils.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.concurrent.*;

public class BanCommand extends ModCommand {
    private int clearTime;
    
    public BanCommand() {
        super("ban", "Bannir un membre.",
            DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS),
            new OptionData(OptionType.USER, "user", "Le membre a bannir.", true),
            new OptionData(OptionType.STRING, "reason", "La raison du bannisement."),
            new OptionData(OptionType.STRING, "time", "La durée du bannisement (s, m, h, d, w)"),
            new OptionData(OptionType.STRING, "clear_time", "La période des messages a supprime du membre (s, m, h, d)")
        );
        defaultTime = "inf";
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        member.ban(clearTime, TimeUnit.SECONDS).reason(reason).queue(
            success -> {
                event.reply("Le membre a bien été banni.").setEphemeral(true).queue();

                ModLog.log(SanctionType.BAN, member.getIdLong(), event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, time);        
            },
            failure -> {
                event.reply("Une erreur est survenue lors de l'éxecution de la commande. Veuillez contacter un administrateur.").setEphemeral(true).queue();
                JosetaBot.logger.error("Error while executing a command ('ban').", failure);
            }
        );
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);
        clearTime = (int) Strings.parseTime(event.getOption("clear_time", "1h", OptionMapping::getAsString));
    }
}
