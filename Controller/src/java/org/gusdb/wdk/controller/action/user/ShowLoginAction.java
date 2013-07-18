package org.gusdb.wdk.controller.action.user;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;

public class ShowLoginAction extends GenericPageAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    if (getCurrentUser().isGuest()) {
      return new ActionResult().setViewName(SUCCESS);
    }
    // show user profile if user is already logged in
    return new ActionResult().setViewName(CConstants.SHOW_PROFILE_MAPKEY);
  }
}
