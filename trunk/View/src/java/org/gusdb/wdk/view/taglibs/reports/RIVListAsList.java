package org.gusdb.gus.wdk.view.taglibs.reports;

import org.gusdb.gus.wdk.controller.WdkLogManager;
import org.gusdb.gus.wdk.view.RIVList;
import org.gusdb.gus.wdk.view.RecordInstanceView;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag to provide a default view of a RecordInstance by calling it's toString method
 */
public class RIVListAsList extends SimpleTagSupport {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.gus.wdk.view.taglibs.reports.RIVListAsList");
    
    private RIVList list;
    private String column;
    
    public void setTq(RIVList list) {
        this.list = list;
    }
    
    /**
     * @param column The column to set.
     */
    public void setColumn(String column) {
        this.column = column;
    }
    
    
    public void doTag() throws IOException, JspException {
    	JspWriter out = getJspContext().getOut();

    	int i=0;
    	logger.severe("About to start writing and i is "+i);
    	while (list.hasNext()) {
    	    RecordInstanceView riv = (RecordInstanceView) list.next();
    	
    	    if (i>0) {
    	        out.print(", ");
    	    }
    	    out.print(riv.get(column));
    	    i++;
    	}
    	logger.severe("About to stop writing and i is "+i);
    }

}
