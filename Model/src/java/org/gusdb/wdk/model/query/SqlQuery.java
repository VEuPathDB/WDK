package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

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

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected QueryInstance makeInstance() {
	return new SqlQueryInstance(this);
    }

    protected String instantiateSql(Hashtable values) {
	String s = this.sql;
	Enumeration e = values.keys();
	while (e.hasMoreElements()) {
	    String key = (String)e.nextElement();
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
