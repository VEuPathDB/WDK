package org.gusdb.wdk.controller.action.standard;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamDefMapBuilder;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

public class LoggingAction extends WdkAction {

    private static final String PARAM_CONTENT = "content";
    
    private static final Logger logger = Logger.getLogger(LoggingAction.class.getName());

    public static final Map<String, ParamDef> PARAM_DEFS = new ParamDefMapBuilder()
        .addParam(PARAM_CONTENT, new ParamDef(Required.REQUIRED)).toMap();
    
    @Override protected boolean shouldValidateParams() {
      return true;
    }
    
    @Override protected Map<String, ParamDef> getParamDefs() {
      return PARAM_DEFS;
    }
    
    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {
      String content = params.getValue(PARAM_CONTENT);
      logger.info(content);
      return ActionResult.EMPTY_RESULT;
    }
}
