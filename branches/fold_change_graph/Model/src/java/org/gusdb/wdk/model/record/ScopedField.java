package org.gusdb.wdk.model.record;

/**
 * A interface for an attribute or table field in the recordClass, and declares
 * the properties related to the scopes of the fields.
 * 
 * @author jerric
 * 
 */
public interface ScopedField {

  /**
   * @return whether or not this field is for internal use only
   */
  public boolean isInternal();

  /**
   * @return whether or not this field can be used to create reports
   */
  public boolean isInReportMaker();

}
