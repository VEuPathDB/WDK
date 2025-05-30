package org.gusdb.wdk.model.config;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.platform.UnsupportedPlatformException;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.veupathdb.lib.ldap.LDAP;
import org.veupathdb.lib.ldap.LDAPConfig;
import org.veupathdb.lib.ldap.LDAPHost;
import org.veupathdb.lib.ldap.NetDesc;
import org.veupathdb.lib.ldap.OracleNetDesc;
import org.veupathdb.lib.ldap.PostgresNetDesc;

/**
 * It defines the properties that are common in both {@code <appDB>} and
 * {@code <userDB>}, such as DB connection pool settings, login information, and
 * DB connection debugging parameters.
 *
 * @author xingao
 */
public abstract class ModelConfigDB implements ConnectionPoolConfig {

  private static final Logger LOG = Logger.getLogger(ModelConfigDB.class);

  protected static final boolean DEFAULT_READ_ONLY = false;
  protected static final boolean DEFAULT_AUTO_COMMIT = true;

  protected static final String CONFIG_TABLE = "config";
  protected static final String CONFIG_NAME_COLUMN = "config_name";
  protected static final String CONFIG_VALUE_COLUMN = "config_value";

  // required properties
  private String login;
  private String password;

  // connection properties; one set of combinations is required
  private boolean connectionInformationFilled = false;
  private String connectionUrl;
  private String platform;
  private String ldapServer;
  private String ldapBaseDn;
  private String ldapCommonName;
  private String dbIdentifier;
  private String dbHost;
  private String dbPort;

  // optional properties
  private String driverInitClass;
  private int maxActive = 20;
  private int maxIdle = 1;
  private int minIdle = 0;
  private long maxWait = 50;
  private int defaultFetchSize = 0;
  private boolean defaultReadOnly = DEFAULT_READ_ONLY;
  private boolean defaultAutoCommit = DEFAULT_AUTO_COMMIT;

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
  @Override
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
  @Override
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
   * Connection information may be provided via:
   *
   *  LDAP host[:port] + baseDn + commonName (TNS or PG DB name) (platform is derived)
   *  -OR-
   *  host + port (optional) + database identifier (serviceName or PG DB name) + platform
   *  -OR-
   *  DB connection URL (platform is derived)
   *    NOTE: Oracle OCI URLs will only work with local Oracle OCI installation
   *
   * This method examines and validates the submitted configuration, filling in
   * any additional values it can of the following:
   *
   * ldapServer
   * ldapBaseDn
   * ldapCommonName
   * dbHost
   * dbPort
   * dbIdentifier
   * platform
   * connectionUrl
   */
  private void fillConnectionInformation() {
    // only do this once
    if (connectionInformationFilled) return;
    connectionInformationFilled = true;

    // prefer connection URL (most comprehensive information in one place)
    if (connectionUrl != null) {
      DBPlatform.parseConnectionUrl(connectionUrl)
        .ifLeft(this::setValuesFromNetDesc)
        .ifRight(tnsName -> {
          platform = SupportedPlatform.ORACLE.toString();
          LOG.warn(getClass().getSimpleName() + " is configured with Oracle OCI connection URL " +
              "(tnsName = " + tnsName + ").  Local Oracle installation is required, and detailed " +
              "database information (host/port/serviceName) is not available to the application.");
        });
      return;
    }

    // no connection URL; try connection components
    if (dbHost != null && dbIdentifier != null && platform != null) { // dbPort is optional
      setValuesFromNetDesc(buildNetDesc(dbHost, dbPort, dbIdentifier, platform));
      return;
    }

    // configured for LDAP lookup
    if (ldapServer != null && ldapBaseDn != null && ldapCommonName != null) {
      LDAP ldap = new LDAP(new LDAPConfig(List.of(LDAPHost.ofString(ldapServer)), ldapBaseDn));
      setValuesFromNetDesc(ldap.requireSingularNetDesc(ldapCommonName));
      return;
    }

    // Should probably be WdkModelException, but don't want all getters to throw that
    // To remedy, convert this class to a builder and figure this out one time in build()
    throw new WdkRuntimeException(getClass().getSimpleName() + " configuration is incomplete. " +
        "One of the following combinations is required:\n" +
        "  1. DB connection URL\n" +
        "  2. host + database identifier (serviceName or PG DB name) + platform\n" +
        "  3. LDAP host[:port] + baseDn + commonName (TNS name or PG DB name)");
  }

  private void setValuesFromNetDesc(NetDesc netDesc) {
    connectionUrl = DBPlatform.getConnectionUrl(netDesc);
    platform = SupportedPlatform.fromLdapPlatform(netDesc.getPlatform()).toString();
    dbHost = netDesc.getHost();
    dbPort = String.valueOf(netDesc.getPort());
    dbIdentifier = netDesc.getIdentifier();
  }

