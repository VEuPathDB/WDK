package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.RecordInstance;

import java.util.Map;
import java.util.Iterator;
import java.util.Vector;

/**
 * A wrapper on a {@link RecordInstance} that provides simplified access for 
 * consumption by a view
 */ 
public class RecordBean {

    RecordInstance recordInstance;

    public RecordBean(RecordInstance recordInstance) {
	this.recordInstance = recordInstance;
    }

    public String getPrimaryKey() {
	return recordInstance.getPrimaryKey();
    }

    public RecordClassBean getRecordClass() {
	return new RecordClassBean(recordInstance.getRecordClass());
    }

    public String[] getSummaryAttributeNames() {
	Map attribs = getAttributes();
	Iterator ai = attribs.keySet().iterator();
	Vector v = new Vector();
	while (ai.hasNext()) {
	    String attribName = (String)ai.next();
	    if (recordInstance.isSummaryAttribute(attribName)) {
		v.add(attribName);
	    }
	}
	int size = v.size();
	String[] sumAttribNames = new String[size];
	v.copyInto(sumAttribNames);
	return sumAttribNames;
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

    /**
     * used by the controller
     */
    public void assignPrimaryKey(String key) {
	recordInstance.setPrimaryKey(key);
    }
}
