/*
 * Created on Feb 16, 2005
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkLogManager;
import org.gusdb.wdk.model.WdkModelException;

import K2.absyn.expns.absynJGLSetOrBag;
import K2.absyn.expns.absynRecordA;
import K2.absyn.util.PGNodeA;
import K2.system.QueryResultHolder;
import K2.system.RMIUserConnectionI;
import K2.system.ServerException;

/**
 * @author Daud @University of Pennsylvania (daud@seas.upenn.edu)
 *
 */
public class K2ResultList extends ResultList {
    QueryResultHolder resultHolder;
    Iterator resultIterator;
    Object currentObject;
    RMIUserConnectionI conn;
    private static final Logger logger = 
        WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.OqlResultList");
    
    
    public K2ResultList(QueryInstance queryInstance, String resultTableName,
            QueryResultHolder resultHolder, RMIUserConnectionI conn) {
        super(queryInstance, resultTableName);
        this.resultHolder = resultHolder;
        this.conn = conn;
        // abysnJGLSetOrBag is the return of bag of struct from
        // relational database
        // we need to make sure that it is a set of tuple
        if(resultHolder.result instanceof absynJGLSetOrBag) {
            absynJGLSetOrBag bag = (absynJGLSetOrBag)resultHolder.result;
            resultIterator = bag.iterator();
        } else {
            throw new RuntimeException("Only abysnJGLSetOrBag is supported currently");
        }
    }
    
    public  void checkQueryColumns(Query query, boolean checkAll) 
    throws WdkModelException {
        
    }
    
    public  boolean next() throws WdkModelException {       
        boolean hasNext =  resultIterator.hasNext();
        
        if(hasNext) {
            currentObject = resultIterator.next();
        }
        return hasNext;
    }
    
    public  void close() throws WdkModelException {
        try {
            conn.close();
        } catch (RemoteException e) {
            throw new WdkModelException("Unable to close connection", e);
        } catch (ServerException e) {
            throw new WdkModelException("Unable to close connection", e);
        }
    }
    
    public  void print() throws WdkModelException {
        PrintWriter pw = new PrintWriter(System.out, true);
        pw.println("Query = " + ((K2QueryInstance)getInstance()).getOql());
        pw.println("Response = ");
        resultHolder.result.outputToWriter(pw, PGNodeA.INDENTED_STRING);
        pw.flush();
    }
    
    protected  Object getValueFromResult(String attributeName) throws WdkModelException {
        // this should be a abysn Record type
        logger.info("Getting attribute name " + attributeName + " from class " +
                currentObject.getClass());
        
        // STEVE: Todo: Create an object adapter from K2 to Java
        if(currentObject instanceof absynRecordA) {
            absynRecordA record = (absynRecordA)currentObject;
            Object attribute = record.proj(attributeName);
            
            // We should do translation to java object here
            return attribute;
        } else {
            throw new RuntimeException("Only record is supported currently");
        }
    }
    
}
