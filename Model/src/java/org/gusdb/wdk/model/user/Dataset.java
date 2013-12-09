/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public class Dataset {

  private static final int UPLOAD_FILE_MAX_SIZE = 2000;

  private DatasetFactory factory;

  private final int datasetId;
  private final User user;

  private int userDatasetId;
  private String uploadFile;
  private Date createTime;
  private String contentType;
  private int size;
  private String datasetChecksum;
  private String contentChecksum;

  public Dataset(DatasetFactory factory, User user, int datasetId) {
    this.factory = factory;
    this.user = user;
    this.datasetId = datasetId;
  }

  public User getUser() {
    return user;
  }

  /**
   * @return the datasetId
   */
  public int getDatasetId() {
    return datasetId;
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
   * @return the size
   */
  public int getSize() {
    return size;
  }

  void setSize(int size) {
    this.size = size;
  }

  /**
   * @return the type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   *          the type to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * @param createTime
   *          the createTime to set
   */
  void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  /**
   * @param uploadFile
   *          the uploadFile to set
   */
  void setUploadFile(String uploadFile) {
    if (uploadFile != null && uploadFile.length() > UPLOAD_FILE_MAX_SIZE)
      uploadFile = uploadFile.substring(0, UPLOAD_FILE_MAX_SIZE - 3) + "...";
    this.uploadFile = uploadFile;
  }

  /**
   * @return
   */
  public List<String[]> getValues() throws WdkModelException {
    return factory.getDatasetValues(datasetId);
  }

  public String getOriginalContent() throws WdkModelException {
    return factory.getOriginalContent(userDatasetId);
  }

  /**
   * @return the userDatasetId
   */
  public int getUserDatasetId() {
    return userDatasetId;
  }

  /**
   * @param userDatasetId
   *          the userDatasetId to set
   */
  void setUserDatasetId(int userDatasetId) {
    this.userDatasetId = userDatasetId;
  }

  /**
   * @return the datasetChecksum
   */
  public String getDatasetChecksum() {
    return datasetChecksum;
  }

  /**
   * @param datasetChecksum
   *          the datasetChecksum to set
   */
  public void setDatasetChecksum(String datasetChecksum) {
    this.datasetChecksum = datasetChecksum;
  }

  /**
   * @return the contentChecksum
   */
  public String getContentChecksum() {
    return contentChecksum;
  }

  /**
   * @param contentChecksum
   *          the contentChecksum to set
   */
  public void setContentChecksum(String contentChecksum) {
    this.contentChecksum = contentChecksum;
  }
}
