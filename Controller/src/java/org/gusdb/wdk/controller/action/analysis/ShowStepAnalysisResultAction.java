package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory.AnalysisResult;

public class ShowStepAnalysisResultAction extends GenericPageAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = new StepAnalysisContext(getCurrentUser(), params.getParamMap());
    int analysisId = context.getValidatedAnalysisId();
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    AnalysisResult result = analysisMgr.getAnalysisResult(analysisId);

    switch (result.status) {
      case COMPLETE:
        return new ActionResult()
            .setViewPath(analysisMgr.resolveResultsView(this, context))
            .setRequestAttribute("viewModel", result.analysisViewModel);
      case PENDING:
      case RUNNING:
        return new ActionResult()
            .setViewName("pending")
            .setRequestAttribute("analysisId", analysisId);
      case ERROR:
      case INTERRUPTED:
        return new ActionResult()
            .setViewName("incomplete")
            .setRequestAttribute("status", result.status.name());
      default:
        throw new WdkModelException("Invalid status returned from DB.  This should never happen.");
    }
  }
  
}
