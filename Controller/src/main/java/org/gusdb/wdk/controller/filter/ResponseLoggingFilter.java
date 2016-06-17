package org.gusdb.wdk.controller.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.MDCUtil;

public class ResponseLoggingFilter implements Filter {

  public static final Logger LOG = Logger.getLogger(ResponseLoggingFilter.class);
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    // Continue processing the rest of the filter chain.
    chain.doFilter(request, response);

    String duration = MDCUtil.getRequestDuration();
    if (duration != null) {
      //LOG.info("Request complete " + ((HttpServletRequest)request).getRequestURI());
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException { }

  @Override
  public void destroy() { }

}
