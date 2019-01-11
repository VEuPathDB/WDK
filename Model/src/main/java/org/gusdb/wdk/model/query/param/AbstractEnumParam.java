package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.FieldTree;
import org.gusdb.wdk.model.SelectableItem;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public abstract class AbstractEnumParam extends AbstractDependentParam {

  private static final Logger LOG = Logger.getLogger(AbstractEnumParam.class);

  private static final boolean INVALID_DEFAULT_IS_FATAL = true;

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

  public static final String DISPLAY_SELECT = "select";
  public static final String DISPLAY_LISTBOX = "listBox"; // deprecated; use select
  public static final String DISPLAY_CHECKBOX = "checkBox";
  public static final String DISPLAY_RADIO = "radioBox"; // deprecated; use checkBox
  public static final String DISPLAY_TREEBOX = "treeBox";
  public static final String DISPLAY_TYPEAHEAD = "typeAhead";

  protected Boolean _multiPick = false;
  protected boolean _quote = true;

  private String _displayType = null;
  private int _minSelectedCount = -1;
  private int maxSelectedCount = -1;
  private boolean countOnlyLeaves = true;

  /**
   * this property is only used by abstractEnumParams, but have to be initialized from suggest.
   * it is an enum with values: NONE, ALL, FIRST
   */
  protected SelectMode selectMode;

  /**
   * collapse single-child branches if set to true
   */
  private boolean suppressNode = false;
  
  private boolean skipValidation = false;

  private int depthExpanded = 0;

  protected abstract EnumParamVocabInstance createVocabInstance(User user, Map<String, String> dependedParamValues)
      throws WdkModelException, WdkUserException;
  
  @Override
  protected DependentParamInstance createDependentParamInstance (User user, Map<String, String> dependedParamValues)
      throws WdkModelException, WdkUserException {
    return createVocabInstance(user,  dependedParamValues);
  }

  public AbstractEnumParam() {
    super();

    // register handlers
    setHandler(new EnumParamHandler());
  }

  public AbstractEnumParam(AbstractEnumParam param) {
    super(param);
    this._multiPick = param._multiPick;
    this._quote = param._quote;
    this._displayType = param._displayType;
    this.selectMode = param.selectMode;
    this.suppressNode = param.suppressNode;
    this._minSelectedCount = param._minSelectedCount;
    this.maxSelectedCount = param.maxSelectedCount;
    this.countOnlyLeaves = param.countOnlyLeaves;
    this.depthExpanded = param.depthExpanded;
  }

  /**
   * Provides a vocabulary instance for this param, using contextParamValues to control depended values, if
   * any. Along the way, ensure that contextParamValues contains values for all depended params (either those
   * originally provided, or defaults). As a side-effect, depended param vocabularies are built (based on
   * values in the context)
   * 
   * @param user
   * @param contextParamValues
   * @return
   */
  public EnumParamVocabInstance getVocabInstance(User user, Map<String, String> contextParamValues) {

    // make sure context is populated with values we need (our depended params)
    contextParamValues = ensureRequiredContext(user, contextParamValues);
    
    // now create the vocab instance, using that context
    try {
      return createVocabInstance(user, contextParamValues);
    }
    catch (WdkModelException | WdkUserException wme) {
      throw new WdkRuntimeException("Unable to create EnumParamVocabInstance for param " + getName() + " with " +
          "depended values " + FormatUtil.prettyPrint(contextParamValues), wme);
    }
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  // used only to initially set this property
  public void setMultiPick(boolean multiPick) {
    this._multiPick = multiPick;
  }

  public boolean getMultiPick() {
    return _multiPick;
  }

  public boolean isSkipValidation() {
    return skipValidation;
  }
  
  public void setSkipValidation(boolean skipValidation) {
    this.skipValidation = skipValidation;
  }

  public void setQuote(boolean quote) {
    this._quote = quote;
  }

  /**
   * If the quote is true, WDK will escape all the single quotes from internal value, and then wrap those
   * values around with single quotes. then the final value will be substituted into the SQL.
   * 
   * @return
   */
  public boolean getQuote() {
    return _quote;
  }

  /**
   * Returns display type configured if set.  Otherwise returns default, which
   * is checkbox if multi-pick, select if single-pick
   * 
   * @return the displayType of this param
   */
  public String getDisplayType() {
    return (_displayType != null ? _displayType :
      (getMultiPick() ? DISPLAY_CHECKBOX : DISPLAY_SELECT));
  }

  /**
   * @param displayType
   *          the displayType to set
   */
  public void setDisplayType(String displayType) {
    String paramName = getFullName();
    if (paramName == null) paramName = "<name_not_yet_set>";
    if (displayType.equals(DISPLAY_RADIO)) {
      LOG.warn("Param ['" + paramName + "']: displayType '" + DISPLAY_RADIO +
          "' is deprecated.  Please use '" + DISPLAY_CHECKBOX + "' instead.");
      displayType = DISPLAY_CHECKBOX;
    }
    else if (displayType.equals(DISPLAY_LISTBOX)) {
      LOG.warn("Param ['" + paramName + "']: displayType '" + DISPLAY_LISTBOX +
          "' is deprecated.  Please use '" + DISPLAY_SELECT + "' instead.");
      displayType = DISPLAY_SELECT;
    }
    this._displayType = displayType;
  }

  /**
   * @return The minimum number of allowed values for this param; if not set (i.e. no min), this method will
   *         return -1.
   */
  public int getMinSelectedCount() {
    return _minSelectedCount;
  }

  /**
   * @param maxSelectedCount
   *          The minimum number of allowed values for this param. If not set, default is "no min"; any number
   *          of values can be assigned.
   */
  public void setMinSelectedCount(int minSelectedCount) {
    this._minSelectedCount = minSelectedCount;
  }

  /**
   * @return The maximum number of allowed values for this param; if not set (i.e. no max), this method will
   *         return -1.
   */
  public int getMaxSelectedCount() {
    // only allow one value if multiPick set to false
    if (!getMultiPick())
      return 1;
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
  
  public void setDepthExpanded(int depthExpanded) {
    this.depthExpanded = depthExpanded;
  }
  
  public int getDepthExpanded() {
    return this.depthExpanded;
  }

 
  /**
   * Returns the default value. In the case that this is a dependent param, uses the default value of the
   * depended param as the depended value (recursively).
   */
  @Override
  public String getDefault() throws WdkModelException {
    return getDefault(_wdkModel.getSystemUser(), new LinkedHashMap<String, String>());
  }

  /**
   * @param contextParamValues
   *          map<paramName, paramValues> of depended params and their values.
   * @return
   * @throws WdkModelException
   */
  @Override
  public String getDefault(User user, Map<String, String> contextParamValues) throws WdkModelException {
    if (isDependentParam() && !contextParamValues.isEmpty()) {
      LOG.debug("Default value requested for param " + getName() + " with context values " +
          FormatUtil.prettyPrint(contextParamValues, Style.SINGLE_LINE));
      String value = getVocabInstance(user, contextParamValues).getDefaultValue();
      LOG.debug("Returning default value of '" + value + "' for dependent param " + getName());
      return value;
    }
    else {
      return getVocabInstance(user, contextParamValues).getDefaultValue();
    }
  }

  @Override
  public String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) {
    return getVocabInstance(user, contextParamValues).getSanityDefaultValue(sanitySelectMode, getMultiPick(), getSanityDefault());
  }

  public EnumParamVocabInstance getVocabInstance(User user) {
    return getVocabInstance(user, null);
  }

  public String[] getVocab(User user) {
    return getVocab(user, null);
  }

  public String[] getVocab(User user, Map<String, String> dependedParamValues) throws WdkRuntimeException {
    return getVocabInstance(user, dependedParamValues).getVocab();
  }

  public EnumParamTermNode[] getVocabTreeRoots(User user) {
    return getVocabTreeRoots(user, null);
  }

  public EnumParamTermNode[] getVocabTreeRoots(User user, Map<String, String> dependedParamValues) {
    return getVocabInstance(user, dependedParamValues).getVocabTreeRoots();
  }

  public String[] getVocabInternal(User user) {
    return getVocabInternal(user, null);
  }

  public String[] getVocabInternal(User user, Map<String, String> dependedParamValues) {
    return getVocabInstance(user, dependedParamValues).getVocabInternal(isNoTranslation());
  }

  /* not used
  public String[] getDisplays(User user) {
    return getDisplays(user, null);
  }

  public String[] getDisplays(User user, Map<String, String> dependedParamValues) {
    return getVocabInstance(user, dependedParamValues).getDisplays();
  }
  */

  public Map<String, String> getVocabMap(User user) {
    return getVocabMap(user, null);
  }

  public Map<String, String> getVocabMap(User user, Map<String, String> contextParamValues) {
    return getVocabInstance(user, contextParamValues).getVocabMap();
  }

  public Map<String, String> getDisplayMap(User user) {
    return getDisplayMap(user, null);
  }

  public Map<String, String> getDisplayMap(User user, Map<String, String> dependedParamValues) {
    return getVocabInstance(user, dependedParamValues).getDisplayMap();
  }

  public Map<String, String> getParentMap(User user) {
    return getParentMap(user, null);
  }

  public Map<String, String> getParentMap(User user, Map<String, String> dependedParamValues) {
    return getVocabInstance(user, dependedParamValues).getParentMap();
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  protected void initTreeMap(EnumParamVocabInstance cache) {

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

  private void suppressChildren(EnumParamVocabInstance cache, List<EnumParamTermNode> children) {
    boolean suppressed = false;
    if (children.size() == 1) {
      // has only one child, suppress it in the tree if it has
      // grandchildren
      EnumParamTermNode child = children.get(0);
      EnumParamTermNode[] grandChildren = child.getChildren();
      if (grandChildren.length > 0) {
        LOG.debug(child.getTerm() + " suppressed.");
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

  public String[] convertToTerms(String stableValue) {
    // the input is a list of terms
    if (stableValue == null)
      return new String[0];
    
    String[] terms;
    if (_multiPick) {
      terms = stableValue.split("[,]+");
      for (int i = 0; i < terms.length; i++) {
        terms[i] = terms[i].trim();
      }
    }
    else {
      terms = new String[] { stableValue.trim() };
    }
    return terms;
  }

  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    if (!isSkipValidation()) {
      String[] terms = getTerms(user, stableValue);
      LOG.debug("param=" + getFullName() + " - validating: " + stableValue +
          ", with contextParamValues=" + FormatUtil.prettyPrint(contextParamValues));

      if (terms.length == 0 && !_allowEmpty)
        throw new WdkUserException("At least one value for " + getPrompt() + " must be selected.");

      // verify that user did not select too few or too many values for this
      // param
      int numSelected = getNumSelected(user, terms, contextParamValues);
      if ((maxSelectedCount > 0 && numSelected > maxSelectedCount) ||
          (_minSelectedCount > 0 && numSelected < _minSelectedCount)) {
        String range = (_minSelectedCount > 0 ? "[ " + _minSelectedCount : "( Inf") + ", " +
            (maxSelectedCount > 0 ? maxSelectedCount + " ]" : "Inf )");
        throw new WdkUserException("Number of selected values (" + numSelected + ") was not in range " +
            range + " for parameter " + getPrompt());
      }

      Map<String, String> map = getVocabMap(user, contextParamValues);
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
      LOG.debug("param=" + getFullName() + " - skip validation");
    }
  }
  
  public String[] getTerms(User user, String stableValue) throws WdkModelException {
    return (String[]) getRawValue(user, stableValue);
  }

  private int getNumSelected(User user, String[] terms, Map<String, String> contextParamValues) {
    // if countOnlyLeaves is set, must generate original tree, set values, and
    // count the leaves
    //String displayType = getDisplayType();
    //logger.debug("Checking whether num selected exceeds max on param " + getFullName() + " with values" +
    //   ": displayType = " + displayType + ", maxSelectedCount = " + getMaxSelectedCount() +
    //   ", countOnlyLeaves = " + getCountOnlyLeaves());
    if (_displayType != null && _displayType.equals(DISPLAY_TREEBOX) && getCountOnlyLeaves()) {
      EnumParamTermNode[] rootNodes = getVocabInstance(user, contextParamValues).getVocabTreeRoots();
      FieldTree tree = getParamTree(getName(), rootNodes);
      populateParamTree(tree, terms);
      return tree.getSelectedLeaves().size();
    }
    // otherwise, just count up terms and compare to max
    return terms.length;
  }

  public static FieldTree getParamTree(String treeName, EnumParamTermNode[] rootNodes) {
    FieldTree tree = new FieldTree(new SelectableItem(treeName, "top"));
    TreeNode<SelectableItem> root = tree.getRoot();
    for (EnumParamTermNode paramNode : rootNodes) {
      if (paramNode.getChildren().length == 0) {
        root.addChild(new SelectableItem(paramNode.getTerm(), paramNode.getDisplay(), paramNode.getDisplay()));
      }
      else {
        root.addChildNode(paramNode.toFieldTree().getRoot());
      }
    }
    return tree;
  }

  public static void populateParamTree(FieldTree tree, String[] values) {
    if (values != null && values.length > 0) {
      List<String> currentValueList = Arrays.asList(values);
      tree.setSelectedLeaves(currentValueList);
      tree.addDefaultLeaves(currentValueList);
    }
  }

  /**
   * @param selectMode
   *          the selectMode to set
   */
  public void setSelectMode(SelectMode selectMode) {
    this.selectMode = selectMode;
  }

  /**
   * @return the selectMode
   */
  public SelectMode getSelectModeEnum() {
    return selectMode;
  }

  /**
   * Builds the default value (and sanity default value) of the "current" enum values
   */
  protected final void applySelectMode(EnumParamVocabInstance cache) throws WdkModelException {
    // logger.debug("applySelectMode(): select mode: '" + selectMode + "', default from model = " +
    //    super.getDefault());
    String defaultFromModel = super.getDefault();

    String errorMessage = "The default value from model, '" + defaultFromModel +
        "', is not a valid term for param " + getFullName() + ", please double check this default value.";
    if (defaultFromModel != null) {
      // default defined in the model, validate default values, and set it
      // to the cache.
      String[] defaults = getMultiPick() ? defaultFromModel.split("\\s*,\\s*")
          : new String[] { defaultFromModel };
      List<String> trimmedDefaults = new ArrayList<>();
      for (String def : defaults) {
        if (cache.getTerms().contains(def)) {
          trimmedDefaults.add(def);
        }
        else {
          // the given default doesn't match any term
          if (isDependentParam()) {
            // need to investigate and make sure the default is as
            // intended.
            // Cannot throws exception here, since the default might
            // not be valid for a different depended value.
            LOG.warn(errorMessage);
          }
          else {
            // param doesn't depend on anything. The default must be wrong.
            LOG.warn(errorMessage);
            if (INVALID_DEFAULT_IS_FATAL) {
              throw new WdkModelException(errorMessage);
            }
          }
        }
      }
      cache.setDefaultValue(FormatUtil.join(
          trimmedDefaults.toArray(new String[trimmedDefaults.size()]), ","));
      return;
    }

    String defaultFromSelectMode = getDefaultWithSelectMode(
        cache.getTerms(), selectMode, _multiPick,
        cache.getTermTreeListRef().isEmpty() ? null :
          cache.getTermTreeListRef().get(0));

    if (defaultFromSelectMode != null) {
      cache.setDefaultValue(defaultFromSelectMode);
    }
  }

  public static String getDefaultWithSelectMode(Set<String> terms, SelectMode selectMode, boolean isMultiPick, EnumParamTermNode firstTreeNode) {
    // single pick can only select one value
    if (selectMode == null || !isMultiPick)
      selectMode = SelectMode.FIRST;
    if (selectMode.equals(SelectMode.ALL)) {
      StringBuilder builder = new StringBuilder();
      for (String term : terms) {
        if (builder.length() > 0)
          builder.append(",");
        builder.append(term);
      }
      return builder.toString();
    }
    else if (selectMode.equals(SelectMode.FIRST)) {
      StringBuilder builder = new StringBuilder();
      Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
      if (firstTreeNode != null)
        stack.push(firstTreeNode);
      while (!stack.empty()) {
        EnumParamTermNode node = stack.pop();
        if (builder.length() > 0)
          builder.append(",");
        builder.append(node.getTerm());
        for (EnumParamTermNode child : node.getChildren()) {
          stack.push(child);
        }
      }
      return builder.toString();
    }
    return null;
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {
    selectMode = ((EnumParamSuggestion) suggest).getSelectModeEnum();
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


    // throw error if user selects treeBox displayType but multiSelect=false
    //   note: no technical reason not to allow this, but we think UX for this is bad
    if (!getMultiPick() && getDisplayType().equals(DISPLAY_TREEBOX)) {
      String contextQueryName = _container == null ? "null" : _container.getFullName();
      throw new WdkModelException("Param ['" + getFullName() +
          "'] in context query ['" + contextQueryName + "']: " +
          "TreeBox display type cannot be selected when multiPick is false.");
    }
  }

  @Override
  public Set<String> getAllValues() throws WdkModelException {
    User user = _wdkModel.getSystemUser();
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
          values.addAll(getVocabMap(user, dependedValue).keySet());
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
      values.addAll(getVocabMap(user).keySet());
    }
    return values;
  }

  protected String getValidStableValue(User user, String stableValue, EnumParamVocabInstance cache) throws WdkModelException {
    if (stableValue == null)
      return cache.getDefaultValue();

    String[] terms = getTerms(user, stableValue);
    // logger.debug("CORRECTING " + name + "=\"" + stableValue + "\"");
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
      return buffer.toString();
    }
    else
      return cache.getDefaultValue();
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

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    super.printDependencyContent(writer, indent);

    // print out depended params, if any
    if (isDependentParam()) {
      List<Param> dependedParams = new ArrayList<>(getDependedParams());
      writer.println(indent + "<dependedParams count=\"" + getDependedParams().size() + "\">");
      Collections.sort(dependedParams, new Comparator<Param>() {
        @Override
        public int compare(Param param1, Param param2) {
          return param1.getFullName().compareToIgnoreCase(param2.getFullName());
        }
      });
      String indent1 = indent + WdkModel.INDENT;
      for (Param param : dependedParams) {
        param.printDependency(writer, indent1);
      }
      writer.println(indent + "</dependedParams>");
    }
  }

  public JSONObject getJsonValues(User user, Map<String, String> contextParamValues) throws WdkModelException,
      WdkUserException {
    return getJsonValues(createVocabInstance(user, contextParamValues));
  }

  /**
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public JSONObject getJsonValues(EnumParamVocabInstance cache)
      throws WdkModelException, WdkUserException {
    JSONObject jsParam = new JSONObject();
    try {
      JSONArray jsValues = new JSONArray();
      for (String term : cache.getTerms()) {
        JSONObject jsValue = new JSONObject();
        jsValue.put("parent", cache.getParent(term));
        jsValue.put("term", term);
        jsValue.put("display", cache.getDisplay(term));
        jsValues.put(jsValue);
      }
      jsParam.put("values", jsValues);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsParam;
  }
  
}
