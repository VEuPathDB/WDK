package org.gusdb.wdk.view.taglibs.misc;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.gusdb.wdk.model.PrimaryKey;

public class PrimaryKeyHandler extends SimpleTagSupport {
    
    private PrimaryKey primaryKey;
    private String url;
    
    /**
     * @param primaryKey The primaryKey to set.
     */
    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
    /**
     * @param url The url to set.
     */
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void doTag() throws IOException, JspException {
        JspContext context = getJspContext();
        process(context, primaryKey, url);
        return;
    }
  
    public static void process(JspContext context, PrimaryKey primaryKey, String url) throws IOException, JspException {
        JspWriter out = context.getOut();
        String uri = (String) context.findAttribute("wdk_record_url");
        if (uri==null) {
        	out.print(primaryKey);
        } else {
        	out.print("<a href=\"");
        	out.print(uri);
        	out.print("&primaryKey=");
        	out.print(primaryKey);
        	out.print("\">");
        	out.print(primaryKey);
        	out.print("</a>");
        }
    }
    
    
}
