/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author xingao
 */
public enum FieldScope {
    ALL          (false, false),
    NON_INTERNAL (true, false),
    REPORT_MAKER (false, true);
    
    private boolean _excludeInternal;
    private boolean _excludeNonReportMaker;
    
    private FieldScope (boolean excludeInternal, boolean excludeNonReportMaker) {
    	_excludeInternal = excludeInternal;
    	_excludeNonReportMaker = excludeNonReportMaker;
    }
    
    public boolean isFieldInScope(ScopedField field) {
    	if (_excludeInternal && field.isInternal()) {
    		return false;
    	}
    	if (_excludeNonReportMaker && !field.isInReportMaker()) {
    		return false;
    	}
    	return true;
    }
}
