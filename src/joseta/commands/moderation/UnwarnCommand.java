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
            new OptionData(OptionType.USER, "user", "Membre a mute", true),
            new OptionData(OptionType.STRING, "id", "L'identifiant du warn")
        );        
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {   
        // TODO need a warn ID ? or latest
        
        event.reply("Unwarn- " + member).queue();
    }    

}
