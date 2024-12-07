package joseta.commands.moderation;

import joseta.commands.ModCommand;
import joseta.utils.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class UnwarnCommand extends ModCommand {
    
    public UnwarnCommand() {
        super("unwarn", "Retire l'avertissement d'un membre.",
            Seq.with(
                new OptionData(OptionType.USER, "user", "Membre a mute", true),
                new OptionData(OptionType.STRING, "id", "L'identifiant du warn")
            ),
            DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        );        
    }

    @Override
    public void runImpl(SlashCommandInteractionEvent event) {   
        // TODO need a warn ID ? or latest
        
        event.reply("Unwarn- " + member).queue();
    }    

}
