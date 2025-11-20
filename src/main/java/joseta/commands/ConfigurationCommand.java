package joseta.commands;

import joseta.annotations.InteractionModule;
import joseta.annotations.types.Option;
import joseta.annotations.types.SlashCommandInteraction;
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
                              @Option(description = "Activer ou désactiver le système de bienvenue.") boolean enabled,
                              @Option(description = "Le salon où envoyer les messages de bienvenue.", channelTypes = ChannelType.TEXT) GuildMessageChannel channel,
                              @Option(description = "Activer ou désactiver l'envoi d'images de bienvenue.") boolean imageEnabled,
                              @Option(description = "Le message qui sera envoyé lorsqu'un membre rejoint le serveur.") String joinMessage,
                              @Option(description = "Le message qui sera envoyé lorsqu'un membre quitte le serveur.") String leaveMessage,
                              @Option(description = "Le rôle à attribuer aux nouveaux membres.") Role joinRole,
                              @Option(description = "Le rôle à attribuer aux nouveaux bots.") Role joinBotRole,
                              @Option(description = "Le rôle à donner aux membres vérifiés.") Role verifiedRole)
    {
    
    }
    
    @SlashCommandInteraction(name = "config markov", description = "Configure les paramètres du bot - Catégorie: Markov.")
    public void configMarkov(SlashCommandInteractionEvent event,
                             @Option(description = "Activer ou désactiver la génération de messages de Markov.") boolean enabled,
                             @Option(description = "Le membre ou le rôle à ajouter à la blacklist pour la génération de messages de Markov.") IMentionable addMentionableBlacklist,
                             @Option(description = "Le membre ou le rôle à retirer de la blacklist pour la génération de messages de Markov.") IMentionable removeMentionableBlacklist,
                             @Option(description = "Le salon, le thread ou la catégorie à ajouter à la blacklist pour la génération de messages de Markov.") GuildChannel addChannelBlacklist,
                             @Option(description = "Le salon, le thread ou la catégorie à retirer de la blacklist pour la génération de messages de Markov.") GuildChannel removeChannelBlacklist)
    {
    
    }
    
    @SlashCommandInteraction(name = "config moderation", description = "Configure les paramètres du bot - Catégorie: Modération.")
    public void configModeration(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les fonctionnalités de modération.") boolean enabled) {
    
    }
    
    @SlashCommandInteraction(name = "config auto_response", description = "Configure les paramètres du bot - Catégorie: Réponse automatique.")
    public void configAutoResponse(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les réponses automatiques.") boolean enabled) {
    
    }
    
    @SlashCommandInteraction(name = "config counting", description = "Configure les paramètres du bot - Catégorie: Comptage.")
    public void configCounting(SlashCommandInteractionEvent event,
                               @Option(description = "Activer ou désactiver le système de comptage.") boolean enabled,
                               @Option(description = "Activer ou désactiver les commentaires.") boolean commentsEnabled,
                               @Option(description = "Activer ou désactiver la pénalité en cas d'erreur de comptage.") boolean penaltyEnabled,
                               @Option(description = "Le salon de comptage.") GuildMessageChannel channel)
     {
    
    }
}
