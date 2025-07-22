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
                    .addOption(OptionType.MENTIONABLE, "add_to_blacklist", "The channel, category, user or role to add to the Markov chain blacklist.")
                    .addOption(OptionType.MENTIONABLE, "remove_from_blacklist", "The channel, category, user or role to remove from the Markov chain blacklist."),
                new SubcommandData("moderation", "Moderation")
                    .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the moderation features."),
                new SubcommandData("auto_response", "Auto Response")
                    .addOption(OptionType.BOOLEAN, "enabled", "Enable or disable the auto response features.")
            );
    }

    @Override
    protected void runImpl(SlashCommandInteractionEvent event) {
        ConfigEntry config = Database.get(ConfigEntry.class, event.getGuild().getIdLong());
        
        switch (event.getSubcommandName()) {
            case "welcome":
                config.setWelcomeEnabled(event.getOption("enabled", config.isWelcomeEnabled(), OptionMapping::getAsBoolean))
                    .setWelcomeChannelId(event.getOption("channel", config.getWelcomeChannelId(), OptionMapping::getAsLong))
                    .setWelcomeImageEnabled(event.getOption("image_enabled", config.isWelcomeEnabled(), OptionMapping::getAsBoolean))
                    .setWelcomeJoinMessage(event.getOption("join_message", config.getWelcomeJoinMessage(), OptionMapping::getAsString))
                    .setWelcomeLeaveMessage(event.getOption("leave_message", config.getWelcomeLeaveMessage(), OptionMapping::getAsString))
                    .setJoinRoleId(event.getOption("join_role", config.getJoinRoleId(), OptionMapping::getAsLong))
                    .setJoinBotRoleId(event.getOption("join_bot_role", config.getJoinBotRoleId(), OptionMapping::getAsLong))
                    .setVerifiedRoleId(event.getOption("verified_role", config.getVerifiedRoleId(), OptionMapping::getAsLong));
                break;
            case "markov":
                config.setMarkovEnabled(event.getOption("enabled", config.isMarkovEnabled(), OptionMapping::getAsBoolean))
                    .addMarkovBlackList(event.getOption("add_to_blacklist", 0L, OptionMapping::getAsLong))
                    .removeMarkovBlackList(event.getOption("remove_from_blacklist", 0L, OptionMapping::getAsLong));
                break;
            case "moderation":
                config.setModerationEnabled(event.getOption("enabled", config.isModerationEnabled(), OptionMapping::getAsBoolean));
                break;
            case "auto_response":
                config.setAutoResponseEnabled(event.getOption("enabled", config.isAutoResponseEnabled(), OptionMapping::getAsBoolean));
                break;
        
            default: break;
        }

        Database.createOrUpdate(config);

        event.reply("Configuration of the server updated succesfully.").setEphemeral(true).queue();
    }
}
