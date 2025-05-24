package joseta.events;

import joseta.*;
import joseta.database.*;
import joseta.database.MessagesDatabase.*;

import arc.func.*;
import arc.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.entities.sticker.*;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.channel.*;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.emoji.*;
import net.dv8tion.jda.api.events.emoji.update.*;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.invite.*;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.*;
import net.dv8tion.jda.api.events.guild.override.*;
import net.dv8tion.jda.api.events.guild.scheduledevent.*;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.http.*;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.events.message.poll.*;
import net.dv8tion.jda.api.events.message.react.*;
import net.dv8tion.jda.api.events.role.*;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.session.*;
import net.dv8tion.jda.api.events.sticker.*;
import net.dv8tion.jda.api.events.sticker.update.*;
import net.dv8tion.jda.api.hooks.*;

import java.awt.*;
import java.util.List;

public class LogSystem extends ListenerAdapter {
    @SuppressWarnings("unchecked")
    private Seq<Class<? extends GenericEvent>> ignoredEvents = Seq.with(// Bot related events
                                                                        StatusChangeEvent.class,
                                                                        HttpRequestEvent.class,
                                                                        GatewayPingEvent.class,
                                                                        GuildReadyEvent.class,
                                                                        ReadyEvent.class,
                                                                        ShutdownEvent.class,
                                                                        SessionDisconnectEvent.class,
                                                                        SessionResumeEvent.class,
                                                                        // Other unused events - generally basic user events or not useful for logging
                                                                        MessageReceivedEvent.class,
                                                                        MessageReactionAddEvent.class,
                                                                        MessageReactionRemoveEvent.class,
                                                                        MessagePollVoteAddEvent.class,
                                                                        MessagePollVoteRemoveEvent.class,
                                                                        GuildAuditLogEntryCreateEvent.class,
                                                                        SlashCommandInteractionEvent.class,
                                                                        ButtonInteractionEvent.class);

    @Override
    public void onGenericEvent(GenericEvent event) {
        if (ignoredEvents.contains(event.getClass())) return;

        EventType eventType = EventType.getFromEvent(event);
        if (eventType == null) {
            JosetaBot.logger.warn("Unknown event type: " + event);
            return;
        }

        MessageEmbed embed = eventType.getEmbed(event);
        if (embed == null) return;

        Vars.testChannel.sendMessageEmbeds(embed).queue();
        // Vars.logChannel.sendMessageEmbeds(embed).queue();
    }

