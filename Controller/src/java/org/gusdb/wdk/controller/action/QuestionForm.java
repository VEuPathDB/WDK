package org.gusdb.wdk.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * form bean for showing a wdk question from a question set
 */

public class QuestionForm extends QuestionSetForm {

    /**
     * 
     */
    private static final long serialVersionUID = -7848685794514383434L;
    private QuestionBean question = null;
    private boolean validating = true;
    private boolean paramsFilled = false;

    /**
     * validate the properties that have been sent from the HTTP request, and
     * return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
        UserBean user = ActionUtility.getUser(servlet, request);

        // set the question name into request
        request.setAttribute(CConstants.QUESTIONSETFORM_KEY, this);
        request.setAttribute(CConstants.QUESTION_FULLNAME_PARAM, qFullName);

        ActionErrors errors = new ActionErrors();
        if (!validating) {
            return errors;
        }

        String clicked = request.getParameter(CConstants.PQ_SUBMIT_KEY);
        if (clicked != null
                && clicked.equals(CConstants.PQ_SUBMIT_EXPAND_QUERY)) {
            return errors;
        }

        QuestionBean wdkQuestion = getQuestion();
        if (wdkQuestion == null) {
            return errors;
        }

        Map<String, ParamBean> params = wdkQuestion.getParamsMap();
        Map<String, String> paramValues = getMyProps();
        for (String paramName : params.keySet()) {
            String prompt = paramName;
            try {
                ParamBean param = params.get(paramName);
                param.setUser(user);
                prompt = param.getPrompt();
                String rawOrDependentValue = paramValues.get(paramName);
                String dependentValue = param.rawOrDependentValueToDependentValue(
                        user, rawOrDependentValue);

                // cannot validate datasetParam here
                if (!(param instanceof DatasetParamBean))
                    param.validate(user, dependentValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                ActionError error = new ActionError("mapped.properties",
                        prompt, ex.getMessage());
                errors.add(ActionErrors.GLOBAL_MESSAGE, error);
            }
        }
        return errors;
    }

    public void setQuestion(QuestionBean question) {
        this.question = question;
    }

    public QuestionBean getQuestion() {
        if (question == null) {
            if (qFullName == null) return null;
            int dotI = qFullName.indexOf('.');
            String qSetName = qFullName.substring(0, dotI);
            String qName = qFullName.substring(dotI + 1, qFullName.length());

            WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                    CConstants.WDK_MODEL_KEY);

            QuestionSetBean wdkQuestionSet = (QuestionSetBean) wdkModel.getQuestionSetsMap().get(
                    qSetName);
            if (wdkQuestionSet == null) return null;
            question = (QuestionBean) wdkQuestionSet.getQuestionsMap().get(
                    qName);
        }
        return question;
    }

    public void setNonValidating() {
        validating = false;
    }

    public void setParamsFilled(boolean paramsFilled) {
        this.paramsFilled = paramsFilled;
    }

    public boolean getParamsFilled() {
        return paramsFilled;
    }
}
