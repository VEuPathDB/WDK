package org.gusdb.wdk.controller.action;

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
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class ToggleFilterAction extends Action {

  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_STEP = "step";
  public static final String PARAM_DISABLED = "disabled";
  
  public static final String ATTR_SUMMARY = "summary";
  
  private static final Logger LOG = Logger.getLogger(ToggleFilterAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    LOG.debug("Entering ToggleFilterAction...");
    
    String filterName = request.getParameter(PARAM_FILTER);
    if (filterName == null)
      throw new WdkUserException("Required filter parameter is missing.");
    String stepId = request.getParameter(PARAM_STEP);
    if (stepId == null)
      throw new WdkUserException("Required step parameter is missing.");
    String strDisabled = request.getParameter(PARAM_DISABLED);
    if (strDisabled == null)
      throw new WdkUserException("Required disabled parameter is missing.");
    boolean disabled = Boolean.valueOf(strDisabled);
    
    UserBean user = ActionUtility.getUser(servlet, request);
    StepBean step = user.getStep(Integer.valueOf(stepId));


    // before changing step, need to check if strategy is saved, if yes, make a copy.
    String strStrategyId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
    if (strStrategyId != null && !strStrategyId.isEmpty()) {
      int strategyId = Integer.valueOf(strStrategyId.split("_", 2)[0]);
      StrategyBean strategy = user.getStrategy(strategyId);
      if (strategy.getIsSaved())
        strategy.update(false);
    }

    step.getFilterOptions().getFilterOption(filterName).setDisabled(disabled);
    step.saveParamFilters();
    
    ActionForward showApplication = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);

    LOG.debug("Filter " + filterName + ": " + (disabled ? "disabled" : "enabled"));
    LOG.debug("Foward to " + CConstants.SHOW_APPLICATION_MAPKEY + ", " + showApplication);

    StringBuffer url = new StringBuffer(showApplication.getPath());
    // String state = request.getParameter(CConstants.WDK_STATE_KEY);
    // url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

    ActionForward forward = new ActionForward(url.toString());
    forward.setRedirect(true);
    LOG.debug("Leaving ToggleFilterAction.");
    return forward;
  }
}
