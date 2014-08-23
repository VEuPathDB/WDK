/**
 * 
 */
package org.gusdb.wdk.model.record.attribute;

import java.util.Map;

import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;

/**
 * An attribute field container is an interface to hold & access a set of
 * {@link AttributeField}s. It can be a {@link RecordClass}, or a {@link TableField} row.
 * 
 * @author xingao
 * 
 */
public interface AttributeFieldContainer {

  public Map<String, AttributeField> getAttributeFieldMap();
}
