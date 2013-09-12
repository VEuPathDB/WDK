package org.gusdb.wdk.controller.action.standard;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * This Action is called by the ActionServlet when an error occurred. It 1)
 * finds the type of error, 3) forwards control to a jsp page specific to this
 * error type
 */
public class ShowErrorPageAction extends WdkAction {

  private static final Logger LOG = Logger.getLogger(ShowErrorPageAction.class.getName());

  @Override protected boolean shouldValidateParams() { return false; }
  @Override protected Map<String, ParamDef> getParamDefs() { return EMPTY_PARAMS; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    Exception causingException = (Exception)
        getRequestData().getRequestAttribute(Globals.EXCEPTION_KEY);
    
    // Alternative mechanism to pass Exception to this action
    //   (see CustomProcessLoginAction.java for example)
    String errorText = params.getValueOrEmpty(CConstants.WDK_ERROR_TEXT_KEY);
    if (causingException == null && !errorText.isEmpty()) {
      causingException = new Exception(errorText);
    }
    
    LOG.error("Exception received by ShowErrorPage: ", causingException);
    
    // FIXME: Should only be one type param; but have seen >1 when error occurs
    //        in this action. For now, just take first type or empty if none.
    String[] types = params.getValues(CConstants.ERROR_TYPE_PARAM);
    String type = types.length == 0 ? "" : types[0];
    
    String errorPageName =
        ((!type.isEmpty() && type.equals(CConstants.ERROR_TYPE_USER)) ?
            CConstants.WDK_USER_ERROR_PAGE : CConstants.WDK_MODEL_ERROR_PAGE);

    return new ActionResult().setViewPath(getCustomViewDir() + errorPageName);
  }
}
