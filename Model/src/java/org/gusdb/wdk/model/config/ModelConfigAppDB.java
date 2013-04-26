package org.gusdb.wdk.model.config;

/**
 * <p>
 * An object representation of the {@code <appDB>} tag in the
 * {@code model-config.xml}. The information defined in this tag is about how to
 * connect to the database which contains application data.
 * </p>
 * 
 * <p>
 * Furthermore, if the user data is stored in a different database, the
 * application database must have a DB Link to point to the user database.
 * </p>
 * 
 * @author jerric
 * 
 */
public class ModelConfigAppDB extends ModelConfigDB {

  private String userDbLink;
  private short maxPkColumnWidth = 150;

  /**
   * @return the userDbLink
   */
  public String getUserDbLink() {
    return userDbLink;
  }

  /**
   * @param userDbLink
   *          the userDbLink to set
   */
  public void setUserDbLink(String userDbLink) {
    if (userDbLink.length() > 0 && !userDbLink.startsWith("@"))
      userDbLink = "@" + userDbLink;
    this.userDbLink = userDbLink;
  }

  /**
   * Get the max width for the primaryKey column. The primary key columns in the
   * cache table will be resized to this given size. If the the value of the
   * columns is larger than this size, an exception will be thrown, and this
   * exception should be caught and resolved during sanity test.
   * 
   * @return
   */
  public short getMaxPkColumnWidth() {
    return maxPkColumnWidth;
  }

  public void setMaxPkColumnWidth(short maxPkColumnWidth) {
    this.maxPkColumnWidth = maxPkColumnWidth;
  }

}
