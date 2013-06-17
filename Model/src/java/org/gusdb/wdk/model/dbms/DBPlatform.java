package org.gusdb.wdk.model.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfigDB;

/**
 * @author Jerric Gao
 * 
 */
public abstract class DBPlatform {

  private static final Logger LOG = Logger.getLogger(DBPlatform.class);

  private class DisplayConnections implements Runnable {

    private DBPlatform platform;

    public DisplayConnections(DBPlatform platform) {
      this.platform = platform;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
      long interval = dbConfig.getShowConnectionsInterval();
      long duration = dbConfig.getShowConnectionsDuration();
      long startTime = System.currentTimeMillis();
      while (true) {
        StringBuffer display = new StringBuffer();
        display.append("[").append(platform.name).append("]");
        display.append(" Connections: Active = ").append(getActiveCount());
        display.append(", Idle = ").append(getIdleCount());

        logger.info(display);
        // System.out.println(display);
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsed > duration)
          break;
        try {
          Thread.sleep(interval * 1000);
        } catch (InterruptedException ex) {
          // ex.printStackTrace();
        }
      }
    }

  }

  public static final String ID_SEQUENCE_SUFFIX = "_pkseq";

  private static final Logger logger = Logger.getLogger(DBPlatform.class);

  private static List<DBPlatform> platforms = new ArrayList<DBPlatform>();

  // #########################################################################
  // Platform related helper functions
  // #########################################################################

  /**
   * normalize the schema name, if it not empty, a dot will be appended to the
   * end of it.
   * 
   * @param schema
   * @return
   */
  public static String normalizeSchema(String schema) {
    if (schema == null)
      return "";
    schema = schema.trim().toLowerCase();
    if (schema.length() > 0 && !schema.endsWith("."))
      schema += ".";
    return schema;
  }

  public static String normalizeString(String string) {
    return string.replaceAll("'", "''");
  }

  public static void closeAllPlatforms() throws Exception {
    for (DBPlatform platform : platforms) {
      platform.close();
    }
  }

  // #########################################################################
  // Member variables
  // #########################################################################

  protected DataSource dataSource;
  protected String defaultSchema;
  protected WdkModel wdkModel;

  private GenericObjectPool connectionPool;
  private String name;
  private ModelConfigDB dbConfig;

  // #########################################################################
  // the Abstract methods that are platform dependent
  // #########################################################################

  public abstract int getNextId(String schema, String table)
      throws WdkModelException;

  public abstract String getNextIdSqlExpression(String schema, String table);

  public abstract String getNumberDataType(int size);

  public abstract String getFloatDataType(int size);

  public abstract String getStringDataType(int size);

  public abstract String getBooleanDataType();

  public abstract String getClobDataType();

  public abstract String getDateDataType();

  public abstract String getMinusOperator();

  public abstract void createSequence(String sequence, int start, int increment)
      throws WdkModelException;

  public abstract int setClobData(PreparedStatement ps, int columnIndex,
      String content, boolean commit) throws SQLException;

  public abstract String getClobData(ResultSet rs, String columnName)
      throws SQLException;

  public abstract String getPagedSql(String sql, int startIndex, int endIndex);

  public abstract boolean checkTableExists(String schema, String tableName)
      throws WdkModelException;

  public abstract String convertBoolean(boolean value);

  public abstract void dropTable(String schema, String table, boolean purge)
      throws WdkModelException;

  public abstract void disableStatistics(String schema, String tableName)
      throws WdkModelException, SQLException;

  protected abstract String getDriverClassName();
  
  protected abstract String getValidationQuery();
  
  /**
   * @param schema
   *          the schema cannot be empty. if you are searching in a local
   *          schema, it has to be the login user name.
   * @param pattern
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public abstract String[] queryTableNames(String schema, String pattern)
      throws WdkModelException;

  public abstract String getDummyTable();

  public abstract String getResizeColumnSql(String tableName, String column,
      int size);

  // #########################################################################
  // Common methods are platform independent
  // #########################################################################

  public DBPlatform() {
    platforms.add(this);
  }

  /**
   * @return the wdkModel
   */
  public WdkModel getWdkModel() {
    return wdkModel;
  }

  /**
   * @return the dbConfig
   */
  public ModelConfigDB getDbConfig() {
    return dbConfig;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public int getActiveCount() {
    return connectionPool.getNumActive();
  }

  public int getIdleCount() {
    return connectionPool.getNumIdle();
  }

  /**
   * @return the defaultSchema
   */
  public String getDefaultSchema() {
    return normalizeSchema(defaultSchema);
  }

  /**
   * Initialize connection pool. The driver should have been registered by the
   * implementation.
   * 
   * @param connectionUrl
   * @param login
   * @param password
   * @param minIdle
   * @param maxidle
   * @param maxWait
   * @param maxActive
   */
  public void initialize(WdkModel wdkModel, String name, ModelConfigDB dbConfig) throws WdkModelException {
    this.wdkModel = wdkModel;
    this.name = name;
    this.dbConfig = dbConfig;
    
    logger.info("DB Connection [" + name + "]: " + dbConfig.getConnectionUrl());

    connectionPool = new GenericObjectPool(null);
    String connectionUrl = dbConfig.getConnectionUrl();
    
    Properties props = new Properties();
    props.put("user", dbConfig.getLogin());
    props.put("password", dbConfig.getPassword());
    
    // initialize DB driver; (possibly modified) url will be returned, connection properties may also be modified
    connectionUrl = initializeDbDriver(dbConfig.getDriverInitClass(), props, connectionUrl);
    
    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUrl, props);

    // create abandoned configuration
    boolean defaultReadOnly = false;
    boolean defaultAutoCommit = true;
    new PoolableConnectionFactory(connectionFactory, connectionPool, null,
        getValidationQuery(), defaultReadOnly, defaultAutoCommit);

    // configure the connection pool
    connectionPool.setMaxWait(dbConfig.getMaxWait());
    connectionPool.setMaxIdle(dbConfig.getMaxIdle());
    connectionPool.setMinIdle(dbConfig.getMinIdle());
    connectionPool.setMaxActive(dbConfig.getMaxActive());

    // configure validationQuery tests
    connectionPool.setTestOnBorrow(true);
    connectionPool.setTestOnReturn(true);
    connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);

    PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
    dataSource.setAccessToUnderlyingConnectionAllowed(true);
    this.dataSource = dataSource;
    this.defaultSchema = DBPlatform.normalizeSchema(dbConfig.getLogin());

    // start the connection monitor if needed
    if (dbConfig.isShowConnections())
      (new Thread(new DisplayConnections(this))).start();
  }

  private String initializeDbDriver(String driverInitClassName, Properties props,
      String connectionUrl) throws WdkModelException {
    try {
      // check to see if user provided custom driver initializer
      if (driverInitClassName == null || driverInitClassName.isEmpty() ||
          driverInitClassName.equals(DefaultDbDriverInitializer.class.getName())) {
        // if none provided (or default), use the default WDK driver initializer
        DbDriverInitializer initClassInstance = new DefaultDbDriverInitializer();
        LOG.debug("Initializing driver " + getDriverClassName() + " using default WDK initializer.");
        return initClassInstance.initializeDriver(getDriverClassName(), connectionUrl, props);
      }
      else {
        // otherwise, try to instantiate user-provided implementation and call
        Class<?> initClass = (Class<?>)Class.forName(driverInitClassName);
        if (!DbDriverInitializer.class.isAssignableFrom(initClass)) {
          throw new WdkModelException("Value for driverInitClass in Model Config " +
              "is not an implementation of " + DbDriverInitializer.class.getName());
        }
        // provided class is the correct type; instantiate and call initialize method
        @SuppressWarnings("unchecked") // checked above
        DbDriverInitializer initClassInstance = ((Class<? extends DbDriverInitializer>)initClass).newInstance();
        LOG.debug("Initializing driver " + getDriverClassName() + " using custom WDK initializer: " + driverInitClassName);
        return initClassInstance.initializeDriver(getDriverClassName(), connectionUrl, props);
      }
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new WdkModelException("Unable to instantiate custom DB Driver Initializer " +
          "class with name " + driverInitClassName, e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.RDBMSPlatformI#close()
   */
  public void close() throws Exception {
    connectionPool.close();
  }

}
