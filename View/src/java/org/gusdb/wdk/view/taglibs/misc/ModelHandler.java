package org.gusdb.wdk.view.taglibs.misc;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.gusdb.wdk.model.WdkModel;

public class ModelHandler extends SimpleTagSupport {

    
    private String var;
    
    public void setVar(String var) {
        this.var = var;
    }
    
    public String getVar() {
        return var;
    }
    
    public void doTag() throws IOException, JspException {
        WdkModel model = (WdkModel) getJspContext().getAttribute("wdk_wdkModel", PageContext.APPLICATION_SCOPE);
        if (getJspBody() != null) {
            getJspContext().setAttribute(var, model, PageContext.PAGE_SCOPE);
            getJspBody().invoke(null);
            getJspContext().removeAttribute(var, PageContext.PAGE_SCOPE);
        }
    }
    
}
