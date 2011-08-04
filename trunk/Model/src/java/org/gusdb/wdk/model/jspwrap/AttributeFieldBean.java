/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.ColumnAttributeField;
import org.gusdb.wdk.model.attribute.plugin.AttributePlugin;

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

    public Map<String, AttributePlugin> getAttributePlugins() {
        if (attributeField instanceof ColumnAttributeField) {
            return ((ColumnAttributeField) attributeField).getAttributePlugins();
        } else {
            return new LinkedHashMap<String, AttributePlugin>();
        }
    }
}
