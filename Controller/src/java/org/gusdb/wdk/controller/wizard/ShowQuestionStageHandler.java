package org.gusdb.wdk.controller.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.controller.action.QuestionForm;
import org.gusdb.wdk.controller.action.ShowQuestionAction;
import org.gusdb.wdk.controller.action.WizardForm;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class ShowQuestionStageHandler implements StageHandler {

    private static final String PARAM_QUESTION_NAME = "questionFullName";

    private static final String ATTR_QUESTION = "question";
    private static final String ATTR_ALLOW_BOOLEAN = "allowBoolean";

    private static final Logger logger = Logger.getLogger(ShowQuestionStageHandler.class);

    public Map<String, Object> execute(ActionServlet servlet,
            HttpServletRequest request, HttpServletResponse response,
            WizardForm wizardForm) throws Exception {
        logger.debug("Entering QuestionStageHandler....");

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        String questionName = request.getParameter(PARAM_QUESTION_NAME);
        QuestionBean question = wdkModel.getQuestion(questionName);

        // prepare question form
        QuestionForm questionForm = new QuestionForm();
        ShowQuestionAction.prepareQuestionForm(question, servlet, request,
                questionForm);
        wizardForm.copyFrom(questionForm);

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(ATTR_QUESTION, question);

        // get previous step
        StepBean previousStep = StageHandlerUtility.getPreviousStep(servlet,
                request, wizardForm);

        // set the previous step value
        String paramName = null;
        for (ParamBean param : question.getParams()) {
            // the previous step should be stored in the value of first
            // answer param.
            if (param instanceof AnswerParamBean) {
                paramName = param.getName();
                break;
            }
        }
        if (paramName != null) {
            attributes.put("value(" + paramName + ")", previousStep.getStepId());
        }
        // check if boolean is allowed
        String importType = question.getRecordClass().getFullName();
        boolean allowBoolean = importType.equals(previousStep.getType());
        logger.debug("allow boolean: " + allowBoolean);
        attributes.put(ATTR_ALLOW_BOOLEAN, allowBoolean);

        logger.debug("Leaving QuestionStageHandler....");
        return attributes;
    }
}
