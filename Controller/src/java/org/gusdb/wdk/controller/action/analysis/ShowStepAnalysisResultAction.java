package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.AnalysisResult;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;

public class ShowStepAnalysisResultAction extends AbstractStepAnalysisIdAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = getContextFromPassedId();
    
    switch (context.getStatus()) {
      case CREATED:
        // analysis has not yet been run; return an empty result
        return new ActionResult().setStream(IoUtil.getStreamFromString(""));
      case COMPLETE:
        // NOTE: there is a race condition here if cache is cleared between
        //       getting status above and retrieving status below.
        AnalysisResult result = getAnalysisMgr().getAnalysisResult(context);
        String viewPath = getAnalysisMgr().getViewResolver().resolveResultsView(this, context);
        return new ActionResult()
            .setViewPath(viewPath)
            .setRequestAttribute("analysisId", context.getAnalysisId())
            .setRequestAttribute("viewModel", result.getResultViewModel());
      case PENDING:
      case RUNNING:
        return new ActionResult()
            .setViewName("pending")
            .setRequestAttribute("analysisId", context.getAnalysisId());
      case ERROR:
      case INTERRUPTED:
      case OUT_OF_DATE:
        return new ActionResult()
            .setViewName("incomplete");
      case UNKNOWN:
      default:
        throw new WdkModelException("Invalid status " + context.getStatus() +
            " found on context.  This should never happen.");
    }
  }
  
}
