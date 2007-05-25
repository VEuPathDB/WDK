/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.WdkModel;
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
    private boolean isDeleted;
    private Boolean isDepended;

	private boolean isValid = true;
    private String version;
    private Map< String, Object > params;
    private String questionName;
    
    
    /**
     * Create a new History object.
     * 
     * @param factory The UserFactory that was used to load this History.
     * @param user The user this History is for.
     * @param historyId The id to be assigned to this History.
     */
    History(UserFactory factory, User user, int historyId) {
        this.factory = factory;
        this.user = user;
        this.historyId = historyId;
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
     *         return the default name provided by the underline Answer - a
     *         combination of question's full name, parameter names and values.
     */
    public String getCustomName() {
        String name = customName;
        if ( name == null || name.length() == 0 ) {
            if ( isBoolean ) name = booleanExpression;
            else if ( answer != null ) {
                name = answer.getQuestion().getDisplayName();
            }
        }
        if ( name == null ) name = questionName;
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
     * @return Returns the historyId.
     */
    public int getHistoryId() {
        return historyId;
    }
    
    /**
     * @return Returns the answer.
     * @throws WdkUserException
     */
    public Answer getAnswer() {
        return answer;
    }
    
    /**
     * @param answer
     *            The answer to set.
     */
    public void setAnswer( Answer answer ) {
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
    public void setEstimateSize( int estimateSize ) {
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
    public void setLastRunTime( Date lastRunTime ) {
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
        return answer.getIdsQueryInstance().getQuery().getSignature();
    }
    
    public String getChecksum() throws WdkModelException {
        return answer.getIdsQueryInstance().getChecksum();
    }

    /**
     * @return The RecordClass name.
     */
    public String getDataType() {
        return answer.getQuestion().getRecordClass().getFullName();
    }

    /**
     * Update the History last used time in the database.
     * @throws WdkUserException If the user doesn't exist.
     */
    public void update() throws WdkUserException {
        factory.updateHistory( user, this, true );
    }

    /**
     * Update the History.
     * @param updateTime Update the time or not.
     * @throws WdkUserException If the user cannot be associated with the history.
     */
    public void update(boolean updateTime) throws WdkUserException {
        factory.updateHistory(user, this, updateTime);
    }

    /**
     * Is this History object needed for other Histories.
     * @return True if this History object is need for others.
     * @throws WdkUserException If the there is a problem associating the user.
     * @throws WdkModelException If there is a problem with History objects.
     */
    public boolean isDepended() throws WdkUserException, WdkModelException {
        if ( isDepended == null ) computeDependencies( user.getHistories() );
        return isDepended;
    }

    /**
     * Determine whether this History object is used in any other
     * History objects.
     * 
     * @param histories A list of the user History objects.
     * @throws WdkModelException If a problem with the History objects exist.
     */
    void computeDependencies(History[] histories) throws WdkModelException {
        isDepended = false;
        for ( History history : histories ) {
            if ( history.historyId == this.historyId ) continue;
            Set< Integer > components = history.getComponentHistories();
            if ( components.contains( historyId ) ) {
                isDepended = true;
                break;
            }
        }
    }
    
    /**
     * @return get a list of history ID's this one depends on directly.
     * @throws WdkModelException
     */
    public Set< Integer > getComponentHistories() throws WdkModelException {
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
    
    public String getDescription() {
        return ( isBoolean ) ? booleanExpression : answer.getName();
    }
    
    /**
     * This method is used when the history is requested deleted by
     * the user but cannot be because of a dependency.
     * @return Was the history requested deleted by the user.
     */
    public boolean isDeleted() {
        return isDeleted;
    }
    
    /**
     * This method will set that this History was to be removed
     * from the database but could not be at this time because of
     * a dependency.
     * @param isDeleted
     *            The isDeleted to set.
     */
    public void setDeleted( boolean isDeleted ) {
        this.isDeleted = isDeleted;
    }
    
    /**
     * Get the name of the table the writeResultToTableName of the
     * QueryInstance wrote the cache to.
     * @return The name of the database table that was written to.
     * @throws WdkModelException If problem exists at some point in the model
     * 		   getting this value.
     */
    public String getCacheFullTable() throws WdkModelException {
        return answer.getIdsQueryInstance().getResultAsTableName();
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
