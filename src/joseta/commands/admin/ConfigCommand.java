package joseta.commands.admin;

import joseta.commands.Command;
import joseta.database.*;
import joseta.database.entry.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config","Configurez les paramètres du bot pour ce serveur.");
        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
            .addSubcommands(
                new SubcommandData("welcome", "Welcome")
                    .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the welcome join and leave.")
                    .addOption(OptionType.CHANNEL, "channel", "The channel where the welcome message will be sent.")
                    .addOption(OptionType.BOOLEAN, "image_enabled", "Enable or disable the welcome image.")
                    .addOption(OptionType.STRING, "join_message", "The message sent when a user joins the server.")
                    .addOption(OptionType.STRING, "leave_message", "The message sent when a user leaves the server.")
                    .addOption(OptionType.ROLE, "join_role", "The role given to new members.")
                    .addOption(OptionType.ROLE, "join_bot_role", "The role given to new bots.")
                    .addOption(OptionType.ROLE, "verified_role", "The role given to verified member."),
                new SubcommandData("markov", "Markov")
                    .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the Markov chain.")
                    .addOption(OptionType.CHANNEL, "add_to_channel_blacklist", "The channel to add to the Markov chain blacklist.")
                    .addOption(OptionType.CHANNEL, "add_to_category_blacklist", "The category to add to the Markov chain blacklist.")
                    .addOption(OptionType.CHANNEL, "remove_from_channel_blacklist", "The channel to remove from the Markov chain blacklist.")
                    .addOption(OptionType.CHANNEL, "remove_from_category_blacklist", "The category to remove from the Markov chain blacklist."),
                new SubcommandData("moderation", "Moderation")
                    .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the moderation features."),
                new SubcommandData("auto_response", "Auto Response")
                    .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the auto response features.")
            );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        ConfigEntry config = ConfigDatabase.getConfig(event.getGuild().getIdLong());
        // If SOMEHOW the config is null, create a new one.
        if (config == null) ConfigDatabase.addNewConfig(event.getGuild().getIdLong());

        switch (event.getSubcommandName()) {
            case "welcome":
                config.setWelcomeEnabled(event.getOption("enabled", config.welcomeEnabled, OptionMapping::getAsBoolean))
                    .setWelcomeChannelId(event.getOption("channel", config.welcomeChannelId, OptionMapping::getAsLong))
                    .setWelcomeImageEnabled(event.getOption("image_enabled", config.welcomeImageEnabled, OptionMapping::getAsBoolean))
                    .setWelcomeJoinMessage(event.getOption("join_message", config.welcomeJoinMessage, OptionMapping::getAsString))
                    .setWelcomeLeaveMessage(event.getOption("leave_message", config.welcomeLeaveMessage, OptionMapping::getAsString))
                    .setJoinRoleId(event.getOption("join_role", config.joinRoleId, OptionMapping::getAsLong))
                    .setJoinBotRoleId(event.getOption("join_bot_role", config.joinBotRoleId, OptionMapping::getAsLong))
                    .setVerifiedRoleId(event.getOption("verified_role", config.joinBotRoleId, OptionMapping::getAsLong));
                break;
            case "markov":
                config.setMarkovEnabled(event.getOption("enabled", config.markovEnabled, OptionMapping::getAsBoolean))
                    .addMarkovChannelBlackList(event.getOption("add_to_channel_blacklist", 0L, OptionMapping::getAsLong))
                    .removeMarkovCategoryBlackList(event.getOption("remove_from_category_blacklist", 0L, OptionMapping::getAsLong))
                    .addMarkovCategoryBlackList(event.getOption("add_to_category_blacklist", 0L, OptionMapping::getAsLong))
                    .removeMarkovChannelBlackList(event.getOption("remove_from_channel_blacklist", 0L, OptionMapping::getAsLong));
                break;
            case "moderation":
                config.setModerationEnabled(event.getOption("enabled", config.moderationEnabled, OptionMapping::getAsBoolean));
                break;
            case "auto_response":
                config.setAutoResponseEnabled(event.getOption("enabled", config.autoResponseEnabled, OptionMapping::getAsBoolean));
                break;
        
            default: break;
        }


        if (!ConfigDatabase.updateConfig(config)) {
            event.reply("An error occurred while updating the configuration.").setEphemeral(true).queue();
            return;
        }

        event.reply("Configuration of the server updated succesfully.").setEphemeral(true).queue();
    }
    
    // private MessageEmbed getEmbed(Color color, Guild guild) {
    //     EmbedBuilder embed = new EmbedBuilder()
    //         .setColor(color)
    //         .setFooter(guild.getName(), guild.getIconUrl())
    //         .setTimestamp(Instant.now());

    //     return embed.build();
    // }
}
