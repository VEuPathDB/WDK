package org.gusdb.gus.wdk.view.taglibs.misc;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BooleanText extends SimpleTagSupport {
    
    private final static String[] pairs = {"Yes", "No", "True", "False", "1", "0"};
    
    private int style = 0;
    

    public void setStyle(String styleString) {
        for (int i=0; i < pairs.length; i++) {
            if (pairs[i].equalsIgnoreCase(styleString)) {
                style = i / 2;
                return;
            }   
        }   
    }

    
    public void doTag() throws IOException, JspException {       
        if (getJspBody() != null) {
            JspFragment jspf = getJspBody();
            StringWriter bodyWriter = new StringWriter();
            getJspBody().invoke(bodyWriter);
            String body = bodyWriter.getBuffer().toString().trim();
            for (int i=0; i < pairs.length; i++) {
                if (pairs[i].equalsIgnoreCase(body)) {
                    JspWriter out = getJspContext().getOut();
                    out.print(pairs[(style*2)+(i % 2)]);
                    return;
                }   
            }   
        }
    }
    
}
