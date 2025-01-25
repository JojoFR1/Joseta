package joseta.commands.moderation;

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
            new OptionData(OptionType.STRING, "clearTime", "La période des messages a supprime du membre (s, m, h, d)")
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        member.ban(clearTime, TimeUnit.SECONDS).reason(reason).queue();

        event.reply("Le membre a bien été banni.").setEphemeral(true).queue();

        modLog.log(SanctionType.BAN, member.getIdLong(), event.getUser().getIdLong(), event.getGuild().getIdLong(), reason, time);
    }

    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        super.getArgs(event);
        time = Strings.parseTime(event.getOption("time", "inf", OptionMapping::getAsString));
        clearTime = (int) Strings.parseTime(event.getOption("clearTime", "1h", OptionMapping::getAsString));
    }
}
