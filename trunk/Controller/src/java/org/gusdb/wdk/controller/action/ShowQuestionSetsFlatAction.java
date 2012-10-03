package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
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

public class ShowQuestionSetsFlatAction extends ShowQuestionSetsAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);

        QuestionForm questionForm = (QuestionForm) form;
        String questionName = questionForm.getQuestionFullName();
        QuestionBean question = wdkModel.getQuestion(questionName);
        ShowQuestionAction.prepareQuestionForm(question, servlet, request,
                questionForm);

        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                + CConstants.WDK_PAGES_DIR;
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_QUESTIONSETS_FLAT_PAGE;

        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile);
        } else {
            forward = mapping.findForward(CConstants.SHOW_QUESTIONSETSFLAT_MAPKEY);
        }

        sessionStart(request, getServlet());

        return forward;
    }
}
