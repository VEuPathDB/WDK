/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;

/**
 * @author Jerric
 * @created Jan 23, 2006
 */
public class TableFieldBean extends FieldBean {

	public TableFieldBean(TableField field) {
		super(field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gusdb.wdk.model.TableField#getAttributeFieldMap()
	 */
	public Map<String, AttributeFieldBean> getAttributeFieldMap() {
		AttributeField[] fields = ((TableField)field).getAttributeFields();
		Map<String, AttributeFieldBean> fieldBeans = new LinkedHashMap<String, AttributeFieldBean>(
				fields.length);
		for (AttributeField attributeField : fields) {
			fieldBeans.put(field.getName(), new AttributeFieldBean(
					attributeField));
		}
		return fieldBeans;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gusdb.wdk.model.TableField#getAttributeFields()
	 */
	public AttributeFieldBean[] getAttributeFields() {
		AttributeField[] fields = ((TableField)field).getAttributeFields();
		AttributeFieldBean[] fieldBeans = new AttributeFieldBean[fields.length];
		for (int i = 0; i < fields.length; i++) {
			fieldBeans[i] = new AttributeFieldBean(fields[i]);
		}
		return fieldBeans;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return ((TableField)field).getDescription();
	}

}
