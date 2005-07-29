package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;

/**
 * SanityQuery.java
 *
 * Object used in running a sanity test; represents a query in a wdk model.  A Sanity
 * query contains information about the wdk query as well as parameters and restrictions
 * for running the test.  
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: sfischer $
 */

public class SanityQuery implements SanityElementI {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * Name of the wdk query (in querySetName.queryName form) represented
     * by this Sanity Query.
     */
    protected String twoPartName;
    
    /**
     * The parameters that will be used when running this query.  The keys
     * of the hashtable are the names of the parameters and the values are
     * SanityParams representing the parameter itself.
     */
    protected Hashtable params = new Hashtable();

    /**
     * Minimum number of rows the wdk query is expected to return when the
     * test is run.
     */
    protected Integer minOutputLength;

    /**
     * Maximum number of rows the wdk query is expected to return when the
     * test is run.
     */
    protected Integer maxOutputLength;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SanityQuery(){

    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public void setRef(String twoPartName){
	this.twoPartName = twoPartName;
    }
    
    public String getRef(){
	return this.twoPartName;
    }

    public void addParam(SanityParam param){
	params.put(param.getName(), param);
    }
    
    public SanityParam getParam(String paramName) throws WdkUserException{
	SanityParam param = (SanityParam)params.get(paramName);
	if (param == null) throw new WdkUserException("SanityQuery " + getRef() + " does not include param " + paramName);
	return param;
    }

    public boolean hasParam(String paramName){
	if (params.containsKey(paramName)){
	    return true;
	}
	else{
	    return false;
	}
    }

    /**
     * @param return An array of all sanity params provided for this query in the sanity test.
     *               Note that the calling method needs to extract names and values from each
     *               Sanity Param object.
     */
    public SanityParam[] getParams() {
	SanityParam[] allParams = new SanityParam[params.size()];
	Iterator paramIterator = params.values().iterator();
	int i = 0;
	while (paramIterator.hasNext()) {
	    allParams[i++] = (SanityParam)paramIterator.next();
	}
	return allParams;
    }

    /**
     * Convenience method that does extraction of SanityParam names and values
     * for the user instead of requiring them to deal with SanityParam objects.
     *
     * @param return a Hashtable where the keys are the name of each parameter and
     *               the values are the actual values of the parameter. 
     */

    public Hashtable getParamHash(){
	Hashtable paramHash = new Hashtable();
	Enumeration keys = params.keys();
	while (keys.hasMoreElements()){
	    String nextName = (String)keys.nextElement();
	    SanityParam nextParam = (SanityParam)params.get(nextName);
	    paramHash.put(nextName, nextParam.getValue());
	}
	return paramHash;
    }

    public void setMinOutputLength(Integer minOutputLength){
	this.minOutputLength = minOutputLength;
    }
    
    public Integer getMinOutputLength(){
	return this.minOutputLength;
    }

    public void setMaxOutputLength(Integer maxOutputLength){
	this.maxOutputLength = maxOutputLength;
    }
    
    public Integer getMaxOutputLength(){
	return this.maxOutputLength;
    }
    
    public String toString(){
	StringBuffer result = new StringBuffer("SanityQuery twoPartName = " + twoPartName +  " minOutputLength = " + minOutputLength + " maxOutputLength = " + maxOutputLength);
	SanityParam allParams[] = getParams();
	if (allParams != null){
	    for (int i = 0; i < allParams.length; i++){
		result.append("\n\t" + allParams[i].toString());
	    }
	}
	return result.toString();
    }

    // ------------------------------------------------------------------
    // SanityElementI
    // ------------------------------------------------------------------
    public String getCommand(String globalArgs) throws WdkModelException{
	
	SanityParam params[] = getParams();
	
	StringBuffer command = new StringBuffer("wdkQuery " + globalArgs);
	command.append(" -query " + twoPartName );
	if (params != null){
	    command.append(" -params ");
	    for (int i = 0; i < params.length; i++){
		SanityParam nextParam = params[i];
		command.append(nextParam.getName() + " \"" + nextParam.getValue() + "\" ");
	    }
	}
	return command.toString();
    }

    public String getName(){

	return this.twoPartName;
    }

    public String getType(){
	return "query";
    }

}



