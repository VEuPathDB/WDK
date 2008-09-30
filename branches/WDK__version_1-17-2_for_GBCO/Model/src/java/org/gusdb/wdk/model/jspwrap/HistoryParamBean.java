/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;

/**
 * @author xingao
 * 
 */
public class HistoryParamBean extends ParamBean {

    private HistoryParam historyParam;
    private String combinedId;

    public HistoryParamBean(HistoryParam historyParam) {
        super(historyParam);
        this.historyParam = historyParam;
    }

    public void setCombinedId(String combinedId) {
        this.combinedId = combinedId;
    }

    public HistoryBean getHistory() throws WdkUserException, WdkModelException {
        return new HistoryBean(historyParam.getHistory(combinedId));
    }

    public HistoryBean[] getHistories(UserBean userBean)
            throws WdkUserException, WdkModelException {
        User user = userBean.getUser();
        History[] histories = historyParam.getHistories(user);
        List<HistoryBean> historyBeans = new ArrayList<HistoryBean>();
        for (History history : histories) {
            historyBeans.add(new HistoryBean(history));
        }
        HistoryBean[] array = new HistoryBean[historyBeans.size()];
        historyBeans.toArray(array);
        return array;
    }
}
