package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;

public class ShowStepAnalysisAction extends AbstractStepAnalysisIdAction {
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    // need to call this to verify access permission
    StepAnalysisInstance context = getContextFromPassedId();
    return new ActionResult().setViewName(SUCCESS)
        .setRequestAttribute(StepAnalysisInstance.ANALYSIS_ID_KEY, getAnalysisId())
        .setRequestAttribute("hasParameters", context.getStepAnalysis().getHasParameters());
  }
}
