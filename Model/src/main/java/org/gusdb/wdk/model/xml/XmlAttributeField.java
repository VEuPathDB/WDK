package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;

import java.util.Collections;
import java.util.Map;

/**
 * @author Jerric
 */
public class XmlAttributeField extends AttributeField {

    public XmlAttributeField() {
        super();
    }

    @Override
    public String toString() {
        String classnm = this.getClass().getName();
        String buf = classnm
          + ": name='" + getName()
          + "'\r\n  displayName='" + getDisplayName()
          + "'\r\n  help='" + getHelp()
          + "'\r\n";
        return buf;
    }

    @Override
    public void excludeResources(String projectId) {
    // do nothing
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // do nothing
    }

    @Override
    public Map<String, ColumnAttributeField> getColumnAttributeFields() {
      return Collections.emptyMap();
    }
}
