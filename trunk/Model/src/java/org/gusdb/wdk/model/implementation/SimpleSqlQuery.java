package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.SimpleQueryI;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;

import java.util.Iterator;
import java.util.Map;

public class SimpleSqlQuery extends Query implements SimpleQueryI {
    
    String sql;
    ResultFactory resultFactory;

    public SimpleSqlQuery () {
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

    public SimpleQueryInstanceI makeInstance() {
	return new SimpleSqlQueryInstance(this);
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

    public void setResultFactory(ResultFactory resultFactory) {
	this.resultFactory = resultFactory;
    }
    
    protected SqlResultFactory getSqlResultFactory() {
	return resultFactory.getSqlResultFactory();
    }
}
