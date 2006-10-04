package org.gusdb.wdk.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.implementation.SqlResultList;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * How the QueryInstance table works.
 * 
 * A QueryInstance is "persistent" (ie, a row is added, and a result table is
 * created), if any of these applies (in priority order): - the query is
 * cacheable - the "cached" bit is set - dispose of row/resultTable when whole
 * cache is dropped (new - the query will be in the history (even if not
 * cacheable, to provide a stable history, ie, results don't change even if the
 * db did) - dispose of row/resultTable when user's history is purged - the
 * query is being used in a boolean operation - this depends on the query being
 * in the history
 * 
 * A row in QueryInstance is, by definition, persistent. If its "cacheable" flag
 * is on, its cacheable. If not, it is still available to the history.
 * 
 * We can think of "cacheability" as "shared for all users."
 */

public class ResultFactory implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -494603755802202030L;

    private static Logger logger = Logger.getLogger(ResultFactory.class);

    // the following constants are used by persistent query history
    public static final String TABLE_QUERY_INSTANCE = "QueryInstance";
    /**
     * The name of query history table
     */
    public static final String TABLE_HISTORY_SUFFIX = "_history";
    public static final String FIELD_USER_ID = "session_id";
    public static final String FIELD_HISTORY_ID = "history_id";
    public static final String FIELD_DATASET_ID = "dataset_id";

    public static final String RESULT_TABLE_I = "result_index_column";
    static final String CACHE_TABLE_PREFIX = "query_result_";

    RDBMSPlatformI platform;
    String schemaName;
    String instanceTableName;
    String instanceTableFullName;

    private String historyTableName;
    private String historyTableFullName;

    private boolean enableQueryLogger;
    private String queryLoggerFile;

    public ResultFactory(RDBMSPlatformI platform, String schemaName,
            boolean enableQueryLogger, String queryLoggerFile) {
        this.platform = platform;
        this.schemaName = schemaName;
        this.instanceTableName = TABLE_QUERY_INSTANCE;
        this.instanceTableFullName = platform.getTableFullName(schemaName,
                instanceTableName);
        // get the full name of query history table
        historyTableName = instanceTableName + TABLE_HISTORY_SUFFIX;
        historyTableFullName = platform.getTableFullName(schemaName,
                historyTableName);

        // configure query logger
        this.enableQueryLogger = enableQueryLogger;
        this.queryLoggerFile = queryLoggerFile;
    }

    // /////////////////////////////////////////////////////////////////////////
    // ///////////// public /////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    public synchronized ResultList getResult(QueryInstance instance)
            throws WdkModelException {
        ResultList resultList = instance.getIsPersistent() ? getPersistentResult(instance)
                : instance.getNonpersistentResult();
        return resultList;
    }

    public synchronized ResultList getPersistentResultPage(
            QueryInstance instance, int startRow, int endRow)
            throws WdkModelException {

        if (!instance.getIsPersistent()) {
            throw new WdkModelException(
                    "Attempting to get a page a fgetNonpersistentResultrom non-perstent result");
        }

        // enable query logger
        if (enableQueryLogger) try {
            logQuery(instance);
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        }

        String resultTableName = getResultTableName(instance);
        ResultSet rs = fetchCachedResultPage(resultTableName, startRow, endRow);
        return new SqlResultList(instance, resultTableName, rs);
    }

    /**
     * @return Full name of table containing result
     */
    public synchronized String getResultAsTableName(QueryInstance instance)
            throws WdkModelException {
        return getResultTableName(instance);
    }

    public synchronized String getHistoryTableName() {
        return historyTableFullName;
    }

    public synchronized String getSqlForBooleanOp(QueryInstance instance,
            String[] columnNames) throws WdkModelException {
        StringBuffer selectb = new StringBuffer("select ");

        for (String name : columnNames)
            selectb.append(name + ", ");

        String resultTableName = getResultTableName(instance); // ensures
        // instance is
        // inserted into
        // cache

        return selectb.substring(0, selectb.length() - 2) + " from "
                + resultTableName;
    }

    /**
     * @param numParams
     *            Number of parameters allowed in a cached query
     */
    public void createCache(int numParams, boolean noSchemaOutput)
            throws WdkModelException {
        String newline = System.getProperty("line.separator");

        String nameToUse = (noSchemaOutput == true ? instanceTableName
                : instanceTableFullName);

        // Format sql to create table
        StringBuffer sqlb = new StringBuffer();
        String tblName = schemaName + "." + instanceTableName;

        String numericType = platform.getNumberDataType();
        String clobType = platform.getClobDataType();

        sqlb.append("create table " + tblName + " (query_instance_id "
                + numericType + "(12) not null, query_name varchar(100) "
                + "not null, cached " + numericType + "(1) not null,"
                + "result_table varchar(30), start_time date not null, "
                + "end_time date, dataset_name varchar(100), session_id "
                + "varchar(50), query_checksum varchar(40), result_message "
                + clobType + ", CONSTRAINT " + instanceTableName
                + "_pk PRIMARY KEY " + "(query_instance_id))");

        // Execute it
        System.out.println(newline + "Making cache table " + nameToUse
                + newline);

        logger.debug("Using sql: " + sqlb.toString());
        try {
            SqlUtils.execute(platform.getDataSource(), sqlb.toString());
            logger.debug("Done" + newline);

            // Create sequence
            platform.createSequence(tblName + "_pkseq", 1, 1);
            System.out.println("Creating sequence " + nameToUse + "_pkseq"
                    + newline);
            System.out.println("Done" + newline);
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }

        // now create this QueryHistory
        createHistoryCache(noSchemaOutput);
    }

    private void createHistoryCache(boolean noSchemaOutput)
            throws WdkModelException {
        String newline = System.getProperty("line.separator");

        String nameToUse = (noSchemaOutput == true ? historyTableName
                : historyTableFullName);

        // Format sql to create table
        StringBuffer sqlb = new StringBuffer();
        String tblName = schemaName + "." + historyTableName;

        String numericType = platform.getNumberDataType();

        sqlb.append("CREATE TABLE ");
        sqlb.append(tblName);
        sqlb.append(" (");
        sqlb.append(FIELD_USER_ID);
        sqlb.append(" varchar(100) not null, ");
        sqlb.append(FIELD_HISTORY_ID);
        sqlb.append(" ");
        sqlb.append(numericType);
        sqlb.append("(12) not null, ");
        sqlb.append(FIELD_DATASET_ID);
        sqlb.append(" ");
        sqlb.append(numericType);
        sqlb.append("(12) not null, PRIMARY KEY (");
        sqlb.append(FIELD_USER_ID);
        sqlb.append(", ");
        sqlb.append(FIELD_HISTORY_ID);
        sqlb.append("))");

        // Execute it
        System.out.println(newline + "Making history table " + nameToUse
                + newline);

        logger.debug("Using sql: " + sqlb.toString());
        try {
            SqlUtils.execute(platform.getDataSource(), sqlb.toString());
            logger.debug("Done" + newline);
            System.out.println("Done" + newline);
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    /**
     * Remove all cached entries; it first deletes all the temporary tables
     * (found by querying the Queries table) and then deletes all of its rows
     * from the Queries table.
     * 
     * This is *not* transaction safe. A better way would be to copy the names
     * of the result tables to a "dropThese" table and delete the associated
     * rows in the cache table both within one transaction; then separately, as
     * a post-process, drop the tables in the dropThese table.
     */
    public synchronized void resetCache(boolean noSchemaOutput,
            boolean forceDrop) throws WdkModelException {

        // Query for the names of all cached result tables
        //
        String nameToUse = (noSchemaOutput == true ? instanceTableName
                : instanceTableFullName);
        StringBuffer s = new StringBuffer();
        s.append("select result_table from " + instanceTableFullName);
        String tables[] = null;
        try {
            tables = SqlUtils.runStringArrayQuery(platform.getDataSource(),
                    s.toString());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
        int nTables = tables.length;
        int nDropped = 0;

        System.out.println("Attempting to drop " + nTables + " results tables");
        for (int i = 0; i < nTables; ++i) {
            try {
                platform.dropTable(schemaName + "." + tables[i]);
                nDropped++;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        System.out.println("Succeeded in dropping " + nDropped);

        if (forceDrop) {
            System.out.println("Force to drop all cache tables");
            try {
                nDropped = platform.forceDropTables(CACHE_TABLE_PREFIX.toUpperCase()
                        + "%");
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
            System.out.println("Succeeded in foreced dropping " + nDropped);
        }

        try {
            System.out.println("Deleting all rows from " + nameToUse);
            SqlUtils.execute(platform.getDataSource(), "delete from "
                    + instanceTableFullName);
            resetHistoryCache(noSchemaOutput);

            // validate the dropping operation
            if (!validateDrop())
                throw new WdkModelException(
                        "Not all cache tables are dropped successfully.");
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    /**
     * Drop all tables and sequences associated with the cache
     */
    public synchronized void dropCache(boolean noSchemaOutput, boolean forceDrop)
            throws WdkModelException {
        try {
            resetCache(noSchemaOutput, forceDrop);
            String nameToUse = (noSchemaOutput == true ? instanceTableName
                    : instanceTableFullName);
            System.out.println("Dropping table " + nameToUse);
            platform.dropTable(schemaName + "." + instanceTableName);
            System.out.println("Dropping sequence " + nameToUse + "_pkseq");
            platform.dropSequence(instanceTableFullName + "_pkseq");
            // and also drop the history cache
            System.out.println("Dropping table "
                    + (noSchemaOutput == true ? historyTableName
                            : historyTableFullName));
            platform.dropTable(schemaName + "." + historyTableName);
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    private synchronized void resetHistoryCache(boolean noSchemaOutput)
            throws SQLException {
        String nameToUse = (noSchemaOutput == true ? historyTableName
                : historyTableFullName);

        System.out.println("Deleting all rows from " + nameToUse);

        SqlUtils.executeUpdate(platform.getDataSource(), "delete from "
                + historyTableFullName);
    }

    public RDBMSPlatformI getRDBMSPlatform() {
        return platform;
    }

    // /////////////////////////////////////////////////////////////////////////
    // ///////////// protected /////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    /**
     * @return Full table name of the result table
     */
    private String getNewResultTableName(QueryInstance instance)
            throws WdkModelException {

        // populates cache if not already in there

        // add row to QueryInstance table
        Integer queryInstanceId = getQueryInstanceId(instance);
        if (queryInstanceId == null) {
            queryInstanceId = insertQueryInstance(instance);
        }

        String resultTableName = CACHE_TABLE_PREFIX + queryInstanceId;
        StringBuffer sql = new StringBuffer();
        sql.append("update " + instanceTableFullName + " set result_table = '"
                + resultTableName + "'");
        sql.append(" where query_instance_id = " + queryInstanceId.toString());
        try {
            // int numRows =
            SqlUtils.executeUpdate(platform.getDataSource(), sql.toString());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
        // write result into result table
        instance.writeResultToTable(resultTableName, this);
        // update row in QueryInstance table with final timestamp
        finishQueryInstance(instance);

        return resultTableName;
    }

    private ResultList getPersistentResult(QueryInstance instance)
            throws WdkModelException {
        String resultTableName = getResultTableName(instance);

        ResultSet rs = fetchCachedResult(resultTableName);
        return new SqlResultList(instance, resultTableName, rs);
    }

    /**
     * @return The full name of the database table that contains a cached query
     *         result. Returns null if no cached query result is stored in the
     *         database (either because it was never there or it has been
     *         expired.)
     * 
     * @param instance
     *            The instance of the query
     */
    private String getResultTableName(QueryInstance instance)
            throws WdkModelException {

        String resultTableFullName;

        // Construct SQL query to retrieve the requested table's name
        //
        StringBuffer sqlb = new StringBuffer();
        sqlb.append("select result_table, result_message from "
                + instanceTableFullName + " where ");

        if (instance.getIsCacheable()) {
            sqlb.append("cached = 1 and end_time IS NOT NULL and ");
        }
        sqlb.append(instanceWhereClause(instance));

        String resultTableName = null;
        String resultMessage = null;
        ResultSet rsInstance = null;
        try {
            rsInstance = SqlUtils.getResultSet(platform.getDataSource(),
                    sqlb.toString());
            if (rsInstance.next()) {
                resultTableName = rsInstance.getString("result_table");
                Clob messageClob = rsInstance.getClob("result_message");
                resultMessage = messageClob.getSubString(1,
                        (int) messageClob.length());

                // instance result is in cache but is newly created object
                if (instance.getQueryInstanceId() == null)
                    instance.setQueryInstanceId(getQueryInstanceId(instance));
                instance.setResultMessage(resultMessage);

                resultTableFullName = platform.getTableFullName(schemaName,
                        resultTableName);
            } else {
                resultTableFullName = getNewResultTableName(instance);
            }
        } catch (SQLException e) {
            throw new WdkModelException(e);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInstance);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
        return resultTableFullName;
    }

    /**
     * Record in the database that a query has been started (by entering an
     * appropriate row into the Queries table.) Returns the (automatically
     * generated) name of a table to which the query results should be written.
     * 
     * @return Full table name of result table
     */
    private Integer insertQueryInstance(QueryInstance instance)
            throws WdkModelException {
        return insertQueryInstance(instance, null, null);
    }

    /**
     * Documentation needs update: but note this does not restrict on whether
     * the instance is cacheable. Sets the ID in the QueryInstance by
     * side-effect.
     * 
     * This variant of the method is called when the query has a dataset param.
     * In that case, the sessionId and datasetName must be recorded in the table
     * that tracks the queries.
     * 
     * NOTE: We could omit the table_name column completely and simply adopt a
     * naming convention based on the primary key value.
     * 
     * @return Full table name of result table
     */
    private Integer insertQueryInstance(QueryInstance instance,
            String sessionId, String datasetName) throws WdkModelException {

        String nextID = null;
        try {
            nextID = platform.getNextId(schemaName, instanceTableName);

        } catch (SQLException e) {
            logger.error("Got an SQLException");
            throw new WdkModelException(e);
        }
        if (nextID == null) {
            nextID = "1";
        }

        // format values
        String queryName = "'" + instance.getQuery().getFullName() + "'";
        sessionId = (sessionId != null) ? ("'" + sessionId + "'") : "null";
        datasetName = (datasetName != null) ? ("'" + datasetName + "'")
                : "null";
        int cached = instance.getIsCacheable() ? 1 : 0;
        String checksum = "'" + instance.getChecksum() + "'";
        String datefunc = platform.getCurrentDateFunction();

        // format insert statement
        StringBuffer sqlb = new StringBuffer();
        sqlb.append("insert into " + instanceTableFullName
                + " (query_instance_id, query_name, cached, session_id, "
                + "dataset_name, start_time, query_checksum) values (" + nextID
                + ", " + queryName + ", " + cached + ", " + sessionId + ", "
                + datasetName + ", " + datefunc + ", " + checksum + ")");

        try {
            SqlUtils.executeUpdate(platform.getDataSource(), sqlb.toString());

            Integer finalId = new Integer(nextID);
            instance.setQueryInstanceId(finalId);
            return finalId;
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    /**
     * Record in the cache that a previously inserted query instance has run,
     * finished and written result to a result table. Updates the appropriate
     * row in the cache.
     * 
     * @return Whether the operation succeeded.
     */
    private boolean finishQueryInstance(QueryInstance instance)
            throws WdkModelException {
        PreparedStatement psClob = null;
        StringBuffer sqlb = new StringBuffer();
        sqlb.append("update " + instanceTableFullName + " set end_time = "
                + platform.getCurrentDateFunction() + ", result_message = ? ");
        sqlb.append(" where ");
        sqlb.append(instanceWhereClause(instance));
        boolean ok = false;

        try {
            // now update the clob data
            String message = instance.getResultMessage();
            if (message == null) message = "No Result found.";

            psClob = SqlUtils.getPreparedStatement(platform.getDataSource(),
                    sqlb.toString());
            ok = (platform.updateClobData(psClob, 1, message) == 1);
        } catch (SQLException e) {
            throw new WdkModelException(e);
        } finally {
            if (psClob != null) try {
                SqlUtils.closeStatement(psClob);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
        return ok;
    }

    /**
     * Create a "where" clause ("where" and/or "and" not included) that selects
     * from the cache table the row corresponding to a particular query instance
     * (if any).
     */
    private String instanceWhereClause(QueryInstance instance)
            throws WdkModelException {
        // get checksum of the instance
        String checksum = instance.getChecksum();

        StringBuffer sql = new StringBuffer();
        sql.append(" query_name = '" + instance.getQuery().getFullName() + "'");
        sql.append(" AND query_checksum = '" + checksum + "'");
        return sql.toString();
    }

    private ResultSet fetchCachedResult(String resultTableName)
            throws WdkModelException {

        String sql = "select *" + " from " + resultTableName + " order by "
                + RESULT_TABLE_I + " asc";

        return fetchCachedResultFromSql(sql);
    }

    private ResultSet fetchCachedResultPage(String resultTableName,
            int startRow, int endRow) throws WdkModelException {

        String sql;
        // have an option to return all rows (when startRow = endRow = 0)
        // if (startRow == 0 && endRow == 0)
        // sql = "select * " +
        // " from " + resultTableName +
        // " order by " + RESULT_TABLE_I + " asc";
        // else
        sql = "select * " + " from " + resultTableName + " where "
                + RESULT_TABLE_I + " >= " + startRow + " and " + RESULT_TABLE_I
                + " <= " + endRow + " order by " + RESULT_TABLE_I + " asc";

        return fetchCachedResultFromSql(sql);
    }

    private ResultSet fetchCachedResultFromSql(String sql)
            throws WdkModelException {
        ResultSet rs = null;
        try {
            rs = SqlUtils.getResultSet(platform.getDataSource(), sql);
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
        return rs;
    }

    private Integer getQueryInstanceId(QueryInstance instance)
            throws WdkModelException {

        Integer queryInstanceId = instance.getQueryInstanceId();
        if (queryInstanceId == null) {

            StringBuffer sqlb = new StringBuffer();
            sqlb.append("select query_instance_id from "
                    + instanceTableFullName + " where ");
            sqlb.append(instanceWhereClause(instance));

            try {
                queryInstanceId = SqlUtils.runIntegerQuery(
                        platform.getDataSource(), sqlb.toString());
            } catch (SQLException e) {
                throw new WdkModelException(e);
            }
        }
        return queryInstanceId;
    }

    private void logQuery(QueryInstance instance) throws WdkModelException,
            IOException {
        // // TEST
        // try { throw new Exception("Inocation path test."); }
        // catch(Exception ex) {ex.printStackTrace();}

        Calendar cal = GregorianCalendar.getInstance();

        // decide the name of the logger file
        int pos = queryLoggerFile.lastIndexOf('.');
        StringBuffer name = new StringBuffer();
        name.append((pos < 0) ? queryLoggerFile : queryLoggerFile.substring(0,
                pos));
        name.append("_" + cal.get(Calendar.MONTH));
        name.append("-" + cal.get(Calendar.YEAR));
        if (pos >= 0) name.append(queryLoggerFile.substring(pos));
        File file = new File(name.toString());
        if (!file.exists()) file.createNewFile();

        // compose the log
        StringBuffer log = new StringBuffer();

        // log time
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        log.append(format.format(cal.getTime()));

        // log query full name
        log.append("\t-query " + instance.getQuery().getFullName());

        // log parameters
        Param[] params = instance.getQuery().getParams();
        if (params.length > 0) {
            log.append("\t-params");
            for (Param param : params) {
                String key = param.getName();
                String value = (String) (instance.getValuesMap().get(key));
                log.append(" " + key + " \"" + value + "\"");
            }
        }

        // now write the log onto a file
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file, true));
            out.println(log.toString());
        } catch (IOException ex) {
            // cannot lock and write to the log file, try it again after a short
            // sleep;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex1) {}
            out = new PrintWriter(new FileWriter(file));
            out.println(log.toString());
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    private boolean validateDrop() throws SQLException {
        String pattern = CACHE_TABLE_PREFIX.toUpperCase() + "%";
        return (0 == platform.getTableCount(pattern));
    }

    // ////////////////////////////////////////////////////////////////////
    // /// Static methods
    // ////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        String cmdName = System.getProperties().getProperty("cmdName");
        File configDir = new File(System.getProperties().getProperty(
                "configDir"));

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        File modelConfigXmlFile = new File(configDir, modelName + "-config.xml");
        boolean newCache = cmdLine.hasOption("new");
        boolean resetCache = cmdLine.hasOption("reset");
        boolean dropCache = cmdLine.hasOption("drop");
        boolean noSchemaOutput = cmdLine.hasOption("noSchemaOutput");
        boolean forceDrop = cmdLine.hasOption("forceDrop");

        try {
            // read config info
            ModelConfig modelConfig = ModelConfigParser.parseXmlFile(modelConfigXmlFile);
            String connectionUrl = modelConfig.getConnectionUrl();
            String login = modelConfig.getLogin();
            String password = modelConfig.getPassword();
            Integer maxQueryParams = modelConfig.getMaxQueryParams();
            String platformClass = modelConfig.getPlatformClass();

            Integer maxIdle = modelConfig.getMaxIdle();
            Integer minIdle = modelConfig.getMinIdle();
            Integer maxWait = modelConfig.getMaxWait();
            Integer maxActive = modelConfig.getMaxActive();
            Integer initialSize = modelConfig.getInitialSize();

            boolean enableQueryLogger = modelConfig.isEnableQueryLogger();
            String queryLoggerFile = modelConfig.getQueryLoggerFile();

            RDBMSPlatformI platform = (RDBMSPlatformI) Class.forName(
                    platformClass).newInstance();
            platform.init(connectionUrl, login, password, minIdle, maxIdle,
                    maxWait, maxActive, initialSize,
                    modelConfigXmlFile.getAbsolutePath());

            ResultFactory factory = new ResultFactory(platform, login,
                    enableQueryLogger, queryLoggerFile);

            long start = System.currentTimeMillis();
            if (newCache) factory.createCache(maxQueryParams.intValue(),
                    noSchemaOutput);
            else if (resetCache) factory.resetCache(noSchemaOutput, forceDrop);
            else if (dropCache) factory.dropCache(noSchemaOutput, forceDrop);
            long end = System.currentTimeMillis();
            System.out.println("Command succeeded in "
                    + ((end - start) / 1000.0) + " seconds");

        } catch (Exception e) {
            System.err.println("FAILED");
            System.err.println("");
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    static Options declareOptions() {
        Options options = new Options();

        // config file
        addOption(
                options,
                "model",
                "the name of the model.  This is used to find the Model config file ($GUS_HOME/config/model_name-config.xml)");

        // operation
        Option newQ = new Option("new", "create a new query cache");

        Option resetQ = new Option("reset", "reset the query cache");

        Option dropQ = new Option("drop", "drop the query cache");

        Option noSchema = new Option("noSchemaOutput",
                "remove references to the schema when printing out messages regarding a table");

        Option forceDrop = new Option("forceDrop",
                "drop all cache tables even if the cache is not listed in query instance table");

        OptionGroup operation = new OptionGroup();
        operation.setRequired(true);
        operation.addOption(newQ);
        operation.addOption(resetQ);
        operation.addOption(dropQ);
        options.addOption(noSchema);
        options.addOption(forceDrop);
        options.addOptionGroup(operation);

        return options;
    }

    private static void addOption(Options options, String argName, String desc) {

        Option option = new Option(argName, true, desc);
        option.setRequired(true);
        option.setArgName(argName);

        options.addOption(option);
    }

    static CommandLine parseOptions(String cmdName, Options options,
            String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName
                + " -model model_name -new|-reset|-drop [-noSchemaOutput] -[forceDrop]";

        String header = newline
                + "Create, reset or drop a query cache. The name of the cache table is found in the Model config file (the table is placed in the schema owned by login).  Resetting the cache drops all results tables and deletes all rows from the cache table.  Dropping the cache first resets it then drops the cache table and sequence."
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
