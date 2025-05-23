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

    
    /** Parse a String and converts the time as seconds - Supports: w(eeks), d(ays), h(ours), m(inutes) and s(econds) */
    public static long parse(String time) {
        long parsedTime = 0;
        int value = 0;

        if (time == "inf") return -1; 
        
        for (char c : time.toLowerCase().toCharArray()) {
            if (c >= '0' && c <= '9')
                value = value * 10 + (c - '0');
            
            else switch (c) {
                                     // Day Hour Min. Sec.
                case 'w': parsedTime += 7 * 24 * 60 * 60 * value; value = 0; break;
                case 'd': parsedTime +=     24 * 60 * 60 * value; value = 0; break;
                case 'h': parsedTime +=          60 * 60 * value; value = 0; break;
                case 'm': parsedTime +=               60 * value; value = 0; break;
                case 's': parsedTime +=                    value; value = 0; break;
                default:  parsedTime +=                    value; value = 0; break;
            }    
        }

        return parsedTime;
    }

}
