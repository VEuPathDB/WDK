package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.model.FlatVocabParam;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews.FlatVocabParamView;
import org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews.StringParamView;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag that displays the param of a query by delegating to a renderer
 */
public class DisplayParam extends SimpleTagSupport {

    private Param param;
    
    
    public void doTag() throws IOException, JspException {
        if (param != null) {
            JspContext context = getJspContext();   
            JspWriter out = context.getOut();
            
            // TODO Move into a debugging custom tag
//            Enumeration e = context.getAttributeNamesInScope(PageContext.PAGE_SCOPE);
//            while (e.hasMoreElements()) {
//                String key = (String) e.nextElement();
//                //System.err.println("<br>The key is "+key);
//                out.println("<font color=\"red\">Key: "+key+"   Value: ");
//                out.println(context.getAttribute(key, PageContext.PAGE_SCOPE));
//                out.println("</font><br>");
//            } 
            
            
    		String formQuery = context.getAttribute("wdk.formName", PageContext.PAGE_SCOPE)
                + "."
                + (String) context.getAttribute("wdk.queryName", PageContext.PAGE_SCOPE);
                
    		if (param instanceof StringParam) {
                StringParamView.showParam(param, formQuery, out, context);
    		    return;
    		}
// TODO Dynamic
//    		Class parser = Class.forName(parserClass);
//    		Method build = parser.getDeclaredMethod("parseXmlFile", new Class[] {URL.class, URL.class, URL.class});
//    		WdkModel wdkModel = (WdkModel) build.invoke(null, new Object[] {querySetURL, propsURL, schemaURL});
                    
                
    		if (param instanceof FlatVocabParam) {
                FlatVocabParamView.showParam(param, formQuery, out, context);
    			return;
    		}
	
	//	out.println("<b>Got a param "+param.toString()+"</b>");

//	if (getJspBody() != null) {
//	    getJspBody().invoke(null);
//	}
//	out.println("</form>");
    	}
    }
    
    
	/**
	 * @return Returns the param.
	 */
	public Param getParam() {
		return param;
	}
	
	/**
	 * @param param The param to set.
	 */
	public void setParam(Param param) {
		this.param = param;
	}
}
