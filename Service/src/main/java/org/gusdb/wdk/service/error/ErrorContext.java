package org.gusdb.wdk.service.error;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.gusdb.wdk.model.WdkModel;

public class ErrorContext {

    private final String[] _publicSitePrefixes;
    private final WdkModel _wdkModel;
    private final String _projectName;
    private final HttpServletRequest _request;
    private final Map<String, Object> _servletContextAttributes;
    private final Map<String, Object> _requestAttributeMap;
    private final Map<String, Object> _sessionAttributeMap;
    
    public ErrorContext(String[] publicSitePrefixes, WdkModel wdkModel,
            String projectName, HttpServletRequest request,
            Map<String, Object> servletContextAttributes,
            Map<String, Object> requestAttributeMap,
            Map<String, Object> sessionAttributeMap) {
        _publicSitePrefixes = publicSitePrefixes;
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

    public boolean isPublicSite() {
      String serverName = _request.getServerName();
      for (String prefix : _publicSitePrefixes) {
        if ((prefix + _projectName + ".org").equalsIgnoreCase(serverName)) {
          return true;
        }
      }
      return false;
    }

    public boolean siteIsMonitored() {
      return isPublicSite();
      // Return true if you want dev sites to email errors.
      //return true;
    }
    
    public String[] getAdminEmails() {
        String emailProp = _wdkModel.getProperties().get("SITE_ADMIN_EMAIL");
        return (emailProp == null || emailProp.isEmpty() ? new String[]{} :
            Pattern.compile("[,\\s]+").split(emailProp));
    }
}
