package joseta.database.entities;

import jakarta.persistence.*;

import java.lang.reflect.*;
import java.lang.reflect.Parameter;

@Entity @Table(name = "configurations")
public class Configuration {
    @Id long guildId;

    @Column boolean welcomeEnabled;
    @Column boolean welcomeImageEnabled;
    @Column long welcomeChannelId;
    @Column String welcomeJoinMessage = "Bienvenue {{user}} !";
    @Column String welcomeLeaveMessage = "**{{userName}}** nous a quitté...";
    @Column long joinRoleId;
    @Column long joinBotRoleId;
    @Column long verifiedRoleId;

    @Column boolean markovEnabled;
    @Column String markovBlackList = "";

    @Column boolean moderationEnabled;

    @Column boolean autoResponseEnabled;

    @Column boolean countingEnabled;
    @Column boolean countingCommentsEnabled;
    @Column boolean countingPenaltyEnabled;
    @Column long countingChannelId;

    protected Configuration() {}

    public Configuration(long guildId) { this.guildId = guildId; }

    public Configuration(Configuration other) {
        this.guildId = other.guildId;

        this.welcomeEnabled = other.welcomeEnabled;
        this.welcomeChannelId = other.welcomeChannelId;
        this.welcomeImageEnabled = other.welcomeImageEnabled;
        this.welcomeJoinMessage = other.welcomeJoinMessage;
        this.welcomeLeaveMessage = other.welcomeLeaveMessage;
        this.joinRoleId = other.joinRoleId;
        this.joinBotRoleId = other.joinBotRoleId;
        this.verifiedRoleId = other.verifiedRoleId;

        this.markovEnabled = other.markovEnabled;
        this.markovBlackList = other.markovBlackList;

        this.moderationEnabled = other.moderationEnabled;

        this.autoResponseEnabled = other.autoResponseEnabled;

        this.countingEnabled = other.countingEnabled;
        this.countingCommentsEnabled = other.countingCommentsEnabled;
        this.countingPenaltyEnabled = other.countingPenaltyEnabled;
        this.countingChannelId = other.countingChannelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public Configuration setGuildId(long guildId) {
        this.guildId = guildId;
        return this;
    }

    public boolean isWelcomeEnabled() {
        return welcomeEnabled;
    }

    public Configuration setWelcomeEnabled(boolean welcomeEnabled) {
        this.welcomeEnabled = welcomeEnabled;
        return this;
    }

    public boolean isWelcomeImageEnabled() {
        return welcomeImageEnabled;
    }

    public Configuration setWelcomeImageEnabled(boolean welcomeImageEnabled) {
        this.welcomeImageEnabled = welcomeImageEnabled;
        return this;
    }

    public long getWelcomeChannelId() {
        return welcomeChannelId;
    }

    public Configuration setWelcomeChannelId(long welcomeChannelId) {
        this.welcomeChannelId = welcomeChannelId;
        return this;
    }

    public String getWelcomeJoinMessage() {
        return welcomeJoinMessage;
    }

    public Configuration setWelcomeJoinMessage(String welcomeJoinMessage) {
        this.welcomeJoinMessage = welcomeJoinMessage;
        return this;
    }

    public String getWelcomeLeaveMessage() {
        return welcomeLeaveMessage;
    }

    public Configuration setWelcomeLeaveMessage(String welcomeLeaveMessage) {
        this.welcomeLeaveMessage = welcomeLeaveMessage;
        return this;
    }

    public long getJoinRoleId() {
        return joinRoleId;
    }

    public Configuration setJoinRoleId(long joinRoleId) {
        this.joinRoleId = joinRoleId;
        return this;
    }

    public long getJoinBotRoleId() {
        return joinBotRoleId;
    }

    public Configuration setJoinBotRoleId(long joinBotRoleId) {
        this.joinBotRoleId = joinBotRoleId;
        return this;
    }

    public long getVerifiedRoleId() {
        return verifiedRoleId;
    }

    public Configuration setVerifiedRoleId(long verifiedRoleId) {
        this.verifiedRoleId = verifiedRoleId;
        return this;
    }

    public boolean isMarkovEnabled() {
        return markovEnabled;
    }

    public Configuration setMarkovEnabled(boolean markovEnabled) {
        this.markovEnabled = markovEnabled;
        return this;
    }

    public String getMarkovBlackList() {
        return markovBlackList;
    }

    public Configuration setMarkovBlackList(String markovBlackList) {
        this.markovBlackList = markovBlackList;
        return this;
    }

    public boolean isModerationEnabled() {
        return moderationEnabled;
    }

    public Configuration setModerationEnabled(boolean moderationEnabled) {
        this.moderationEnabled = moderationEnabled;
        return this;
    }

    public boolean isAutoResponseEnabled() {
        return autoResponseEnabled;
    }

    public Configuration setAutoResponseEnabled(boolean autoResponseEnabled) {
        this.autoResponseEnabled = autoResponseEnabled;
        return this;
    }

    public boolean isCountingEnabled() {
        return countingEnabled;
    }

    public Configuration setCountingEnabled(boolean countingEnabled) {
        this.countingEnabled = countingEnabled;
        return this;
    }

    public boolean isCountingCommentsEnabled() {
        return countingCommentsEnabled;
    }

    public Configuration setCountingCommentsEnabled(boolean countingCommentsEnabled) {
        this.countingCommentsEnabled = countingCommentsEnabled;
        return this;
    }

    public boolean isCountingPenaltyEnabled() {
        return countingPenaltyEnabled;
    }

    public Configuration setCountingPenaltyEnabled(boolean countingPenaltyEnabled) {
        this.countingPenaltyEnabled = countingPenaltyEnabled;
        return this;
    }

    public long getCountingChannelId() {
        return countingChannelId;
    }

    public Configuration setCountingChannelId(long countingChannelId) {
        this.countingChannelId = countingChannelId;
        return this;
    }
}
