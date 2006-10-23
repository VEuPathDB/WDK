/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;
import java.util.Set;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

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
    private Answer answer = null;
    private int estimateSize;
    private boolean isBoolean;
    private String booleanExpression;

    History(UserFactory factory, User user, int historyId) {
        this.factory = factory;
        this.user = user;
        this.historyId = historyId;
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

    /**
     * @return Returns the customName. If no custom name set before, it will
     *         return the default name provided by the underline Answer - a
     *         combination of question's full name, parameter names and values.
     */
    public String getCustomName() {
        if (customName == null) {
            customName = (isBoolean) ? booleanExpression : answer.getName();
        }
        if (customName.length() > 4000) return customName.substring(0, 4000);
        else return customName;
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
     */
    public int getEstimateSize() {
        return estimateSize;
    }

    /**
     * @param estimateSize
     *            The estimateSize to set.
     */
    void setEstimateSize(int estimateSize) {
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

    public String getSignature() throws WdkModelException {
        return answer.getIdsQueryInstance().getQuery().getSignature();
    }

    public String getChecksum() throws WdkModelException {
        return answer.getIdsQueryInstance().getChecksum();
    }

    public String getDataType() {
        return answer.getQuestion().getRecordClass().getFullName();
    }

    public void update() throws WdkUserException {
        factory.updateHistory(user, this, true);
    }

    public void update(boolean updateTime) throws WdkUserException {
        factory.updateHistory(user, this, updateTime);
    }

    public boolean isDepended() throws WdkUserException, WdkModelException {
        History[] histories = user.getHistories();
        BooleanExpression expression = new BooleanExpression(user);
        for (History history : histories) {
            if (history.isBoolean()) {
                Set<Integer> depends = expression.getOperands(history.getBooleanExpression());
                if (depends.contains(historyId)) return true;
            }
        }
        return false;
    }
}
