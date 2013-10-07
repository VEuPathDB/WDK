package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.gusdb.fgputil.JavaScript;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

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
  private List<WdkModelText> dependedExpressions;
  private String dependedExpression;
  /**
   * If true, the current enum item will be used as the default value of the
   * param.
   */
  private boolean isDefault = false;

  /**
   * default constructor called by digester
   */
  public EnumItem() {
    dependedExpressions = new ArrayList<>();
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
    this.dependedExpressions = new ArrayList<>(enumItem.dependedExpressions);
    this.dependedExpression = enumItem.dependedExpression;
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
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude depended values. the depended values in the model are defined in
    // a form such as name:value,name:value,name:value
    for (WdkModelText exp : dependedExpressions) {
      if (exp.include(projectId)) {
        if (dependedExpression != null) {
          // the expression has already been set, can have only one.
          throw new WdkModelException("More than one depended expression "
              + "defined for term '" + term + "'");
        }
        dependedExpression = exp.getText();
        // validate the expression
        JavaScript jsLibrary = new JavaScript();
        if (!jsLibrary.isValidBooleanExpression(dependedExpression)) {
          // the expression is invalid
          throw new WdkModelException("The depended expression is invalid: "
              + dependedExpression);
        }
      }
    }
    dependedExpressions.clear();
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
    dependedExpressions.add(dependedValue);
  }

  public String getDependedExpression() {
    return dependedExpression;
  }

  /**
   * check if the given list of depended values are included in the declared
   * depended values.
   * 
   * @param dependedValues
   * @return
   */
  public boolean isValidFor(Map<String, String> dependedParamValues)
      throws WdkModelException {
    // convert the map into a JSON
    try {
      JSONObject jsValues = new JSONObject();
      for(String name : dependedParamValues.keySet()) {
        String values = dependedParamValues.get(name);
        JSONArray array = new JSONArray();
        for (String value : values.split(",")) {
          array.put(value);
        }
        jsValues.put(name, array);
      }

      JavaScript jsLibrary = new JavaScript();
      return jsLibrary.evaluateBooleanExpression(dependedExpression,
          jsValues.toString());
    } catch (ScriptException | JSONException ex) {
      throw new WdkModelException(ex);
    }
  }
}
