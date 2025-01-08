package joseta.commands.moderation;

import joseta.commands.*;
import joseta.utils.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class BanCommand extends ModCommand {
    
    public BanCommand() {
        super("ban", "Bannir un membre.",
            DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS),
            new OptionData(OptionType.USER, "user", "Le membre a bannir.", true),
            new OptionData(OptionType.STRING, "reason", "La raison du bannisement."),
            new OptionData(OptionType.STRING, "time", "La durée du bannisement (s, m, h, d, w)")
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        member.ban(0, null).reason(reason).queue();

        event.reply("Le membre a bien été banni." ).setEphemeral(true).queue();

        modLog.log(SanctionType.BAN, member.getIdLong(), event.getUser().getIdLong(), reason, time);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);
        time = Strings.parseTime(event.getOption("time") != null ? event.getOption("time").getAsString() : "inf");
    }
}
