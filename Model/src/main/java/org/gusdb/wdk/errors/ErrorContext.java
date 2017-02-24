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

    public static enum RequestType {
      WDK_SITE, WDK_SERVICE;
    }

    private final WdkModel _wdkModel;
    private final RequestData _requestData;
    private final Map<String, Object> _servletContextAttributes;
    private final Map<String, Object> _requestAttributeMap;
    private final Map<String, Object> _sessionAttributeMap;
    private final RequestType _requestType;
    private final MdcBundle _mdcBundle;
    private final String _logMarker;
    private final Date _date;

    public ErrorContext(WdkModel wdkModel, RequestData requestData,
            Map<String, Object> servletContextAttributes,
            Map<String, Object> requestAttributeMap,
            Map<String, Object> sessionAttributeMap,
            RequestType requestType) {
        _wdkModel = wdkModel;
        _requestData = requestData;
        _servletContextAttributes = servletContextAttributes;
        _requestAttributeMap = requestAttributeMap;
        _sessionAttributeMap = sessionAttributeMap;
        _requestType = requestType;
        _mdcBundle = MDCUtil.getMdcBundle();
        _logMarker = UUID.randomUUID().toString();
        _date = new Date();
    }

    public String getProjectName() { return _wdkModel.getProjectId(); }
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
     * @return whether this error was caused during a site (i.e. struts action) or service (i.e. jersey) request
     */
    public RequestType getRequestType() {
      return _requestType;
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
