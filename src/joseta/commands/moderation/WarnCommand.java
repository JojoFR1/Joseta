package joseta.commands.moderation;

import joseta.commands.ModCommand;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class WarnCommand extends ModCommand {

    public WarnCommand() {
        super("warn", "Averti un membre.",
            Seq.with(
                new OptionData(OptionType.USER, "user", "Membre a mute", true),
                new OptionData(OptionType.STRING, "reason", "La raison du mute"),
                new OptionData(OptionType.STRING, "for", "La durée avant expiration du warn (s, m, h, d, w)")
            ),
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.reply("Warn- " + member + "\n" + reason + "\n" + time).queue();
        modLog.log(SanctionType.WARN, member.getIdLong(), event.getUser().getIdLong(), reason, time);
    }
}
