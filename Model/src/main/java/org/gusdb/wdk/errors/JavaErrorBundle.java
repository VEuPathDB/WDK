package org.gusdb.wdk.errors;

import static org.gusdb.fgputil.FormatUtil.getStackTrace;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;

public class JavaErrorBundle implements ErrorBundle {

  private static final Logger LOG = Logger.getLogger(JavaErrorBundle.class);

  private Exception _pageException;
  private Exception _requestException;
  private Exception _passedException;
  private List<String> _actionErrors;

  public JavaErrorBundle(Exception requestException) {
      this(requestException, null, null, Collections.EMPTY_LIST);
  }

  public JavaErrorBundle(Exception requestException, Exception pageException,
          Exception actionException, List<String> actionErrors) {
      _requestException = requestException;
      _pageException = pageException;
      _passedException = actionException;
      _actionErrors = actionErrors;
      LOG.debug("Created bundle with exceptions: " +
              _pageException + ", " +
              _requestException + ", " +
              _passedException + ", " +
              actionErrors.size());
  }

  @Override
  public boolean hasErrors() {
      return (_pageException != null ||
              _requestException != null ||
              _passedException != null ||
              !_actionErrors.isEmpty());
  }

  @Override
  public Exception getException() {
    Exception pex = _pageException;
    Exception rex = _requestException;
    Exception aex = _passedException;
    if (pex != null) return pex;
    if (rex != null) return rex;
    if (aex != null) return aex;
    return null;
  }

  @Override
  public String getDetailedDescription() {
      Exception pex = _pageException;
      Exception rex = _requestException;
      Exception aex = _passedException;

      if (pex == null && rex == null && aex == null)
          return null;

      StringBuilder st = new StringBuilder();

      if (rex != null) {
          st.append(getStackTrace(rex));
          st.append("\n\n-- from pageContext.getException()\n");
      }
      if (pex != null) {
          st.append(getStackTrace(pex));
          st.append("\n\n-- from request.getAttribute(Globals.EXCEPTION_KEY)\n");
      }
      if (aex != null) {
          st.append(getStackTrace(aex));
          st.append("\n\n-- from request.getAttribute(CConstants.WDK_EXCEPTION)\n");
      }
      return st.toString();
  }

  @Override
  public String getActionErrorsAsHtml() {
    StringBuilder sb = new StringBuilder();
    for (String error : _actionErrors) {
        // filter non-sensical errors
        if (!error.equals("???en_US.global.error.user???") &&
            !error.equals("???en_US.global.error.model???")) {
            sb.append("<li>" + error + "</li>\n");
        }
    }
    String errorList = sb.toString();
    return (errorList.isEmpty() ? "" : "<ul>\n" + errorList + "</ul>\n");
  }

  @Override
  public String getActionErrorsAsText() {
    StringBuilder sb = new StringBuilder();
    for (String error : _actionErrors) {
        // filter non-sensical errors
        if (!error.equals("???en_US.global.error.user???") &&
            !error.equals("???en_US.global.error.model???")) {
            sb.append(error).append(FormatUtil.NL);
        }
    }
    return sb.toString();
  }
}
