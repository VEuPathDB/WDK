package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class Reset extends SimpleTagSupport {
    
    public void doTag() throws IOException {
	getJspContext().getOut().println("<input type=\"reset\">");
    }

}
