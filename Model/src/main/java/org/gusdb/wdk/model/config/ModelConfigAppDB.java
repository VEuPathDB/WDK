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

  private String _userDbLink;
  private String _cacheSchema;
  private String _remoteUserDataSchema;
  private int _maxPkColumnWidth = 150;

  /**
   * @return the userDbLink
   */
  public String getUserDbLink() {
    return _userDbLink;
  }

  /**
   * @param userDbLink
   *          the userDbLink to set
   */
  public void setUserDbLink(String userDbLink) {
    if (userDbLink.length() > 0 && !userDbLink.startsWith("@"))
      userDbLink = "@" + userDbLink;
    _userDbLink = userDbLink;
  }

  public void setCacheSchema(String cacheSchema) {
    _cacheSchema = cacheSchema;
  }

  /**
   * @return the schema to be used for the WDK cache; if not specified in model-config.xml,
   * the login user schema will be used as the default.  A '.' is appended to the end if
   * not already present.
   */
  public String getCacheSchema() {
    String schema = _cacheSchema == null ? getLogin() : _cacheSchema;
    if (!schema.endsWith(".")) schema += ".";
    return schema;
  }

  public String getRemoteUserDataSchema() {
    return _remoteUserDataSchema;
  }

  public void setRemoteUserDataSchema(String remoteUserDataSchema) {
    if (remoteUserDataSchema == null) {
      _remoteUserDataSchema = "";
    }
    else {
      remoteUserDataSchema = remoteUserDataSchema.trim().toLowerCase();
      if (!remoteUserDataSchema.isEmpty() && !remoteUserDataSchema.endsWith(".")) {
         remoteUserDataSchema = remoteUserDataSchema + ".";
      }
      _remoteUserDataSchema = remoteUserDataSchema;
    }
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
    return _maxPkColumnWidth;
  }

  public void setMaxPkColumnWidth(int maxPkColumnWidth) {
    _maxPkColumnWidth = maxPkColumnWidth;
  }

}
