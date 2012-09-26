package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
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

    private static final Logger logger = Logger.getLogger(Step.class);

    private StepFactory stepFactory;
    private User user;
    private int displayId;
    private int internalId;
    private Date createdTime;
    private Date lastRunTime;
    private String customName;
    private Answer answer = null;
    private boolean deleted = false;
    private boolean collapsible = false;
    private String collapsedName = null;

    private Step nextStep = null;
    private Step previousStep = null;
    private Step parentStep = null;
    private Step childStep = null;

    private String booleanExpression;
    private boolean valid = true;
    private String validationMessage;

    private int estimateSize = 0;

    private String filterName;
    private AnswerValue answerValue;

    private Map<String, String> paramValues = new LinkedHashMap<String, String>();

    private int assignedWeight;

    private int previousStepId;
    private int childStepId;

    private boolean revisable = true;
    private Exception exception;

    Step(StepFactory stepFactory, User user, int displayId, int internalId) {
        this.stepFactory = stepFactory;
        this.user = user;
        this.displayId = displayId;
        this.internalId = internalId;
        deleted = false;
        assignedWeight = 0;
    }

    public Step getPreviousStep() throws WdkUserException, WdkModelException,
            SQLException {
        if (previousStep == null && previousStepId != 0)
            setPreviousStep(stepFactory.loadStep(user, previousStepId));
        return previousStep;
    }

    public Step getNextStep() {
        return nextStep;
    }

    public Step getParentStep() {
        return parentStep;
    }

    public Step getParentOrNextStep() {
        return (nextStep != null) ? nextStep : parentStep;
    }

    public Step getChildStep() throws WdkUserException, WdkModelException,
            SQLException {
        if (childStep == null && childStepId != 0)
            setChildStep(stepFactory.loadStep(user, childStepId));
        return childStep;
    }

    public int getAnswerParamCount() {
        try {
            return getQuestion().getQuery().getAnswerParamCount();
        } catch(WdkModelException ex) {
            return 0;
        }
    }

    public int getResultSize() {
        try {
            this.estimateSize = getAnswerValue().getResultSize();
        } catch (Exception ex) {
            valid = false;
            ex.printStackTrace();
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
        if (parentStep != null) {
            parentStep.childStep = this;
            parentStep.childStepId = displayId;
        }
    }

    public void setChildStep(Step childStep) {
        this.childStep = childStep;
        if (childStep != null) {
            childStep.parentStep = this;
            childStepId = childStep.getDisplayId();
        } else
            childStepId = 0;
    }

    public void setNextStep(Step nextStep) {
        this.nextStep = nextStep;
        if (nextStep != null) {
            nextStep.previousStep = this;
            nextStep.previousStepId = displayId;
        }
    }

    public void setPreviousStep(Step previousStep) {
        this.previousStep = previousStep;
        if (previousStep != null) {
            previousStep.nextStep = this;
            previousStepId = previousStep.getDisplayId();
        } else
            previousStepId = 0;
    }

    public boolean isFirstStep() {
        return (previousStepId == 0);
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
            try {
                name = getQuestion().getShortDisplayName();
            } catch(WdkModelException ex) {
                name = null;
            }
        }
        if (name == null)
            name = getQuestionName();
        if (name != null) {
            // remove script injections
            name = name.replaceAll("<.+?>", " ");
            name = name.replaceAll("[\"]", " ");
            name = name.trim().replaceAll("\\s+", " ");
            if (name.length() > 4000)
                name = name.substring(0, 4000);
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
        /*
         * String name = customName;
         * 
         * if (name == null) name = getQuestion().getShortDisplayName(); if
         * (name != null) { // remove script injections name =
         * name.replaceAll("<.+?>", " "); name = name.replaceAll("['\"]", " ");
         * name = name.trim().replaceAll("\\s+", " "); if (name.length() > 4000)
         * name = name.substring(0, 4000); } return name;
         */
        try {
            return getQuestion().getShortDisplayName();
        } catch(WdkModelException ex) {
            return getDisplayName();
        }
    }

    public String getDisplayName() {
        try {
            return getQuestion().getDisplayName();
        } catch(WdkModelException ex) {
            if (customName != null) return customName;
            else if (answer != null) return answer.getQuestionName();
            else return null;
        }
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
    public int getInternalId() {
        return internalId;
    }

    /**
     * @param answer
     *            The answer to set.
     */
    public void setAnswer(Answer answer) {
        this.answer = answer;
        String questionName = answer.getQuestionName();
        try {
            user.getWdkModel().getQuestion(questionName);
        } catch (WdkModelException ex) {
            this.valid = false;
            this.revisable = false;
        }
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
    public int getEstimateSize() {
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
    public boolean isCombined() {
        try {
            return getQuestion().getQuery().isCombined();
        } catch(WdkModelException ex) {
            return false;
        }
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
    public boolean isTransform() {
        try {
            return getQuestion().getQuery().isTransform();
        } catch(WdkModelException ex) {
            return false;
        }
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

    public void update(boolean updateTime) throws WdkUserException, WdkModelException {
        stepFactory.updateStep(user, this, updateTime);
    }

    public String getDescription() {
        try {
            return getQuestion().getDescription();
        } catch(WdkModelException ex) {
            return null;
        }
    }

    /**
     * @return Returns the isDeleted.
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @param isDeleted
     *            The isDeleted to set.
     */
    public void setDeleted(boolean isDeleted) {
        this.deleted = isDeleted;
    }

    public boolean isCollapsible() {
        if (collapsible)
            return true;
        // it is true if the step is a branch
        return (getParentStep() != null && isCombined());
    }

    public void setCollapsible(boolean isCollapsible) {
        this.collapsible = isCollapsible;
    }

    public String getCollapsedName() throws WdkModelException {
        if (collapsedName == null && isCollapsible())
            return getCustomName();
        return collapsedName;
    }

    public void setCollapsedName(String collapsedName) {
        this.collapsedName = collapsedName;
    }

    /**
     * @return the isValid
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public boolean isValid() throws WdkUserException, WdkModelException,
            SQLException {
        if (!valid)
            return false;
        Step prevStep = getPreviousStep();
        if (prevStep != null) {
            if (!prevStep.isValid())
                return false;
        }
        Step childStep = getChildStep();
        if (childStep != null) {
            if (!childStep.isValid())
                return false;
        }
        return true;
    }

    /**
     * @param isValid
     *            the isValid to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Map<String, String> getParamNames() throws WdkModelException {
        Map<String, Param> params = getQuestion().getQuery().getParamMap();
        Map<String, String> names = new LinkedHashMap<String, String>();
        for (Param param : params.values()) {
            names.put(param.getName(), param.getPrompt());
        }
        return names;
    }

    public String getQuestionName() {
        return (answer != null) ? answer.getQuestionName() : null;
    }

    /* functions for navigating/manipulating step tree */
    public Step getStep(int index) throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        Step[] steps = getAllSteps();
        return steps[index];
    }

    public Step[] getAllSteps() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        ArrayList<Step> allSteps = new ArrayList<Step>();
        allSteps = buildAllStepsArray(allSteps, this);
        return allSteps.toArray(new Step[allSteps.size()]);
    }

    public int getLength() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        return getAllSteps().length;
    }

    private ArrayList<Step> buildAllStepsArray(ArrayList<Step> array, Step step)
            throws WdkUserException, WdkModelException, SQLException,
            JSONException {
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

    public Step getStepByDisplayId(int displayId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        Step target;
        if (this.displayId == displayId) {
            return this;
        }
        Step childStep = getChildStep();
        if (childStep != null) {
            target = childStep.getStepByDisplayId(displayId);
            if (target != null) {
                return target;
            }
        }
        Step prevStep = getPreviousStep();
        if (prevStep != null) {
            target = prevStep.getStepByDisplayId(displayId);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    public Step getStepByChildId(int childId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        logger.debug("gettting step by child id. current=" + this + ", input="
                + childId);
        Step target;
        if (this.childStepId == childId) {
            return this;
        }
        Step childStep = getChildStep();
        if (childStep != null) {
            target = childStep.getStepByChildId(childId);
            if (target != null) {
                return target;
            }
        }
        Step prevStep = getPreviousStep();
        if (prevStep != null) {
            target = prevStep.getStepByChildId(childId);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    public Step getStepByPreviousId(int previousId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        logger.debug("gettting step by prev id. current=" + this + ", input="
                + previousId);
        Step target;
        if (this.previousStepId == previousId) {
            return this;
        }
        Step childStep = getChildStep();
        if (childStep != null) {
            target = childStep.getStepByPreviousId(previousId);
            if (target != null) {
                return target;
            }
        }
        Step prevStep = getPreviousStep();
        if (prevStep != null) {
            target = prevStep.getStepByPreviousId(previousId);
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
        if (this.valid) {
            Map<String, Param> params = getQuestion().getParamMap();
            for (String paramName : paramValues.keySet()) {
                if (!params.containsKey(paramName)) {
                    this.valid = false;
                    break;
                }
            }
        }
    }

    public String getType() {
        try {
            return getQuestion().getRecordClass().getFullName();
        } catch (WdkModelException ex) {
            // ex.printStackTrace();
            return "Unknown Type";
        }
    }

    public int getIndexFromId(int displayId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        Step[] steps = getAllSteps();
        for (int i = 0; i < steps.length; ++i) {
            if (steps[i].getDisplayId() == displayId
                    || (steps[i].getChildStep() != null && steps[i]
                            .getChildStep().getDisplayId() == displayId)) {
                return i;
            }
        }
        throw new WdkUserException("Id not found!");
    }

    public Step createStep(String filterName, int assignedWeight)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        RecordClass recordClass = getQuestion().getRecordClass();
        AnswerFilterInstance filter = recordClass.getFilter(filterName);
        return createStep(filter, assignedWeight);
    }

    public Step createStep(AnswerFilterInstance filter, int assignedWeight)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        AnswerFilterInstance oldFilter = getFilter();
        if (filter == null && oldFilter == null
                && this.assignedWeight == assignedWeight)
            return this;
        if (filter != null && oldFilter != null
                && filter.getName().equals(oldFilter.getName())
                && this.assignedWeight == assignedWeight)
            return this;

        // create new steps
        Question question = getQuestion();
        Map<String, String> params = getParamValues();
        int startIndex = getAnswerValue().getStartIndex();
        int endIndex = getAnswerValue().getEndIndex();
        Step step = user.createStep(question, params, filter, startIndex,
                endIndex, deleted, false, assignedWeight);
        step.collapsedName = collapsedName;
        step.customName = customName;
        step.collapsible = collapsible;
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
            step = user.createStep(answerValue, deleted, assignedWeight);
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
                    pageEnd, deleted, false, assignedWeight);
        }
        step.collapsedName = collapsedName;
        step.customName = customName;
        step.collapsible = collapsible;
        step.update(false);
        return step;
    }

    public boolean isFiltered() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        AnswerFilterInstance filter = getFilter();
        if (filter == null)
            return false;

        Question question;
        try {
            question = getQuestion();
        } catch(WdkModelException ex) {
            return false;
        }
        RecordClass recordClass = question.getRecordClass();
        AnswerFilterInstance defaultFilter = recordClass.getDefaultFilter();
        if (defaultFilter == null)
            return true;

        return (!defaultFilter.getName().equals(filter.getName()));
    }

    public String getFilterDisplayName() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        AnswerFilterInstance filter = getFilter();
        return (filter != null) ? filter.getDisplayName() : filterName;
    }

    public Step getFirstStep() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        Step step = this;
        while (step.getPreviousStep() != null)
            step = step.getPreviousStep();
        return step;
    }

    public boolean isBoolean() {
        try {
            return getQuestion().getQuery().isBoolean();
        } catch(WdkModelException ex) {
            return false;
        }
    }

    public JSONObject getJSONContent(int strategyId) throws JSONException,
            WdkUserException, WdkModelException, SQLException {
        JSONObject jsStep = new JSONObject();

        jsStep.put("id", this.displayId);
        jsStep.put("customName", this.customName);
        try {
            jsStep.put("answer", this.answer.getAnswerChecksum());
            jsStep.put("collapsed", this.isCollapsible());
            jsStep.put("collapsedName", this.getCollapsedName());
        } catch (WdkModelException ex) { // the question is invalid
            jsStep.put("answer", "");
            jsStep.put("collapsed", false);
            jsStep.put("collapsedName", "");
        }

        jsStep.put("deleted", deleted);
        jsStep.put("size", this.estimateSize);
        Step prevStep = getPreviousStep();
        if (prevStep != null) {
            jsStep.put("previous", prevStep.getJSONContent(strategyId));
        }
        Step childStep = getChildStep();
        if (childStep != null) {
            jsStep.put("child", childStep.getJSONContent(strategyId));
        }
        if (this.isCollapsible()) { // a sub-strategy, needs to get order number
            String subStratId = strategyId + "_" + this.displayId;
            Integer order = user.getStrategyOrder(subStratId);
            if (order == null)
                order = 0; // the sub-strategy is not displayed
            jsStep.put("order", order);
        }
        return jsStep;
    }

    public Question getQuestion() throws WdkModelException {
        String questionName = answer.getQuestionName();
        WdkModel wdkModel = user.getWdkModel();
        return (Question) wdkModel.resolveReference(questionName);
    }

    public AnswerFilterInstance getFilter() {
        try {
            return getQuestion().getRecordClass().getFilter(filterName);
        } catch (WdkModelException ex) {
            return null;
        }
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public AnswerValue getAnswerValue()
            throws WdkModelException, WdkUserException {
        return getAnswerValue(true);
    }

    public AnswerValue getAnswerValue(boolean validate)
            throws WdkModelException, WdkUserException {
        // even if a step is invalid, still allow user to create answerValue
        // if (!valid)
        // throw new WdkUserException("Step #" + internalId
        // + "(i) is invalid, cannot create answerValue.");
        if (answerValue == null) {
            Question question = getQuestion();
            Map<String, Boolean> sortingMap = user
                    .getSortingAttributes(question.getFullName());
            int endIndex = user.getItemsPerPage();
            answerValue = question.makeAnswerValue(user, paramValues, 1,
                    endIndex, sortingMap, getFilter(), validate, assignedWeight);
            this.estimateSize = answerValue.getResultSize();
            update(false);
        }
        return answerValue;
    }

    public String getAnswerKey() {
        String key = answer.getAnswerChecksum();
        if (filterName != null)
            key += ":" + filterName;
        return key;
    }

    public boolean isUseBooleanFilter() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        if (!isBoolean())
            return false;
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

    public String getShortDisplayType() {
        try {
            return getQuestion().getRecordClass().getShortDisplayName();
        } catch (WdkModelException ex) {
            return getType();
        }
    }

    /**
     * Validate a step and all the children steps it depends on. the result of
     * validation will also be stored in "valid" variable. If a step was already
     * invalid it will stay invalid.
     * 
     * @return
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     */
    public boolean validate() throws SQLException, WdkUserException,
            WdkModelException, JSONException {
        // only validate leaf steps. the validation of a combined step is
        // determined by the children.
        Step prevStep = getPreviousStep();
        Step childStep = getChildStep();
        if (childStep == null && prevStep == null) {
            if (!valid)
                return valid;
            try {
                getAnswerValue();
            } catch (Exception ex) {
                this.validationMessage = ex.getMessage();
                this.valid = false;
            }
        } else {
            if (childStep != null) {
                if (!childStep.validate())
                    this.valid = false;
            } else if (prevStep != null) {
                if (!prevStep.validate())
                    this.valid = false;
            }
        }
        // set the invalid flag
        if (!valid)
            stepFactory.setStepValidFlag(this);
        return valid;
    }

    /**
     * @return the validationMessage
     */
    public String getValidationMessage() {
        return validationMessage;
    }

    /**
     * @param validationMessage
     *            the validationMessage to set
     */
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    /**
     * @return the assignedWeight
     */
    public int getAssignedWeight() {
        return assignedWeight;
    }

    /**
     * @param assignedWeight
     *            the assignedWeight to set
     */
    public void setAssignedWeight(int assignedWeight) {
        this.assignedWeight = assignedWeight;
    }

    /**
     * @return the previousStepId
     */
    public int getPreviousStepId() {
        return previousStepId;
    }

    /**
     * @param previousStepId
     *            the previousStepId to set
     */
    public void setPreviousStepId(int previousStepId) {
        this.previousStepId = previousStepId;
    }

    /**
     * @return the childStepId
     */
    public int getChildStepId() {
        return childStepId;
    }

    /**
     * @param childStepId
     *            the childStepId to set
     */
    public void setChildStepId(int childStepId) {
        this.childStepId = childStepId;
    }

    public boolean isRevisable() {
        return revisable;
    }

    /**
     * The previous step param is always the first answerParam.
     * 
     * @return
     * @throws WdkModelException
     */
    public String getPreviousStepParam() throws WdkModelException {
        Param[] params = getQuestion().getParams();
        for (Param param : params) {
            if (param instanceof AnswerParam) {
                return param.getName();
            }
        }
        return null;
    }

    /**
     * the child step param is always the second answerParam
     * 
     * @return
     * @throws WdkModelException
     */
    public String getChildStepParam() throws WdkModelException {
        Param[] params = getQuestion().getParams();
        int index = 0;
        for (Param param : params) {
            if (param instanceof AnswerParam) {
                index++;
                if (index == 2)
                    return param.getName();
            }
        }
        return null;
    }

    public int getFrontId() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        int frontId;
        Step previousStep = getPreviousStep();
        if (previousStep == null)
            frontId = 1;
        else {
            frontId = previousStep.getFrontId();
            frontId++;
        }
        return frontId;
    }

    public String toString() {
        return displayId + " (" + previousStepId + ", " + childStepId + ")";
    }
    
    public boolean isUncollapsible() throws WdkModelException {
        // if the step hasn't been collapsed, it cannot be uncollapsed.
        if (!collapsible) return false;
        
        // if the step is a combined step, it cannot be uncollapsed
        if (isCombined()) return false;
        
        return true;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception ex) {
        this.exception = ex;
    }
}
