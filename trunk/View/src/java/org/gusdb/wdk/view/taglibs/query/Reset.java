package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.IOException;

import javax.servlet.jsp.tagext.SimpleTagSupport;

public class Reset extends SimpleTagSupport {
    
    public void doTag() throws IOException {
	getJspContext().getOut().println("<input type=\"reset\">");
    }

}
