package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;

public class CopyStepAnalysisAction extends AbstractStepAnalysisIdAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    return getStepAnalysisJsonResult(getAnalysisMgr().copyContext(getContextFromPassedId()));
  }
}
