package org.gusdb.wdk.model.jspwrap;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.TreeNode;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;

/**
 * A wrapper on a {@link AbstractEnumParam} that provides simplified access for
 * consumption by a view.
 * 
 * Note on dependent params: if this is a dependent param and depended param is
 * set, will access values based on that value; otherwise will access values
 * based on the default value of the depended param (i.e. are assuming caller
 * knows what they are doing).
 */
public class EnumParamBean extends ParamBean<AbstractEnumParam> {

  private static final Logger logger = Logger.getLogger(EnumParamBean.class.getName());

  private String[] currentValues;
  private String[] originalValues;

  // if this obj wraps a dependent param, holds depended values
  private Map<String, String> _dependedValues;
  private boolean _dependedValueChanged = false;
  private EnumParamCache _cache;

  public EnumParamBean(AbstractEnumParam param) {
    super(param);
    _dependedValues = new LinkedHashMap<>();
  }

  public Boolean getMultiPick() {
    return param.getMultiPick();
  }

  public boolean getQuote() {
    return param.getQuote();
  }

  public boolean isSkipValidation() {
    return param.isSkipValidation();
  }

  public String getDisplayType() {
    return param.getDisplayType();
  }

  public int getMinSelectedCount() {
    return param.getMinSelectedCount();
  }

  public int getMaxSelectedCount() {
    return param.getMaxSelectedCount();
  }
  
  public Map<String, String> getDependedValues() {
    return _dependedValues;
  }

  public boolean isDependentParam() {
    return param.isDependentParam();
  }

  public void setDependedValues(Map<String, String> dependedValues) {
    if ((_dependedValues == null && dependedValues != null)
        || (_dependedValues != null && !compareValues(_dependedValues,
            dependedValues))) {
      _dependedValues = dependedValues;
      _dependedValueChanged = true;
    }
  }

  private boolean compareValues(Map<String, String> left,
      Map<String, String> right) {
    if (left.size() != right.size())
      return false;
    for (String name : left.keySet()) {
      String value = left.get(name);
      if (!right.containsKey(name))
        return false;
      if (!right.get(name).equals(value))
        return false;
    }
    return true;
  }

  @Override
  public String getDefault() throws WdkModelException {
    return getCache().getDefaultValue();
  }

  // NOTE: not threadsafe! This class is expected only to be used in a single
  // thread
  private EnumParamCache getCache() {
    if (_cache == null || _dependedValueChanged) {
      _cache = param.getValueCache(_dependedValues);
      _dependedValueChanged = false;
    }
    return _cache;
  }

  public String[] getVocabInternal() {
    return getCache().getVocabInternal();
  }

  public String[] getVocab() {
    return getCache().getVocab();
  }

  public Map<String, String> getVocabMap() {
    return getCache().getVocabMap();
  }

  public String[] getDisplays() {
    return getCache().getDisplays();
  }

  public Map<String, String> getDisplayMap() {
    return getCache().getDisplayMap();
  }

  public Map<String, String> getParentMap() {
    return getCache().getParentMap();
  }

  public String getInternalValue(User user, String dependentValue)
      throws WdkModelException {
    return param.getInternalValue(user, dependentValue, _dependedValues);
  }

  public Set<ParamBean<?>> getDependedParams() throws WdkModelException {
    Set<Param> dependedParams = param.getDependedParams();
    if (dependedParams != null) {
      Set<ParamBean<?>> paramBeans = new LinkedHashSet<>();
      for (Param param : dependedParams) {
        paramBeans.add(ParamBeanFactory.createBeanFromParam(user, param));
      }
      return paramBeans;
    }
    return null;
  }

  public String getDependedParamNames() throws WdkModelException {
    Set<Param> dependedParams = param.getDependedParams();
    if (dependedParams == null)
      return null;
    StringBuilder buffer = new StringBuilder();
    for (Param p : dependedParams) {
      if (buffer.length() > 0)
        buffer.append(",");
      buffer.append(p.getName());
    }
    return buffer.toString();
  }

