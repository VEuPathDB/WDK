package org.gusdb.gus.wdk.view.taglibs.misc;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class RenameAttributeKey extends SimpleTagSupport {
    // TODO Logging, better error checking
    
    private String from;
    private String to;
    private int context = 0;
    private boolean remove = true;
    

    public void setContext(String context) {
    	// TODO Check context is valid and update internal var if so
        
    }

    public void setFrom(String from) {
        this.from = from;
    }
    
    public void setTo(String to) {
        this.to = to;
    }   
    
    
    public void doTag() throws IOException, JspException {       
        JspContext jc = getJspContext();
        int scope = jc.getAttributesScope(from);
        if (scope != 0) {
            jc.setAttribute(to, jc.getAttribute(from, scope),scope);
            if (remove) {
            	jc.removeAttribute(from, scope);   
            }
        }
    }
    
	public void setRemove(String remove) {
        // TODO - Check remove is valid value
		//this.remove = remove;
	}
}
