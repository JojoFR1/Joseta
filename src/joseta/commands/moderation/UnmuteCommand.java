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
        member.removeTimeout().queue();

        event.reply("Le membre a bien été unmute.").setEphemeral(true).queue();
    }

    @Override
    protected boolean check(SlashCommandInteractionEvent event) {
        if (!member.isTimedOut()) {
            event.reply("Ce membre n'est pas mute !").setEphemeral(true).queue();
            return false;
        }

        return super.check(event);
    }
}