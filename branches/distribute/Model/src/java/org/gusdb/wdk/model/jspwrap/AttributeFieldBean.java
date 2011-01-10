/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.AttributeField;

public class AttributeFieldBean extends FieldBean {

    private AttributeField attributeField;

    /**
     * 
     */
    public AttributeFieldBean(AttributeField field) {
        super(field);
        this.attributeField = field;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AttributeField#isSortable()
     */
    public boolean isSortable() {
        return attributeField.isSortable();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AttributeField#getAlign()
     */
    public String getAlign() {
        return attributeField.getAlign();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getHelp()
     */
    public String getHelp() {
        return attributeField.getHelp();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AttributeField#isRemovable()
     */
    public boolean isRemovable() {
        return attributeField.isRemovable();
    }
}
