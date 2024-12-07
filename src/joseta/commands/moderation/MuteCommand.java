package joseta.commands.moderation;

import joseta.commands.ModCommand;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class MuteCommand extends ModCommand {

    public MuteCommand() {
        super("mute", "Mute un membre",
            Seq.with(
                new OptionData(OptionType.USER, "user", "Membre a mute", true),
                new OptionData(OptionType.STRING, "reason", "La raison du mute"),
                new OptionData(OptionType.STRING, "time", "La durée du mute (s, m, h, d, w)")
            ),
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        );
    }
    
    @Override
    public void run(SlashCommandInteractionEvent event) {
        super.run(event);

        event.reply("Mute- " + member + "\n" + reason + "\n" + time).queue();

        // member.timeoutFor(time, TimeUnit.SECONDS).reason(reason).queue();

        modLog.log(SanctionType.MUTE, member.getIdLong(), event.getUser().getIdLong(), reason, time);
    }
}
