package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Answer;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         The input to a answerParam is <answer_checksum>[:<filter>]; 
 *         
 *         The output is a SQL that represents the cached id list.
 * 
 */
public class AnswerParam extends Param {

    private static final String INDEPENDENT_PATTERN = "\\w+(\\:\\w+)?";;
    private static final String DEPENDENT_PATTERN = "\\w+\\:\\d+";;

    private String recordClassRef;
    private RecordClass recordClass;
    private AnswerFactory answerFactory;

    public AnswerParam() {}

    private AnswerParam(AnswerParam param) {
        super(param);
        this.recordClassRef = param.recordClassRef;
        this.recordClass = param.recordClass;
    }

    @Override
    public void validateValue(String combinedKey) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // try to get an answerValue
        getAnswerValue(combinedKey);
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
        this.answerFactory = wdkModel.getAnswerFactory();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.Object)
     */
    @Override
    public String getInternalValue(String combinedKey)
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        AnswerValue answerValue = getAnswerValue(combinedKey);
        return answerValue.getIdSql();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) throws JSONException {
        // add recordClass ref
        jsParam.put("recordClass", recordClassRef);
    }

    public AnswerValue getAnswerValue(String combinedKey)
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        // this step might not be needed, since the input is never compressed
        combinedKey = decompressValue(combinedKey);

        // check format
        if (!combinedKey.matches(INDEPENDENT_PATTERN))
            throw new WdkModelException("The input to answerParam should "
                    + "be in form such as 'answer_checksum[:<filter>]'; "
                    + "instead, it is '" + combinedKey + "'");
        String[] parts = combinedKey.split("\\:");
        String answerChecksum = parts[0];
        String filterName = (parts.length == 1) ? null : parts[1];

        Answer answer = answerFactory.getAnswer(answerChecksum);
        if (answer == null)
            throw new WdkModelException("The answer of given id '"
                    + answerChecksum + "' does not exist");
        AnswerValue answerValue = answer.getAnswerValue();

        // verify the existence of the filter
        if (filterName != null) {
            AnswerFilterInstance filter = recordClass.getFilter(filterName);
            answerValue.setFilter(filter);
        }
        return answerValue;
    }

    /**
     * @param value
     *            The input is in user-dependent form: <user_key>:<step_id>
     * @return the output is user-independent form: <answer_key>[:<filter>]
     */
    @Override
    protected String getUserIndependentValue(String value)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        // verify input format
        if (!value.matches(DEPENDENT_PATTERN))
            throw new WdkModelException("The User dependent input '" + value
                    + "' to AnswerParam [" + getFullName()
                    + "] is not in form " + "of: <user_key>:<step_id>");
        String[] parts = value.split("\\:");
        String userSignature = parts[0];
        int stepId = Integer.parseInt(parts[1]);

        // get step
        UserFactory userFactory = wdkModel.getUserFactory();
        User user = userFactory.getUser(userSignature);
        Step step = user.getStep(stepId);

        // get answer value info
        AnswerValue answerValue = step.getAnswer().getAnswerValue();
        String answerChecksum = answerValue.getChecksum();
        AnswerFilterInstance filter = answerValue.getFilter();
        if (filter != null) answerChecksum += ":" + filter.getName();
        return answerChecksum;
    }
}
