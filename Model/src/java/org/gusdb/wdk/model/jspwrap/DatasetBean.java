/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Date;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
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

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getCreateTime()
     */
    public Date getCreateTime() {
        return dataset.getCreateTime();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getDatasetId()
     */
    public int getDatasetId() {
        return dataset.getDatasetId();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getSize()
     */
    public int getSize() {
        return dataset.getSize();
    }

    /**
     * @return
     * @throws WdkModelException 
     * @see org.gusdb.wdk.model.user.Dataset#getSummary()
     */
    public String getOriginalContent() throws WdkModelException {
        return dataset.getOriginalContent();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getUploadFile()
     */
    public String getUploadFile() {
        return dataset.getUploadFile();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getUser()
     */
    public UserBean getUser() {
        return new UserBean(dataset.getUser());
    }

    /**
     * @return
     * @throws WdkModelException 
     * @see org.gusdb.wdk.model.user.Dataset#getValues()
     */
    public List<String[]> getValues() throws WdkModelException {
      return dataset.getValues();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getChecksum()
     */
    public String getDatasetChecksum() {
        return dataset.getDatasetChecksum();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getContentChecksum()
     */
    public String getContentChecksum() {
      return dataset.getContentChecksum();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getUserDatasetId()
     */
    public int getUserDatasetId() {
        return dataset.getUserDatasetId();
    }
}
