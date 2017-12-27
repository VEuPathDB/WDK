package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.actionutil.QuestionRequestParams;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.query.param.RequestParams;
import org.gusdb.wdk.model.query.param.values.StableValues;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.query.param.values.WriteableStableValues;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It 1) reads param values from
 * input form bean, 2) runs the query and saves the answer 3) forwards control to a jsp page that displays a
 * summary
 */

public class ProcessQuestionAction extends Action {

  private static final Logger logger = Logger.getLogger(ProcessQuestionAction.class);

  public static StableValues prepareParams(UserBean user, HttpServletRequest request,
      QuestionForm qform) throws WdkModelException, WdkUserException {
    RequestParams requestParams = new QuestionRequestParams(request, qform);
    QuestionBean question = qform.getQuestion();

    Map<String, ParamBean<?>> params = question.getParamsMap();
    Map<String, String> stableValues = new LinkedHashMap<>();
    for (String paramName : params.keySet()) {
      ParamBean<?> param = params.get(paramName);
      String stableValue = param.getStableValue(user, requestParams);
      stableValues.put(paramName, stableValue);
    }
    return new WriteableStableValues(question.getQuestion().getQuery(), stableValues);
  }

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.debug("Entering ProcessQuestionAction..");
    QuestionForm qForm = (QuestionForm) form;
		String customName = qForm.getCustomName();

    // get question name first so it can be used in error reporting
    String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
    try {
      UserBean wdkUser = ActionUtility.getUser(request);

      // validate question name
      ActionUtility.getWdkModel(servlet).validateQuestionFullName(qFullName);

      // the params has been validated, and now is parsed, and if the size
      // of the value is too long, it will be replaced is checksum
      StableValues params = prepareParams(wdkUser, request, qForm);

      // get the assigned weight
      String strWeight = request.getParameter(CConstants.WDK_ASSIGNED_WEIGHT_KEY);
      boolean hasWeight = (strWeight != null && strWeight.length() > 0);
      int weight = Utilities.DEFAULT_WEIGHT;
      if (hasWeight) {
        if (!strWeight.matches("[\\-\\+]?\\d+"))
          throw new WdkUserException("Invalid weight value: '" + strWeight +
              "'. Only integer numbers are allowed.");
        if (strWeight.length() > 9)
          throw new WdkUserException("Weight number is too big: " + strWeight);
        weight = Integer.parseInt(strWeight);
      }

      QuestionBean wdkQuestion = qForm.getQuestion();
      // the question is already validated in the question form, don't need to do it again.
      String filterName = request.getParameter(CConstants.WDK_FILTER_KEY);
      CompleteValidStableValues validParams = ValidStableValuesFactory.createFromCompleteValues(wdkUser.getUser(), params);
      StepBean step = wdkUser.createStep(null, wdkQuestion, validParams, filterName, false, weight);
      if (step.getException() != null) {
        // exception occurred loading initial results for this step
        throw step.getException();
      }
      step.setCustomName(customName);
      step.update(false);

      logger.debug("Test run search [" + qFullName + "] and get # of results: " + step.getResultSize());

      /*
       * Charles Treatman 4/23/09 Add code here to set the current_application_tab cookie so that user will go
       * to the Run Strategies tab after running a question from a question page.
       */
      ShowApplicationAction.setWdkTabStateCookie(request, response);

      ActionForward showSummary = mapping.findForward(CConstants.PQ_SHOW_SUMMARY_MAPKEY);
      StringBuffer url = new StringBuffer(showSummary.getPath());
      url.append("?" + CConstants.WDK_STEP_ID_KEY + "=" + step.getStepId());

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      logger.debug("Leaving ProcessQuestionAction successfully, forward to " + forward.getPath());
      return forward;
    }
    catch (WdkUserException ex) {
      logger.error("Error while processing question", ex);

      ActionMessages messages = new ActionErrors();
      Map<String,String> paramErrors = ex.getParamErrors();
      // check to see if param errors are present
      if (paramErrors == null) {
        ActionMessage message = new ActionMessage("mapped.properties", (qFullName == null ||
            qFullName.isEmpty() ? "Unknown question name" : qFullName), ex.getMessage());
        messages.add(ActionErrors.GLOBAL_MESSAGE, message);
      }
      else {
        // param validation probably failed
        for (Entry<String,String> paramError : paramErrors.entrySet()) {
          ActionMessage message = new ActionMessage("mapped.properties", paramError.getKey() + ": " + paramError.getValue(), null);
          messages.add(ActionErrors.GLOBAL_MESSAGE, message);
        }
      }
      saveErrors(request, messages);
      ActionForward forward = mapping.getInputForward();
      logger.error("ProcessQuestionAction error forward = " + forward.getPath());
      return forward;
    }
  }
}
