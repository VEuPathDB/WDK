/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author xingao
 * 
 */
public class HistoryParam extends Param {

    private UserFactory factory;

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    @Override
    public String validateValue(Object value) throws WdkModelException {
        String compound = value.toString();
        String[] parts = compound.split(":");
        if (parts.length != 2)
            return "Invalid value format of HistoryParam " + name + ": "
                    + value;

        // the input have a valid user id and history id
        String signature = parts[0].trim();
        String strHistId = parts[1].trim();
        if (strHistId.matches("\\d+")) {
            int historyId = Integer.parseInt(strHistId);
            try {
                User user = factory.loadUserBySignature(signature);
                user.getHistory(historyId);
            } catch (WdkUserException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            }
            return null;
        } else {
            return "Invalid value format of HistoryParam " + name + ": "
                    + value;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void resolveReferences(WdkModel model) throws WdkModelException {}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#setResources(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void setResources(WdkModel model) throws WdkModelException {
        try {
            factory = model.getUserFactory();
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        HistoryParam param = new HistoryParam();
        super.clone(param);
        param.factory = this.factory;
        return param;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    protected String getInternalValue(String value) throws WdkModelException {
        String[] parts = value.split(":");
        // the input have a valid user id and history id
        String signature = parts[0].trim();
        String strHistId = parts[1].trim();
        int historyId = Integer.parseInt(strHistId);
        try {
            User user = factory.loadUserBySignature(signature);
            History history = user.getHistory(historyId);
            Answer answer = history.getAnswer();
            Integer datasetId = answer.getDatasetId();
            return datasetId.toString();
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

}
