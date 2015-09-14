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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class ProcessSummaryViewAction extends Action {

  private static final String PARAM_STEP = "step";
  private static final String PARAM_VIEW = "view";

  private static final String FORWARD_SHOW_SUMMARY_VIEW = "show-summary-view";

  private static Logger LOG = Logger.getLogger(ProcessSummaryViewAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
          throws Exception {
    LOG.debug("Entering ProcessSummaryViewAction...");

    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    UserBean user = ActionUtility.getUser(servlet, request);
    StepBean step = getStep(user, request);
    QuestionBean question = step.getQuestion();
    SummaryView summaryView = getSummaryView(question, request);
    Map<String, String[]> params = getParamMap(request);

    String newQueryString = summaryView.getHandler().processUpdate(
        step.getStep(), params, user.getUser(), wdkModel.getModel());

    if (isRequestFromBasket(request)) {
      return getBasketForward(mapping, question.getRecordClass());
    }

    // construct url to show summary action
    ActionForward showSummaryView = mapping.findForward(FORWARD_SHOW_SUMMARY_VIEW);
    StringBuilder url = new StringBuilder(showSummaryView.getPath())
        .append("?step=").append(step.getStepId())
        .append("&view=").append(summaryView.getName());

    // append handler's new query string if present
    if (newQueryString != null && !newQueryString.isEmpty()) {
      url.append(newQueryString.startsWith("&") ? "" : "&")
         .append(newQueryString);
    }

    ActionForward forward = new ActionForward(url.toString());
    forward.setRedirect(true);
    return forward;
  }

  private ActionForward getBasketForward(ActionMapping mapping, RecordClassBean recordClass) {
    ActionForward showBasket = mapping.findForward(CConstants.PQ_SHOW_BASKET_MAPKEY);
    StringBuilder url = new StringBuilder(showBasket.getPath());
    url.append("?recordClass=" + recordClass.getFullName());
    return new ActionForward(url.toString());
  }

  private boolean isRequestFromBasket(HttpServletRequest request) {
    String strBasket = request.getParameter("from_basket");
    boolean fromBasket = (strBasket != null && strBasket.equals("true"));
    LOG.debug("to basket: " + fromBasket);
    return fromBasket;
  }

  private SummaryView getSummaryView(QuestionBean question, HttpServletRequest request) throws WdkUserException {
    String viewName = request.getParameter(PARAM_VIEW);
    if (viewName == null)
      throw new WdkUserException("Parameter " + PARAM_VIEW + " is required.");
    SummaryView view = question.getSummaryViews().get(viewName);
    if (view == null)
      throw new WdkUserException("Question " + question.getName() + " has no summary view named " + view);
    return view;
  }

  private Map<String, String[]> getParamMap(HttpServletRequest request) {
    Map<String, String[]> params = new HashMap<>(request.getParameterMap());
    params.remove(PARAM_STEP);
    params.remove(PARAM_VIEW);
    return params;
  }

  private StepBean getStep(UserBean user, HttpServletRequest request) throws WdkUserException {
    String stepId = request.getParameter(PARAM_STEP);
    if (stepId == null || stepId.length() == 0)
      throw new WdkUserException("Parameter " + PARAM_STEP + " is required.");
    try {
      return user.getStep(Integer.parseInt(stepId));
    }
    catch (NumberFormatException nfe) {
      throw new WdkUserException("Parameter " + PARAM_STEP + " must be an integer.");
    }
    catch (WdkModelException wme) {
      throw new WdkUserException("No step exists with ID " + stepId);
    }
  }
}
