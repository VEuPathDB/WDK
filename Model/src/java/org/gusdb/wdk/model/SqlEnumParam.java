package org.gusdb.gus.wdk.model;

import org.gusdb.gus.wdk.model.implementation.SimpleSqlQuery;
import org.gusdb.gus.wdk.model.implementation.SimpleSqlQueryInstance;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;
import org.gusdb.gus.wdk.model.implementation.SqlUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SqlEnumParam extends Param {
    
    Boolean multiPick = new Boolean(false);
    SimpleSqlQuery simpleSqlQuery;
    Map hash;

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

    public Map getKeysAndValues(ResultFactory resultFactory) throws SQLException {
	SqlResultFactory sqlResultFactory = resultFactory.getSqlResultFactory();
	if (hash == null) {
	    hash = new HashMap();
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
