package joseta.database.entry;

import joseta.database.persister.*;

import arc.struct.*;

import java.net.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "config")
public class ConfigEntry {
    @DatabaseField(id = true, generatedId = false)
    private long guildId;

    //#region Welcome
    @DatabaseField (defaultValue = "false")
    private boolean welcomeEnabled; // TODO maybe separate the leave and join enabled? + global enable?
    @DatabaseField(defaultValue = "0")
    private long welcomeChannelId;
    @DatabaseField(defaultValue = "false")
    private boolean welcomeImageEnabled;
    //TODO adapt welcome message for url
    @DatabaseField
    private String welcomeImageUrl; //todo hard to implement with text position (especially when only text config is available).
    @DatabaseField(defaultValue = "Bienvenue {{user}} !")
    private String welcomeJoinMessage;
    @DatabaseField(defaultValue = "**{{userName}}** nous a quitté...")
    private String welcomeLeaveMessage;
    @DatabaseField(defaultValue = "0")
    private long joinRoleId;
    @DatabaseField(defaultValue = "0")
    private long joinBotRoleId;
    @DatabaseField(defaultValue = "0")
    private long verifiedRoleId;
    //#endregion

    //#region Markov
    @DatabaseField(defaultValue = "false")
    private boolean markovEnabled;
    @DatabaseField(defaultValue = "", persisterClass = LongSeqPersister.class)
    private Seq<Long> markovBlackList;
    //#endregion
    
    @DatabaseField(defaultValue = "false")
    private boolean moderationEnabled;

    @DatabaseField(defaultValue = "false")
    private boolean autoResponseEnabled;

    // A no-arg constructor is required by ORMLite
    private ConfigEntry() {}

    public ConfigEntry(
        long guildId,

        boolean welcomeEnabled, long welcomeChannelId, boolean welcomeImageEnabled,
        URL welcomeImageUrl, String welcomeJoinMessage, String welcomeLeaveMessage,
        long joinRoleId, long joinBotRoleId, long verifiedRoleId,

        boolean markovEnabled, Seq<Long> markovBlackList, Seq<Long> markovCategoryBlackList,

        boolean moderationEnabled,

        boolean autoResponseEnabled
    ) {
        this.guildId = guildId;

        this.welcomeEnabled = welcomeEnabled;
        this.welcomeChannelId = welcomeChannelId;
        this.welcomeImageEnabled = welcomeImageEnabled;
        this.welcomeImageUrl = welcomeImageUrl.toString();
        this.welcomeJoinMessage = welcomeJoinMessage;
        this.welcomeLeaveMessage = welcomeLeaveMessage;
        this.joinRoleId = joinRoleId;
        this.joinBotRoleId = joinBotRoleId;
        this.verifiedRoleId = verifiedRoleId;

        this.markovEnabled = markovEnabled;
        this.markovBlackList = markovBlackList;
        
        this.moderationEnabled = moderationEnabled;

        this.autoResponseEnabled = autoResponseEnabled;
    }


    public long getGuildId() { return guildId; }

    public ConfigEntry setWelcomeEnabled(boolean welcomeEnabled) { this.welcomeEnabled = welcomeEnabled; return this; }
    public boolean isWelcomeEnabled() { return welcomeEnabled; }
    
    public ConfigEntry setWelcomeChannelId(long welcomeChannelId) { this.welcomeChannelId = welcomeChannelId; return this; }
    public long getWelcomeChannelId() { return welcomeChannelId; }

    public ConfigEntry setWelcomeImageEnabled(boolean welcomeImageEnabled) { this.welcomeImageEnabled = welcomeImageEnabled; return this; }
    public boolean isWelcomeImageEnabled() { return welcomeImageEnabled; }

    public ConfigEntry setWelcomeImageUrl(URL welcomeImageUrl) { this.welcomeImageUrl = welcomeImageUrl.toString(); return this; }
    public URL getWelcomeImageUrl() throws MalformedURLException { return URI.create(welcomeImageUrl).toURL(); }

    public ConfigEntry setWelcomeJoinMessage(String welcomeJoinMessage) { this.welcomeJoinMessage = welcomeJoinMessage; return this; }
    public String getWelcomeJoinMessage() { return welcomeJoinMessage; }

    public ConfigEntry setWelcomeLeaveMessage(String welcomeLeaveMessage) { this.welcomeLeaveMessage = welcomeLeaveMessage; return this; }
    public String getWelcomeLeaveMessage() { return welcomeLeaveMessage; }

    public ConfigEntry setJoinRoleId(long joinRoleId) { this.joinRoleId = joinRoleId; return this; }
    public long getJoinRoleId() { return joinRoleId; }

    public ConfigEntry setJoinBotRoleId(long joinBotRoleId) { this.joinBotRoleId = joinBotRoleId; return this; }
    public long getJoinBotRoleId() { return joinBotRoleId; }

    public ConfigEntry setVerifiedRoleId(long verifiedRoleId) { this.verifiedRoleId = verifiedRoleId; return this; }
    public long getVerifiedRoleId() { return verifiedRoleId; }

    public ConfigEntry setMarkovEnabled(boolean markovEnabled) { this.markovEnabled = markovEnabled; return this; }
    public boolean isMarkovEnabled() { return markovEnabled; }

    public ConfigEntry setMarkovBlackList(Seq<Long> markovBlackList) { this.markovBlackList = markovBlackList; return this; }
    public Seq<Long> getMarkovBlackList() { return markovBlackList; }

    public ConfigEntry setModerationEnabled(boolean moderationEnabled) { this.moderationEnabled = moderationEnabled; return this; }
    public boolean isModerationEnabled() { return moderationEnabled; }

    public ConfigEntry setAutoResponseEnabled(boolean autoResponseEnabled) { this.autoResponseEnabled = autoResponseEnabled; return this; }
    public boolean isAutoResponseEnabled() { return autoResponseEnabled; }

    private static Seq<Long> parseLongArray(String[] values) {
        Seq<Long> result = new Seq<>(values.length);
        for (String value : values) {
            if (!value.isEmpty()) result.add(Long.parseLong(value));
        }

        return result;
    }

    public ConfigEntry addMarkovBlackList(long channelId) {
        if (!markovBlackList.contains(channelId) && channelId != 0L) {
            markovBlackList.add(channelId);
        }
        return this;
    }
    public ConfigEntry removeMarkovBlackList(long channelId) {
        if (channelId != 0L) markovBlackList.remove(channelId);
        return this;
    }
}