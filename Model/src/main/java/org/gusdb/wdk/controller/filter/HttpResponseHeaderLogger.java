package org.gusdb.wdk.controller.filter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.Tuples.TwoTuple;

public class HttpResponseHeaderLogger implements Filter {

  private static final Logger LOG = Logger.getLogger(HttpResponseHeaderLogger.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // only do the work of this class if debug is enabled
    if (LOG.isTraceEnabled()) {

      HttpServletResponse httpResponse = (HttpServletResponse)response;
      CapturingHttpServletResponse responseWrapper = new CapturingHttpServletResponse(httpResponse);
  
      // execute this request
      chain.doFilter(request, responseWrapper);
  
      // log response
      LOG.trace(new StringBuilder(NL)
          .append("Request [").append(getRequestUrl((HttpServletRequest)request)).append("]").append(NL)
          .append(responseWrapper.getData()).toString());
    }
    else {
      // simply pass to next filter
      chain.doFilter(request,  response);
    }
  }

  public static String getRequestUrl(HttpServletRequest request) {
    StringBuffer requestURL = request.getRequestURL();
    String queryString = request.getQueryString();

    if (queryString == null) {
        return requestURL.toString();
    } else {
        return requestURL.append('?').append(queryString).toString();
    }
  }

  private static class CapturingHttpServletResponse extends HttpServletResponseWrapper {

    private static enum HeaderType {
      STRING, INTEGER, DATE;
    }

    private static class HeaderValuePair extends TwoTuple<HeaderType, String>{
      public HeaderValuePair(HeaderType first, String second) {
        super(first, second);
      }
      @Override
      public String toString() {
        return new StringBuilder("(").append(getFirst().name()).append(") ").append(getSecond()).toString();
      }
    }

    private Integer _responseStatus = null;
    private Map<String, HeaderValuePair> _headers = new HashMap<>();
    private String _redirectLocation = null;

    public CapturingHttpServletResponse(HttpServletResponse response) {
      super(response);
    }

    public String getData() {
      return new StringBuilder("Response [status=").append(_responseStatus)
          .append(", redirect=").append(_redirectLocation == null ? "<none>" : _redirectLocation)
          .append(", headers=").append(FormatUtil.prettyPrint(_headers, Style.MULTI_LINE)).append("]").toString();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
      _redirectLocation = location;
      super.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
      _headers.put(name, new HeaderValuePair(HeaderType.DATE, new Date(date).toString()));
      super.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
      _headers.put(name, new HeaderValuePair(HeaderType.DATE, new Date(date).toString()));
      super.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
      _headers.put(name, new HeaderValuePair(HeaderType.STRING, value));
      super.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
      _headers.put(name, new HeaderValuePair(HeaderType.STRING, value));
      super.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
      _headers.put(name, new HeaderValuePair(HeaderType.INTEGER, String.valueOf(value)));
      super.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
      _headers.put(name, new HeaderValuePair(HeaderType.INTEGER, String.valueOf(value)));
      super.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
      _responseStatus = sc;
      super.setStatus(sc);
    }

  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    // nothing to do here
  }

  @Override
  public void destroy() {
    // nothing to do here
  }

}
