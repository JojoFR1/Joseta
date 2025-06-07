package joseta.commands.admin;

import joseta.commands.Command;
import joseta.database.*;
import joseta.database.ConfigDatabase.*;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config","Configure the bot settings.",
            DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)
        );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        ConfigEntry config = ConfigDatabase.getConfigEntry(event.getGuild().getIdLong());
        

        event.reply("This command is not implemented yet.").setEphemeral(true).queue();
    }
    
}
