package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * This class represent a display/term/internal tuplet of an enumParam value. An
 * enumParam can have multiple EnumItems, and are grouped in the enumList.
 * 
 * Only term and internal is required, and the term cannot have special
 * characters, such as comma and single quotes in it. If the parentTerm is
 * specified, it will be used to construct a tree display for the enumParam.
 * 
 * If the enumParam is declared to depend on another param, the enumItem should
 * declared a list of depended values in the model, and those depended values
 * should match the terms of the other param.
 * 
 * @author jerric
 * 
 */
public class EnumItem extends WdkModelBase {

  private String display;
  private String term;
  private String internal;
  private String parentTerm;
  private List<String> dependedValues;
  /**
   * If true, the current enum item will be used as the default value of the
   * param.
   */
  private boolean isDefault = false;

  /**
   * default constructor called by digester
   */
  public EnumItem() {
    dependedValues = new ArrayList<String>();
  }

  /**
   * Copy constructor
   * 
   * @param enumItem
   */
  public EnumItem(EnumItem enumItem) {
    this.display = enumItem.display;
    this.term = enumItem.term;
    this.internal = enumItem.internal;
    this.isDefault = enumItem.isDefault;
    this.parentTerm = enumItem.parentTerm;
    this.dependedValues = enumItem.dependedValues;
  }

  /**
   * @return the display
   */
  public String getDisplay() {
    return (display == null) ? term : display;
  }

  /**
   * @param display
   *          the display to set
   */
  public void setDisplay(String display) {
    this.display = display;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  String getTerm() {
    return term;
  }

  public void setInternal(String internal) {
    this.internal = internal;
  }

  String getInternal() {
    return internal;
  }

  /**
   * @return the isDefault
   */
  public boolean isDefault() {
    return this.isDefault;
  }

  /**
   * @param isDefault
   *          the isDefault to set
   */
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
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
    // do nothing.
  }

  /**
   * @return the parentTerm
   */
  public String getParentTerm() {
    return parentTerm;
  }

  /**
   * @param parentTerm
   *          the parentTerm to set
   */
  public void setParentTerm(String parentTerm) {
    this.parentTerm = parentTerm;
  }

  public void addDependedValue(WdkModelText dependedValue) {
    if (!dependedValues.contains(dependedValue.getText())) {
      dependedValues.add(dependedValue.getText());
    }
  }

  public List<String> getDependedValues() {
    return dependedValues;
  }

  /**
   * check if the given list of depended values are included in the declared
   * depended values.
   * 
   * @param dependedValues
   * @return
   */
  public boolean isValidFor(String[] dependedValues) {
    for (String dependedValue : dependedValues) {
      if (this.dependedValues.contains(dependedValue)) {
        return true;
      }
    }
    return false;
  }
}
