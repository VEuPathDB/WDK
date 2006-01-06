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
    
    static final String RESULT_TABLE_MACRO = "%%RESULT_TABLE%%";

    String sql;
    RDBMSPlatformI platform;
    
    public SqlQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public QueryInstance makeInstance() {
	return new SqlQueryInstance(this);
    }

    public void setSql(String sql) {
	this.sql = sql;
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
	return instantiateSql(values, sql);
    }

    /**
     * @param values These values are assumed to be pre-validated
     * @param inputSql Sql to use (may be modified from this.sql)
     */
    protected String instantiateSql(Map values, String inputSql) {
	Iterator keySet = values.keySet().iterator();
	String s = inputSql;
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

    String getSql() {
	return sql;
    }

    RDBMSPlatformI getRDBMSPlatform() {
	return platform;
    }
 }
