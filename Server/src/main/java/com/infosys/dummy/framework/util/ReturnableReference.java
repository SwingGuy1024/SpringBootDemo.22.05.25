package com.infosys.dummy.framework.util;

/**
 * Useful when a method needs to return multiple values. This is intended to be used as a parameter
 * to a method. Its use should be avoided. It's a better practice to define a single object and just return that. 
 * But that's not always practical, such as when you need to return a standard object like a Date, along with 
 * something else This class should be used when it's less work than it would be to create a more complete 
 * single class to return.
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/21/18
 * <p>Time: 5:22 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class ReturnableReference<T> {
  private T value = null;

  public boolean hasValue() { return value != null; }
  public void setValue(T newValue) { value = newValue; }
  public T getValue() { return value; }
}
