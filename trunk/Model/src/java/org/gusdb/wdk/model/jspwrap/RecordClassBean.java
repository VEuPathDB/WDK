package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.FieldI;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a Summary that provides simplified access for 
 * consumption by a view
 */ 
public class RecordClassBean {

    RecordClass recordClass;

    public RecordClassBean(RecordClass recordClass) {
	this.recordClass = recordClass;
    }

    /**
     * @returns Map of fieldName --> FieldI
     */
    public Map getAttributeFields() {
	return recordClass.getAttributeFields();
    }

    /**
     * @returns Map of fieldName --> FieldI
     */
    public Map getTableFields() {
	return recordClass.getTableFields();
    }
}
