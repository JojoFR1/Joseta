package joseta.events;

import joseta.*;
import joseta.database.*;
import joseta.database.MessagesDatabase.*;
import joseta.utils.*;

import arc.func.*;
import arc.struct.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.entities.channel.unions.*;
import net.dv8tion.jda.api.entities.emoji.*;
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

    private static Color COLOR_CREATE = Color.decode("#7ED321"),
                         COLOR_CREATE_IMPORTANT = Color.decode("#417505"),
                         COLOR_UPDATE = Color.decode("#F8E71C"),
                         COLOR_DELETE = Color.decode("#FF0020"),
                         COLOR_DELETE_IMPORTANT = Color.decode("#D0021B");

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

    //TODO for future me: still need to do the rest of the event category & make clean descriptions
    public enum EventType {
        //#region Channel Events
        CHANNEL_CREATE(ChannelCreateEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ChannelUnion channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.CHANNEL_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau salon "+ channel.getAsMention() +" (`"+ channel.getName() +"`) par " + user.getAsMention() + "**";
                
                return createEmbed(COLOR_CREATE_IMPORTANT, guild, user, description);
            }
        ),
        CHANNEL_DELETE(ChannelDeleteEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ChannelUnion channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.CHANNEL_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**Le salon `"+ channel.getName() +"` a été supprimé par " + user.getAsMention() + "**";
                
                return createEmbed(COLOR_DELETE_IMPORTANT, guild, user, description);
            }
        ),
        CHANNEL_UPDATE_TOPIC(ChannelUpdateTopicEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ChannelUnion channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.CHANNEL_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Salon mis à jour: "+ channel.getAsMention() +" (`"+ channel.getName() +"`) par " + user.getAsMention() + "**\n\n**Ancien sujet:**";
                if (event.getOldValue().isEmpty()) description += " *Vide*";
                else description += "\n```"+ event.getOldValue().replace("`", "\\`") + "```";
                description += "\n\n**Nouveau sujet:**";
                if (event.getNewValue().isEmpty()) description += " *Vide*";
                else description += "```" + event.getNewValue().replace("`", "\\`") + "```";

                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        CHANNEL_UPDATE_SLOWMODE(ChannelUpdateSlowmodeEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ChannelUnion channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.CHANNEL_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Salon mis à jour: "+ channel.getAsMention() +" (`"+ channel.getName() +"`) par " + user.getAsMention() + "**\n\n**Ancien ralenti: ** " + TimeUtils.formatTime(event.getOldValue()) + "\n**Nouveau ralenti:** " + TimeUtils.formatTime(event.getNewValue());
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        CHANNEL_UPDATE_NAME(ChannelUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ChannelUnion channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.CHANNEL_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Salon mis à jour: "+ channel.getAsMention() +" (`"+ channel.getName() +"`) par " + user.getAsMention() + "**\n\n**Ancien nom: ** " + event.getOldValue() + "\n**Nouveau nom:** " + event.getNewValue();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        CHANNEL_UPDATE_NSFW(ChannelUpdateNSFWEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ChannelUnion channel = event.getChannel();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.CHANNEL_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Salon mis à jour: "+ channel.getAsMention() +" (`"+ channel.getName() +"`) par " + user.getAsMention() + "**\n\n**Ancien NSFW: ** " + event.getOldValue() + "\n**Nouveau NSFW:** " + event.getNewValue();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        //#endregion
        //#region Emoji Events
        EMOJI_ADDED(EmojiAddedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                RichCustomEmoji emoji = event.getEmoji();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.EMOJI_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau emoji "+ emoji.getFormatted() +" (`"+ emoji.getName() +"`) par " + user.getAsMention() + ".**";
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        EMOJI_REMOVED(EmojiRemovedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                RichCustomEmoji emoji = event.getEmoji();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.EMOJI_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**L'emoji `"+ emoji.getName() +"` a été supprimer par " + user.getAsMention() + ".**";
                
                return createEmbed(COLOR_DELETE, guild, user, description);
            }
        ),
        EMOJI_UPDATE_NAME(EmojiUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                RichCustomEmoji emoji = event.getEmoji();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.EMOJI_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau nom pour "+ emoji.getFormatted() +" (`"+ emoji.getName() +"`) par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        EMOJI_UPDATE_ROLES(EmojiUpdateRolesEvent.class,
            event -> {
                Guild guild = event.getGuild();
                RichCustomEmoji emoji = event.getEmoji();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.EMOJI_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau rôle(s) pour "+ emoji.getFormatted() +" (`"+ emoji.getName() +"`) par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        //#endregion
        //#region Guild Events
        GUILD_BAN(GuildBanEvent.class,
            event -> {
                Guild guild = event.getGuild();
                User bannedUser = event.getUser();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.BAN);
                User user = auditLogEntry.getUser();

                String description = "**"+ bannedUser.getAsMention() +" (`" + bannedUser.getName() +"`) a été banni par " + user.getAsMention() +"**";
                
                return createEmbed(COLOR_DELETE_IMPORTANT, guild, user, description);
            }
        ),
        GUILD_UNBAN(GuildUnbanEvent.class,
            event -> {
                Guild guild = event.getGuild();
                User unbannedUser = event.getUser();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.UNBAN);
                User user = auditLogEntry.getUser();

                String description = "**"+ unbannedUser.getAsMention() +" (`" + unbannedUser.getName() +"`) a été débanni par " + user.getAsMention() +"**";
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        GUILD_INVITE_CREATE(GuildInviteCreateEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.INVITE_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau lien d'invitation créé par " + user.getAsMention() +"**\n\n**Code:** `"+ event.getCode() + "`";
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        GUILD_INVITE_DELETE(GuildInviteDeleteEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.INVITE_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**Le lien d'invitation `"+ event.getCode() +"` a été supprimé par" + user.getAsMention() +"**";
                
                return createEmbed(COLOR_DELETE, guild, user, description);
            }
        ),
        GUILD_UPDATE_AFK_CHANNEL(GuildUpdateAfkChannelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien salon AFK:** "+ event.getOldAfkChannel().getAsMention() + " (`"+ event.getOldAfkChannel().getName() +"`)\n**Nouveau salon AFK:** "+ event.getNewAfkChannel().getAsMention() + " (`"+ event.getNewAfkChannel().getName() +"`)";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_AFK_TIMEOUT(GuildUpdateAfkTimeoutEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien temps d'AFK:** "+ event.getOldAfkTimeout() +"\n**Nouveau temps d'AFK:** "+ event.getNewAfkTimeout();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_BANNER(GuildUpdateBannerEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancienne bannière:** "+ event.getOldBannerUrl() +"\n**Nouvelle bannière:** "+ event.getNewBannerUrl();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_COMMUNITY_UPDATES_CHANNEL(GuildUpdateCommunityUpdatesChannelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien salon de mise à jour de communauté:** "+ event.getOldCommunityUpdatesChannel().getAsMention() +"\n**Nouveau salon de mise à jour de communauté:** "+ event.getNewCommunityUpdatesChannel().getAsMention();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_DESCRIPTION(GuildUpdateDescriptionEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancienne description:** "+ event.getOldDescription() +"\n**Nouvelle description:** "+ event.getNewDescription();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_EXPLICIT_CONTENT_LEVEL(GuildUpdateExplicitContentLevelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien filtre d'images explicite:** "+ event.getOldLevel() +"\n**Nouveau filtre d'images explicite:** "+ event.getNewLevel();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_FEATURES(GuildUpdateFeaturesEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien feature?:** "+ event.getOldFeatures() +"\n**Nouveau feature?:** "+ event.getNewFeatures();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_ICON(GuildUpdateIconEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien icône:** "+ event.getOldIconUrl() +"\n**Nouvelle icône:** "+ event.getNewIconUrl();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_LOCALE(GuildUpdateLocaleEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancienne langue:** "+ event.getOldValue().getNativeName() +"\n**Nouvelle langue:** "+ event.getNewValue().getNativeName();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_MFA_LEVEL(GuildUpdateMFALevelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien mfa?:** "+ event.getOldValue() +"\n**Nouveau mfa?:** "+ event.getNewMFALevel();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_NSFW_LEVEL(GuildUpdateNSFWLevelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien niveau NSFW:** "+ event.getOldNSFWLevel() +"\n**Nouveau niveau NSFW:** "+ event.getNewNSFWLevel();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_NAME(GuildUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien nom:** "+ event.getOldName() +"\n**Nouveau nom:** "+ event.getNewName();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_NOTIFICATION_LEVEL(GuildUpdateNotificationLevelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien niveau de notification:** "+ event.getOldNotificationLevel() +"\n**Nouveau niveau de notification:** "+ event.getNewNotificationLevel();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_OWNER(GuildUpdateOwnerEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien propriétaire:** "+ event.getOldOwnerId() +"\n**Nouveau propriétaire:** "+ event.getNewOwnerId();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_RULES_CHANNEL(GuildUpdateRulesChannelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien salon des règles:** "+ event.getOldRulesChannel().getAsMention() +"\n**Nouveau salon des règles:** "+ event.getNewRulesChannel().getAsMention();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_SAFETY_ALERTS_CHANNEL(GuildUpdateSafetyAlertsChannelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien salon des alerte de sécurité:** "+ event.getOldSafetyAlertsChannel().getAsMention() +"\n**Nouveau salon des alerte de sécurité:** "+ event.getNewSafetyAlertsChannel().getAsMention();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_SPLASH(GuildUpdateSplashEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien splash?:** "+ event.getOldSplashUrl() +"\n**Nouveau splash?:** "+ event.getNewSplashUrl();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_SYSTEM_CHANNEL(GuildUpdateSystemChannelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien salon des mises à jour système:** "+ event.getOldSystemChannel().getAsMention() +"\n**Nouveau salon des mises à jour système:** "+ event.getNewSystemChannel().getAsMention();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_VANITY_CODE(GuildUpdateVanityCodeEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien vanity code?:** "+ event.getOldVanityCode() +"\n**Nouveau vanity code?:** "+ event.getNewVanityCode();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_UPDATE_VERIFICATION_LEVEL(GuildUpdateVerificationLevelEvent.class,
            event -> {
                Guild guild = event.getGuild();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.GUILD_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Guilde mise à jour par " + user.getAsMention() +"**\n\n**Ancien niveau de vérification:** "+ event.getOldVerificationLevel() +"\n**Nouveau niveau de vérification:** "+ event.getNewVerificationLevel();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        //#endregion
        //#region Member Events
        GUILD_MEMBER_JOIN(GuildMemberJoinEvent.class,
            event -> {
                Guild guild = event.getGuild();
                User user = event.getUser();

                String description = "**Nouveau membre "+ user.getAsMention() +" (`"+ user.getName() +"`)**";
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        GUILD_MEMBER_REMOVE(GuildMemberRemoveEvent.class,
            event -> {
                Guild guild = event.getGuild();
                User user = event.getUser();

                String description = "**Le membre "+ user.getAsMention() +" (`"+ user.getName() +"`) a quitté.**";
                
                return createEmbed(COLOR_DELETE, guild, user, description);
            }
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
        GUILD_MEMBER_UPDATE_NICKNAME(GuildMemberUpdateNicknameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                User member = event.getUser();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.MEMBER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Membre mis a jour: "+ member.getAsMention() +" (`" + member.getName() +"`) par " + user.getAsMention() +"**\n\n**Ancien pseudo:** "+ event.getOldNickname() +"\n**Nouveau pseudo:** "+ event.getNewNickname();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_VOICE_GUILD_DEAFEN(GuildVoiceGuildDeafenEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Member member = event.getMember();

                String description = "**Membre mis a jour: "+ member.getAsMention() +" (`" + member.getUser().getName() +"`)**\n\n**Sourd:** "+ event.getVoiceState().isDeafened();
                
                return createEmbed(COLOR_UPDATE, guild, member.getUser(), description);
            }
        ),
        GUILD_VOICE_GUILD_MUTE(GuildVoiceGuildMuteEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Member member = event.getMember();

                String description = "**Membre mis a jour: "+ member.getAsMention() +" (`" + member.getUser().getName() +"`)**\n\n**Mute:** "+ event.getVoiceState().isMuted();
                
                return createEmbed(COLOR_UPDATE, guild, member.getUser(), description);
            }

        ),
        GUILD_VOICE_UPDATE(GuildVoiceUpdateEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Member member = event.getMember();

                String description = "**Le membre "+ member.getAsMention() +" (`" + member.getUser().getName() +"`) a changer de salon vocal**\n\n**Ancien:** "+ event.getChannelLeft() +"\n**Nouveau:** "+ event.getChannelJoined();
                
                return createEmbed(COLOR_UPDATE, guild, member.getUser(), description);
            }
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
        //#region Schedule Event Events
        SCHEDULED_EVENT_CREATE(ScheduledEventCreateEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau événement "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + ".**";
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        SCHEDULED_EVENT_DELETE(ScheduledEventDeleteEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**L'événement `"+ scheduledEvent.getName() +"` a été supprimer par " + user.getAsMention() + ".**";
                
                return createEmbed(COLOR_DELETE, guild, user, description);
            }
        ),
        SCHEDULED_EVENT_UPDATE_DESCRIPTION(ScheduledEventUpdateDescriptionEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancienne description:**";
                if (event.getOldValue().isEmpty()) description += " *Vide*";
                else description += "\n```"+ event.getOldValue().replace("`", "\\`") + "```";
                description += "\n\n**Nouvelle description:**";
                if (event.getNewValue().isEmpty()) description += " *Vide*";
                else description += "```" + event.getNewValue().replace("`", "\\`") + "```";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        // TODO format to more readable time*
        SCHEDULED_EVENT_UPDATE_END_TIME(ScheduledEventUpdateEndTimeEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancienne date de fin:** "+ event.getOldEndTime() +"\n**Nouvelle date de fin:** "+ event.getNewEndTime();
                                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        SCHEDULED_EVENT_UPDATE_IMAGE(ScheduledEventUpdateImageEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancienne image:** "+ event.getOldImageUrl() +"\n**Nouvelle image:** "+ event.getNewImageUrl();

                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        // TODO support channel ids
        SCHEDULED_EVENT_UPDATE_LOCATION(ScheduledEventUpdateLocationEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancien lieu:** "+ event.getOldLocation() +"\n**Nouveau lieu:** "+ event.getNewLocation();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        SCHEDULED_EVENT_UPDATE_NAME(ScheduledEventUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancien nom:** "+ event.getOldName() +"\n**Nouveau nom:** "+ event.getNewName();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        // TODO format to more readable time
        SCHEDULED_EVENT_UPDATE_START_TIME(ScheduledEventUpdateStartTimeEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancienne date de début:** "+ event.getOldStartTime() +"\n**Nouvelle date de début:** "+ event.getNewStartTime();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        SCHEDULED_EVENT_UPDATE_STATUS(ScheduledEventUpdateStatusEvent.class,
            event -> {
                Guild guild = event.getGuild();
                ScheduledEvent scheduledEvent = event.getScheduledEvent();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.SCHEDULED_EVENT_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Événement mis à jour: "+ scheduledEvent.getJumpUrl() +" (`"+ scheduledEvent.getName() +"`) par " + user.getAsMention() + "**.\n\n**Ancien statut:** "+ event.getOldStatus() +"\n**Nouveau statut:** "+ event.getNewStatus();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
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
                
                return createEmbed(COLOR_DELETE, guild, user, description);
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

                String description = "**Message envoyé par " + user.getAsMention() + " modifié: (" + event.getMessage().getJumpUrl() + ")**\n\n**Ancien**\n```" + MessagesDatabase.getMessageEntry(messageId, guild.getIdLong(), channel.getIdLong()).content.replace("`", "\\`") + "```\n**Nouveau**\n```" + event.getMessage().getContentRaw().replace("`", "\\`") + "```";
                MessagesDatabase.updateMessage(messageId, guild.getIdLong(), channel.getIdLong(), event.getMessage().getContentRaw());
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        MESSAGE_REACTION_REMOVE_EMOJI(MessageReactionRemoveEmojiEvent.class,
            event -> {
                Guild guild = event.getGuild();

                String description = "**La réaction "+ event.getEmoji().getFormatted() +" de "+ event.getJumpUrl() +" a été retirer.**";
                
                return createEmbed(COLOR_DELETE, guild, null, description);
            }
        ),
        MESSAGE_REACTION_REMOVE_ALL(MessageReactionRemoveAllEvent.class,
            event -> {
                Guild guild = event.getGuild();

                String description = "**Les réactions de "+ event.getJumpUrl() +" ont toutes été retirer.**";
                
                return createEmbed(COLOR_DELETE_IMPORTANT, guild, null, description);
            }
        ),
        //#endregion
        //#region Role Events
        ROLE_CREATE(RoleCreateEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_CREATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau rôle "+ role.getAsMention() +" (`"+ role.getName() +"`) par " + user.getAsMention() + "**";
                
                return createEmbed(COLOR_CREATE_IMPORTANT, guild, user, description);
            }
        ),
        ROLE_DELETE(RoleDeleteEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**Le rôle "+ role.getName() +" a été supprimé par " + user.getAsMention() + "**";
                
                return createEmbed(COLOR_DELETE_IMPORTANT, guild, user, description);
            }
        ),
        ROLE_UPDATE_COLOR(RoleUpdateColorEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                Color oldColor = event.getOldColor();
                Color newColor = event.getNewColor();

                //TODO maybe add image representing the color?
                String description = "**Rôle mis a jour: "+ role.getAsMention() +" (`" + role.getName() +"`) par " + user.getAsMention() + "**\n\n #"+ Integer.toHexString(oldColor.getRGB()) +" --> #"+ Integer.toHexString(newColor.getRGB());
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }

        ),
        ROLE_UPDATE_HOIST(RoleUpdateHoistedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Rôle mis a jour: "+ role.getAsMention() +" (`" + role.getName() +"`) par " + user.getAsMention() + "**\n\n **S'affiche séparement:** "+ event.getOldValue()  +" --> "+ event.getNewValue();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        ROLE_UPDATE_ICON(RoleUpdateIconEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                RoleIcon oldIcon = event.getOldIcon();
                RoleIcon newIcon = event.getNewIcon();

                String description = "**Rôle mis a jour: "+ role.getAsMention() +" (`" + role.getName() +"`) par " + user.getAsMention() + "**\n\n**Ancienne icône:** "+ (oldIcon.isEmoji() ? oldIcon.getEmoji() : oldIcon.getIconUrl())  +"\n**Nouvelle icône:** "+ (newIcon.isEmoji() ? newIcon.getEmoji() : newIcon.getIconUrl());
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        ROLE_UPDATE_MENTIONABLE(RoleUpdateMentionableEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Rôle mis a jour: "+ role.getAsMention() +" (`" + role.getName() +"`) par " + user.getAsMention() + "**\n\n **Mentionnable:** "+ event.getOldValue()  +" --> "+ event.getNewValue();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        ROLE_UPDATE_NAME(RoleUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Rôle mis a jour: "+ role.getAsMention() +" (`" + role.getName() +"`) par " + user.getAsMention() + "**\n\n **Ancien nom:** "+ event.getOldName()  +"\n**Nouveau nom:** "+ event.getNewName();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
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
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        ROLE_UPDATE_POSITION(RoleUpdatePositionEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Role role = event.getRole();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.ROLE_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Rôle mis a jour: "+ role.getAsMention() +" (`" + role.getName() +"`) par " + user.getAsMention() + "**\n\n **Position:** "+ event.getOldValue()  +" --> "+ event.getNewValue();
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
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
                
                return createEmbed(COLOR_CREATE, guild, user, description);
            }
        ),
        GUILD_STICKER_REMOVED(GuildStickerRemovedEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_DELETE);
                User user = auditLogEntry.getUser();

                String description = "**Le sticker (`"+ sticker.getName() +"`) a été supprimé par " + user.getAsMention() + ".**";
                
                return createEmbed(COLOR_DELETE, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_AVAILABLE(GuildStickerUpdateAvailableEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau \"availablility\"? pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_DESCRIPTION(GuildStickerUpdateDescriptionEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouelle description pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
            }
        ),
        GUILD_STICKER_UPDATE_NAME(GuildStickerUpdateNameEvent.class,
            event -> {
                Guild guild = event.getGuild();
                Sticker sticker = event.getSticker();

                AuditLogEntry auditLogEntry = retrieveAuditLog(guild, ActionType.STICKER_UPDATE);
                User user = auditLogEntry.getUser();

                String description = "**Nouveau nom pour le sticker `" + sticker.getName() +"` par " + user.getAsMention() + "**\n\n `"+ event.getOldValue() +"` --> `"+ event.getNewValue() +"`";
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
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
                
                return createEmbed(COLOR_UPDATE, guild, user, description);
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

    //TODO improve this function + more method to lower repetition of code in all the events
    // and allow to add images to the messag + maybe generate color image for color difference 
    private static MessageEmbed createEmbed(Color color, Guild guild, User author, String description) {
        return Vars.getDefaultEmbed(color, guild, author)
                .setDescription(description)
                .build();
    }

    //TODO is this needed to precise?
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