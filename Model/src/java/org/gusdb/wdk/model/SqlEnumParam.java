package org.gusdb.gus.wdk.model;

import org.gusdb.gus.wdk.model.implementation.SqlQuery;
import org.gusdb.gus.wdk.model.implementation.SqlQueryInstance;

import java.util.HashMap;
import java.util.Map;

public class SqlEnumParam extends Param {
    
    Boolean multiPick = new Boolean(false);
    SqlQuery sqlQuery;
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

    public void setSqlQuery(SqlQuery sqlQuery) {
	this.sqlQuery = sqlQuery;
    }

    public SqlQuery getSqlQuery() {
	return sqlQuery;
    }

    public Map getKeysAndValues(ResultFactory resultFactory) throws Exception {
	
	if (hash == null) {
	    hash = new HashMap();
	    SqlQueryInstance instance = new SqlQueryInstance(sqlQuery); 
	    ResultList rl = resultFactory.getResult(instance);
	    try {
		while (rl.next()) {
		    hash.put(rl.getValue("key"), rl.getValue("value"));
		}
	    } catch (Exception e) {
		    throw e;
	    } finally {
		rl.close();
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
			    "  sql='" + getSqlQuery().getSql() + "'" + newline 
			    );

       return buf.toString();
    }
}
