package org.gusdb.wdk.model.implementation;

//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.logging.Logger;
//import org.gusdb.wdk.model.WdkLogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.*;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;

import oracle.jdbc.rowset.OracleCachedRowSet;

/**
 * this class is used to execute the low-level sql query
 * 
 * @author Steve Fischer, Jerric Gao, Sammy Wang
 * @version 1.11-4
 */

public class SqlUtils {

    /*
     * debug flag -- added by Jerric
     */
    private static boolean debug = false;

    /*
     * set this variable to true will start a separate thread to monitor the
     * connection usage
     */
    private static boolean createShowThread = false;

    // private static final Logger logger =
    // WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.SqlUtils");
    /*
     * log major steps to trace later
     */
    private static final Logger logger = Logger.getLogger(SqlUtils.class);

    /*
     * private static BufferedWriter getOut(String name) { //private static File
     * info = new File("/home/gususer/fedTestTime.txt"); //private static File
     * info = new File(".\\config\\fedTestTime.txt"); BufferedWriter out = null;
     * try { File outFile = new File(name); if(!(outFile.exists()))
     * outFile.createNewFile(); out = new BufferedWriter(new FileWriter(outFile,
     * true)); } catch (IOException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); }
     * 
     * return out; }
     */

    // private static int parts;
    /**
     * this method return ResultSet to high-level WDK through querying database
     * modified by samwzm to deal with distribute query
     * 
     * @param dataSource
     * @param sql
     * @return ResultSet
     * @throws SQLException
     */
    public static ResultSet getResultSet(DataSource dataSource, String sql)
            throws SQLException {

        // TEST
        if (debug) System.out.println("<==getResultSet==>: " + sql);
        long before = 0, after = 0;
        if (debug) {
            before = System.currentTimeMillis();
        }

        // added by samwzm
        // if sql includes "<FEDERATION SITE=... CONNECTIONSTRING=...>", then
        // decompose it and union according to three results
        // if (sql.contains("FEDERATION")){ //to use federation alternative
        // reverse the commented out line
        if (sql.contains("UNION ALL --")) {

            // decompose sql according to the position of FEDERATION
            String[][] queries = decompose(sql);
            // String[] tableNames = createTables(dataSource, queries);
            // ResultSet result = unionResult(dataSource, tableNames);
            // ResultSet result = mergeResult(dataSource, queries);
            ResultSet result = multiMergeResult(dataSource, queries);
            if (debug) {
                after = System.currentTimeMillis();
                System.out.println("----it is a federation query and the costed time in seconds is---- "
                        + (after - before) / 1000F);
            }

            return result;
        }

        // the following deals query without "FEDERATION"

        Statement stmt = null;
        try {
            Connection connection = dataSource.getConnection();
            showConnectionCount();

            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (debug) {
                after = System.currentTimeMillis();
            }
            if (debug) {
                System.out.println("----the costed time in seconds is---- "
                        + (after - before) / 1000F);
            }

            // OracleCachedRowSet rowset = new OracleCachedRowSet();
            // rowset.populate(rs);
            return rs;
            // return rs;
        } catch (SQLException sqlE) {
            logger.error("Failed attempting to execute sql in getResultSet: '"
                    + sql + "'");
            closeStatement(stmt);
            throw sqlE;
        }
    }

    /**
     * Gets a result set using a PreparedStatement. Since the statement is
     * prepared, it is likely that the user is intending to use it more than
     * once before closing it. It is thus up to the user to close the
     * PreparedStatement using <code>closeStatement</code> when finished.
     */
    public static int getResultSet(DataSource dataSource,
            PreparedStatement prepStmt) throws SQLException {

        // TEST
        // if (debug) System.out.println("<==getResultSetPrepared==>: " +
        // prepStmt);

        int result = -1;
        result = prepStmt.executeUpdate();
        return result;
    }

    public static PreparedStatement getPreparedStatement(DataSource dataSource,
            String sql) throws SQLException {

        // TEST
        // if (debug) System.out.println("<==getPreparedStatement==>: " + sql);

        Connection connection = dataSource.getConnection();
        showConnectionCount();
        PreparedStatement prepStmt = connection.prepareStatement(sql);
        return prepStmt;
    }

