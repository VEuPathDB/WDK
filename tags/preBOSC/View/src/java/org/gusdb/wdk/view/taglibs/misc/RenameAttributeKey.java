package org.gusdb.gus.wdk.view.taglibs.misc;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Rename (copy) an attribute from one key to another 
 */
public class RenameAttributeKey extends SimpleTagSupport {
    // TODO Logging, better error checking
    
    private String from;
    private String to;
    private boolean rm = true;


    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }   

    public void setRemove(String remove) {
        this.rm = Boolean.getBoolean(remove);
    }
    
    public void doTag() throws IOException, JspException {       
        JspContext jc = getJspContext();
        int scope = jc.getAttributesScope(from);
        if (scope != 0) {
            jc.setAttribute(to, jc.getAttribute(from, scope),scope);
            if (rm) {
            	jc.removeAttribute(from, scope);   
            }
        }
    }


}
