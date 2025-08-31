package joseta.commands;

import joseta.annotations.*;
import joseta.annotations.modules.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.nio.channels.*;

public class AdminCommands extends CommandModule {

    public AdminCommands(SlashCommandInteractionEvent event) { super(event); }

    /* admin (SlashCommand)
     * |- rules     (SubcommandGroup)
     * |  |- send      (Subcommand)
     * |  |- update    (Subcommand)
     * |- counting  (SubcommandGroup)
     * |  |- set_number     (Subcommand)
     * |  |- reset_number   (Subcommand)
     * |  |- reset_author   (Subcommand)
     */
    @SlashCommand(name = "admiiin")
    public void admin(@Option(required = true) IMentionable target) {
        event.reply("Admin command executed. " + target).queue();

        // new SubcommandGroupData("name", "description")
        //     .addSubcommands(
        //         new SubcommandData("name", "description")
        //             .addOptions()
        //     );
        // new SubcommandData("name", "description") //Similar to SlashCommandData logic
        //     .addOptions();
    }

    @SlashCommand(name = "update")
    public void rulesUpdate() {
        event.reply("Rules updated.").queue();
    }
}
