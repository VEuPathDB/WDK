package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;
import java.sql.SQLException;
import java.sql.ResultSet;


public class SqlEnumParam extends Param {
    
    Boolean multiPick = new Boolean(false);
    SqlQuery sqlQuery;
    Hashtable hash;

    public SqlEnumParam () {}

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setMultiPick(Boolean multiPick) {
	this.multiPick = multiPick;
    }

    public Boolean getMultiPick() {
	return multiPick;
    }

    public void setSqlQuery(SqlQuery sqlQuery) {
	this.sqlQuery = sqlQuery;
    }

    public SqlQuery getSqlQuery() {
	return sqlQuery;
    }

    public Hashtable getKeysAndValues(SqlResultSetManager resultMgr) throws SQLException {
	if (hash == null) {
	    hash = new Hashtable();
	    SqlQueryInstance instance = new SqlQueryInstance(sqlQuery); 
	    ResultSet rs = resultMgr.getResult(instance);
	    try {
		while (rs.next()) {
		    hash.put(rs.getString("key"), rs.getString("value"));
		}
	    } catch (SQLException e) {
		    throw e;
	    } finally {
		resultMgr.closeResultSet(rs);
	    }
	}

	return hash;
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected String validateValue(String value) { return null; }



    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer(super.toString() + 
			    "  multiPick='" + multiPick + "'" + newline +
			    "  sql='" + getSqlQuery().getSql() + "'" + newline 
			    );

       return buf.toString();
    }
}
