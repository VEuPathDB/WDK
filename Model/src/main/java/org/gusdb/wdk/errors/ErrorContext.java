package org.gusdb.wdk.errors;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.fgputil.logging.ThreadLocalLoggingVars;
import org.gusdb.fgputil.logging.ThreadLocalLoggingVars.ThreadContextBundle;
import org.gusdb.fgputil.web.RequestSnapshot;
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
    private final RequestSnapshot _requestData;
    private final ReadOnlyMap<String, Object> _requestAttributeMap;
    private final ReadOnlyMap<String, Object> _sessionAttributeMap;
    private final ErrorLocation _errorLocation;
    private final ThreadContextBundle _mdcBundle;
    private final String _logMarker;
    private final Date _date;

    public ErrorContext(WdkModel wdkModel, RequestSnapshot requestData,
            ReadOnlyMap<String, Object> sessionAttributeMap,
            ErrorLocation errorLocation) {
        _wdkModel = wdkModel;
        _requestData = requestData;
        _requestAttributeMap = requestData.getAttributes();
        _sessionAttributeMap = sessionAttributeMap;
        _errorLocation = errorLocation;
        _mdcBundle = ThreadLocalLoggingVars.getThreadContextBundle();
        _logMarker = UUID.randomUUID().toString();
        _date = new Date();
    }

    public WdkModel getWdkModel() { return _wdkModel; }
    public RequestSnapshot getRequestData() { return _requestData; }
    public ReadOnlyMap<String, Object> getRequestAttributeMap() { return _requestAttributeMap; }
    public ReadOnlyMap<String, Object> getSessionAttributeMap() { return _sessionAttributeMap; }

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
     * @return snapshot of set log4j2 ThreadContext values in the current thread
     */
    public ThreadContextBundle getThreadContextBundle() {
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
