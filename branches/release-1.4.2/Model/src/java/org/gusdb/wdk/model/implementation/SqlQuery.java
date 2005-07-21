package org.gusdb.wdk.model.implementation;

import java.lang.StringBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;

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

    /**
     * Added by Jerric - the method handles union queries for federation that
     * used caching table
     * @param resultTableName
     * @param pkValue
     * @param startId
     * @param endId
     * @param initSql
     * @return
     */
    protected String addUnionMultiModeConstraints(String resultTableName,
            String pkValue, int startId, int endId, String initSql) {
        StringBuffer sb = new StringBuffer();
        String subSql;

        String regex = "\\b(union|except|intersect)(\\s+all)?\\b";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(initSql);
        int prev = 0;

        while (match.find()) {
            subSql = initSql.substring(prev, match.start()).trim();
            subSql = addMultiModeConstraints(resultTableName, pkValue, startId,
                    endId, subSql);
            sb.append(subSql.trim());
            sb.append(' ');
            sb.append(match.group());
            sb.append(' ');
            prev = match.end();
        }
        // handle the last part
        subSql = initSql.substring(prev).trim();
        subSql = addMultiModeConstraints(resultTableName, pkValue, startId,
                endId, subSql);
        sb.append(subSql.trim());
        
        // now create an outer query that handles ORDER BY
        String head = "SELECT * FROM ( ";
        String nestedSql = sb.toString().trim();
        String orderBy = " ) ORDER BY " + ResultFactory.MULTI_MODE_I;
        return head + nestedSql + orderBy;
    }

    /**
     * Modified by Jerric
     * @param resultTableName
     * @param pkValue
     * @param startId
     * @param endId
     * @param initSql
     * @return
     */
    protected String addMultiModeConstraints(String resultTableName, String pkValue, int startId, 
					     int endId, String initSql){

	StringBuffer initSqlBuf = new StringBuffer(initSql);

	// check if the query is surrounded by a pair of parenthesis; if so,
	// remove it
	boolean hasParenthesis = false;
	if (initSqlBuf.charAt(0) == '('
            && initSqlBuf.charAt(initSqlBuf.length() - 1) == ')') {
	    hasParenthesis = true;
	    initSqlBuf.deleteCharAt(initSqlBuf.length() - 1);
	    initSqlBuf.deleteCharAt(0);
	}

	String sqlStr = initSqlBuf.toString().toUpperCase();
	int selectStarts = sqlStr.indexOf("SELECT");
	if (selectStarts < 0) {
	    throw new RuntimeException("did not find select in " + sqlStr); 
	}

	int selectEnds = selectStarts + 6;

	String firstPartSql = initSqlBuf.substring(0, selectEnds);
	String lastPartSql = initSqlBuf.substring(selectEnds);
	
	String newSql = firstPartSql + " " + resultTableName + "." 
        + ResultFactory.MULTI_MODE_I + ", " + lastPartSql;
    
	//return addWhereMultiModeConstraints(resultTableName, pkValue, startId, endId, newSql);
    
	newSql = addWhereMultiModeConstraints(resultTableName, pkValue,
					      startId, endId, newSql).trim();

	// add back parenthesis, if removed before
	return (hasParenthesis) ? "(" + newSql + ")" : newSql;
    }

    /**
     * Modified by Jerric
     * @param resultTableName
     * @param pkValue
     * @param startId
     * @param endId
     * @param initSql
     * @return
     */
    protected String addWhereMultiModeConstraints(String resultTableName, String pkValue, int startId, 
					     int endId, String initSql){

	StringBuffer initSqlBuf = new StringBuffer(initSql);
	String sqlStr = initSqlBuf.toString().toUpperCase();
	int whereBegins = sqlStr.lastIndexOf("WHERE");
	
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
	    rowStartSql = " and " + resultTableName + "." + ResultFactory.MULTI_MODE_I + " >= " + startId;
	    
	}
	else{  //no where clause
	    firstPartSql = initSqlBuf.toString();
	    rowStartSql = " where " + resultTableName + "." + ResultFactory.MULTI_MODE_I + " >= " + startId;
	}

	String extraFromString = ", " + resultTableName + " ";
	String rowEndSql = " and " + resultTableName + "." + ResultFactory.MULTI_MODE_I + " <= " + endId;
	String orderBySql = " order by " + resultTableName + "." + ResultFactory.MULTI_MODE_I;
	String finalSql = firstPartSql + extraFromString + lastPartSql + rowStartSql + rowEndSql + orderBySql;
	
	return finalSql;
    }
}
