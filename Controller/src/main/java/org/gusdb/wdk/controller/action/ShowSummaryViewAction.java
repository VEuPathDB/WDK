package org.gusdb.wdk.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.summary.DefaultSummaryViewHandler;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.answer.SummaryViewHandler;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class ShowSummaryViewAction extends Action {

    public static final String PARAM_STEP = "step";
    public static final String PARAM_VIEW = "view";

    public static final String ATTR_STEP = "wdkStep";
    public static final String ATTR_VIEW = "wdkView";
    public static final String ATTR_REQUEST_URI = "requestUri";

    private static final Logger logger = Logger.getLogger(ShowSummaryViewAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowSummaryViewAction");

        // get step
        UserBean wdkUser = ActionUtility.getUser(servlet, request);

        String strStep = request.getParameter(PARAM_STEP);
        if (strStep == null || strStep.length() == 0)
            throw new WdkUserException("Required step parameter is missing.");
        StepBean step;
        try {
          step = wdkUser.getStep(Integer.valueOf(strStep));
        } catch(NumberFormatException ex) {
          throw new WdkUserException("The step id is invalid: " + strStep);
        }

        request.setAttribute(ATTR_STEP, step);

        QuestionBean question = step.getQuestion();
        String viewName = request.getParameter(PARAM_VIEW);
        SummaryView view;
        if (viewName == null || viewName.length() == 0) {
            view = wdkUser.getCurrentSummaryView();
            if (view == null) view = question.getDefaultSummaryView();
        } else {
            Map<String, SummaryView> views = question.getSummaryViews();
            view = views.get(viewName);
            if (view == null)
                throw new WdkUserException("Invalid view name: '" + view + "'");

            wdkUser.setCurrentSummaryView(question, view);
        }

        // process the view handler
        SummaryViewHandler handler = view.getHandler();
        if (handler == null) {
          handler = new DefaultSummaryViewHandler();
        }
        ActionUtility.applyModel(request,
            handler.process(step.getStep(), request.getParameterMap()));

        logger.debug("request uri: " + request.getRequestURI());
        request.setAttribute(ATTR_REQUEST_URI, request.getRequestURI());

        logger.debug("summary view: " + view.getName());
        request.setAttribute(ATTR_VIEW, view);

        logger.debug("view=" + view.getName() + ", jsp=" + view.getJsp());
        ActionForward forward = new ActionForward(view.getJsp());

        logger.debug("Leaving ShowSummaryViewAction");
        return forward;
    }
}
