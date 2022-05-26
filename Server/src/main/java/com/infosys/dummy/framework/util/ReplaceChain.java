package com.infosys.dummy.framework.util;

import java.util.regex.Pattern;

/**
 * Chains find/replace calls. For example:
 * 
 * <pre>
 * // Replace url-encoded braces with normal braces
 * private static final Pattern OPEN_BRACE_PATTERN = Pattern.compile("%7B");
 * private static final Pattern CLOSE_BRACE_PATTERN = Pattern.compile("%7D");
 * 
 * private static final String charFilter(final String request) {
 *     return ReplaceChain.build(request)
 *         .replaceAll(OPEN_BRACE_PATTERN, "{")
 *         .replaceAll(CLOSE_BRACE_PATTERN, "}")
 *         .toString();
 *   }
 * }
 * </pre>
 * 
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 9/20/20
 * <p>Time: 2:51 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public final class ReplaceChain {
  private String currentText;
  
  private ReplaceChain(String source) {
    currentText = source;
  }

  public static ReplaceChain build(String text) {
    return new ReplaceChain(text);
  }

  public ReplaceChain replaceAll(Pattern match, String replacement) {
    currentText = match.matcher(currentText).replaceAll(replacement);
    return this;
  }

  @Override
  public String toString() {
    return currentText;
  }
}
