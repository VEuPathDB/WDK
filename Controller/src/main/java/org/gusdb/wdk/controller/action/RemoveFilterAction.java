package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;

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
    
    String filterName = request.getParameter(PARAM_FILTER);
    if (filterName == null)
      throw new WdkUserException("Required filter parameter is missing.");
    String stepId = request.getParameter(PARAM_STEP);
    if (stepId == null)
      throw new WdkUserException("Required step parameter is missing.");
    
    UserBean user = ActionUtility.getUser(servlet, request);
    StepBean step = user.getStep(Integer.valueOf(stepId));
    AnswerValueBean answer = step.getAnswerValue();
    Filter filter = answer.getQuestion().getFilter(filterName);
    step.removeFilterOption(filter.getKey());
    
    ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
    StringBuffer url = new StringBuffer(showStrategy.getPath());
    String state = request.getParameter(CConstants.WDK_STATE_KEY);
    url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

    ActionForward forward = new ActionForward(url.toString());
    forward.setRedirect(true);
    LOG.debug("Leaving RemoveFilterAction.");
    return forward;
  }
}
