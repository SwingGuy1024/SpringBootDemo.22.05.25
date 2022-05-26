package com.infosys.dummy.exception;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/28/21
 * <p>Time: 10:05 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum ExceptionUtils {
  ;

  public static String getLastMessage(Throwable ex) {
    Throwable priorEx;
    String message;
    //noinspection ObjectEquality
    do {
      message = ex.getLocalizedMessage();
      priorEx = ex;
      ex = ex.getCause();
    } while ((ex != null) && (ex != priorEx));
    return message;
  }

}
