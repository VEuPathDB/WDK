package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.RecordInstance;

import java.util.Map;

/**
 * A wrapper on a {@link RecordInstance} that provides simplified access for 
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
     * @return Map of attributeName --> {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map getAttributes() {
	return recordInstance.getAttributes();
    }

    /**
     * @return Map of tableName --> {@link org.gusdb.wdk.model.TableFieldValue}
     */
    public Map getTables() {
	return recordInstance.getTables();
    }

}
