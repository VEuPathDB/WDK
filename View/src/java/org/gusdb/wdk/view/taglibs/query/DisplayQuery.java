package org.gusdb.gus.wdk.view.taglibs.query;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.gusdb.gus.wdk.model.SimpleQueryI;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.implementation.NullQueryInstance;
import org.gusdb.gus.wdk.view.GlobalRepository;

public class DisplayQuery extends SimpleTagSupport {
    
    private SimpleQueryInstanceI queryInstance;
    private String querySet = "RNASimpleQueries"; // FIXME - Should get from QueryHolder
    
    public void setQueryInstance(SimpleQueryInstanceI queryInstance) {
	this.queryInstance = queryInstance;
    }

// TODO Should it pick up other names through NullQuery???
    
    
    public void doTag() throws IOException, JspException {
	JspWriter out = getJspContext().getOut();

	if ( queryInstance instanceof NullQueryInstance) {
	    SimpleQuerySet sqs = GlobalRepository.getInstance().getSimpleQuerySet();
	    SimpleQueryI[] sq = sqs.getQueries();
	    out.println("<b>Queries:</b> <select>");
	    for ( int i=0 ; i < sq.length ;i++) {
		String val = sq[i].getDisplayName();
		out.println("<option value=\""+val+"\">"+val);
	    }
	    out.println("</select>");
	    return;
	} else {
		out.println("<h4>"+queryInstance.getQuery().getDisplayName()+"</h4>");
	}
	if (getJspBody() != null) {
	    getJspBody().invoke(null);
	}
    }

}