    public enum EventType {
        //#region Channel Events TODO
        // TODO
        CHANNEL_CREATE(ChannelCreateEvent.class,
                       event -> Vars.getDefaultEmbed(Color.decode("#417505"), event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                    .setTitle("Nouveau salon - " + getChannelTypeString(event.getChannelType()))
                                    .setDescription("Salon créé: " + event.getChannel().getAsMention())
                                    .build()
        ),
        // TODO
        CHANNEL_DELETE(ChannelDeleteEvent.class,
                       event -> Vars.getDefaultEmbed(Color.decode("#D0021B"), event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_DELETE).getUser())
                                    .setTitle("Salon supprimé - " + getChannelTypeString(event.getChannelType()))
                                    .setDescription("Salon supprimé: `" + event.getChannel().getName()  + "`")
                                    .build()
        ),
        // TODO
        CHANNEL_UPDATE(GenericChannelUpdateEvent.class,
                      event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_UPDATE).getUser())
                               .setTitle("Salon mis a jour - " + getChannelTypeString(event.getChannelType()))
                               .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_UPDATE).getUser().getAsMention())
                               .build()
        ),
        // TODO
        CHANNEL_UPDATE_TOPIC(ChannelUpdateTopicEvent.class,
                             event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                          .setTitle("Salon mis a jour (Topic)")
                                          .setDescription(event.getOldValue() + " en " + event.getNewValue())
                                          .build()
        ),
        // TODO
        CHANNEL_UPDATE_SLOWMODE(ChannelUpdateSlowmodeEvent.class,
                                event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                             .setTitle("Salon mis a jour (Slowmode)")
                                             .setDescription("" +event.getOldValue() + " en " + event.getNewValue())
                                             .build()
        ),
        // TODO
        CHANNEL_UPDATE_NAME(ChannelUpdateNameEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Salon mis à jour")
                                         .setDescription("Ancien nom: `" + event.getOldValue() + "`\nNouveau nom: `" + event.getNewValue() + "`")
                                         .build()
        ),
        //TODO
        CHANNEL_UPDATE_NSFW(ChannelUpdateNSFWEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Salon mis a jour (NSFW)")
                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_UPDATE).getUser().getAsMention())
                                         .build()
        ),
        //#endregion
        //#region Emoji Events TODO
        //TODO
        EMOJI_ADDED(EmojiAddedEvent.class,
                    event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                 .setTitle("Emoji ajouté")
                                 .setDescription(event.getEmoji().getAsMention() + " by " + retrieveAuditLog(event.getGuild(), ActionType.EMOJI_CREATE).getUser().getAsMention())
                                 .build()
        ),
        //TODO
        EMOJI_REMOVED(EmojiRemovedEvent.class,
                      event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                   .setTitle("Emoji retiré")
                                   .setDescription(event.getEmoji().getAsMention() + " by " + retrieveAuditLog(event.getGuild(), ActionType.EMOJI_CREATE).getUser().getAsMention())
                                   .build()
        ),
        //TODO
        EMOJI_UPDATE_NAME(EmojiUpdateNameEvent.class,
                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                       .setTitle("Emoji mis a jour")
                                       .setDescription(event.getEmoji().getAsMention() + " " + event.getOldName() + " en " + event.getNewName() + " by " + retrieveAuditLog(event.getGuild(), ActionType.EMOJI_UPDATE).getUser().getAsMention())
                                       .build()
        ),
        //TODO
        EMOJI_UPDATE_ROLES(EmojiUpdateRolesEvent.class,
                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                        .setTitle("Emoji mis a jour (Rôles)")
                                        .setDescription(event.getEmoji().getAsMention() + " " + event.getOldRoles() + " en " + event.getNewRoles() + " by " + retrieveAuditLog(event.getGuild(), ActionType.EMOJI_UPDATE).getUser().getAsMention())
                                        .build()
        ),
        //#endregion
        //#region Guild Events TODO
        //TODO
        GUILD_BAN(GuildBanEvent.class,
                  event -> Vars.getDefaultEmbed(Color.RED, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                               .setTitle("Membre banni")
                               .setDescription(event.getUser().getAsMention() + " by " + retrieveAuditLog(event.getGuild(), ActionType.BAN).getUser().getAsMention())
                               .build()
        ),
        //TODO
        GUILD_UNBAN(GuildUnbanEvent.class,
                    event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                 .setTitle("Membre débanni")
                                 .setDescription(event.getUser().getAsMention() + " by " + retrieveAuditLog(event.getGuild(), ActionType.UNBAN).getUser().getAsMention())
                                 .build()
        ),
        //TODO
        GUILD_INVITE_CREATE(GuildInviteCreateEvent.class,
                            event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Invitation créée")
                                         .setDescription(event.getCode() + " by " + retrieveAuditLog(event.getGuild(), ActionType.INVITE_CREATE).getUser().getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_INVITE_DELETE(GuildInviteDeleteEvent.class,
                            event -> Vars.getDefaultEmbed(Color.RED, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Invitation supprimée")
                                         .setDescription(event.getCode() + " by " + retrieveAuditLog(event.getGuild(), ActionType.INVITE_DELETE).getUser().getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_UPDATE_AFK_CHANNEL(GuildUpdateAfkChannelEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                              .setTitle("Salon AFK mis a jour")
                                              .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                              .build()
        ),
        //TODO
        GUILD_UPDATE_AFK_TIMEOUT(GuildUpdateAfkTimeoutEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                              .setTitle("Timeout AFK mis a jour")
                                              .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                              .build()
        ),
        //TODO
        GUILD_UPDATE_BANNER(GuildUpdateBannerEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Bannière mise a jour")
                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_UPDATE_COMMUNITY_UPDATES_CHANNEL(GuildUpdateCommunityUpdatesChannelEvent.class,
                                               event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                            .setTitle("Salon de mise a jour de la communauté mis a jour")
                                                            .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                            .build()
        ),
        //TODO
        GUILD_UPDATE_DESCRIPTION(GuildUpdateDescriptionEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                              .setTitle("Description mise a jour")
                                              .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                              .build()
        ),
        //TODO
        GUILD_UPDATE_EXPLICIT_CONTENT_LEVEL(GuildUpdateExplicitContentLevelEvent.class,
                                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                         .setTitle("Niveau de contenu explicite mis a jour")
                                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                         .build()
        ),
        //TODO
        GUILD_UPDATE_FEATURES(GuildUpdateFeaturesEvent.class,
                              event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                           .setTitle("Fonctionnalités mises a jour")
                                           .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                           .build()
        ),
        //TODO
        GUILD_UPDATE_ICON(GuildUpdateIconEvent.class,
                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                       .setTitle("Icône mise a jour")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_UPDATE_LOCALE(GuildUpdateLocaleEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Langue mise a jour")
                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_UPDATE_MFA_LEVEL(GuildUpdateMFALevelEvent.class,
                               event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                            .setTitle("Niveau MFA mis a jour")
                                            .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                            .build()
        ),
        //TODO
        GUILD_UPDATE_NSFW_LEVEL(GuildUpdateNSFWLevelEvent.class,
                                event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                             .setTitle("Niveau NSFW mis a jour")
                                             .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                             .build()
        ),
        //TODO
        GUILD_UPDATE_NAME(GuildUpdateNameEvent.class,
                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                       .setTitle("Nom de la guilde mis a jour")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_UPDATE_NOTIFICATION_LEVEL(GuildUpdateNotificationLevelEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                     .setTitle("Niveau de notification mis a jour")
                                                     .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                     .build()
        ),
        //TODO
        GUILD_UPDATE_OWNER(GuildUpdateOwnerEvent.class,
                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                        .setTitle("Propriétaire de la guilde mis a jour")
                                        .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                        .build()
        ),
        //TODO
        GUILD_UPDATE_RULES_CHANNEL(GuildUpdateRulesChannelEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                .setTitle("Salon de règles mis a jour")
                                                .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                .build()
        ),
        //TODO
        GUILD_UPDATE_SAFETY_ALERTS_CHANNEL(GuildUpdateSafetyAlertsChannelEvent.class,
                                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                        .setTitle("Salon d'alerte de sécurité mis a jour")
                                                        .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                        .build()
        ),
        //TODO
        GUILD_UPDATE_SPLASH(GuildUpdateSplashEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Splash mis a jour")
                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_UPDATE_SYSTEM_CHANNEL(GuildUpdateSystemChannelEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                 .setTitle("Salon système mis a jour")
                                                 .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                 .build()
        ),
        //TODO
        GUILD_UPDATE_VANITY_CODE(GuildUpdateVanityCodeEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                              .setTitle("Code de vanité mis a jour")
                                              .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                              .build()
        ),
        //TODO
        GUILD_UPDATE_VERIFICATION_LEVEL(GuildUpdateVerificationLevelEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                     .setTitle("Niveau de vérification mis a jour")
                                                     .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.GUILD_UPDATE).getUser().getAsMention())
                                                     .build()
        ),
        //#endregion
        //#region Member Events TODO
        //TODO
        GUILD_MEMBER_JOIN(GuildMemberJoinEvent.class,
                          event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                       .setTitle("Membre rejoint")
                                       .setDescription(event.getUser().getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_MEMBER_REMOVE(GuildMemberRemoveEvent.class,
                            event -> Vars.getDefaultEmbed(Color.RED, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Membre quitté")
                                         .setDescription(event.getUser().getAsMention() + " est parti.")
                                         .build()
        ),
        // TODO
        GUILD_MEMBER_UPDATE(GenericGuildMemberUpdateEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                         .setTitle("Membre mis a jour")
                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_UPDATE).getUser().getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_MEMBER_ROLE_ADDED(GuildMemberRoleAddEvent.class,
                                event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                             .setTitle("Rôle ajouté")
                                             .setDescription(event.getUser().getAsMention() + " " + event.getRoles() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_ROLE_UPDATE).getUser().getAsMention())
                                             .build()
        ),
        //TODO
        GUILD_MEMBER_ROLE_REMOVED(GuildMemberRoleRemoveEvent.class,
                                  event -> Vars.getDefaultEmbed(Color.RED, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                               .setTitle("Rôle retiré")
                                               .setDescription(event.getUser().getAsMention() + " " + event.getRoles() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_ROLE_UPDATE).getUser().getAsMention())
                                               .build()
        ),
        //TODO
        GUILD_MEMBER_UPDATE_NICKNAME(GuildMemberUpdateNicknameEvent.class,
                                     event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                  .setTitle("Surnom mis a jour")
                                                  .setDescription(event.getUser().getAsMention() + " " + event.getOldNickname() + " en " + event.getNewNickname() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_UPDATE).getUser().getAsMention())
                                                  .build()
        ),
        //TODO
        GUILD_VOICE_GUILD_DEAFEN(GuildVoiceGuildDeafenEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                              .setTitle("Déaf mis a jour")
                                              .setDescription(event.getMember().getAsMention() + " " + event.getVoiceState() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_UPDATE).getUser().getAsMention())
                                              .build()
        ),
        //TODO
        GUILD_VOICE_GUILD_MUTE(GuildVoiceGuildMuteEvent.class,
                               event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                            .setTitle("Mute mis a jour")
                                            .setDescription(event.getMember().getAsMention() + " " + event.getVoiceState() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_UPDATE).getUser().getAsMention())
                                            .build()
        ),
        //TODO
        GUILD_VOICE_UPDATE(GuildVoiceUpdateEvent.class,
                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                        .setTitle("Salon vocal mis a jour")
                                        .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.MEMBER_UPDATE).getUser().getAsMention())
                                        .build()
        ),
        //#endregion
        //#region Permission Events TODO
        //TODO
        PERMISSION_OVERRIDE_CREATE(PermissionOverrideCreateEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                .setTitle("Permission ajoutée")
                                                .setDescription(event.getPermissionOverride() + " by " + retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_OVERRIDE_CREATE).getUser().getAsMention())
                                                .build()
        ),
        //TODO
        PERMISSION_OVERRIDE_DELETE(PermissionOverrideDeleteEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.RED, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                .setTitle("Permission supprimée")
                                                .setDescription(event.getPermissionOverride() + " by " + retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_OVERRIDE_DELETE).getUser().getAsMention())
                                                .build()
        ),
        //TODO
        PERMISSION_OVERRIDE_UPDATE(PermissionOverrideUpdateEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                .setTitle("Permission mise a jour")
                                                .setDescription(event.getPermissionOverride() + " by " + retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_OVERRIDE_UPDATE).getUser().getAsMention())
                                                .build()
        ),
        //#endregion
        //#region Schedule Event Events TODO
        //TODO
        SCHEDULED_EVENT_CREATE(ScheduledEventCreateEvent.class,
                               event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                            .setTitle("Événement créé")
                                            .setDescription(event.getScheduledEvent().getName() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_CREATE).getUser().getAsMention())
                                            .build()
        ),
        //TODO
        SCHEDULED_EVENT_DELETE(ScheduledEventDeleteEvent.class,
                               event -> Vars.getDefaultEmbed(Color.RED, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                            .setTitle("Événement supprimé")
                                            .setDescription(event.getScheduledEvent().getName() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_DELETE).getUser().getAsMention())
                                            .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_DESCRIPTION(ScheduledEventUpdateDescriptionEvent.class,
                                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                        .setTitle("Événement mis a jour (Description)")
                                                        .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                        .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_END_TIME(ScheduledEventUpdateEndTimeEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                     .setTitle("Événement mis a jour (Heure de fin)")
                                                     .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                     .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_IMAGE(ScheduledEventUpdateImageEvent.class,
                                     event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                  .setTitle("Événement mis a jour (Image)")
                                                  .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                  .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_LOCATION(ScheduledEventUpdateLocationEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                     .setTitle("Événement mis a jour (Lieu)")
                                                     .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                     .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_NAME(ScheduledEventUpdateNameEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                 .setTitle("Événement mis a jour (Nom)")
                                                 .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                 .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_START_TIME(ScheduledEventUpdateStartTimeEvent.class,
                                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                       .setTitle("Événement mis a jour (Heure de début)")
                                                       .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                       .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_STATUS(ScheduledEventUpdateStatusEvent.class,
                                      event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild(), retrieveAuditLog(event.getGuild(), ActionType.CHANNEL_CREATE).getUser())
                                                   .setTitle("Événement mis a jour (Statut)")
                                                   .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveAuditLog(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getUser().getAsMention())
                                                   .build()
        ),
        //#endregion
        //#region Message Events
        MESSAGE_DELETE(MessageDeleteEvent.class,
            event -> {
                long messageId = event.getMessageIdLong();
                Guild guild = event.getGuild();
                Channel channel = event.getChannel();

                MessageEntry messageEntry = MessagesDatabase.getMessageEntry(messageId, guild.getIdLong(), channel.getIdLong());

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.MESSAGE_UPDATE);
                User user = auditLogEntry.getUser();
                if (user == null) user = guild.retrieveMemberById(messageEntry.authorId).complete().getUser();
                if (user.isBot() || user.isSystem()) return null;

                String description = "**Message envoyé par " + user.getAsMention() + " supprimé dans " + channel.getAsMention() + "**\n\n```" + messageEntry.content + "```";
                MessagesDatabase.deleteMessage(messageId, guild.getIdLong(), channel.getIdLong());
                
                return createEmbed(Color.RED, guild, user, description);
            }
        ),
        MESSAGE_UPDATE(MessageUpdateEvent.class,
            event -> {
                long messageId = event.getMessageIdLong();
                Guild guild = event.getGuild();
                Channel channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.MESSAGE_UPDATE);
                User user = auditLogEntry.getUser();
                if (user == null) user = event.getAuthor();
                if (user.isBot() || user.isSystem()) return null;

                String description = "**Message envoyé par " + user.getAsMention() + " modifié: (" + event.getMessage().getJumpUrl() + ")**\n**Ancien**\n```" + MessagesDatabase.getMessageEntry(messageId, guild.getIdLong(), channel.getIdLong()).content.replace("`", "\\`") + "```\n**Nouveau**\n```" + event.getMessage().getContentRaw().replace("`", "\\`") + "```";
                MessagesDatabase.updateMessage(messageId, guild.getIdLong(), channel.getIdLong(), event.getMessage().getContentRaw());
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }
        ),
        MESSAGE_REACTION_REMOVE_EMOJI(MessageReactionRemoveEmojiEvent.class,
            event -> {
                Guild guild = event.getGuild();

                String description = "**La réaction "+ event.getEmoji().getFormatted() +" de "+ event.getJumpUrl() +" a été retirer.**";
                
                return createEmbed(Color.YELLOW, guild, null, description);
            }
        ),
        MESSAGE_REACTION_REMOVE_ALL(MessageReactionRemoveAllEvent.class,
            event -> {
                Guild guild = event.getGuild();

                String description = "**Les réactions de "+ event.getJumpUrl() +" ont toutes été retirer.**";
                
                return createEmbed(Color.RED, guild, null, description);
            }
        ),
        //#endregion
        //#region Role Events TODO
        ROLE_CREATE(RoleCreateEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau rôle par " + user.getAsMention() + "**\n\n+ `"+ role.getName() +"`";
                
                return createEmbed(Color.GREEN, guild, user, description);
            }
        ),
        ROLE_DELETE(RoleDeleteEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**Rôle supprimé par " + user.getAsMention() + "**\n\n\\- `"+ role.getName() +"`";
                
                return createEmbed(Color.RED, guild, user, description);
            }
        ),
        ROLE_UPDATE_COLOR(RoleUpdateColorEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouvelle couleur pour le rôle `" + role.getName() +"` par " + user.getAsMention() + "**\n\n "+ event.getOldColor() +" --> "+ event.getNewColor();
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }

        ),
        //TODO
        ROLE_UPDATE_HOIST(RoleUpdateHoistedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau s'affiche séparemtn... avec updatepermaussi? par " + user.getAsMention() + "\n\n"+ role.getAsMention();
                
                return createEmbed(Color.GREEN, guild, user, description);
            }

        ),
        //TODO
        ROLE_UPDATE_ICON(RoleUpdateIconEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouvelle icône par " + user.getAsMention() + "\n\n "+ event.getOldIcon();
                
                return createEmbed(Color.GREEN, guild, user, description);
            }

        ),
        //TODO
        ROLE_UPDATE_MENTIONABLE(RoleUpdateMentionableEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau mentionnable... p'tet fusionner avec update perm? par " + user.getAsMention() + "\n\n"+ role.getAsMention();
                
                return createEmbed(Color.GREEN, guild, user, description);
            }

        ),
        ROLE_UPDATE_NAME(RoleUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau nom pour le rôle `" + role.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldName() +"` --> `"+ event.getNewName() +"`";
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }
        ),
        //TODO
        ROLE_UPDATE_PERMISSIONS(RoleUpdatePermissionsEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouvelle.. permission....; ouais non pas aujourd'hui trop de boulot cette merde - par " + user.getAsMention() + "\n\n"+ role.getAsMention();
                
                return createEmbed(Color.GREEN, guild, user, description);
            }

        ),
        //TODO
        ROLE_UPDATE_POSITION(RoleUpdatePositionEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouvelle.. position....; ouais non pas aujourd'hui trop de boulot cette merde - par " + user.getAsMention() + "\n\n"+ role.getAsMention();
                
                return createEmbed(Color.GREEN, guild, user, description);
            }

        ),
        //#endregion
        //#region Sticker Events
        GUILD_STICKER_ADDED(GuildStickerAddedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau sticker (`"+ sticker.getName() +"`) par " + user.getAsMention() + ".**";
                
                return createEmbed(Color.GREEN, guild, user, description);
            }
        ),
        GUILD_STICKER_REMOVED(GuildStickerRemovedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**Le sticker (`"+ sticker.getName() +"`) a été supprimé par " + user.getAsMention() + ".**";
                
                return createEmbed(Color.RED, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_AVAILABLE(GuildStickerUpdateAvailableEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau \"availablility\"? pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_DESCRIPTION(GuildStickerUpdateDescriptionEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouelle description pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_NAME(GuildStickerUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau nom pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_TAGS(GuildStickerUpdateTagsEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String oldTag = event.getOldValue().iterator().next();
                String newTag = event.getNewValue().iterator().next();
                // Check if they're a custom emoji or default emoji
                String oldEmoji = !oldTag.matches("[0-9]+") ? ":"+oldTag+":" : guild.retrieveEmojiById(oldTag).complete().getFormatted();
                String newEmoji = !newTag.matches("[0-9]+") ? ":"+newTag+":" : guild.retrieveEmojiById(newTag).complete().getFormatted();

                String description = "**Nouveau tags pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n "+ oldEmoji +" --> "+ newEmoji;
                
                return createEmbed(Color.YELLOW, guild, user, description);
            }
        );
        //#endregion

        private final Class<? extends GenericEvent> eventClass;
        private final Func<? extends GenericEvent, MessageEmbed> embed;
        
        private <T extends GenericEvent> EventType(Class<T> eventClass, Func<T, MessageEmbed> embed) {
            this.eventClass = eventClass;
            this.embed = embed;
        }
        
        @SuppressWarnings("unchecked")
        public <T extends GenericEvent> MessageEmbed getEmbed(T event) {
            if (eventClass.isInstance(event)) return ((Func<T, MessageEmbed>) embed).get(event);
            return new EmbedBuilder().setDescription("Error: Event type mismatch").build();
        }

        public static EventType getFromEvent(GenericEvent event) {
            for (EventType eventType : values())
                if (event.getClass() == eventType.eventClass) return eventType;
            return null;
        }
    }

    private static AuditLogEntry retrieveAuditLog(Guild guild, ActionType actionType) {
        List<AuditLogEntry> auditLog = guild.retrieveAuditLogs()
                                        .type(actionType)
                                        .limit(1)
                                        .complete();
        
        return !auditLog.isEmpty() ? auditLog.get(0) : new AuditLogEntry(ActionType.UNKNOWN, 0, 0, 0, 0, null, null, null, null, null, null);
    }

    private static MessageEmbed createEmbed(Color color, Guild guild, User author, String description) {
        return Vars.getDefaultEmbed(color, guild, author)
                .setDescription(description)
                .build();

    }

    private static String getChannelTypeString(ChannelType channelType) {
        if (channelType.equals(ChannelType.FORUM)) return "Forum";
        if (channelType.equals(ChannelType.MEDIA)) return "Média";
        if (channelType.equals(ChannelType.NEWS)) return "Annonce";
        if (channelType.equals(ChannelType.STAGE)) return "Conférence";
        if (channelType.equals(ChannelType.TEXT)) return "Textuel";
        if (channelType.equals(ChannelType.VOICE)) return "Vocal";

        return "Inconnu";
    }
}