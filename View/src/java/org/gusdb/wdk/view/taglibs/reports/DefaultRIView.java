package org.gusdb.gus.wdk.view.taglibs.reports;

import org.gusdb.gus.wdk.model.RecordInstance;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag to provide a default view of a RecordInstance by calling it's toString method
 */
public class DefaultRIView extends SimpleTagSupport {

    public void doTag() throws IOException, JspException {
    	JspWriter out = getJspContext().getOut();
        RecordInstance ri = (RecordInstance) getJspContext().getAttribute("ri");

        out.print("<pre>");
        try {
        	out.print(ri.print());
        }
        catch (Exception exp) {
        	out.print("Exception: "+exp.getMessage());
        }
        out.print("</pre>");

    }

}
