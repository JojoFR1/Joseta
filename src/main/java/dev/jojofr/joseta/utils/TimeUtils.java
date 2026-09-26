package dev.jojofr.joseta.utils;

import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    private static final long SECONDS_IN_MINUTE = 60L;
    private static final long SECONDS_IN_HOUR = 60L * SECONDS_IN_MINUTE;
    private static final long SECONDS_IN_DAY = 24L * SECONDS_IN_HOUR;
    private static final long SECONDS_IN_WEEK = 7L * SECONDS_IN_DAY;
    private static final long SECONDS_IN_MONTH = 30L * SECONDS_IN_DAY;
    private static final long SECONDS_IN_YEAR = 365L * SECONDS_IN_DAY;
    
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhdjwSMYA])");
    
    /**
     * Converts a duration in seconds to a formatted human-readable string.
     *
     * @param seconds The time in seconds to be converted.
     * @return A formatted string representing the time in months, weeks, days, hours, minutes and seconds.
     * @throws IllegalArgumentException if the input seconds is negative.
     */
    public static String formatTime(long seconds) { return formatTime(seconds, false); }
    public static String formatTime(long seconds, boolean readable) {
        if (seconds < 0) return readable ? "infini" : "inf";
        if (seconds == 0) return readable ? "0 seconde" : "0s";
        
        StringBuilder timeBuilder = new StringBuilder();
        long years = seconds / SECONDS_IN_YEAR;
        if (years > 0) {
            timeBuilder.append(years);
            if (readable) timeBuilder.append("année").append(years > 1 ? "s " : " ");
            else timeBuilder.append("A ");
            
            seconds %= SECONDS_IN_YEAR;
        }
        
        long months = seconds / SECONDS_IN_MONTH;
        if (months > 0) {
            timeBuilder.append(months);
            if (readable) timeBuilder.append("mois ");
            else timeBuilder.append("M ");
            
            seconds %= SECONDS_IN_MONTH;
        }
        
        long weeks = seconds / SECONDS_IN_WEEK;
        if (weeks > 0) {
            timeBuilder.append(weeks);
            if (readable) timeBuilder.append("semaine").append(weeks > 1 ? "s " : " ");
            else timeBuilder.append("S ");
            
            seconds %= SECONDS_IN_WEEK;
        }
        
        long days = seconds / SECONDS_IN_DAY;
        if (days > 0) {
            timeBuilder.append(days);
            if (readable) timeBuilder.append("jour").append(days > 1 ? "s " : " ");
            else timeBuilder.append("j ");
            
            seconds %= SECONDS_IN_DAY;
        }
        
        long hours = seconds / SECONDS_IN_HOUR;
        if (hours > 0) {
            timeBuilder.append(hours);
            if (readable) timeBuilder.append("heure").append(hours > 1 ? "s " : " ");
            else timeBuilder.append("h ");
            
            seconds %= SECONDS_IN_HOUR;
        }
        
        long minutes = seconds / SECONDS_IN_MINUTE;
        if (minutes > 0) {
            timeBuilder.append(minutes);
            if (readable) timeBuilder.append("minute").append(minutes > 1 ? "s " : " ");
            else timeBuilder.append("m ");
            
            seconds %= SECONDS_IN_MINUTE;
        }
        
        if (seconds > 0) timeBuilder.append(seconds).append(readable ? "seconde" + (seconds > 1 ? "s" : "") : "s");
        
        return timeBuilder.toString();
    }
    
    /**
     * Parses a formatted time string into a total number of seconds.
     *
     * @param time The time string to be parsed, e.g., "1M2w3d4h5m6s" or "inf".
     * @return The total time in seconds represented by the input string.
     */
    public static long parseTime(String time) {
        if (time == null || time.isEmpty()) return 0;
        if (time.equalsIgnoreCase("inf")) return -1;
        
        long totalSeconds = 0;
        Matcher matcher = TIME_PATTERN.matcher(time);
        
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            
            totalSeconds += switch (unit) {
                case 'Y', 'y', 'A', 'a' -> value * SECONDS_IN_YEAR;
                case 'M' -> value * SECONDS_IN_MONTH;
                case 'W', 'w', 'S' -> value * SECONDS_IN_WEEK;
                case 'D', 'd', 'J', 'j' -> value * SECONDS_IN_DAY;
                case 'H', 'h' -> value * SECONDS_IN_HOUR;
                case 'm' -> value * SECONDS_IN_MINUTE;
                case 's' -> value;
                default -> 0;
            };
        }
        
        return totalSeconds;
    }
}
