/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.History;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class AnswerParamBean extends ParamBean {

    private AnswerParam answerParam;

    private String historyKey;

    public AnswerParamBean(AnswerParam answerParam) {
        super(answerParam);
        this.answerParam = answerParam;
    }

    public HistoryBean[] getHistories(UserBean user) throws WdkUserException,
            WdkModelException, SQLException, JSONException, NoSuchAlgorithmException {
        RecordClass recordClass = answerParam.getRecordClass();
        return user.getHistories(recordClass.getFullName());
    }

    public void setHistoryKey(String historyKey) {
        this.historyKey = historyKey;
    }
    
    public HistoryBean getHistory() throws Exception {
        try {
            return new HistoryBean(answerParam.getHistory(historyKey));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public AnswerBean getAnswer() throws Exception {
        try {
            History history = answerParam.getHistory(historyKey);
            return new AnswerBean(history.getAnswer());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
