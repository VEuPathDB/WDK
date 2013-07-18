package org.gusdb.wdk.controller.action;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
import org.gusdb.wdk.model.answer.SummaryViewHandler;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class ShowSummaryViewAction extends Action {

    public static final String PARAM_STEP = "step";
    public static final String PARAM_VIEW = "view";

    public static final String ATTR_STEP = "wdkStep";
    public static final String ATTR_VIEW = "wdkView";

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
        int stepId = Integer.valueOf(strStep);
        StepBean step = wdkUser.getStep(stepId);

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
        if (handler != null) {
            Map<String, Object> result = handler.process(step.getStep());
            for (String key : result.keySet()) {
                request.setAttribute(key, result.get(key));
            }
        }

        logger.debug("summary view: " + view.getName());
        request.setAttribute(ATTR_VIEW, view);

        ProcessPaging(request, step);

        logger.debug("view=" + view.getName() + ", jsp=" + view.getJsp());
        ActionForward forward = new ActionForward(view.getJsp());

        logger.debug("Leaving ShowSummaryViewAction");
        return forward;
    }

    private void ProcessPaging(HttpServletRequest request, StepBean step)
            throws WdkModelException {
        int start = ShowSummaryAction.getPageStart(request);
        int pageSize = ShowSummaryAction.getPageSize(request,
                step.getQuestion(), step.getUser());

        AnswerValueBean answerValue = step.getAnswerValue();
        int totalSize = answerValue.getResultSize();

        if (start > totalSize) {
            int pages = totalSize / pageSize;
            start = (pages * pageSize) + 1;
        }

        int end = start + pageSize - 1;

        answerValue.setPageIndex(start, end);

        List<String> editedParamNames = new ArrayList<String>();
        for (Enumeration<?> en = request.getParameterNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            if (!key.equals(CConstants.WDK_PAGE_SIZE_KEY)
                    && !key.equals(CConstants.WDK_ALT_PAGE_SIZE_KEY)
                    && !"start".equals(key) && !"pager.offset".equals(key)) {
                editedParamNames.add(key);
            }
        }

        request.setAttribute("wdk_paging_total", new Integer(totalSize));
        request.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
        request.setAttribute("wdk_paging_start", new Integer(start));
        request.setAttribute("wdk_paging_end", new Integer(end));
        request.setAttribute("wdk_paging_url", request.getRequestURI());
        request.setAttribute("wdk_paging_params", editedParamNames);

    }
}
