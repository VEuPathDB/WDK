package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONException;

/**
 * A wrapper on a {@link Param} that provides simplified access for consumption
 * by a view
 */
public abstract class ParamBean {

    // private static Logger logger = Logger.getLogger( ParamBean.class );

    protected UserBean user;
    protected String dependentValue;
    protected int truncateLength;
    private Param param;

    public ParamBean(Param param) {
        this.param = param;
        truncateLength = Utilities.TRUNCATE_DEFAULT;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public String getName() {
        return param.getName();
    }

    public String getId() {
        return param.getId();
    }

    public String getFullName() {
        return param.getFullName();
    }

    public String getPrompt() {
        return param.getPrompt();
    }

    public String getHelp() {
        return param.getHelp();
    }

    public String getDefault() throws NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException, WdkModelException {
        return param.getDefault();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#isReadonly()
     */
    public boolean getIsReadonly() {
        return this.param.isReadonly();
    }

	public boolean getIsAllowEmpty(){
		return this.param.isAllowEmpty();
	}

	public String getEmptyValue(){
		return this.param.getEmptyValue();
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#isVisible()
     */
    public boolean getIsVisible() {
        return this.param.isVisible();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Param#getGroup()
     */
    public GroupBean getGroup() {
        return new GroupBean(param.getGroup());
    }

    /**
     * for controller
     * 
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public void validate(UserBean user, String rawOrDependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        param.validate(user.getUser(), rawOrDependentValue);
    }

    public void setDependentValue(String dependentValue) {
        this.dependentValue = dependentValue;
    }

    public String getRawValue() throws Exception {
        try {
        return param.dependentValueToRawValue(user.getUser(), dependentValue);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public String getBriefRawValue() throws Exception {
        try {
        String rawValue = getRawValue();
        if (rawValue != null) {
            rawValue = rawValue.replaceAll("\\,", ", ");
            if (rawValue.length() > truncateLength)
                rawValue = rawValue.substring(0, truncateLength) + "...";
        }
        return rawValue;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void setTruncateLength(int truncateLength) {
        if (truncateLength >= 0) {
            this.truncateLength = truncateLength;
        }
    }

    /**
     * @param user
     * @param dependentValue
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String)
     */
    public String dependentValueToIndependentValue(UserBean user,
            String dependentValue) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        return param.dependentValueToIndependentValue(user.getUser(),
                dependentValue);
    }

    /**
     * @param user
     * @param independentValue
     * @return
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String)
     */
    public String dependentValueToRawValue(UserBean user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        return param.dependentValueToRawValue(user.getUser(), dependentValue);
    }

    /**
     * @param user
     * @param rawValue
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String)
     */
    public String rawOrDependentValueToDependentValue(UserBean user,
            String rawValue) throws NoSuchAlgorithmException,
            WdkModelException, WdkUserException, SQLException, JSONException {
        return param.rawOrDependentValueToDependentValue(user.getUser(),
                rawValue);
    }

    public UserBean getUser() {
        return this.user;
    }
}
