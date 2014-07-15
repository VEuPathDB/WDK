/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public interface ResultList {

    public boolean next() throws WdkModelException;
    
    public Object get(String columnName) throws WdkModelException;
    
    public boolean contains(String columnName) throws WdkModelException;
    
    public void close() throws WdkModelException;
}
