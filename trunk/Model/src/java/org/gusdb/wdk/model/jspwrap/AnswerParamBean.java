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
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class AnswerParamBean extends ParamBean {

    private AnswerParam answerParam;

    private String answerChecksum;

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

    /**
     * It's okay to set bean property, since the bean is created for every
     * request and thus has a local (request) level life span.
     * 
     * @param checksum
     */
    public void setAnswerChecksum(String checksum) {
        this.answerChecksum = checksum;
    }

    public AnswerValueBean getAnswerValue() throws Exception {
        try {
            AnswerValue answerValue = answerParam.getAnswerValue(answerChecksum);
            return new AnswerValueBean(answerValue);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
