package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.TreeNode;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.user.User;

/**
 * This class provides functions that are common among EnumParam and FlatVocabParam. The parameter of this
 * type can be rendered in the following ways:
 * <ul>
 * <li>A radio button list, and user can choose only one value from it.</li>
 * <li>A dropdown menu, and user can choose only one value.</li>
 * <li>A checkbox list, and user can choose more than one values.</li>
 * <li>A checkbox tree, and user can choose branches with all the leaves.</li>
 * <li>A type-ahead input box, and when user starts typing, all the matched values will be suggested to the
 * user, and currently only one value is allowed to be chosen from the suggested list.</li>
 * </ul>
 * 
 * Furthermore, such a param can depend on another param, and if the value of that param is changed, the
 * allowed list of values of this enum/flatVocab param will also be changed on the fly. Currently, an
 * enum/flatVocab param can only depend on another enum or flatVocab param.
 * 
 * @author xingao
 * 
 *         The meaning of different param values, based on the processing stage:
 * 
 *         raw value: a String[] of term values;
 * 
 *         stable value: a comma separated list of terms;
 * 
 *         signature: a checksum of the list of terms, ordered alphabetically.
 * 
 *         internal value: a comma separated list of internals. If noTranslation is true, this will be a list
 *         of terms. If quoted is true, quotes are applied to each of the individual items.
 * 
 *         Note about dependent params: AbstractEnumParams can be dependent on other parameter values. Thus,
 *         this class provides two versions of many methods: one that takes a dependent value, and one that
 *         doesn't. If the version is called without a depended value, or the version requiring a depended
 *         value is called with null, yet this param requires a value, the default value of the depended param
 *         is used.
 */
public abstract class AbstractEnumParam extends Param {

  /**
   * @author jerric
   * 
   *         The ParamValueMap is used to eliminate duplicate value tuplets.
   */
  private static class ParamValueMap extends LinkedHashMap<String, String> {

    private static final long serialVersionUID = 8058527840525499401L;

    public ParamValueMap() {
      super();
    }

    public ParamValueMap(Map<? extends String, ? extends String> m) {
      super(m);
    }

    @Override
    public int hashCode() {
      return Utilities.createHashFromValueMap(this);
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
        }
        else
          return false;
      }
      else
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
  private int minSelectedCount = -1;
  private int maxSelectedCount = -1;
  private boolean countOnlyLeaves = false;

  /**
   * this property is only used by abstractEnumParams, but have to be initialized from suggest.
   */
  protected String selectMode;

  /**
   * collapse single-child branches if set to true
   */
  private boolean suppressNode = false;

  protected abstract EnumParamCache createEnumParamCache(Map<String, String> dependedParamValues)
      throws WdkModelException;

  public AbstractEnumParam() {
    super();
    dependedParamRefs = new LinkedHashSet<>();

    // register handlers
    setHandler(new EnumParamHandler());
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
    this.minSelectedCount = param.minSelectedCount;
    this.maxSelectedCount = param.maxSelectedCount;
    this.countOnlyLeaves = param.countOnlyLeaves;
  }

