package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.RecordList;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.NullQueryInstance;
import org.gusdb.gus.wdk.view.QueryRecordGroupMgr;

import java.io.*;
import java.util.Enumeration;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Custom tag that represents a simple or boolean query 
 */ 
public class QueryHolder extends SimpleTagSupport {
    
	private boolean bool;
	private String name;
	private String var;
	private String initRecordList;
	private String platformClass;
	private String initCountString;
	private int initCount;
	private String recordQueryGroup;
	
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
	
	public void setInitRecordList(String initQuery) {
		this.initRecordList = initQuery;
	}

    public String getInitRecordList() {
        return initRecordList;
    }
    
    public void setInitCountString(String initCountString) {
        this.initCountString = initCountString;
    }
    
    public int getInitCount() {
        return initCount;
    }
    
    public void doTag() throws IOException, JspException {
        JspWriter out = getJspContext().getOut();
        out.println("<form method=\"GET\" action=\"/sampleWDK/InteractiveRecordList\">");
        out.println("<input type=\"hidden\" name=\"formName\" value=\""+name+"\">");
        out.println("<input type=\"hidden\" name=\"queryRecordGroup\" value=\""+recordQueryGroup+"\">");
        
        // Print out any warning/validation error messages
        // They should all start with formName.error.
        Enumeration e = getJspContext().getAttributeNamesInScope(PageContext.REQUEST_SCOPE);
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            //System.err.println("The key is "+key);
            if (key.startsWith(name+".error.")) {
                out.println("<font color=\"red\">");
                out.println(getJspContext().getAttribute(key, PageContext.REQUEST_SCOPE));
                out.println("</font><br>");
            } 
        }
        
        QueryInstance sqii = (QueryInstance) getJspContext().getAttribute(name+".sqii", PageContext.REQUEST_SCOPE);
            
        if (sqii == null) {    
            sqii = NullQueryInstance.INSTANCE;

            if ( initRecordList != null) {
                WdkModel wm = (WdkModel) getJspContext().getAttribute("wdk.wdkModel", PageContext.APPLICATION_SCOPE);

                RecordList rl = QueryRecordGroupMgr.getRecordList(wm, initRecordList);
                Query sq = rl.getQuery();
            	sqii = sq.makeInstance();
            }
        }
        
        if (getJspBody() != null) {
            getJspContext().setAttribute("wdk.formName", name, PageContext.PAGE_SCOPE);
            getJspContext().setAttribute(var, sqii, PageContext.PAGE_SCOPE);
            getJspBody().invoke(null);
            getJspContext().removeAttribute("wdk.formName", PageContext.PAGE_SCOPE);
        }
        out.println("</form>");
    }

	/**
	 * @return Returns the querySet.
	 */
	public String getRecordQueryGroup() {
		return recordQueryGroup;
	}
    
	/**
	 * @param querySet The querySet to set.
	 */
	public void setRecordQueryGroup(String recordGroup) {
		this.recordQueryGroup = recordGroup;
	}
}
