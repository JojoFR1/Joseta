package joseta.commands.moderation;

import joseta.commands.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class WarnCommand extends ModCommand {

    public WarnCommand() {
        super("warn", "Averti un membre.",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
            new OptionData(OptionType.USER, "user", "Membre a mute", true),
            new OptionData(OptionType.STRING, "reason", "La raison du mute"),
            new OptionData(OptionType.STRING, "time", "La durée avant expiration du warn (s, m, h, d, w)")
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.reply("Warn- " + member + "\n" + reason + "\n" + time).queue();
        modLog.log(SanctionType.WARN, member.getIdLong(), event.getUser().getIdLong(), reason, time);
    }
}
