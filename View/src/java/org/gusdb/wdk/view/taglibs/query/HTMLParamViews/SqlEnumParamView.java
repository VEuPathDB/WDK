package org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews;

import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.SqlEnumParam;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Simple HTML form view on a SqlEnumParam
 */
public class SqlEnumParamView implements ParamViewI {
  
    public void showParam(Param param, String formQuery, JspWriter out, PageContext pc) throws IOException {
    	
        SqlEnumParam p = (SqlEnumParam) param;
    	ResultFactory rf = (ResultFactory) pc.getAttribute("wdk.queryResultFactory", PageContext.APPLICATION_SCOPE);
    	Map m = null;
    	try {
    		m = p.getKeysAndValues(rf);
    	}
    	catch (Exception exp) {
    		// TODO How are we logging?
    	}
    	if (m != null) {
    		out.println("<select name=\""+formQuery+"."+p.getName()+"\">");
    		
    		for (Iterator it = m.entrySet().iterator(); it.hasNext(); ) {
    			Map.Entry entry = (Map.Entry) it.next();
    			out.print("<option value=\""+entry.getValue()+"\">"+entry.getKey());
    		}
    		out.println("</select>");
    	}
    }

}
