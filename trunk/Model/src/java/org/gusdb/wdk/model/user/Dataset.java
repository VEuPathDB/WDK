/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;

import org.gusdb.wdk.model.WdkUserException;

/**
 * @author xingao
 * 
 */
public class Dataset {

    private DatasetFactory factory;

    private int datasetId;
    private User user;
    private String uploadFile;
    private Date createTime;
    private String summary;
    private int size;

    public Dataset(DatasetFactory factory, User user, int datasetId) {
        this.factory = factory;
        this.user = user;
        this.datasetId = datasetId;
    }

    /**
     * @return Returns the factory.
     */
    public DatasetFactory getFactory() {
        return factory;
    }
    
    public User getUser() {
        return user;
    }
    
    void setUser(User user) {
        this.user = user;
    }

    public String getUploadFile() {
        return uploadFile;
    }

    /**
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @return the datasetId
     */
    public int getDatasetId() {
        return datasetId;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param createTime
     *            the createTime to set
     */
    void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @param size
     *            the size to set
     */
    void setSize(int size) {
        this.size = size;
    }

    /**
     * @param summary
     *            the summary to set
     */
    void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @param uploadFile
     *            the uploadFile to set
     */
    void setUploadFile(String uploadFile) {
        this.uploadFile = uploadFile;
    }
    

    /**
     * @return 
     * @throws WdkUserException
     */
    public String[] getValues() throws WdkUserException {
        return factory.getDatasetValues(this);
    }
    
    public String getValue() throws WdkUserException {
        String[] values = getValues();
        StringBuffer sb = new StringBuffer();
        for (String value : values) {
            if (sb.length()>0) sb.append(", ");
            sb.append(value);
        }
        return sb.toString();
    }
}
