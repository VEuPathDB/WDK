package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * @author xingao
 */
public class ShowPasswordAction extends WdkAction {

    @Override
    protected boolean shouldValidateParams() {
      return true;
    }

    @Override
    protected Map<String, ParamDef> getParamDefs() {
      return EMPTY_PARAMS;
    }

    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {
      // if a custom password page exists, use it; otherwise, use default one
      String customViewFile = getCustomViewDir() + CConstants.WDK_PASSWORD_PAGE;
      return (wdkResourceExists(customViewFile) ?
          new ActionResult().setViewPath(customViewFile) :
          new ActionResult().setViewName(SUCCESS));
    }
}
