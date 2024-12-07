package joseta.commands.moderation;

import joseta.commands.ModCommand;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnmuteCommand extends ModCommand {
    
    public UnmuteCommand() {
        super("unmute", "Retire le mute d'un membre",
            Seq.with(new OptionData(OptionType.USER, "user", "Membre a unmute", true)),
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
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