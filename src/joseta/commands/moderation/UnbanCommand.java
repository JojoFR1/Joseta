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
            new OptionData(OptionType.STRING, "user", "L'utilisateur a débanir.", true).setAutoComplete(true)
        );
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        event.getGuild().unban(member).queue();
        
        event.reply("Le membre a bien été débani.").setEphemeral(true).queue();

        //TODO remove sanction
    }
}
