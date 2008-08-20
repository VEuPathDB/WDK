/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public class SqlResultList implements ResultList {

    private Set<String> columns;
    private ResultSet resultSet;

    public SqlResultList(ResultSet resultSet) throws SQLException {
        this.resultSet = resultSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#close()
     */
    public void close() throws WdkModelException {
        try {
            SqlUtils.closeResultSet(resultSet);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#contains(java.lang.String)
     */
    public boolean contains(String columnName) throws WdkModelException {
        try {
            if (columns == null) columns = SqlUtils.getColumnNames(resultSet);
            return columns.contains(columnName);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#get(java.lang.String)
     */
    public Object get(String columnName) throws WdkModelException {
        try {
            return resultSet.getObject(columnName);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.ResultList#next()
     */
    public boolean next() throws WdkModelException {
        try {
            boolean hasNext = resultSet.next();
            if (!hasNext) SqlUtils.closeResultSet(resultSet);
            return hasNext;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }
}
