package org.gusdb.gus.wdk.model.query.implementation;

import java.util.Map;
import java.util.Iterator;
import javax.sql.DataSource;
import java.sql.SQLException;

import org.gusdb.gus.wdk.model.query.PageableQueryI;
import org.gusdb.gus.wdk.model.query.PageableQueryInstanceI;
import org.gusdb.gus.wdk.model.query.SimpleQuerySet;
import org.gusdb.gus.wdk.model.query.SimpleQueryI;
import org.gusdb.gus.wdk.model.query.Param;

public class PageableSqlQuery extends Query implements PageableQueryI {
    
    public static final String RESULT_TABLE_SYMBOL = "resultTable";
    public static final String START_ROW_SYMBOL = "startRow";
    public static final String END_ROW_SYMBOL = "endRow";
    static String[] syms = {RESULT_TABLE_SYMBOL, START_ROW_SYMBOL,END_ROW_SYMBOL};

    SimpleSqlQuery mainQuery;
    SimpleSqlQuery pageQuery;
    String mainQueryName;
    String pageQueryName;

    public PageableSqlQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    public void setMainQueryRef(String mainQueryName) {
	this.mainQueryName = mainQueryName;
    }

    public void setPageQueryRef(String pageQueryName) {
	this.pageQueryName = pageQueryName;
    }

    public void dereference(Map querySetMap) throws Exception {
	mainQuery = (SimpleSqlQuery)dereference(querySetMap, mainQueryName, 
						"mainQueryRef");
	pageQuery = (SimpleSqlQuery)dereference(querySetMap, pageQueryName,
						"pageQueryRef");
	checkPageQueryParams(pageQuery.getParams());
    }

    SimpleQueryI dereference(Map querySetMap, String twoPartName, String part) throws Exception {
	String s = "PageableSqlQuery '" + getName() + "' has a " + part;

	if (!twoPartName.matches("\\w+\\.\\w+")) {
	    String s2 = s + " which is not in the form 'simpleQuerySetName.simpleQueryName'";
	    throw new Exception(s2);
	}

	String[] parts = twoPartName.split("\\.");
	String querySetName = parts[0];
	String queryName = parts[1];

	SimpleQuerySet sqs = (SimpleQuerySet)querySetMap.get(parts[0]);
	if (sqs == null) {
	    String s3 = s + " which contains an unrecognized querySet '" 
		+ querySetName + "'";
	    throw new Exception(s3);
	}
	SimpleQueryI sq = sqs.getQuery(parts[1]);
	if (sq == null) {
	    String s4 = s + " which contains an unrecognized query '" 
		+ queryName + "'";
	    throw new Exception(s4);
	}
	return sq;
    }

    public PageableQueryInstanceI makeInstance() {
	return new PageableSqlQueryInstance(mainQuery, pageQuery);
    }

    public String getDisplayName() {
	if (displayName == null && mainQuery != null) 
	    return mainQuery.getDisplayName();
	else return displayName;
    }

    public Boolean getIsCacheable() {
	if (isCacheable == null && mainQuery != null) 
	    return mainQuery.getIsCacheable();
	else return isCacheable;
    }

    public String getHelp() {
	if (help == null && mainQuery != null) 
	    return mainQuery.getHelp();
	else return help;
    }

    public Param[] getParams() {
	return mainQuery.getParams();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    protected void checkPageQueryParams(Param[] params) {

	String s = "The pageQuery " + pageQuery.getName() + 
	    " contained by PageableSqlQuery " + getName();
	if (params.length != syms.length) 
	    throw new IllegalArgumentException(s + " must have " + syms.length + " params");

	for (int i=0; i<syms.length; i++) {
	    boolean found = false;
	    for (int j=0; j<params.length; j++) {
		found |= syms[i].equals(params[j].getName());
	    }
	    if (!found) throw new IllegalArgumentException(s + " must contain a parameter of name " + syms[i]);
	}
    }

}
