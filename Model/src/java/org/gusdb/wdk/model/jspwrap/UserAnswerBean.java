/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.RecordPage;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.UserAnswer;

/**
 * @author xingao
 * 
 */
public class UserAnswerBean {
    
    UserAnswer userAnswer;
    private int nameTruncateTo;
    
    public UserAnswerBean( UserAnswer userAnswer ) {
        this.userAnswer = userAnswer;
    }
    
    UserAnswer getUserAnswer() {
        return userAnswer;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getChecksum()
     */
    public String getChecksum() throws WdkModelException {
        return userAnswer.getChecksum();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getCreatedTime()
     */
    public Date getCreatedTime() {
        return userAnswer.getCreatedTime();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getCustomName()
     */
    public String getCustomName() {
        return userAnswer.getCustomName();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getBaseCustomName()
     */
    public String getBaseCustomName() {
        return userAnswer.getBaseCustomName();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getDataType()
     */
    public String getDataType() {
        return userAnswer.getDataType();
    }
    
     /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getVersion()
     */   
    public String getVersion() {
        return userAnswer.getVersion();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getEstimateSize()
     */
    public int getEstimateSize() {
        return userAnswer.getEstimateSize();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#setEstimateSize(int)
     */
    public void setEstimateSize( int estimateSize ) {
        userAnswer.setEstimateSize( estimateSize );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getUserAnswerId()
     */
    public int getUserAnswerId() {
        return userAnswer.getUserAnswerId();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getLastRunTime()
     */
    public Date getLastRunTime() {
        return userAnswer.getLastRunTime();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getSignature()
     */
    public String getSignature() throws WdkModelException {
        return userAnswer.getSignature();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getUser()
     */
    public UserBean getUser() {
        return new UserBean( userAnswer.getUser() );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#isBoolean()
     */
    public boolean isBoolean() {
        return userAnswer.isBoolean();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#update()
     */
    public void update() throws WdkUserException {
        userAnswer.update();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#update(boolean)
     */
    public void update( boolean updateTime ) throws WdkUserException {
        userAnswer.update( updateTime );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#isDepended()
     */
    /*
    public boolean getDepended() throws WdkUserException, WdkModelException {
        return userAnswer.isDepended();
    }
    */
    
    /**
     * @return
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.user.UserAnswer#getRecordPage()
     */
    public RecordPageBean getRecordPage() throws WdkUserException {
        RecordPage answer = userAnswer.getRecordPage();
        RecordPageBean answerBean = new RecordPageBean( answer );
        if ( answer.getIsBoolean() )
            answerBean.customName = userAnswer.getCustomName();
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
    public void setNameTruncateTo( int nameTruncateTo ) {
        this.nameTruncateTo = nameTruncateTo;
    }
    
    public String getTruncatedName() {
        String name = userAnswer.getCustomName();
        if (name != null && name.length() > nameTruncateTo )
            name = name.substring( 0, nameTruncateTo ) + "...";
        return name;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#setCustomName(java.lang.String)
     */
    public void setCustomName( String customName ) {
        userAnswer.setCustomName( customName );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getDescription()
     */
    public String getDescription() {
        return userAnswer.getDescription();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#getBooleanExpression()
     */
    public String getBooleanExpression() {
        return userAnswer.getBooleanExpression();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserAnswer#isDeleted()
     */
    public boolean isDeleted() {
        return userAnswer.isDeleted();
    }
    
    /**
     * @return
     * @see org.gusdb.wdk.model.user.UserAnswer#getParamNames()
     */
    public Map< String, String > getParamNames() {
        return userAnswer.getParamNames();
    }
    
    /**
     * @return
     * @see org.gusdb.wdk.model.user.UserAnswer#getParams()
     */
    public Map< String, Object > getParams() {
        return userAnswer.getParams();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.UserAnswer#getQuestionName()
     */
    public String getQuestionName() {
        return userAnswer.getQuestionName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.UserAnswer#isValid()
     */
    public boolean isValid() {
        return userAnswer.isValid();
    }
}
