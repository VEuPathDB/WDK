package org.gusdb.gus.wdk.model.query;

import java.util.Map;
import java.util.Iterator;
import javax.sql.DataSource;
import java.sql.SQLException;

public class SqlQuery extends Query {
    
    String sql;

    public SqlQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setSql(String sql) {
	this.sql = sql;
    }

    public String getSql() {
	return sql;
    }

    public String[] getColumnNames(DataSource dataSource, String sql) throws SQLException {
	return SqlUtils.getColumnNames(dataSource, sql);
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected QueryInstance makeInstance(boolean useCache) {
	return new SqlQueryInstance(this);
    }

    /**
     * @param values These values are assumed to be pre-validated
     */
    protected String instantiateSql(Map values) {
	String s = this.sql;
	Iterator keySet = values.keySet().iterator();
	while (keySet.hasNext()) {
	    String key = (String)keySet.next();
	    String regex = "\\$\\$" + key  + "\\$\\$";
	    s = s.replaceAll(regex, (String)values.get(key));
	}
	return s;
    }

    protected StringBuffer formatHeader() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = super.formatHeader();
       buf.append("  sql='" + sql + "'" + newline);
       return buf;
    }
}
