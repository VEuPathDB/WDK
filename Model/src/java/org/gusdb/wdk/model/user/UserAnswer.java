/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.RecordPage;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Answer;

/**
 * @author Charles Treatman
 * 
 */

public class UserAnswer {
    
    private UserFactory factory;
    private User user;
    private int userAnswerId;
    private Date createdTime;
    private Date lastRunTime;
    private String customName;
    private Answer answer = null;
    private boolean isDeleted;
    private Boolean isDepended;
    
    private boolean isValid = true;
    private String version;
        
    UserAnswer( UserFactory factory, User user, int userAnswerId ) {
        this.factory = factory;
        this.user = user;
        this.userAnswerId = userAnswerId;
        isDeleted = false;
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
    void setCreatedTime( Date createdTime ) {
        this.createdTime = createdTime;
    }
    
    public String getBaseCustomName() {
        return customName;
    }
    
    /**
     * @return Returns the customName. If no custom name set before, it will
     *         return the default name provided by the underline RecordPage - a
     *         combination of question's full name, parameter names and values.
     */
    public String getCustomName() 
	throws WdkUserException {
        String name = customName;
	if ( name == null || name.length() == 0) {
	    if (answer.getRecordPage() != null) {
		name = answer.getRecordPage().getQuestion().getDisplayName();
	    }
	}
        if ( name == null ) name = answer.getQuestionName();
        if ( name != null ) {
            // remove script injections
            name = name.replaceAll( "<.+?>", " " );
            name = name.replaceAll( "['\"]", " " );
            name = name.trim().replaceAll( "\\s+", " " );
            if ( name.length() > 4000 ) name = name.substring( 0, 4000 );
        }
        return name;
    }
    

    /**
     * @return Returns the custom name, if it is set.  Otherwise, returns
     *         the short display name for the underlying question.
     */
    public String getShortDisplayName()
	throws WdkUserException {
	String name = customName;
	
	if ( name == null ) name = answer.getRecordPage().getQuestion().getShortDisplayName();
	if ( name != null ) {
            // remove script injections
            name = name.replaceAll( "<.+?>", " " );
            name = name.replaceAll( "['\"]", " " );
            name = name.trim().replaceAll( "\\s+", " " );
            if ( name.length() > 4000 ) name = name.substring( 0, 4000 );
        }
        return name;
    }

    /**
     * @param customName
     *            The customName to set.
     */
    public void setCustomName( String customName ) {
        this.customName = customName;
    }
    
    /**
     * @return Returns the userAnswerId.
     */
    public int getUserAnswerId() {
        return userAnswerId;
    }
    
    /**
     * @param answer
     *          The answer to set.
     */
    public void setAnswer( Answer answer ) {
	this.answer = answer;
    }

    /**
     * @return Returns the answer.
     */
    public Answer getAnswer() {
	return answer;
    }


    /**
     * @return Returns the result.
     * @throws WdkUserException
     */
    public RecordPage getRecordPage() throws WdkUserException {
        return answer.getRecordPage();
    }
    
    /**
     * @param answer
     *            The answer to set.
     */
    public void setRecordPage( RecordPage result ) {
        answer.setRecordPage(result);
    }
    
    /**
     * @return Returns the estimateSize.
     */
    public int getEstimateSize() {
        return answer.getEstimateSize();
    }
    
    /**
     * @param estimateSize
     *            The estimateSize to set.
     */
    public void setEstimateSize( int estimateSize ) {
        answer.setEstimateSize(estimateSize);
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
    public void setLastRunTime( Date lastRunTime ) {
        this.lastRunTime = lastRunTime;
    }
    
    /**
     * @return Returns the isBoolean.
     */
    public boolean isBoolean() {
        return answer.isBoolean();
    }

    /**
     * @return Returns whether this UserAnswer is a transform
     */
    public boolean isTransform() {
	return answer.isTransform();
    }
    
    /**
     * @param isBoolean
     *            The isBoolean to set.
     */
    public void setBoolean( boolean isBoolean ) {
        answer.setBoolean(isBoolean);
    }
    
    /**
     * @return Returns the booleanExpression.
     */
    public String getBooleanExpression() {
        return answer.getBooleanExpression();
    }
    
    /**
     * @param booleanExpression
     *            The booleanExpression to set.
     */
    public void setBooleanExpression( String booleanExpression ) {
        answer.setBooleanExpression(booleanExpression);
    }
    
    public String getSignature() throws WdkModelException {
        return answer.getSignature();
    }
    
    public String getChecksum() throws WdkModelException {
        return answer.getChecksum();
    }
    
    public String getDataType() {
        return answer.getDataType();
    }
    
    public void update() throws WdkUserException {
        factory.updateUserAnswer( user, this, true );
    }
    
    public void update( boolean updateTime ) throws WdkUserException {
        factory.updateUserAnswer( user, this, updateTime );
    }
    

    public String getDescription() {
        return answer.getDescription();
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
    public void setDeleted( boolean isDeleted ) {
        this.isDeleted = isDeleted;
    }
    
    public String getCacheFullTable() throws WdkModelException {
        return answer.getCacheFullTable();
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
    public void setValid( boolean isValid ) {
        this.isValid = isValid;
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
    public void setVersion( String version ) {
        this.version = version;
    }
    
    public void setParams( Map< String, Object > params ) {
        answer.setParams(params);
    }
    
    public Map< String, Object > getParams() {
        return new LinkedHashMap< String, Object >( answer.getParams() );
    }
    
    public Map< String, String > getParamNames() {
        return answer.getParamNames();
    }
    
    void setQuestionName( String questionName ) {
	answer.setQuestionName(questionName);
    }
    
    public String getQuestionName() {
	return answer.getQuestionName();
    }
}
