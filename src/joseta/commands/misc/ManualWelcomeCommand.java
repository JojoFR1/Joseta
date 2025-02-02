package joseta.commands.misc;

import joseta.commands.Command;
import joseta.events.WelcomeMessage;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class ManualWelcomeCommand extends Command {
    protected User user;
    protected GuildChannelUnion channel;
    
    public ManualWelcomeCommand() {
        super(
            "manual-welcome",
            "Crée un message de bienvenue manuellement.",
            DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR),
            new OptionData(
                OptionType.USER,
                "user",
                "L'utilisateur à accueillir.",
                true
            ),
            new OptionData(
                OptionType.CHANNEL,
                "channel",
                "Le salon dans lequel le message est envoyé.",
                true
            )
        );
    }
    
    @Override
    protected void getArgs(SlashCommandInteractionEvent event) {
        user = event.getOption("user") != null ? event.getOption("user").getAsUser() : null;
        channel = event.getOption("channel") != null ? event.getOption("channel").getAsChannel() : null;
    }
    
    @Override
    public void runImpl(SlashCommandInteractionEvent event) {
        if (channel != null && channel.getType() == ChannelType.TEXT) {
            WelcomeMessage.sendWelcomeMessage(
                event.getGuild(),
                channel.asTextChannel(),
                user
            );
            event.reply("Succès").setEphemeral(true).queue();
        }
        event.reply("Erreur: Le salon spécifié n'est pas un salon textuel.").setEphemeral(true).queue();
    }
}
