/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public class SqlResultList implements ResultList {

    private static final Logger logger = Logger.getLogger(SqlResultList.class);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        SqlUtils.closeResultSetAndStatement(resultSet);
        // TODO Auto-generated method stub
        super.finalize();
    }

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
        SqlUtils.closeResultSetAndStatement(resultSet);
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
            logger.error("Cannot get value for column '" + columnName + "'");
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
            if (!hasNext) close();
            return hasNext;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }
}
