package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;

/**
 * form bean for showing a wdk question from a question set
 */

public class QuestionForm extends MapActionForm {

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
        Map<String, String> paramValues = new LinkedHashMap<String, String>();
        for (String paramName : params.keySet()) {
            String prompt = paramName;
            try {
                ParamBean param = params.get(paramName);
                param.setUser(user);
                
                if (param instanceof DatasetParamBean) {
                    DatasetParamBean datasetParam = (DatasetParamBean)param;
                    processDatasetValue(request, user, datasetParam);
                }
                prompt = param.getPrompt();
                String rawOrDependentValue = (String) getValue(paramName);
                String dependentValue = param.rawOrDependentValueToDependentValue(
                        user, rawOrDependentValue);

                // cannot validate datasetParam here
                if (!(param instanceof DatasetParamBean))
                    param.validate(user, dependentValue);
                paramValues.put(paramName, dependentValue);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                ActionMessage message = new ActionMessage("mapped.properties",
                        prompt, ex.getMessage());
                errors.add(ActionErrors.GLOBAL_MESSAGE, message);
            }
        }

        // validate weight
        boolean hasWeight = (weight != null && weight.length() > 0);
        int weightValue = Utilities.DEFAULT_WEIGHT;
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

        try {
            AnswerValueBean answerValue = wdkQuestion.makeAnswerValue(user,
                    paramValues, weightValue);
            logger.debug("Test run search [" + questionFullName
                    + "] and get # of results: " + answerValue.getResultSize());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            ActionMessage message = new ActionMessage("mapped.properties",
                    "Failed to run search", ex.getMessage());
            errors.add(ActionErrors.GLOBAL_MESSAGE, message);
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
     * @param customName
     *            the customName to set
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    private void processDatasetValue(HttpServletRequest request,
            UserBean user, DatasetParamBean param) throws WdkUserException,
            IOException, WdkModelException, NoSuchAlgorithmException,
            SQLException, JSONException {
        String paramName = param.getName();
        // get the input type
        String type = request.getParameter(paramName + "_type");
        if (type == null)
            throw new WdkUserException("Missing input parameter: " + paramName
                    + "_type.");

        RecordClassBean recordClass = ((DatasetParamBean) param).getRecordClass();
        String data = null;
        String uploadFile = "";
        if (type.equalsIgnoreCase("data")) {
            data = request.getParameter(paramName + "_data");
        } else if (type.equalsIgnoreCase("file")) {
            FormFile file = (FormFile) getValue(paramName + "_file");
            uploadFile = file.getFileName();
            logger.debug("upload file: " + uploadFile);
            data = new String(file.getFileData());
        } else if (type.equalsIgnoreCase("basket")) {
            data = user.getBasket(recordClass);
        } else if (type.equals("strategy")) {
            String strId = request.getParameter(paramName + "_strategy");
            int displayId = Integer.parseInt(strId);
            StrategyBean strategy = user.getStrategy(displayId);
            StepBean step = strategy.getLatestStep();
            data = step.getAnswerValue().getAllIdList();
        }

        logger.debug("dataset data: '" + data + "'");
        if (data != null && data.trim().length() > 0) {
            // if data exists, creates a dataset, and store the id into value
            // field.
            DatasetBean dataset = user.createDataset(recordClass, uploadFile,
                    data);
            String dsId = Integer.toString(dataset.getUserDatasetId());
            setValue(paramName, dsId);
        }
    }
}
