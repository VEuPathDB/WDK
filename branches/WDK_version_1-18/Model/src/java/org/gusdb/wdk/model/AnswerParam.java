package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.QueryInfo;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.AnswerInfo;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         external value of the AnswerParam should be history id; the value
 *         stored in the history.displayparams should be history ids too; the
 *         value stored in the answer.params should be answer_checksum:filter;
 *         the internal value is answer_checksum;
 */
public class AnswerParam extends Param {

    /**
     * dependent input is a step_id:user_checksum combo;
     */
    private static final String ANSWER_CHECKSUM = "\\w+\\:\\w+";

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

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    public String validateValue(Object objHistoryKey) throws WdkModelException {
        // validate the input value by getting the history
        try {
            History history = getHistory(objHistoryKey);
            if (history == null || !history.isValid())
                throw new WdkModelException("The history " + objHistoryKey + " is invalid.");
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
    public String getInternalValue(Object objHistoryKey) throws WdkModelException,
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        History history = getHistory(objHistoryKey);
        Answer answer = history.getAnswer();
        return answer.getAnswerKey();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#replaceSql(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String replaceSql(String sql, String answerKey) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        String[] parts = parseChecksum(answerKey);
        String answerChecksum = parts[0];
        String filterName = parts[1];

        // get answer info
        AnswerFactory answerFactory = wdkModel.getAnswerFactory();
        AnswerInfo answerInfo = answerFactory.getAnswerInfo(answerChecksum);
        if (answerInfo == null)
            throw new WdkModelException("The answer of given checksum '"
                    + answerChecksum + "' does not exist");

        // get cache table name
        String questionName = answerInfo.getQuestionName();
        Question question = (Question) wdkModel.resolveReference(questionName);
        CacheFactory cacheFactory = wdkModel.getResultFactory().getCacheFactory();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(question.getQuery());

        // get query instance id
        Answer answer = answerFactory.getAnswer(answerInfo);
        int instanceId = answer.getIdsQueryInstance().getInstanceId();

        // construct the inner query that will replace the answerParam macro
        StringBuffer innerSql = new StringBuffer("SELECT * FROM ");
        innerSql.append(queryInfo.getCacheTable());
        innerSql.append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_ID);
        innerSql.append(" = ").append(instanceId);

        // apply filter if needed
        String inner = innerSql.toString();
        if (filterName != null) {
            AnswerFilterInstance filter = recordClass.getFilter(filterName);
            inner = filter.applyFilter(inner);
        }

        // replace the answer param with the nested sql
        sql = sql.replaceAll("\\$\\$" + name + "\\$\\$", "(" + inner + ")");

        return sql;
    }

    public Answer getAnswer(String answerKey) throws WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException,
            SQLException {
        String[] parts = parseChecksum(answerKey);
        String checksum = parts[0];
        String filterName = parts[1];

        AnswerFactory answerFactory = wdkModel.getAnswerFactory();
        AnswerInfo answerInfo = answerFactory.getAnswerInfo(checksum);
        if (answerInfo == null)
            throw new WdkModelException("The answer of given id '" + checksum
                    + "' does not exist");
        Answer answer = answerFactory.getAnswer(answerInfo);

        // verify the existance of the filter
        if (filterName != null) {
            AnswerFilterInstance filter = recordClass.getFilter(filterName);
            answer.setFilter(filter);
        }
        return answer;
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

    private String[] parseChecksum(String value) {
        value = value.trim();
        // check if the filter is specified
        int pos = value.indexOf(":");
        String checksum = value;
        String filter = null;
        if (pos >= 0) {
            filter = value.substring(pos + 1);
            if (filter.length() == 0) filter = null;
            checksum = value.substring(0, pos);
        }
        return new String[] { checksum, filter };
    }

    private History getHistory(Object objHistoryKey) throws WdkModelException, SQLException, JSONException, WdkUserException {
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
}
