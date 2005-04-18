package org.gusdb.wdk.model;

import java.util.Iterator;
import java.util.HashMap;

/**
 * Created: Mon May 10 12:34:30 2004
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class ReferenceList implements ModelSetI {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * The data structure that will hold the actual list.
     */
    private HashMap referenceMap;

    /**
     * The name of this ReferenceList.
     */
    private String name;


    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------


    public ReferenceList(){
       	this.referenceMap = new HashMap();
    }
    
    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public Reference[] getReferences(){

	Reference references[] = null;
	return (Reference[])(referenceMap.values().toArray(references));
    }

    public void addReference(Reference reference) throws WdkModelException {
       	if (referenceMap.get(reference) != null){
	    throw new WdkModelException("Reference '" + reference.getTwoPartName() + "' already exists in ReferenceList '" + getName() + "'");
	}
	
	referenceMap.put(reference, new Integer(1));
    }
  
    public void setName(String name){
	this.name = name;
    }
    
    public String getName(){
	return this.name;
    }

    public Object getElement(String name) {
	return referenceMap.get(name); 
    }

    /**
     * Check to make sure the reference twoPartNames are valid
     */
    public void resolveReferences(WdkModel model) throws WdkModelException {
	Iterator refIter = referenceMap.keySet().iterator();
	while (refIter.hasNext()) {
	    Reference ref = (Reference)refIter.next();
	    model.resolveReference(ref.getTwoPartName(),
				   getName(),
				   "referenceList",
				   "twoPartName");
	}
	
    }

    public void setResources(WdkModel model) throws WdkModelException {
    }
} 
