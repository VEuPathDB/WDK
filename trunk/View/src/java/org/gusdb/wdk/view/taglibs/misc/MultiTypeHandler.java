package org.gusdb.wdk.view.taglibs.misc;

import org.gusdb.wdk.view.PrimaryKey;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class MultiTypeHandler extends SimpleTagSupport {
    
    private Object o;

    public void setValue(Object o) {
        this.o = o;
    }

    
    public void doTag() throws IOException, JspException {
        JspWriter out = getJspContext().getOut();
        
        if (o instanceof PrimaryKey) {
            out.write("PrimaryKey");
            return;
        }
        
        out.write(o.toString());
        
    }
    
}
