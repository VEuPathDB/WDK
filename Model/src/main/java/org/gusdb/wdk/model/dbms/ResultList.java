/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public interface ResultList extends AutoCloseable {

    public boolean next() throws WdkModelException;
    
    public Object get(String columnName) throws WdkModelException;
    
    public boolean contains(String columnName) throws WdkModelException;
    
    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws WdkModelException;
}
