package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.IOException;

import javax.servlet.jsp.tagext.SimpleTagSupport;

public class Submit extends SimpleTagSupport {
    
    public void doTag() throws IOException {
//	getJspContext().getOut().println("<input type=\"submit\">");
	getJspContext().getOut().println("Submit");
    }

}
