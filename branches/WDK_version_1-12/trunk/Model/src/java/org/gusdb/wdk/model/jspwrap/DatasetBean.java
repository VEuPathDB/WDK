/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Date;

import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Dataset;


/**
 * @author xingao
 *
 */
public class DatasetBean {

    private Dataset dataset;
    
    public DatasetBean(Dataset dataset) {
        this.dataset = dataset;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#getCreateTime()
     */
    public Date getCreateTime() {
        return dataset.getCreateTime();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#getDatasetId()
     */
    public int getDatasetId() {
        return dataset.getDatasetId();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#getDatasetName()
     */
    public String getDatasetName() {
        return dataset.getDatasetName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#getDataType()
     */
    public String getDataType() {
        return dataset.getDataType();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#getValues()
     */
    public String[][] getValues() throws WdkUserException {
        return dataset.getValues();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#isTemporary()
     */
    public boolean getTemporary() {
        return dataset.isTemporary();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.Dataset#update()
     */
    public void update() throws WdkUserException {
        dataset.update();
    }
    
}
