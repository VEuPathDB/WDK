package org.gusdb.wdk.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

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
 * How the QueryInstance table works. A QueryInstance is "persistent" (ie, a row
 * is added, and a result table is created), if any of these applies (in
 * priority order): - the query is cacheable - the "cached" bit is set - dispose
 * of row/resultTable when whole cache is dropped (new - the query will be in
 * the history (even if not cacheable, to provide a stable history, ie, results
 * don't change even if the db did) - dispose of row/resultTable when user's
 * history is purged - the query is being used in a boolean operation - this
 * depends on the query being in the history A row in QueryInstance is, by
 * definition, persistent. If its "cacheable" flag is on, its cacheable. If not,
 * it is still available to the history. We can think of "cacheability" as
 * "shared for all users."
 */

public class ResultFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -494603755802202030L;

	private static Logger logger = Logger.getLogger(ResultFactory.class);

	// the following constants are used by persistent query history
	public static final String TABLE_QUERY_INSTANCE = "QueryInstance";
	public static final String TABLE_SORTING_INDEX = "sorting_index";

	public static final String RESULT_TABLE_I = "result_index_column";
	public static final String COLUMN_SORTING_INDEX = "sorting_index_id";
	public static final String COLUMN_SORTING_COLUMNS = "sorting_columns";
	public static final String COLUMN_QUERY_INSTANCE_ID = "query_instance_id";

	public static final String CACHE_TABLE_PREFIX = "query_result_";

	RDBMSPlatformI platform;
	String schemaName;
	String instanceTableName;
	String instanceTableFullName;
	String sortingTableName;
	String sortingTableFullName;

	private boolean enableQueryLogger;
	private String queryLoggerFile;

	public ResultFactory(RDBMSPlatformI platform, String schemaName,
			boolean enableQueryLogger, String queryLoggerFile) {
		this.platform = platform;
		this.schemaName = schemaName;

		this.instanceTableName = TABLE_QUERY_INSTANCE;
		this.instanceTableFullName = platform.getTableFullName(schemaName,
				instanceTableName);

		this.sortingTableName = TABLE_SORTING_INDEX;
		this.sortingTableFullName = platform.getTableFullName(schemaName,
				sortingTableName);

		// configure query logger
		this.enableQueryLogger = enableQueryLogger;
		this.queryLoggerFile = queryLoggerFile;
	}

	// /////////////////////////////////////////////////////////////////////////
	// ///////////// public /////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////

	public ResultList getResult(QueryInstance instance)
			throws WdkModelException {
		ResultList resultList = instance.getIsPersistent() ? getPersistentResult(instance)
				: instance.getNonpersistentResult();
		return resultList;
	}

	public ResultList getPersistentResultPage(QueryInstance instance,
			int startRow, int endRow) throws WdkModelException {

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
		// a little bit risky here
		// sortingIndex is initialized by SqlQueryInstance during the
		// getResultTable(), but the variable is defined in QueryInstance
		int sortingIndex = instance.getSortingIndex();
		ResultSet rs = fetchCachedResultPage(resultTableName, sortingIndex,
				startRow, endRow);
		return new SqlResultList(instance, resultTableName, rs);
	}

	/**
     * @return Full name of table containing result
     */
	public String getResultAsTableName(QueryInstance instance)
			throws WdkModelException {
		return getResultTableName(instance);
	}

	public String getSqlForBooleanOp(QueryInstance instance,
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

	public void recreateCache(int numParams, boolean noSchemaOutput,
			boolean forceDrop) throws WdkModelException {
		dropCache(noSchemaOutput, forceDrop);
		createCache(numParams, noSchemaOutput);
	}

	/**
     * @param numParams
     *            Number of parameters allowed in a cached query
     */
	public void createCache(int numParams, boolean noSchemaOutput)
			throws WdkModelException {
		String newline = System.getProperty("line.separator");

		// Format sql to create table
		StringBuffer sqlQueryInstance = new StringBuffer();
		StringBuffer sqlSortingIndex = new StringBuffer();

		String numericType = platform.getNumberDataType();
		String clobType = platform.getClobDataType();

		// SQL to create the queryinstance table
		sqlQueryInstance.append("create table " + instanceTableFullName + " (");
		sqlQueryInstance.append(COLUMN_QUERY_INSTANCE_ID + " " + numericType
				+ "(12) not null, ");
		sqlQueryInstance.append("query_name varchar(100) not null, ");
		sqlQueryInstance.append("cached " + numericType + "(1) not null, ");
		sqlQueryInstance.append("result_table varchar(30), ");
		sqlQueryInstance.append("start_time date not null, ");
		sqlQueryInstance.append("end_time date, ");
		sqlQueryInstance.append("query_checksum varchar(40), ");
		sqlQueryInstance.append("result_message " + clobType + ", ");
		sqlQueryInstance.append("CONSTRAINT " + instanceTableName
				+ "_pk PRIMARY KEY (" + COLUMN_QUERY_INSTANCE_ID + "),");
		sqlQueryInstance.append("CONSTRAINT " + instanceTableName
				+ "_result_table_uk UNIQUE (result_table),");
		sqlQueryInstance.append("CONSTRAINT " + instanceTableName
				+ "_checksum_uk UNIQUE (query_checksum))");

		// SQL to create the sorting index table
		sqlSortingIndex.append("CREATE TABLE " + sortingTableFullName + " (");
		sqlSortingIndex.append(COLUMN_SORTING_INDEX + " " + numericType
				+ "(12) NOT NULL, ");
		sqlSortingIndex.append(COLUMN_QUERY_INSTANCE_ID + " " + numericType
				+ "(12) NOT NULL, ");
		sqlSortingIndex.append(COLUMN_SORTING_COLUMNS
				+ " VARCHAR(4000) NOT NULL, ");
		sqlSortingIndex.append("CONSTRAINT " + sortingTableName
				+ "_pk PRIMARY KEY (" + COLUMN_QUERY_INSTANCE_ID + ", "
				+ COLUMN_SORTING_COLUMNS + "), ");
		sqlSortingIndex.append("CONSTRAINT " + sortingTableName
				+ "_fk FOREIGN KEY (" + COLUMN_QUERY_INSTANCE_ID
				+ ") REFERENCES " + instanceTableFullName + " ("
				+ COLUMN_QUERY_INSTANCE_ID + "))");

		// Execute it
		try {
			logger.info("Using sql: " + sqlQueryInstance.toString());
			SqlUtils.execute(platform.getDataSource(),
					sqlQueryInstance.toString());

			logger.info("Using sql: " + sqlSortingIndex.toString());
			SqlUtils.execute(platform.getDataSource(),
					sqlSortingIndex.toString());

			// Create sequence
			String tblQueryInstanceToUse = (noSchemaOutput == true ? instanceTableName
					: instanceTableFullName);
			String tblSortingIndexToUse = (noSchemaOutput == true ? sortingTableName
					: sortingTableFullName);

			logger.info("Creating sequence " + tblQueryInstanceToUse + "_pkseq"
					+ newline);
			platform.createSequence(instanceTableFullName + "_pkseq", 1, 1);
			logger.info("Creating sequence " + tblSortingIndexToUse + "_pkseq"
					+ newline);
			platform.createSequence(sortingTableFullName + "_pkseq", 1, 1);

			System.out.println("Done" + newline);
		} catch (SQLException e) {
			throw new WdkModelException(e);
		}
	}

	/**
     * Remove all cached entries; it first deletes all the temporary tables
     * (found by querying the Queries table) and then deletes all of its rows
     * from the Queries table. This is *not* transaction safe. A better way
     * would be to copy the names of the result tables to a "dropThese" table
     * and delete the associated rows in the cache table both within one
     * transaction; then separately, as a post-process, drop the tables in the
     * dropThese table.
     */
	public synchronized void resetCache(boolean noSchemaOutput,
			boolean forceDrop) throws WdkModelException {

		// Query for the names of all cached result tables
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
			System.out.println("Succeeded in forced dropping " + nDropped);
		}

		try {
			// delete sorting indices
			String tblSortingIndexToUse = (noSchemaOutput == true ? sortingTableName
					: sortingTableFullName);
			System.out.println("Deleting all rows from " + tblSortingIndexToUse);
			SqlUtils.execute(platform.getDataSource(), "delete from "
					+ sortingTableFullName);

			String tblQueryInstanceToUse = (noSchemaOutput == true ? instanceTableName
					: instanceTableFullName);
			System.out.println("Deleting all rows from "
					+ tblQueryInstanceToUse);
			SqlUtils.execute(platform.getDataSource(), "delete from "
					+ instanceTableFullName);

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
	public synchronized void dropCache(boolean noSchemaOutput, boolean forceDrop) {
		// reset cache first
		try {
			resetCache(noSchemaOutput, forceDrop);
		} catch (WdkModelException ex) {
			System.err.println("WARNING: " + ex);
		}

		// drop sorting index table
		String tblSortingIndexToUse = (noSchemaOutput == true ? sortingTableName
				: sortingTableFullName);
		System.out.println("Dropping table " + tblSortingIndexToUse);
		try {
			platform.dropTable(sortingTableFullName);
		} catch (SQLException e) {
			System.err.println("WARNING: " + e);
		}
		System.out.println("Dropping sequence " + tblSortingIndexToUse
				+ "_pkseq");
		try {
			platform.dropSequence(sortingTableFullName + "_pkseq");
		} catch (SQLException e) {
			System.err.println("WARNING: " + e);
		}

		// drop query instance table
		String tblQueryInstanceToUse = (noSchemaOutput == true ? instanceTableName
				: instanceTableFullName);
		System.out.println("Dropping table " + tblQueryInstanceToUse);
		try {
			platform.dropTable(instanceTableFullName);
		} catch (SQLException e) {
			System.err.println("WARNING: " + e);
		}
		System.out.println("Dropping sequence " + tblQueryInstanceToUse
				+ "_pkseq");
		try {
			platform.dropSequence(instanceTableFullName + "_pkseq");
		} catch (SQLException e) {
			System.err.println("WARNING: " + e);
		}
	}

	public RDBMSPlatformI getRDBMSPlatform() {
		return platform;
	}

	// /////////////////////////////////////////////////////////////////////////
	// ///////////// protected /////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////

	/**
     * This method is rewritten to handle competing situation. The steps include
     * following: (1) get a new query instance id (2) create the cache table (3)
     * insert into queryinstance table
     * 
     * @return Full table name of the result table
     */
	private String getNewResultTableName(QueryInstance instance, String querySql)
			throws WdkModelException {
		String resultTableName = null;
		// get the query instance id
		Integer queryInstanceId = getQueryInstanceId(instance);
		try {
			if (queryInstanceId == null) {
				String strID = platform.getNextId(schemaName, instanceTableName);
				queryInstanceId = Integer.parseInt(strID);
			}

			resultTableName = CACHE_TABLE_PREFIX + queryInstanceId;

			// write result into result table
			instance.writeResultToTable(resultTableName, this);

			// insert a record to the QueryInstance table. All needed fields are
			// composed outside of the method, since the method is globally
			// synchronized.
			// The method returns a queryInstanceId, which can be identical (if
			// a new row is inserted successfully), or different (if a row has
			// already existed, and the id of that row will be returned)
			String queryName = instance.getQuery().getFullName();
			int cached = instance.getIsCacheable() ? 1 : 0;
			String instanceChecksum = instance.getChecksum();
			String resultMessage = instance.getResultMessage();
			queryInstanceId = insertQueryInstance(querySql, queryInstanceId,
					queryName, cached, resultTableName, instanceChecksum,
					resultMessage);

			// set the query instance id to the query instance
			instance.setQueryInstanceId(queryInstanceId);

			return resultTableName;
		} catch (SQLException e) {
			// need to roll back
			rollback(queryInstanceId, resultTableName);
			throw new WdkModelException(e);
		} catch (WdkModelException ex) {
			// need to roll back
			rollback(queryInstanceId, resultTableName);
			throw ex;
		}
	}

	private void rollback(int queryInstanceId, String resultTableName)
			throws WdkModelException {
		DataSource dataSource = platform.getDataSource();

		// drop the cache table, if have; ignore the exception
		try {
			SqlUtils.execute(dataSource, "DROP TABLE " + resultTableName);
		} catch (SQLException ex) {
			logger.info(ex);
		}

		// delete the query record
		try {
			SqlUtils.execute(dataSource, "DELETE FROM " + instanceTableFullName
					+ " WHERE " + COLUMN_QUERY_INSTANCE_ID + " = "
					+ queryInstanceId);
		} catch (SQLException ex) {
			throw new WdkModelException(ex);
		}
	}

	private ResultList getPersistentResult(QueryInstance instance)
			throws WdkModelException {
		String resultTableName = getResultTableName(instance);
		int sortingIndex = instance.getSortingIndex();

		ResultSet rs = fetchCachedResult(resultTableName, sortingIndex);
		return new SqlResultList(instance, resultTableName, rs);
	}

	/**
     * @return The full name of the database table that contains a cached query
     *         result. Returns null if no cached query result is stored in the
     *         database (either because it was never there or it has been
     *         expired.)
     * @param instance
     *            The instance of the query
     */
	private String getResultTableName(QueryInstance instance)
			throws WdkModelException {

		String resultTableFullName;

		// Construct SQL query to retrieve the requested table's name
		//
		StringBuffer sqlb = new StringBuffer();
		sqlb.append("select " + COLUMN_QUERY_INSTANCE_ID + ", result_table, "
				+ "result_message from " + instanceTableFullName + " where ");

		if (instance.getIsCacheable()) {
			sqlb.append("cached = 1 and end_time IS NOT NULL and ");
		}
		sqlb.append(instanceWhereClause(instance));

        // TEST
        //logger.info( sqlb.toString() );
        
		String resultTableName = null;
		String resultMessage = null;
		ResultSet rsInstance = null;
		try {
			rsInstance = SqlUtils.getResultSet(platform.getDataSource(),
					sqlb.toString());
			if (rsInstance.next()) {
				resultTableName = rsInstance.getString("result_table");
				resultMessage = platform.getClobData(rsInstance,
						"result_message");

				// instance result is in cache but is newly created object
				if (instance.getQueryInstanceId() == null)
					instance.setQueryInstanceId(getQueryInstanceId(instance));

				instance.setResultMessage(resultMessage);
			} else {
				resultTableName = getNewResultTableName(instance,
						sqlb.toString());
			}
			resultTableFullName = platform.getTableFullName(schemaName,
					resultTableName);

			// create a CacheTable object that represents the cache table
			CacheTable cacheTable = new CacheTable(platform, schemaName,
					instance.getQueryInstanceId(), instance.projectColumnName,
					instance.primaryKeyColumnName);
			instance.setCacheTable(cacheTable);
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
     * The method does: - see if there is a row in QueryInstance for this query
     * instance - if so, use the cache_table from that row and throw the one we
     * made away - if not, insert a row
     * 
     * @return the query instance id to be used by the query instance object
     */
	private synchronized Integer insertQueryInstance(String querySql,
			Integer queryInstanceId, String queryName, int cached,
			String cacheTable, String instanceChecksum, String resultMessage)
			throws WdkModelException {
		String datefunc = platform.getCurrentDateFunction();

		// format insert statement
		DataSource dataSource = platform.getDataSource();
		ResultSet rsSelect = null;
		PreparedStatement psInsert = null;
		try {
			// check if the row exists, reusing the query string composed before
			rsSelect = SqlUtils.getResultSet(dataSource, querySql);
			if (rsSelect.next()) {
				// the record exists, a competing thread did that; roll back
				rollback(queryInstanceId, cacheTable);

				// now use the existing instance id
				queryInstanceId = rsSelect.getInt(COLUMN_QUERY_INSTANCE_ID);
			} else {
				// the record doesn't exist, no competitions - insert the row
				psInsert = SqlUtils.getPreparedStatement(dataSource, "insert "
						+ "into " + instanceTableFullName + " ("
						+ COLUMN_QUERY_INSTANCE_ID + ", query_name, cached, "
						+ "result_table, start_time, end_time, query_checksum,"
						+ " result_message) values (?, ?, ?, ?, " + datefunc
						+ ", " + datefunc + ", ?, ?)");
				psInsert.setInt(1, queryInstanceId);
				psInsert.setString(2, queryName);
				psInsert.setInt(3, cached);
				psInsert.setString(4, cacheTable);
				psInsert.setString(5, instanceChecksum);
				psInsert.setString(6, resultMessage);
				psInsert.execute();
			}
			// return the queryInstanceId;
			return queryInstanceId;
		} catch (SQLException e) {
			throw new WdkModelException(e);
		} finally {
			try {
				SqlUtils.closeResultSet(rsSelect);
				SqlUtils.closeStatement(psInsert);
			} catch (SQLException ex) {
				throw new WdkModelException(ex);
			}
		}
	}

	/**
     * Record in the cache that a previously inserted query instance has run,
     * finished and written result to a result table. Updates the appropriate
     * row in the cache.
     * 
     * @return Whether the operation succeeded.
     */
	// private boolean finishQueryInstance(QueryInstance instance)
	// throws WdkModelException {
	// PreparedStatement psClob = null;
	// StringBuffer sqlb = new StringBuffer();
	// sqlb.append("update " + instanceTableFullName + " set ");
	// sqlb.append("end_time = " + platform.getCurrentDateFunction() + ", ");
	// sqlb.append("result_message = ? ");
	// sqlb.append(" where ");
	// sqlb.append(instanceWhereClause(instance));
	// boolean ok = false;
	//
	// try {
	// // now update the clob data
	// String message = instance.getResultMessage();
	// if (message == null) message = "No Result found.";
	//
	// psClob = SqlUtils.getPreparedStatement(platform.getDataSource(),
	// sqlb.toString());
	// ok = (platform.updateClobData(psClob, 1, message, true) == 1);
	// } catch (SQLException e) {
	// throw new WdkModelException(e);
	// } finally {
	// if (psClob != null) try {
	// SqlUtils.closeStatement(psClob);
	// } catch (SQLException ex) {
	// throw new WdkModelException(ex);
	// }
	// }
	// return ok;
	// }

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

	private ResultSet fetchCachedResult(String resultTableName, int sortingIndex)
			throws WdkModelException {

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM ");
		sql.append(resultTableName + " WHERE ");
		sql.append(COLUMN_SORTING_INDEX + " = " + sortingIndex);
		sql.append(" order by " + RESULT_TABLE_I + " asc");

		return fetchCachedResultFromSql(sql.toString());
	}

	private ResultSet fetchCachedResultPage(String resultTableName,
			int sortingIndex, int startRow, int endRow)
			throws WdkModelException {

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM ");
		sql.append(resultTableName + " WHERE ");
		sql.append(RESULT_TABLE_I + " >= " + startRow);
		sql.append(" and " + RESULT_TABLE_I + " <= " + endRow);
		sql.append(" AND " + COLUMN_SORTING_INDEX + " = " + sortingIndex);
		sql.append(" order by " + RESULT_TABLE_I + " asc");

		return fetchCachedResultFromSql(sql.toString());
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

	/**
     * The method get the id of the given query instance. If the query instance
     * doesn't have the id, it will select the id from the QueryInstance table;
     * if there is no record in that table to match with the given query
     * instance, a null is returned.
     * 
     * @param instance
     * @return
     * @throws WdkModelException
     */
	private Integer getQueryInstanceId(QueryInstance instance)
			throws WdkModelException {

		Integer queryInstanceId = instance.getQueryInstanceId();
		if (queryInstanceId == null) {

			StringBuffer sqlb = new StringBuffer();
			sqlb.append("select " + COLUMN_QUERY_INSTANCE_ID + " from "
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

		if (queryLoggerFile == null || queryLoggerFile.length() == 0) return;

		Calendar cal = GregorianCalendar.getInstance();

		// decide the name of the logger file
		int sepPos = queryLoggerFile.lastIndexOf(File.separator);
		int extPos = queryLoggerFile.lastIndexOf(".");
		StringBuffer name = new StringBuffer();
		if (extPos > sepPos) name.append(queryLoggerFile.substring(0, extPos));
		else name.append(queryLoggerFile);
		name.append("_" + (cal.get(Calendar.MONTH) + 1));
		name.append("-" + cal.get(Calendar.YEAR));
		if (extPos >= sepPos) name.append(queryLoggerFile.substring(extPos));
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
				String value = instance.getValuesMap().get(key).toString();
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
		boolean recreateCache = cmdLine.hasOption("recreate");
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
			else if (recreateCache)
				factory.recreateCache(maxQueryParams.intValue(),
						noSchemaOutput, forceDrop);
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

		Option recreateQ = new Option("recreate",
				"drop the query cache and create a new one");

		Option noSchema = new Option("noSchemaOutput",
				"remove references to the schema when printing out messages regarding a table");

		Option forceDrop = new Option("forceDrop",
				"drop all cache tables even if the cache is not listed in query instance table");

		OptionGroup operation = new OptionGroup();
		operation.setRequired(true);
		operation.addOption(newQ);
		operation.addOption(resetQ);
		operation.addOption(dropQ);
		operation.addOption(recreateQ);
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
