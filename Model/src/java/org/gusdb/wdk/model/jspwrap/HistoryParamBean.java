/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

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
}
