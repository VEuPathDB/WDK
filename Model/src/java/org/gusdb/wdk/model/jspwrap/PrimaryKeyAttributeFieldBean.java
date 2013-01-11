package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;

public class PrimaryKeyAttributeFieldBean extends AttributeFieldBean {

	public PrimaryKeyAttributeFieldBean(PrimaryKeyAttributeField field) {
		super(field);
	}

	public PrimaryKeyAttributeField getPrimaryKeyAttributeField() {
		return (PrimaryKeyAttributeField)attributeField;
	}

	public String[] getColumnRefs() {
		return getPrimaryKeyAttributeField().getColumnRefs();
	}
	
}
