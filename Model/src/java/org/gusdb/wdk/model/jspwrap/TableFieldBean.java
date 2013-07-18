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

	private TableField field;

	/**
	 * 
	 */
	public TableFieldBean(TableField field) {
		super(field);
		this.field = field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gusdb.wdk.model.TableField#getAttributeFieldMap()
	 */
	public Map<String, AttributeFieldBean> getAttributeFieldMap() {
		AttributeField[] fields = field.getAttributeFields();
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
		AttributeField[] fields = field.getAttributeFields();
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
		return field.getDescription();
	}

}
