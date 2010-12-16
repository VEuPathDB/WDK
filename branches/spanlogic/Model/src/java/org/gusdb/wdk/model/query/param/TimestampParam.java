/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         the four types of values are identical.
 */
public class TimestampParam extends Param {

    /**
     * 
     */
    public TimestampParam() {}

    /**
     * @param param
     */
    public TimestampParam(TimestampParam param) {
        super(param);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#appendJSONContent(org.json.JSONObject
     * )
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException {
    // nothing to add
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.param.Param#clone()
     */
    @Override
    public Param clone() {
        return new TimestampParam(this);
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
        return dependentValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#dependentValueToInternalValue(org
     * .gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToInternalValue(User user,
            String dependentValue) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return dependentValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#dependentValueToRawValue(org.gusdb
     * .wdk.model.user.User, java.lang.String)
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
     * org.gusdb.wdk.model.query.param.Param#rawOrDependentValueToDependentValue
     * (org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String rawOrDependentValueToDependentValue(User user, String rawValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        return rawValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model
     * .user.User, java.lang.String)
     */
    @Override
    protected void validateValue(User user, String rawOrDependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // the value of timestamp can be any string
        // make sure the value is in valid time format
        // try {
        //    Date date = DateFormat.getDateTimeInstance().parse(
        //            rawOrDependentValue);
        //    if (date == null)
        //        throw new WdkModelException("Invalid timestampParam value; '"
        //                + rawOrDependentValue + "'");
        //} catch (ParseException ex) {
        //    throw new WdkModelException(ex);
        //}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
    // do nothing.
    }

    /**
     * it is always false (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.param.Param#isVisible()
     */
    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public String getDefault() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    @Override
    protected void applySuggection(ParamSuggestion suggest) {
        // do nothing
    }
}
