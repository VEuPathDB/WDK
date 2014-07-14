package org.gusdb.wdk.controller.filter;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.MDC;

/**
 * A filter that adds (and removes) MDC variables for log4j.  Added to MDC by
 * this filter are:
 * 
 *   ipAddress: IP address of the sender of the current request
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

  public static final String LOG4J_IP_ADDRESS_KEY = "ipAddress";
  public static final String LOG4J_SESSION_ID_KEY = "sessionId";
  
  @Override
  public void init(FilterConfig filterConfig) { }

  @Override
  public void destroy() { }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    try {

      String ipAddress = request.getRemoteAddr();
      if (ipAddress != null) {
        MDC.put(LOG4J_IP_ADDRESS_KEY, ipAddress);
      }

      HttpSession session = ((HttpServletRequest)request).getSession(false);
      if (session != null) {
        String sessionId = session.getId();
        if (sessionId != null) {
          MDC.put(LOG4J_SESSION_ID_KEY, sessionId);
        }
      }

      // Continue processing the rest of the filter chain.
      chain.doFilter(request, response);
    }
    finally {
      MDC.remove(LOG4J_IP_ADDRESS_KEY);
      MDC.remove(LOG4J_SESSION_ID_KEY);
    }
  }
}
