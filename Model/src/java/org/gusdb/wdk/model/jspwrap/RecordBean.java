package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.FieldValueMap;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a Summary that provides simplified access for 
 * consumption by a view
 */ 
public class RecordBean {

    RecordInstance recordInstance;

    public RecordBean(RecordInstance record) {
	this.recordInstance = recordInstance;
    }

    public String getPrimaryKey() {
	return recordInstance.getPrimaryKey();
    }

    /**
     * @return Map of attributeName --> AttributeFieldValue
     */
    public Map getAttributes() {
	return recordInstance.getAttributes();
    }

    /**
     * @return Map of tableName --> TableFieldValue
     */
    public Map getTables() {
	return recordInstance.getTables();
    }

}