  private static NetDesc buildNetDesc(String host, String portStr, String identifier, String platformStr) {
    SupportedPlatform platformEnum = SupportedPlatform.toPlatform(platformStr.toUpperCase());
    int port = portStr == null ? platformEnum.getDefaultPort() : Integer.valueOf(portStr);
    switch(platformEnum) {
      case ORACLE:
        return new OracleNetDesc(host, port, identifier);
      case POSTGRESQL:
        return new PostgresNetDesc(host, port, identifier);
      default:
        throw new IllegalStateException("Not all supported platforms are configurable here.");
    }
  }

  /**
   * @return the connectionUrl
   */
  @Override
  public String getConnectionUrl() {
    fillConnectionInformation();
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
   * @param platform
   *          the platform to set
   * @throws UnsupportedPlatformException if platform is not supported
   */
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /**
   * @return DB platform string for this configuration
   */
  public String getPlatform() {
    fillConnectionInformation();
    return platform;
  }
  @Override
  public SupportedPlatform getPlatformEnum() {
    fillConnectionInformation();
    return (platform == null ? null :
      SupportedPlatform.toPlatform(platform.toUpperCase()));
  }

  public String getLdapServer() {
    fillConnectionInformation();
    return ldapServer;
  }

  public void setLdapServer(String ldapServer) {
    this.ldapServer = ldapServer;
  }

  public String getLdapBaseDn() {
    fillConnectionInformation();
    return ldapBaseDn;
  }

  public void setLdapBaseDn(String ldapBaseDn) {
    this.ldapBaseDn = ldapBaseDn;
  }

  public String getLdapCommonName() {
    fillConnectionInformation();
    return ldapCommonName;
  }

  public void setLdapCommonName(String ldapCommonName) {
    this.ldapCommonName = ldapCommonName;
  }

  public String getDbIdentifier() {
    fillConnectionInformation();
    return dbIdentifier;
  }

  public void setDbIdentifier(String dbIdentifier) {
    this.dbIdentifier = dbIdentifier;
  }

  public String getDbHost() {
    fillConnectionInformation();
    return dbHost;
  }

  public void setDbHost(String dbHost) {
    this.dbHost = dbHost;
  }

  public String getDbPort() {
    fillConnectionInformation();
    return dbPort;
  }

  public void setDbPort(String dbPort) {
    this.dbPort = dbPort;
  }

  /**
   * @return the maxActive
   */
  @Override
  public int getMaxActive() {
    return maxActive;
  }

  /**
   * @param maxActive
   *          the maxActive to set
   */
  public void setMaxActive(int maxActive) {
    this.maxActive = maxActive;
  }

  /**
   * @return the maxIdle
   */
  @Override
  public int getMaxIdle() {
    return maxIdle;
  }

  /**
   * @param maxIdle
   *          the maxIdle to set
   */
  public void setMaxIdle(int maxIdle) {
    this.maxIdle = maxIdle;
  }

  /**
   * @return the minIdle
   */
  @Override
  public int getMinIdle() {
    return minIdle;
  }

  /**
   * @param minIdle
   *          the minIdle to set
   */
  public void setMinIdle(int minIdle) {
    this.minIdle = minIdle;
  }

  /**
   * @return the maxWait
   */
  @Override
  public long getMaxWait() {
    return maxWait;
  }

  /**
   * @param maxWait
   *          the maxWait to set
   */
  public void setMaxWait(long maxWait) {
    this.maxWait = maxWait;
  }

  @Override
  public int getDefaultFetchSize() {
    return defaultFetchSize;
  }

  public void setDefaultFetchSize(int defaultFetchSize) {
    this.defaultFetchSize = defaultFetchSize;
  }

  @Override
  public boolean getDefaultAutoCommit() {
    return defaultAutoCommit;
  }

  public void setDefaultAutoCommit(boolean defaultAutoCommit) {
    this.defaultAutoCommit = defaultAutoCommit;
  }

  @Override
  public boolean getDefaultReadOnly() {
    return defaultReadOnly;
  }

  public void setDefaultReadOnly(boolean defaultReadOnly) {
    this.defaultReadOnly = defaultReadOnly;
  }

  /**
   * @param driverInitClass implementation class to initialize DB driver
   */
  public void setDriverInitClass(String driverInitClass) {
    this.driverInitClass = driverInitClass;
  }

  /**
   * @return implementation class to initialize DB driver
   */
  @Override
  public String getDriverInitClass() {
    return driverInitClass;
  }

  /**
   * @return the showConnections
   */
  @Override
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
  @Override
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
  @Override
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

  @Override
  public String toString() {
    String defaultSchema = getPlatform() != null ? SupportedPlatform.toPlatform(getPlatform())
        .getPlatformInstance().getDefaultSchema(getLogin()) : "Unknown";
    return toJson().put("defaultSchema", defaultSchema).toString(2);
  }

  public boolean isSameConnectionInfoAs(ModelConfigDB config) {
    return (getLogin().equals(config.getLogin()) &&
        getPassword().equals(config.getPassword()) &&
        getConnectionUrl().equals(config.getConnectionUrl()) &&
        getPlatformEnum() == config.getPlatformEnum());
  }
}
