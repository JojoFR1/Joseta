package dev.jojofr.joseta.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
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
    public static String format(long seconds) {
        if (seconds < 0) throw new IllegalArgumentException("Seconds cannot be negative.");
        if (seconds == 0) return "0s";
        
        StringBuilder timeBuilder = new StringBuilder();
        long years = seconds / SECONDS_IN_YEAR;
        if (years > 0) {
            timeBuilder.append(years).append("A ");
            seconds %= SECONDS_IN_YEAR;
        }
        
        long months = seconds / SECONDS_IN_MONTH;
        if (months > 0) {
            timeBuilder.append(months).append("M ");
            seconds %= SECONDS_IN_MONTH;
        }
        
        long weeks = seconds / SECONDS_IN_WEEK;
        if (weeks > 0) {
            timeBuilder.append(weeks).append("S ");
            seconds %= SECONDS_IN_WEEK;
        }
        
        long days = seconds / SECONDS_IN_DAY;
        if (days > 0) {
            timeBuilder.append(days).append("j ");
            seconds %= SECONDS_IN_DAY;
        }
        
        long hours = seconds / SECONDS_IN_HOUR;
        if (hours > 0) {
            timeBuilder.append(hours).append("h ");
            seconds %= SECONDS_IN_HOUR;
        }
        
        long minutes = seconds / SECONDS_IN_MINUTE;
        if (minutes > 0) {
            timeBuilder.append(minutes).append("m ");
            seconds %= SECONDS_IN_MINUTE;
        }
        
        if (seconds > 0) timeBuilder.append(seconds).append("s");
        
        return timeBuilder.toString();
    }
    
    
    /**
     * Parses a formatted time string into a total number of seconds.
     *
     * @param time The time string to be parsed, e.g., "1M2w3d4h5m6s" or "inf".
     * @return The total time in seconds represented by the input string.
     */
    public static long parse(String time) {
        if (time == null || time.isEmpty()) return 0;
        if (time.equalsIgnoreCase("inf")) return -1;
        
        long totalSeconds = 0;
        Matcher matcher = TIME_PATTERN.matcher(time);
        
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            
            totalSeconds += switch (unit) {
                case 'Y', 'A' -> value * SECONDS_IN_YEAR;
                case 'M' -> value * SECONDS_IN_MONTH;
                case 'w', 'S' -> value * SECONDS_IN_WEEK;
                case 'd', 'j' -> value * SECONDS_IN_DAY;
                case 'h' -> value * SECONDS_IN_HOUR;
                case 'm' -> value * SECONDS_IN_MINUTE;
                case 's' -> value;
                default -> 0;
            };
        }
        
        return totalSeconds;
    }
    
}