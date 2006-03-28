package org.gusdb.wdk.model.test;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * Object used in running a sanity test; represents a query or question in a 
 * wdk model. 
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date: 2004-10-28 13:46:23 -0400 (Thu, 28 Oct 2004) $Author$
 */

public abstract class SanityQueryOrQuestion {

    protected String twoPartName;
    
    /**
     * The parameters that will be used when running this query or question.  The keys
     * of the hashtable are the names of the parameters and the values are
     * SanityParams representing the parameter itself.
     */
    protected Hashtable<String, SanityParam> params = new Hashtable<String, SanityParam>();

    /**
     * Minimum number of rows expected when the test is run.
     */
    protected Integer minOutputLength;

    /**
     * Maximum number of rows expected when the test is run.
     */
    protected Integer maxOutputLength;

    protected String type;   // query or question

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SanityQueryOrQuestion(String type){
	this.type = type;
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
	if (param == null) throw new WdkUserException("Sanity" + getTypeCap() + " " + getRef() + " does not include param " + paramName);
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
     * @param return An array of all sanity params provided in the sanity test.
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

    public Hashtable<String, Object> getParamHash(){
	Hashtable<String, Object> paramHash = new Hashtable<String, Object>();
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
	StringBuffer result = new StringBuffer("Sanity" + getTypeCap() + " twoPartName = " + twoPartName +  " minOutputLength = " + minOutputLength + " maxOutputLength = " + maxOutputLength);
	SanityParam allParams[] = getParams();
	if (allParams != null){
	    for (int i = 0; i < allParams.length; i++){
		result.append("\n\t" + allParams[i].toString());
	    }
	}
	return result.toString();
    }

    public String getTypeCap(){
	return type.replaceAll("q", "Q");
    }
    // ------------------------------------------------------------------
    // SanityElementI
    // ------------------------------------------------------------------

    public abstract String getCommand(String globalArgs) throws WdkModelException;

    protected String getCommand(String globalArgs, String firstPart) throws WdkModelException{
	
	SanityParam params[] = getParams();
	
	StringBuffer command = new StringBuffer(firstPart + " " + globalArgs);
	command.append(" -" + getType() + " " + twoPartName );
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
	return type;
    }

}



