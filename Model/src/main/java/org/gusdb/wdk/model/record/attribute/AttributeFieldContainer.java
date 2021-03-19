package org.gusdb.wdk.model.record.attribute;

import java.util.Map;
import java.util.Optional;

import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;

/**
 * An attribute field container is an interface to hold &amp; access a set of
 * {@link AttributeField}s.  It can be a {@link RecordClass}, or a
 * {@link TableField} row.
 *
 * @author xingao
 */
public interface AttributeFieldContainer {

  Map<String, AttributeField> getAttributeFieldMap();

  Optional<AttributeField> getAttributeField(String key);

  Map<String, Boolean> getSortingAttributeMap();

  String getNameForLogging();

}
