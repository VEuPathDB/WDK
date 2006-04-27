package org.gusdb.wdk.model.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.sql.DataSource;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.service.WsfService;
import org.gusdb.wsf.service.WsfServiceServiceLocator;

public class WSQueryInstance extends QueryInstance  {

    private static final Logger logger = Logger.getLogger(WSQueryInstance.class);
    
    public WSQueryInstance (WSQuery query) {
        super(query);
        this.isCacheable = true;
    }

    public String getLowLevelQuery() throws WdkModelException {
	return null;
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

            // TEST
            logger.info("Invoking " + wsQuery.getProcessName() + " at " + getServiceUrl());
            
            // get
            String[][] result = client.invoke(wsQuery.getProcessName(), params,
                    columnNames);
            return new WSResultList(this, result);

        } catch (RemoteException e) {
            throw new WdkModelException(e);
        } catch (ServiceException e) {
            throw new WdkModelException(e);
        }
    }

    protected void writeResultToTable(String resultTableName, ResultFactory rf)
            throws WdkModelException {
        // TEST
        logger.info("Caching the result from "
                + ((WSQuery) query).getProcessName());

        RDBMSPlatformI platform = rf.getRDBMSPlatform();
        DataSource dataSource = platform.getDataSource();

        Column[] columns = query.getColumns();
        StringBuffer createSqlB = new StringBuffer("create table "
                + resultTableName + "(");

        Map<String, Integer> colIsClob = new LinkedHashMap<String, Integer>();
        for (Column column : columns) {
            int cw = column.getWidth();
            // the clob datatype is DBMS specific
            String clob = platform.getClobDataType();

             createSqlB.append(column.getName()
                    + ((cw > 2000) ? (" " + clob + ", ")
                            : (" varchar(" + cw + "), ")));
            
            if (cw > 2000) {
                colIsClob.put(column.getName(), new Integer(1));
            }
        }
        String createSql = createSqlB.toString()
                + (ResultFactory.RESULT_TABLE_I + " "
                        + platform.getNumberDataType() + " (12))");

        String insertSql = "insert into " + resultTableName + " values (";

        ResultList resultList = getNonpersistentResult();

        // Since each row has same number/type of fields, the PreparedStatement
        // should be constructed outside of the while loop, to make it really
        // "prepared". Consider refactoring the code later
        PreparedStatement pstmt = null;
        try {

            SqlUtils.execute(dataSource, createSql);

            int idx = 0;
            while (resultList.next()) {
                StringBuffer insertSqlB = new StringBuffer(insertSql);
                Vector<String> v = new Vector<String>();
                for (Column column : columns) {
                    String val = (String) resultList.getValueFromResult(column.getName());
                    insertSqlB.append("?,");
                    v.add(val);
                }
                String[] vals = new String[v.size()];
                v.copyInto(vals);

                String s = insertSqlB.toString() + "?)";
                pstmt = SqlUtils.getPreparedStatement(dataSource, s);

                for (int i = 0; i < vals.length; i++) {
                    //todo: may need to handle large strings for clob columns?
                    pstmt.setString(i + 1, vals[i]);
                }
                pstmt.setInt(vals.length + 1, ++idx);
                pstmt.execute();
                SqlUtils.closeStatement(pstmt);
                pstmt = null;
            }
        } catch (SQLException e) {
            try {
                SqlUtils.closeStatement(pstmt);
            } catch (SQLException ex) {
                throw new WdkModelException("Failed closing the PreparedStatement.",  ex);
            }
            throw new WdkModelException(e);
        }
    }

    private URL getServiceUrl() throws WdkModelException {
	try {
	    return new URL(((WSQuery)query).getWebServiceUrl());
	} catch (MalformedURLException e) {
	    throw new WdkModelException(e);
	}
     }

}
