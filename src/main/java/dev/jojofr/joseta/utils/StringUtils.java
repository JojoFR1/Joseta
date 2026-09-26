package dev.jojofr.joseta.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class StringUtils {
    
    // Format number to a more readable format
    public static String formatNumber(long number) {
        return NumberFormat.getNumberInstance(Locale.FRANCE).format(number).replace('\u202F', ' ');
    }
}