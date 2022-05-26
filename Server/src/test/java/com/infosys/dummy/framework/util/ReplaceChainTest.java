package com.infosys.dummy.framework.util;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/9/21
 * <p>Time: 12:28 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class ReplaceChainTest {

  @Test
  public void testReplacement() {
    String[] cases = {
        "/abc/def", "/abc/def",
        "/abc/%7Bdef%7D", "/abc/{def}",
        "/abc/%7Bdef%7D/%7Bghi%7D/", "/abc/{def}/{ghi}/",
        "%7Babc%7D", "{abc}",
        "%7B", "{",
        "%7", "%7",
    };

    Pattern OPEN_BRACE_MATCH = Pattern.compile("%7B");
    Pattern CLOSE_BRACE_MATCH = Pattern.compile("%7D");

    for (int i = 0; i < cases.length; i += 2) {
      String start = cases[i];
      String expected = cases[i + 1];
      String result = ReplaceChain.build(start)
          .replaceAll(OPEN_BRACE_MATCH, "{")
          .replaceAll(CLOSE_BRACE_MATCH, "}")
          .toString();
      assertEquals(expected, result);
    }
  }

  @Test
  public void testIgnoreCase() {
    String[] cases = {
        "/abc/def", "/abc/def",
        "/abc/%7Bdef%7D", "/abc/{def}",
        "/abc/%7Bdef%7D/%7Bghi%7D/", "/abc/{def}/{ghi}/",
        "%7Babc%7D", "{abc}",
        "%7B", "{",
        "%7", "%7",

        "/abc/%7bdef%7d", "/abc/{def}",
        "/abc/%7bdef%7d/%7bghi%7d/", "/abc/{def}/{ghi}/",
        "%7babc%7d", "{abc}",
        "%7b", "{",
        "%7", "%7",
    };

    Pattern OPEN_BRACE_MATCH = Pattern.compile("%7[B|b]");
    Pattern CLOSE_BRACE_MATCH = Pattern.compile("%7[D|d]");

    for (int i = 0; i < cases.length; i += 2) {
      String start = cases[i];
      String expected = cases[i + 1];
      String result = ReplaceChain.build(start)
          .replaceAll(OPEN_BRACE_MATCH, "{")
          .replaceAll(CLOSE_BRACE_MATCH, "}")
          .toString();
      assertEquals(expected, result);
    }
  }
}