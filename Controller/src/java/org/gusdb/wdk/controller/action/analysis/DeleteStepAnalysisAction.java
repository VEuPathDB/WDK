package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;

public class DeleteStepAnalysisAction extends AbstractStepAnalysisIdAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    return getStepAnalysisJsonResult(getAnalysisMgr().deleteAnalysis(getContextFromPassedId()));
  }
}
