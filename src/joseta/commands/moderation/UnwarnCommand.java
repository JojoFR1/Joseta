package joseta.commands.moderation;

import joseta.commands.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnwarnCommand extends ModCommand {
    
    public UnwarnCommand() {
        super("unwarn", "Retire l'avertissement d'un membre.",
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS),
            new OptionData(OptionType.STRING, "user", "Le membre a qui retirer l'avertissment.", true),
            new OptionData(OptionType.STRING, "id", "L'identifiant du warn.").setAutoComplete(true)
        );        
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {   
        // TODO remove sanction

        event.reply("Le membre a bien été unwarn.").setEphemeral(true).queue();
        
        // TODO need a warn ID ? or latest
    }    

}
