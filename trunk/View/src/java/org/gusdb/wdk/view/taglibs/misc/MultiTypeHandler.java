package org.gusdb.wdk.view.taglibs.misc;

import org.gusdb.wdk.model.PrimaryKey;
import org.gusdb.wdk.model.RIVList;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class MultiTypeHandler extends SimpleTagSupport {

    private Object o;
    
    
    /**
     * @param o The o to set.
     */
    public void setValue(Object o) {
        this.o = o;
    }
    
    public void doTag() throws IOException, JspException {
        JspWriter out = getJspContext().getOut();
        if (o instanceof RIVList) {
            RIVListHandler.process(getJspContext(), (RIVList)o);
            return;
        }
        
        if (o instanceof PrimaryKey) {
            PrimaryKeyHandler.process(getJspContext(), ((PrimaryKey)o), null);
            return;
        }
        
        if (o instanceof Boolean) {
            BooleanText.process(getJspContext(), ((Boolean)o).toString());
            return;
        }
        if (getJspBody() != null) {
            getJspBody().invoke(null);
        }
    }
    
}
