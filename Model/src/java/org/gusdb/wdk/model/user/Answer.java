/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author Charles Treatman
 * 
 */
public class Answer {
    
    private UserFactory factory;
    private User user;
    private int answerId;
    private AnswerValue answerValue = null;
    private int estimateSize;
    private boolean isBoolean;
    private String booleanExpression;
    private Boolean isDepended;
    
    private boolean isValid = true;
    private String version;
    private Map< String, Object > params;
    private String questionName;
    
    Answer( UserFactory factory, User user, int answerId) {
        this.factory = factory;
        this.user = user;
        this.answerId = answerId;
    }
    
    /**
     * @return Returns the answerId.
     */
    public int getAnswerId() {
        return answerId;
    }
    
    /**
     * @return Returns the answerValue.
     * @throws WdkUserException
     */
    public AnswerValue getAnswerValue() throws WdkUserException {
        if ( !isValid )
            throw new WdkUserException( "The history #" + answerId
                    + " is invalid." );
        return answerValue;
    }
    
    /**
     * @param answer
     *            The answer to set.
     */
    public void setAnswerValue( AnswerValue answerValue ) {
        this.answerValue = answerValue;
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
    public void setEstimateSize( int estimateSize ) {
        this.estimateSize = estimateSize;
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
    public void setBoolean( boolean isBoolean ) {
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
    public void setBooleanExpression( String booleanExpression ) {
        this.booleanExpression = booleanExpression;
    }
    
    public String getSignature() throws WdkModelException {
        return answerValue.getIdsQueryInstance().getQuery().getSignature();
    }
    
    public String getChecksum() throws WdkModelException {
        return answerValue.getIdsQueryInstance().getChecksum();
    }
    
    public String getDataType() {
        return answerValue.getQuestion().getRecordClass().getFullName();
    }
    
    public String getDescription() {
        return ( isBoolean ) ? booleanExpression : answerValue.getName();
    }
    
    public String getCacheFullTable() throws WdkModelException {
        return answerValue.getIdsQueryInstance().getResultAsTableName();
    }
    
    public boolean isTransform() {
	Param[ ] params = answerValue.getQuestion().getParams();
	for ( Param param : params ) {
	    if ( param instanceof HistoryParam ) {
		return true;
	    }
	}
	return false;
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
        this.params = params;
    }
    
    public Map< String, Object > getParams() {
        return new LinkedHashMap< String, Object >( this.params );
    }
    
    // How to get user object out of here?
    public Map< String, String > getParamNames() {
        Map< String, String > paramNames = new LinkedHashMap< String, String >();
        WdkModel wdkModel = user.getWdkModel();
        for ( String paramName : params.keySet() ) {
            String displayName = wdkModel.getParamDisplayName( paramName );
            if ( displayName == null ) displayName = paramName;
            paramNames.put( paramName, displayName );
        }
        
        return paramNames;
    }
    
    void setQuestionName( String questionName ) {
        this.questionName = questionName;
    }
    
    public String getQuestionName() {
        return this.questionName;
    }
}
