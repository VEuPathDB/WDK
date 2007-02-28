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
        // bypass the validation of DatasetParam, since there can be different
        // inputs for it
        return null;
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
     * (non-Javadoc) The internal value for the DatasetParam will be the
     * dataset_id
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    protected String getInternalValue(String value) throws WdkModelException {
        try {
            Dataset dataset = getDataset(value);
            return Integer.toString(dataset.getDatasetId());
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    public UserFactory getUserFactory() {
        return factory;
    }

    public Dataset getDataset(String combinedId) throws WdkModelException,
            WdkUserException {
        // at this point, the input value should be formatted as
        // signature:dataset_id
        String[] parts = combinedId.split(":");
        if (parts.length != 2)
            throw new WdkModelException("Invalid value for DatasetParam "
                    + name + ": '" + combinedId + "'");

        String signature = parts[0].trim();
        int datasetId = Integer.parseInt(parts[1]);

        // make sure the dataset belongs to this user
        User user = factory.loadUserBySignature(signature);
        return user.getDataset(datasetId);
    }
}
