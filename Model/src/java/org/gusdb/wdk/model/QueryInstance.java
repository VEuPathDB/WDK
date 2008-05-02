package org.gusdb.wdk.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.implementation.SqlQuery;

/**
 * Simple implementation of QueryInstanceI; generally expects its subclasses to
 * do most of the real implementation.
 */
public abstract class QueryInstance {

    // private static Logger logger = Logger.getLogger( QueryInstance.class );

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
    protected int signal;

    protected CacheTable cacheTable;

    protected Set<SortingColumn> sortingColumns;

    /**
     * The type of the records the query returns. Only applied to id query
     */
    protected RecordClass recordClass;

    protected Object subTypeValue;

    protected boolean expandSubType = false;

    protected QueryInstance(Query query) {
        this.query = query;
        this.isCacheable = query.getIsCacheable().booleanValue();
        this.joinMode = false;
        this.sortingColumns = new LinkedHashSet<SortingColumn>();
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public void setValues(Map<String, Object> values) throws WdkModelException {
        checksum = null;
        query.applyDefaults(values);
        query.validateParamValues(values);
        this.values = new LinkedHashMap<String, Object>(values);
    }

    public void setSubTypeValue(Object values) {
        this.subTypeValue = values;
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
     * @param cacheTable
     *            the cacheTable to set
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
        if (cacheTable == null) return getResultFactory().getResultAsTableName(
                this);
        else return cacheTable.getCacheTableFullName();
    }

    public Collection<Object> getValues() {
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
            return new String[] { primaryKeyColumnName };
        } else {
            return new String[] { projectColumnName, primaryKeyColumnName };
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
     * @param sortingColumns
     *            the sortingColumns to set
     */
    void setSortingColumns(Set<SortingColumn> sortingColumns) {
        this.sortingColumns = sortingColumns;
    }

    public Collection<Object> getCacheValues() throws WdkModelException {
        return getValues();
    }

    public String getParamsContent() {
        StringBuffer content = new StringBuffer();
        content.append(query.getFullName());

        // append sub types, if presented
        if (recordClass != null && recordClass.getSubType() != null) {
            content.append(Utilities.DATA_DIVIDER);
            content.append(Utilities.SUB_TYPE);
            content.append(Utilities.DATA_DIVIDER);
            content.append(subTypeValue);
        }
        // get parameter name list, and sort it
        String[] paramNames = new String[values.size()];
        values.keySet().toArray(paramNames);
        Arrays.sort(paramNames);

        // concatenate parameter name, type, and values
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

        sb.append(getParamsContent());
        return sb.toString();
    }

    public String getChecksum() throws WdkModelException {
        if (checksum == null) {
            // get the clob content: a combination of query name, param
            // names and values
            String content = getQueryInstanceContent();

            // TEST
            // logger.info( content );

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
     * @param resultMessage
     *            The resultMessage to set.
     */
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Map<String, Object> getValuesMap() {
        return values;
    }

    public void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    public Object getSubTypeValue() {
        return subTypeValue;
    }

    /**
     * @return the expandSubType
     */
    public boolean isExpandSubType() {
        return expandSubType;
    }

    /**
     * @param expandSubType the expandSubType to set
     */
    public void setExpandSubType(boolean expandSubType) {
        this.expandSubType = expandSubType;
    }

    protected String getSqlForBooleanOp(String[] commonColumns) throws WdkModelException {
        ResultFactory resultFactory = query.getResultFactory();
        String resultTableName = resultFactory.getResultAsTableName(this); // ensures

        // check if the expanding needs to be performed
        SubType subType = recordClass.getSubType();
        if (subType != null && expandSubType && !subType.isQuestionOnly()) {
                return getTransformerSql(resultTableName);
        }

        // no need to transform
        StringBuffer selectb = new StringBuffer("SELECT ");
        boolean first = true;
        for (String name : commonColumns) {
            if (first) first = false;
            else selectb.append(", ");
            selectb.append(name);
        }
        selectb.append(" FROM " + resultTableName);
        return selectb.toString();
    }

    public abstract String getLowLevelQuery() throws WdkModelException;

    // ------------------------------------------------------------------
    // Protected methods
    // ------------------------------------------------------------------

    protected ResultFactory getResultFactory() {
        return query.getResultFactory();
    }

    protected int getSortingIndex() throws WdkModelException {
        // get sorting index, may involve creating sorting cache
        return cacheTable.getSortingIndex(sortingColumns);
    }

    protected String getFilterSql(String resultValue)
            throws WdkModelException {
        SubType subType = recordClass.getSubType();
        SqlQuery filterQuery = subType.getFilterQuery();

        Map<String, Object> filterValues = new LinkedHashMap<String, Object>();
        filterValues.put(subType.getResultParam().getName(), "temp");
        filterValues.put(subType.getSubTypeParam().getName(), subTypeValue);

        Map<String, String> values = filterQuery.getInternalParamValues(filterValues);
        // set the resultValue here to avoid the SQL being transformed
        // incorrectly
        values.put(subType.getResultParam().getName(), resultValue);

        return filterQuery.instantiateSql(values);
    }
    
    private String getTransformerSql(String resultTable) throws WdkModelException {
        SubType subType = recordClass.getSubType();
        SqlQuery transformQuery = subType.getTransformQuery();

        Map<String, Object> transfomrerValues = new LinkedHashMap<String, Object>();
        transfomrerValues.put(subType.getResultParam().getName(), resultTable);
        
        transformQuery.applyDefaults(transfomrerValues);
        
        Map<String, String> values = transformQuery.getInternalParamValues(transfomrerValues);
        
        return transformQuery.instantiateSql(values);
    }

    protected abstract ResultList getNonpersistentResult()
            throws WdkModelException;

    protected abstract void writeResultToTable(String resultTableName,
            ResultFactory rf) throws WdkModelException;
}
