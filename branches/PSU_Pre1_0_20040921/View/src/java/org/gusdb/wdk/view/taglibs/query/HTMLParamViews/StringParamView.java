package org.gusdb.wdk.view.taglibs.query.HTMLParamViews;

import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.StringParam;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;

/**
 * Simple HTML form view of s StringParam
 */
public class StringParamView implements ParamViewI {

    public static void showParam(Param param, String formQuery, JspWriter out, JspContext context) throws IOException {
        StringParam p = (StringParam) param;
    	String def = p.getDefault();
    	if ( def == null) {
    		def = "";
    	}
    	out.println("<input name=\""+formQuery+"."+p.getName()+"\" type=\"text\" length=\"8\" value=\""+def+"\" />");
    }

}
