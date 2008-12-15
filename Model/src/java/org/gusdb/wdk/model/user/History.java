/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.QueryInstance;
import org.json.JSONException;

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

    private Map<String, Object> params;
    private String filterName;
    private int filterSize;

    private String questionName;

    History(UserFactory factory, User user, int historyId) {
        this.factory = factory;
        this.user = user;
        this.historyId = historyId;
        isDeleted = false;
        params = new LinkedHashMap<String, Object>();
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
                    WdkModel wdkModel = user.getWdkModel();
                    Question question = (Question) wdkModel.getQuestion(questionName);
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
     */
    public Answer getAnswer() throws WdkUserException {
        if (!isValid)
            throw new WdkUserException("The history #" + historyId
                    + " is invalid.");
        return answer;
    }

    /**
     * @param answer
     *            The answer to set.
     */
    public void setAnswer(Answer answer) {
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

    public String getSignature() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return answer.getIdsQueryInstance().getQuery().getChecksum();
    }

    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return answer.getChecksum();
    }

    public String getDataType() {
        return answer.getQuestion().getRecordClass().getFullName();
    }

    public void update() throws WdkUserException, NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException {
        factory.updateHistory(user, this, true);
    }

    public void update(boolean updateTime) throws WdkUserException,
            NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException {
        this.estimateSize = answer.getResultSize();
        factory.updateHistory(user, this, updateTime);
    }

    public boolean isDepended() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
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
        if (isBoolean) {
            BooleanExpression parser = new BooleanExpression(user);
            return parser.getOperands(booleanExpression);
        } else return new LinkedHashSet<Integer>();
    }

    public String getDescription() {
        return (isBoolean) ? booleanExpression
                : answer.getQuestion().getDescription();
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

    public void setParams(Map<String, Object> params) throws JSONException {
        this.params = new LinkedHashMap<String, Object>(params);
    }

    public Map<String, Object> getParams() {
        return new LinkedHashMap<String, Object>(params);
    }

    public Map<String, String> getParamPrompts() throws WdkModelException {
        Map<String, String> paramNames = new LinkedHashMap<String, String>();
        if (isValid) {
            QueryInstance instance = answer.getIdsQueryInstance();
            Param[] params = instance.getQuery().getParams();
            for (Param param : params) {
                paramNames.put(param.getName(), param.getPrompt());
            }
        } else {
            WdkModel wdkModel = user.getWdkModel();
            for (String paramName : params.keySet()) {
                String displayName = wdkModel.queryParamDisplayName(paramName);
                paramNames.put(paramName, displayName);
            }
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

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }
}
