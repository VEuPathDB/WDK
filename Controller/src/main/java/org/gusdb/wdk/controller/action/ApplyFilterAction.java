package org.gusdb.wdk.controller.action;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.StepUtilities;
import org.json.JSONArray;
import org.json.JSONObject;

public class ApplyFilterAction extends Action {

  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_STEP = "step";

  private static final Logger LOG = Logger.getLogger(ApplyFilterAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    LOG.debug("Entering ApplyFilterAction...");
    try {
      String filterName = request.getParameter(PARAM_FILTER);
      if (filterName == null)
        throw new WdkUserException("Required filter parameter is missing.");
      String strStepId = request.getParameter(PARAM_STEP);
      if (strStepId == null)
        throw new WdkUserException("Required step parameter is missing.");
      long stepId = Long.valueOf(strStepId);

      UserBean user = ActionUtility.getUser(request);
 
      // before changing step, need to check if strategy is saved, if yes, make a copy.
      String strStrategyId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
      if (strStrategyId != null && !strStrategyId.isEmpty()) {
        long strategyId = Long.valueOf(strStrategyId.split("_", 2)[0]);
        StrategyBean strategy = new StrategyBean(user, StepUtilities.getStrategy(user.getUser(), strategyId));
        if (strategy.getIsSaved()) {
          // cannot modify saved strategy directly, will need to create a copy, and change the steps of the
          // copy instead.
          Map<Long, Long> stepIdMap = new HashMap<>();
          strategy = user.copyStrategy(strategy, stepIdMap, strategy.getName());
          // map the old step id to the new one.
          stepId = stepIdMap.get(stepId);
        }
      }
      
      StepBean step = new StepBean(user, StepUtilities.getStepByValidStepId(user.getUser(), stepId));
      JSONObject options = prepareOptions(request);
      AnswerValueBean answer = step.getAnswerValue();
      QuestionBean question = answer.getQuestion();
      Filter filter = question.getFilter(filterName);
      
      LOG.debug("Got filter: " + filter.getKey() + ", options=" + options);
      
      step.addFilterOption(filter.getKey(), options);
      step.saveParamFilters();

      ActionForward showApplication = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);

      LOG.debug("Foward to " + CConstants.SHOW_APPLICATION_MAPKEY + ", " + showApplication);

      StringBuffer url = new StringBuffer(showApplication.getPath());
      //String state = request.getParameter(CConstants.WDK_STATE_KEY);
      //url.append("?state=" + FormatUtil.urlEncodeUtf8(state));

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      LOG.debug("Leaving ApplyFilterAction.");
      return forward;
    }
    catch (Exception ex) {
      LOG.error(ex.getMessage(), ex);
      ex.printStackTrace();
      throw ex;
    }
  }

  private JSONObject prepareOptions(HttpServletRequest request) {
    JSONObject jsOptions = new JSONObject();
    Enumeration<?> names = request.getParameterNames();
    while (names.hasMoreElements()) {
      String name = (String)names.nextElement();
      if (name.equals(PARAM_FILTER) || name.equals(PARAM_STEP))
        continue;
      String[] values = request.getParameterValues(name);
      JSONArray jsValues = new JSONArray();
      for (String value : values) {
        jsValues.put(value);
      }
      jsOptions.put(name, jsValues);
    }
    return jsOptions;
  }
}
