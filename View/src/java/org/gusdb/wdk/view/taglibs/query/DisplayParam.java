package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.*;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.SqlEnumParam;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.view.GlobalRepository;

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
    	
    	ResultFactory rf = GlobalRepository.getInstance().getQueryResultFactory();
    	Map m = null;
    	try {
    		m = p.getKeysAndValues(rf);
    	}
    	catch (SQLException exp) {
    		// TODO How are we logging?
    	}
    	if (m != null) {
    		out.println("<select name=\""+p.getName()+"\">");
    		
    		for (Iterator it = m.entrySet().iterator(); it.hasNext(); ) {
    			Map.Entry entry = (Map.Entry) it.next();
    			out.print("<option value=\""+entry.getValue()+"\">"+entry.getKey());
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
