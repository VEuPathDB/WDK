package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.QuestionBean;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ProcessQuestionAction extends ShowQuestionAction {

    private static final Logger logger = Logger.getLogger(ProcessQuestionAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionForward forward = null;
        String submitAction = request.getParameter(CConstants.PQ_SUBMIT_KEY);
        logger.debug("submitAction=" + submitAction);
        if (submitAction.equals(CConstants.PQ_SUBMIT_GET_ANSWER)) {
            if (request.getSession().getAttribute(CConstants.QUESTIONFORM_KEY) == null) {
                String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
                QuestionBean wdkQuestion = getQuestionByFullName(qFullName);
                prepareQuestionForm(wdkQuestion, request);
                logger.debug("DEBUG: PQA, mended the session");
            }
            forward = mapping.findForward(CConstants.PQ_SHOW_SUMMARY_MAPKEY);
        } else if (submitAction.equals(CConstants.PQ_SUBMIT_EXPAND_QUERY)) {
            forward = mapping.findForward(CConstants.PQ_START_BOOLEAN_MAPKEY);
        }
        return forward;
    }
}
