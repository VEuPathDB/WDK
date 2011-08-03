package org.gusdb.wdk.model;

public interface ScopedField {

	/**
	 * @return whether or not this field is for internal use only
	 */
    public boolean isInternal();
    
    /**
     * @return whether or not this field can be used to create reports
     */
    public boolean isInReportMaker();
    
}
