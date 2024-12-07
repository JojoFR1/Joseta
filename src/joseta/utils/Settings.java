/*
 * This file is part of Anti-VPN-Service (AVS). The plugin securing your server against VPNs.
 *
 * MIT License
 *
 * Copyright (c) 2024 Xpdustry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package joseta.utils;

import java.io.*;

import joseta.utils.struct.*;
import joseta.utils.json.*;


/**
 * Taken from 
 * <a href="https://github.com/xpdustry/Anti-VPN-Service/blob/master/src/main/java/com/xpdustry/avs/util/DynamicSettings.java">
 * Anti-VPN-Service's DynamicSettings</a> without the autosave.
 * 
 * @author Zackari LOHR
 */
public class Settings {
  protected static Seq<Settings> files = new Seq<>();

  protected final File file;
  protected final ObjectMap<String, Object> values = new ObjectMap<>();
  protected boolean modified;

  public Settings(File file) {
    this.file = file;
    files.add(this);
  }

  public boolean modified(){
      return modified;
  }
  
  public synchronized static boolean needGlobalSave() {
    return files.contains(f -> f.modified());
  }
  
  public synchronized static void globalSave() {
    // Only save if changes are made
    if (needGlobalSave()) 
      files.each(s -> s.save());    
  }

  /** Loads all values. */
  public synchronized void load(){
      //doesn't load the file and create an empty one
      if(!getFile().exists()){
          getFile().getParentFile().mkdirs();
          try { getFile().createNewFile(); } 
          catch (IOException e) {}
          return;
      }
    
      try{
          clear(); // remove all existing values first
          loadValues(getFile());
      }catch(Throwable error){
          throw new RuntimeException(error);
      }
  }

  /** Saves all values. */
  public synchronized void save(){
      if (!modified()) return;
      
      try{
          saveValues(getFile());
          modified = false;
      }catch(Throwable error){
          throw new RuntimeException(error);
      }
  }

  public synchronized void loadValues(File file) throws IOException{
      try{      
          file.getParentFile().mkdirs();
          file.createNewFile();
          JsonValue content = new JsonReader().parse(new BufferedInputStream(new FileInputStream(file)));
          if (content == null) return; // Probably empty file
          
          for(JsonValue child = content.child; child != null; child = child.next){
              if (child.isBoolean()) values.put(child.name, child.asBoolean());
              else if (child.isLong()) values.put(child.name, child.asLong());
              else if (child.isDouble()) values.put(child.name, child.asDouble());
              else if (child.isString()) values.put(child.name, child.asString());
              else if (child.isNull()) values.put(child.name, null);
              // Check if it's a "bytes json object" (explains in {@link #saveValues()})
              else if (child.isObject() && child.child != null && child.child.next == null &&
                       child.child.name != null && child.child.name.equals("bytes") && child.child.isString())
                  values.put(child.name, Base64Coder.decode(child.child.asString()));
              // All sub-objects or arrays, are count as json values.
              else values.put(child.name, child);
          }    
          
      }catch(Throwable e){
          throw new IOException("Error while reading file: " + file, e);
      }
  }

  public synchronized void saveValues(File file) throws IOException{
      try{
          JsonValue content = new JsonValue(JsonValue.ValueType.object);
          
          for(ObjectMap.Entry<String, Object> entry : values.entries()){
              Object value = entry.value;
              JsonValue jvalue;
        
              if (value instanceof Boolean) jvalue = new JsonValue((Boolean) value);
              else if (value instanceof Long) jvalue = new JsonValue((Long) value);
              else if (value instanceof Double) jvalue = new JsonValue((Double) value);
              //else if (value instanceof Double) jvalue = new JsonValue((Double) value);
              else if (value instanceof String) jvalue = new JsonValue((String) value);
              else if (value instanceof byte[]) {
                  /** 
                   * There is no byte array in json, so it will be converted to a json object with a 
                   * key "bytes" and base64 coded value.
                   */
                  jvalue = new JsonValue(JsonValue.ValueType.object);
                  jvalue.addChild("bytes", new JsonValue(new String(Base64Coder.encode((byte[]) value))));
              }
              else if (value instanceof JsonValue) {
                  jvalue = (JsonValue) value;
                  // Because when saving again without any modifications, this can create a recursion
                  jvalue.next = jvalue.prev = jvalue.parent = null;
              }
              else throw new IOException("Unknown value type: " + value.getClass().getName());
              
              content.addChild(entry.key, jvalue);
          }
          
          file.getParentFile().mkdirs();
          file.createNewFile();
          OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
          content.prettyPrint(OutputType.json, out);
          try { out.close(); }
          catch (Throwable ignored) {}     
        
      }catch(Throwable e){
          throw new IOException("Error while writing file: " + file, e);
      }
  }

  /** Return whether the file exists or not. */
  public boolean fileExists() {
    return file.exists();
  }
  
  /** Returns the file used for writing settings to. */
  public File getFile(){
      return file;
  }

  /** Clears all preference values. */
  public synchronized void clear(){
      values.clear();
      modified = true;
  }

  public synchronized boolean has(String name){
      return values.containsKey(name);
  }
  
  public synchronized Object get(String name, Object def){
      return values.containsKey(name) ? values.get(name) : def;
  }
  
  /** Same as {@link #get(String, Object)}, but put {@code def} if the key is not found */
  public synchronized Object getOrPut(String name, Object def){
      Object o = get(name, def);
      if (o == def) put(name, def);
      return o;
  }


  public boolean isModified(){
      return modified;
  }

  public double getDouble(String name, double def){
      return (double)getOrPut(name, def);
  }

  public long getLong(String name, long def){
      return (long)getOrPut(name, def);
  }

  public boolean getBool(String name, boolean def){
      return (boolean)getOrPut(name, def);
  }

  public byte[] getBytes(String name, byte[] def){
      return (byte[])getOrPut(name, def);
  }

  public String getString(String name, String def){
      return (String)getOrPut(name, def);
  }

  public JsonValue getJson(String name, JsonValue def){
      return (JsonValue)getOrPut(name, def);
  }
  
  public double getDouble(String name){
      return (double)get(name, 0d);
  }
  
  public Long getLong(String name){
      return (long)get(name, 0l);
  }

  public boolean getBool(String name){
      return (boolean)get(name, false);
  }

  /** Runs the specified code once, and never again. */
  public void getBoolOnce(String name, Runnable run){
      if(!getBool(name, false)){
          run.run();
          put(name, true);
      }
  }

  /** Returns true once, and never again. */
  public boolean getBoolOnce(String name){
      boolean val = getBool(name, false);
      put(name, true);
      return val;
  }

  public byte[] getBytes(String name){
      return (byte[])get(name, null);
  }

  public String getString(String name){
      return (String)get(name, null);
  }
  
  public JsonValue getJson(String name){
    return (JsonValue)get(name, null);
  }

  public void putAll(ObjectMap<String, Object> map){
      map.each((k, v) -> put(k, v));
  }

  /** Stores an object in the preference map. */
  public synchronized void put(String name, Object object){
      if(object instanceof Double || object instanceof Long || object instanceof Boolean ||
         object instanceof String || object instanceof byte[] || object instanceof JsonValue){
          values.put(name, object);
          modified = true;
      }else{
          throw new IllegalArgumentException("Invalid object stored: " + (object == null ? null : object.getClass()) + ".");
      }
  }

  public synchronized void remove(String name){
      values.remove(name);
      modified = true;
  }

  public synchronized Iterable<String> keys(){
      return values.keys();
  }

  public synchronized int keySize(){
      return values.size;
  }
}
