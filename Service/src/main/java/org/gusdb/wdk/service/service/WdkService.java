package org.gusdb.wdk.service.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.util.WdkResultFactory;

public abstract class WdkService {

  @Context
  private HttpServletRequest _request;

  @Context
  private UriInfo _uriInfo;

  private WdkModelBean _wdkModelBean;
  private WdkResultFactory _resultFactory;

  protected WdkModelBean getWdkModelBean() {
    return _wdkModelBean;
  }

  protected WdkModel getWdkModel() {
    return _wdkModelBean.getModel();
  }

  protected UriInfo getUriInfo() {
    return _uriInfo;
  }

  protected UserBean getCurrentUserBean() {
    return ((UserBean)_request.getSession().getAttribute("wdkUser"));
  }
  
  protected int getCurrentUserId() {
    return getCurrentUserBean().getUserId();
  }

  protected User getCurrentUser() {
    return getCurrentUserBean().getUser();
  }

  protected WdkResultFactory getResultFactory() {
    if (_resultFactory == null) {
      _resultFactory = new WdkResultFactory(getCurrentUserBean());
    }
    return _resultFactory;
  }

  @Context
  protected void setServletContext(ServletContext context) {
    _wdkModelBean = ((WdkModelBean)context.getAttribute("wdkModel"));
  }

}
