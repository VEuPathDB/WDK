package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.SqlEnumParam;
import org.gusdb.gus.wdk.model.StringParam;

public class DisplayParam extends SimpleTagSupport {
    
//    private boolean bool;
//    private String name;
//    private String var;
//    private String initQuery;
//    private String platformClass;
//    private String initCountString;
//    private int initCount;

    private Param param;
    
    
    public void doTag() throws IOException, JspException {
    	if (param != null) {
	JspWriter out = getJspContext().getOut();

	if (param instanceof StringParam) {
		handleStringParam((StringParam) param, out);
		return;
	}

	if (param instanceof SqlEnumParam) {
		handlePairParam((SqlEnumParam) param, out);
		return;
	}
	
	//	out.println("<b>Got a param "+param.toString()+"</b>");

//	if (getJspBody() != null) {
//	    getJspBody().invoke(null);
//	}
//	out.println("</form>");
    }
    }

    private void handleStringParam(StringParam p, JspWriter out) throws IOException {
    	String def = p.getDefault();
    	if ( def == null) {
    		def = "";
    	}
    	out.println("<input name=\""+p.getName()+"\" type=\"text\" length=\"8\" value=\""+def+"\">");
    }
  
    private void handlePairParam(SqlEnumParam p, JspWriter out) throws IOException {
    	String def = p.getDefault();
    	if ( def == null) {
    		def = "";
    	}
    	out.println("<input name=\""+p.getName()+"\" type=\"text\" length=\"8\" value=\""+def+"\">");
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
