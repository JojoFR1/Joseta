package joseta.utils.json;

import joseta.utils.Strings;

import java.util.regex.Pattern;

/**
 * @author Nathan Sweet
 */
public enum OutputType {
  /** Normal JSON, with all its double quotes. */
  json,
  /** Like JSON, but names are only double quoted if necessary. */
  javascript,
  /**
   * Like JSON, but:
   * <ul>
   * <li>Names only require double quotes if they start with <code>space</code> or any of <code>":,}/</code> or they contain
   * <code>//</code> or <code>/*</code> or <code>:</code>.
   * <li>Values only require double quotes if they start with <code>space</code> or any of <code>":,{[]/</code> or they
   * contain <code>//</code> or <code>/*</code> or any of <code>}],</code> or they are equal to <code>true</code>,
   * <code>false</code> , or <code>null</code>.
   * <li>Newlines are treated as commas, making commas optional in many cases.
   * <li>C style comments may be used: <code>//...</code> or <code>/*...*<b></b>/</code>
   * </ul>
   */
  minimal;

  private static Pattern javascriptPattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z_$0-9]*$");
  private static Pattern minimalNamePattern = Pattern.compile("^[^\":,}/ ][^:]*$");
  private static Pattern minimalValuePattern = Pattern.compile("^[^\":,{\\[\\]/ ][^}\\],]*$");

  public String quoteValue(Object value){
      if(value == null) return "null";
      String string = value.toString();
      if(value instanceof Number || value instanceof Boolean) return string;
      StringBuilder buffer = new StringBuilder(string);
      Strings.replace(buffer, '\\', "\\\\");
      Strings.replace(buffer, '\r', "\\r");
      Strings.replace(buffer, '\n', "\\n");
      Strings.replace(buffer, '\t', "\\t");
      if(this == OutputType.minimal && !string.equals("true") && !string.equals("false") && !string.equals("null")
      && !string.contains("//") && !string.contains("/*")){
          int length = buffer.length();
          if(length > 0 && buffer.charAt(length - 1) != ' ' && minimalValuePattern.matcher(buffer).matches())
              return buffer.toString();
      }
      Strings.replace(buffer, '"', "\\\"");
      return '"' + buffer.toString() + '"';
  }

  public String quoteName(String value){
      StringBuilder buffer = new StringBuilder(value);
      Strings.replace(buffer, '\\', "\\\\");
      Strings.replace(buffer, '\r', "\\r");
      Strings.replace(buffer, '\n', "\\n");
      Strings.replace(buffer, '\t', "\\t");
      switch(this){
          case minimal:
              if(!value.contains("//") && !value.contains("/*") && minimalNamePattern.matcher(buffer).matches())
                  return buffer.toString();
          case javascript:
              if(javascriptPattern.matcher(buffer).matches()) 
                  return buffer.toString();
          default:
              Strings.replace(buffer, '"', "\\\"");
              return '"' + buffer.toString() + '"';
      }
  }
}
