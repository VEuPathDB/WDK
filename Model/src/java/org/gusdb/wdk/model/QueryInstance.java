package org.gusdb.wdk.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Simple implementation of QueryInstanceI; generally expects its subclasses to
 * do most of the real implementation.
 * <br/><br/>
 * Added By Doug:
 * This class is actually responsible for looking up the answers to Queries.
 * @see org.gusdb.wdk.model.QueryInstance.getNonpersistentResult
 * @see org.gusdb.wdk.model.QueryInstance.writeResultToTable
 * <br/><br/>
 * Subclasses are expected to implement those two methods so that results will
 * actually be retrieved.
 */
public abstract class QueryInstance {

    protected boolean isCacheable;

    /**
     * Query of which this QueryInstance is an invocation.
     */
    protected Query query;

    /**
     * ID of this QueryInstance as defined in the QueryInstance table.
     */
    protected Integer queryInstanceId;

    /**
     * Values that define this QueryInstance relative to others pointing to
     * <code>query</code>
     */
    protected Map<String, Object> values = new LinkedHashMap<String, Object>();

    /**
     * In the join table.
     */
    protected String primaryKeyColumnName;

    /**
     * In the join table
     */
    protected String projectColumnName;

    protected int startIndex;

    protected int endIndex;

    protected boolean joinMode;

    /**
     * the attributes query has an additional parameter, which is the name of
     * the result table
     */
    protected boolean isDynamic;

    protected String checksum = null;

    /**
     * The result message contains a description of the status of the result.
     * For example, for blast searh, if the blast returns no hit, the message
     * will contain the warning information provided by blast program.
     */
    protected String resultMessage;

    protected CacheTable cacheTable;

    protected Set<SortingColumn> sortingColumns;

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * This method will set the <code>Param</code> values for this
     * instance of a <code>Query</code>.
     * 
     * @param values A map with the parameter name as the key and the value as the object.
     * @throws WdkModelException if for some reason the values cannot be validated.
     */
    public void setValues(Map<String, Object> values) throws WdkModelException {
        this.values = new LinkedHashMap<String, Object>(values);
        checksum = null;
        query.applyDefaults(values);
        query.validateParamValues(values);
    }

    /**
     * Can we write this QueryInstance to a database table.
     * By default this should be yes to everything.
     *
     * (aside: If it's not true IDK how the WDK functions)
     * 
     * @return True if cacheable, false otherwise.
     */
    public boolean getIsCacheable() {
        return isCacheable;
    }

    /**
     * @return the cacheTable
     */
    public CacheTable getCacheTable() {
        return cacheTable;
    }
    
    /**
     * @param cacheTable the cacheTable to set
     */
    public void setCacheTable(CacheTable cacheTable) {
        this.cacheTable = cacheTable;
    }

    /**
     * Whether this query is going to be joined to a another table.
     * This really only seems to be important for attribute queries.
     * 
     * @return True if we will join against another table, false otherwise.
     */
    public boolean getJoinMode() {
        return joinMode;
    }

    /**
     * Currently this is the same as if this thing is cacheable.
     * 
     * (The value is subject to change in future revisions)
     * @return True if we can persist for history, false otherwise.
     */
    public boolean getIsPersistent() {

        // eventually this will include whether this can be put into history as
        // well
        return getIsCacheable();
    }

    /**
     * Set whether this QueryInstance will be put in the database as a table or not.
     * 
     * @param isCacheable True if we want cache.
     * @throws WdkModelException If the underlying <code>Query</code> is not cacheable.
     */
    public void setIsCacheable(boolean isCacheable) throws WdkModelException {
        if (isCacheable && !query.getIsCacheable().booleanValue()) {
            throw new WdkModelException(
                    query.getFullName()
                            + " is not cacheable, but a query instance is, which is illegal");
        }
        this.isCacheable = isCacheable;
    }

    /**
     * Get the originally declared query that was used to create
     * this QueryInstance.
     * 
     * @return The original Query.
     */
    public Query getQuery() {
        return this.query;
    }

    /**
     * Get a <code>ResultList</code> of the answer columns to the
     * Query.
     * <br/><br/>
     * (note: This does not do execution but ends up calling<br/>
     * 		  writeResultToTable or getNonpersistentResult<br/>
     * 		  depending on the return value of getIsPersistent)
     * 
     * @return A list of results to the executed query.
     * @throws WdkModelException When a problem occurs.
     */
    public ResultList getResult() throws WdkModelException {
        ResultList rl = getResultFactory().getResult(this);
        // modified by Jerric - do not check if SQL has more columns than
        // declared in the Query. This modification is required by boolean
        // operation + dynamic attributes
        // rl.checkQueryColumns(query, true, getIsCacheable() || joinMode);
        rl.checkQueryColumns(query, false, getIsCacheable() || joinMode);
        return rl;
    }

    /**
     * An analog to getResult. The difference being this only uses
     * ResultLists that have been saved in database cache table.
     * 
     * @param startRow The row in the table to start the results at.
     * @param endRow The row in the table at which the result will stop.
     * @return A ResultList of a cached query answer.
     * @throws WdkModelException If a problem occurs reading the table.
     */
    public ResultList getPersistentResultPage(int startRow, int endRow)
            throws WdkModelException {

        if (!getIsCacheable())
            throw new WdkModelException(
                    "Attempting to get persistent result page, but query "
                            + "instance is not cacheable");

        ResultList rl = getResultFactory().getPersistentResultPage(this,
                startRow, endRow);
        // modified by Jerric - do not check if SQL has more columns than
        // declared in the Query. This modification is required by boolean
        // operation + dynamic attributes
        // rl.checkQueryColumns(query, true, true);
        rl.checkQueryColumns(query, false, true);
        return rl;
    }

