/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class History {

    private UserFactory factory;
    private User user;
    private int historyId;

    private Date createdTime;
    private Date lastRunTime;
    private String customName;
    private int estimateSize = 0;
    private Answer answer = null;
    private boolean isBoolean;
    private String booleanExpression;
    private boolean isDeleted;
    private Boolean isDepended;

    private boolean isValid = true;

    private String paramValuesString;
    private Map<String, Object> paramValues;
    private String filterName;
    private int filterSize;

    private String questionName;

    private int answerId;
    private String answerChecksum;

    History(UserFactory factory, User user, int historyId) {
        this.factory = factory;
        this.user = user;
        this.historyId = historyId;
        isDeleted = false;
        paramValues = new LinkedHashMap<String, Object>();
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
     *         return the default name provided by the underline Answer - a
     *         combination of question's full name, parameter names and values.
     */
    public String getCustomName() {
        String name = customName;
        if (name == null || name.length() == 0) {
            if (isBoolean) {
                // boolean history, use boolean expression as default
                name = booleanExpression;
            } else if (answer != null) {
                // valid normal question, use question display name as default
                name = answer.getQuestion().getDisplayName();
            } else {
                // invalid question, try question display name;
                try {
                    Question question = getQuestion();
                    name = question.getDisplayName();
                } catch (Exception ex) {
                    // question no longer exists, use the recorded question name
                    name = questionName;
                }
            }
        }
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
     * @return Returns the historyId.
     */
    public int getHistoryId() {
        return historyId;
    }

    /**
     * @return Returns the answer.
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public Answer getAnswer() throws WdkUserException,
            NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException {
        if (answer != null) return answer;
        if (!isValid)
            throw new WdkUserException("The history #" + historyId
                    + " is invalid.");

        // try load the answer
        try {
            this.answer = factory.loadAnswer(this);
            if (answer == null) {
                this.isValid = false;
                this.update();
            }
        } catch (Exception ex) {
            this.isValid = false;
            this.update();
            throw new WdkUserException(ex);
        }
        return answer;
    }

    /**
     * This should only be called by UserFactory on creating history.
     * 
     * @param answer
     */
    void setAnswer(Answer answer) {
        this.answer = answer;
    }

    /**
     * @return Returns the estimateSize.
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public int getEstimateSize() {
        return estimateSize;
    }

    /**
     * @param estimateSize
     *            the estimateSize to set
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
     */
    public boolean isBoolean() {
        return isBoolean;
    }

    /**
     * @param isBoolean
     *            The isBoolean to set.
     */
    public void setBoolean(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }

    /**
     * @return Returns the booleanExpression.
     * @throws WdkModelException
     */
    public String getBooleanExpression() throws WdkModelException {
        if (isBoolean) {
            if (booleanExpression != null) return booleanExpression;
            Question question = getQuestion();
            BooleanQuery query = (BooleanQuery) question.getQuery();
            String leftName = query.getLeftOperandParam().getName();
            String leftValue = (String) paramValues.get(leftName);
            String leftHistory = leftValue.substring(leftValue.indexOf(":") + 1);
            String rightName = query.getRightOperandParam().getName();
            String rightValue = (String) paramValues.get(rightName);
            String rightHistory = rightValue.substring(rightValue.indexOf(":") + 1);
            String operatorName = query.getOperatorParam().getName();
            String operator = (String) paramValues.get(operatorName);

            return leftHistory + " " + operator + " " + rightHistory;
        } else return null;
    }

    /**
     * @param booleanExpression
     *            The booleanExpression to set.
     */
    public void setBooleanExpression(String booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    public String getSignature() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return getQuestion().getQuery().getChecksum();
    }

    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException,
            WdkUserException {
        return getAnswer().getChecksum();
    }

    public String getDataType() throws WdkModelException {
        return getQuestion().getRecordClass().getFullName();
    }

    public void update() throws WdkUserException, NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException {
        update(true);
    }

    public void update(boolean updateTime) throws WdkUserException,
            NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException {
        if (answer != null) this.estimateSize = answer.getResultSize();
        factory.updateHistory(user, this, updateTime);
    }

    public boolean isDepended() throws WdkUserException, WdkModelException,
            SQLException, JSONException, NoSuchAlgorithmException {
        if (isDepended == null) computeDependencies(user.getHistories());
        return isDepended;
    }

    void computeDependencies(History[] histories) throws WdkModelException {
        isDepended = false;
        for (History history : histories) {
            if (history.historyId == this.historyId) continue;
            Set<Integer> components = history.getComponentHistories();
            if (components.contains(historyId)) {
                isDepended = true;
                break;
            }
        }
    }

    /**
     * @return get a list of history ID's this one depends on directly.
     * @throws WdkModelException
     */
    public Set<Integer> getComponentHistories() throws WdkModelException {
        Set<Integer> predicates = new LinkedHashSet<Integer>();
        Map<String, Param> params = getQuestion().getParamMap();
        for (String paramName : this.paramValues.keySet()) {
            Param param = params.get(paramName);
            if (param instanceof AnswerParam) {
                // the value of an answer param is user_checksum:history_id
                Object paramValue = this.paramValues.get(paramName);
                String[] parts = paramValue.toString().split(":");
                int historyId = Integer.parseInt(parts[1].trim());
                predicates.add(historyId);
            }
        }
        return predicates;
    }

    public String getDescription() throws WdkModelException {
        return (isBoolean) ? booleanExpression : getQuestion().getDescription();
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

    /**
     * @return the isValid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * @param isValid
     *            the isValid to set
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public void setParamValues(String strParams) throws JSONException,
            WdkModelException {
        this.paramValuesString = strParams;
        paramValues = new LinkedHashMap<String, Object>();
        if (strParams == null || strParams.trim().length() == 0) return;

        JSONObject jsParams = new JSONObject(strParams);
        Iterator<?> keys = jsParams.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object value = jsParams.get(key).toString();
            paramValues.put(key, value);
        }
        // recreate the boolean expression
        if (isBoolean) {
            BooleanQuery query = (BooleanQuery) getQuestion().getQuery();
            String leftParam = query.getLeftOperandParam().getName();
            String rightParam = query.getRightOperandParam().getName();
            String operatorParam = query.getOperatorParam().getName();
            String leftOperand = paramValues.get(leftParam).toString().split(
                    ":")[1];
            String rightOperand = paramValues.get(rightParam).toString().split(
                    ":")[1];
            String operator = paramValues.get(operatorParam).toString();
            this.booleanExpression = leftOperand + " " + operator + " "
                    + rightOperand;
        }
    }

    public Map<String, Object> getParamValues() {
        return new LinkedHashMap<String, Object>(paramValues);
    }

    public String getParamValuesString() {
        return this.paramValuesString;
    }

    public Map<String, String> getParamPrompts() {
        Map<String, String> paramNames = new LinkedHashMap<String, String>();
        WdkModel wdkModel = user.getWdkModel();
        Question question = null;
        try {
            question = (Question) wdkModel.resolveReference(this.questionName);
        } catch (WdkModelException ex) { // question is invalid
            for (String paramName : paramValues.keySet()) {
                String displayName = wdkModel.queryParamDisplayName(paramName);
                paramNames.put(paramName, displayName);
            }
            return paramNames;
        }
        Map<String, Param> params = question.getParamMap();
        for (String paramName : this.paramValues.keySet()) {
            String displayName = null;
            if (params.containsKey(paramName)) params.get(paramName).getPrompt();
            else wdkModel.queryParamDisplayName(paramName);
            paramNames.put(paramName, displayName);
        }
        return paramNames;
    }

    /**
     * @return the filterName
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * @param filterName
     *            the filterName to set
     */
    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterDisplayName() {
        if (filterName == null) return null;
        try {
            RecordClass recordClass = getQuestion().getRecordClass();
            AnswerFilterInstance filter = recordClass.getFilter(filterName);
            return filter.getDisplayName();
        } catch (WdkModelException ex) {
            return filterName;
        }
    }

    /**
     * @return the filterSize
     */
    public int getFilterSize() {
        return filterSize;
    }

    /**
     * @param filterName
     *            the filterSize to set
     */
    public void setFilterSize(int filterSize) {
        this.filterSize = filterSize;
    }

    public String getQuestionName() {
        return questionName;
    }

    public Question getQuestion() throws WdkModelException {
        WdkModel wdkModel = user.getWdkModel();
        return (Question) wdkModel.resolveReference(questionName);
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public boolean isUseBooleanFilter() {
        if (!isBoolean) return false;
        Object useBoolean = paramValues.get(BooleanQuery.USE_BOOLEAN_FILTER_PARAM);
        if (useBoolean == null) return false;
        return Boolean.parseBoolean(useBoolean.toString());
    }

    /**
     * @return the answerId
     */
    public int getAnswerId() {
        return answerId;
    }

    /**
     * @param answerId
     *            the answerId to set
     */
    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    /**
     * @return the answerChecksum
     */
    public String getAnswerChecksum() {
        return answerChecksum;
    }

    /**
     * @param answerChecksum
     *            the answerChecksum to set
     */
    public void setAnswerChecksum(String answerChecksum) {
        this.answerChecksum = answerChecksum;
    }
}
