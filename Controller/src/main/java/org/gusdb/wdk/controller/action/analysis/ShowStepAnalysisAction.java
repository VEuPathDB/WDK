package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;

public class ShowStepAnalysisAction extends AbstractStepAnalysisIdAction {
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    // need to call this to verify access permission
    getContextFromPassedId();
    return new ActionResult().setViewName(SUCCESS)
        .setRequestAttribute(StepAnalysisContext.ANALYSIS_ID_KEY, getAnalysisId());
  }
}
