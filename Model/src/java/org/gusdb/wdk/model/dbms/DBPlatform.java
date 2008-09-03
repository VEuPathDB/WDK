/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;

/**
 * @author Jerric Gao
 * 
 */
public abstract class DBPlatform {

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
            while (true) {
                StringBuffer display = new StringBuffer();
                display.append("[").append(platform.name).append("]");
                display.append(" Connections: Active = ").append(
                        getActiveCount());
                display.append(", Idle = ").append(getIdleCount());
                
                logger.info(display);
                //System.out.println(display);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    // ex.printStackTrace();
                }
            }
        }

    }

    public static final String ID_SEQUENCE_SUFFIX = "_pkseq";
    
    private static final Logger logger = Logger.getLogger(DBPlatform.class);

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
        if (schema == null) return "";
        schema = schema.trim().toLowerCase();
        if (schema.length() > 0 && !schema.endsWith(".")) schema += ".";
        return schema;
    }

    public static String normalizeString(String string) {
        return string.replaceAll("'", "''");
    }

    // #########################################################################
    // Member variables
    // #########################################################################

    protected DataSource dataSource;
    protected String defaultSchema;

    private GenericObjectPool connectionPool;
    private String name;
    private WdkModel wdkModel;

    // #########################################################################
    // the Abstract methods that are platform dependent
    // #########################################################################

    public abstract int getNextId(String schema, String table)
            throws SQLException;

    public abstract String getNumberDataType(int size);

    public abstract String getStringDataType(int size);

    public abstract String getBooleanDataType();

    public abstract String getClobDataType();

    public abstract String getDateDataType();

    public abstract String getMinusOperator();

    public abstract void createSequence(String sequence, int start,
            int increment) throws SQLException;

    public abstract int updateClobData(PreparedStatement ps, int columnIndex,
            String content, boolean commit) throws SQLException;

    public abstract String getClobData(ResultSet rs, String columnName)
            throws SQLException;

    public abstract String getPagedSql(String sql, int startIndex, int endIndex);

    public abstract boolean checkTableExists(String schema, String tableName)
            throws SQLException;

    // #########################################################################
    // Common methods are platform independent
    // #########################################################################

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
    public void initialize(WdkModel wdkModel, String name,
            String connectionUrl, String login, String password, int minIdle,
            int maxIdle, int maxWait, int maxActive) {
        this.wdkModel = wdkModel;
        this.name = name;

        connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                connectionUrl, login, password);

        // create abandoned configuration
        String validationQuery = null;
        boolean defaultReadOnly = false;
        boolean defaultAutoCommit = true;
        new PoolableConnectionFactory(connectionFactory, connectionPool, null,
                validationQuery, defaultReadOnly, defaultAutoCommit);

        // configure the connection pool
        connectionPool.setMaxWait(maxWait);
        connectionPool.setMaxIdle(maxIdle);
        connectionPool.setMinIdle(minIdle);
        connectionPool.setMaxActive(maxActive);

        // configure validationQuery tests
        connectionPool.setTestOnBorrow(true);
        connectionPool.setTestOnReturn(true);
        connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);

        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        this.dataSource = dataSource;
        this.defaultSchema = login;

        // start the connection monitor if needed
        if (this.wdkModel.getModelConfig().isShowConnections())
            (new Thread(new DisplayConnections(this))).start();
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
