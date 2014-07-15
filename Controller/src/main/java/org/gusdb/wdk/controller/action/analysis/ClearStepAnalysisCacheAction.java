package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;

public class ClearStepAnalysisCacheAction extends GenericPageAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    analysisMgr.clearResultsCache();
    return new ActionResult(ResponseType.text).setStream(
        IoUtil.getStreamFromString("Results cache successfully cleared."));
  }
}
