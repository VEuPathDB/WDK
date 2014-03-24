package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory.AnalysisResult;

public class ShowStepAnalysisResultAction extends AbstractStepAnalysisIdAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = getContextFromPassedId();
    AnalysisResult result = getAnalysisMgr().getAnalysisResult(context);

    switch (result.status) {
      case CREATED:
        // analysis has not yet been run; return an empty result
        return new ActionResult().setStream(IoUtil.getStreamFromString(""));
      case COMPLETE:
        return new ActionResult()
            .setViewPath(getAnalysisMgr().resolveResultsView(this, context))
            .setRequestAttribute("viewModel", result.analysisViewModel);
      case PENDING:
      case RUNNING:
        return new ActionResult()
            .setViewName("pending")
            .setRequestAttribute("analysisId", context.getAnalysisId());
      case ERROR:
      case INTERRUPTED:
        return new ActionResult()
            .setViewName("incomplete")
            .setRequestAttribute("status", result.status.name());
      case UNKNOWN:
      default:
        throw new WdkModelException("Invalid status returned from DB.  This should never happen.");
    }
  }
  
}
