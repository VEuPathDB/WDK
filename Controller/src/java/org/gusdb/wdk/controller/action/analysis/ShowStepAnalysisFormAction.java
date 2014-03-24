package org.gusdb.wdk.controller.action.analysis;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;

public class ShowStepAnalysisFormAction extends AbstractStepAnalysisIdAction {
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = getContextFromPassedId();
    String resolvedView = getAnalysisMgr().resolveFormView(this, context);

    StepAnalyzer analyzer = context.getStepAnalysis().getAnalyzerInstance();
    ActionResult result = new ActionResult().setViewPath(resolvedView)
        .setRequestAttribute("viewModel", analyzer.getFormViewModel());
    
    // special case for interacting with a WDK question
    /*
    if (analyzer instanceof AbstractWdkQuestionAnalyzer) {
      AbstractWdkQuestionAnalyzer questionAnalyzer = (AbstractWdkQuestionAnalyzer)analyzer;
      
      for (Entry<String,Object> entry : questionAnalyzer.getQuestionViewModel(getWdkModel().getModel()).entrySet()) {
        result.setRequestAttribute(entry.getKey(), entry.getValue());
      }
    }*/
    
    return result;
  }
  /*
  public void assign(ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    ActionServlet servlet = getServlet();
    QuestionForm qForm = (QuestionForm) getStrutsActionForm();
    String qFullName = getQuestionName(qForm, request);
    ActionUtility.getWdkModel(servlet).validateQuestionFullName(qFullName);
    QuestionBean wdkQuestion = getQuestionBean(servlet, qFullName);

    prepareQuestionForm(wdkQuestion, servlet, request, qForm);
    setParametersAsAttributes(request);
  */
}
