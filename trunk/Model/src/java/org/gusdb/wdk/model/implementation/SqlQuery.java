package org.gusdb.gus.wdk.model.implementation;

import java.util.Iterator;
import java.util.Map;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;

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

    public QueryInstance makeInstance() {
	return new SqlQueryInstance(this);
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

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

    protected String addMultiModeConstraints(String resultTableName, String pkToJoinWith,
					     int startId, int endId, String initSql){

	int whereBegins = initSql.indexOf(" where");
	String firstPartSql = initSql.substring(0, whereBegins);
	String lastPartSql = initSql.substring(whereBegins);
	String extraFromString = ", " + resultTableName;
	String rowStartSql = " and " + resultTableName + ".i >= " + startId;
	String rowEndSql = " and " + resultTableName + ".i <= " + endId;
	String orderBySql = " order by " + resultTableName + "." + "i";
	String finalSql = firstPartSql + extraFromString + lastPartSql + rowStartSql + rowEndSql + orderBySql;
	
	return finalSql;
    }
}
