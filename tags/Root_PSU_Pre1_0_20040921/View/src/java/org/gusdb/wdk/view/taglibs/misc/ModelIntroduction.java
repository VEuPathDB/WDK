package org.gusdb.wdk.view.taglibs.misc;

import org.gusdb.wdk.model.WdkModel;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ModelIntroduction extends SimpleTagSupport {

    
    public void doTag() throws IOException, JspException {
        JspWriter out = getJspContext().getOut();
        WdkModel model = (WdkModel) getJspContext().getAttribute("wdk_wdkModel", PageContext.APPLICATION_SCOPE);
        String desc = model.getIntroduction();
        if (desc != null) {
            out.print(desc);
        }
        return;
    }
    
}
