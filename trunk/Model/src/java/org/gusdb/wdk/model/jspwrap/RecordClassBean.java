package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.RecordClass;

import java.util.Map;

/**
 * A wrapper on a {@link RecordClass} that provides simplified access for 
 * consumption by a view
 */ 
public class RecordClassBean {

    RecordClass recordClass;

    public RecordClassBean(RecordClass recordClass) {
	this.recordClass = recordClass;
    }

    public String getFullName() {
	return recordClass.getFullName();
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map getAttributeFields() {
	return recordClass.getAttributeFields();
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map getTableFields() {
	return recordClass.getTableFields();
    }

    /**
     * used by the controller
     */
    public RecordBean makeRecord () {
	return new RecordBean(recordClass.makeRecordInstance());
    }
}
