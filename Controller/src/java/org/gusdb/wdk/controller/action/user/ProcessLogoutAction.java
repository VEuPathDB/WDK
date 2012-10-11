package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import javax.servlet.http.Cookie;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * @author xingao
 * @author rdoherty
 */
public class ProcessLogoutAction extends WdkAction {

  // no params are necessary or expected, but don't mind if user sends them
  @Override protected Map<String, ParamDef> getParamDefs() { return null; }
  @Override protected boolean shouldValidateParams() { return false; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    // reset the current user to new guest
    setCurrentUser(getWdkModel().getUserFactory().getGuestUser());

    // tell cookie to expire immediately
    addCookieToResponse(getLogoutCookie());

    return new ActionResult().setViewName(SUCCESS);
  }
  
  public static Cookie getLogoutCookie() {
    Cookie cookie = new Cookie(CConstants.WDK_LOGIN_COOKIE_KEY, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    return cookie;
  }
}
