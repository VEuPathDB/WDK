package org.gusdb.wdk.model.test;

/**
 * SanityParam.java
 *
 * Represents a parameter name and value that will be used in conjunction with
 * a SanityQuery to run a query as part of a sanity test.
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date: 2004-09-09 10:32:19 -0400 (Thu, 09 Sep 2004) $Author$
 */
public class SanityParam {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * Name of the parameter.
     */
    protected String name;

    /**
     * Value of the parameter.
     */ 
    protected String value;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SanityParam(){

    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public String getName(){
	return this.name;
    }
    
    public void setName(String name){
	this.name = name;
    }

    public String getValue(){
	return this.value;
    }
    
    public void setValue(String value){
	this.value = value;
    }

    public String toString(){
	return "Param name = " + name + " value = " + value;
    }
}