    /**
     * @return Full name of table containing result
     */
    public String getResultAsTableName() throws WdkModelException {
        if (cacheTable == null)
            return getResultFactory().getResultAsTableName(this);
        else return cacheTable.getCacheTableFullName();
    }

    /**
     * @return The values that the where given to each <code>Param</code>
     */
    public Collection getValues() {
        return values.values();
    }

    // ------------------------------------------------------------------
    // Package methods
    // ------------------------------------------------------------------

    Integer getQueryInstanceId() {
        return queryInstanceId;
    }

    String[] getPrimaryKeyColumns() {
        if (projectColumnName == null || projectColumnName.length() == 0) {
            return new String[]{ primaryKeyColumnName };
        } else {
            return new String[]{ projectColumnName, primaryKeyColumnName };
        }
    }

    void setQueryInstanceId(Integer queryInstanceId) {
        this.queryInstanceId = queryInstanceId;
    }

    /**
     * Tell this <code>QueryInstance</code> that it will be joined
     * against another database table to provide a full result.
     * 
     * @param cacheTable The table to join against.
     * @param projectColumnName The project id column name.
     * @param primaryKeyColumnName The name of the column that acts as primary key.
     * @param startIndex The index at which to start results.
     * @param endIndex The index at which to end results.
     */
    void initJoinMode(CacheTable cacheTable, String projectColumnName,
            String primaryKeyColumnName, int startIndex, int endIndex, 
            boolean isDynamic) {
        this.cacheTable = cacheTable;
        this.projectColumnName = projectColumnName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.joinMode = true;
        this.isDynamic = isDynamic;
    }
    
    /**
     * @param sortingColumns the sortingColumns to set
     */
    void setSortingColumns(Set<SortingColumn> sortingColumns) {
        this.sortingColumns = sortingColumns;
   }

    /**
     * @see org.gusdb.wdk.model.QueryInstance.getValues
     */
    public Collection getCacheValues() throws WdkModelException {
        return getValues();
    }

    /**
     * The ClobContent for the QueryInstance.
     * This is also known as String holding the Parameter=Value
     * pairs.
     * 
     * @return A String of Parameter=Value pairs.
     */
    public String getParamsContent() {
        // get parameter name list, and sort it
        String[] paramNames = new String[values.size()];
        values.keySet().toArray(paramNames);
        Arrays.sort(paramNames);

        // concatenate parameter name, type, and values
        StringBuffer content = new StringBuffer();
        for (String paramName : paramNames) {
            content.append(Utilities.DATA_DIVIDER);
            content.append(paramName);
            content.append('=');
            content.append(values.get(paramName));
        }
        return content.toString();
    }

	public String getQueryInstanceContent() {
		StringBuffer sb = new StringBuffer();
		sb.append(query.getProjectId());
		sb.append(Utilities.DATA_DIVIDER);
		sb.append(query.getFullName());
		sb.append(Utilities.DATA_DIVIDER);
		sb.append(getParamsContent());
		return sb.toString();
	}
	
    /**
     * Used by the UserFactory on saving history. This will
     * uniquely identify the QueryInstance in the database in
     * relation to a user.
     * 
     * @return A checksum of values in the QueryInstance.
     * @throws WdkModelException If the CheckSum algorithm does not exist.
     */
public String getChecksum() throws WdkModelException {
		if (checksum == null) {
			// get the clob content: a combination of query name, param
			// names and values
			String content = getQueryInstanceContent();
			checksum = Utilities.encrypt(content);
		}
		return checksum;
	}

    /**
     * @return Returns the resultMessage.
     */
    public String getResultMessage() {
        return (resultMessage == null) ? "" : resultMessage;
    }

    /**
     * @param resultMessage The resultMessage to set.
     */
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    /**
     * @return The <code>Param</code>=Value map.
     */
    public Map<String, Object> getValuesMap() {
        return values;
    }

    /**
     * The actual query that was entered by the user.
     * 
     * @return The actual user query.
     * @throws WdkModelException If there's a problem getting that query.
     */
    public abstract String getLowLevelQuery() throws WdkModelException;

    // ------------------------------------------------------------------
    // Protected methods
    // ------------------------------------------------------------------

    protected QueryInstance(Query query) {
        this.query = query;
        this.isCacheable = query.getIsCacheable().booleanValue();
        this.joinMode = false;
        this.sortingColumns = new LinkedHashSet< SortingColumn >();
    }

    /**
     * @return Returns the <code>ResultFactory</code> that will
     * 		   create <code>ResultList</code>s for this Query.
     */
    protected ResultFactory getResultFactory() {
        return query.getResultFactory();
    }
    
    protected int getSortingIndex() throws WdkModelException {
        // get sorting index, may involve creating sorting cache
        return cacheTable.getSortingIndex(sortingColumns);
    }

    /**
     * Added By Doug:
     * 
     * This method will actually perform the query and get results from
     * the query.
     * 
     * @return A list of results from the performed query.
     * @throws WdkModelException If there is a problem performing the
     * 							 query.
     */
    protected abstract ResultList getNonpersistentResult()
            throws WdkModelException;

    /**
     * Added By Doug:
     * 
     * This method will do the same as <code>getNonpersistentResult</code>.
     * The difference between the two being that this method will save the
     * ResultList to a table in the database.
     * 
     * @param resultTableName The name of the table in which to store the results.
     * @param rf The ResultFactory responsible for producing <code>ResultsList</code>s.
     * @throws WdkModelException If we cannot write to the SQLTable or get the results.
     */
    protected abstract void writeResultToTable(String resultTableName,
            ResultFactory rf) throws WdkModelException;
}
