package joseta.commands.admin;

import joseta.commands.Command;
import joseta.database.*;
import joseta.database.ConfigDatabase.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.awt.*;
import java.time.*;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config","Configurez les paramètres du bot pour ce serveur.",
            DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER),
            new SubcommandData("welcome", "Welcome")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the welcome join and leave.")
                .addOption(OptionType.CHANNEL, "channel", "The channel where the welcome message will be sent.")
                .addOption(OptionType.BOOLEAN, "image_enabled", "Enable or disable the welcome image.")
                .addOption(OptionType.STRING, "join_message", "The message sent when a user joins the server.")
                .addOption(OptionType.STRING, "leave_message", "The message sent when a user leaves the server.")
                .addOption(OptionType.ROLE, "new_member_role", "The role given to new members.")
                .addOption(OptionType.ROLE, "new_bot_role", "The role given to new bots."),
            new SubcommandData("markov", "Welcome")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the welcome join and leave.")
                .addOption(OptionType.CHANNEL, "add_to_channel_blacklist", "The channel to add to the Markov chain blacklist.")
                .addOption(OptionType.CHANNEL, "add_to_category_blacklist", "The category to add to the Markov chain blacklist.")
                .addOption(OptionType.CHANNEL, "remove_from_channel_blacklist", "The channel to remove from the Markov chain blacklist.")
                .addOption(OptionType.CHANNEL, "remove_from_category_blacklist", "The category to remove from the Markov chain blacklist."),
            new SubcommandData("moderation", "Welcome")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the welcome join and leave."),
            new SubcommandData("auto_response", "Welcome")
                .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the welcome join and leave.")
        );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());
        // If SOMEHOW the config is null, create a new one.
        if (config == null) ConfigDatabase.addNewConfig(event.getGuild().getIdLong());

        if (!ConfigDatabase.updateConfig(config)) {
            event.reply("An error occurred while updating the configuration.").setEphemeral(true).queue();
            return;
        }

        event.reply("This command is not implemented yet.").setEphemeral(true).queue();
    }
    
    private MessageEmbed getEmbed(Color color, Guild guild) {
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(color)
            .setFooter(guild.getName(), guild.getIconUrl())
            .setTimestamp(Instant.now());

        return embed.build();
    }
}
