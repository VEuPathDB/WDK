package org.gusdb.gus.wdk.model;


/**
 * A JavaBean wrapper around a string
 */
public class Reference {

    String referent;

    public Reference() {}

    public void setReferent(String referent) {
	this.referent = referent;
    }
    
    public String getReferent() {
	return referent;
    }
}
