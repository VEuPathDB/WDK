package org.gusdb.gus.wdk.view.taglibs.misc;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BodyEvaluator extends SimpleTagSupport {

    
    public void doTag() throws IOException, JspException {
        getJspBody().invoke(null);
    }
    
}
