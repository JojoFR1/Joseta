package joseta.utils;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.Locale;
import java.util.regex.*;

import joseta.utils.func.Func;
import joseta.utils.func.Func2;
import joseta.utils.func.Intf;
import joseta.utils.struct.Seq;

/**
 * Merge of <a href="https://github.com/Anuken/Arc/blob/master/arc-core/src/arc/util/Strings.java">Anuken's</a> and 
 * <a href="https://github.com/xpdustry/Anti-VPN-Service/blob/master/src/main/java/com/xpdustry/avs/util/Strings.java">Xpdustry's</a> 
 * Strings.java
 * 
 * @author Zackari LOHR
 */
public class Strings{
    private static StringBuilder tmp1 = new StringBuilder(), tmp2 = new StringBuilder();
    private static Pattern
        filenamePattern = Pattern.compile("[\0/\"<>|:*?\\\\]"),
        reservedFilenamePattern = Pattern.compile("(CON|AUX|PRN|NUL|(COM[0-9])|(LPT[0-9]))((\\..*$)|$)", Pattern.CASE_INSENSITIVE);

    public static final Charset utf8 = Charset.forName("UTF-8");

    /** @return whether the name matches the query; case-insensitive. Always returns true if query is empty. */
    public static boolean matches(String query, String name){
        return query == null || query.isEmpty() || (name != null && name.toLowerCase().contains(query.toLowerCase()));
    }

