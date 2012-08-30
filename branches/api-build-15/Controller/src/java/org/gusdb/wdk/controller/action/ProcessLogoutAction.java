/**
 * 
 */
package org.gusdb.wdk.controller.action;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author xingao
 * 
 */
public class ProcessLogoutAction extends Action {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping
   * , org.apache.struts.action.ActionForm,
   * javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    ActionForward forward = mapping
        .findForward(CConstants.PROCESS_LOGOUT_MAPKEY);

    // clear the session, and reset the default user to guest
    WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext()
        .getAttribute(CConstants.WDK_MODEL_KEY);
    UserFactoryBean factory = wdkModel.getUserFactory();
    UserBean guest = factory.getGuestUser();
    request.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);

    // tell cookie to expire immediately
    response.setContentType("text/html");
    Cookie cookie = new Cookie(CConstants.WDK_LOGIN_COOKIE_KEY, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    cookie.setComment("EXPIRING COOKIE at " + System.currentTimeMillis());
    response.addCookie(cookie);

    return forward;
  }
}
