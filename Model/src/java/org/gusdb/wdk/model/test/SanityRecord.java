package org.gusdb.gus.wdk.model.test;

import org.gusdb.gus.wdk.model.Reference;
import org.gusdb.gus.wdk.model.WdkModelException;

/**
 * SanityRecord.java
 *
 * Object used in running a sanity test; represents a record in a wdk model.  
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: $
 */


public class SanityRecord implements SanityElementI {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------
    
    /**
     * Name of the wdk record (in recordSetName.recordName format) represented by this
     * SanityRecord.
     */
    protected String twoPartName;
    
    /**
     * Primary key of the element that this record represents.
     */
    protected Integer primaryKey;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SanityRecord(){

    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public void setTwoPartName(String twoPartName){
	this.twoPartName = twoPartName;
    }
    
    public String getTwoPartName(){
	return this.twoPartName;
    }

    public void setPrimaryKey(Integer primaryKey){
	this.primaryKey = primaryKey;
    }
    
    public Integer getPrimaryKey(){
	return this.primaryKey;
    }

    public String toString(){
	return "SanityRecord twoPartName = " + twoPartName + " pk = " + primaryKey;
    }

    // ------------------------------------------------------------------
    // SanityElementI
    // ------------------------------------------------------------------
    
    public String getName(){
	return twoPartName;
    }

    public String getType(){
	return "record";
    }

    public String getCommand(String globalArgs) throws WdkModelException{

	Reference recordReference = new Reference(getTwoPartName());
	String recordSetName = recordReference.getSetName();
	String recordName = recordReference.getElementName();
	String pk = getPrimaryKey().toString();

	StringBuffer command = new StringBuffer ("wdkRecord " + globalArgs);

	command.append(" -recordSetName " + recordSetName + " -recordName " + recordName + " -primaryKey " + pk);
	
	return command.toString();
    }

}