  public EnumParamTermNode[] getVocabTreeRoots() {
    return getCache().getVocabTreeRoots();
  }

  public String[] getTerms(String termList) {
    return param.convertToTerms(termList);
  }

  public String getRawDisplayValue() throws WdkModelException {
    String rawValue = getRawValue();
    if (rawValue == null)
      rawValue = "";
    if (!param.isSkipValidation()) {
      String[] terms = rawValue.split(",");
      Map<String, String> displays = getDisplayMap();
      StringBuffer buffer = new StringBuffer();
      for (String term : terms) {
        if (buffer.length() > 0)
          buffer.append(", ");
        String display = displays.get(term.trim());
        if (display == null)
          display = term;
        buffer.append(display);
      }
      return buffer.toString();
    } else {
      return rawValue;
    }
  }

  /**
   * Sets the currently selected values (as set on a user form) on the bean.
   * 
   * @param currentValues
   *          currently selected values
   */
  public void setCurrentValues(String[] currentValues) {
    this.currentValues = currentValues;
  }
  
  public void setOriginalValues(String[] originalValues) {
    this.originalValues = originalValues;
  }

  /**
   * Returns map where keys are vocab values and values are booleans telling
   * whether each value is currently selected or not.
   * 
   * @return map from value to selection status
   */
  public Map<String, Boolean> getOriginalValues() {
    if (originalValues == null)
      return new LinkedHashMap<String, Boolean>();
    
    Map<String, Boolean> values = new LinkedHashMap<String, Boolean>();
    Map<String, String> terms = getVocabMap();
    // ignore the validation for type-ahead params.
    String displayType = getDisplayType();
    if (displayType == null)
      displayType = "";
    boolean typeAhead = displayType.equals(AbstractEnumParam.DISPLAY_TYPE_AHEAD);
    for (String term : originalValues) {
      boolean valid = typeAhead || terms.containsKey(term);
      values.put(term, valid);
    }
    return values;
  }

  /**
   * Returns a TreeNode containing all values for this tree param, with the
   * "currently selected" values checked
   * 
   * @return up-to-date tree of this param
   */
  public TreeNode getParamTree() {
    EnumParamTermNode[] rootNodes = getVocabTreeRoots();
    TreeNode root = getParamTree(getName(), rootNodes);
    populateParamTree(root, currentValues);
    return root;
  }
  
  public static TreeNode getParamTree(String treeName, EnumParamTermNode[] rootNodes) {
    TreeNode root = new TreeNode(treeName, "top");
    for (EnumParamTermNode paramNode : rootNodes) {
      if (paramNode.getChildren().length == 0) {
        root.addChildNode(new TreeNode(paramNode.getTerm(),
            paramNode.getDisplay(), paramNode.getDisplay()));
      } else {
        root.addChildNode(paramNode.toTreeNode());
      }
    }
    return root;
  }
  
  public static void populateParamTree(TreeNode root, String[] values) {
    if (values != null && values.length > 0) {
      List<String> currentValueList = Arrays.asList(values);
      root.turnOnSelectedLeaves(currentValueList);
      root.setDefaultLeaves(currentValueList);
    }
  }

  /**
   * Temporary method to allow easy on/off of checkbox tree for value selection.
   * 
   * @return whether checkbox tree should be used (columns layout otherwise)
   */
  public boolean getUseCheckboxTree() {
    return true;
  }

  @Override
  public void validate(UserBean user, String rawOrDependentValue,
      Map<String, String> contextValues) throws WdkModelException,
      WdkUserException {
    logger.debug("Validating param=" + getName() + ", value="
        + rawOrDependentValue + ", dependedValue="
        + Utilities.print(_dependedValues));
    param.validate(user.getUser(), rawOrDependentValue, _dependedValues);
  }

  public boolean isSuppressNode() {
    return param.isSuppressNode();
  }

}
