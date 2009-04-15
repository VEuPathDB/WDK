package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.QueryInfo;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         external value of the AnswerParam should be history id; the value
 *         stored in the history.displayparams should be history ids too;
 * 
 *         the independent stored in the answer.params should be
 *         answer_checksum:filter;
 * 
 *         the internal value should be historyKey too;
 */
public class AnswerParam extends Param {

    /**
     * independent input is an answer_checksum:filter_name;
     */
    private static final String HISTORY_KEY = "\\w+\\:\\d+";

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    private String recordClassRef;
    private RecordClass recordClass;
    private WdkModel wdkModel;

    public AnswerParam() {}

    private AnswerParam(AnswerParam param) {
        super(param);
        this.recordClassRef = param.recordClassRef;
        this.recordClass = param.recordClass;
        this.wdkModel = param.wdkModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    public String validateValue(Object objHistoryKey) throws WdkModelException {
        // validate the input value by getting the history
        try {
            History history = getHistory(objHistoryKey);
            if (history == null || !history.isValid())
                throw new WdkModelException("The history " + objHistoryKey
                        + " is invalid.");
        } catch (Exception ex) {
            throw new WdkModelException(ex);
        }
        return null;
    }

    // ///////////////////////////////////////////////////////////////
    // protected methods
    // ///////////////////////////////////////////////////////////////

    /**
     * @return the recordClassRef
     */
    public String getRecordClassRef() {
        return recordClassRef;
    }

    /**
     * @param recordClassRef
     *            the recordClassRef to set
     */
    public void setRecordClassRef(String recordClassRef) {
        this.recordClassRef = recordClassRef;
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new AnswerParam(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve recordClass ref
        this.recordClass = (RecordClass) model.resolveReference(recordClassRef);
        this.wdkModel = model;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.Object)
     */
    @Override
    public String getInternalValue(Object objHistoryKey)
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        return objHistoryKey.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#replaceSql(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String replaceSql(String sql, String historyKey)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        History history = getHistory(historyKey);

        // get answer info

        // get cache table name

        // get query instance id
        Answer answer = history.getAnswer();
        CacheFactory cacheFactory = wdkModel.getResultFactory().getCacheFactory();
        Query query = answer.getIdsQueryInstance().getQuery();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(query);
        int instanceId = answer.getIdsQueryInstance().getInstanceId();

        // construct the inner query that will replace the answerParam macro
        StringBuffer innerSql = new StringBuffer("SELECT * FROM ");
        innerSql.append(queryInfo.getCacheTable());
        innerSql.append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_ID);
        innerSql.append(" = ").append(instanceId);

        // apply filter if needed
        String inner = innerSql.toString();
        AnswerFilterInstance filter = answer.getFilter();
        if (filter != null) inner = filter.applyFilter(inner);

        // replace the answer param with the nested sql
        sql = sql.replaceAll("\\$\\$" + name + "\\$\\$", "(" + inner + ")");

        return sql;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) {
    // nothing to add
    }

    public History getHistory(Object objHistoryKey) throws WdkModelException,
            SQLException, JSONException, WdkUserException {
        String errMessage = "the input of the answerParam [" + getName()
                + "] should be of format user_checksum:history_id";
        if (!(objHistoryKey instanceof String))
            throw new WdkUserException(errMessage);
        String historyKey = (String) objHistoryKey;
        if (!historyKey.matches(HISTORY_KEY))
            throw new WdkUserException(errMessage);

        String[] parts = historyKey.split(":");
        String signature = parts[0].trim();
        int historyId = Integer.parseInt(parts[1].trim());
        UserFactory userFactory = wdkModel.getUserFactory();
        User user = userFactory.loadUserBySignature(signature);
        return user.getHistory(historyId);
    }

    @Override
    public Object dependentValueToIndependentValue(Object dependentValue)
            throws WdkModelException, SQLException, JSONException,
            WdkUserException, NoSuchAlgorithmException {
        History history = getHistory(dependentValue);
        return history.getAnswer().getAnswerKey();
    }
}
