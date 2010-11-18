package org.gusdb.wdk.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * form bean for showing a wdk question from a question set
 */

public class QuestionForm extends MapActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = -7848685794514383434L;
    private static final Logger logger = Logger.getLogger(QuestionForm.class);

    private String questionFullName;
    private QuestionBean question;
    private boolean validating = true;
    private boolean paramsFilled = false;
    private String weight;
    private String customName;

    /**
     * validate the properties that have been sent from the HTTP request, and
     * return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
        logger.debug("start form validation...");
        ActionErrors errors = super.validate(mapping, request);
        if (errors == null) errors = new ActionErrors();

        UserBean user = ActionUtility.getUser(servlet, request);

        // set the question name into request
        request.setAttribute(CConstants.QUESTIONFORM_KEY, this);
        request.setAttribute(CConstants.QUESTION_FULLNAME_PARAM,
                questionFullName);

        if (!validating) return errors;

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
        for (String paramName : params.keySet()) {
            String prompt = paramName;
            try {
                ParamBean param = params.get(paramName);
                param.setUser(user);
                prompt = param.getPrompt();
                String rawOrDependentValue = (String) getValue(paramName);
                String dependentValue = param.rawOrDependentValueToDependentValue(
                        user, rawOrDependentValue);

                // cannot validate datasetParam here
                if (!(param instanceof DatasetParamBean))
                    param.validate(user, dependentValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                ActionMessage message = new ActionMessage("mapped.properties",
                        prompt, ex.getMessage());
                errors.add(ActionErrors.GLOBAL_MESSAGE, message);
            }
        }

        // validate weight
        boolean hasWeight = (weight != null && weight.length() > 0);
        if (hasWeight) {
            String message = null;
            if (!weight.matches("[\\-\\+]?\\d+")) {
                message = "Invalid weight value: '" + weight
                        + "'. Only integer numbers are allowed.";
            } else if (weight.length() > 9) {
                message = "Weight number is too big: " + weight;
            }
            if (message != null) {
                ActionMessage am = new ActionMessage("mapped.properties",
                        "Assigned weight", message);
                errors.add(ActionErrors.GLOBAL_MESSAGE, am);
            }
        }

        logger.debug("finish validation...");
        return errors;
    }

    public void setQuestionFullName(String questionFullName) {
        this.questionFullName = questionFullName;
    }

    public String getQuestionFullName() {
        return this.questionFullName;
    }

    public void setQuestion(QuestionBean question) {
        this.question = question;
        this.questionFullName = question.getFullName();
    }

    public QuestionBean getQuestion() {
        if (question == null) {
            if (questionFullName == null) return null;
            int dotI = questionFullName.indexOf('.');
            String qSetName = questionFullName.substring(0, dotI);
            String qName = questionFullName.substring(dotI + 1,
                    questionFullName.length());

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

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWeight() {
        return weight;
    }

    @Override
    public Object getValue(String key) {
        return getValueOrArray(key);
    }

    /**
     * @return the customName
     */
    public String getCustomName() {
        return customName;
    }

    /**
     * @param customName the customName to set
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

}
