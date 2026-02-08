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
import dev.jojofr.joseta.utils.function.Function;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@InteractionModule
public class ConfigurationCommand {
    private static final Map<Long, ConfigurationMessage> configurationMessages = new HashMap<>();
    private static final Function<ActionRow, ConfigurationMessage> bottomRow = configurationMessage ->
        ActionRow.of(
            Button.primary("config:menu_back", "Retour au menu principal").withDisabled(configurationMessage == null || configurationMessage.isMainMenu),
            Button.success("config:save", "Enregistrer les modifications").withDisabled(configurationMessage == null || !configurationMessage.hasChanged)
        );
    
    private static final Function<Container, ConfigurationMessage> mainMenuContainer = configurationMessage ->
        Container.of(
            TextDisplay.of("# Configuration"),
            TextDisplay.of("Configuration du bot - Choisissez une catégorie à configurer :"),
            
            Section.of(Button.primary("config-cat_autores", "Configurer"), TextDisplay.of("### Réponse automatique")),
            Section.of(Button.primary("config-cat_counting", "Configurer"), TextDisplay.of("### Comptage")),
            Section.of(Button.primary("config-cat_markov", "Configurer"), TextDisplay.of("### Markov")),
            Section.of(Button.primary("config-cat_moderation", "Configurer"), TextDisplay.of("### Modération")),
            Section.of(Button.primary("config-cat_welcome", "Configurer"), TextDisplay.of("### Bienvenue")),
            
            bottomRow.get(configurationMessage)
        );
    
    // A configuration menu using the new Components V2 system
    @SlashCommandInteraction(name = "test", description = "Configure les paramètres du bot.", permissions = Permission.MANAGE_SERVER)
    public void config(SlashCommandInteractionEvent event) {
        event.replyComponents(mainMenuContainer.get(null)).useComponentsV2().queue(
            hook -> configurationMessages.put(hook.getCallbackResponse().getMessage().getIdLong(), new ConfigurationMessage(event.getGuild().getIdLong(), Instant.now()))
        );
    }
    
    // @Option(description = "Activer ou désactiver les réponses automatiques.") Boolean enabled
    @ButtonInteraction(id = "config-cat_autores")
    public void onConfigAutoResponseButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Réponse automatique"),
            
            Section.of(
                Button.of(
                    configurationMessage.configuration.autoResponseEnabled ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                    "config-toggle_welcome",
                    configurationMessage.configuration.autoResponseEnabled ? "Activer" : "Désactiver",
                    configurationMessage.configuration.autoResponseEnabled ? Emoji.fromUnicode("✅") : Emoji.fromUnicode("❌")
                ),
                TextDisplay.of("### Actif"),
                TextDisplay.of("-# Permet d'activer ou de désactiver les réponses automatiques. Lorsque les réponses automatiques sont désactivées, les autres paramètres de réponse automatique seront ignorés.")
            ),
            
            
            bottomRow.get(configurationMessage)
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    // @Option(description = "Activer ou désactiver le système de comptage.") Boolean enabled,
    // @Option(description = "Activer ou désactiver les commentaires.") Boolean commentsEnabled,
    // @Option(description = "Activer ou désactiver la pénalité en cas d'erreur de comptage.") Boolean penaltyEnabled,
    // @Option(description = "Le salon de comptage.") GuildMessageChannel channel
    @ButtonInteraction(id = "config-cat_counting")
    public void onConfigCountingButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Comptage"),
            TextDisplay.of("En cours de développement."),
            bottomRow.get(configurationMessage)
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    // @Option(description = "Activer ou désactiver la génération de messages de Markov.") Boolean enabled,
    // @Option(description = "Le membre ou rôle à ajouter à la blacklist pour la génération de messages de Markov.") IMentionable addMentionableBlacklist,
    // @Option(description = "Le membre ou rôle à retirer de la blacklist pour la génération de messages de Markov.") IMentionable removeMentionableBlacklist,
    // @Option(description = "Le salon, thread ou catégorie à ajouter à la blacklist pour la génération de messages de Markov.") GuildChannel addChannelBlacklist,
    // @Option(description = "Le salon, thread ou catégorie à retirer de la blacklist pour la génération de messages de Markov.") GuildChannel removeChannelBlacklist)
    @ButtonInteraction(id = "config-cat_markov")
    public void onConfigMarkovButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Markov"),
            TextDisplay.of("En cours de développement."),
            bottomRow.get(configurationMessage)
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    // @Option(description = "Activer ou désactiver les fonctionnalités de modération.") Boolean enabled
    @ButtonInteraction(id = "config-cat_moderation")
    public void onConfigModerationButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        Container container = Container.of(
            TextDisplay.of("# Configuration - Modération"),
            Section.of(
                Button.of(
                    configurationMessage.configuration.moderationEnabled ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                    "config-toggle_welcome",
                    configurationMessage.configuration.moderationEnabled ? "Activer" : "Désactiver",
                    configurationMessage.configuration.moderationEnabled ? Emoji.fromUnicode("✅") : Emoji.fromUnicode("❌")
                ),
                TextDisplay.of("### Actif"),
                TextDisplay.of("-# Permet d'activer ou de désactiver les fonctionnalités de modération. Lorsque les fonctionnalités de modération sont désactivées, les autres paramètres de modération seront ignorés.")
            ),
            
            bottomRow.get(configurationMessage)
        );
        
