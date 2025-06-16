package joseta.database.entry;

import arc.struct.*;

import java.net.*;

public class ConfigEntry {
    public long guildId;

    //#region Welcome
    public boolean welcomeEnabled; // TODO maybe separate the leave and join enabled? + global enable?
    public long welcomeChannelId;
    public boolean welcomeImageEnabled;
    //TODO adapt welcome message for url & var like {{var}}
    public URL welcomeImageUrl; //todo hard to implement with text position (especially when only text config is available).
    public String welcomeJoinMessage;
    public String welcomeLeaveMessage;
    public long joinRoleId;
    public long joinBotRoleId;
    public long verifiedRoleId;
    //#endregion

    //#region Markov
    public boolean markovEnabled;
    public Seq<Long> markovChannelBlackList;
    public Seq<Long> markovCategoryBlackList;
    //#endregion
    
    public boolean moderationEnabled;

    public boolean autoResponseEnabled;

    public ConfigEntry(
        long guildId,

        boolean welcomeEnabled, long welcomeChannelId, boolean welcomeImageEnabled,
        URL welcomeImageUrl, String welcomeJoinMessage, String welcomeLeaveMessage,
        long joinRoleId, long joinBotRoleId, long verifiedRoleId,

        boolean markovEnabled, Seq<Long> markovChannelBlackList, Seq<Long> markovCategoryBlackList,

        boolean moderationEnabled,

        boolean autoResponseEnabled
    ) {
        this.guildId = guildId;

        this.welcomeEnabled = welcomeEnabled;
        this.welcomeChannelId = welcomeChannelId;
        this.welcomeImageEnabled = welcomeImageEnabled;
        this.welcomeImageUrl = welcomeImageUrl;
        this.welcomeJoinMessage = welcomeJoinMessage;
        this.welcomeLeaveMessage = welcomeLeaveMessage;
        this.joinRoleId = joinRoleId;
        this.joinBotRoleId = joinBotRoleId;
        this.verifiedRoleId = verifiedRoleId;

        this.markovEnabled = markovEnabled;
        this.markovChannelBlackList = markovChannelBlackList;
        this.markovCategoryBlackList = markovCategoryBlackList;
        
        this.moderationEnabled = moderationEnabled;

        this.autoResponseEnabled = autoResponseEnabled;
    }

    public ConfigEntry setWelcomeEnabled(boolean welcomeEnabled) { this.welcomeEnabled = welcomeEnabled; return this; }
    public ConfigEntry setWelcomeChannelId(long welcomeChannelId) { this.welcomeChannelId = welcomeChannelId; return this; }
    public ConfigEntry setWelcomeImageEnabled(boolean welcomeImageEnabled) { this.welcomeImageEnabled = welcomeImageEnabled; return this; }
    public ConfigEntry setWelcomeImageUrl(URL welcomeImageUrl) { this.welcomeImageUrl = welcomeImageUrl; return this; }
    public ConfigEntry setWelcomeJoinMessage(String welcomeJoinMessage) { this.welcomeJoinMessage = welcomeJoinMessage; return this; }
    public ConfigEntry setWelcomeLeaveMessage(String welcomeLeaveMessage) { this.welcomeLeaveMessage = welcomeLeaveMessage; return this; }
    public ConfigEntry setJoinRoleId(long joinRoleId) { this.joinRoleId = joinRoleId; return this; }
    public ConfigEntry setJoinBotRoleId(long joinBotRoleId) { this.joinBotRoleId = joinBotRoleId; return this; }
    public ConfigEntry setVerifiedRoleId(long verifiedRoleId) { this.verifiedRoleId = verifiedRoleId; return this; }
    public ConfigEntry setMarkovEnabled(boolean markovEnabled) { this.markovEnabled = markovEnabled; return this; }
    public ConfigEntry setMarkovChannelBlackList(Seq<Long> markovChannelBlackList) { this.markovChannelBlackList = markovChannelBlackList; return this; }
    public ConfigEntry setMarkovCategoryBlackList(Seq<Long> markovCategoryBlackList) { this.markovCategoryBlackList = markovCategoryBlackList; return this; }
    public ConfigEntry setModerationEnabled(boolean moderationEnabled) { this.moderationEnabled = moderationEnabled; return this; }
    public ConfigEntry setAutoResponseEnabled(boolean autoResponseEnabled) { this.autoResponseEnabled = autoResponseEnabled; return this; }

    public ConfigEntry addMarkovChannelBlackList(long channelId) {
        if (!markovChannelBlackList.contains(channelId) && channelId != 0L) {
            markovChannelBlackList.add(channelId);
        }
        return this;
    }
    public ConfigEntry removeMarkovChannelBlackList(long channelId) {
        if (channelId != 0L) markovChannelBlackList.remove(channelId);
        return this;
    }
    public ConfigEntry addMarkovCategoryBlackList(long categoryId) {
        if (!markovCategoryBlackList.contains(categoryId) &&categoryId != 0L) {
            markovCategoryBlackList.add(categoryId);
        }
        return this;
    }
    public ConfigEntry removeMarkovCategoryBlackList(long categoryId) {
        if (categoryId != 0L) markovCategoryBlackList.remove(categoryId);
        return this;
    }
}