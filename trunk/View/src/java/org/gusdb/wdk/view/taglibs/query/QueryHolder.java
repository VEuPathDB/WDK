package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.controller.WdkModelExtra;
import org.gusdb.gus.wdk.model.Summary;
import org.gusdb.gus.wdk.model.WdkModel;

import java.io.*;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Custom tag that represents a simple or boolean query 
 */ 
public class QueryHolder extends SimpleTagSupport {

	private String name;
	private String var;
	private String summaryName;
	private String platformClass;
	private String summarySetName;
	
	
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
	
	public void setSummaryName(String initQuery) {
		this.summaryName = initQuery;
	}

    public String getSummaryName() {
        return summaryName;
    }
    
    public void doTag() throws IOException, JspException {
        JspWriter out = getJspContext().getOut();
        
        PageContext pageContext = (PageContext) getJspContext();
        
        HttpServletRequest request =
            (HttpServletRequest) pageContext.getRequest();
        String contextPath = request.getContextPath();

        out.println("<form method=\"GET\" action=\""+contextPath+"/InteractiveRecordList\">");
        out.println("<input type=\"hidden\" name=\"formName\" value=\""+name+"\">");
        out.println("<input type=\"hidden\" name=\"summarySetName\" value=\""+summarySetName+"\">");
        
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
        
        Summary summary = (Summary) getJspContext().getAttribute(name+".summary", PageContext.REQUEST_SCOPE);
            
        if (summary == null) {    
            //sqii = NullQueryInstance.INSTANCE;

            if ( summaryName != null) {
                WdkModel wm = (WdkModel) getJspContext().getAttribute("wdk.wdkModel", PageContext.APPLICATION_SCOPE);

                summary = WdkModelExtra.getSummary(wm, summaryName);
            }
        }
        
        if (getJspBody() != null) {
            getJspContext().setAttribute("wdk.formName", name, PageContext.PAGE_SCOPE);
            getJspContext().setAttribute(var, summary, PageContext.PAGE_SCOPE);
            getJspBody().invoke(null);
            getJspContext().removeAttribute("wdk.formName", PageContext.PAGE_SCOPE);
        }
        out.println("</form>");
    }

	/**
	 * @return Returns the querySet.
	 */
	public String getSummarySetName() {
		return summarySetName;
	}
    
	/**
	 * @param querySet The querySet to set.
	 */
	public void setSummarySetName(String recordGroup) {
		this.summarySetName = recordGroup;
	}
}
