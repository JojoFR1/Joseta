package joseta.events;

import joseta.*;
import joseta.utils.func.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
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
import net.dv8tion.jda.api.hooks.*;

import java.awt.*;

public class LogSystem extends ListenerAdapter {

    @Override
    public void onGenericEvent(GenericEvent event) {
        EventType eventType = EventType.getFromEvent(event);
        if (eventType == null) {
            JosetaBot.logger.warn("Unknown event type: " + event);
            return;
        }

        Vars.testChannel.sendMessageEmbeds(eventType.getEmbed(event)).queue();
    }

    public enum EventType {
        //#region Channel Events
        //TODO
        CHANNEL_CREATE(ChannelCreateEvent.class,
                       event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                  .setTitle("Salon créé")
                                  .setDescription("Salon créé: " + event.getChannel().getAsMention() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_CREATE).getAsMention())
                                  .build()
        ),
        //TODO
        CHANNEL_DELETE(ChannelDeleteEvent.class,
                       event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                  .setTitle("Salon supprimé")
                                  .setDescription("desc" + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_DELETE).getAsMention())
                                  .build()
        ),
        //TODO
        CHANNEL_UPDATE_TOPIC(ChannelUpdateTopicEvent.class,
                             event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                        .setTitle("Salon mis a jour (Topic)")
                                        .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                        .build()
        ),
        //TODO
        CHANNEL_UPDATE_SLOWMODE(ChannelUpdateSlowmodeEvent.class,
                                event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                           .setTitle("Salon mis a jour (Slowmode)")
                                           .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                           .build()
        ),
        //TODO
        CHANNEL_UPDATE_NAME(ChannelUpdateNameEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Salon renommé")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                       .build()
        ),
        //TODO
        CHANNEL_UPDATE_NSFW(ChannelUpdateNSFWEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Salon mis a jour (NSFW)")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_UPDATE).getAsMention())
                                       .build()
        ),
        //#endregion
        //#region Emoji Events
        //TODO
        EMOJI_ADDED(EmojiAddedEvent.class,
                    event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                               .setTitle("Emoji ajouté")
                               .setDescription(event.getEmoji().getAsMention() + " by " + retrieveModerator(event.getGuild(), ActionType.EMOJI_CREATE).getAsMention())
                               .build()
        ),
        //TODO
        EMOJI_REMOVED(EmojiRemovedEvent.class,
                      event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                 .setTitle("Emoji retiré")
                                 .setDescription(event.getEmoji().getAsMention() + " by " + retrieveModerator(event.getGuild(), ActionType.EMOJI_CREATE).getAsMention())
                                 .build()
        ),
        //TODO
        EMOJI_UPDATE_NAME(EmojiUpdateNameEvent.class,
                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                     .setTitle("Emoji mis a jour")
                                     .setDescription(event.getEmoji().getAsMention() + " " + event.getOldName() + " en " + event.getNewName() + " by " + retrieveModerator(event.getGuild(), ActionType.EMOJI_UPDATE).getAsMention())
                                     .build()
        ),
        //TODO
        EMOJI_UPDATE_ROLES(EmojiUpdateRolesEvent.class,
                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                      .setTitle("Emoji mis a jour (Rôles)")
                                      .setDescription(event.getEmoji().getAsMention() + " " + event.getOldRoles() + " en " + event.getNewRoles() + " by " + retrieveModerator(event.getGuild(), ActionType.EMOJI_UPDATE).getAsMention())
                                      .build()
        ),
        //#endregion
        //#region Guild Events
        //TODO
        GUILD_BAN(GuildBanEvent.class,
                  event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                              .setTitle("Membre banni")
                              .setDescription(event.getUser().getAsMention() + " by " + retrieveModerator(event.getGuild(), ActionType.BAN).getAsMention())
                              .build()
        ),
        //TODO
        GUILD_UNBAN(GuildUnbanEvent.class,
                    event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                               .setTitle("Membre débanni")
                               .setDescription(event.getUser().getAsMention() + " by " + retrieveModerator(event.getGuild(), ActionType.UNBAN).getAsMention())
                               .build()
        ),
        //TODO
        GUILD_INVITE_CREATE(GuildInviteCreateEvent.class,
                            event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                       .setTitle("Invitation créée")
                                       .setDescription(event.getCode() + " by " + retrieveModerator(event.getGuild(), ActionType.INVITE_CREATE).getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_INVITE_DELETE(GuildInviteDeleteEvent.class,
                            event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                       .setTitle("Invitation supprimée")
                                       .setDescription(event.getCode() + " by " + retrieveModerator(event.getGuild(), ActionType.INVITE_DELETE).getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_UPDATE_AFK_CHANNEL(GuildUpdateAfkChannelEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                               .setTitle("Salon AFK mis a jour")
                                               .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                               .build()
        ),
        //TODO
        GUILD_UPDATE_AFK_TIMEOUT(GuildUpdateAfkTimeoutEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                               .setTitle("Timeout AFK mis a jour")
                                               .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                               .build()
        ),
        //TODO
        GUILD_UPDATE_BANNER(GuildUpdateBannerEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Bannière mise a jour")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_UPDATE_COMMUNITY_UPDATES_CHANNEL(GuildUpdateCommunityUpdatesChannelEvent.class,
                                               event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                          .setTitle("Salon de mise a jour de la communauté mis a jour")
                                                          .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                                          .build()
        ),
        //TODO
        GUILD_UPDATE_DESCRIPTION(GuildUpdateDescriptionEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                            .setTitle("Description mise a jour")
                                            .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                            .build()
        ),
        //TODO
        GUILD_UPDATE_EXPLICIT_CONTENT_LEVEL(GuildUpdateExplicitContentLevelEvent.class,
                                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                       .setTitle("Niveau de contenu explicite mis a jour")
                                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                                       .build()
        ),
        //TODO
        GUILD_UPDATE_FEATURES(GuildUpdateFeaturesEvent.class,
                              event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                         .setTitle("Fonctionnalités mises a jour")
                                         .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                         .build()
        ),
        //TODO
        GUILD_UPDATE_ICON(GuildUpdateIconEvent.class,
                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                     .setTitle("Icône mise a jour")
                                     .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                     .build()
        ),
        //TODO
        GUILD_UPDATE_LOCALE(GuildUpdateLocaleEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Langue mise a jour")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_UPDATE_MFA_LEVEL(GuildUpdateMFALevelEvent.class,
                               event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                          .setTitle("Niveau MFA mis a jour")
                                          .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                          .build()
        ),
        //TODO
        GUILD_UPDATE_NSFW_LEVEL(GuildUpdateNSFWLevelEvent.class,
                                event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                           .setTitle("Niveau NSFW mis a jour")
                                           .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                           .build()
        ),
        //TODO
        GUILD_UPDATE_NAME(GuildUpdateNameEvent.class,
                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                     .setTitle("Nom de la guilde mis a jour")
                                     .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                     .build()
        ),
        //TODO
        GUILD_UPDATE_NOTIFICATION_LEVEL(GuildUpdateNotificationLevelEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                   .setTitle("Niveau de notification mis a jour")
                                                   .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                                   .build()
        ),
        //TODO
        GUILD_UPDATE_OWNER(GuildUpdateOwnerEvent.class,
                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                      .setTitle("Propriétaire de la guilde mis a jour")
                                      .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                      .build()
        ),
        //TODO
        GUILD_UPDATE_RULES_CHANNEL(GuildUpdateRulesChannelEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                              .setTitle("Salon de règles mis a jour")
                                              .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                              .build()
        ),
        //TODO
        GUILD_UPDATE_SAFETY_ALERTS_CHANNEL(GuildUpdateSafetyAlertsChannelEvent.class,
                                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                      .setTitle("Salon d'alerte de sécurité mis a jour")
                                                      .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                                      .build()
        ),
        //TODO
        GUILD_UPDATE_SPLASH(GuildUpdateSplashEvent.class,
                            event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                       .setTitle("Splash mis a jour")
                                       .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_UPDATE_SYSTEM_CHANNEL(GuildUpdateSystemChannelEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                               .setTitle("Salon système mis a jour")
                                               .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                               .build()
        ),
        //TODO
        GUILD_UPDATE_VANITY_CODE(GuildUpdateVanityCodeEvent.class,
                                 event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                            .setTitle("Code de vanité mis a jour")
                                            .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                            .build()
        ),
        //TODO
        GUILD_UPDATE_VERIFICATION_LEVEL(GuildUpdateVerificationLevelEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                   .setTitle("Niveau de vérification mis a jour")
                                                   .setDescription(event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.GUILD_UPDATE).getAsMention())
                                                   .build()
        ),
        //#endregion
        //#region Member Events
        //TODO
        GUILD_MEMBER_JOIN(GuildMemberJoinEvent.class,
                          event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                     .setTitle("Membre rejoint")
                                     .setDescription(event.getUser().getAsMention())
                                     .build()
        ),
        //TODO
        GUILD_MEMBER_REMOVE(GuildMemberRemoveEvent.class,
                            event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                       .setTitle("Membre quitté")
                                       .setDescription(event.getUser().getAsMention())
                                       .build()
        ),
        //TODO
        GUILD_MEMBER_ROLE_ADDED(GuildMemberRoleAddEvent.class,
                                event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                           .setTitle("Rôle ajouté")
                                           .setDescription(event.getUser().getAsMention() + " " + event.getRoles() + " by " + retrieveModerator(event.getGuild(), ActionType.MEMBER_ROLE_UPDATE).getAsMention())
                                           .build()
        ),
        //TODO
        GUILD_MEMBER_ROLE_REMOVED(GuildMemberRoleRemoveEvent.class,
                                  event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                             .setTitle("Rôle retiré")
                                             .setDescription(event.getUser().getAsMention() + " " + event.getRoles() + " by " + retrieveModerator(event.getGuild(), ActionType.MEMBER_ROLE_UPDATE).getAsMention())
                                             .build()
        ),
        //TODO
        GUILD_MEMBER_UPDATE_NICKNAME(GuildMemberUpdateNicknameEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                               .setTitle("Surnom mis a jour")
                                               .setDescription(event.getUser().getAsMention() + " " + event.getOldNickname() + " en " + event.getNewNickname() + " by " + retrieveModerator(event.getGuild(), ActionType.MEMBER_UPDATE).getAsMention())
                                               .build()
        ),
        //#endregion
        //#region Permission Events
        //TODO
        PERMISSION_OVERRIDE_CREATE(PermissionOverrideCreateEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                              .setTitle("Permission ajoutée")
                                              .setDescription(event.getPermissionOverride() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_OVERRIDE_CREATE).getAsMention())
                                              .build()
        ),
        //TODO
        PERMISSION_OVERRIDE_DELETE(PermissionOverrideDeleteEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                              .setTitle("Permission supprimée")
                                              .setDescription(event.getPermissionOverride() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_OVERRIDE_DELETE).getAsMention())
                                              .build()
        ),
        //TODO
        PERMISSION_OVERRIDE_UPDATE(PermissionOverrideUpdateEvent.class,
                                   event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                              .setTitle("Permission mise a jour")
                                              .setDescription(event.getPermissionOverride() + " by " + retrieveModerator(event.getGuild(), ActionType.CHANNEL_OVERRIDE_UPDATE).getAsMention())
                                              .build()
        ),
        //#endregion
        //#region Schedule Event Events
        //TODO
        SCHEDULED_EVENT_CREATE(ScheduledEventCreateEvent.class,
                               event -> Vars.getDefaultEmbed(Color.GREEN, event.getGuild())
                                          .setTitle("Événement créé")
                                          .setDescription(event.getScheduledEvent().getName() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_CREATE).getAsMention())
                                          .build()
        ),
        //TODO
        SCHEDULED_EVENT_DELETE(ScheduledEventDeleteEvent.class,
                               event -> Vars.getDefaultEmbed(Color.RED, event.getGuild())
                                          .setTitle("Événement supprimé")
                                          .setDescription(event.getScheduledEvent().getName() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_DELETE).getAsMention())
                                          .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_DESCRIPTION(ScheduledEventUpdateDescriptionEvent.class,
                                           event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                      .setTitle("Événement mis a jour (Description)")
                                                      .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                                      .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_END_TIME(ScheduledEventUpdateEndTimeEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                   .setTitle("Événement mis a jour (Heure de fin)")
                                                   .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                                   .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_IMAGE(ScheduledEventUpdateImageEvent.class,
                                     event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                .setTitle("Événement mis a jour (Image)")
                                                .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                                .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_LOCATION(ScheduledEventUpdateLocationEvent.class,
                                        event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                   .setTitle("Événement mis a jour (Lieu)")
                                                   .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                                   .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_NAME(ScheduledEventUpdateNameEvent.class,
                                    event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                               .setTitle("Événement mis a jour (Nom)")
                                               .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                               .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_START_TIME(ScheduledEventUpdateStartTimeEvent.class,
                                          event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                     .setTitle("Événement mis a jour (Heure de début)")
                                                     .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                                     .build()
        ),
        //TODO
        SCHEDULED_EVENT_UPDATE_STATUS(ScheduledEventUpdateStatusEvent.class,
                                      event -> Vars.getDefaultEmbed(Color.YELLOW, event.getGuild())
                                                 .setTitle("Événement mis a jour (Statut)")
                                                 .setDescription(event.getScheduledEvent().getName() + " " + event.getOldValue() + " en " + event.getNewValue() + " by " + retrieveModerator(event.getGuild(), ActionType.SCHEDULED_EVENT_UPDATE).getAsMention())
                                                 .build()
        ),
        //#endregion
        ;

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

    private static User retrieveModerator(Guild guild, ActionType actionType) {
        return guild.retrieveAuditLogs()
                .type(actionType)
                .limit(1)
                .complete()
                .get(0)
                .getUser();
    }
}
