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

package joseta.utils.json;


/** Build a {@link JsonValue} object */
public class JsonBuilder {
  private JsonValue base, current, last;
  private String name;

  public JsonValue getJson() {
    if (current != null && base != current)
      throw new IllegalStateException("Builder must be closed before getting the result");
    return base;
  }

  public JsonBuilder name(String name) {
    if (current == null || current.isArray())
      throw new IllegalStateException("Current item must be an object.");
      
    this.name = name;
    return this;
  }

  public JsonBuilder value(Object object) {
    requireName();
    if (current == null) throw new IllegalStateException("No current object/array.");
    JsonValue jval;
    
    if (object == null) jval = new JsonValue(JsonValue.ValueType.nullValue);
    else if (object instanceof Number) {
        Number number = (Number)object;
        if (object instanceof Byte) jval = new JsonValue(number.byteValue());
        else if (object instanceof Short) jval = new JsonValue(number.shortValue());
        else if (object instanceof Integer) jval = new JsonValue(number.intValue());
        else if (object instanceof Long) jval = new JsonValue(number.longValue());
        else if (object instanceof Float) jval = new JsonValue(number.floatValue());
        else if (object instanceof Double) jval = new JsonValue(number.doubleValue());
        else throw new IllegalArgumentException("Unknown number object type.");
    } else if (object instanceof CharSequence || 
               object instanceof Character) jval = new JsonValue(object.toString());
    else if (object instanceof Boolean) jval = new JsonValue((boolean)object);
    else if (object instanceof JsonValue) jval = (JsonValue) object;
    else throw new IllegalArgumentException("Unknown object type.");
    
    addValue(jval);
    return this;
  }

  public JsonBuilder object() {
    requireName();
    newChild(false);
    return this;
  }

  public JsonBuilder array() {
    requireName();
    newChild(true);
    return this;
  }
  
  private void addValue(JsonValue value) {
    if (current.child == null || last == null) {
      current.addChild(name, value);
      last = current.child;
      while (last.next != null) last = last.next;
    
    } else {
      if (name != null) value.name = new String(name); // idk why this works
      value.parent = current;
      last.next = value;
      last = last.next;
    }
    
    name = null;
  }
  
  private void newChild(boolean array) {
    JsonValue newValue = new JsonValue(array ? JsonValue.ValueType.array : JsonValue.ValueType.object);
    JsonValue old = current;
    
    if (current == null) current = newValue;
    if (base == null) base = current;
    if (old != null) {
      addValue(newValue);
      current = newValue;
    }
    last = null;
  }
  
  
  private void requireName() {
    if (current != null && !current.isArray() && name == null)
      throw new IllegalStateException("Name must be set.");
  }
  
  public JsonBuilder object(String name) {
      return name(name).object();
  }

  public JsonBuilder array(String name) {
      return name(name).array();
  }

  public JsonBuilder set(String name, Object value) {
      return name(name).value(value);
  }

  public JsonBuilder pop() {
    if (name != null) 
      throw new IllegalStateException("Expected an object, array or value, since a name was set.");
    last = current;
    if (last != null) {
      while (last.next != null) last = last.next;
      current = current.parent;      
    }
    return this;
  }

  public void close() {
    while (current != null && base != current) 
      pop();
  }

}
