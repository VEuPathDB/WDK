package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * An interface for storing and accessing {@link AttributeValue}s.
 * 
 * @author jerric
 * 
 */
public interface AttributeValueMap {

  public void addAttributeValue(AttributeValue value);

  public AttributeValue getAttributeValue(String key) throws WdkModelException,
      WdkUserException;

}
