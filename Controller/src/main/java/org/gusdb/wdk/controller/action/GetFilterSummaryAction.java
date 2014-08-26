package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.FilterSummary;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class GetFilterSummaryAction extends Action {

  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_STEP = "step";
  
  public static final String ATTR_SUMMARY = "summary";
  
  private static final Logger LOG = Logger.getLogger(GetFilterSummaryAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    LOG.trace("Entering GetFilterSummaryAction...");
    
    String filterName = request.getParameter(PARAM_FILTER);
    if (filterName == null)
      throw new WdkUserException("Required filter parameter is missing.");
    String stepId = request.getParameter(PARAM_STEP);
    if (stepId == null)
      throw new WdkUserException("Required step parameter is missing.");
    
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    UserBean user = ActionUtility.getUser(servlet, request);
    StepBean step = user.getStep(Integer.valueOf(stepId));
    AnswerValueBean answer = step.getAnswerValue();
    Filter filter = answer.getQuestion().getFilter(filterName);
    FilterSummary summary = filter.getSummary(answer.getAnswerValue());
    
    request.setAttribute(ATTR_SUMMARY, summary);
    
    ActionForward forward = new ActionForward(filter.getView(), false);
    LOG.debug("Leaving GetFilterSummaryAction.");
    return forward;
  }
}
