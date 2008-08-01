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

/**
 * @author Charles Treatman
 * 
 */

// Copied from History.java; need to convert to new Answer object.
// Need to get rid of references to User (this is global answer)
// Get rid of custom name, dates, deleted, (?) depended, version

public class Answer {
    
    private UserFactory factory;
    private User user;
    private int answerId;
    private RecordPage recordPage = null;
    private int estimateSize;
    private boolean isBoolean;
    private String booleanExpression;
    private Boolean isDepended;
    
    private boolean isValid = true;
    private String version;
    private Map< String, Object > params;
    private String questionName;
    
    Answer( UserFactory factory, User user, int answerId ) {
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
     * @return Returns the recordPage.
     * @throws WdkUserException
     */
    public RecordPage getRecordPage() throws WdkUserException {
        if ( !isValid )
            throw new WdkUserException( "The history #" + answerId
                    + " is invalid." );
        return recordPage;
    }
    
    /**
     * @param answer
     *            The answer to set.
     */
    public void setRecordPage( RecordPage recordPage ) {
        this.recordPage = recordPage;
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
        return recordPage.getIdsQueryInstance().getQuery().getSignature();
    }
    
    public String getChecksum() throws WdkModelException {
        return recordPage.getIdsQueryInstance().getChecksum();
    }
    
    public String getDataType() {
        return recordPage.getQuestion().getRecordClass().getFullName();
    }
    
    // Do these just go in UserAnswer, no need for it in answer?
    /*
    public boolean isDepended() throws WdkUserException, WdkModelException {
        if ( isDepended == null ) computeDependencies( user.getAnswers() );
        return isDepended;
    }
    
    void computeDependencies( Answer[ ] histories ) throws WdkModelException {
        isDepended = false;
        for ( Answer history : histories ) {
            if ( history.answerId == this.answerId ) continue;
            Set< Integer > components = history.getComponentAnswers();
            if ( components.contains( answerId ) ) {
                isDepended = true;
                break;
            }
        }
    }
    */
    /**
     * @return get a list of history ID's this one depends on directly.
     * @throws WdkModelException
     */
    /*
    public Set< Integer > getComponentAnswers() throws WdkModelException {
        if ( isBoolean ) {
            BooleanExpression parser = new BooleanExpression( user );
            return parser.getOperands( booleanExpression );
        } else {
            Set< Integer > components = new LinkedHashSet< Integer >();
            Param[ ] params = answer.getQuestion().getParams();
            Map< String, Object > values = answer.getParams();
            for ( Param param : params ) {
                if ( param instanceof HistoryParam ) {
                    String compound = values.get( param.getName() ).toString();
                    // two parts: user_signature, history_id
                    String[ ] parts = compound.split( ":" );
                    components.add( Integer.parseInt( parts[ 1 ].trim() ) );
                }
            }
            return components;
        }
    }
    */
    public String getDescription() {
        return ( isBoolean ) ? booleanExpression : recordPage.getName();
    }
    
    public String getCacheFullTable() throws WdkModelException {
        return recordPage.getIdsQueryInstance().getResultAsTableName();
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
