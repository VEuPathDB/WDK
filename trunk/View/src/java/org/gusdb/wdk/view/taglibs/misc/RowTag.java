package org.gusdb.wdk.view.taglibs.misc;

import java.io.IOException;
import javax.servlet.jsp.*;


public class RowTag extends TableTag {

  public boolean ignore = false;

  public RowTag() {
    super();
  }

  public boolean isIgnore() {
    return ignore;
  }

  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }
  
  public int doStartTag() throws JspTagException {
    TableTag parent=(TableTag)findAncestorWithClass(this,TableTag.class);
    if(parent==null) {
      throw new JspTagException("Row Tag cant exist on own must be enclosed in table");
    }
        
    try {
      JspWriter out = pageContext.getOut();
      try {
        if (ignore) {
          out.println("<tr>");
          return EVAL_BODY_INCLUDE;
        }
        // defines the patterning of row colours (- set at alternate)
        else if ((parent.getRowPosition() % 2) > 0) {
          parent.incrementRowPos();
          out.println("<tr class=\"rowLight\">");
    	  return EVAL_BODY_INCLUDE;
        }
        else {
          parent.incrementRowPos();
          out.println("<tr class=\"rowDark\">");
    	  return EVAL_BODY_INCLUDE;
        }
      }
      catch (IOException ie) {
      	return SKIP_BODY;
      }
    }
    catch(NullPointerException e) {
      return SKIP_BODY;
    }
  }
        
  public int doEndTag() throws JspException {
    JspWriter out = pageContext.getOut();
    try {
      out.println("</tr>");
    }
    catch (IOException ie){
        // FIXME
  	}
    return EVAL_PAGE;
  }
}	 
	 
	    
	    
	 
	 
