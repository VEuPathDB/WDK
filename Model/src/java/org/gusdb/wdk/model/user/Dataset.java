/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.sql.SQLException;
import java.util.Date;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author xingao
 * 
 */
public class Dataset {

    private static final int UPLOAD_FILE_MAX_SIZE = 2000;

    private DatasetFactory factory;

    private int datasetId;
    private int userDatasetId;
    private User user;
    private String uploadFile;
    private Date createTime;
    private String summary;
    private int size;
    private String checksum;

    public Dataset(DatasetFactory factory, int datasetId) {
        this.factory = factory;
        this.datasetId = datasetId;
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
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @param createTime
     *            the createTime to set
     */
    void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @param values
     *            the summary to set
     */
    void setValues(String[] values) {
        this.size = values.length;
        // get summary
        StringBuffer sbSummary = new StringBuffer();
        int maxLength = Utilities.MAX_PARAM_VALUE_SIZE;
        for (String value : values) {
            if (sbSummary.length() != 0) sbSummary.append(", ");
            sbSummary.append(value);
            if (sbSummary.length() > maxLength) break;
        }
        summary = sbSummary.toString();
        if (summary.length() > maxLength) {
            int pos = summary.lastIndexOf(",", maxLength - 3);
            summary = summary.substring(0, (pos > 0) ? pos + 1 : maxLength - 3);
            summary += "...";
        }
    }

    /**
     * @param uploadFile
     *            the uploadFile to set
     */
    void setUploadFile(String uploadFile) {
        if (uploadFile.length() > UPLOAD_FILE_MAX_SIZE)
            uploadFile = uploadFile.substring(0, UPLOAD_FILE_MAX_SIZE - 3)
                    + "...";
        this.uploadFile = uploadFile;
    }

    /**
     * @return
     * @throws WdkUserException
     * @throws SQLException
     */
    public String[] getValues() throws WdkUserException, SQLException {
        return factory.getDatasetValues(this);
    }

    public String getValue() throws WdkUserException, SQLException {
        String[] values = getValues();
        StringBuffer sb = new StringBuffer();
        for (String value : values) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * @return the userDatasetId
     */
    public int getUserDatasetId() {
        return userDatasetId;
    }

    /**
     * @param userDatasetId
     *            the userDatasetId to set
     */
    void setUserDatasetId(int userDatasetId) {
        this.userDatasetId = userDatasetId;
    }

    /**
     * @param checksum
     *            the checksum to set
     */
    void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
