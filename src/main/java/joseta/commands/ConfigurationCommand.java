package joseta.commands;

import joseta.annotations.InteractionModule;
import joseta.annotations.types.Option;
import joseta.annotations.types.SlashCommandInteraction;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.utils.Log;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@InteractionModule
public class ConfigurationCommand {
    // TODO Logic implementation
    @SlashCommandInteraction(name = "config welcome", description = "Configure les paramètres du bot - Catégorie: Bienvenue.")
    public void configWelcome(SlashCommandInteractionEvent event,
                              @Option(description = "Activer ou désactiver le système de bienvenue.") Boolean enabled,
                              @Option(description = "Le salon où envoyer les messages de bienvenue.", channelTypes = ChannelType.TEXT) GuildMessageChannel channel,
                              @Option(description = "Activer ou désactiver l'envoi d'images de bienvenue.") Boolean imageEnabled,
                              @Option(description = "Le message qui sera envoyé lorsqu'un membre rejoint le serveur.") String joinMessage,
                              @Option(description = "Le message qui sera envoyé lorsqu'un membre quitte le serveur.") String leaveMessage,
                              @Option(description = "Le rôle à attribuer aux nouveaux membres.") Role joinRole,
                              @Option(description = "Le rôle à attribuer aux nouveaux bots.") Role joinBotRole,
                              @Option(description = "Le rôle à donner aux membres vérifiés.") Role verifiedRole)
    {
        
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        
        Log.info(config);
        Log.info("Enabled: " + enabled);
        Log.info("Channel: " + channel);
        Log.info("Image Enabled: " + imageEnabled);
        Log.info("Join Message: " + joinMessage);
        Log.info("Leave Message: " + leaveMessage);
        Log.info("Join Role: " + joinRole);
        Log.info("Join Bot Role: " + joinBotRole);
        Log.info("Verified Role: " + verifiedRole);
        //
        // config.setWelcomeEnabled(enabled)
        //       .setWelcomeChannelId(channel != null ? channel.getIdLong() : null)
        //       .setWelcomeImageEnabled(imageEnabled)
        //       .setWelcomeJoinMessage(joinMessage)
        //       .setWelcomeLeaveMessage(leaveMessage)
        //       .setJoinRoleId(joinRole != null ? joinRole.getIdLong() : null)
        //       .setJoinBotRoleId(joinBotRole != null ? joinBotRole.getIdLong() : null)
        //       .setVerifiedRoleId(verifiedRole != null ? verifiedRole.getIdLong() : null);
    }
    
    @SlashCommandInteraction(name = "config markov", description = "Configure les paramètres du bot - Catégorie: Markov.")
    public void configMarkov(SlashCommandInteractionEvent event,
                             @Option(description = "Activer ou désactiver la génération de messages de Markov.") boolean enabled,
                             @Option(description = "Le membre ou rôle à ajouter à la blacklist pour la génération de messages de Markov.") IMentionable addMentionableBlacklist,
                             @Option(description = "Le membre ou rôle à retirer de la blacklist pour la génération de messages de Markov.") IMentionable removeMentionableBlacklist,
                             @Option(description = "Le salon, thread ou catégorie à ajouter à la blacklist pour la génération de messages de Markov.") GuildChannel addChannelBlacklist,
                             @Option(description = "Le salon, thread ou catégorie à retirer de la blacklist pour la génération de messages de Markov.") GuildChannel removeChannelBlacklist)
    {
    
    }
    
    @SlashCommandInteraction(name = "config moderation", description = "Configure les paramètres du bot - Catégorie: Modération.")
    public void configModeration(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les fonctionnalités de modération.") Boolean enabled) {
    
    }
    
    @SlashCommandInteraction(name = "config auto_response", description = "Configure les paramètres du bot - Catégorie: Réponse automatique.")
    public void configAutoResponse(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les réponses automatiques.") Boolean enabled) {
    
    }
    
    @SlashCommandInteraction(name = "config counting", description = "Configure les paramètres du bot - Catégorie: Comptage.")
    public void configCounting(SlashCommandInteractionEvent event,
                               @Option(description = "Activer ou désactiver le système de comptage.") Boolean enabled,
                               @Option(description = "Activer ou désactiver les commentaires.") Boolean commentsEnabled,
                               @Option(description = "Activer ou désactiver la pénalité en cas d'erreur de comptage.") Boolean penaltyEnabled,
                               @Option(description = "Le salon de comptage.") GuildMessageChannel channel)
     {
    
    }
}
