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
     * @see org.gusdb.wdk.model.user.Dataset#getSummary()
     */
    public String getSummary() {
        return dataset.getSummary();
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
    public List<String> getValues() throws WdkModelException {
      return dataset.getValues();
    }

    /**
     * @return
     * @throws WdkModelException 
     * @see org.gusdb.wdk.model.user.Dataset#getValue()
     */
    public String getValue() throws WdkModelException {
      return dataset.getValue();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getChecksum()
     */
    public String getChecksum() {
        return dataset.getChecksum();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Dataset#getUserDatasetId()
     */
    public int getUserDatasetId() {
        return dataset.getUserDatasetId();
    }

    void setRecordClass(RecordClassBean recordClass) {
        dataset.setRecordClass(recordClass.recordClass);
    }
    
    public RecordClassBean getRecordClass() {
        return new RecordClassBean(dataset.getRecordClass());
    }
}
