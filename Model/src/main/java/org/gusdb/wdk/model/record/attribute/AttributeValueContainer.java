package org.gusdb.wdk.model.record.attribute;

import java.util.Map;

public interface AttributeValueContainer extends AttributeValueMap {

  Map<String, AttributeField> getAttributeFieldMap();

}
