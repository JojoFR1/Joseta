package joseta.commands.moderation;

import joseta.commands.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnbanCommand extends ModCommand {
    
    public UnbanCommand() {
        super("unban", "Débanir un membre.",
            DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS),
            new OptionData(OptionType.USER, "user", "L'utilisateur a débanir.", true)
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.reply("Unban- " + member).queue();

        // event.getGuild().unban(member).queue();
    }
}
