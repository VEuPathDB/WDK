package org.gusdb.wdk.service.error;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.gusdb.wdk.model.WdkModel;

public class ErrorContext {

    private final WdkModel _wdkModel;
    private final String _projectName;
    private final HttpServletRequest _request;
    private final Map<String, Object> _servletContextAttributes;
    private final Map<String, Object> _requestAttributeMap;
    private final Map<String, Object> _sessionAttributeMap;
    
    public ErrorContext(WdkModel wdkModel,
            String projectName, HttpServletRequest request,
            Map<String, Object> servletContextAttributes,
            Map<String, Object> requestAttributeMap,
            Map<String, Object> sessionAttributeMap) {
        _wdkModel = wdkModel;
        _projectName = projectName;
        _request = request;
        _servletContextAttributes = servletContextAttributes;
        _requestAttributeMap = requestAttributeMap;
        _sessionAttributeMap = sessionAttributeMap;
    }

    public String getProjectName() { return _projectName; }
    public HttpServletRequest getRequest() { return _request; }
    public Map<String, Object> getServletContextAttributes() { return _servletContextAttributes; }
    public Map<String, Object> getRequestAttributeMap() { return _requestAttributeMap; }
    public Map<String, Object> getSessionAttributeMap() { return _sessionAttributeMap; }

   
    /**
     * A site is considered monitored if the administrator email from adminEmail in the model-config.xml has content.
     * @return - true if the administrator email has content, false otherwise.
     */
    public boolean isSiteMonitored() {
      String emailProp = _wdkModel.getModelConfig().getAdminEmail();
      return emailProp != null && !emailProp.isEmpty();
    }
    
    /**
     * Collect the comma delimited list of administrator emails from adminEmail in the model-config.xml and
     * return them as an array
     * @return - array of administrator emails
     */
    public String[] getAdminEmails() {
        String emailProp = _wdkModel.getModelConfig().getAdminEmail();
        return (emailProp == null || emailProp.isEmpty() ? new String[]{} :
            Pattern.compile("[,\\s]+").split(emailProp));
    }
}
