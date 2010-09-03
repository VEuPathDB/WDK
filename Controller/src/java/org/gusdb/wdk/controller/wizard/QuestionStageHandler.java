package org.gusdb.wdk.controller.wizard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.controller.action.QuestionForm;
import org.gusdb.wdk.controller.action.ShowQuestionAction;
import org.gusdb.wdk.controller.action.WizardForm;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class QuestionStageHandler implements StageHandler {

    private static final String PARAM_QUESTION_NAME = "questionFullName";

    private static final String ATTR_QUESTION = "question";

    private static final Logger logger = Logger.getLogger(QuestionStageHandler.class);

    public void execute(ActionServlet servlet, HttpServletRequest request,
            HttpServletResponse response, WizardForm wizardForm)
            throws Exception {
        logger.debug("Entering QuestionStageHandler....");

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        String questionName = request.getParameter(PARAM_QUESTION_NAME);
        QuestionBean question = wdkModel.getQuestion(questionName);

        // prepare question form
        QuestionForm questionForm = new QuestionForm();
        ShowQuestionAction.prepareQuestionForm(question, servlet, request,
                questionForm);
        wizardForm.copyFrom(questionForm);

        request.setAttribute(ATTR_QUESTION, question);

        logger.debug("Leaving QuestionStageHandler....");
    }

}
