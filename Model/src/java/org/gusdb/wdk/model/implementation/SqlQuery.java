package org.gusdb.wdk.model.implementation;

import java.lang.StringBuffer;
import java.util.Iterator;
import java.util.Map;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;

public class SqlQuery extends Query {
    
    String sql;
    RDBMSPlatformI platform;

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

    public RDBMSPlatformI getRDBMSPlatform() {
	return platform;
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected void setResources(WdkModel model) throws WdkModelException {
	this.platform = model.getRDBMSPlatform();
	super.setResources(model);
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

    protected String addMultiModeConstraints(String resultTableName, String pkValue, int startId, 
					     int endId, String initSql){

	StringBuffer initSqlBuf = new StringBuffer(initSql);
	int whereBegins = initSqlBuf.indexOf(" where");
	if (whereBegins == -1) {
	    whereBegins = initSqlBuf.indexOf(" WHERE");
	}
	
	String firstPartSql = "";
	String lastPartSql = "";
	String rowStartSql = null;
	
	if (whereBegins != -1){   
	    
	    //trim quotes from primary key value if they are there
	    int pkValueStart = initSqlBuf.indexOf(pkValue);
	    int pkValueEnd = pkValueStart + pkValue.length();

	    if (initSqlBuf.charAt(pkValueStart - 1) == '\''){
		initSqlBuf = initSqlBuf.deleteCharAt(pkValueStart -1);
		//initSqlBuf size reduced by 1; delete ' at new position
		initSqlBuf = initSqlBuf.deleteCharAt(pkValueEnd - 1);
	    }
	
	    //join result table name with row number
	    firstPartSql = initSqlBuf.substring(0, whereBegins);	    
	    lastPartSql = initSqlBuf.substring(whereBegins);
	    rowStartSql = " and " + resultTableName + ".i >= " + startId;
	    
	}
	else{  //no where clause
	    firstPartSql = initSqlBuf.toString();
	    rowStartSql = " where " + resultTableName + ".i >= " + startId;
	}

	String extraFromString = ", " + resultTableName;
	String rowEndSql = " and " + resultTableName + ".i <= " + endId;
	String orderBySql = " order by " + resultTableName + "." + "i";
	String finalSql = firstPartSql + extraFromString + lastPartSql + rowStartSql + rowEndSql + orderBySql;
	
	return finalSql;
    }
}
