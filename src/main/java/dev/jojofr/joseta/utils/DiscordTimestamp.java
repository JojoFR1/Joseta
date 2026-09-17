package dev.jojofr.joseta.utils;

import java.time.OffsetDateTime;

public class DiscordTimestamp {
    private final long epochSeconds;
    
    private DiscordTimestamp(long epochSeconds) { this.epochSeconds = epochSeconds; }
    
    public static DiscordTimestamp from(long epochSeconds) { return new DiscordTimestamp(epochSeconds); }
    public static DiscordTimestamp from(OffsetDateTime dateTime) { return new DiscordTimestamp(dateTime.toEpochSecond()); }
    
    /**
     * Returns a short time timestamp, e.g. "12:00 AM".
     * @return A string representing the short time.
     */
    public String shortTime() { return "<t:" + epochSeconds + ":t>"; }
    /**
     * Returns a long time timestamp, e.g. "12:00:00 AM".
     * @return A string representing the long time.
     */
    public String longTime() { return "<t:" + epochSeconds + ":T>"; }
    /**
     * Returns a short date timestamp, e.g. "01/01/2024".
     * @return A string representing the short date.
     */
    public String shortDate() { return "<t:" + epochSeconds + ":d>"; }
    /**
     * Returns a long date timestamp, e.g. "January 1, 2024".
     * @return A string representing the long date.
     */
    public String longDate() { return "<t:" + epochSeconds + ":D>"; }
    /**
     * Returns a short full timestamp, e.g. "Jan 1, 2024 12:00 AM".
     * @return A string representing the short full time.
     */
    public String shortFull() { return "<t:" + epochSeconds + ":f>"; }
    /**
     * Returns a long full timestamp, e.g. "Monday, January 1, 2024 12:00 AM".
     * @return A string representing the long full time.
     */
    public String longFull() { return "<t:" + epochSeconds + ":F>"; }
    /**
     * Returns a relative timestamp, e.g. "5 minutes ago" or "in 2 days".
     * @return A string representing the relative time.
     */
    public String relative() { return "<t:" + epochSeconds + ":R>"; }
}
