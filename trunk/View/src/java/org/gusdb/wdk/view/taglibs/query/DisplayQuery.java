package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.implementation.NullQueryInstance;
import org.gusdb.gus.wdk.view.GlobalRepository;

/**
 * Custom tag which displays a Query to the user
 */
public class DisplayQuery extends SimpleTagSupport {
    
    private static final String DEFAULT_OPTION = "Choose...";
    private QueryInstance queryInstance;
    private String querySet = "RNASimpleQueries"; // FIXME - Should get from QueryHolder
    
    public void setQueryInstance(QueryInstance queryInstance) {
        this.queryInstance = queryInstance;
    }

// TODO Should it pick up other names through NullQuery???
    
    
    public void doTag() throws IOException, JspException {
    	JspWriter out = getJspContext().getOut();

    	if ( queryInstance instanceof NullQueryInstance) {
    		QuerySet sqs = GlobalRepository.getInstance().getQuerySet(querySet);
    		Query[] sq = sqs.getQueries();
    		out.println("<b>Queries:</b> <select name=\"queryName\">");
    		out.println("<option value=\""+DEFAULT_OPTION+"\">"+DEFAULT_OPTION);
    		for ( int i=0 ; i < sq.length ;i++) {
    			String val = sq[i].getDisplayName();
    			out.println("<option value=\""+sq[i].getName()+"\">"+val);
    		}
    		out.println("</select>");
    		out.println("<input type=\"hidden\" name=\"defaultChoice\" value=\""+DEFAULT_OPTION+"\">");
            out.println("<input type=\"hidden\" name=\"initialExpansion\" value=\"true\">");
    		return;
    	}
        
    	out.println("<h4>"+queryInstance.getQuery().getDisplayName()+"</h4>");
    	out.println("<input type=\"hidden\" name =\"queryName\" value=\""+queryInstance.getQuery().getName()+"\">");
    	out.println("<input type=\"hidden\" name=\"defaultChoice\" value=\""+DEFAULT_OPTION+"\">");

        if (getJspBody() != null) {
            //out.println("<br>I'm trying to set wdk.queryName to fred<br>");
            getJspContext().setAttribute("wdk.queryName", queryInstance.getQuery().getName(), PageContext.PAGE_SCOPE);
            getJspBody().invoke(null);
            getJspContext().removeAttribute("wdk.queryName", PageContext.PAGE_SCOPE);
    	}
  
    }

}
