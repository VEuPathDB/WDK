/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

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
        RecordClass recordClass = answerParam.getRecordClass();
        return user.getSteps(recordClass.getFullName());
    }

    public AnswerValueBean getAnswerValue() throws Exception {
        try {
            User user = this.user.getUser();
            String independentValue = answerParam.dependentValueToIndependentValue(
                    user, dependentValue);
            AnswerValue answerValue = answerParam.getAnswerValue(user,
                    independentValue);
            return new AnswerValueBean(answerValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
