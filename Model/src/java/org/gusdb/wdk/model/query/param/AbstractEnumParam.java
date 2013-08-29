package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.user.User;

/**
 * This class provides functions that are common among EnumParam and
 * FlatVocabParam. The parameter of this type can be rendered in the following
 * ways:
 * <ul>
 * <li>A radio button list, and user can choose only one value from it.</li>
 * <li>A dropdown menu, and user can choose only one value.</li>
 * <li>A checkbox list, and user can choose more than one values.</li>
 * <li>A checkbox tree, and user can choose branches with all the leaves.</li>
 * <li>A type-ahead input box, and when user starts typing, all the matched
 * values will be suggested to the user, and currently only one value is allowed
 * to be chosen from the suggested list.</li>
 * </ul>
 * 
 * Furthermore, such a param can depend on another param, and if the value of
 * that param is changed, the allowed list of values of this enum/flatVocab
 * param will also be changed on the fly. Currently, an enum/flatVocab param can
 * only depend on another enum or flatVocab param.
 * 
 * @author xingao
 * 
 *         The meaning of different param values, based on the processing stage:
 * 
 *         raw data: a comma separated list of terms;
 * 
 *         user-dependent data: same as user-independent data, can be either a
 *         comma separated list of terms or a compressed checksum;
 * 
 *         user-independent data: same as user-dependent data;
 * 
 *         internal data: a comma separated list of internals, quotes are
 *         properly escaped or added
 * 
 *         Note about dependent params: AbstractEnumParams can be dependent on
 *         other parameter values. Thus, this class provides two versions of
 *         many methods: one that takes a dependent value, and one that doesn't.
 *         If the version is called without a depended value, or the version
 *         requiring a depended value is called with null, yet this param
 *         requires a value, the default value of the depended param is used.
 */
public abstract class AbstractEnumParam extends Param {

  /**
   * @author jerric
   * 
   *         The ParamValueMap is used to eliminate duplicate value tuplets.
   */
  private static class ParamValueMap extends LinkedHashMap<String, String> {

    /**
   * 
   */
    private static final long serialVersionUID = 8058527840525499401L;

    public ParamValueMap() {
      super();
    }

    public ParamValueMap(Map<? extends String, ? extends String> m) {
      super(m);
    }

