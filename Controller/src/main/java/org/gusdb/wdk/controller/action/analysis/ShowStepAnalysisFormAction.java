package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;

public class ShowStepAnalysisFormAction extends AbstractStepAnalysisIdAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisInstance context = getContextFromPassedId();

    if (!context.getIsValidStep()) {
      return new ActionResult().setViewName("invalidStep")
          .setRequestAttribute("reason", context.getInvalidStepReason());
    }

    String resolvedView = getAnalysisMgr().getViewResolver().resolveFormView(this, context);
    Object formViewModel = getAnalysisMgr().getFormViewModel(context);
    return new ActionResult().setViewPath(resolvedView)
        .setRequestAttribute("wdkModel", getWdkModel())
        .setRequestAttribute("viewModel", formViewModel)
        .setRequestAttribute("hasParameters", context.getStepAnalysis().getHasParameters());
  }
}
