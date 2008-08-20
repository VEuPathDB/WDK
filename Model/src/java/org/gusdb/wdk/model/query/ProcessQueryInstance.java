/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ArrayResultList;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wsf.client.WsfService;
import org.gusdb.wsf.client.WsfServiceServiceLocator;
import org.gusdb.wsf.plugin.WsfResult;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class ProcessQueryInstance extends QueryInstance {

    private static final Logger logger = Logger.getLogger(ProcessQueryInstance.class);

    private ProcessQuery query;
    private int signal;

    /**
     * @param query
     * @param values
     * @throws WdkModelException
     */
    public ProcessQueryInstance(ProcessQuery query, Map<String, Object> values)
            throws WdkModelException {
        super(query, values);
        this.query = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#appendSJONContent(org.json.JSONObject)
     */
    @Override
    protected void appendSJONContent(JSONObject jsInstance)
            throws JSONException {
        jsInstance.put("signal", signal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#createCache(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void createCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException {
        DBPlatform platform = query.getWdkModel().getQueryPlatform();
        Column[] columns = query.getColumns();

        StringBuffer sqlTable = new StringBuffer("CREATE TABLE ");
        sqlTable.append(tableName);
        sqlTable.append(" (");

        sqlTable.append(CacheFactory.COLUMN_INSTANCE_ID);
        sqlTable.append(" ");
        sqlTable.append(platform.getNumberDataType(12));
        sqlTable.append(" NOT NULL");

        for (Column column : columns) {
            sqlTable.append(", ");
            sqlTable.append(column.getName());
            sqlTable.append(" ");
            sqlTable.append(platform.getStringDataType(column.getWidth()));
        }
        sqlTable.append(")");

        Statement stmt = null;
        try {
            try {
                stmt = connection.createStatement();
                stmt.execute(sqlTable.toString());
            } catch (SQLException ex) {
                if (stmt != null && !stmt.isClosed()) stmt.close();
            }
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#insertToCache(java.sql.Connection,
     *      java.lang.String)
     */
    @Override
    public void insertToCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        Column[] columns = query.getColumns();

        // prepare the sql
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(tableName);
        sql.append(" (");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID);
        for (Column column : columns) {
            sql.append(", ");
            sql.append(column.getName());
        }
        sql.append(") VALUES (");
        sql.append(instanceId);
        for (int i = 0; i < columns.length; i++) {
            sql.append(", ?");
        }
        sql.append(")");

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            ResultList resultList = getUncachedResults(columns, null, null);
            int rowId = 0;
            while (resultList.next()) {
                int columnId = 1;
                for (Column column : columns) {
                    String value = (String) resultList.get(column.getName());
                    ps.setString(columnId++, value);
                }
                ps.addBatch();

                rowId++;
                if (rowId % 1000 == 0) ps.executeBatch();
            }
            if (rowId % 1000 != 0) ps.executeBatch();
        } finally {
            // close the statement manually, since we need to keep the
            // connection open to finish the transaction.
            if (ps != null && !ps.isClosed()) ps.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#getUncachedResults(org.gusdb.wdk.model.Column[],
     *      java.lang.Integer, java.lang.Integer)
     */
    @Override
    protected ResultList getUncachedResults(Column[] columns,
            Integer startIndex, Integer endIndex) throws WdkModelException,
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        // prepare parameters and columns
        Map<String, String> paramValues = getInternalParamValues();
        String[] params = new String[paramValues.size()];
        int idx = 0;
        for (String param : paramValues.keySet()) {
            String value = paramValues.get(param);
            params[idx++] = param + "=" + value;
        }

        String[] columnNames = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnNames[i] = columns[i].getName();
            // if the wsName is defined, reassign it to the columns
            if (columns[i].getWsName() != null)
                columnNames[i] = columns[i].getWsName();
        }

        String invokeKey = query.getFullName();

        StringBuffer resultMessage = new StringBuffer();
        try {
            WsfResult result = getResult(query.getProcessClass(), invokeKey,
                    params, columnNames, query.isLocal());
            this.resultMessage = result.getMessage();
            this.signal = result.getSignal();

            // TEST
            logger.debug("WSQI Result Message:" + resultMessage);
            logger.info("Result Array size = " + result.getResult().length);

            return new ArrayResultList<String>(columns, result.getResult());

        } catch (RemoteException ex) {
            throw new WdkModelException(ex);
        } catch (ServiceException ex) {
            throw new WdkModelException(ex);
        } catch (MalformedURLException ex) {
            throw new WdkModelException(ex);
        }
    }

    private WsfResult getResult(String processName, String invokeKey,
            String[] params, String[] columnNames, boolean local)
            throws ServiceException, WdkModelException, RemoteException,
            MalformedURLException {
        String serviceUrl = query.getWebServiceUrl();

        // DEBUG
        logger.info("Invoking " + processName + " at " + serviceUrl);
        long start = System.currentTimeMillis();

        WsfResult result;
        if (local) { // invoke the process query locally
            org.gusdb.wsf.service.WsfService service = new org.gusdb.wsf.service.WsfService();

            // get the response from the local service
            result = service.invokeEx(processName, invokeKey, params,
                    columnNames);
        } else { // invoke the process query via web service
            // get a WSF Service client stub
            WsfServiceServiceLocator locator = new WsfServiceServiceLocator();
            WsfService client = locator.getWsfService(new URL(serviceUrl));

            // get the response from the web service
            result = client.invokeEx(processName, invokeKey, params,
                    columnNames);
        }
        long end = System.currentTimeMillis();
        logger.debug("Client took " + ((end - start) / 1000.0) + " seconds.");

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#getSql()
     */
    @Override
    public String getSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        // always get sql that queries on the cached result
        return getCachedSql();
    }

}
