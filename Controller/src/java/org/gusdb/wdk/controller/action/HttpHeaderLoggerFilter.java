package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;

/**
 * <p>Logs HTTP response headers before sending them out.  To add this
 * functionality, you must re-add the filter definition into web.xml after
 * the definition of the servlet.  For example:</p>
 * 
 * <pre>
 * <filter>
 *   <filter-name>httpHeaderLogger</filter-name>
 *   <filter-class>org.gusdb.wdk.controller.action.HttpHeaderLoggerFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *   <filter-name>httpHeaderLogger</filter-name>
 *   <url-pattern>*.do</url-pattern>
 * </filter-mapping>
 * </pre>
 * 
 * @author rdoherty
 */
public class HttpHeaderLoggerFilter implements Filter {

  private static final Logger LOG = Logger.getLogger(HttpHeaderLoggerFilter.class.getName());
  private static final String NL = System.getProperty("line.separator");

  @Override
  public void doFilter(ServletRequest request, final ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    final Map<String, List<String>> headers = new HashMap<String, List<String>>();
    
    chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {

      @Override public void setHeader(String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        headers.put(name, values);
        super.setHeader(name, value);
      }
      
      @Override public void addHeader(String name, String value) {
        List<String> values = headers.get(name);
        if (values == null) {
          values = new ArrayList<String>(); 
          headers.put(name, values);
        }
        values.add(value);
        super.addHeader(name, value);
      }
    });
    
    LOG.warn(printHeaders(headers));
  }

  private String printHeaders(Map<String, List<String>> headers) {
    StringBuilder out = new StringBuilder("HTTP HEADERS {").append(NL);
    for (String key : headers.keySet()) {
      Iterator<String> values = headers.get(key).iterator();
      out.append("  ").append(key).append(": [ ").append(values.next());
      while(values.hasNext()) {
        out.append(", ").append(values.next());
      }
      out.append(" ]").append(NL);
    }
    return out.append("}").append(NL).toString();
  }
  
  
  @Override
  public void init(FilterConfig arg0) throws ServletException {
    // do nothing 
  }
  @Override
  public void destroy() {
    // do nothing
  }
}
