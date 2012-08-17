/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ArrayResultList;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wsf.client.WsfService;
import org.gusdb.wsf.client.WsfServiceServiceLocator;
import org.gusdb.wsf.plugin.WsfRequest;
import org.gusdb.wsf.plugin.WsfResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class ProcessQueryInstance extends QueryInstance {

    private static final Logger logger = Logger
            .getLogger(ProcessQueryInstance.class);

    private ProcessQuery query;
    private int signal;

    /**
     * @param query
     * @param values
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public ProcessQueryInstance(User user, ProcessQuery query,
            Map<String, String> values, boolean validate, int assignedWeight,
            Map<String, String> context) throws WdkModelException, WdkUserException {
        super(user, query, values, validate, assignedWeight, context);
        this.query = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#appendSJONContent(org.json.JSONObject
     * )
     */
    @Override
    protected void appendSJONContent(JSONObject jsInstance)
            throws JSONException {
        jsInstance.put("signal", signal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#insertToCache(java.sql.Connection
     * , java.lang.String)
     */
    @Override
    public void insertToCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException, WdkUserException {
        logger.debug("inserting process query result to cache...");
        Map<String, Column> columns = query.getColumnMap();
        String weightColumn = Utilities.COLUMN_WEIGHT;

        // prepare the sql
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(tableName);
        sql.append(" (");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID);
        for (String column : columns.keySet()) {
            sql.append(", " + column);
        }
        if (query.isHasWeight() && !columns.containsKey(weightColumn))
            sql.append(", " + weightColumn);
        sql.append(") VALUES (");
        sql.append(instanceId);
        for (int i = 0; i < columns.size(); i++) {
            sql.append(", ?");
        }
        // insert weight to the last column, if doesn't exist
        if (query.isHasWeight() && !columns.containsKey(weightColumn))
            sql.append(", " + assignedWeight);
        sql.append(")");

        DBPlatform platform = query.getWdkModel().getQueryPlatform();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            ResultList resultList = getUncachedResults();
            int rowId = 0;
            while (resultList.next()) {
                int columnId = 1;
                for (Column column : columns.values()) {
                    String value = (String) resultList.get(column.getName());

                    // determine the type
                    ColumnType type = column.getType();
                    if (type == ColumnType.BOOLEAN) {
                        ps.setBoolean(columnId, Boolean.parseBoolean(value));
                    } else if (type == ColumnType.CLOB) {
                        platform.setClobData(ps, columnId, value, false);
                    } else if (type == ColumnType.DATE) {
                        ps.setTimestamp(columnId,
                                new Timestamp(Date.valueOf(value).getTime()));
                    } else if (type == ColumnType.FLOAT) {
                        ps.setFloat(columnId, Float.parseFloat(value));
                    } else if (type == ColumnType.NUMBER) {
                        ps.setInt(columnId, Integer.parseInt(value));
                    } else {
                        int width = column.getWidth();
                        if (value != null && value.length() > width) {
                            logger.warn("Column [" + column.getName()
                                    + "] value truncated.");
                            value = value.substring(0, width - 3) + "...";
                        }
                        ps.setString(columnId, value);
                    }
                    columnId++;
                }
                ps.addBatch();

                rowId++;
                if (rowId % 1000 == 0)
                    ps.executeBatch();
            }
            if (rowId % 1000 != 0)
                ps.executeBatch();
        }
        catch (SQLException e) {
        	throw new WdkUserException("Unable to insert record into cache.", e);
        }
        finally {
            // close the statement manually, since we need to keep the
            // connection open to finish the transaction.
            if (ps != null)
                try { ps.close(); }
                catch (SQLException e) { logger.error("Unable to close PreparedStatement!"); }
        }
        logger.debug("process query cache insertion finished.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#getUncachedResults(org.gusdb.
     * wdk.model.Column[], java.lang.Integer, java.lang.Integer)
     */
    @Override
				protected ResultList getUncachedResults() throws WdkModelException, WdkUserException {
						logger.debug("\n\n\n***** ProcessQueryInstance: getUncachedResults:  WE GOT HERE********\n\n\n");
        WsfRequest request = new WsfRequest();
        request.setPluginClass(query.getProcessName());
        request.setProjectId(wdkModel.getProjectId());

        // prepare parameters
        Map<String, String> paramValues = getInternalParamValues();
        HashMap<String, String> params = new HashMap<String, String>();
        for (String name : paramValues.keySet()) {
            params.put(name, paramValues.get(name));
        }
        request.setParams(params);

        // prepare columns
        Map<String, Column> columns = query.getColumnMap();
        String[] columnNames = new String[columns.size()];
        Map<String, Integer> indices = new LinkedHashMap<String, Integer>();
        columns.keySet().toArray(columnNames);
        String temp = "";
        for (int i = 0; i < columnNames.length; i++) {
            // if the wsName is defined, reassign it to the columns
            Column column = columns.get(columnNames[i]);
            if (column.getWsName() != null)
                columnNames[i] = column.getWsName();
            indices.put(column.getName(), i);
            temp += columnNames[i] + ", ";
        }
        request.setOrderedColumns(columnNames);
        logger.debug("process query columns: " + temp);

        request.setContext(context);

        StringBuffer resultMessage = new StringBuffer();
        try {
						logger.debug("\n\n\n***** ProcessQueryInstance: getUncachedResults:  WE GOT HERE inside try before response********\n\n\n");
            WsfResponse response = getResponse(request, query.isLocal());
					
            this.resultMessage = response.getMessage();
            this.signal = response.getSignal();
            String[][] content = response.getResult();

            logger.debug("WSQI Result Message:" + resultMessage);
            logger.info("Result Array size = " + content.length);

            // add weight if needed
            String weightColumn = Utilities.COLUMN_WEIGHT;
            if (query.isHasWeight() && !columns.containsKey(weightColumn)) {
                indices.put(weightColumn, indices.size());
                for (int i = 0; i < content.length; i++) {
                    String[] line = content[i];
                    String[] newLine = new String[line.length + 1];
                    System.arraycopy(line, 0, newLine, 0, line.length);
                    newLine[line.length] = Integer.toString(assignedWeight);
                    content[i] = newLine;
                }
            }

            return new ArrayResultList<String>(indices, content);

        } catch (RemoteException ex) {
		logger.debug("\n\n\n***** ProcessQueryInstance: getUncachedResults: caught RemoteException*******\n\n\n");
            throw new WdkModelException(ex);
        } catch (ServiceException ex) {
		logger.debug("\n\n\n***** ProcessQueryInstance: getUncachedResults: caught ServiceException*******\n\n\n");
            throw new WdkModelException(ex);
        } catch (MalformedURLException ex) {
		logger.debug("\n\n\n***** ProcessQueryInstance: getUncachedResults: caught MalformedException*******\n\n\n");
            throw new WdkModelException(ex);
        } catch (JSONException ex) {
		logger.debug("\n\n\n***** ProcessQueryInstance: getUncachedResults: caught JSONException*******\n\n\n");
            throw new WdkModelException(ex);
		}
    }

    private WsfResponse getResponse(WsfRequest request, boolean local) throws WdkModelException, ServiceException, MalformedURLException, RemoteException, JSONException {
    	
        String serviceUrl = query.getWebServiceUrl();

        // DEBUG
        logger.info("Invoking " + request.getPluginClass() + " at "
                + serviceUrl);
        long start = System.currentTimeMillis();

        String jsonRequest = request.toString();

        WsfResponse response;
        if (local) { // invoke the process query locally
            org.gusdb.wsf.service.WsfService service = new org.gusdb.wsf.service.WsfService();
            // get the response from the local service
            response = service.invoke(jsonRequest);
            int packets = response.getTotalPackets();
            if (packets > 1) {
                StringBuffer buffer = new StringBuffer(
                        response.getResult()[0][0]);
                String requestId = response.getRequestId();
                for (int i = 1; i < packets; i++) {
                    logger.debug("getting message " + requestId + " pieces: "
                            + i + "/" + packets);
                    String more = service.requestResult(requestId, i);
                    buffer.append(more);
                }
                String[][] content = Utilities.convertContent(buffer.toString());
                response.setResult(content);
            }
        } else { // invoke the process query via web service
            // get a WSF Service client stub
            WsfServiceServiceLocator locator = new WsfServiceServiceLocator();
            WsfService client = locator.getWsfService(new URL(serviceUrl));

            // get the response from the web service
            response = client.invoke(jsonRequest);
            int packets = response.getTotalPackets();
            if (packets > 1) {
                StringBuffer buffer = new StringBuffer(
                        response.getResult()[0][0]);
                String requestId = response.getRequestId();
                for (int i = 1; i < packets; i++) {
                    logger.debug("getting message " + requestId + " pieces: "
                            + i + "/" + packets);
                    String more = client.requestResult(requestId, i);
                    buffer.append(more);
                }
                String[][] content = Utilities
                        .convertContent(buffer.toString());
                response.setResult(content);
            }
        }
        long end = System.currentTimeMillis();
        logger.debug("Client took " + ((end - start) / 1000.0) + " seconds.");

        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#getSql()
     */
    @Override
    public String getSql() throws WdkModelException, WdkUserException {
        // always get sql that queries on the cached result
        return getCachedSql();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#createCache(java.sql.Connection,
     * java.lang.String, int)
     */
    @Override
    public void createCache(Connection connection, String tableName,
            int instanceId, String[] indexColumns) throws WdkModelException, WdkUserException {
        logger.debug("creating process query cache...");
        DBPlatform platform = query.getWdkModel().getQueryPlatform();
        Column[] columns = query.getColumns();

        StringBuffer sqlTable = new StringBuffer("CREATE TABLE ");
        sqlTable.append(tableName).append(" (");

        // define the instance id column
        String numberType = platform.getNumberDataType(12);
        sqlTable.append(CacheFactory.COLUMN_INSTANCE_ID + " " + numberType);
        sqlTable.append(" NOT NULL");
        if (query.isHasWeight())
            sqlTable.append(", " + Utilities.COLUMN_WEIGHT + " " + numberType);

        // define the rest of the columns
        for (Column column : columns) {
            // weight column is already added to the sql.
            if (column.getName().equals(Utilities.COLUMN_WEIGHT)
                    && query.isHasWeight())
                continue;

            int width = column.getWidth();
            ColumnType type = column.getType();

            String strType;
            if (type == ColumnType.BOOLEAN) {
                strType = platform.getBooleanDataType();
            } else if (type == ColumnType.CLOB) {
                strType = platform.getClobDataType();
            } else if (type == ColumnType.DATE) {
                strType = platform.getDateDataType();
            } else if (type == ColumnType.FLOAT) {
                strType = platform.getFloatDataType(width);
            } else if (type == ColumnType.NUMBER) {
                strType = platform.getNumberDataType(width);
            } else if (type == ColumnType.STRING) {
                strType = platform.getStringDataType(width);
            } else {
                throw new WdkModelException("Unknown data type [" + type
                        + "] of column [" + column.getName() + "]");
            }

            sqlTable.append(", " + column.getName() + " " + strType);
        }
        sqlTable.append(")");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(sqlTable.toString());

            ResultFactory resultFactory = wdkModel.getResultFactory();
            resultFactory.createCacheTableIndex(connection, tableName,
                    indexColumns);

            // also insert the result into the cache
            insertToCache(connection, tableName, instanceId);
        }
        catch (SQLException e) {
        	throw new WdkUserException("Unable to create cache.", e);
        }
        finally {
            if (stmt != null)
                try { stmt.close(); }
                catch (SQLException e) { logger.error("Unable to close Statement!", e); }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#getResultSize()
     */
    @Override
    public int getResultSize() throws WdkModelException, WdkUserException {
        if (!isCached()) {
            int count = 0;
            ResultList resultList = getResults();
            while (resultList.next()) {
                count++;
            }
            return count;
        } else
            return super.getResultSize();
    }

}
