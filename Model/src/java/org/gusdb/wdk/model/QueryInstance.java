package org.gusdb.wdk.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Simple implementation of QueryInstanceI; generally expects its subclasses to
 * do most of the real implementation.
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

    public void setValues(Map<String, Object> values) throws WdkModelException {
        this.values = new LinkedHashMap<String, Object>(values);
        checksum = null;
        query.applyDefaults(values);
        query.validateParamValues(values);
    }

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

    public boolean getJoinMode() {
        return joinMode;
    }

    public boolean getIsPersistent() {

        // eventually this will include whether this can be put into history as
        // well
        return getIsCacheable();
    }

    public void setIsCacheable(boolean isCacheable) throws WdkModelException {
        if (isCacheable && !query.getIsCacheable().booleanValue()) {
            throw new WdkModelException(
                    query.getFullName()
                            + " is not cacheable, but a query instance is, which is illegal");
        }
        this.isCacheable = isCacheable;
    }

    public Query getQuery() {
        return this.query;
    }

    public ResultList getResult() throws WdkModelException {
        ResultList rl = getResultFactory().getResult(this);
        // modified by Jerric - do not check if SQL has more columns than
        // declared in the Query. This modification is required by boolean
        // operation + dynamic attributes
        // rl.checkQueryColumns(query, true, getIsCacheable() || joinMode);
        rl.checkQueryColumns(query, false, getIsCacheable() || joinMode);
        return rl;
    }

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
     * @param resultTableName
     * @param projectColumnName
     * @param primaryKeyColumnName
     * @param startIndex
     * @param endIndex
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

    public Collection getCacheValues() throws WdkModelException {
        return getValues();
    }

    public String getClobContent() {
        // get parameter name list, and sort it
        String[] paramNames = new String[values.size()];
        values.keySet().toArray(paramNames);
        Arrays.sort(paramNames);

        // query content is a concatenation of project id, query full name, and param-value paires
        StringBuffer content = new StringBuffer();
        content.append(WdkModel.INSTANCE.getProjectId());
        content.append(WdkModel.PARAM_DIVIDER);
        content.append(query.getFullName());
        for (String paramName : paramNames) {
            content.append(WdkModel.PARAM_DIVIDER);
            content.append(paramName);
            content.append('=');
            content.append(values.get(paramName));
        }
        return content.toString();
    }

    public String getChecksum() throws WdkModelException {
        if (checksum == null) {
            try {
                // get the clob content: a combination of query name, param
                // names and values
                String content = getClobContent();
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] byteBuffer = digest.digest(content.getBytes());
                // convert each byte into hex format
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < byteBuffer.length; i++) {
                    int code = (byteBuffer[i] & 0xFF);
                    if (code < 0x10) buffer.append('0');
                    buffer.append(Integer.toHexString(code));
                }
                checksum = buffer.toString();
            } catch (NoSuchAlgorithmException ex) {
                throw new WdkModelException(ex);
            }
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

    public Map<String, Object> getValuesMap() {
        return values;
    }

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

    protected ResultFactory getResultFactory() {
        return query.getResultFactory();
    }
    
    protected int getSortingIndex() throws WdkModelException {
        // get sorting index, may involve creating sorting cache
        return cacheTable.getSortingIndex(sortingColumns);
    }

    protected abstract ResultList getNonpersistentResult()
            throws WdkModelException;

    protected abstract void writeResultToTable(String resultTableName,
            ResultFactory rf) throws WdkModelException;
}
