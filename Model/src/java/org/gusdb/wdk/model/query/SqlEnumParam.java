package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;
import java.sql.SQLException;
import java.sql.ResultSet;

import org.gusdb.gus.wdk.model.query.implementation.SimpleSqlQuery;
import org.gusdb.gus.wdk.model.query.implementation.SimpleSqlQueryInstance;
import org.gusdb.gus.wdk.model.query.implementation.SqlUtils;
import org.gusdb.gus.wdk.model.query.implementation.SqlResultFactory;

public class SqlEnumParam extends Param {
    
    Boolean multiPick = new Boolean(false);
    SimpleSqlQuery simpleSqlQuery;
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

    public void setSimpleSqlQuery(SimpleSqlQuery simpleSqlQuery) {
	this.simpleSqlQuery = simpleSqlQuery;
    }

    public SimpleSqlQuery getSimpleSqlQuery() {
	return simpleSqlQuery;
    }

    public Hashtable getKeysAndValues(ResultFactory resultFactory) throws SQLException {
	SqlResultFactory sqlResultFactory = resultFactory.getSqlResultFactory();
	if (hash == null) {
	    hash = new Hashtable();
	    SimpleSqlQueryInstance instance = new SimpleSqlQueryInstance(simpleSqlQuery); 
	    ResultSet rs = sqlResultFactory.getResult(instance);
	    try {
		while (rs.next()) {
		    hash.put(rs.getString("key"), rs.getString("value"));
		}
	    } catch (SQLException e) {
		    throw e;
	    } finally {
		SqlUtils.closeResultSet(rs);
	    }
	}

	return hash;
    }

    public String validateValue(String value) { return null; }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer(super.toString() + 
			    "  multiPick='" + multiPick + "'" + newline +
			    "  sql='" + getSimpleSqlQuery().getSql() + "'" + newline 
			    );

       return buf.toString();
    }
}
