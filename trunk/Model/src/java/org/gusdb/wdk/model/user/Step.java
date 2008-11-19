package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.BooleanQueryInstance;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;

/**
 * @author Charles Treatman
 * 
 */
public class Step {

    public static final String INTERNAL_AND = "INTERSECT";
    public static final String INTERNAL_OR = "UNION";
    public static final String INTERNAL_NOT = "MINUS";

    private StepFactory stepFactory;
    private User user;
    private int displayId;
    private int stepId;
    private Date createdTime;
    private Date lastRunTime;
    private String customName;
    private Answer answer = null;
    private boolean isDeleted;
    private boolean isCollapsible = false;
    private String collapsedName = null;
    private String version;

    private Step nextStep = null;
    private Step previousStep = null;
    private Step parentStep = null;
    private Step childStep = null;

    private String booleanExpression;
    private boolean valid;

    private Integer estimateSize;

    private Map<String, String> displayParams = new LinkedHashMap<String, String>();

    Step(StepFactory stepFactory, User user, int displayId, int stepId) {
        this.stepFactory = stepFactory;
        this.user = user;
        this.displayId = displayId;
        this.stepId = stepId;
        isDeleted = false;
    }

    public Step getPreviousStep() {
        return previousStep;
    }

    public Step getNextStep() {
        return nextStep;
    }

    public Step getParentStep() {
        return parentStep;
    }

    public Step getChildStep() {
        return childStep;
    }

