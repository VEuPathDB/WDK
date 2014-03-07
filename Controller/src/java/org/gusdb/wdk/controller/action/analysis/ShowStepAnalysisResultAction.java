package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory.AnalysisResult;

public class ShowStepAnalysisResultAction extends GenericPageAction {
  
  private static final String ANALYSIS_ID_KEY = "analysisId";
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = new StepAnalysisContext(getCurrentUser(), params.getParamMap());
    int analysisId = getAnalysisId(params);
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

  private int getAnalysisId(ParamGroup params) throws WdkUserException {
    String saIdStr = params.getValueOrEmpty(ANALYSIS_ID_KEY);
    if (saIdStr.isEmpty() || !FormatUtil.isInteger(saIdStr)) {
      throw new WdkUserException("Parameter '" + ANALYSIS_ID_KEY + "' must exist and be an integer.");
    }
    return Integer.parseInt(saIdStr);
  }
  
}
