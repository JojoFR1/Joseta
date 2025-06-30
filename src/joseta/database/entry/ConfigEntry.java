package joseta.database.entry;

import arc.struct.*;

import java.net.*;

import org.hibernate.annotations.*;

import jakarta.persistence.*;

@Entity @Table(name = "config")
public class ConfigEntry {
    @Id
    private long guildId;

    //#region Welcome
    @Column @ColumnDefault("false")
    private boolean welcomeEnabled; // TODO maybe separate the leave and join enabled? + global enable?
    @Column @ColumnDefault("0")
    private long welcomeChannelId;
    @Column @ColumnDefault("false")
    private boolean welcomeImageEnabled;
    //TODO adapt welcome message for url
    @Column
    private String welcomeImageUrl; //todo hard to implement with text position (especially when only text config is available).
    @Column @ColumnDefault("Bienvenue {{user}} !")
    private String welcomeJoinMessage;
    @Column @ColumnDefault("**{{userName}}** nous a quitté...")
    private String welcomeLeaveMessage;
    @Column @ColumnDefault("0")
    private long joinRoleId;
    @Column @ColumnDefault("0")
    private long joinBotRoleId;
    @Column @ColumnDefault("0")
    private long verifiedRoleId;
    //#endregion

    //#region Markov
    @Column @ColumnDefault("false")
    private boolean markovEnabled;
    @Column @ColumnDefault("[]")
    private Seq<Long> markovBlackList;
    //#endregion
    
    @Column @ColumnDefault("false")
    private boolean moderationEnabled;

    @Column @ColumnDefault("false")
    private boolean autoResponseEnabled;

    // A no-arg constructor is required by ORMLite & for base initialization
    private ConfigEntry() {}
    
    public ConfigEntry(long guildId) { this.guildId = guildId; }

    public ConfigEntry(ConfigEntry other) {
        this.guildId = other.guildId;

        this.welcomeEnabled = other.welcomeEnabled;
        this.welcomeChannelId = other.welcomeChannelId;
        this.welcomeImageEnabled = other.welcomeImageEnabled;
        this.welcomeImageUrl = other.welcomeImageUrl;
        this.welcomeJoinMessage = other.welcomeJoinMessage;
        this.welcomeLeaveMessage = other.welcomeLeaveMessage;
        this.joinRoleId = other.joinRoleId;
        this.joinBotRoleId = other.joinBotRoleId;
        this.verifiedRoleId = other.verifiedRoleId;
        this.markovEnabled = other.markovEnabled;
        this.markovBlackList = new Seq<>(other.markovBlackList);

        this.moderationEnabled = other.moderationEnabled;

        this.autoResponseEnabled = other.autoResponseEnabled;
    }

    public long getGuildId() { return guildId; }
    public ConfigEntry setGuildId(long guildId) { this.guildId = guildId; return this; }

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

    public static Seq<Long> seqFromString(String str) {
        if (str == null || str.isEmpty() || str.equals("[]")) return new Seq<>();
        str = str.replace("[", "").replace("]", "").trim();
        String[] values = str.split(",");
        return parseLongArray(values);
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