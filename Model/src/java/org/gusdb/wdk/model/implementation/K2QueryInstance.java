/*
 * Created on Feb 16, 2005
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.gusdb.wdk.model.AttributeFieldValue;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkLogManager;

import org.gusdb.wdk.model.WdkModelException;

import K2.absyn.expns.absynExpA;
import K2.absyn.expns.absynNumInt;
import K2.absyn.expns.absynString;
import K2.absyn.expns.absynVariant;
import K2.system.ConnectionException;
import K2.system.Login;
import K2.system.QueryResultHolder;
import K2.system.RMIConnectionFactoryI;
import K2.system.RMIUserConnectionI;
import K2.system.ServerException;

/**
 * The instance of K2Query
 * @author Daud @University of Pennsylvania (daud@seas.upenn.edu)
 *
 */
public class K2QueryInstance extends QueryInstance {
     /**
     * The unique name of the table in a database namespace which holds the cached 
     * results for this Instance.
     */
    String resultTable = null;
    private static final Logger logger =
          WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.OqlQueryInstance");
  
    
    public K2QueryInstance(Query metadata) {
        super(metadata);
    }
    
    public ResultList getResult() throws WdkModelException {
        return getNonpersistentResult();    
    }
    
    public  String getResultAsTable() throws WdkModelException {
        if (resultTable == null) 
            resultTable = getResultFactory().getResultAsTable(this);
        
        return resultTable;
    }
    
    public  Collection getCacheValues() throws WdkModelException {
        return getValues();    
    }
    
    protected  String getSqlForCache() throws WdkModelException {
        SqlQuery q = (SqlQuery)query;
        String cacheSql = q.getResultFactory().getSqlForCache(this);
        return cacheSql;
    }
    
    /**
     * Get the result list by connecting to K2
     */
    protected ResultList getNonpersistentResult() throws WdkModelException {
        String rmiAddress =  ((K2Query)query).getRmiNameBinding();
        
        RMIConnectionFactoryI server = null;
        
        try {
            // Get the stub for the server
            server = (RMIConnectionFactoryI)(Naming.lookup(rmiAddress));
        } catch (Exception e) {
            throw new RuntimeException("Unable to bind with address " + rmiAddress, e);
        }
        
        RMIUserConnectionI connection = null;
        
        try {
            // this is a default K2 login
            // STEVE: This one should belong to some kind of configuration file
            connection = server.getUserConnection(new Login("readonly", "readonly"));
            String oql = getOql();
            
            // execute the oql, K2 will put it into QueryResultHolder class
            QueryResultHolder qrh = connection.executeOQLQuery(oql);
            return new K2ResultList(this, null, qrh, connection);
        } catch (RemoteException e) {
            throw new RuntimeException("Unable to bind with address " + rmiAddress, e);           
        } catch(ConnectionException e) {
            throw new RuntimeException("Unable to bind with address " + rmiAddress, e);           
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Write the result to table
     */
    protected void writeResultToTable(String resultTableName, 
            ResultFactory rf) throws WdkModelException {
        
        // STEVE: I have to make ResultFactory.getRDBMSPlatform public
        // in order to get the platform
        // Is there any other way to obtain RDBMSPlatform?
        RDBMSPlatformI platform = rf.getRDBMSPlatform();
        DataSource ds = platform.getDataSource();
           
        try {                     
            Column[] columns = query.getColumns();
            String createTableSql = "create table " + resultTableName + "(";
            
            for(int i=0;i<columns.length;i++) {
                createTableSql += columns[i].getName() + " " +  columns[i].getSpecialType() + ",";
            }
            
            // add counter to it
            createTableSql += "i integer );";
            logger.info("SQL For Create Table " + resultTableName + ":\n" + createTableSql);
            
            // create the table
            SqlUtils.execute(ds, createTableSql);
            
            // now insert sequentially....
            ResultList result = getNonpersistentResult();
            Iterator rows = result.getRows();
            int index = 0;
            
            while(rows.hasNext()) {
                String currentInsertSql = "insert into " + resultTableName + " values (";
                Map currentRow = (Map)rows.next();
                for(int i=0;i<columns.length;i++) {               
                    AttributeFieldValue column = 
                        (AttributeFieldValue)currentRow.get(columns[i].getName());
                    
                    logger.info("Current column "  + columns[i].getName() + " value = " +
                            column.getValue().getClass());
                    
                    Object k2Type = column.getValue();
                    
                    // add type as it goes......
                    if(k2Type instanceof absynVariant) {
                        // k2 type is always in form of its tree (a little bit confusing
                        // you have to go to the class to see it, a little understanding
                        // of ODL is helpful
                        absynVariant variant = (absynVariant)k2Type;
                        absynExpA value = variant.value();
                        logger.info("K2 Type " + value.getClass());
                        
                        // again the value in the variant could be one of the following:
                        if(value instanceof absynNumInt) {
                            currentInsertSql += ((absynNumInt)value).intValue() + ",";
                        } else if (value instanceof absynString) {
                            currentInsertSql += "'" + ((absynString)value).value() + "',";                       
                        } 
                        
                    }
                }
                
                // this is the last one for column i
                currentInsertSql += index++ + ")";
                logger.info("\nCurrent insert to " + resultTableName + ": " +
                        currentInsertSql);
                SqlUtils.execute(ds, currentInsertSql);
            }
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
      
    }  
    
    protected String getOql()  throws WdkModelException {       
        String initOql = 
            ((K2Query)query).instantiateOql(query.getInternalParamValues(values));
        return initOql;
    }
}
