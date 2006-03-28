package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Object used in running a sanity test; represents a query or question in a 
 * wdk model. 
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date: 2004-10-28 13:46:23 -0400 (Thu, 28 Oct 2004) $Author$
 */

public class SanityQuestion extends SanityQueryOrQuestion implements SanityElementI {
    
    int pageStart;
    int pageEnd;

    public SanityQuestion() {
	super("question");
    }

    public void setPageStart(int pageStart) { this.pageStart = pageStart; }
    public void setPageEnd(int pageEnd) { this.pageEnd = pageEnd; }
    public int getPageStart() { return pageStart; }
    public int getPageEnd() { return pageEnd; }

    public String getCommand(String globalArgs) throws WdkModelException{
	String firstPart = "wdkSummary -rows " + pageStart + " " + pageEnd;
	return super.getCommand(globalArgs, firstPart);
    }
}



