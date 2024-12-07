package joseta.commands.moderation;

import joseta.commands.ModCommand;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnbanCommand extends ModCommand {
    
    public UnbanCommand() {
        super("unban", "WIP - Unban",
            Seq.with(new OptionData(OptionType.USER, "user", "WIP", true)),
            DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)
        );

    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.reply("Unban- " + member).queue();

        // event.getGuild().unban(member).queue();
    }
}
