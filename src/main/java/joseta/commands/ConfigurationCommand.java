package joseta.commands;

import joseta.annotations.InteractionModule;
import joseta.annotations.types.Option;
import joseta.annotations.types.SlashCommandInteraction;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.database.helper.MessageDatabase;
import joseta.utils.Log;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@InteractionModule
public class ConfigurationCommand {
    //TODO better permission
    @SlashCommandInteraction(name = "config welcome", description = "Configure les paramètres du bot - Catégorie: Bienvenue.", permissions = Permission.ADMINISTRATOR)
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
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setWelcomeEnabled(enabled)
              .setWelcomeChannel(channel)
              .setWelcomeImageEnabled(imageEnabled)
              .setWelcomeJoinMessage(joinMessage)
              .setWelcomeLeaveMessage(leaveMessage)
              .setJoinRole(joinRole)
              .setJoinBotRole(joinBotRole)
              .setVerifiedRole(verifiedRole);
        
        Database.createOrUpdate(config);
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config markov", description = "Configure les paramètres du bot - Catégorie: Markov.", permissions = Permission.ADMINISTRATOR)
    public void configMarkov(SlashCommandInteractionEvent event,
                             @Option(description = "Activer ou désactiver la génération de messages de Markov.") Boolean enabled,
                             @Option(description = "Le membre ou rôle à ajouter à la blacklist pour la génération de messages de Markov.") IMentionable addMentionableBlacklist,
                             @Option(description = "Le membre ou rôle à retirer de la blacklist pour la génération de messages de Markov.") IMentionable removeMentionableBlacklist,
                             @Option(description = "Le salon, thread ou catégorie à ajouter à la blacklist pour la génération de messages de Markov.") GuildChannel addChannelBlacklist,
                             @Option(description = "Le salon, thread ou catégorie à retirer de la blacklist pour la génération de messages de Markov.") GuildChannel removeChannelBlacklist)
    {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setMarkovEnabled(enabled)
              .addMarkovBlacklist(addMentionableBlacklist)
              .removeMarkovBlacklist(removeMentionableBlacklist)
              .addMarkovBlacklist(addChannelBlacklist)
              .removeMarkovBlacklist(removeChannelBlacklist);
        
        MessageDatabase.updateMarkovEligibility(event.getGuild().getIdLong(), config.markovBlacklist);
        
        Database.createOrUpdate(config);
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config moderation", description = "Configure les paramètres du bot - Catégorie: Modération.", permissions = Permission.ADMINISTRATOR)
    public void configModeration(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les fonctionnalités de modération.") Boolean enabled) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setModerationEnabled(enabled);
        
        Database.createOrUpdate(config);
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config auto_response", description = "Configure les paramètres du bot - Catégorie: Réponse automatique.", permissions = Permission.ADMINISTRATOR)
    public void configAutoResponse(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les réponses automatiques.") Boolean enabled) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setAutoResponseEnabled(enabled);
        
        Database.createOrUpdate(config);
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config counting", description = "Configure les paramètres du bot - Catégorie: Comptage.", permissions = Permission.ADMINISTRATOR)
    public void configCounting(SlashCommandInteractionEvent event,
                               @Option(description = "Activer ou désactiver le système de comptage.") Boolean enabled,
                               @Option(description = "Activer ou désactiver les commentaires.") Boolean commentsEnabled,
                               @Option(description = "Activer ou désactiver la pénalité en cas d'erreur de comptage.") Boolean penaltyEnabled,
                               @Option(description = "Le salon de comptage.") GuildMessageChannel channel)
    {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setCountingEnabled(enabled)
              .setCountingCommentsEnabled(commentsEnabled)
              .setCountingPenaltyEnabled(penaltyEnabled)
              .setCountingChannel(channel);
        
        Database.createOrUpdate(config);
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
}
