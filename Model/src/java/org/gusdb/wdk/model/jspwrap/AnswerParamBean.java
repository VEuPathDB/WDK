/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;

import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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

    public HistoryBean[] getHistories(UserBean user) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        RecordClass recordClass = answerParam.getRecordClass();
        return user.getHistories(recordClass.getFullName());
    }
}
