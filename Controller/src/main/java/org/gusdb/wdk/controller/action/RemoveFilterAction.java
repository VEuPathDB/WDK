package org.gusdb.wdk.controller.action;

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
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class RemoveFilterAction extends Action {

  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_STEP = "step";
  
  public static final String ATTR_SUMMARY = "summary";
  
  private static final Logger LOG = Logger.getLogger(RemoveFilterAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    LOG.debug("Entering RemoveFilterAction...");
 
    UserBean user = ActionUtility.getUser(servlet, request);

    String filterName = request.getParameter(PARAM_FILTER);
    if (filterName == null)
      throw new WdkUserException("Required filter parameter is missing.");
    String strStepId = request.getParameter(PARAM_STEP);
    if (strStepId == null)
      throw new WdkUserException("Required step parameter is missing.");
    int stepId = Integer.valueOf(strStepId);
    StepBean step ;
    
    // before changing step, need to check if strategy is saved, if yes, make a copy.
    String strStrategyId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
    if (strStrategyId != null && !strStrategyId.isEmpty()) {
      int strategyId = Integer.valueOf(strStrategyId.split("_", 2)[0]);
      StrategyBean strategy = user.getStrategy(strategyId);
      if (strategy.getIsSaved()) {
        Map<Integer, Integer> stepIdMap = new HashMap<>();
        strategy = user.copyStrategy(strategy, stepIdMap, strategy.getName());
        // map the old step id to the new one
        stepId = stepIdMap.get(stepId);
      }
      step = strategy.getStepById(stepId);
    } else step = user.getStep(stepId);

    AnswerValueBean answer = step.getAnswerValue();
    Filter filter = answer.getQuestion().getFilter(filterName);
 
    
    step.removeFilterOption(filter.getKey());
    step.saveParamFilters();
    
    ActionForward showApplication = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);

    LOG.debug("Foward to " + CConstants.SHOW_APPLICATION_MAPKEY + ", " + showApplication);

    StringBuffer url = new StringBuffer(showApplication.getPath());
    // String state = request.getParameter(CConstants.WDK_STATE_KEY);
    // url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

    ActionForward forward = new ActionForward(url.toString());
    forward.setRedirect(true);
    LOG.debug("Leaving RemoveFilterAction.");
    return forward;
  }
}
