package org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews;

import org.gusdb.gus.wdk.model.FlatCVParam;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ResultFactory;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Simple HTML form view on a SqlEnumParam
 */
public class FlatCVParamView implements ParamViewI {
  
    public void showParam(Param param, String formQuery, JspWriter out, PageContext pc) throws IOException {
    	
        FlatCVParam p = (FlatCVParam) param;
    	ResultFactory rf = (ResultFactory) pc.getAttribute("wdk.queryResultFactory", PageContext.APPLICATION_SCOPE);
    	String[] m = null;
    	try {
    		m = p.getVocab();
    	}
    	catch (Exception exp) {
    		// TODO How are we logging?
    	}
    	if (m != null) {
    		out.println("<select name=\""+formQuery+"."+p.getName()+"\">");
    		
    		for (int i = 0; i < m.length ; i++) {
    			String entry = m[i];
    			out.print("<option value=\""+entry+"\">"+entry);
    		}
    		out.println("</select>");
    	}
    }

}