    @Override
    public int hashCode() {
      return Utilities.print(this).hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ParamValueMap) {
        ParamValueMap map = (ParamValueMap) o;
        if (size() == map.size()) {
          for (String key : keySet()) {
            if (!map.containsKey(key))
              return false;
            if (!map.get(key).equals(get(key)))
              return false;
          }
          return true;
        } else
          return false;
      } else
        return false;
    }
  }

  public static final String DISPLAY_TYPE_AHEAD = "typeAhead";
  public static final String DISPLAY_TREE_BOX = "treeBox";

  static final String SELECT_MODE_NONE = "none";
  static final String SELECT_MODE_ALL = "all";
  static final String SELECT_MODE_FIRST = "first";

  protected boolean multiPick = false;
  protected boolean quote = true;

  private String dependedParamRef;
  private Set<String> dependedParamRefs;
  private Set<Param> dependedParams;
  private String displayType;

  /**
   * this property is only used by abstractEnumParams, but have to be
   * initialized from suggest.
   */
  protected String selectMode;

  /**
   * collapse single-child branches if set to true
   */
  private boolean suppressNode = false;

  public AbstractEnumParam() {
    dependedParamRefs = new LinkedHashSet<>();
  }

  public AbstractEnumParam(AbstractEnumParam param) {
    super(param);
    this.multiPick = param.multiPick;
    this.quote = param.quote;
    this.dependedParamRef = param.dependedParamRef;
    this.dependedParamRefs = new LinkedHashSet<>(param.dependedParamRefs);
    this.displayType = param.displayType;
    this.selectMode = param.selectMode;
    this.suppressNode = param.suppressNode;
  }

  protected abstract EnumParamCache createEnumParamCache(
      Map<String, String> dependedParamValues) throws WdkModelException;

  private EnumParamCache getEnumParamCache(
      Map<String, String> contextParamValues) {
    if (contextParamValues == null)
      contextParamValues = new LinkedHashMap<>();
    if (isDependentParam() && contextParamValues.size() == 0) {
      try {
        for (Param dependedParam : getDependedParams()) {

          String dependedParamVal = contextParamValues.get(dependedParam.getName());
          if (dependedParamVal == null) {
            dependedParamVal = (dependedParam instanceof AbstractEnumParam)
                ? ((AbstractEnumParam) dependedParam).getDefault(contextParamValues)
                : dependedParam.getDefault();
            contextParamValues.put(dependedParam.getName(), dependedParamVal);
          }
          if (dependedParamVal == null)
            throw new NoDependedValueException(
                "Attempt made to retrieve values of " + dependedParam.getName()
                    + " in dependent param " + getName()
                    + " without setting depended value.");
          contextParamValues.put(dependedParam.getName(), dependedParamVal);
        }
      } catch (Exception ex) {
        throw new NoDependedValueException(ex);
      }
    }
    try {
      return createEnumParamCache(contextParamValues);
    } catch (WdkModelException wme) {
      throw new WdkRuntimeException(
          "Unable to create EnumParamCache for param " + getName()
              + " with depended values " + Utilities.print(contextParamValues),
          wme);
    }
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  // used only to initially set this property
  public void setMultiPick(Boolean multiPick) {
    this.multiPick = multiPick.booleanValue();
  }

  public Boolean getMultiPick() {
    return new Boolean(multiPick);
  }

  public boolean isSkipValidation() {
    return (displayType != null && displayType.equals(DISPLAY_TYPE_AHEAD));
  }

  public void setQuote(boolean quote) {
    this.quote = quote;
  }

  /**
   * If the quote is true, WDK will escape all the single quotes from internal
   * value, and then wrap those values around with single quotes. then the final
   * value will be substituted into the SQL.
   * 
   * @return
   */
  public boolean getQuote() {
    return quote;
  }

  /**
   * @return the displayType
   */
  public String getDisplayType() {
    return displayType;
  }

  /**
   * @param displayType
   *          the displayType to set
   */
  public void setDisplayType(String displayType) {
    this.displayType = displayType;
  }

  public boolean isDependentParam() {
    return (dependedParamRefs.size() > 0);
  }

  /**
   * TODO - describe why we get depended param dynamically every time.
   * 
   * @return
   * @throws WdkModelException
   */
  public Set<Param> getDependedParams() throws WdkModelException {
    if (!isDependentParam())
      return null;

    if (!isResolved())
      throw new WdkModelException(
          "This method can't be called before the references for the object are resolved.");

    if (dependedParams == null) {
      dependedParams = new LinkedHashSet<>();
      Map<String, Param> params = null;
      if (contextQuestion != null)
        params = contextQuestion.getParamMap();
      else if (contextQuery != null)
        params = contextQuery.getParamMap();
      for (String paramRef : dependedParamRefs) {
        String paramName = paramRef.split("\\.", 2)[1].trim();
        Param param = (params != null) ? params.get(paramName)
            : (Param) wdkModel.resolveReference(paramRef);
        dependedParams.add(param);
      }
    }
    return dependedParams;
  }

  public void setDependedParamRef(String dependedParamRef) {
    this.dependedParamRef = dependedParamRef;
  }

  /**
   * Returns the default value. In the case that this is a dependent param, uses
   * the default value of the depended param as the depended value
   * (recursively).
   */
  @Override
  public String getDefault() throws WdkModelException {
    return getDefault(new LinkedHashMap<String, String>());
  }

  /**
   * @param contextParamValues
   *          map<paramName, paramValues> of depended params and their values.
   * @return
   * @throws WdkModelException
   */
  public String getDefault(Map<String, String> contextParamValues)
      throws WdkModelException {
    return getEnumParamCache(contextParamValues).getDefaultValue();
  }

  public EnumParamCache getValueCache() {
    return getValueCache(null);
  }

  public EnumParamCache getValueCache(Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues);
  }

  public String[] getVocab() {
    return getVocab(null);
  }

  public String[] getVocab(Map<String, String> dependedParamValues)
      throws WdkRuntimeException {
    return getEnumParamCache(dependedParamValues).getVocab();
  }

  public EnumParamTermNode[] getVocabTreeRoots() {
    return getVocabTreeRoots(null);
  }

  public EnumParamTermNode[] getVocabTreeRoots(
      Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getVocabTreeRoots();
  }

  public String[] getVocabInternal() {
    return getVocabInternal(null);
  }

  public String[] getVocabInternal(Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getVocabInternal();
  }

  public String[] getDisplays() {
    return getDisplays(null);
  }

  public String[] getDisplays(Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getDisplays();
  }

  public Map<String, String> getVocabMap() {
    return getVocabMap(null);
  }

  public Map<String, String> getVocabMap(Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getVocabMap();
  }

  public Map<String, String> getDisplayMap() {
    return getDisplayMap(null);
  }

  public Map<String, String> getDisplayMap(
      Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getDisplayMap();
  }

  public Map<String, String> getParentMap() {
    return getParentMap(null);
  }

  public Map<String, String> getParentMap(
      Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getParentMap();
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  protected void initTreeMap(EnumParamCache cache) {

    // construct index
    Map<String, EnumParamTermNode> indexMap = new LinkedHashMap<String, EnumParamTermNode>();
    for (String term : cache.getTerms()) {
      EnumParamTermNode node = new EnumParamTermNode(term);
      node.setDisplay(cache.getDisplay(term));
      indexMap.put(term, node);

      // check if the node is root
      String parentTerm = cache.getParent(term);
      if (parentTerm != null && !cache.containsTerm(parentTerm))
        parentTerm = null;
      if (parentTerm == null) {
        cache.addParentNodeToTree(node);
        cache.unsetParentTerm(term);
      }
    }
    // construct the relationships
    for (String term : cache.getTerms()) {
      String parentTerm = cache.getParent(term);
      // skip if parent doesn't exist
      if (parentTerm == null)
        continue;

      EnumParamTermNode node = indexMap.get(term);
      EnumParamTermNode parent = indexMap.get(parentTerm);
      parent.addChild(node);
    }

    if (suppressNode)
      suppressChildren(cache, cache.getTermTreeListRef());
  }

  private void suppressChildren(EnumParamCache cache,
      List<EnumParamTermNode> children) {
    boolean suppressed = false;
    if (children.size() == 1) {
      // has only one child, suppress it in the tree if it has
      // grandchildren
      EnumParamTermNode child = children.get(0);
      EnumParamTermNode[] grandChildren = child.getChildren();
      if (grandChildren.length > 0) {
        logger.debug(child.getTerm() + " suppressed.");
        children.remove(0);
        for (EnumParamTermNode grandChild : grandChildren) {
          children.add(grandChild);
        }
        // Also remove the suppressed node from term & internal map.
        // Disable the cache change, to have a correct tree on portal.
        // cache.removeTerm(child.getTerm());

        // need to suppress children
        suppressChildren(cache, children);
        suppressed = true;
      }
    }
    if (!suppressed) {
      for (EnumParamTermNode child : children) {
        suppressChildren(cache, child.getChildrenList());
      }
    }
  }

  public String[] convertToTerms(String termList) {
    // the input is a list of terms
    if (termList == null)
      return new String[0];

    String[] terms;
    if (multiPick) {
      terms = termList.split("[,]+");
      for (int i = 0; i < terms.length; i++) {
        terms[i] = terms[i].trim();
      }
    } else {
      terms = new String[] { termList.trim() };
    }

    // disable the validation - it prevented the revising of invalid step
    // if a strategy has more than one invalid steps.
    // if (!isSkipValidation()) {
    // initVocabMap(dependedParamVal);
    // for (String term : terms) {
    // if (!termInternalMap.containsKey(term))
    // throw new WdkModelException(" - Invalid term '" + term
    // + "' for parameter '" + name + "'");
    // }
    // }
    return terms;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue
   * (org.gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String dependentValueToIndependentValue(User user,
      String dependentValue) throws WdkModelException {
    return dependentValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
   * (org.gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String dependentValueToInternalValue(User user, String dependedValue)
      throws WdkModelException {
    return dependentValueToInternalValue(user, dependedValue, null);
  }

  public String dependentValueToInternalValue(User user, String dependedValue,
      Map<String, String> dependedParamValues) throws WdkModelException {
    EnumParamCache cache = getEnumParamCache(dependedParamValues);

    String rawValue = decompressValue(dependedValue);
    if (rawValue == null || rawValue.length() == 0)
      rawValue = emptyValue;

    String[] terms = convertToTerms(rawValue);
    StringBuffer buf = new StringBuffer();
    for (String term : terms) {
      String internal = (isNoTranslation()) ? term : cache.getInternal(term);
      if (!cache.containsTerm(term)) {
        // doesn't validate term, if it doesn't exist in the list, just
        // use it as internval value. This is for wildcard support in
        // type-ahead params.
        if (isSkipValidation()) {
          internal = term;
        } else {
          // term doesn't exists need to correct it later
          throw new WdkModelException("param " + getFullName()
              + " encountered an invalid term from user #" + user.getUserId()
              + ": " + term);
        }
      }
      if (quote && !(internal.startsWith("'") && internal.endsWith("'")))
        internal = "'" + internal.replaceAll("'", "''") + "'";
      if (buf.length() != 0)
        buf.append(", ");
      buf.append(internal);
    }
    return buf.toString();
  }

  @Override
  public String getInternalValue(User user, String dependentValue)
      throws WdkModelException {
    return getInternalValue(user, dependentValue, null);
  }

  public String getInternalValue(User user, String dependentValue,
      Map<String, String> dependedParamValues) throws WdkModelException {
    String internalValue = dependentValueToInternalValue(user, dependentValue,
        dependedParamValues);
    if (handler != null)
      internalValue = handler.transform(user, internalValue);
    return internalValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.
   * gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String dependentValueToRawValue(User user, String dependentValue)
      throws WdkModelException {
    return decompressValue(dependentValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.
   * gusdb.wdk.model.user.User, java.lang.String)
   */
  @Override
  public String rawOrDependentValueToDependentValue(User user, String rawValue)
      throws WdkModelException {
    return compressValue(rawValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model
   * .user.User, java.lang.String)
   */
  @Override
  protected void validateValue(User user, String userDependentValue,
      Map<String, String> contextValues) throws WdkModelException,
      WdkUserException {
    // handle the empty case
    if (userDependentValue == null || userDependentValue.length() == 0) {
      if (!allowEmpty)
        throw new WdkModelException("The parameter '" + getPrompt()
            + "' does not allow empty value");
      // otherwise, got empty value and is allowed, no need for further
      // validation.
    }

    if (!isSkipValidation()) {
      String rawValue = decompressValue(userDependentValue);
      logger.debug("param=" + getFullName() + " - validating: " + rawValue
          + ", with dependedParamValues=" + Utilities.print(contextValues));

      String[] terms = convertToTerms(rawValue);
      if (terms.length == 0 && !allowEmpty)
        throw new WdkUserException("The value to enumParam/flatVocabParam "
            + getFullName() + " cannot be empty");
      Map<String, String> map = getVocabMap(contextValues);
      boolean error = false;
      StringBuilder message = new StringBuilder();
      for (String term : terms) {
        if (!map.containsKey(term)) {
          error = true;
          message.append("Invalid term for param [" + getFullName() + "]: "
              + term + ". \n");
        }
      }
      if (error)
        throw new WdkUserException(message.toString());
    } else {
      logger.debug("param=" + getFullName() + " - skip validation");
    }
  }

  /**
   * @param selectMode
   *          the selectMode to set
   */
  public void setSelectMode(String selectMode) {
    this.selectMode = selectMode;
  }

  /**
   * @return the selectMode
   */
  public String getSelectMode() {
    return selectMode;
  }

  /**
   * Builds the default value of the "current" enum values
   */
  protected void applySelectMode(EnumParamCache cache) throws WdkModelException {
    logger.debug("applySelectMode(): select mode: '" + selectMode
        + "', default from model = " + super.getDefault());
    String defaultFromModel = super.getDefault();

    String errorMessage = "The default value from model, '" + defaultFromModel
        + "', is not a valid term for param " + getFullName()
        + ", please double check this default value.";
    if (defaultFromModel != null) {
      // default defined in the model, validate default values, and set it
      // to the cache.
      String[] defaults = getMultiPick() ? defaultFromModel.split("\\s*,\\s*")
          : new String[] { defaultFromModel };
      for (String def : defaults) {
        if (!cache.getTerms().contains(def)) {
          // the given default doesn't match any term
          if (isDependentParam()) {
            // need to investigate and make sure the default is as
            // intended.
            // Cannot throws exception here, since the default might
            // not be valid for a different depended value.
            logger.warn(errorMessage);
          } else {
            // param doesn't depend on anything. The default must be
            // wrong.
            logger.warn(errorMessage);
            throw new WdkModelException(errorMessage);

          }
        }
      }
      cache.setDefaultValue(defaultFromModel);
      return;
    }

    // single pick can only select one value
    if (selectMode == null || !multiPick)
      selectMode = SELECT_MODE_FIRST;
    if (selectMode.equalsIgnoreCase(SELECT_MODE_ALL)) {
      StringBuilder builder = new StringBuilder();
      for (String term : cache.getTerms()) {
        if (builder.length() > 0)
          builder.append(",");
        builder.append(term);
      }
      cache.setDefaultValue(builder.toString());
    } else if (selectMode.equalsIgnoreCase(SELECT_MODE_FIRST)) {
      StringBuilder builder = new StringBuilder();
      Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
      if (cache.getTermTreeListRef().size() > 0)
        stack.push(cache.getTermTreeListRef().get(0));
      while (!stack.empty()) {
        EnumParamTermNode node = stack.pop();
        if (builder.length() > 0)
          builder.append(",");
        builder.append(node.getTerm());
        for (EnumParamTermNode child : node.getChildren()) {
          stack.push(child);
        }
      }
      cache.setDefaultValue(builder.toString());
    }
  }

  @Override
  protected void applySuggection(ParamSuggestion suggest) {
    selectMode = suggest.getSelectMode();
  }

  /**
   * @return the suppressNode
   */
  public boolean isSuppressNode() {
    return suppressNode;
  }

  /**
   * @param suppressNode
   *          the suppressNode to set
   */
  public void setSuppressNode(boolean suppressNode) {
    this.suppressNode = suppressNode;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (resolved) return;

    super.resolveReferences(wdkModel);

    dependedParamRefs.clear();
    if (dependedParamRef != null && dependedParamRef.trim().length() > 0) {
      for (String paramRef : dependedParamRef.split(",")) {
        // make sure the param exists
        wdkModel.resolveReference(paramRef);

        // make sure the paramRef is unique
        if (dependedParamRefs.contains(paramRef))
          throw new WdkModelException("Duplicate depended param [" + paramRef
              + "] defined in dependent param " + getFullName());
        dependedParamRefs.add(paramRef);
      }
    }
    resolved = true;
  }

  @Override
  public Set<String> getAllValues() throws WdkModelException {
    Set<String> values = new LinkedHashSet<>();
    if (isDependentParam()) {
      // dependent param, need to get all the combinations of the depended
      // param values.
      Set<Param> dependedParams = getDependedParams();
      Set<ParamValueMap> dependedValues = new LinkedHashSet<>();
      dependedValues.add(new ParamValueMap());
      for (Param dependedParam : dependedParams) {
        Set<String> subValues = dependedParam.getAllValues();
        Set<ParamValueMap> newDependedValues = new LinkedHashSet<>();
        for (String subValue : subValues) {
          for (ParamValueMap dependedValue : dependedValues) {
            dependedValue = new ParamValueMap(dependedValue);
            dependedValue.put(dependedParam.getName(), subValue);
            newDependedValues.add(dependedValue);
          }
        }
        dependedValues = newDependedValues;
      }
      // now for each dependedValue tuplet, get the possible values
      for (Map<String, String> dependedValue : dependedValues) {
        try {
          values.addAll(getVocabMap(dependedValue).keySet());
        } catch (WdkRuntimeException ex) {
          // if (ex.getMessage().startsWith("No item returned by")) {
          // the enum param doeesn't return any row, ignore it.
          continue;
          // } else
          // throw ex;
        }
      }
    } else {
      values.addAll(getVocabMap().keySet());
    }
    return values;
  }

  public void fetchCorrectValue(User user, Map<String, String> contextValues,
      Map<String, EnumParamCache> caches) throws WdkModelException {
      logger.debug("Fixing value " + name + "='" + contextValues.get(name) + "'");

    // make sure the values for depended params are fetched first.
    if (isDependentParam()) {
      for (Param dependedParam : getDependedParams()) {
        logger.debug (name + " depends on " + dependedParam.getName());
        if (dependedParam instanceof AbstractEnumParam) {
          ((AbstractEnumParam) dependedParam).fetchCorrectValue(user,
              contextValues, caches);
        }
      }
    }

    // check if the value for this param is correct
    EnumParamCache cache = caches.get(name);
    if (cache == null) {
      cache = createEnumParamCache(contextValues);
      caches.put(name, cache);
    }
    String value;
    if (!contextValues.containsKey(name)) {
      // value not in context values yet, will use default
      value = cache.getDefaultValue();
    } else { // value exists in context values, check if value is valid
      String paramValue = contextValues.get(name);
      paramValue = dependentValueToRawValue(user, paramValue);
      logger.debug("CORRECTING " + name + "=\"" + paramValue + "\"");
      String[] terms = convertToTerms(paramValue);
      Map<String, String> termMap = cache.getVocabMap();
      Set<String> validValues = new LinkedHashSet<>();
      for (String term : terms) {
        if (termMap.containsKey(term))
          validValues.add(term);
      }

      // if no valid values exist, use default; otherwise, use valid values
      if (validValues.size() > 0) {
        StringBuilder buffer = new StringBuilder();
        for (String term : validValues) {
          if (buffer.length() > 0)
            buffer.append(",");
          buffer.append(term);
        }
        value = buffer.toString();
      } else {
        value = cache.getDefaultValue();
      }
    }
    if (value != null) contextValues.put(name, value);
    logger.debug("Corrected " + name + "\"" + contextValues.get(name) + "\"");
  }

}
