package org.gusdb.wdk.view.taglibs.query;


import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag that simply wraps the HTML submit tag
 */
public class Submit extends SimpleTagSupport {
    
    public void doTag() throws IOException,JspException {
        JspWriter out = getJspContext().getOut();
        out.println("<input type=\"submit\" name=\"submit\" value=\"");
        getJspBody().invoke(null);
        out.println("\">");
    }

}
