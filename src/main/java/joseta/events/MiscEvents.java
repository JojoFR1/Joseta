package joseta.events;

import joseta.annotations.EventModule;
import joseta.annotations.types.Event;
import joseta.database.Database;
import joseta.database.entities.Configuration;
import joseta.database.helper.MessageDatabase;
import joseta.events.misc.CountingChannel;
import joseta.events.misc.WelcomeChannel;
import joseta.generated.EventType;
import joseta.utils.Log;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.IOException;
import java.util.regex.Pattern;

@EventModule
public class MiscEvents {
    //#region Message
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageDatabase.addNewMessage(event.getMessage());
    }
    
    @Event(type = EventType.MESSAGE_UPDATE)
    public void onMessageUpdate(MessageUpdateEvent event) {
        MessageDatabase.updateMessage(event.getMessage());
    }
    
    @Event(type = EventType.MESSAGE_DELETE)
    public void onMessageDelete(MessageDeleteEvent event) {
        MessageDatabase.deleteMessage(event.getMessageIdLong());
    }
    
    @Event(type = EventType.MESSAGE_BULK_DELETE)
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        for (String messageId : event.getMessageIds())
            MessageDatabase.deleteMessage(Long.parseLong(messageId));
    }
    
    @Event(type = EventType.CHANNEL_DELETE)
    public void onChannelDelete(net.dv8tion.jda.api.events.channel.ChannelDeleteEvent event) {
        MessageDatabase.deleteChannelMessages(event.getChannel().getIdLong());
    }
    //#endregion
    
    
    // TODO improve, too many false positives
    private static final Pattern patternQuestion = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:com*[ea]nt?|pos*ible|m(?:oyen|ani[èeé]re)|fa[cç]on)(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ
    );
    private static final Pattern patternMulti = Pattern.compile(
        "(?:\\b|[.,?!;:])(?:multi[ -]?(?:joueu?r|playeu?r)?|co*p(?:eration|[ea]?ins?)?|amis?|pot[oe]s?|(?:[aà] (?:deux|[2-9]|[1-9]+|plu?si?e?u?rs?)))(?:\\b|[.,?!;:])",
        Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ
    );
    //TODO unhardcode message & emoji
    public static final String autoResponseMessage = "<:doyouknowtheway:1338158294702755900> Vous voulez héberger votre partie pour jouer avec des amis ?\nVous trouverez plus d'informations ici : <https://zetamap.fr/mindustry_hosting/>";
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void autoResponse(MessageReceivedEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.autoResponseEnabled) return;
        
        String text = event.getMessage().getContentRaw();
        if (patternQuestion.matcher(text).find() && patternMulti.matcher(text).find())
            event.getMessage().reply(autoResponseMessage + "\n*Ceci est une réponse automatique possiblement hors-sujet.*").queue();
    }
    
    
    @Event(type = EventType.MESSAGE_RECEIVED)
    public void countingCheck(MessageReceivedEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.countingEnabled) return;
        
        if (event.getAuthor().isBot() || event.getChannel().getIdLong() != config.countingChannelId) return;
        CountingChannel.check(event.getChannel(), event.getMessage());
    }
    
    
    @Event(type = EventType.GUILD_MEMBER_JOIN)
    public void memberJoin(GuildMemberJoinEvent event) throws IOException {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.welcomeEnabled) return;
        
        User user = event.getUser();
        TextChannel channel;
        Role botRole = null, memberRole = null;
        if (config.welcomeChannelId == null || (channel = event.getGuild().getTextChannelById(config.welcomeChannelId)) == null) {
            Log.warn("Welcome channel not found for guild " + event.getGuild().getIdLong());
            return;
        }
        if (!user.isBot() && (config.joinRoleId == null || (memberRole = event.getGuild().getRoleById(config.joinRoleId)) == null)) {
            Log.warn("Join role not found for guild " + event.getGuild().getIdLong());
            return;
        }
        if (user.isBot() && (config.joinBotRoleId == null || (botRole = event.getGuild().getRoleById(config.joinBotRoleId)) == null)) {
            Log.warn("Bot role not found for guild " + event.getGuild().getIdLong());
            return;
        }
        
        if (config.welcomeImageEnabled && WelcomeChannel.imageLoaded) {
            byte[] image = WelcomeChannel.getWelcomeImage(event.getUser(), event.getGuild().getMemberCount());
            
            channel.sendMessage(user.getAsMention()).addFiles(FileUpload.fromData(image, "welcome.png")).queue();
        }
        else if (!config.welcomeJoinMessage.isEmpty()) channel.sendMessage(config.welcomeJoinMessage.replace("{{user}}", user.getAsMention())).queue();
        
        if (user.isBot()) event.getGuild().addRoleToMember(user, botRole).queue();
        else event.getGuild().addRoleToMember(user, memberRole).reason("Rôle d'arrivée automatique").queue();
    }
    
    @Event(type = EventType.GUILD_MEMBER_REMOVE)
    public void memberRemove(GuildMemberRemoveEvent event) {
        Configuration config = Database.get(Configuration.class, event.getGuild().getIdLong());
        if (config == null || !config.welcomeEnabled) return;
        
        TextChannel channel;
        if (config.welcomeChannelId == null || (channel = event.getGuild().getTextChannelById(config.welcomeChannelId)) == null) {
            Log.warn("Welcome channel not found for guild " + event.getGuild().getIdLong());
            return;
        }
        
        if (!config.welcomeLeaveMessage.isEmpty()) channel.sendMessage(config.welcomeLeaveMessage.replace("{{userName}}", event.getUser().getName())).queue();
    }
}
