package org.gusdb.gus.wdk.model.query.implementation;

import java.util.Map;
import java.util.Iterator;
import javax.sql.DataSource;
import java.sql.SQLException;

import org.gusdb.gus.wdk.model.query.PageableQueryI;
import org.gusdb.gus.wdk.model.query.PageableQueryInstanceI;
import org.gusdb.gus.wdk.model.query.Param;

public class PageableSqlQuery extends Query implements PageableQueryI {
    
    public static final String RESULT_TABLE_SYMBOL = "resultTable";
    public static final String START_ROW_SYMBOL = "startRow";
    public static final String END_ROW_SYMBOL = "endRow";
    static String[] syms = {RESULT_TABLE_SYMBOL, START_ROW_SYMBOL,END_ROW_SYMBOL};

    SimpleSqlQuery mainQuery;
    SimpleSqlQuery pageQuery;

    public PageableSqlQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setMainQuery(SimpleSqlQuery mainQuery) {
	this.mainQuery = mainQuery;
    }

    public void setPageQuery(SimpleSqlQuery pageQuery) {
	checkPageQueryParams(pageQuery.getParams());
	this.pageQuery = pageQuery;
    }

    public PageableQueryInstanceI makeInstance() {
	return new PageableSqlQueryInstance(mainQuery, pageQuery);
    }

    public String getDisplayName() {
	if (displayName == null) return mainQuery.getDisplayName();
	else return displayName;
    }

    public Boolean getIsCacheable() {
	if (isCacheable == null) return mainQuery.getIsCacheable();
	else return isCacheable;
    }

    public String getHelp() {
	if (help == null) return mainQuery.getHelp();
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
