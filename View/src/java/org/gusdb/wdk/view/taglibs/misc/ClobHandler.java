package org.gusdb.gus.wdk.view.taglibs.misc;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ClobHandler extends SimpleTagSupport {
    
    private Clob cl;
    

    public void setValue(Clob cl) {
        this.cl = cl;
    }

    
    public void doTag() throws IOException, JspException {
        try {
        JspWriter out = getJspContext().getOut();
        out.print(cl.getSubString(1, (int)cl.length()));
        return;
        }
        catch (SQLException exp) {
            throw new JspException(exp);
        }
    }
    
}
