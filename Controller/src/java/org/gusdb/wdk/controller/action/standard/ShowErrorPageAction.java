package org.gusdb.wdk.controller.action.standard;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamDefMapBuilder;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * This Action is called by the ActionServlet when an error occurred. It 1)
 * finds the type of error, 3) forwards control to a jsp page specific to this
 * error type
 */

public class ShowErrorPageAction extends WdkAction {

  private static final Logger LOG = Logger.getLogger(ShowErrorPageAction.class.getName());

  private static final String PARAM_SOURCE = "source_id";
  private static final String PARAM_PROJECT = "project_id";
  private static final String PARAM_NAME = "name";
  
  public static final Map<String, ParamDef> PARAM_DEFS = new ParamDefMapBuilder()
      .addParam(CConstants.ERROR_TYPE_PARAM, new ParamDef(Required.OPTIONAL))
      .addParam(PARAM_SOURCE, new ParamDef(Required.OPTIONAL))
      .addParam(PARAM_PROJECT, new ParamDef(Required.OPTIONAL))
      .addParam(PARAM_NAME, new ParamDef(Required.OPTIONAL))
      .toMap();  

  @Override
  protected boolean shouldValidateParams() {
    return true;
  }

  @Override
  protected Map<String, ParamDef> getParamDefs() {
    return PARAM_DEFS;
  }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    Exception causingException = (Exception)
        getRequestData().getRequestAttribute(Globals.EXCEPTION_KEY);
    
    LOG.error("Exception received by ShowErrorPage: ", causingException);
    
    String type = params.getValueOrEmpty(CConstants.ERROR_TYPE_PARAM);
    
    String errorPageName =
        ((!type.isEmpty() && type.equals(CConstants.ERROR_TYPE_USER)) ?
            CConstants.WDK_USER_ERROR_PAGE : CConstants.WDK_MODEL_ERROR_PAGE);

    return new ActionResult().setViewPath(getCustomViewDir() + errorPageName);
  }
}
