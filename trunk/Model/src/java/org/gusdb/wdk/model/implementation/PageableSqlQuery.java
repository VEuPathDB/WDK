package org.gusdb.gus.wdk.model.implementation;

import java.util.Map;
import java.util.Iterator;
import javax.sql.DataSource;
import java.sql.SQLException;

import org.gusdb.gus.wdk.model.PageableQueryI;
import org.gusdb.gus.wdk.model.PageableQueryInstanceI;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.SimpleQueryI;
import org.gusdb.gus.wdk.model.Param;

public class PageableSqlQuery extends Query implements PageableQueryI {
    
    public static final String RESULT_TABLE_SYMBOL = "resultTable";
    public static final String START_ROW_SYMBOL = "startRow";
    public static final String END_ROW_SYMBOL = "endRow";
    static String[] syms = {RESULT_TABLE_SYMBOL, START_ROW_SYMBOL,END_ROW_SYMBOL};

    SimpleSqlQuery mainQuery;
    SimpleSqlQuery pageQuery;
    String mainQueryTwoPartName;
    String pageQueryTwoPartName;

    public PageableSqlQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    public void setMainQueryRef(String mainQueryTwoPartName) {
	this.mainQueryTwoPartName = mainQueryTwoPartName;
    }

    public void setPageQueryRef(String pageQueryTwoPartName) {
	this.pageQueryTwoPartName = pageQueryTwoPartName;
    }

    public void resolveReferences(Map querySetMap) throws Exception {
	mainQuery = (SimpleSqlQuery)SimpleQuerySet.resolveReference(querySetMap, 
								    mainQueryTwoPartName, 
								    this.getClass().getName(),
								    getName(),
								    "mainQueryRef");
	pageQuery = (SimpleSqlQuery)SimpleQuerySet.resolveReference(querySetMap, 
								    pageQueryTwoPartName,
								    this.getClass().getName(),
								    getName(),
								    "pageQueryRef");
	checkPageQueryParams();
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
    protected void checkPageQueryParams() {

	Param[] params = pageQuery.getParams();
	String s = "The pageQuery " + pageQuery.getName() + 
	    " contained in PageableSqlQuery " + getName();
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
