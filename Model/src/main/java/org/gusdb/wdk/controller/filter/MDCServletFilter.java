package org.gusdb.wdk.controller.filter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gusdb.fgputil.logging.ThreadLocalLoggingVars;

/**
 * A filter that adds (and removes) MDC variables for log4j.  Added to MDC by
 * this filter are:
 * 
 *   ipAddress: IP address of the sender of the current request
 *   requestedDomain: domain at which this request was received
 *   sessionId: Java session ID of the current request
 * 
 * To use this filter, add the following to web.xml:
 * 
 *   <filter>
 *     <filter-name>MDCServletFilter</filter-name>
 *     <filter-class>org.gusdb.wdk.controller.filter.MDCServletFilter</filter-class>
 *   </filter>
 *   <filter-mapping>
 *     <filter-name>MDCServletFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 *   </filter-mapping>
 * 
 * Then use the above properties as %X{param} in your log4j configuration.  For
 * example, in log4j.properties:
 * 
 *   log4j.appender.R.layout.ConversionPattern=%X{ipAddress}
 * 
 * @author mheiges
 */
public class MDCServletFilter implements Filter {
  
  private static final AtomicInteger requestId = new AtomicInteger(1);

  @Override
  public void init(FilterConfig filterConfig) { }

  @Override
  public void destroy() { }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    try {

      ThreadLocalLoggingVars.setRequestStartTime(System.currentTimeMillis());
      ThreadLocalLoggingVars.setIpAddress(request.getRemoteAddr());
      ThreadLocalLoggingVars.setRequestedDomain(request.getServerName());
      ThreadLocalLoggingVars.setRequestId(String.valueOf(requestId.getAndIncrement()));

      HttpSession session = ((HttpServletRequest)request).getSession(false);
      if (session != null) {
        ThreadLocalLoggingVars.setSessionId(session.getId());
      }

      // Continue processing the rest of the filter chain.
      chain.doFilter(request, response);
    }
    finally {
      ThreadLocalLoggingVars.clearValues();
    }
  }
}
