package com.infosys.dummy.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 3/1/21
 * <p>Time: 12:00 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum WebUtils {
  ;

  private static final Logger log = LoggerFactory.getLogger(WebUtils.class);

  public static String getUriTail(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri == null) { // This may happen in unit tests, but not production. This is only used for logging anyway
      return "";
    }
    String path = request.getContextPath();
    log.debug("  URI: {}", uri);
    log.debug("  path: {} (length: {})", path, path.length());
    if (uri.startsWith(path)) {
      if (log.isDebugEnabled()) {
        log.debug("  Path stripped to {}", uri.substring(path.length()));
      }
      return uri.substring(path.length());
    }
    return uri;
  }

}
