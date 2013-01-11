/**
 * 
 */
package org.gusdb.wdk.model.config;

/**
 * It defines the properties that are common in both {@code <appDB>} and
 * {@code <userDB>}, such as DB connection pool settings, login information, and
 * DB connection debugging parameters.
 * 
 * @author xingao
 * 
 */
public abstract class ModelConfigDB {

  // required properties
  private String login;
  private String password;
  private String connectionUrl;
  private String platform;

  // optional properties
  private short maxActive = 20;
  private short maxIdle = 1;
  private short minIdle = 0;
  private short maxWait = 50;

  /**
   * display DB connection count periodically in the log. This is used to
   * identify connection leaks. default false.
   */
  private boolean showConnections = false;
  /**
   * display interval, in seconds.
   */
  private long showConnectionsInterval = 10;
  /**
   * how long it will continue to display the connection count, in seconds.
   * after that, it will stop display any more counts.
   */
  private long showConnectionsDuration = 600;

  /**
   * @return the login
   */
  public String getLogin() {
    return login;
  }

  /**
   * @param login
   *          the login to set
   */
  public void setLogin(String login) {
    this.login = login;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the connectionUrl
   */
  public String getConnectionUrl() {
    return connectionUrl;
  }

  /**
   * @param connectionUrl
   *          the connectionUrl to set
   */
  public void setConnectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  /**
   * @return the platform
   */
  public String getPlatformClass() {
    return "org.gusdb.wdk.model.dbms." + platform;
  }

  /**
   * @param platform
   *          the platform to set
   */
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /**
   * @return the maxActive
   */
  public short getMaxActive() {
    return maxActive;
  }

  /**
   * @param maxActive
   *          the maxActive to set
   */
  public void setMaxActive(short maxActive) {
    this.maxActive = maxActive;
  }

  /**
   * @return the maxIdle
   */
  public short getMaxIdle() {
    return maxIdle;
  }

  /**
   * @param maxIdle
   *          the maxIdle to set
   */
  public void setMaxIdle(short maxIdle) {
    this.maxIdle = maxIdle;
  }

  /**
   * @return the minIdle
   */
  public short getMinIdle() {
    return minIdle;
  }

  /**
   * @param minIdle
   *          the minIdle to set
   */
  public void setMinIdle(short minIdle) {
    this.minIdle = minIdle;
  }

  /**
   * @return the maxWait
   */
  public short getMaxWait() {
    return maxWait;
  }

  /**
   * @param maxWait
   *          the maxWait to set
   */
  public void setMaxWait(short maxWait) {
    this.maxWait = maxWait;
  }

  /**
   * @return the showConnections
   */
  public boolean isShowConnections() {
    return showConnections;
  }

  /**
   * @param showConnections
   *          the showConnections to set
   */
  public void setShowConnections(boolean showConnections) {
    this.showConnections = showConnections;
  }

  /**
   * @return the showConnectionsInterval
   */
  public long getShowConnectionsInterval() {
    return showConnectionsInterval;
  }

  /**
   * @param showConnectionsInterval
   *          the showConnectionsInterval to set
   */
  public void setShowConnectionsInterval(long showConnectionsInterval) {
    this.showConnectionsInterval = showConnectionsInterval;
  }

  /**
   * @return the showConnectionsDuration
   */
  public long getShowConnectionsDuration() {
    return showConnectionsDuration;
  }

  /**
   * @param showConnectionsDuration
   *          the showConnectionsDuration to set
   */
  public void setShowConnectionsDuration(long showConnectionsDuration) {
    this.showConnectionsDuration = showConnectionsDuration;
  }
}
