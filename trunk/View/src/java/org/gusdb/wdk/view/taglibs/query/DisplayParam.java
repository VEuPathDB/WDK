package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.model.FlatCVParam;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.StringParam;

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
    				handleStringParam((StringParam) param, formQuery, out);
    				return;
    			}

    		if (param instanceof FlatCVParam) {
    			handlePairParam((FlatCVParam) param, formQuery, out);
    			return;
    		}
	
	//	out.println("<b>Got a param "+param.toString()+"</b>");

//	if (getJspBody() != null) {
//	    getJspBody().invoke(null);
//	}
//	out.println("</form>");
    	}
    }

    private void handleStringParam(StringParam p, String formQuery, JspWriter out) throws IOException {
    	String def = p.getDefault();
    	if ( def == null) {
    		def = "";
    	}
    	out.println("<input name=\""+formQuery+"."+p.getName()+"\" type=\"text\" length=\"8\" value=\""+def+"\">");
    }
  
    private void handlePairParam(FlatCVParam p, String formQuery, JspWriter out) throws IOException {
    	
    	ResultFactory rf = (ResultFactory) getJspContext().getAttribute("wdk.queryResultFactory", PageContext.APPLICATION_SCOPE);
    	String[] m = null;
    	try {
    		m = p.getVocab();
    	}
    	catch (Exception exp) {
    		// TODO How are we logging?
    	}
    	if (m != null) {
    		out.println("<select name=\""+formQuery+"."+p.getName()+"\">");
    		
    		for (int i = 0; i< m.length ; i++) {
    			String entry = m[i];
    			out.print("<option value=\""+entry+"\">"+entry);
    		}
    		out.println("</select>");
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
