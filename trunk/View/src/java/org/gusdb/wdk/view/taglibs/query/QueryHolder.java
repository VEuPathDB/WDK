package org.gusdb.gus.wdk.view.taglibs.query;

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

	if (getJspBody() != null) {
	    getJspBody().invoke(null);
	}
	out.println("</form>");
    }

}
