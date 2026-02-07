package dev.jojofr.joseta.commands;

import dev.jojofr.joseta.annotations.InteractionModule;
import dev.jojofr.joseta.annotations.types.ButtonInteraction;
import dev.jojofr.joseta.annotations.types.Option;
import dev.jojofr.joseta.annotations.types.SlashCommandInteraction;
import dev.jojofr.joseta.database.Database;
import dev.jojofr.joseta.database.entities.Configuration;
import dev.jojofr.joseta.database.helper.MessageDatabase;
import dev.jojofr.joseta.entities.ConfigurationMessage;
import dev.jojofr.joseta.utils.BotCache;
import dev.jojofr.joseta.utils.Log;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@InteractionModule
public class ConfigurationCommand {
    private static final Map<Long, ConfigurationMessage> configurationMessages = new HashMap<>();
    
    
    // A configuration menu using the new Components V2 system
    @SlashCommandInteraction(name = "test", description = "Configure les paramètres du bot.", permissions = Permission.MANAGE_SERVER)
    public void config(SlashCommandInteractionEvent event) {
        Container container = Container.of(
            TextDisplay.of("# Configuration"),
            TextDisplay.of("Configuration du bot - Choisissez une catégorie à configurer :"),
            
            Section.of(Button.primary("config-cat_autores", "Configurer"), TextDisplay.of("Réponse automatique")),
            Section.of(Button.primary("config-cat_counting", "Configurer"), TextDisplay.of("Comptage")),
            Section.of(Button.primary("config-cat_markov", "Configurer"), TextDisplay.of("Markov")),
            Section.of(Button.primary("config-cat_moderation", "Configurer"), TextDisplay.of("Modération")),
            Section.of(Button.primary("config-cat_welcome", "Configurer"), TextDisplay.of("Bienvenue"))
        );
        
        event.replyComponents(container).useComponentsV2().queue(
            hook -> configurationMessages.put(hook.getCallbackResponse().getMessage().getIdLong(), new ConfigurationMessage(event.getGuild().getIdLong(), Instant.now()))
        );
    }
    
    @ButtonInteraction(id = "config-cat_autores")
    public void onConfigAutoResponseButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Réponse automatique"),
            TextDisplay.of("En cours de développement."),
            ActionRow.of(Button.danger("config-back", "Retour au menu principal"))
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config-cat_counting")
    public void onConfigCountingButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Comptage"),
            TextDisplay.of("En cours de développement."),
            ActionRow.of(Button.danger("config-back", "Retour au menu principal"))
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config-cat_markov")
    public void onConfigMarkovButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Markov"),
            TextDisplay.of("En cours de développement."),
            ActionRow.of(Button.danger("config-back", "Retour au menu principal"))
        );
        
        event.editComponents(container).useComponentsV2().setComponents().queue();
    }
    
    @ButtonInteraction(id = "config-cat_moderation")
    public void onConfigModerationButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Modération"),
            TextDisplay.of("En cours de développement."),
            ActionRow.of(Button.danger("config-back", "Retour au menu principal"))
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config-cat_welcome")
    public void onConfigWelcomeButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Bienvenue"),
            TextDisplay.of("En cours de développement."),
            ActionRow.of(Button.danger("config-back", "Retour au menu principal"))
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config-back")
    public void onConfigBackButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration"),
            TextDisplay.of("Configuration du bot - Choisissez une catégorie à configurer :"),
            
            Section.of(Button.primary("config-cat_autores", "Configurer"), TextDisplay.of("Réponse automatique")),
            Section.of(Button.primary("config-cat_counting", "Configurer"), TextDisplay.of("Comptage")),
            Section.of(Button.primary("config-cat_markov", "Configurer"), TextDisplay.of("Markov")),
            Section.of(Button.primary("config-cat_moderation", "Configurer"), TextDisplay.of("Modération")),
            Section.of(Button.primary("config-cat_welcome", "Configurer"), TextDisplay.of("Bienvenue"))
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    
    private ConfigurationMessage checkConfigurationMessage(ButtonInteractionEvent event, long id) {
        ConfigurationMessage configurationMessage = configurationMessages.get(id);
        Log.info("Checking configuration message with id {}: {}", id, configurationMessage);
        if (configurationMessage == null || Instant.now().isAfter(configurationMessage.timestamp.plusSeconds(15 * 60))) {
            event.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu de configuration.").setEphemeral(true).queue();
            
            event.getMessage().editMessageComponents(TextDisplay.of("⚠️ Ce menu de configuration a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu."))
                .useComponentsV2().queue();
            configurationMessages.remove(id);
            return null;
        }
        
        return configurationMessage;
    }
    
    


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
        Configuration config = BotCache.guildConfigurations.get(event.getGuild().getIdLong());
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
        BotCache.guildConfigurations.put(event.getGuild().getIdLong(), config);
        
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
        Configuration config = BotCache.guildConfigurations.get(event.getGuild().getIdLong());
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
        BotCache.guildConfigurations.put(event.getGuild().getIdLong(), config);
        
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config moderation", description = "Configure les paramètres du bot - Catégorie: Modération.", permissions = Permission.ADMINISTRATOR)
    public void configModeration(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les fonctionnalités de modération.") Boolean enabled) {
        Configuration config = BotCache.guildConfigurations.get(event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setModerationEnabled(enabled);
        
        Database.createOrUpdate(config);
        BotCache.guildConfigurations.put(event.getGuild().getIdLong(), config);
        
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config auto_response", description = "Configure les paramètres du bot - Catégorie: Réponse automatique.", permissions = Permission.ADMINISTRATOR)
    public void configAutoResponse(SlashCommandInteractionEvent event, @Option(description = "Activer ou désactiver les réponses automatiques.") Boolean enabled) {
        Configuration config = BotCache.guildConfigurations.get(event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setAutoResponseEnabled(enabled);
        
        Database.createOrUpdate(config);
        BotCache.guildConfigurations.put(event.getGuild().getIdLong(), config);
        
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
    
    @SlashCommandInteraction(name = "config counting", description = "Configure les paramètres du bot - Catégorie: Comptage.", permissions = Permission.ADMINISTRATOR)
    public void configCounting(SlashCommandInteractionEvent event,
                               @Option(description = "Activer ou désactiver le système de comptage.") Boolean enabled,
                               @Option(description = "Activer ou désactiver les commentaires.") Boolean commentsEnabled,
                               @Option(description = "Activer ou désactiver la pénalité en cas d'erreur de comptage.") Boolean penaltyEnabled,
                               @Option(description = "Le salon de comptage.") GuildMessageChannel channel)
    {
        Configuration config = BotCache.guildConfigurations.get(event.getGuild().getIdLong());
        if (config == null) {
            config = new Configuration(event.getGuild().getIdLong());
            Log.info("Creating new configuration for guild {}", event.getGuild().getIdLong());
        }
        
        config.setCountingEnabled(enabled)
              .setCountingCommentsEnabled(commentsEnabled)
              .setCountingPenaltyEnabled(penaltyEnabled)
              .setCountingChannel(channel);
        
        Database.createOrUpdate(config);
        BotCache.guildConfigurations.put(event.getGuild().getIdLong(), config);
        
        event.reply("La configuration du serveur a été mise à jour avec succès.").setEphemeral(true).queue();
    }
}
