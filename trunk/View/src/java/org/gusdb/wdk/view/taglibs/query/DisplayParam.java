package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.gusdb.gus.wdk.model.Param;

public class DisplayParam extends SimpleTagSupport {
    
//    private boolean bool;
//    private String name;
//    private String var;
//    private String initQuery;
//    private String platformClass;
//    private String initCountString;
//    private int initCount;

    private Param param;
    
//    public void setBoolean(boolean bool) {
//	this.bool = bool;
//    }
//
//    public boolean isBoolean() {
//	return bool;
//    }
//    
//    public void setName(Param param) {
//	this.name = name;
//    }
//
//    public String getName() {
//	return name;
//    }
//    
//    public void setVar(String var) {
//	this.var = var;
//    }
//
//    public String getVar() {
//	return var;
//    }
//    
//    public void setInitQuery(String initQuery) {
//	this.initQuery = initQuery;
//    }
//
//    public String getInitQuery() {
//	return initQuery;
//    }
//
//    public void setInitCountString(String initCountString) {
//	this.initCountString = initCountString;
//    }
//
//    public int getInitCount() {
//	return initCount;
//    }
    
    public void doTag() throws IOException, JspException {
	JspWriter out = getJspContext().getOut();
	out.println("<form>");

	if (getJspBody() != null) {
	    getJspBody().invoke(null);
	}
	out.println("</form>");
    }

	/**
	 * @return Returns the param.
	 */
	public Param getParam() {
		return param;
	}
	
	/**
	 * @param param The param to set.
	 */
	public void setParam(Param param) {
		this.param = param;
	}
}
