package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.model.SimpleQueryI;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.implementation.NullQueryInstance;
import org.gusdb.gus.wdk.view.GlobalRepository;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class QueryHolder extends SimpleTagSupport {
    
    private boolean bool;
    private String name;
    private String var;
    private String initQuery;
    private String platformClass;
    private String initCountString;
    private int initCount;
    private String querySet;
    
    public void setBoolean(boolean bool) {
	this.bool = bool;
    }

    public boolean isBoolean() {
	return bool;
    }
    
    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }
    
    public void setVar(String var) {
	this.var = var;
    }

    public String getVar() {
	return var;
    }
    
    public void setInitQuery(String initQuery) {
	this.initQuery = initQuery;
    }

    public String getInitQuery() {
	return initQuery;
    }

    public void setInitCountString(String initCountString) {
	this.initCountString = initCountString;
    }

    public int getInitCount() {
	return initCount;
    }
    
    public void doTag() throws IOException, JspException {
	JspWriter out = getJspContext().getOut();
	out.println("<form>");

	
	SimpleQueryInstanceI sqii = NullQueryInstance.INSTANCE;
	
	if ( initQuery != null) {
		SimpleQuerySet sqs = GlobalRepository.getInstance().getSimpleQuerySet();
		SimpleQueryI sq = sqs.getQuery(initQuery);
		sqii = sq.makeInstance();
	}
	
	if (getJspBody() != null) {
		getJspContext().setAttribute(var, sqii, PageContext.PAGE_SCOPE);
	    getJspBody().invoke(null);
	}
	out.println("</form>");
    }

	/**
	 * @return Returns the querySet.
	 */
	public String getQuerySet() {
		return querySet;
	}
	/**
	 * @param querySet The querySet to set.
	 */
	public void setQuerySet(String querySet) {
		this.querySet = querySet;
	}
}
