package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;
import org.json.JSONObject;

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
    private int internalId;
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
    private boolean valid = true;

    private Integer estimateSize;

    private String filterName;
    private AnswerValue answerValue;

    private Map<String, String> paramValues = new LinkedHashMap<String, String>();

    Step(StepFactory stepFactory, User user, int displayId, int internalId) {
        this.stepFactory = stepFactory;
        this.user = user;
        this.displayId = displayId;
        this.internalId = internalId;
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
        if (answerValue != null || estimateSize == null) {
            this.estimateSize = getAnswerValue().getResultSize();
        }
        return estimateSize;
    }

    // Needs to be updated for transforms
    public String getOperation() throws WdkModelException, WdkUserException {
        if (isFirstStep()) {
            throw new WdkUserException(
                    "getOperation cannot be called on the first Step.");
        }
        BooleanQuery query = (BooleanQuery) getQuestion().getQuery();
        StringParam operator = query.getOperatorParam();
        return this.paramValues.get(operator.getName());
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

    public boolean isFirstStep() {
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
     * @throws WdkModelException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public String getCustomName() throws WdkModelException {
        String name = customName;
        if (name == null || name.length() == 0) {
            name = getQuestion().getDisplayName();
        }
        if (name == null) name = getQuestionName();
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
     * @throws WdkModelException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public String getShortDisplayName() throws WdkModelException {
        String name = customName;

        if (name == null) name = getQuestion().getShortDisplayName();
        if (name != null) {
            // remove script injections
            name = name.replaceAll("<.+?>", " ");
            name = name.replaceAll("['\"]", " ");
            name = name.trim().replaceAll("\\s+", " ");
            if (name.length() > 4000) name = name.substring(0, 4000);
        }
        return name;
    }

    public String getDisplayName() throws WdkModelException {
        return getQuestion().getDisplayName();
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
    int getInternalId() {
        return internalId;
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
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public int getEstimateSize() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        if (estimateSize == null)
            estimateSize = getAnswerValue().getResultSize();
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
     * @throws WdkModelException
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public boolean isCombined() throws WdkModelException {
        return getQuestion().getQuery().isCombined();
    }

    /**
     * @return Returns whether this Step is a transform
     * @throws WdkModelException
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public boolean isTransform() throws WdkModelException {
        return getQuestion().getQuery().isTransform();
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

    public String getDescription() throws WdkModelException {
        return getQuestion().getDescription();
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

    public boolean isCollapsible() throws WdkModelException {
        if (isCollapsible) return true;
        // it is true if the step is a branch
        return (getParentStep() != null && isCombined());
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

    public Map<String, String> getParamNames() throws WdkModelException {
        Map<String, Param> params = getQuestion().getQuery().getParamMap();
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
        return (answer != null) ? answer.getQuestionName() : null;
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
        if (step.isFirstStep()) {
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
     * @return the paramErrors
     */
    public Map<String, String> getParamValues() {
        return new LinkedHashMap<String, String>(paramValues);
    }

    /**
     * @param paramErrors
     *            the paramErrors to set
     */
    public void setParamValues(Map<String, String> paramValues)
            throws WdkModelException {
        this.paramValues = new LinkedHashMap<String, String>(paramValues);
        // make sure the params do exist
        Map<String, Param> params = getQuestion().getParamMap();
        for (String paramName : paramValues.keySet()) {
            if (!params.containsKey(paramName)) {
                this.valid = false;
                break;
            }
        }
    }

    public String getType() {
        try {
            return getQuestion().getRecordClass().getFullName();
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            return "Unknown Type";
        }
    }

    public int getIndexFromId(int displayId) throws WdkUserException {
        Step[] steps = getAllSteps();
        for (int i = 0; i < steps.length; ++i) {
            if (steps[i].getDisplayId() == displayId
                    || (steps[i].getChildStep() != null && steps[i].getChildStep().getDisplayId() == displayId)) {
                return i;
            }
        }
        throw new WdkUserException("Id not found!");
    }

    public Step createStep(String filterName) throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        RecordClass recordClass = getQuestion().getRecordClass();
        AnswerFilterInstance filter = recordClass.getFilter(filterName);
        return createStep(filter);
    }

    public Step createStep(AnswerFilterInstance filter)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        AnswerFilterInstance oldFilter = getFilter();
        if (filter == null && oldFilter == null) return this;
        if (filter != null && oldFilter != null
                && filter.getName().equals(oldFilter.getName())) return this;

        // create new steps
        Question question = getQuestion();
        Map<String, String> params = getParamValues();
        int startIndex = getAnswerValue().getStartIndex();
        int endIndex = getAnswerValue().getEndIndex();
        Step step = user.createStep(question, params, filter, startIndex,
                endIndex, isDeleted, false);
        step.collapsedName = collapsedName;
        step.customName = customName;
        step.isCollapsible = isCollapsible;
        step.update(false);
        return step;
    }

    /**
     * deep clone a step, the step will get a new id, and if the step contains
     * other sub-steps, all those sub steps are cloned recursively.
     * 
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * 
     */
    public Step deepClone() throws NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException, SQLException {
        Step step;
        AnswerValue answerValue = getAnswerValue();
        if (!isCombined()) {
            step = user.createStep(answerValue, isDeleted);
        } else {
            Question question = getQuestion();
            Map<String, String> paramValues = new LinkedHashMap<String, String>();
            Map<String, Param> params = question.getParamMap();
            for (String paramName : this.paramValues.keySet()) {
                Param param = params.get(paramName);
                String paramValue = this.paramValues.get(paramName);
                if (param instanceof AnswerParam) {
                    Step child = user.getStep(Integer.parseInt(paramValue));
                    child = child.deepClone();
                    paramValue = Integer.toString(child.getDisplayId());
                }
                paramValues.put(paramName, paramValue);
            }
            AnswerFilterInstance filter = getFilter();
            int pageStart = answerValue.getStartIndex();
            int pageEnd = answerValue.getEndIndex();
            step = user.createStep(question, paramValues, filter, pageStart,
                    pageEnd, isDeleted, false);
        }
        step.collapsedName = collapsedName;
        step.customName = customName;
        step.isCollapsible = isCollapsible;
        step.update(false);
        return step;
    }

    public boolean isFiltered() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        AnswerFilterInstance filter = getFilter();
        if (filter == null) return false;

        RecordClass recordClass = getQuestion().getRecordClass();
        AnswerFilterInstance defaultFilter = recordClass.getDefaultFilter();
        if (defaultFilter == null) return true;

        return (!defaultFilter.getName().equals(filter.getName()));
    }

    public String getFilterDisplayName() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        AnswerFilterInstance filter = getFilter();
        return (filter != null) ? filter.getDisplayName() : filterName;
    }

    public Step getFirstStep() {
        Step step = this;
        while (step.getPreviousStep() != null)
            step = step.getPreviousStep();
        return step;
    }

    public boolean isBoolean() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return getQuestion().getQuery().isBoolean();
    }

    public JSONObject getJSONContent(int strategyId) throws JSONException {
        JSONObject jsStep = new JSONObject();
        jsStep.put("id", this.displayId);
        jsStep.put("customName", this.customName);
        jsStep.put("answer", this.answer.getAnswerChecksum());
        jsStep.put("collapsed", this.isCollapsible);
        jsStep.put("collapsedName", this.collapsedName);
        jsStep.put("deleted", isDeleted);
        if (this.previousStep != null) {
            jsStep.put("previous", previousStep.getJSONContent(strategyId));
        }
        if (this.childStep != null) {
            jsStep.put("child", childStep.getJSONContent(strategyId));
        }
        if (this.isCollapsible) { // a sub-strategy, needs to get order number
            String subStratId = strategyId + "_" + this.displayId;
            Integer order = user.getStrategyOrder(subStratId);
            if (order == null) order = 0; // the sub-strategy is not displayed
            jsStep.put("order", order);
        }
        return jsStep;
    }

    public Question getQuestion() throws WdkModelException {
        return answer.getQuestion();
    }

    public AnswerFilterInstance getFilter() throws WdkModelException {
        return getQuestion().getRecordClass().getFilter(filterName);
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public AnswerValue getAnswerValue() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        if (answerValue == null) {
            Question question = getQuestion();
            Map<String, Boolean> sortingMap = user.getSortingAttributes(question.getFullName());
            int endIndex = user.getItemsPerPage();
            answerValue = question.makeAnswerValue(user, paramValues, 1,
                    endIndex, sortingMap, getFilter());
        }
        return answerValue;
    }

    public String getAnswerKey() {
        String key = answer.getAnswerChecksum();
        if (filterName != null) key += ":" + filterName;
        return key;
    }

    public boolean isUseBooleanFilter() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        if (!isBoolean()) return false;
        BooleanQuery query = (BooleanQuery) getQuestion().getQuery();
        String paramName = query.getUseBooleanFilter().getName();
        String strBooleanFlag = (String) paramValues.get(paramName);
        return Boolean.parseBoolean(strBooleanFlag);
    }

    void setAnswerValue(AnswerValue answerValue) {
        this.answerValue = answerValue;
    }

    public void resetAnswerValue() {
        this.answerValue = null;
    }

    public String getDisplayType() {
        try {
            return getQuestion().getRecordClass().getType();
        } catch (WdkModelException ex) {
            return getType();
        }
    }
}