    public static int count(CharSequence s, char c){
        int total = 0;
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == c) total ++;
        }
        return total;
    }

    public static String truncate(String s, int length){
        return s.length() <= length ? s : s.substring(0, length);
    }

    public static String truncate(String s, int length, String ellipsis){
        return s.length() <= length ? s : s.substring(0, length) + ellipsis;
    }

    public static String getSimpleMessage(Throwable e){
        Throwable fcause = getFinalCause(e);
        return fcause.getMessage() == null ? fcause.getClass().getSimpleName() : fcause.getClass().getSimpleName() + ": " + fcause.getMessage();
    }

    public static String getFinalMessage(Throwable e){
        String message = e.getMessage();
        while(e.getCause() != null){
            e = e.getCause();
            if(e.getMessage() != null){
                message = e.getMessage();
            }
        }
        return message;
    }

    public static Throwable getFinalCause(Throwable e){
        while(e.getCause() != null){
            e = e.getCause();
        }
        return e;
    }

    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /** @return a neat error message of a throwable, with stack trace. */
    public static String neatError(Throwable e){
        return neatError(e, true);
    }

    /** @return a neat error message of a throwable, with stack trace. */
    public static String neatError(Throwable e, boolean stacktrace){
        StringBuilder build = new StringBuilder();

        while(e != null){
            String name = e.getClass().toString().substring("class ".length()).replace("Exception", "");
            if(name.indexOf('.') != -1){
                name = name.substring(name.lastIndexOf('.') + 1);
            }

            build.append("> ").append(name);
            if(e.getMessage() != null){
                build.append(": ");
                build.append("'").append(e.getMessage()).append("'");
            }

            if(stacktrace){
                for(StackTraceElement s : e.getStackTrace()){
                    if(s.getClassName().contains("MethodAccessor") || s.getClassName().substring(s.getClassName().lastIndexOf(".") + 1).equals("Method")) continue;
                    build.append("\n");

                    String className = s.getClassName();
                    build.append(className.substring(className.lastIndexOf(".") + 1)).append(".").append(s.getMethodName()).append(": ").append(s.getLineNumber());
                }
            }

            build.append("\n");

            e = e.getCause();
        }


        return build.toString();
    }

    public static int count(String str, String substring){
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(substring, lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += substring.length();
            }
        }
        return count;
    }

    /** Replaces non-safe filename characters with '_'. Handles reserved window file names. */
    public static String sanitizeFilename(String str){
        if(str.equals(".")){
            return "_";
        }else if(str.equals("..")){
            return "__";
        }else if(reservedFilenamePattern.matcher(str).matches()){
            //turn things like con.msch -> _con.msch, which is no longer reserved
            str = "_" + str;
        }
        return filenamePattern.matcher(str).replaceAll("_");
    }

    public static String encode(String str){
        try{
            return URLEncoder.encode(str, "UTF-8");
        }catch(UnsupportedEncodingException why){
            //why the HECK does this even throw an exception
            throw new RuntimeException(why);
        }
    }

    public static String format(String text, Object... args){
        if(args.length > 0){
            StringBuilder out = new StringBuilder(text.length() + args.length*2);
            int argi = 0;
            for(int i = 0; i < text.length(); i++){
                char c = text.charAt(i);
                if(c == '@' &&  argi < args.length){
                    out.append(args[argi++]);
                }else{
                    out.append(c);
                }
            }

            return out.toString();
        }

        return text;
    }

    public static String join(String separator, String... strings){
        StringBuilder builder = new StringBuilder();
        for(String s : strings){
            builder.append(s);
            builder.append(separator);
        }
        builder.setLength(builder.length() - separator.length());
        return builder.toString();
    }

    public static String join(String separator, Iterable<String> strings){
        StringBuilder builder = new StringBuilder();
        for(String s : strings){
            builder.append(s);
            builder.append(separator);
        }
        builder.setLength(builder.length() - separator.length());
        return builder.toString();
    }

    /** Returns the levenshtein distance between two strings. */
    public static int levenshtein(String x, String y){
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for(int i = 0; i <= x.length(); i++){
            for(int j = 0; j <= y.length(); j++){
                if(i == 0){
                    dp[i][j] = j;
                }else if(j == 0){
                    dp[i][j] = i;
                }else{
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1]
                    + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1),
                    dp[i - 1][j] + 1),
                    dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static String animated(float time, int length, float scale, String replacement){
        return new String(new char[Math.abs((int)(time / scale) % length)]).replace("\0", replacement);
    }

    public static String kebabToCamel(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c != '_' && c != '-'){
                if(i != 0 && (s.charAt(i - 1) == '_' || s.charAt(i - 1) == '-')){
                    result.append(Character.toUpperCase(c));
                }else{
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    public static String camelToKebab(String s){
        StringBuilder result = new StringBuilder(s.length() + 1);

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(i > 0 && Character.isUpperCase(s.charAt(i))){
                result.append('-');
            }

            result.append(Character.toLowerCase(c));

        }

        return result.toString();
    }

    /**Converts a snake_case or kebab-case string to Upper Case.
     * For example: "test_string" -> "Test String"*/
    public static String capitalize(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c == '_' || c == '-'){
                result.append(" ");
            }else if(i == 0 || s.charAt(i - 1) == '_' || s.charAt(i - 1) == '-'){
                result.append(Character.toUpperCase(c));
            }else{
                result.append(c);
            }
        }

        return result.toString();
    }

    /** Adds spaces to a camel/pascal case string. */
    public static String insertSpaces(String s){
        StringBuilder result = new StringBuilder(s.length() + 1);

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(i > 0 && Character.isUpperCase(c)){
                result.append(' ');
            }

            result.append(c);
        }

        return result.toString();
    }

    /**Converts a Space Separated string to camelCase.
     * For example: "Camel Case" -> "camelCase"*/
    public static String camelize(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(i == 0){
                result.append(Character.toLowerCase(c));
            }else if(c != ' '){
                result.append(c);
            }

        }

        return result.toString();
    }

    public static boolean canParseInt(String s){
        return parseInt(s) != Integer.MIN_VALUE;
    }

    public static boolean canParsePositiveInt(String s){
        int p = parseInt(s);
        return p >= 0;
    }

    public static int parseInt(String s, int defaultValue){
        return parseInt(s, 10, defaultValue);
    }

    public static int parseInt(String s, int radix, int defaultValue){
        return parseInt(s, radix, defaultValue, 0, s.length());
    }

    public static int parseInt(String s, int radix, int defaultValue, int start, int end){
        boolean negative = false;
        int i = start, len = end - start, limit = -2147483647;
        if(len <= 0){
            return defaultValue;
        }else{
            char firstChar = s.charAt(i);
            if(firstChar < '0'){
                if(firstChar == '-'){
                    negative = true;
                    limit = -2147483648;
                }else if(firstChar != '+'){
                    return defaultValue;
                }

                if(len == 1) return defaultValue;

                ++i;
            }

            int limitForMaxRadix = (-Integer.MAX_VALUE) / 36;
            int limitBeforeMul = limitForMaxRadix;

            int digit, result = 0;
            while(i < end){
                digit = Character.digit(s.charAt(i++), radix);
                if(digit < 0) return defaultValue;
                if(result < limitBeforeMul){
                    if(limitBeforeMul == limitForMaxRadix){
                        limitBeforeMul = limit / radix;

                        if(result < limitBeforeMul){
                            return defaultValue;
                        }
                    }else{
                        return defaultValue;
                    }
                }

                result *= radix;
                if(result < limit + digit){
                    return defaultValue;
                }

                result -= digit;
            }

            return negative ? result : -result;
        }
    }

    public static long parseLong(String s, long defaultValue){
        return parseLong(s, 10, defaultValue);
    }
    public static long parseLong(String s, int radix, long defaultValue){
        return parseLong(s, radix, 0, s.length(), defaultValue);
    }

    public static long parseLong(String s, int radix, int start, int end, long defaultValue){
        boolean negative = false;
        int i = start, len = end - start;
        long limit = -9223372036854775807L;
        if(len <= 0){
            return defaultValue;
        }else{
            char firstChar = s.charAt(i);
            if(firstChar < '0'){
                if(firstChar == '-'){
                    negative = true;
                    limit = -9223372036854775808L;
                }else if(firstChar != '+'){
                    return defaultValue;
                }

                if(len == 1) return defaultValue;

                ++i;
            }

            long result;
            int digit;
            for(result = 0L; i < end; result -= digit){
                digit = Character.digit(s.charAt(i++), radix);
                if(digit < 0){
                    return defaultValue;
                }

                result *= radix;
                if(result < limit + (long)digit){
                    return defaultValue;
                }
            }

            return negative ? result : -result;
        }
    }

    /** Faster double parser that doesn't throw exceptions. */
    public static double parseDouble(String value, double defaultValue){
        int len = value.length();
        if(len == 0) return defaultValue;

        int sign = 1;
        int start = 0, end = len;
        char last = value.charAt(len - 1), first = value.charAt(0);
        if(last == 'F' || last == 'f' || last == '.'){
            end --;
        }
        if(first == '+'){
            start = 1;
        }
        if(first == '-'){
            start = 1;
            sign = -1;
        }

        int dot = -1, e = -1;
        for(int i = start; i < end; i++){
            char c = value.charAt(i);
            if(c == '.') dot = i;
            if(c == 'e' || c == 'E') e = i;
        }

        if(dot != -1 && dot < end){
            //negation as first character
            long whole = start == dot ? 0 : parseLong(value, 10, start, dot, Long.MIN_VALUE);
            if(whole == Long.MIN_VALUE) return defaultValue;
            long dec = parseLong(value, 10, dot + 1, end, Long.MIN_VALUE);
            if(dec < 0) return defaultValue;
            return (whole + Math.copySign(dec / Math.pow(10, (end - dot - 1)), whole)) * sign;
        }

        //check scientific notation
        if(e != -1){
            long whole = parseLong(value, 10, start, e, Long.MIN_VALUE);
            if(whole == Long.MIN_VALUE) return defaultValue;
            long power = parseLong(value, 10, e + 1, end, Long.MIN_VALUE);
            if(power == Long.MIN_VALUE) return defaultValue;
            return whole * Math.pow(10, power) * sign;
        }

        //parse as standard integer
        long out = parseLong(value, 10, start, end, Long.MIN_VALUE);
        return out == Long.MIN_VALUE ? defaultValue : out*sign;
    }

    /** Returns Integer.MIN_VALUE if parsing failed. */
    public static int parseInt(String s){
        return parseInt(s, Integer.MIN_VALUE);
    }

    /** Parse a String and converts the time as seconds - Supports: w(eeks), d(ays), h(ours), m(inutes) and s(econds) */
    public static long parseTime(String time) {
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

    /** Parse a long and converts the seconds as a time String */
    public static String convertSecond(long seconds) {
        String time = "";

        // if (time == "inf") return Long.MAX_VALUE; 

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

    public static boolean canParseFloat(String s){
        try{
            Float.parseFloat(s);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static boolean canParsePositiveFloat(String s){
        try{
            return Float.parseFloat(s) >= 0;
        }catch(Exception e){
            return false;
        }
    }

    /** Returns Float.NEGATIVE_INFINITY if parsing failed. */
    public static float parseFloat(String s){
        return parseFloat(s, Float.MIN_VALUE);
    }

    public static float parseFloat(String s, float defaultValue){
        try{
            return Float.parseFloat(s);
        }catch(Exception e){
            return defaultValue;
        }
    }

    public static String autoFixed(float value, int max){
        int precision = Math.abs((int)(value + 0.0001f) - value) <= 0.0001f ? 0 :
                Math.abs((int)(value * 10 + 0.0001f) - value * 10) <= 0.0001f ? 1 : 2;
        return fixed(value, Math.min(precision, max));
    }

    public static String fixed(float d, int decimalPlaces){
        return fixedBuilder(d, decimalPlaces).toString();
    }

    public static StringBuilder fixedBuilder(float d, int decimalPlaces){
        if(decimalPlaces < 0 || decimalPlaces > 8){
            throw new IllegalArgumentException("Unsupported number of " + "decimal places: " + decimalPlaces);
        }
        boolean negative = d < 0;
        d = Math.abs(d);
        StringBuilder dec = tmp2;
        dec.setLength(0);
        dec.append((int)(float)(d * Math.pow(10, decimalPlaces) + 0.0001f));

        int len = dec.length();
        int decimalPosition = len - decimalPlaces;
        StringBuilder result = tmp1;
        result.setLength(0);
        if(negative) result.append('-');
        if(decimalPlaces == 0){
            if(negative) dec.insert(0, '-');
            return dec;
        }else if(decimalPosition > 0){
            // Insert a dot in the right place
            result.append(dec, 0, decimalPosition);
            result.append(".");
            result.append(dec, decimalPosition, dec.length());
        }else{
            result.append("0.");
            // Insert leading zeroes into the decimal part
            while(decimalPosition++ < 0){
                result.append("0");
            }
            result.append(dec);
        }
        return result;
    }

    public static String formatMillis(long val){
        StringBuilder buf = new StringBuilder(20);
        String sgn = "";

        if(val < 0) sgn = "-";
        val = Math.abs(val);

        append(buf, sgn, 0, (val / 3600000));
        val %= 3600000;
        append(buf, ":", 2, (val / 60000));
        val %= 60000;
        append(buf, ":", 2, (val / 1000));
        return buf.toString();
    }

    private static void append(StringBuilder tgt, String pfx, int dgt, long val){
        tgt.append(pfx);
        if(dgt > 1){
            int pad = (dgt - 1);
            for(long xa = val; xa > 9 && pad > 0; xa /= 10) pad--;
            for(int xa = 0; xa < pad; xa++) tgt.append('0');
        }
        tgt.append(val);
    }

    /** Replaces all instances of {@code find} with {@code replace}. */
    public static StringBuilder replace(StringBuilder builder, String find, String replace){
        int findLength = find.length(), replaceLength = replace.length();
        int index = 0;
        while(true){
            index = builder.indexOf(find, index);
            if(index == -1) break;
            builder.replace(index, index + findLength, replace);
            index += replaceLength;
        }
        return builder;
    }

    /** Replaces all instances of {@code find} with {@code replace}. */
    public static StringBuilder replace(StringBuilder builder, char find, String replace) {
        int replaceLength = replace.length();
        int index = 0;
        while(true){
            while(true){
                if(index == builder.length()) return builder;
                if(builder.charAt(index) == find) break;
                index++;
            }
            builder.replace(index, index + 1, replace);
            index += replaceLength;
        }
    }
    
    /** Taken from the {@link String#repeat(int)} method of JDK 11 */
    public static String repeat(String str, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count is negative: " + count);
        }
        if (count == 1) {
            return str;
        }
        final byte[] value = str.getBytes();
        final int len = value.length;
        if (len == 0 || count == 0) {
            return "";
        }
        if (Integer.MAX_VALUE / count < len) {
            throw new OutOfMemoryError("Required length exceeds implementation limit");
        }
        if (len == 1) {
            final byte[] single = new byte[count];
            java.util.Arrays.fill(single, value[0]);
            return new String(single);
        }
        final int limit = len * count;
        final byte[] multiple = new byte[limit];
        System.arraycopy(value, 0, multiple, 0, len);
        int copied = len;
        for (; copied < limit - copied; copied <<= 1) {
            System.arraycopy(multiple, 0, multiple, copied, copied);
        }
        System.arraycopy(multiple, 0, multiple, copied, limit - copied);
        return new String(multiple);
    }
    
    public static String rJust(String str, int length) { return rJust(str, length, " "); }
    /** Justify string to the right. E.g. "&emsp; right" */
    public static String rJust(String str, int length, String filler) {
      int sSize = str.length(), fSize = filler.length();
      
      if (fSize == 0 || sSize >= length) return str; 
      if (fSize == 1) return repeat(filler, length-sSize)+str;   
      int add = length-sSize;
      return repeat(filler, add/fSize)+filler.substring(0, add%fSize)+str;
    }
    public static Seq<String> rJust(Seq<String> list, int length) { return rJust(list, length, " "); }
    public static Seq<String> rJust(Seq<String> list, int length, String filler) {
      return list.map(str -> rJust(str, length, filler));
    }

    public static String lJust(String str, int length) { return lJust(str, length, " "); }
    /** Justify string to the left. E.g. "left &emsp;" */
    public static String lJust(String str, int length, String filler) {
      int sSize = str.length(), fSize = filler.length();
      
      if (fSize == 0 || sSize >= length) return str;
      if (fSize == 1) return str+repeat(filler, length-sSize);
      int add = length-sSize;
      return str+repeat(filler, add/fSize)+filler.substring(0, add%fSize);
    }
    public static Seq<String> lJust(Seq<String> list, int length) { return lJust(list, length, " "); }
    public static Seq<String> lJust(Seq<String> list, int length, String filler) {
      return list.map(str -> lJust(str, length, filler));
    }
    
    public static String cJust(String str, int length) { return cJust(str, length, " "); }
    /** Justify string to the center. E.g. "&emsp; center &emsp;". */
    public static String cJust(String str, int length, String filler) {
      int sSize = str.length(), fSize = filler.length();
      
      if (fSize == 0 || sSize >= length) return str;
      int add = length-sSize, left = add/2, right = add-add/2;
      if (fSize == 1) return repeat(filler, left)+str+repeat(filler, right);
      return repeat(filler, left/fSize)+filler.substring(0, left%fSize)+str+
             repeat(filler, right/fSize)+filler.substring(0, right%fSize);
    }
    public static Seq<String> cJust(Seq<String> list, int length) { return cJust(list, length, " "); }
    public static Seq<String> cJust(Seq<String> list, int length, String filler) {
      return list.map(str -> cJust(str, length, filler));
    }

    public static String sJust(String left, String right, int length) { return sJust(left, right, length, " "); }
    /** Justify string to the sides. E.g. "left &emsp; right" */
    public static String sJust(String left, String right, int length, String filler) {
      int fSize = filler.length(), lSize = left.length(), rSize = right.length();
      
      if (fSize == 0 || lSize+rSize >= length) return left+right; 
      int add = length-lSize-rSize;
      if (fSize == 1) return left+repeat(filler, add)+right;
      return left+repeat(filler, add/fSize)+filler.substring(0, add%fSize)+right;
    }
    public static Seq<String> sJust(Seq<String> left, Seq<String> right, int length) { return sJust(left, right, length, " "); }
    public static Seq<String> sJust(Seq<String> left, Seq<String> right, int length, String filler) {
      Seq<String> arr = new Seq<>(Integer.max(left.size, right.size));
      int i = 0;

      for (; i<Integer.min(left.size, right.size); i++) arr.add(sJust(left.get(i), right.get(i), length, filler));
      // Fill the rest
      for (; i<left.size; i++) arr.add(lJust(left.get(i), length, filler));
      for (; i<right.size; i++) arr.add(rJust(right.get(i), length, filler));
      
      return arr;
    }
    
    public static Seq<String> tableify(Seq<String> lines, int width) {
      return tableify(lines, width, Strings::lJust);
    }
    /** 
     * Create a table with given {@code lines} 
     * and automatic columns number calculated with the table's {@code width}.
     */
    public static Seq<String> tableify(Seq<String> lines, int width, 
                                       Func2<String, Integer, String> justifier) {
      int spacing = 2, // Additional spacing between columns
          columns = Math.max(1, width / (bestLength(lines) + 2)); // Estimate the columns
      Seq<String> result = new Seq<>(lines.size / columns + 1);
      int[] bests = new int[columns];
      StringBuilder builder = new StringBuilder();
      
      // Calculate the best length for each columns
      for (int i=0, c=0, s=0; i<lines.size; i++) {
        s = lines.get(i).length();
        c = i % columns;
        if (s > bests[c]) bests[c] = s;
      }
      
      // Now justify lines
      for (int i=0, c; i<lines.size;) { 
        for (c=0; c<columns && i<lines.size; c++, i++) 
          builder.append(justifier.get(lines.get(i), bests[c]+spacing));
        
        result.add(builder.toString());
        builder.setLength(0);
      }
      
      return result;
    }

    public static <T> int best(Iterable<T> list, Intf<T> intifier) {
      int best = 0;
      
      for (T i : list) {
        int s = intifier.get(i);
        if (s > best) best = s;
      }
      
      return best;
    }
    
    public static <T> int best(T[] list, Intf<T> intifier) {
      int best = 0;
      
      for (T i : list) {
        int s = intifier.get(i);
        if (s > best) best = s;
      }
      
      return best;
    }
    
    public static int bestLength(Iterable<? extends String> list) {
      return best(list, str -> str.length());
    }
    
    public static int bestLength(String... list) {
      return best(list, str -> str.length());
    }

    public static long bits2int(boolean... list) {
      int out = 0;

      for (int i=0; i<list.length; i++) {
        out |= list[i] ? 1 : 0;
        out <<= 1;
      }
      
      return out >> 1;
    }

    public static boolean[] int2bits(long number) { return int2bits(number, 0); }
    public static boolean[] int2bits(long number, int bits) {
      // Check value because 0 have a negative size  
      if (number == 0) return new boolean[bits == 0 ? 1 : bits];
        
      int size = bits < 1 ? (int) (Math.log(number)/Math.log(2)+1) : bits;
      boolean[] out = new boolean[size];
      
      while (size-- > 0) {
        out[size] = (number & 1) != 0;
        number >>= 1;
      }

      return out;
    }
    
    /** @return whether the specified string mean true */
    public static boolean isTrue(String str) {
      switch (str.toLowerCase()) {
        case "1": case "true": case "on": 
        case "enable": case "activate":
                 return true;
        default: return false;
      }
    }
    
    /** @return whether the specified string mean false */
    public static boolean isFalse(String str) {
      switch (str.toLowerCase()) {
        case "0": case "false": case "off": 
        case "disable": case "desactivate":
                 return true;
        default: return false;
      }
    }
    
    /** 
     * @return whether {@code newVersion} is greater than {@code currentVersion}, e.g. "v146" > "124.1"
     * @apiNote can handle multiple dots in the version, and it's very fast because it only does one iteration.
     */
    public static boolean isVersionAtLeast(String currentVersion, String newVersion) {
      int last1 = currentVersion.startsWith("v") ? 1 : 0, 
          last2 = newVersion.startsWith("v") ? 1 : 0, 
          len1 = currentVersion.length(), 
          len2 = newVersion.length(),
          dot1 = 0, dot2 = 0, 
          p1 = 0, p2 = 0;
      
      while ((dot1 != -1  && dot2 != -1) && (last1 < len1 && last2 < len2)) {
        dot1 = currentVersion.indexOf('.', last1);
        dot2 = newVersion.indexOf('.', last2);
        if (dot1 == -1) dot1 = len1;
        if (dot2 == -1) dot2 = len2;
        
        p1 = parseInt(currentVersion, 10, 0, last1, dot1);
        p2 = parseInt(newVersion, 10, 0, last2, dot2);
        last1 = dot1+1;
        last2 = dot2+1;

        if (p1 != p2) return p2 > p1;
      }

      // Continue iteration on newVersion to see if it's just leading zeros.
      while (dot2 != -1 && last2 < len2) {
        dot2 = newVersion.indexOf('.', last2);
        if (dot2 == -1) dot2 = len2;
        
        p2 = parseInt(newVersion, 10, 0, last2, dot2);
        last2 = dot2+1;
        
        if (p2 > 0) return true;
      }
      
      return false;
    }
    
    /** Modified version of {@link arc.util.I18NBundle#toFileHandle()} */
    public static String locale2String(Locale locale) {
      if (locale != null && !locale.equals(Locale.ROOT)) {
        String l = convertOldISOCodes(locale.getLanguage());
        String c = locale.getCountry();
        String v = locale.getVariant();
        boolean el = l.isEmpty();
        boolean ec = c.isEmpty();
        boolean ev = v.isEmpty();
        
        if (!(el && ec && ev)) {
          if (!ev) return l + '_' + c + '_' + v;
          if (!ec) return l + '_' + c;
          return l;      
        }
      }
      
      return "";
    }

    public static Locale string2Locale(String tag) {
      if (tag == null) return null;
      if (tag.isEmpty()) return Locale.ROOT;

      String[] codes = tag.replace('-', '_').split("_");
      return codes.length == 1 ? new Locale(codes[0]) : 
             codes.length == 2 ? new Locale(codes[0], codes[1]) :
                                 new Locale(codes[0], codes[1], codes[2]);
    }
    
    /** Re-implementatio of sun.util.locale.BaseLocale#convertOldISOCodes(String) */
    public static String convertOldISOCodes(String language) {
      switch (language) {
        case "iw": return "he";
        case "in": return "id";
        case "ji": return "yi";
        default: return language;
      }
    }
    
    /** Convert the object into a string. If the string is empty, return double-qotes (""). */
    public static String objToStr(Object o) {
      String str = String.valueOf(o);
      if (str.isEmpty()) str = "\"\"";
      return str;
    }
    
    public static <T> String listToSentence(Iterable<T> list) { return listToSentence(list, String::valueOf); }
    /** Convert a list to a human readable sentence. E.g. [1, 2, 3, 4, 5] -> "1, 2, 3, 4 and 5" */
    public static <T> String listToSentence(Iterable<T> list, Func<T, String> stringifier) {
      java.util.Iterator<T> iter = list.iterator();
      if (!iter.hasNext()) return "";
      
      StringBuilder builder = new StringBuilder(stringifier.get(iter.next()));
      
      while (iter.hasNext()) {
        T tmp = iter.next();
        builder.append(iter.hasNext() ? ", " : " and ");
        builder.append(stringifier.get(tmp));
      }
      
      return builder.toString();
    }
    
    /** Simple method to validate a path, is by using {@link java.nio.file.Paths#get(String, String...)} */
    public static boolean isValidPath(String path) {
      if (path == null || path.isEmpty()) return false;

      try { new java.io.File(path).getCanonicalPath(); }
      catch (Exception e) { return false; }
      return true;
    }
}