        event.editComponents(container).useComponentsV2().queue();
    }
    
    // String joinMessage,
    // @Option(description = "Le message qui sera envoyé lorsqu'un membre quitte le serveur.") String leaveMessage,
    public final Function<Container, ConfigurationMessage> welcomeContainer = configurationMessage -> {
        EntitySelectMenu channelSelectMenu = EntitySelectMenu.create("config-welcome_channel_select", EntitySelectMenu.SelectTarget.CHANNEL)
            .setPlaceholder("Sélectionnez un salon de bienvenue")
            .setChannelTypes(ChannelType.TEXT)
            .setDefaultValues(EntitySelectMenu.DefaultValue.channel(configurationMessage.configuration.welcomeChannelId))
            .build();
        
        EntitySelectMenu joinRoleSelectMenu = EntitySelectMenu.create("config-welcome_join_role_select", EntitySelectMenu.SelectTarget.ROLE)
            .setPlaceholder("Sélectionnez un rôle à attribuer aux nouveaux membres")
            .setDefaultValues(EntitySelectMenu.DefaultValue.role(configurationMessage.configuration.joinRoleId))
            .build();
        
        EntitySelectMenu joinBotRoleSelectMenu = EntitySelectMenu.create("config-welcome_join_bot_role_select", EntitySelectMenu.SelectTarget.ROLE)
            .setPlaceholder("Sélectionnez un rôle à attribuer aux nouveaux bots")
            .setDefaultValues(EntitySelectMenu.DefaultValue.role(configurationMessage.configuration.joinBotRoleId))
            .build();
        
        EntitySelectMenu verifiedRoleSelectMenu = EntitySelectMenu.create("config-welcome_verified_role_select", EntitySelectMenu.SelectTarget.ROLE)
            .setPlaceholder("Sélectionnez un rôle à attribuer aux membres vérifiés")
            .setDefaultValues(EntitySelectMenu.DefaultValue.role(configurationMessage.configuration.verifiedRoleId))
            .build();
        
        return Container.of(
            TextDisplay.of("# Configuration - Bienvenue"),
            
            Section.of(
                Button.of(
                    configurationMessage.configuration.welcomeEnabled ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                    "config-toggle_welcome",
                    configurationMessage.configuration.welcomeEnabled ? "Activer" : "Désactiver",
                    configurationMessage.configuration.welcomeEnabled ? BotCache.CHECK_EMOJI : BotCache.CROSS_EMOJI
                ),
                TextDisplay.of("### Actif"),
                TextDisplay.of("-# Permet d'activer ou de désactiver le système de bienvenue. Lorsque le système de bienvenue est désactivé, les autres paramètres de bienvenue seront ignorés.")
            ),
            
            TextDisplay.of("### Salon de bienvenue"),
            TextDisplay.of("-# Permet de choisir le salon où les messages de bienvenue seront envoyés."),
            ActionRow.of(channelSelectMenu),
            
            Section.of(
                Button.of(
                    configurationMessage.configuration.welcomeImageEnabled ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                    "config-toggle_welcome_image",
                    configurationMessage.configuration.welcomeImageEnabled ? "Activer" : "Désactiver"
                ),
                TextDisplay.of("### Image de bienvenue"),
                TextDisplay.of("-# Permet d'activer ou de désactiver l'envoi d'images de bienvenue. Lorsque cette option est activée, le bot enverra une image de bienvenue personnalisée avec le message de bienvenue.")
            ),
            
            TextDisplay.of("### Message de bienvenue"),
            TextDisplay.of("-# A FAIRE, non implémenté pour le moment."),
            
            TextDisplay.of("### Rôles de bienvenue"),
            TextDisplay.of("-# Permet de choisir les rôles à attribuer aux nouveaux membres, aux nouveaux bots et aux membres vérifiés. Lorsque ces rôles sont sélectionnés, le bot les attribuera automatiquement aux membres concernés lorsqu'ils rejoindront le serveur ou seront vérifiés."),
            TextDisplay.of("- Rôle pour les nouveaux membres : attribué à tous les membres qui rejoignent le serveur, sauf s'ils sont des bots."),
            ActionRow.of(joinRoleSelectMenu),
            TextDisplay.of("- Rôle pour les nouveaux bots : attribué à tous les membres qui rejoignent le serveur et qui sont des bots."),
            ActionRow.of(joinBotRoleSelectMenu),
            TextDisplay.of("- Rôle pour les membres vérifiés : attribué à tous les membres qui sont vérifiés."),
            ActionRow.of(verifiedRoleSelectMenu),
            
            bottomRow.get(configurationMessage)
        );
    };
    
    @ButtonInteraction(id = "config-cat_welcome")
    public void onConfigWelcomeButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        event.editComponents(welcomeContainer.get(configurationMessage)).useComponentsV2().queue();
    }
    
    @ButtonInteraction(id = "config-toggle_welcome")
    public void onConfigToggleWelcomeButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        configurationMessage.configuration.setWelcomeEnabled(!configurationMessage.configuration.welcomeEnabled);
        
        event.editComponents(welcomeContainer.get(configurationMessage)).useComponentsV2().queue();
        
    }
    
    @ButtonInteraction(id = "config:menu_back")
    public void onConfigBackButton(ButtonInteractionEvent event) {
        ConfigurationMessage configurationMessage = checkConfigurationMessage(event, event.getMessageIdLong());
        if (configurationMessage == null) return;
        
        configurationMessage.isMainMenu = true;
        event.editComponents(mainMenuContainer.get(configurationMessage)).useComponentsV2().queue();
    }
    
    
    private ConfigurationMessage checkConfigurationMessage(ButtonInteractionEvent event, long id) {
        ConfigurationMessage configurationMessage = configurationMessages.get(id);
        if (configurationMessage == null || Instant.now().isAfter(configurationMessage.timestamp.plusSeconds(15 * 60))) {
            event.reply("Cette interaction a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu de configuration.").setEphemeral(true).queue();
            
            event.getMessage().editMessageComponents(TextDisplay.of("⚠️ Ce menu de configuration a expiré. Veuillez réutiliser la commande pour obtenir un nouveau menu."))
                .useComponentsV2().queue();
            configurationMessages.remove(id);
            return null;
        }
        
        configurationMessage.isMainMenu = false;
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
