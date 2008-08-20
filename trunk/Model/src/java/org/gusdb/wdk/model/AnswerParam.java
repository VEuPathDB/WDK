package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.AnswerInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerParam extends Param {

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

    public String validateValue(Object value) throws WdkModelException {
        // the input should be an answer id
        if (value instanceof String) {
            try {
                AnswerFactory answerFactory = wdkModel.getAnswerFactory();
                AnswerInfo answerInfo = answerFactory.getAnswerInfo((String) value);
                if (answerInfo == null)
                    throw new WdkModelException("The answer of given id '"
                            + value + "' does not exist");
                return null;
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        } else {
            throw new WdkModelException("The input of answerParam "
                    + getFullName() + " should be a string. Instead, it was "
                    + value.getClass().getName());
        }
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
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
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
    public String getInternalValue(Object value) throws WdkModelException,
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        // get answer info
        String answerChecksum = (String) value;
        AnswerFactory answerFactory = wdkModel.getAnswerFactory();
        AnswerInfo answerInfo = answerFactory.getAnswerInfo(answerChecksum);
        if (answerInfo == null) {
            throw new WdkModelException("The answer of given checksum '"
                    + answerChecksum + "' does not exist");
        } else {
            // construct answer and cache the result
            Answer answer = answerFactory.getAnswer(answerInfo);

            // make sure the answer result is cached
            QueryInstance instance = answer.getIdsQueryInstance();
            ResultFactory resultFactory = wdkModel.getResultFactory();
            resultFactory.getInstanceId(instance);
        }
        // return the same answerChecksum, since it'll be used in the replaceSql
        return answerChecksum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#replaceSql(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String replaceSql(String sql, String answerChecksum)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        // get answer info
        AnswerFactory answerFactory = wdkModel.getAnswerFactory();
        AnswerInfo answerInfo = answerFactory.getAnswerInfo(answerChecksum);
        if (answerInfo == null)
            throw new WdkModelException("The answer of given checksum '"
                    + answerChecksum + "' does not exist");

        // get cache table name
        String questionName = answerInfo.getQuestionName();
        Question question = (Question) wdkModel.resolveReference(questionName);
        String queryName = question.getQuery().getFullName();
        String tableName = CacheFactory.normalizeTableName(queryName);

        // get query instance id
        Answer answer = answerFactory.getAnswer(answerInfo);
        ResultFactory resultFactory = wdkModel.getResultFactory();
        int instanceId = resultFactory.getInstanceId(answer.getIdsQueryInstance());

        // substitute the join conditions
        StringBuffer condition = new StringBuffer(tableName);
        condition.append(".").append(CacheFactory.COLUMN_INSTANCE_ID);
        condition.append(" = ").append(instanceId);
        sql = sql.replaceAll("\\$\\$" + name + "\\.condition\\$\\$",
                condition.toString());

        // substitute the cache table
        sql = sql.replaceAll("\\$\\$" + name + "\\$\\$", tableName);

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

}