    public static void closeResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            Statement stmt = resultSet.getStatement();
            try {
                resultSet.close();
            } catch (Exception e) {}
            closeStatement(stmt);
        }
    }

    public static void closeStatement(Statement stmt) throws SQLException {
        // TEST
        // if (debug) System.out.println("<== CloseConnection ==>");

        if (stmt != null) {
            Connection connection = stmt.getConnection();
            try {
                stmt.close();
            } catch (Exception e) {}
            try {
                connection.close();
                showConnectionCount();
            } catch (Exception e) {}
        }
    }

    /**
     * Execute a JDBC query that returns a single string (and return the
     * string!)
     */
    public static String runStringQuery(DataSource dataSource, String sql)
            throws SQLException {
        ResultSet resultSet = null;
        String result = null;

        try {
            resultSet = getResultSet(dataSource, sql);
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            logger.error("Failed attempting to execute sql in runStringQuery: '"
                    + sql + "'");
            e.printStackTrace(System.err);
            throw e;
        } finally {
            closeResultSet(resultSet);
        }

        return result;
    }

    /**
     * Execute a JDBC query that returns a single Integer and return the
     * Integer.
     */
    public static Integer runIntegerQuery(DataSource dataSource, String sql)
            throws SQLException {
        ResultSet resultSet = null;
        Integer result = null;

        // TEST
        // if (debug) System.out.println("<==runIntegerQuery==>: " + sql);

        try {
            resultSet = getResultSet(dataSource, sql);
            if (resultSet.next()) result = new Integer(resultSet.getInt(1));
        } catch (SQLException e) {
            // System.err.println("Failed attempting to execute sql in
            // runIntegerQuery: '" + sql + "'");
            logger.error("Failed attempting to execute sql in runIntegerQuery: '"
                    + sql + "'");
            e.printStackTrace(System.err);
            throw e;
        } finally {
            closeResultSet(resultSet);
        }

        return result;
    }

    /**
     * Execute a JDBC query that returns a list of strings, and return the
     * strings in an array.
     */
    public static String[] runStringArrayQuery(DataSource dataSource, String sql)
            throws SQLException {
        ResultSet resultSet = null;
        Connection connection = null;
        Statement stmt = null;
        Vector<String> v = new Vector<String>();

        // TEST
        // if (debug) System.out.println("<==runStringArrayQuery==>: " + sql);

        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next())
                v.addElement(resultSet.getString(1));
        } catch (SQLException e) {
            // System.err.println("Failed attempting to execute sql in
            // runStringArrayQuery: '" + sql + "'");
            logger.error("Failed attempting to execute sql in runStringArrayQuery: '"
                    + sql + "'");
            throw e;
        } finally {
            closeResultSet(resultSet);
        }

        String result[] = new String[v.size()];
        v.toArray(result);
        return result;
    }

    /**
     * @return the list of column names from the select statement
     */
    public static String[] getColumnNames(DataSource dataSource, String sql)
            throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ArrayList<String> colNames = new ArrayList<String>();

        // TEST
        // if (debug) System.out.println("<==getColumnNames==>: " + sql);

        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.prepareStatement(sql);
            ResultSetMetaData metaData = stmt.getMetaData();
            int colCount = metaData.getColumnCount();
            for (int i = 0; i < colCount; i++) {
                colNames.add(metaData.getColumnName(i));
            }
        } finally {
            closeStatement(stmt);
        }

        return (String[]) colNames.toArray();
    }

    /**
     * Perform a JDBC insert/update/delete.
     * 
     * @return The number of rows affected.
     */
    public static int executeUpdate(DataSource dataSource, String sql)
            throws SQLException {
        int result = -1;
        Connection connection = null;
        Statement stmt = null;

        // TEST
        // if (debug) System.out.println("<==executeUpdate==>: " + sql);

        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.createStatement();
            result = stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Failed executing sql:\n" + sql);
            logger.error("Failed attempting to execute sql in executeUpdate: '"
                    + sql + "'");
            throw e;
        } finally {
            closeStatement(stmt);
        }

        return result;
    }

    /**
     * Execute an SQL statement
     * 
     * @return Value as described in java.sql.statement.execute()
     */
    public static boolean execute(DataSource dataSource, String sql)
            throws SQLException {
        Connection connection = null;
        Statement stmt = null;

        // TEST
        // if (debug) System.out.println("<==execute==>: " + sql);

        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.createStatement();
            return stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed executing sql:");
            System.err.println(sql);
            System.err.println("");
            logger.error("Failed attempting to execute sql in executeUpdate: '"
                    + sql + "'");
            throw e;
        } finally {
            closeStatement(stmt);
        }
    }

    // TODO: this method and writeResultSet should be factored
    public static void printResultSet(ResultSet rs) throws SQLException {
        try {
            int colCount = rs.getMetaData().getColumnCount();
            int count = 0;
            while (rs.next() && count++ <= 100) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }

    public static void writeResultSet(ResultSet rs, StringBuffer buf)
            throws SQLException {
        String newline = System.getProperty("line.separator");
        try {
            int colCount = rs.getMetaData().getColumnCount();
            int count = 0;
            while (rs.next() && count++ <= 100) {
                for (int i = 1; i <= colCount; i++) {
                    buf.append(rs.getString(i) + "\t");
                }
                buf.append(newline);
            }
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }

    public static synchronized void showConnectionCount() {
        
        if (createShowThread) {
            createShowThread = false;
            Thread t = new Thread() {

                public void run() {
                    logger.info("Logging connections.");
                    while (true) {
                        WdkModel model = WdkModel.INSTANCE;
                        if (model != null) {
                            RDBMSPlatformI platform = model.getPlatform();
                            if (platform != null)
                                logger.info("Connections: ("
                                        + platform.getActiveCount() + ", "
                                        + platform.getIdleCount() + ")");
                        }
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException ex) {
                            // TODO Auto-generated catch block
                            ex.printStackTrace();
                        }
                    }
                }
            };
            t.start();
        }
    }

    /**
     * @param sql
     * @return three parts that can create tables according to select result
     *         each row includes three parts: site,remote database connection
     *         string,sql added by samwzm, decompose sql query according to
     *         "FEDERATION" position
     */
    private static String[][] decompose(String sql) {
        if (!sql.startsWith("SELECT * FROM (")) {
            System.out.println("this sql seems not right, the following is the sql:");
            System.out.println(sql);
            System.exit(1);
        }
        if (!sql.endsWith("ORDER BY result_index_column")) {
            System.out.println("this sql seems not right, the following is the sql:");
            System.out.println(sql);
            System.exit(1);
        }

        // trim out the begin part and ending part
        int startPosition = "SELECT * FROM (".length();
        int endPosition = sql.indexOf(") auto_wrapped_");
        sql = sql.substring(startPosition, endPosition);

        // since UNION ALL --...> is the beginning of string, the first split
        // result is ""
        // String delimitor = "<FEDERATION.*?>";
        String delimitor = "[--]*UNION ALL --.*?>";
        String[] queries = sql.split(delimitor);
        int parts = queries.length - 1;
        // the format of results: [SITE][SQL QUERY]
        String[][] results = new String[parts][2];
        for (int i = 0; i < parts; i++) {
            results[i][1] = switchHint(queries[i + 1]);
        }
        // retrieving <UNION ALL --SITE>
        String patternStr = delimitor;
        Pattern pattern = Pattern.compile(patternStr);
        CharSequence inputStr = sql;
        Matcher matcher = pattern.matcher(inputStr);
        boolean matchFound = matcher.find();
        for (int i = 0; i < parts; i++) {
            if (matchFound) {
                String matchString = matcher.group();
                matchString = matchString.substring(0, matchString.length() - 1);
                String[] fields = matchString.split("\\|\\|");
                results[i][0] = fields[1]; // SITE
                matchFound = matcher.find();
            }
        }

        return results;
    }

    /**
     * @param sql
     * @return sql query which put oracle hint at the first position now it can
     *         only deal with one hint in sql which followes "select" added by
     *         samwzm
     */
    private static String switchHint(String sql) {
        Pattern p = Pattern.compile("/\\*+.*\\*/");
        Matcher m = p.matcher(sql);
        boolean b = m.find();
        if (!b) return sql;
        String hint = sql.substring(m.start(), m.end());
        if (debug) System.out.println("the hint is ---" + hint);
        sql = m.replaceFirst(" ");

        Pattern p1 = Pattern.compile("(?i)select");
        Matcher m1 = p1.matcher(sql);
        m1.find();
        int position = m1.end();
        String first = sql.substring(0, position);
        String second = sql.substring(position);
        sql = first + " " + hint + " " + second;
        if (debug) System.out.println("the final sql is ---" + sql);
        return sql;
    }

    /**
     * querying remote dbs through db_link parallel by threads, merge
     * CachedRowSet in memory and then upcast to ResultSet finished--if thread
     * finished or not, no matter finished normally or killed connected--if
     * thread can connect the remote site. false:either can't connect or can't
     * execute sql or thread is killed merge result criterion: if connected is
     * false, meaning that remote site has problem, needn't merge that result
     * 
     * @param dataSource
     * @param queries
     * @return
     */
    private static ResultSet multiMergeResult(DataSource dataSource,
            String[][] queries) {
        // inner class to indicate if thread is finished or not
        class Finished {

            private boolean status;

            public Finished(boolean status) {
                this.status = status;
            }

            synchronized void set(boolean status) {
                this.status = status;
            }

            synchronized boolean get() {
                return status;
            }
        }

        // inner class to indicate if thread connects with the remote site or
        // not
        class Connected {

            boolean status;

            public Connected(boolean status) {
                super();
                this.status = status;
            }

            synchronized void set(boolean status) {
                this.status = status;
            }

            synchronized boolean get() {
                return status;
            }
        }

        // inner class to store CachedRowSet result from thread
        class Result {

            OracleCachedRowSet rowset = null;

            public Result(OracleCachedRowSet rowset) {
                this.rowset = rowset;
            }

            synchronized void set(OracleCachedRowSet rowset) {
                this.rowset = rowset;
            }

            synchronized OracleCachedRowSet get() {
                return rowset;
            }
        }

        // inner class to run sql query parallel
        class Query extends Thread {

            private DataSource dataSource;
            private String site;
            private String sql;
            private Connected connect;
            private Finished finish;
            private Result result;
            private long before, after;
            private double timeout;

            Query(DataSource dataSource, String site, String sql,
                    Connected connect, Finished finish, Result result, double timeout) {
                this.dataSource = dataSource;
                this.site = site;
                this.sql = sql;
                this.connect = connect;
                this.finish = finish;
                this.result = result;
                this.timeout = timeout;
            }

            public void run() {
                if (debug) before = System.currentTimeMillis();
                if (debug && (site.trim()).equalsIgnoreCase("CRYPTO")) {
                    // try {
                    // System.out.println("thread CRYPTO is sleeping now");
                    // Thread.sleep(30000);
                    // } catch (InterruptedException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                }

                // running sql query through apidb
                Statement stmt = null;
                try {
                    Connection connection = dataSource.getConnection();
                    // showConnectionCount();
                    stmt = connection.createStatement();
                    Double timeoutD = timeout/1000;
                    stmt.setQueryTimeout(timeoutD.intValue());
                    ResultSet rs = stmt.executeQuery(sql);
                    if (rs == null) {
                        System.out.println("sql query result shouldn't null");
                        System.exit(1);
                    }
                    OracleCachedRowSet crs = new OracleCachedRowSet();
                    crs.populate(rs);
                    if (crs.size() == 0) {
                        result.set(null);
                        finish.set(true);
                        connect.set(true);
                        if (debug) {
                            after = System.currentTimeMillis();
                            System.out.println("the time costed by thread "
                                    + site + " is " + (after - before) / 1000
                                    + " seconds");
                        }

                        return;
                    }
                    result.set(crs);
                    finish.set(true);
                    connect.set(true);
                    if (debug) {
                        after = System.currentTimeMillis();
                        System.out.println("the time costed by thread " + site
                                + " is " + (after - before) / 1000 + " seconds");
                    }
                    return;
                } catch (SQLException sqlE) {
                    logger.error("Failed attempting to execute sql in getResultSet: '"
                            + sql + "'");
                    System.out.println("Failed attempting to execute sql in getResultSet: '"
                            + sql + "'");
                    connect.set(false);
                    finish.set(true);
                    result.set(null);
                    if (debug) {
                        after = System.currentTimeMillis();
                        System.out.println("the time costed by thread " + site
                                + " is " + (after - before) / 1000 + " seconds");
                    }

                    return;
                } finally {
                    try {
                        closeStatement(stmt);
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                // throw sqlE;
            }// end for. end querying
        }// end thread

        // ***************************main part***************************

        final int TIME_OUT = 180000; // time out set to 3 minutes
        long before = 0, after = 0;
        before = System.currentTimeMillis();

        // TODO: make more flexible to deal with any number of queries
        // automatically
        Connected cConnected = new Connected(false);
        Connected pConnected = new Connected(false);
        Connected tConnected = new Connected(false);
        Finished cFinished = new Finished(false);
        Finished pFinished = new Finished(false);
        Finished tFinished = new Finished(false);
        Result cResult = new Result(null);
        Result pResult = new Result(null);
        Result tResult = new Result(null);
        // suppose the order of "queries" is: crypto, plasmo and toxo
        Thread cThread = new Query(dataSource, queries[0][0], queries[0][1],
                cConnected, cFinished, cResult, TIME_OUT);
        cThread.start();
        Thread pThread = new Query(dataSource, queries[1][0], queries[1][1],
                pConnected, pFinished, pResult, TIME_OUT);
        pThread.start();
        Thread tThread = new Query(dataSource, queries[2][0], queries[2][1],
                tConnected, tFinished, tResult, TIME_OUT);
        tThread.start();

        boolean allFinished = false;
        boolean ifTimeOut = false;

        while (!allFinished) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            after = System.currentTimeMillis();
            if (cFinished.get() && pFinished.get() && tFinished.get())
                allFinished = true; // all threads finished
            if ((after - before) > TIME_OUT) ifTimeOut = true;
            if (ifTimeOut) { // time out, DON'T kill threads, leave it alone, because query timeout has been set
                if (!cFinished.get()) {
//                    try {
//                        cThread.stop();
//                        cThread.destroy();
//                    } catch (Error e) {
//                        // TODO Auto-generated catch block
//                        // e.printStackTrace();
//                    }
                    cConnected.set(false);
                    cFinished.set(true);
                    cResult.set(null);
                }
                if (!pFinished.get()) {
//                    try {
//                        pThread.stop();
//                        pThread.destroy();
//                    } catch (Error e) {
//                        // TODO Auto-generated catch block
//                        // e.printStackTrace();
//                    }
                    pConnected.set(false);
                    pFinished.set(true);
                    pResult.set(null);
                }
                if (!tFinished.get()) {
//                    try {
//                        tThread.stop();
//                        tThread.destroy();
//                    } catch (Error e) {
//                        // TODO Auto-generated catch block
//                        // e.printStackTrace();
//                    }
                    tConnected.set(false);
                    tFinished.set(true);
                    tResult.set(null);
                }
                allFinished = true;
            }// end if(ifTimeOut)
        }// end while

        if (debug) {
            after = System.currentTimeMillis();
            System.out.println("the time for querying database is "
                    + (after - before) / 1000 + " seconds");
            before = System.currentTimeMillis();
        }

        if (!cConnected.get())
            System.out.println("can't connect to crypto, or time out when querying to crypto");
        if (!pConnected.get())
            System.out.println("can't connect to plasmo, or time out when querying to plasmo");
        if (!tConnected.get())
            System.out.println("can't connect to toxo, or time out when querying to toxo");

        // merge CachedResultSet
        if ((cResult.get() == null) && (pResult.get() == null)
                && (tResult.get() == null)) return null;
        OracleCachedRowSet[] results = new OracleCachedRowSet[3];
        for (int i = 0; i < 3; i++) {
            if (cResult.get() != null) {
                results[i] = cResult.get();
                cResult.set(null);
                continue;
            }
            if (pResult.get() != null) {
                results[i] = pResult.get();
                pResult.set(null);
                continue;
            }
            if (tResult.get() != null) {
                results[i] = tResult.get();
                tResult.set(null);
                continue;
            }
        }
        if ((results[0] != null) && (results[1] == null)
                && (results[2] == null)) {
            if (debug) {
                after = System.currentTimeMillis();
                System.out.println("the time for merging results is "
                        + (after - before) / 1000 + " seconds");
            }
            return (ResultSet) results[0];
        }
        try {
            results[0].setReadOnly(false);
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for (int i = 1; i < results.length; i++) {
            if (results[i] == null) continue;
            try {
                if (results[0].getMetaData().getColumnCount() != results[i].getMetaData().getColumnCount()) {
                    System.out.println("fedartion query's structure doesn't correspond each other");
                    System.exit(1);
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

            try {
                results[0].afterLast();
                results[i].beforeFirst();

                while (results[i].next()) {
                    results[0].moveToInsertRow();
                    for (int j = 1; j <= results[0].getMetaData().getColumnCount(); j++) {
                        results[0].updateObject(j, results[i].getObject(j));
                    }
                    results[0].insertRow();
                }
                results[0].beforeFirst();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

            results[i] = null; // clean the memory of result
        } // end merging cachedrowset
        // stop running for 10 seconds to measure memory usage of cachedrowset
        if (debug) {
            after = System.currentTimeMillis();
            System.out.println("the time for merging results is "
                    + (after - before) / 1000 + " seconds");
            /*
             * try {
             * System.out.println("***************************************");
             * System.out.println("stopping running for 10 secondes");
             * System.out.println("look at memory usage of OracleCachedRowSet");
             * Thread.sleep(10000); } catch (InterruptedException e) { // TODO
             * Auto-generated catch block e.printStackTrace(); }
             * System.out.println("sleeping is over, next will be the memory of
             * cachedrowset plus recordinstance");
             */
        }
        return (ResultSet) results[0];
    }// end method multiMergeResult

}
