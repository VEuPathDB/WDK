package org.gusdb.wdk.view.taglibs.misc;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class PrimaryKeyHandler extends SimpleTagSupport {
    
    private String primaryKey;
    private String url;
    
    /**
     * @param primaryKey The primaryKey to set.
     */
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
    /**
     * @param url The url to set.
     */
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void doTag() throws IOException, JspException {
        JspWriter out = getJspContext().getOut();
        out.print("<a href=\"");
        out.print(url);
        out.print("&primaryKey=");
        out.print(primaryKey);
        out.print("\">Details</a>");
        return;
    }
    
}
