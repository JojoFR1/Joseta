package joseta.commands.moderation;

import joseta.commands.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class BanCommand extends ModCommand {
    
    public BanCommand() {
        super("ban", "WIP - Ban",
            DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS),
            new OptionData(OptionType.USER, "user", "WIP", true),
            new OptionData(OptionType.STRING, "reason", "WIP"),
            new OptionData(OptionType.STRING, "time", "WIP")
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.reply("Ban- " + member + "\n" + reason + "\n" + time).queue();
        // member.ban(0, null).reason(reason).queue();

        modLog.log(SanctionType.BAN, member.getIdLong(), event.getUser().getIdLong(), reason, time);
    }
}
