package org.gusdb.wdk.view.taglibs.misc;

import org.gusdb.wdk.model.RIVList;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag to convert between boolean eg 0/1 into more user-friendly text
 * 
 */
public class BooleanText extends SimpleTagSupport {
    
    private final static String[] pairs = {"Yes", "No", "True", "False", "1", "0"};
    
    private String value;
    private int style = 0;

    
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
    public void setStyle(String styleString) {
        for (int i=0; i < pairs.length; i++) {
            if (pairs[i].equalsIgnoreCase(styleString)) {
                style = i / 2;
                return;
            }   
        }   
    }
    
    
    public void doTag() throws IOException, JspException {
        process(getJspContext(), value, style);
    }

    public static void process(JspContext context, String value) throws IOException, JspException {
        process(context, value, 0);
    }
    
    public static void process(JspContext context, String value, int style) throws IOException, JspException {
        String body = value.trim();
        for (int i=0; i < pairs.length; i++) {
            if (pairs[i].equalsIgnoreCase(body)) {
                JspWriter out = context.getOut();
                out.print(pairs[(style*2)+(i % 2)]);
                return;
            }   
        }
    }
}

