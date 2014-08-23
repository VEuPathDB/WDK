/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Date;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dataset.Dataset;

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
     * @see org.gusdb.wdk.model.dataset.Dataset#getCreateTime()
     */
    public Date getCreatedTime() {
        return dataset.getCreatedTime();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.dataset.Dataset#getDatasetId()
     */
    public int getDatasetId() {
        return dataset.getDatasetId();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.dataset.Dataset#getSize()
     */
    public int getSize() {
        return dataset.getSize();
    }

    /**
     * @return
     * @throws WdkModelException 
     * @see org.gusdb.wdk.model.dataset.Dataset#getSummary()
     */
    public String getContent() throws WdkModelException {
        return dataset.getContent();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.dataset.Dataset#getUploadFile()
     */
    public String getUploadFile() {
        return dataset.getUploadFile();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.dataset.Dataset#getUser()
     */
    public UserBean getUser() {
        return new UserBean(dataset.getUser());
    }

    /**
     * @return
     * @throws WdkModelException 
     * @see org.gusdb.wdk.model.dataset.Dataset#getValues()
     */
    public List<String[]> getValues() throws WdkModelException {
      return dataset.getValues();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.dataset.Dataset#getChecksum()
     */
    public String getChecksum() {
        return dataset.getChecksum();
    }
    
    public String getParserName() {
      return dataset.getParserName();
    }
}
