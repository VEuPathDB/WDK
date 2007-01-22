/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author xingao
 * 
 */
public class DatasetParam extends Param {

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
            return "Invalid value format of DatasetParam " + name + ": "
                    + value;

        // the input have a valid user id and dataset id
        String signature = parts[0].trim();
        String strdsId = parts[1].trim();
        if (strdsId.matches("\\d+")) {
            int datasetId = Integer.parseInt(strdsId);
            try {
                User user = factory.loadUserBySignature(signature);
                user.getDataset(datasetId);
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
        DatasetParam param = new DatasetParam();
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
        int datasetId = Integer.parseInt(strHistId);
        try {
            User user = factory.loadUserBySignature(signature);
            Dataset dataset = user.getDataset(datasetId);
            return dataset.getCacheFullTable();
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

}
