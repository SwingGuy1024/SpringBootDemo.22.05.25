package com.infosys.dummy.server;

import com.infosys.dummy.framework.util.ReplaceChain;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.regex.Pattern;

@Component
public class ApiOriginFilter implements javax.servlet.Filter {
  private static final Logger log = LoggerFactory.getLogger(ApiOriginFilter.class);

  public ApiOriginFilter() {
    log.trace("Instantiating ApiOriginFilter");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    if (log.isDebugEnabled()) {
      if (request instanceof RequestFacade) {
        RequestFacade facade = (RequestFacade) request;
        String method = facade.getMethod();
        log.debug("RF Request URI: {} {}", method, charFilter(facade.getRequestURI()));
      } else if (request instanceof HttpServletRequestWrapper) {
        HttpServletRequest hsr = (HttpServletRequest) ((HttpServletRequestWrapper)request).getRequest();
        log.debug("HW Request URI: {} {}", hsr.getMethod(), hsr.getRequestURI());
      } else if ((request instanceof ServletRequestWrapper)
              && (((ServletRequestWrapper) request).getRequest() instanceof HttpServletRequest)) {
        final ServletRequest wrapped = ((ServletRequestWrapper) request).getRequest();
        final HttpServletRequest hsr = (HttpServletRequest) wrapped;
        String uri = hsr.getRequestURI();
        log.debug("SW Request URI: {} {}", hsr.getMethod(), charFilter(uri));
      } else {
        log.debug("Request URI:    ??? {}", request.getClass());
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("Request: class: {}", request.getClass());
    }

    httpResponse.addHeader("Access-Control-Allow-Origin", "*");
    httpResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
    httpResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig filterConfig) { // normally throws ServletException
  }

  private static final Pattern OPEN_BRACE_PATTERN = Pattern.compile("%7B");
  private static final Pattern CLOSE_BRACE_PATTERN = Pattern.compile("%7D");

  /**
   *  Replace "%7B" and "%7D" with curly braces in url paths
   * @param request The request string
   * @return the corrected String
   */
  private static String charFilter(final String request) {
    return ReplaceChain.build(request)
        .replaceAll(OPEN_BRACE_PATTERN, "{")
        .replaceAll(CLOSE_BRACE_PATTERN, "}")
        .toString();
  }
}
