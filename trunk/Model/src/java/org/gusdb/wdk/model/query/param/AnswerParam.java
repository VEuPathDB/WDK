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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         raw data: same as user-dependent data, it is a step id;
 * 
 *         user-dependent data: same as raw data, a step id;
 * 
 *         user-independent data: a key such as: answer_checksum:filter_name;
 *         the ":filter_name" is optional;
 * 
 *         internal data: a sql that represents the cached result
 * 
 */
public class AnswerParam extends Param {

    private static final String INDEPENDENT_PATTERN = "\\w+(\\:\\w+)?";;

    private String recordClassRef;
    private RecordClass recordClass;
    private AnswerFactory answerFactory;

    public AnswerParam() {}

    private AnswerParam(AnswerParam param) {
        super(param);
        this.recordClassRef = param.recordClassRef;
        this.recordClass = param.recordClass;
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
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) throws JSONException {
        // add recordClass ref
        jsParam.put("recordClass", recordClassRef);
    }

    public AnswerValue getAnswerValue(User user, String independentValue)
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        // this step might not be needed, since the input is never compressed
        independentValue = decompressValue(independentValue);

        // check format
        if (!independentValue.matches(INDEPENDENT_PATTERN))
            throw new WdkModelException("The input to answerParam should "
                    + "be in form such as 'answer_checksum[:<filter>]'; "
                    + "instead, it is '" + independentValue + "'");
        String[] parts = independentValue.split("\\:");
        String answerChecksum = parts[0];
        String filterName = (parts.length == 1) ? null : parts[1];

        Answer answer = answerFactory.getAnswer(user, answerChecksum);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue
     * (org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToIndependentValue(User user,
            String dependentValue) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        int stepId = Integer.parseInt(dependentValue);
        Step step = user.getStep(stepId);
        return step.getAnswer().getAnswerValue().getAnswerKey();
    }

    /**
     * basically this will create a new step using the params of the answer
     * 
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @see org.gusdb.wdk.model.query.param.Param#independentValueToDependentValue
     *      (org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String independentValueToDependentValue(User user,
            String independentValue) throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        AnswerValue answerValue = getAnswerValue(user, independentValue);
        Step step = user.createStep(answerValue, false);
        return Integer.toString(step.getDisplayId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
     * (java.lang.String)
     */
    @Override
    public String dependentValueToInternalValue(User user,
            String dependentValue) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        int stepId = Integer.parseInt(dependentValue);
        Step step = user.getStep(stepId);
        AnswerValue answerValue = step.getAnswer().getAnswerValue();
        return answerValue.getIdSql();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(java
     * .lang.String)
     */
    @Override
    public String dependentValueToRawValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        return dependentValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(java
     * .lang.String)
     */
    @Override
    public String rawOrDependentValueToDependentValue(User user, String rawValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        return rawValue;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    protected void validateValue(User user, String dependentValue)
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        int stepId = Integer.parseInt(dependentValue);
        Step step = user.getStep(stepId);
        // try to get the answer value
        step.getAnswer().getAnswerValue();
    }

}
