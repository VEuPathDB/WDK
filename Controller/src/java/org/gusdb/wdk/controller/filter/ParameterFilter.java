package org.gusdb.wdk.controller.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class ParameterFilter implements Filter {

  FilterConfig filterConfig = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String regex = this.filterConfig.getInitParameter("excludeParams");
    chain.doFilter(new ParamFilteredRequest(request, regex), response);
  }

  @Override
  public void destroy() {}

  static class ParamFilteredRequest extends HttpServletRequestWrapper {

    private String regex;

    public ParamFilteredRequest(ServletRequest request, String regex) {
      super((HttpServletRequest) request);
      this.regex = regex;
    }

    @Override
    public Enumeration<String> getParameterNames() {
      @SuppressWarnings("unchecked")
      List<String> requestParameterNames = Collections.list((Enumeration<String>) super.getParameterNames());
      List<String> finalParameterNames = new ArrayList<>();

      for (String parameterName : requestParameterNames) {
        if (!parameterName.matches(regex)) {
          finalParameterNames.add(parameterName);
          System.out.println("Param : " + parameterName);
        }
      }
      return Collections.enumeration(finalParameterNames);
    }
  }
}
package org.gusdb.wdk.controller.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class ParameterFilter implements Filter {

  FilterConfig filterConfig = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String regex = this.filterConfig.getInitParameter("excludeParams");
    chain.doFilter(new ParamFilteredRequest(request, regex), response);
  }

  @Override
  public void destroy() {}

  static class ParamFilteredRequest extends HttpServletRequestWrapper {

    private String regex;

    public ParamFilteredRequest(ServletRequest request, String regex) {
      super((HttpServletRequest) request);
      this.regex = regex;
    }

    @Override
    public Enumeration<String> getParameterNames() {
      @SuppressWarnings("unchecked")
      List<String> requestParameterNames = Collections.list((Enumeration<String>) super.getParameterNames());
      List<String> finalParameterNames = new ArrayList<>();

      for (String parameterName : requestParameterNames) {
        if (!parameterName.matches(regex)) {
          finalParameterNames.add(parameterName);
          System.out.println("Param : " + parameterName);
        }
      }
      return Collections.enumeration(finalParameterNames);
    }
  }
}
