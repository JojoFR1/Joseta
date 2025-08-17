package joseta.utils;

public class TimeParser {

    /** Parse a long and converts the seconds as a time String */
    public static String convertSecond(long seconds) {
        String time = "";

        long weeks = seconds / (7 * 24 * 60 * 60);
        seconds %= (7 * 24* 60 * 60);
        long days = seconds / (24 * 60 * 60);
        seconds %= (24 * 60 * 60);
        long hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        long minutes = seconds / 60;
        seconds %= 60;

        if (weeks > 0) time += weeks + "w";
        if (days > 0) time += days + "d";
        if (hours > 0) time += hours + "h";
        if (minutes > 0) time += minutes + "m";
        if (seconds > 0) time += seconds + "s";

        return time;
    }

    
    /** Parse a String and converts the time as seconds - Supports: M(onths), w(eeks), d(ays), h(ours), m(inutes) and s(econds) */
    public static long parse(String time) {
        long parsedTime = 0;
        long value = 0;

        if (time.equals("inf")) return -1;
        
        for (char c : time.toCharArray()) {
            if (c >= '0' && c <= '9')
                value = value * 10 + (c - '0');
            
            else {
                parsedTime += switch (c) {
                    //         Mon. Day Hour Min. Sec.
                    case 'M' -> 4 * 7 * 24 * 60 * 60 * value;
                    case 'w' -> 7 * 24 * 60 * 60 * value;
                    case 'd' -> 24 * 60 * 60 * value;
                    case 'h' -> 60 * 60 * value;
                    case 'm' -> 60 * value;
                    case 's' -> value;
                    default -> 0;
                };
                value = 0;
            }
        }

        return parsedTime;
    }

}
