package org.gusdb.gus.wdk.view.taglibs.query;

import org.gusdb.gus.wdk.model.query.QueryI;
import org.gusdb.gus.wdk.model.query.SimpleQueryI;
import org.gusdb.gus.wdk.model.query.NullQuery;
import org.gusdb.gus.wdk.model.query.SimpleQuerySet;
import  org.gusdb.gus.wdk.view.GlobalRepository;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class DisplayQuery extends SimpleTagSupport {
    
    private SimpleQueryInstanceI queryInstance;
    private String querySet = "RNASimpleQueries";
    
    public void setQueryInstance(SimpleQueryInstanceI queryInstance) {
	this.queryInstance = queryInstance;
    }

    public void setQuerySet(String querySet) {
	this.querySet = querySet;
    }

    public void doTag() throws IOException, JspException {
	JspWriter out = getJspContext().getOut();

	if ( queryInstance instanceof NullQueryInstance) {
	    SimpleQuerySet sqs = GlobalRepository.getInstance().getQuerySetContainer().getSimpleQuerySet(querySet);
	    SimpleQueryI[] sq = sqs.getQueries();
	    out.println("<b>Queries:</b> <select>");
	    for ( int i=0 ; i < sq.length ;i++) {
		String val = sq[i].getDisplayName();
		out.println("<option value=\""+val+"\">"+val);
	    }
	    out.println("</select>");
	    return;
	} else {
	    // handle query
	}
	if (getJspBody() != null) {
	    getJspBody().invoke(null);
	}
    }

}
