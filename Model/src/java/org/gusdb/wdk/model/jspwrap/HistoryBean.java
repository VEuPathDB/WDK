/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.History;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class HistoryBean {

    private History history;
    private int nameTruncateTo;

    public HistoryBean(History history) {
        this.history = history;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getChecksum()
     */
    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return history.getChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getCreatedTime()
     */
    public Date getCreatedTime() {
        return history.getCreatedTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getCustomName()
     */
    public String getCustomName() {
        return history.getCustomName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getBaseCustomName()
     */
    public String getBaseCustomName() {
        return history.getBaseCustomName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getDataType()
     */
    public String getDataType() {
        return history.getDataType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getVersion()
     */
    public String getVersion() {
        return history.getVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getEstimateSize()
     */
    public int getEstimateSize() {
        return history.getEstimateSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#setEstimateSize(int)
     */
    public void setEstimateSize(int estimateSize) {
        history.setEstimateSize(estimateSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getHistoryId()
     */
    public int getHistoryId() {
        return history.getHistoryId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getLastRunTime()
     */
    public Date getLastRunTime() {
        return history.getLastRunTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getSignature()
     */
    public String getSignature() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return history.getSignature();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getUser()
     */
    public UserBean getUser() {
        return new UserBean(history.getUser());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#isBoolean()
     */
    public boolean isBoolean() {
        return history.isBoolean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#update()
     */
    public void update() throws WdkUserException {
        history.update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#update(boolean)
     */
    public void update(boolean updateTime) throws WdkUserException {
        history.update(updateTime);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#isDepended()
     */
    public boolean getDepended() throws WdkUserException, WdkModelException {
        return history.isDepended();
    }

    /**
     * @return
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.user.History#getAnswer()
     */
    public AnswerBean getAnswer() throws WdkUserException {
        Answer answer = history.getAnswer();
        AnswerBean answerBean = new AnswerBean(answer);
        if (answer.getIsBoolean())
            answerBean.customName = history.getCustomName();
        return answerBean;
    }

    /**
     * @return Returns the nameTruncateTo.
     */
    public int getNameTruncateTo() {
        return nameTruncateTo;
    }

    /**
     * @param nameTruncateTo
     *            The nameTruncateTo to set.
     */
    public void setNameTruncateTo(int nameTruncateTo) {
        this.nameTruncateTo = nameTruncateTo;
    }

    public String getTruncatedName() {
        String name = history.getCustomName();
        if (name != null && name.length() > nameTruncateTo)
            name = name.substring(0, nameTruncateTo) + "...";
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#setCustomName(java.lang.String)
     */
    public void setCustomName(String customName) {
        history.setCustomName(customName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getDescription()
     */
    public String getDescription() {
        return history.getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#getBooleanExpression()
     */
    public String getBooleanExpression() {
        return history.getBooleanExpression();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.History#isDeleted()
     */
    public boolean isDeleted() {
        return history.isDeleted();
    }

    /**
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.History#getParamNames()
     */
    public Map<String, String> getParamNames() throws WdkModelException {
        return history.getParamNames();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.History#getParams()
     */
    public Map<String, Object> getParams() {
        return history.getParams();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.History#getQuestionName()
     */
    public String getQuestionName() {
        return history.getQuestionName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.History#isValid()
     */
    public boolean isValid() {
        return history.isValid();
    }
}
