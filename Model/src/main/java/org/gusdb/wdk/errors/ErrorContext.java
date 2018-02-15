package org.gusdb.wdk.errors;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.model.MDCUtil;
import org.gusdb.wdk.model.MDCUtil.MdcBundle;
import org.gusdb.wdk.model.WdkModel;

public class ErrorContext {

    public static enum ErrorLocation {
      WDK_SITE("Site"),
      WDK_SERVICE("Service"),
      WDK_CLIENT("Client");

      private final String _label;
      private ErrorLocation(String label) { _label = label; }
      public String getLabel() { return _label; }
    }

    private final WdkModel _wdkModel;
    private final RequestData _requestData;
    private final Map<String, Object> _servletContextAttributes;
    private final Map<String, Object> _requestAttributeMap;
    private final Map<String, Object> _sessionAttributeMap;
    private final ErrorLocation _errorLocation;
    private final MdcBundle _mdcBundle;
    private final String _logMarker;
    private final Date _date;

    public ErrorContext(WdkModel wdkModel, RequestData requestData,
            Map<String, Object> servletContextAttributes,
            Map<String, Object> requestAttributeMap,
            Map<String, Object> sessionAttributeMap,
            ErrorLocation errorLocation) {
        _wdkModel = wdkModel;
        _requestData = requestData;
        _servletContextAttributes = servletContextAttributes;
        _requestAttributeMap = requestAttributeMap;
        _sessionAttributeMap = sessionAttributeMap;
        _errorLocation = errorLocation;
        _mdcBundle = MDCUtil.getMdcBundle();
        _logMarker = UUID.randomUUID().toString();
        _date = new Date();
    }

    public WdkModel getWdkModel() { return _wdkModel; }
    public RequestData getRequestData() { return _requestData; }
    public Map<String, Object> getServletContextAttributes() { return _servletContextAttributes; }
    public Map<String, Object> getRequestAttributeMap() { return _requestAttributeMap; }
    public Map<String, Object> getSessionAttributeMap() { return _sessionAttributeMap; }

    /**
     * A site is considered monitored if the administrator email from adminEmail in the model-config.xml has content.
     * @return true if the administrator email has content, false otherwise.
     */
    public boolean isSiteMonitored() {
      return !getAdminEmails().isEmpty();
    }

    /**
     * Collect the comma delimited list of administrator emails from adminEmail in the model-config.xml and
     * return them as an array
     * @return - array of administrator emails
     */
    public List<String> getAdminEmails() {
      return _wdkModel.getModelConfig().getAdminEmails();
    }

    /**
     * @return where this error was generated.
     *   - during a site (i.e. struts action) request
     *   - during a service (i.e. jersey) request
     *   - from the client
     */
    public ErrorLocation getErrorLocation() {
      return _errorLocation;
    }

    /**
     * @return snapshot of set MDC values in the current thread
     */
    public MdcBundle getMdcBundle() {
      return _mdcBundle;
    }

    /**
     * Returns marker to uniquely identify this error in logs and other error handling contexts
     * @return unique identifier for this error
     */
    public String getLogMarker() {
      return _logMarker;
    }

    /**
     * @return date the error occurred
     */
    public Date getErrorDate() {
      return _date;
    }
}
