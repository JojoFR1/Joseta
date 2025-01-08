package joseta.commands.moderation;

import joseta.commands.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnmuteCommand extends ModCommand {
    
    public UnmuteCommand() {
        super("unmute", "Unmute un membre.",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
            new OptionData(OptionType.USER, "user", "Le membre a unmute.", true)
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {        
        event.reply("Unmute- " + member).queue();
        
        member.removeTimeout().queue();
    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        if (!member.isTimedOut()) {
            event.reply("Dumdum, " + user.getName() + "is not muted.").queue();
            return false;
        }

        return super.check(event);
    }
}