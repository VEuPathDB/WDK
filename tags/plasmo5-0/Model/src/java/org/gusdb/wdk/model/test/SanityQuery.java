package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;


/**
 * Object used in running a sanity test; represents a query or question in a 
 * wdk model. 
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: sfischer $
 */

public class SanityQuery extends SanityQueryOrQuestion implements SanityElementI {
    public SanityQuery() {
	super("query");
    }

    public String getCommand(String globalArgs) throws WdkModelException{
	return super.getCommand(globalArgs, "wdkQuery");	
    }

}



