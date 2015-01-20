package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a flat display of
 * QuestionSets is needed. It 1) gets all questions in all questionSets from the
 * WDK model 2) forwards control to a jsp page that displays all questions in
 * all questionSets
 */

public class ProcessQuestionSetsFlatAction extends ShowQuestionAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        WdkModelBean wdkModel = ActionUtility.getWdkModel(getServlet());
        
        QuestionForm qForm = (QuestionForm) form;
        String questionName = qForm.getQuestionFullName();
        wdkModel.validateQuestionFullName(questionName);
        QuestionBean wdkQuestion = wdkModel.getQuestion(questionName);

        ShowQuestionAction.prepareQuestionForm(wdkQuestion, servlet, request, qForm);

        request.setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
        request.setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);
        request.setAttribute("parentURI", request.getRequestURI());

        ActionForward forward = mapping.findForward(CConstants.PROCESS_QUESTIONSETSFLAT_MAPKEY);

        return forward;
    }
}
