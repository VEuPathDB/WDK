package org.gusdb.wdk.view.taglibs.misc;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.gusdb.wdk.model.RIVList;




/**
 * Custom tag to convert between boolean eg 0/1 into more user-friendly text
 * 
 */
public class RIVListHandler extends SimpleTagSupport {
	
	private RIVList rivl;
    

    public void setValue(RIVList rivl) {
        this.rivl = rivl;
    }
    
	
    public void doTag() throws IOException, JspException {
        process(getJspContext(), rivl);
    }
    
    public static void process(JspContext context, RIVList rivl) throws IOException, JspException {
        JspWriter out = context.getOut();
        if (rivl == null) {
            out.print("No information available");
            return;
        }
        List columnNames = rivl.getColumnNames();
        Map displayNames = rivl.getDisplayName();
        out.println("<table width=\"100%\" border=\"1\"><tr>");
        for (Iterator it = columnNames.iterator(); it.hasNext();) {
            String columnName = (String) it.next();
            out.print("<th><b>");
            out.print(displayNames.get(columnName));
            out.print("</b></th>");
        }
        out.print("</tr>");
        
        while (rivl.hasNext()) {
            Map row = (Map) rivl.next();
            out.print("<tr>");
            for (Iterator it = columnNames.iterator(); it.hasNext();) {
                String columnName = (String) it.next();
                out.print("<td>");
                out.print(row.get(columnName));
                out.print("</td>");
            }
            out.println("</tr>");
        }
        out.println("</table>");
    }
    
}
