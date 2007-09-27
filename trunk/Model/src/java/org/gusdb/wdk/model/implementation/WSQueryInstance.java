package org.gusdb.wdk.model.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.client.WsfResponse;
import org.gusdb.wsf.client.WsfService;
import org.gusdb.wsf.client.WsfServiceServiceLocator;

public class WSQueryInstance extends QueryInstance {
    
    /**
     * The threshold for the width of the columns that are stored as CLOBs; If
     * the column width is defined < 4000, a varchar(width) will be used,
     * otherwise, the CLOB will be used
     */
    public static final int CLOB_WIDTH_THRESHOLD = 4000;

    private static final Logger logger = Logger.getLogger(WSQueryInstance.class);

    public WSQueryInstance(WSQuery query) {
        super(query);
        this.isCacheable = true;
    }

    public String getLowLevelQuery() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        sb.append("Web Service: " + getServiceUrl() + "\n");
        // print out the param internal values
        sb.append("Params {");
        Map<String, String> paramMap = query.getInternalParamValues(values);
        for (String param : paramMap.keySet()) {
            sb.append(param + "=" + paramMap.get(param) + "; ");
        }
        sb.append("}\n");
        //print out the columns
        sb.append("Columns {");
        for (Column column : query.getColumns()) {
            sb.append(column.getName() +", ");
        }
        sb.append("}\n");
        return sb.toString();
    }

    protected ResultList getNonpersistentResult() throws WdkModelException {
        WSQuery wsQuery = (WSQuery) query;

        try {
            // get a WSF Service client stub
            WsfServiceServiceLocator locator = new WsfServiceServiceLocator();
            WsfService client = locator.getWsfService(getServiceUrl());

            // prepare parameters and columns
            Map<String, String> paramMap = query.getInternalParamValues(values);
            String[] params = new String[paramMap.size()];
            int idx = 0;
            for (String param : paramMap.keySet()) {
                String value = paramMap.get(param);
                params[idx++] = param + "=" + value;
            }

            Column[] columns = query.getColumns();
            String[] columnNames = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnNames[i] = columns[i].getName();
                // if the wsName is defined, reassign it to the columns
                if (columns[i].getWsName() != null)
                    columnNames[i] = columns[i].getWsName();
            }
            
            String invokeKey = query.getFullName();

            // TEST
            logger.info("Invoking " + wsQuery.getProcessName() + " at "
                    + getServiceUrl());
            long start = System.currentTimeMillis();
            
            // get the response from the web service
            WsfResponse response = client.invoke(wsQuery.getProcessName(), invokeKey,
                    params, columnNames);
            this.resultMessage = response.getMessage();
            
            long end = System.currentTimeMillis();
            logger.debug("Invoking on client takes " + ((end - start)/1000.0) + " seconds.");

            // TEST
            logger.debug("WSQI Result Message:" + resultMessage);

            String[][] result = response.getResults();
	    
	    logger.info("Result Array size = " + result.length);

            return new WSResultList(this, result);

        } catch (RemoteException e) {
            throw new WdkModelException(e);
        } catch (ServiceException e) {
            throw new WdkModelException(e);
        }
    }

    protected void writeResultToTable(String resultTableName, ResultFactory rf)
            throws WdkModelException {

        long start = System.currentTimeMillis();
        
        // invoke web service to get the result
        ResultList resultList = getNonpersistentResult();

        // TEST
        logger.info("Caching the result from "
                + ((WSQuery) query).getProcessName());

        RDBMSPlatformI platform = rf.getRDBMSPlatform();
        DataSource dataSource = platform.getDataSource();

        Column[] columns = query.getColumns();
        Set<String> clobCols = new LinkedHashSet<String>();

        StringBuffer createSqlB = new StringBuffer("create table "
                + resultTableName + "(");
        StringBuffer insertSqlB = new StringBuffer("insert into "
                + resultTableName + " (");
        StringBuffer insertSqlV = new StringBuffer(" values (");

        boolean hasProjectId = false;
        
        for (Column column : columns) {
            String colName = column.getName();
            int cw = column.getWidth();
            
            // check if it is a project_id column
            if (colName.equals(Query.PROJECT_ID_COLUMN)) hasProjectId = true;

            // the clob datatype is DBMS specific
            String clobType = platform.getClobDataType();
            createSqlB.append(colName + " ");
            if (cw >= CLOB_WIDTH_THRESHOLD) {
                createSqlB.append(clobType + ", ");
                clobCols.add(colName);
            } else {
                createSqlB.append("varchar(" + cw + "), ");
            }
            insertSqlB.append(colName + ", ");
            insertSqlV.append("?,");
        }
        // check if we need to add a project_id column
        if (!hasProjectId) {
            createSqlB.append(Query.PROJECT_ID_COLUMN + " varchar(50), ");
            insertSqlB.append(Query.PROJECT_ID_COLUMN + ", ");
            insertSqlV.append("'" + query.getProjectId() + "', ");
        }
        
        createSqlB.append(ResultFactory.RESULT_TABLE_I + " "
                + platform.getNumberDataType() + " (12), ");
        createSqlB.append(ResultFactory.COLUMN_SORTING_INDEX + " "
                + platform.getNumberDataType() + " (12))");
        
        // set sorting index id as 0 by default
        insertSqlB.append(ResultFactory.RESULT_TABLE_I + ", ");
        insertSqlB.append(ResultFactory.COLUMN_SORTING_INDEX + ") ");
        insertSqlB.append(insertSqlV);
        insertSqlB.append("?, 0)");
        
        PreparedStatement pstmt = null;
        try {
            // create cache table
            SqlUtils.execute(dataSource, createSqlB.toString());

            pstmt = SqlUtils.getPreparedStatement(dataSource,
                    insertSqlB.toString());

            int idx = 0;
            while (resultList.next()) {
                for (int index = 0; index < columns.length; index++) {
                    String colName = columns[index].getName();
                    String val = (String) resultList.getValueFromResult(colName);
                    
                    // check if we need to fill the project id
                    if (colName.equals(Query.PROJECT_ID_COLUMN) && (val == null || val.trim().length() == 0))
                        val = query.getProjectId();

                    // check if it's clob field or not
                    if (clobCols.contains(colName)) {
                        platform.updateClobData(pstmt, index + 1, val, false);
                    } else {
			int colWidth = columns[index].getWidth();
			if (val != null && val.length() > colWidth) {
			    val = val.substring(0, colWidth-5) + "[...]";
			}
                       pstmt.setString(index + 1, val);
                    }
                }
                pstmt.setInt(columns.length + 1, ++idx);
                pstmt.addBatch();
		if(idx % 1000 == 0) pstmt.executeBatch();
            }
	    logger.info("idx = "+idx);
            // do a batch update
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new WdkModelException(e);
        } finally {
            long end = System.currentTimeMillis();
            logger.info("Insert cache takes: " + ((end - start) / 1000.0)
                    + " seconds.");
            try {
                SqlUtils.closeStatement(pstmt);
            } catch (SQLException ex) {
                throw new WdkModelException(
                        "Failed closing the PreparedStatement.", ex);
            }
        }
    }

    private URL getServiceUrl() throws WdkModelException {
        try {
            return new URL(((WSQuery) query).getWebServiceUrl());
        } catch (MalformedURLException e) {
            throw new WdkModelException(e);
        }
    }
}
