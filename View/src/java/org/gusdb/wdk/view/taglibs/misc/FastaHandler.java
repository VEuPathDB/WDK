package org.gusdb.gus.wdk.view.taglibs.misc;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag to convert between boolean eg 0/1 into more user-friendly text
 * 
 */
public class FastaHandler extends SimpleTagSupport {

    public void doTag() throws IOException, JspException {       
        if (getJspBody() != null) {
            StringWriter bodyWriter = new StringWriter();
            getJspBody().invoke(bodyWriter);
            String body = bodyWriter.getBuffer().toString().trim();
            int blockCount = 0;
            int count = 0;
            JspWriter out = getJspContext().getOut();
            for (int i=0; i < body.length(); i++) {
                char c = body.charAt(i);
                count++;
                out.print(c);
                if ((count % 10) == 0) {
                    blockCount++;
                    if ((blockCount % 6) == 0) {
                        out.print("<br />");
                    } else {
                        out.print(" ");
                    }
                }
            }   
        }
    }
    
}
