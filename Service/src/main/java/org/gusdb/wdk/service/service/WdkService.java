package org.gusdb.wdk.service.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;

public abstract class WdkService {

  @Context
  private HttpServletRequest _request;
  private WdkModel _wdkModel;

  protected WdkModel getWdkModel() {
    return _wdkModel;
  }

  protected int getCurrentUserId() {
    return ((UserBean)_request.getSession().getAttribute("wdkUser")).getUser().getUserId();
  }
  
  protected User getCurrentUser() {
    return ((UserBean)_request.getSession().getAttribute("wdkUser")).getUser();
  }

  @Context
  protected void setServletContext(ServletContext context) {
    _wdkModel = ((WdkModelBean)context.getAttribute("wdkModel")).getModel();
  }
}
