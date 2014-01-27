/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * An object representation of the suggestion tag. The default values can be
 * defined in this tag.
 * 
 * @author Jerric
 */
public class ParamSuggestion extends WdkModelBase {

  private String defaultValue;
  /**
   * The allowEmpty & emptyValue is mainly used by stringParam. default, user
   * has to provide some input to a string param, but if this flag is true, then
   * the input box of a string param can be left empty, and the empty value will
   * be used as user's input.
   */
  private boolean allowEmpty = false;
  private String emptyValue = "";

  /**
   * the default constructor is used by the digester
   */
  public ParamSuggestion() {}

  /**
   * the copy constructor is used by the clone methods
   */
  public ParamSuggestion(ParamSuggestion suggestion) {
    this.defaultValue = suggestion.defaultValue;
    this.allowEmpty = suggestion.allowEmpty;
    this.emptyValue = suggestion.emptyValue;
  }

  /**
   * @return the allowEmpty
   */
  public boolean isAllowEmpty() {
    return this.allowEmpty;
  }

  /**
   * @param allowEmpty
   *          the allowEmpty to set
   */
  public void setAllowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
  }

  /**
   * @return the defaultValue
   */
  public String getDefault() {
    return this.defaultValue;
  }

  /**
   * @param defaultValue
   *          the defaultValue to set
   */
  public void setDefault(String defaultValue) {
    this.defaultValue = (defaultValue.trim().length() == 0) ? null
        : defaultValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
   * .WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // do nothing
  }

  /**
   * @return the emptyValue
   */
  public String getEmptyValue() {
    return emptyValue;
  }

  /**
   * @param emptyValue
   *          the emptyValue to set
   */
  public void setEmptyValue(String emptyValue) {
    this.emptyValue = emptyValue;
  }
}
