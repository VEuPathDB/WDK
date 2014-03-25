package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;

public class CopyStepAnalysisAction extends AbstractStepAnalysisIdAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    StepAnalysisContext oldContext = getContextFromPassedId();
    StepAnalysisContext context = StepAnalysisContext.createCopy(oldContext);
    context = getAnalysisMgr().createAnalysis(context);
    return getStepAnalysisJsonResult(context);
  }

}