  private EnumParamCache getEnumParamCache(Map<String, String> contextParamValues) {
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
            throw new NoDependedValueException("Attempt made to retrieve values of " +
                dependedParam.getName() + " in dependent param " + getName() +
                " without setting depended value.");
          contextParamValues.put(dependedParam.getName(), dependedParamVal);
        }
      }
      catch (Exception ex) {
        throw new NoDependedValueException(ex);
      }
    }
    try {
      return createEnumParamCache(contextParamValues);
    }
    catch (WdkModelException wme) {
      throw new WdkRuntimeException("Unable to create EnumParamCache for param " + getName() + " with " +
          "depended values " + FormatUtil.prettyPrint(contextParamValues), wme);
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
   * If the quote is true, WDK will escape all the single quotes from internal value, and then wrap those
   * values around with single quotes. then the final value will be substituted into the SQL.
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

  /**
   * @return The minimum number of allowed values for this param; if not set (i.e. no min), this method will
   *         return -1.
   */
  public int getMinSelectedCount() {
    return minSelectedCount;
  }

  /**
   * @param maxSelectedCount
   *          The minimum number of allowed values for this param. If not set, default is "no min"; any number
   *          of values can be assigned.
   */
  public void setMinSelectedCount(int minSelectedCount) {
    this.minSelectedCount = minSelectedCount;
  }

  /**
   * @return The maximum number of allowed values for this param; if not set (i.e. no max), this method will
   *         return -1.
   */
  public int getMaxSelectedCount() {
    return maxSelectedCount;
  }

  /**
   * @param maxSelectedCount
   *          The maximum number of allowed values for this param. If not set, default is "no max"; any number
   *          of values can be assigned.
   */
  public void setMaxSelectedCount(int maxSelectedCount) {
    this.maxSelectedCount = maxSelectedCount;
  }

  /**
   * @return true if, when validating min- and maxSelectedCount (see above), we should only count leaves
   *         towards the total selected value count, or, if false, count both leaves and branch selections
   */
  public boolean getCountOnlyLeaves() {
    return countOnlyLeaves;
  }

  /**
   * @param countOnlyLeaves
   *          Set to true if, when validating min- and maxSelectedCount (see above), we should only count
   *          leaves towards the total selected value count, or set to false if both leaves and branch
   *          selections should be counted
   */
  public void setCountOnlyLeaves(boolean countOnlyLeaves) {
    this.countOnlyLeaves = countOnlyLeaves;
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
      if (contextQuestion != null) {
        params = contextQuestion.getParamMap();
      } else if (contextQuery != null)
        params = contextQuery.getParamMap();
      for (String paramRef : dependedParamRefs) {
        String paramName = paramRef.split("\\.", 2)[1].trim();
        Param param = (params != null) ? params.get(paramName) : (Param) wdkModel.resolveReference(paramRef);
        if (param != null)
          dependedParams.add(param);
        else {
          String message = "Missing depended param: " + paramRef + " for enum param " + getFullName();
          if (contextQuestion != null)
            message += ", in context question " + contextQuestion.getFullName();
          if (contextQuery != null)
            message += ", in context query " + contextQuery.getFullName();
          throw new WdkModelException(message);
        }
      }
    }
    return dependedParams;
  }

  public void setDependedParamRef(String dependedParamRef) {
    this.dependedParamRef = dependedParamRef;
  }

  /**
   * Returns the default value. In the case that this is a dependent param, uses the default value of the
   * depended param as the depended value (recursively).
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
  public String getDefault(Map<String, String> contextParamValues) throws WdkModelException {
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

  public String[] getVocab(Map<String, String> dependedParamValues) throws WdkRuntimeException {
    return getEnumParamCache(dependedParamValues).getVocab();
  }

  public EnumParamTermNode[] getVocabTreeRoots() {
    return getVocabTreeRoots(null);
  }

  public EnumParamTermNode[] getVocabTreeRoots(Map<String, String> dependedParamValues) {
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

  public Map<String, String> getVocabMap(Map<String, String> contextValues) {
    return getEnumParamCache(contextValues).getVocabMap();
  }

  public Map<String, String> getDisplayMap() {
    return getDisplayMap(null);
  }

  public Map<String, String> getDisplayMap(Map<String, String> dependedParamValues) {
    return getEnumParamCache(dependedParamValues).getDisplayMap();
  }

  public Map<String, String> getParentMap() {
    return getParentMap(null);
  }

  public Map<String, String> getParentMap(Map<String, String> dependedParamValues) {
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

  private void suppressChildren(EnumParamCache cache, List<EnumParamTermNode> children) {
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
    }
    else {
      terms = new String[] { termList.trim() };
    }
    return terms;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model .user.User,
   * java.lang.String)
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    if (!isSkipValidation()) {
      String[] terms = (String[]) getRawValue(user, stableValue, contextValues);
      logger.debug("param=" + getFullName() + " - validating: " + stableValue +
          ", with dependedParamValues=" + FormatUtil.prettyPrint(contextValues));

      if (terms.length == 0 && !allowEmpty)
        throw new WdkUserException("The value to enumParam/flatVocabParam " + getPrompt() +
            " cannot be empty");

      // verify that user did not select too few or too many values for this
      // param
      int numSelected = getNumSelected(terms, contextValues);
      if ((maxSelectedCount > 0 && numSelected > maxSelectedCount) ||
          (minSelectedCount > 0 && numSelected < minSelectedCount)) {
        String range = (minSelectedCount > 0 ? "[ " + minSelectedCount : "( Inf") + ", " +
            (maxSelectedCount > 0 ? maxSelectedCount + " ]" : "Inf )");
        throw new WdkUserException("Number of selected values (" + numSelected + ") was not in range " +
            range + " for parameter " + getPrompt());
      }

      Map<String, String> map = getVocabMap(contextValues);
      boolean error = false;
      StringBuilder message = new StringBuilder();
      for (String term : terms) {
        if (!map.containsKey(term)) {
          error = true;
          message.append("Invalid term for param [" + getFullName() + "]: " + term + ". \n");
        }
      }
      if (error)
        throw new WdkUserException(message.toString());
    }
    else {
      logger.debug("param=" + getFullName() + " - skip validation");
    }
  }

  private int getNumSelected(String[] terms, Map<String, String> contextValues) {
    // if countOnlyLeaves is set, must generate original tree, set values, and
    // count the leaves
    String displayType = getDisplayType();
    logger.debug("Checking whether num selected exceeds max on param " + getFullName() + " with values" +
        ": displayType = " + displayType + ", maxSelectedCount = " + getMaxSelectedCount() +
        ", countOnlyLeaves = " + getCountOnlyLeaves());
    if (displayType != null && displayType.equals("treeBox") && getCountOnlyLeaves()) {
      EnumParamTermNode[] rootNodes = getEnumParamCache(contextValues).getVocabTreeRoots();
      TreeNode tree = EnumParamBean.getParamTree(getName(), rootNodes);
      EnumParamBean.populateParamTree(tree, terms);
      return tree.getSelectedLeaves().size();
    }
    // otherwise, just count up terms and compare to max
    return terms.length;
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
    logger.debug("applySelectMode(): select mode: '" + selectMode + "', default from model = " +
        super.getDefault());
    String defaultFromModel = super.getDefault();

    String errorMessage = "The default value from model, '" + defaultFromModel +
        "', is not a valid term for param " + getFullName() + ", please double check this default value.";
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
          }
          else {
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
    }
    else if (selectMode.equalsIgnoreCase(SELECT_MODE_FIRST)) {
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
    selectMode = ((EnumParamSuggestion) suggest).getSelectMode();
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
    super.resolveReferences(wdkModel);

    dependedParamRefs.clear();
    if (dependedParamRef != null && dependedParamRef.trim().length() > 0) {
      for (String paramRef : dependedParamRef.split(",")) {
        // make sure the param exists
        wdkModel.resolveReference(paramRef);

        // make sure the paramRef is unique
        if (dependedParamRefs.contains(paramRef))
          throw new WdkModelException("Duplicate depended param [" + paramRef +
              "] defined in dependent param " + getFullName());
        dependedParamRefs.add(paramRef);
      }
    }

    resolved = true;

    // make sure the depended params exist in the context query.
    if (isDependentParam() && contextQuery != null) {
      Map<String, Param> params = contextQuery.getParamMap();
      Set<Param> dependedParams = getDependedParams();
      for (Param param : dependedParams) {
        if (!params.containsKey(param.getName()))
          throw new WdkModelException("Param " + getFullName() + " depends on param " + param.getFullName() +
              ", but the depended param doesn't exist in the same query " + contextQuery.getFullName());
      }
    }
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
        }
        catch (WdkRuntimeException ex) {
          // if (ex.getMessage().startsWith("No item returned by")) {
          // the enum param doeesn't return any row, ignore it.
          continue;
          // } else
          // throw ex;
        }
      }
    }
    else {
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
        logger.debug(name + " depends on " + dependedParam.getName());
        if (dependedParam instanceof AbstractEnumParam) {
          ((AbstractEnumParam) dependedParam).fetchCorrectValue(user, contextValues, caches);
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
    }
    else { // value exists in context values, check if value is valid
      String stableValue = contextValues.get(name);
      String[] terms = (String[]) getRawValue(user, stableValue, contextValues);
      logger.debug("CORRECTING " + name + "=\"" + stableValue + "\"");
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
      }
      else {
        value = cache.getDefaultValue();
      }
    }
    if (value != null)
      contextValues.put(name, value);
    logger.debug("Corrected " + name + "\"" + contextValues.get(name) + "\"");
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    String[] terms = (String[]) rawValue;
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0)
        buffer.append(", ");
      buffer.append(term);
      if (buffer.length() > truncateLength) {
        buffer.append("...");
        break;
      }
    }
    return buffer.toString();
  }
}