    public int getResultSize() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        return answer.getAnswerValue().getResultSize();
    }

    // Needs to be updated for transforms
    public String getOperation() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        if (getIsFirstStep()) {
            throw new WdkUserException(
                    "getOperation cannot be called on the first Step.");
        }
        AnswerValue answerValue = answer.getAnswerValue();
        BooleanQueryInstance idQueryInstance = (BooleanQueryInstance) answerValue.getIdsQueryInstance();
        BooleanQuery idQuery = (BooleanQuery) idQueryInstance.getQuery();
        StringParam operator = idQuery.getOperatorParam();
        return idQueryInstance.getValues().get(operator.getName());
    }

    public void setParentStep(Step parentStep) {
        this.parentStep = parentStep;
    }

    public void setChildStep(Step childStep) {
        this.childStep = childStep;
    }

    public void setNextStep(Step nextStep) {
        this.nextStep = nextStep;
    }

    public void setPreviousStep(Step previousStep) {
        this.previousStep = previousStep;
    }

    public boolean getIsFirstStep() {
        return (previousStep == null);
    }

    public User getUser() {
        return user;
    }

    /**
     * @return Returns the createTime.
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createTime
     *            The createTime to set.
     */
    void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getBaseCustomName() {
        return customName;
    }

    /**
     * @return Returns the customName. If no custom name set before, it will
     *         return the default name provided by the underline AnswerValue - a
     *         combination of question's full name, parameter names and values.
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public String getCustomName() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        String name = customName;
        if (name == null || name.length() == 0) {
            if (answer.getAnswerValue() != null) {
                name = answer.getAnswerValue().getQuestion().getDisplayName();
            }
        }
        if (name == null) name = answer.getQuestionName();
        if (name != null) {
            // remove script injections
            name = name.replaceAll("<.+?>", " ");
            name = name.replaceAll("['\"]", " ");
            name = name.trim().replaceAll("\\s+", " ");
            if (name.length() > 4000) name = name.substring(0, 4000);
        }
        return name;
    }

    /**
     * @return Returns the custom name, if it is set. Otherwise, returns the
     *         short display name for the underlying question.
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public String getShortDisplayName() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        String name = customName;

        if (name == null)
            name = answer.getAnswerValue().getQuestion().getShortDisplayName();
        if (name != null) {
            // remove script injections
            name = name.replaceAll("<.+?>", " ");
            name = name.replaceAll("['\"]", " ");
            name = name.trim().replaceAll("\\s+", " ");
            if (name.length() > 4000) name = name.substring(0, 4000);
        }
        return name;
    }

    /**
     * @param customName
     *            The customName to set.
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    /**
     * @return Returns the displayId.
     */
    public int getDisplayId() {
        return displayId;
    }

    /**
     * @return Returns the stepId.
     */
    int getStepId() {
        return stepId;
    }

    /**
     * @param answer
     *            The answer to set.
     */
    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    /**
     * @return Returns the answer.
     */
    public Answer getAnswer() {
        return answer;
    }

    public int getAnswerId() {
        return answer.getAnswerId();
    }

    /**
     * @return Returns the estimateSize.
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public int getEstimateSize() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        if (estimateSize == null)
            estimateSize = answer.getAnswerValue().getResultSize();
        return estimateSize;
    }

    /**
     * @param estimateSize
     *            The estimateSize to set.
     */
    public void setEstimateSize(int estimateSize) {
        this.estimateSize = estimateSize;
    }

    /**
     * @return Returns the lastRunTime.
     */
    public Date getLastRunTime() {
        return lastRunTime;
    }

    /**
     * @param lastRunTime
     *            The lastRunTime to set.
     */
    public void setLastRunTime(Date lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    /**
     * @return Returns the isBoolean.
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public boolean isCombined() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return answer.getAnswerValue().isCombined();
    }

    /**
     * @return Returns whether this Step is a transform
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public boolean isTransform() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return answer.getAnswerValue().isTransform();
    }

    /**
     * @return Returns the booleanExpression.
     */
    public String getBooleanExpression() {
        return booleanExpression;
    }

    /**
     * @param booleanExpression
     *            The booleanExpression to set.
     */
    public void setBooleanExpression(String booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    public void update() throws WdkUserException, NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException {
        stepFactory.updateStep(user, this, true);
    }

    public void update(boolean updateTime) throws WdkUserException,
            NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException {
        stepFactory.updateStep(user, this, updateTime);
    }

    public String getDescription() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return answer.getAnswerValue().getQuestion().getDescription();
    }

    /**
     * @return Returns the isDeleted.
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * @param isDeleted
     *            The isDeleted to set.
     */
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isCollapsible() {
        return isCollapsible;
    }

    public void setCollapsible(boolean isCollapsible) {
        this.isCollapsible = isCollapsible;
    }

    public String getCollapsedName() {
        return collapsedName;
    }

    public void setCollapsedName(String collapsedName) {
        this.collapsedName = collapsedName;
    }

    /**
     * @return the isValid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @param isValid
     *            the isValid to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getParams() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return answer.getAnswerValue().getIdsQueryInstance().getValues();
    }

    public Map<String, String> getParamNames() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        Map<String, Param> params = answer.getAnswerValue().getIdsQueryInstance().getQuery().getParamMap();
        Map<String, String> names = new LinkedHashMap<String, String>();
        for (Param param : params.values()) {
            names.put(param.getName(), param.getPrompt());
        }
        return names;
    }

    void setQuestionName(String questionName) {
        answer.setQuestionName(questionName);
    }

    public String getQuestionName() {
        return answer.getQuestionName();
    }

    /* functions for navigating/manipulating step tree */
    public Step getStep(int index) {
        Step[] steps = getAllSteps();
        return steps[index];
    }

    public Step[] getAllSteps() {
        ArrayList<Step> allSteps = new ArrayList<Step>();
        allSteps = buildAllStepsArray(allSteps, this);
        return allSteps.toArray(new Step[allSteps.size()]);
    }

    public int getLength() {
        return getAllSteps().length;
    }

    private ArrayList<Step> buildAllStepsArray(ArrayList<Step> array, Step step) {
        if (step.getIsFirstStep()) {
            array.add(step);
        } else {
            array = buildAllStepsArray(array, step.getPreviousStep());
            array.add(step);
        }
        return array;
    }

    public void addStep(Step step) throws WdkUserException {
        step.setPreviousStep(this);
        this.setNextStep(step);
    }

    public Step getStepByDisplayId(int displayId) {
        Step target;
        if (this.displayId == displayId) {
            return this;
        }
        if (childStep != null) {
            target = childStep.getStepByDisplayId(displayId);
            if (target != null) {
                return target;
            }
        }
        if (previousStep != null) {
            target = previousStep.getStepByDisplayId(displayId);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /**
     * @return the displayParams
     */
    public Map<String, String> getDisplayParams() {
        return new LinkedHashMap<String, String>(displayParams);
    }

    /**
     * @param displayParams
     *            the displayParams to set
     */
    public void setDisplayParams(Map<String, String> displayParams) {
        this.displayParams = new LinkedHashMap<String, String>(displayParams);
    }

    public String getType() throws NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException, SQLException {
        Question question = getAnswer().getAnswerValue().getQuestion();
        return question.getRecordClass().getFullName();
    }
}
