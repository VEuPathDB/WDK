/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class AnswerParamBean extends ParamBean {

    private AnswerParam answerParam;

    public AnswerParamBean(AnswerParam answerParam) {
        super(answerParam);
        this.answerParam = answerParam;
    }

    public StepBean[] getSteps(UserBean user) throws WdkUserException,
            WdkModelException, SQLException, JSONException,
            NoSuchAlgorithmException {
        // only get the steps for the first record class
        Map<String, RecordClass> recordClasses = answerParam.getRecordClasses();
        RecordClass recordClass = recordClasses.values().iterator().next();
        return user.getSteps(recordClass.getFullName());
    }

    public AnswerValueBean getAnswerValue() throws Exception {
        try {
            User user = this.user.getUser();
            AnswerValue answerValue = answerParam.getAnswerValue(user,
                    dependentValue);
            return new AnswerValueBean(answerValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * @param recordClassName
     * @return
     * @see org.gusdb.wdk.model.query.param.AnswerParam#allowRecordClass(java.lang.String)
     */
    public boolean allowRecordClass(String recordClassName) {
        return answerParam.allowRecordClass(recordClassName);
    }
}
