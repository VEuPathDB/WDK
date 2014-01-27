/**
 * 
 */
package org.gusdb.wdk.model.dataset;

import java.util.Date;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;

/**
 * @author xingao
 * 
 */
public class Dataset {

  private DatasetFactory factory;

  private final int datasetId;
  private final User user;

  private String name;
  private String uploadFile;
  private Date createdTime;
  private String parserName;
  private int size;
  private String checksum;
  private int categoryId;

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
   * @return the size
   */
  public int getSize() {
    return size;
  }

  void setSize(int size) {
    this.size = size;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public String getParserName() {
    return parserName;
  }

  public void setParserName(String parserName) {
    this.parserName = parserName;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  /**
   * @param uploadFile
   *          the uploadFile to set
   */
  void setUploadFile(String uploadFile) {
    this.uploadFile = uploadFile;
  }

  /**
   * @return
   */
  public List<String[]> getValues() throws WdkModelException {
    return factory.getDatasetValues(datasetId);
  }

  public String getContent() throws WdkModelException {
    return factory.getContent(datasetId);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(int categoryId) {
    this.categoryId = categoryId;
  }
}
