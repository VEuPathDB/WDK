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
    private String datasetName;
    private String cacheTable;
    private boolean temporary;
    private Date createTime;
    private String dataType;

    public Dataset(DatasetFactory factory, User user, int datasetId) {
        this.factory = factory;
        this.user = user;
        this.datasetId = datasetId;
    }

    /**
     * @return Returns the cacheTable.
     */
    public String getCacheTable() {
        return cacheTable;
    }

    /**
     * @return Returns the cacheTable.
     */
    public String getCacheFullTable() {
        return factory.getDatasetSchema() + cacheTable;
    }

    /**
     * @param cacheTable
     *            The cacheTable to set.
     */
    public void setCacheTable(String cacheTable) {
        this.cacheTable = cacheTable;
    }

    /**
     * @return Returns the createTime.
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     *            The createTime to set.
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return Returns the datasetId.
     */
    public int getDatasetId() {
        return datasetId;
    }

    /**
     * @param datasetId
     *            The datasetId to set.
     */
    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * @return Returns the datasetName.
     */
    public String getDatasetName() {
        if (datasetName.length() > 200) return datasetName.substring(0, 200);
        else return datasetName;
    }

    /**
     * @param datasetName
     *            The datasetName to set.
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * @return Returns the temporary.
     */
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * @param temporary
     *            The temporary to set.
     */
    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    /**
     * @return Returns the userId.
     */
    public User getUser() {
        return user;
    }

    /**
     * @param userId
     *            The userId to set.
     */
    void setUser(User user) {
        this.user = user;
    }

    /**
     * @return Returns the type.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * @return Returns the factory.
     */
    public DatasetFactory getFactory() {
        return factory;
    }

    /**
     * @return returns the projectId-primaryKeyId pairs
     * @throws WdkUserException
     */
    public String[] getValues() throws WdkUserException {
        return factory.loadDatasetValues(this);
    }

    public void setValues(String[] values) throws WdkUserException {
        factory.saveDatasetValue(this, values);
    }

    public void update() throws WdkUserException {
        factory.saveDatasetInfo(this);
    }
}
