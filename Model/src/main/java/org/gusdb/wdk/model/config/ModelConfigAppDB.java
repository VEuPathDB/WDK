package org.gusdb.wdk.model.config;

/**
 * <p>
 * An object representation of the {@code &lt;appDB>} tag in the
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
  private String acctDbLink;
  private int maxPkColumnWidth = 150;

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
   * @return the acctDbLink
   */
  public String getAcctDbLink() {
    return acctDbLink;
  }

  /**
   * @param acctDbLink
   *          the acctDbLink to set
   */
  public void setAcctDbLink(String acctDbLink) {
    if (acctDbLink.length() > 0 && !acctDbLink.startsWith("@"))
      acctDbLink = "@" + acctDbLink;
    this.acctDbLink = acctDbLink;
  }

  /**
   * Get the max width for the primaryKey column. The primary key columns in the
   * cache table will be resized to this given size. If the the value of the
   * columns is larger than this size, an exception will be thrown, and this
   * exception should be caught and resolved during sanity test.
   * 
   * @return
   */
  public int getMaxPkColumnWidth() {
    return maxPkColumnWidth;
  }

  public void setMaxPkColumnWidth(int maxPkColumnWidth) {
    this.maxPkColumnWidth = maxPkColumnWidth;
  }

}
