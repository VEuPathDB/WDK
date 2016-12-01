package org.gusdb.wdk.model.record.attribute;

/**
 * Special case of TextAttributeField that provides UI benefits
 * 
 * @author rdoherty
 */
public class IdAttributeField extends TextAttributeField {

  /**
   * Primary key cannot be removed
   * 
   * @return false
   */
  @Override
  public boolean isRemovable() {
    return false;
  }
}
