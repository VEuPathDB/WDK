package org.gusdb.wdk.model;
 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple implementation of QueryInstanceI; generally expects its subclasses
 * to do most of the real implementation.
 */
public abstract class QueryInstance {
    
    public static final String PARAM_CLOB_DIVIDER = "--WDK_CLOB_DIVIDER--";

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
     * Values that define this QueryInstance relative to others pointing to <code>query</code>
     */
    protected Map<String, Object> values = new LinkedHashMap<String, Object>();

    /**
     * Name of the table to join with when in join mode
     */
    protected String joinTableName;

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
     * the attributes query has an additional parameter, which is the
     * name of the result table
     */
    protected boolean isDynamic;

    /**
     * The unique name of the table in a database namespace which holds the cached 
     * results for this Instance.
     */
    String resultTableName = null;
    
    protected String checksum = null;


    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public void setValues(Map<String, Object> values) throws WdkUserException, WdkModelException {
	this.values = new LinkedHashMap<String, Object>(values);
    checksum = null;
	query.applyDefaults(values);
	query.validateParamValues(values);
    }

    public boolean getIsCacheable() {
	return isCacheable;
    }
    
    public boolean getJoinMode() {
	return joinMode;
    }

    public boolean getIsPersistent() {

	//eventually this will include whether this can be put into history as well
	return getIsCacheable();
    }
    
    public void setIsCacheable(boolean isCacheable) throws WdkModelException {
	if (isCacheable && !query.getIsCacheable().booleanValue()) {
	    throw new WdkModelException(query.getFullName() + " is not cacheable, but a query instance is, which is illegal");
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
                    "Attempting to get persistent result page, but query " +
                    "instance is not cacheable");

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
        if (resultTableName == null) 
            resultTableName = getResultFactory().getResultAsTableName(this);
        return resultTableName;
    }

    // ------------------------------------------------------------------
    // Package methods
    // ------------------------------------------------------------------

    public Collection getValues() {
	return values.values();
    }
    
    Integer getQueryInstanceId() {
	return queryInstanceId;
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
    void initJoinMode(String resultTableName,String projectColumnName, String primaryKeyColumnName, int startIndex, int endIndex, boolean isDynamic){
	if (resultTableName == null || resultTableName.equals("")) {
	    throw new RuntimeException("resultTableName is required but not provided");
	}
	this.joinTableName = resultTableName;
	this.projectColumnName = projectColumnName;
	this.primaryKeyColumnName = primaryKeyColumnName;
	this.startIndex = startIndex;
	this.endIndex = endIndex;
	this.joinMode = true;
	this.isDynamic = isDynamic;
    }

    public Collection getCacheValues() throws WdkModelException{
	return getValues();
    }
    
    public String getClobContent() {
        StringBuffer content = new StringBuffer();
        content.append(query.getFullName());
        for (String param : values.keySet()) {
            content.append(PARAM_CLOB_DIVIDER + param + "=" + values.get(param));
        }
        return content.toString();
    }
    
    public String getChecksum() throws WdkModelException {
        if (checksum == null) {
            try {
                // get the clob content: a combination of query name, param names and values
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

    public abstract String getLowLevelQuery() throws WdkModelException;

    // ------------------------------------------------------------------
    // Protected methods
    // ------------------------------------------------------------------

    protected Map<String, Object> getValuesMap() {
	return values;
    }

    protected QueryInstance (Query query) {
	this.query = query;
	this.isCacheable = query.getIsCacheable().booleanValue();
	this.joinMode = false;
    }

    protected ResultFactory getResultFactory() {
	return query.getResultFactory();
    }

    protected abstract ResultList getNonpersistentResult() throws WdkModelException;

    protected abstract void writeResultToTable(String resultTableName, 
					       ResultFactory rf) throws WdkModelException;
}
