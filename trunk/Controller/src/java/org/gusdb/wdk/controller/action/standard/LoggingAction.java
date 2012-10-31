package org.gusdb.wdk.controller.action.standard;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;

public class LoggingAction extends GenericPageAction {

    private static final String PARAM_CONTENT = "content";
    
    private static final Logger logger = Logger.getLogger(LoggingAction.class.getName());
    
    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {
      String content = params.getValueOrEmpty(PARAM_CONTENT);
      if (content.isEmpty()) {
        content = "Received logging request but no value in '" + PARAM_CONTENT + "' parameter.";
      }
      logger.info(content);
      return ActionResult.EMPTY_RESULT;
    }
}
