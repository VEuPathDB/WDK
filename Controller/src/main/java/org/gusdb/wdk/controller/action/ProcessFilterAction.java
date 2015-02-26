package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerFilterInstanceBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONObject;

/**
 * This action should be used to handle only the following situation:
 * 
 * User applies a filter on a step;
 * 
 * the expected inputs are:
 * 
 * step: a step id
 * 
 * filter: a filter name.
 */
public class ProcessFilterAction extends ProcessQuestionAction {

  private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);
  
  public static final String PARAM_STEP = "step";
  public static final String PARAM_FILTER = "filter";
  
  public static final String KEY_STATE = "state";
  
  public static final String STATE_SUCCESS = "success";
  public static final String STATE_FAILURE = "failure";
  
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    logger.debug("Entering ProcessFilterAction...");

    String strStepId = request.getParameter(PARAM_STEP);
    String filterName = request.getParameter(PARAM_FILTER);
    if (strStepId == null) throw new WdkUserException("Required step param is missing.");

    UserBean wdkUser = ActionUtility.getUser(servlet, request);
    
    JSONObject jsResult = new JSONObject();
    try {
      int stepId = Integer.valueOf(strStepId);
      StepBean step = wdkUser.getStep(stepId);
      
      if (filterName != null) {
        AnswerFilterInstanceBean filter = step.getRecordClass().getFilter(filterName);
       if (filter == null) throw new WdkUserException("The filter is invalid: " + filterName);
      }
      step.setFilterName(filterName);
      step.update(true);

      jsResult.put(KEY_STATE, STATE_SUCCESS);
      PrintWriter writer = response.getWriter();
      writer.write(jsResult.toString());
      writer.flush();
      writer.close();
    }
    catch (Exception ex) {
      logger.error("Error while processing filter.", ex);
      ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
    }
    return null;
  }
}
