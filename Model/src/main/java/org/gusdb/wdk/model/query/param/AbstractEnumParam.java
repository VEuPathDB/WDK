package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.FieldTree;
import org.gusdb.wdk.model.SelectableItem;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides functions that are common among EnumParam and
 * FlatVocabParam. The parameter of this type can be rendered in the following
 * ways:
 * <ul>
 *   <li>A radio button list, and user can choose only one value from it.
 *   <li>A dropdown menu, and user can choose only one value.
 *   <li>A checkbox list, and user can choose more than one values.
 *   <li>A checkbox tree, and user can choose branches with all the leaves.
 *   <li>A type-ahead input box, and when user starts typing, all the matched
 *       values will be suggested to the user, and currently only one value is
 *       allowed to be chosen from the suggested list.
 * </ul>
 * <p>
 * Furthermore, such a param can depend on another param, and if the value of
 * that param is changed, the allowed list of values of this enum/flatVocab
 * param will also be changed on the fly. Currently, an enum/flatVocab param can
 * only depend on another enum or flatVocab param.
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

  public enum SelectMode {
    NONE,   // select none of the available options
    ALL,    // select all the available options
    FIRST;  // select only the first value in the list of available options
  }

  public enum DisplayType {
    SELECT("select"),
    CHECKBOX("checkBox"),
    TREEBOX("treeBox"),
    TYPEAHEAD("typeAhead");

    public static final String DEPRECATION_MESSAGE =
        "NOTE: radioBox and listBox are no longer in use and should " +
        "be replaced with checkBox and select respectively.";

    private final String _value;

    private DisplayType(String value) {
      _value = value;
    }

    public String getValue() {
      return _value;
    }

    public static DisplayType parse(String value) throws WdkModelException {
      for (DisplayType type : values()) {
        if (type._value.equals(value)) {
          return type;
        }
      }
      String validValues = Arrays.stream(values())
          .map(val -> "'" + val._value + "'")
          .collect(Collectors.joining(", "));
      throw new WdkModelException("'" + value + "' is not a valid display type." +
          " Only [" + validValues + "] allowed. " + DEPRECATION_MESSAGE);
    }
  }

  protected Boolean _multiPick = false;
  protected boolean _quote = true;

  private DisplayType _displayType;
  private int _minSelectedCount = -1;
  private int _maxSelectedCount = -1;
  private boolean _countOnlyLeaves = true;

  /**
   * this property is only used by abstractEnumParams, but have to be
   * initialized from suggest. it is an enum with values: NONE, ALL, FIRST
   */
  protected SelectMode _selectMode;

  /**
   * collapse single-child branches if set to true
   */
  private boolean _suppressNode;

  private int _depthExpanded;

  private boolean _allowEmptyVocabulary = false;

  // FIXME: this method is public only to support WdkQueryPlugin.java, which is
  //    due to be retired when ApiFed starts using the WDK service (or UniDB is
  //    in use).  When either of these happens, change this back to protected.
  public abstract EnumParamVocabInstance getVocabInstance(User user, Map<String,String> stableValues)
      throws WdkModelException;

  public AbstractEnumParam() {
    super();

    // register handlers
    setHandler(new EnumParamHandler());
  }

  public AbstractEnumParam(AbstractEnumParam param) {
    super(param);
    _multiPick = param._multiPick;
    _quote = param._quote;
    _displayType = param._displayType;
    _selectMode = param._selectMode;
    _suppressNode = param._suppressNode;
    _minSelectedCount = param._minSelectedCount;
    _maxSelectedCount = param._maxSelectedCount;
    _countOnlyLeaves = param._countOnlyLeaves;
    _depthExpanded = param._depthExpanded;
    _allowEmptyVocabulary = param._allowEmptyVocabulary;
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  // used only to initially set this property
  public void setMultiPick(boolean multiPick) {
    _multiPick = multiPick;
  }

  public boolean isMultiPick() {
    return _multiPick;
  }

  public void setQuote(boolean quote) {
    _quote = quote;
  }

  /**
   * If the quote is true, WDK will escape all the single quotes from internal
   * value, and then wrap those values around with single quotes. then the final
   * value will be substituted into the SQL.
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
    return ((_displayType != null ? _displayType :
      (isMultiPick() ? DisplayType.CHECKBOX : DisplayType.SELECT))).getValue();
  }

  /**
   * @param displayType
   *          the displayType to set
   */
  public void setDisplayType(String displayType) throws WdkModelException {
    _displayType = DisplayType.parse(displayType);
  }

  /**
   * Returns the minimun number of selections allowed for this param; the value
   * in the model is intertwined with allowEmpty, which always takes precedence
   * in a conflict with this value.  Note that if this value is greater than
   * that returned by getMaxSelectedCount(), there may be an empty range of the
   * number of allowed values.
   *
   * min-sel  -1  0  >0
   * --------------------
   * allow 1   0  0  min
   * empty 0   1  1  min
   *
   * @return The minimum number of allowed values for this param
   */
  public int getMinSelectedCount() {
    return _minSelectedCount > 1 ? _minSelectedCount : _allowEmpty ? 0 : 1;
  }

  /**
   * @param minSelectedCount
   *   The minimum number of allowed values for this param. If not set, default
   *   is "no min"; any number of values can be assigned.
   */
  public void setMinSelectedCount(int minSelectedCount) {
    _minSelectedCount = minSelectedCount;
  }

  /**
   * Returns the maximum number of selections allowed for this param; the value
   * in the model is intertwined with isMultiPick, which always takes precedence
   * in a conflict with this value.  Note that if this value is smaller than
   * that returned by getMinSelectedCount(), there may be an empty range of the
   * number of allowed values.
   *
   * @return The maximum number of allowed values for this param; if not set
   * (i.e. no max), this method will return -1.
   */
  public int getMaxSelectedCount() {
    // only allow one value if multiPick set to false
    if (!isMultiPick())
      return 1;
    return _maxSelectedCount;
  }

  /**
   * @param maxSelectedCount
   *   The maximum number of allowed values for this param. If not set, default
   *   is "no max"; any number of values can be assigned.
   */
  public void setMaxSelectedCount(int maxSelectedCount) {
    _maxSelectedCount = maxSelectedCount;
  }

  /**
   * @return true if, when validating min- and maxSelectedCount (see above), we
   * should only count leaves towards the total selected value count, or, if
   * false, count both leaves and branch selections
   */
  public boolean getCountOnlyLeaves() {
    return _countOnlyLeaves;
  }

  /**
   * @param countOnlyLeaves
   *   Set to true if, when validating min- and maxSelectedCount (see above), we
   *   should only count leaves towards the total selected value count, or set
   *   to false if both leaves and branch selections should be counted
   */
  public void setCountOnlyLeaves(boolean countOnlyLeaves) {
    _countOnlyLeaves = countOnlyLeaves;
  }

  public void setDepthExpanded(int depthExpanded) {
    _depthExpanded = depthExpanded;
  }

  public int getDepthExpanded() {
    return _depthExpanded;
  }

  public void setAllowEmptyVocabulary(boolean allowEmptyVocabulary) {
    _allowEmptyVocabulary = allowEmptyVocabulary;
  }

  public boolean isAllowEmptyVocabulary() {
    return _allowEmptyVocabulary;
  }

  private EnumParamVocabInstance getVocabInstance(PartiallyValidatedStableValues context)
      throws WdkModelException {
    return getVocabInstance(context.getUser(), context);
  }

  public <T extends ParameterContainerInstanceSpec<T>> EnumParamVocabInstance getVocabInstance(DisplayablyValid<T> spec)
      throws WdkModelException {
    return getVocabInstance(spec.get().getUser(), spec.get().toMap());
  }

  @Override
  protected String getDefault(PartiallyValidatedStableValues stableVals) throws WdkModelException {
    LOG.debug("Default value requested for param " + getName() + " with context values " +
        FormatUtil.prettyPrint(stableVals, Style.SINGLE_LINE));
    String priorValue = stableVals.get(getName());
    String value = getDefault(priorValue, getVocabInstance(stableVals));
    LOG.debug("Returning default value of '" + value + "' for dependent param " + getName());
    return value;
  }

  @Override
  public String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) throws WdkModelException {
    return getSanityDefaultValue(getVocabInstance(user, contextParamValues), sanitySelectMode, isMultiPick(), getSanityDefault());
  }

  public Map<String, String> getDisplayMap(User user, Map<String, String> contextParamValues) throws WdkModelException {
    return getVocabInstance(user, contextParamValues).getDisplayMap();
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  protected void initTreeMap(EnumParamVocabInstance cache) {

    // construct index
    Map<String, EnumParamTermNode> indexMap = new LinkedHashMap<>();
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

    if (_suppressNode)
      suppressChildren(cache, cache.getTermTreeListRef());
  }

  private void suppressChildren(EnumParamVocabInstance cache, List<EnumParamTermNode> children) {
    boolean suppressed = false;
    if (children.size() == 1) {
      // has only one child, suppress it in the tree if it has grandchildren
      EnumParamTermNode child = children.get(0);
      EnumParamTermNode[] grandChildren = child.getChildren();
      if (grandChildren.length > 0) {
        LOG.debug(child.getTerm() + " suppressed.");
        children.remove(0);
        children.addAll(Arrays.asList(grandChildren));
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

  public static List<String> convertToTerms(String stableValue) throws JSONException {
    return stableValue == null ? Collections.emptyList() :
      Arrays.asList(JsonUtil.toStringArray(new JSONArray(stableValue)));
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues ctxParamVals, ValidationLevel level)
      throws WdkModelException {

    final String name = getName();
    final String stableValue = ctxParamVals.get(name);

    if (LOG.isEnabledFor(Param.VALIDATION_LOG_PRIORITY))
      LOG.log(Param.VALIDATION_LOG_PRIORITY, "param=" + getFullName() + " - validating: " + stableValue +
        ", with contextParamValues=" + FormatUtil.prettyPrint(ctxParamVals));

    // all other validation requires a DB lookup, so exit here if displayable or less
    if (level.isLessThanOrEqualTo(ValidationLevel.SYNTACTIC)) {
      LOG.log(Param.VALIDATION_LOG_PRIORITY, "Don't need to check against vocab since validation level is syntactic or less; returning valid=true");
      return ctxParamVals.setValid(name, level);
    }

    // if semantic or runnable, must verify term counts and validity
    EnumParamVocabInstance vocab = getVocabInstance(ctxParamVals);
    List<String> selectedTerms = AbstractEnumParam.convertToTerms(stableValue);

    // verify that user did not select too few or too many values for this param
    int numSelected = getNumSelected(vocab, selectedTerms);
    int minSelectedCount = getMinSelectedCount();
    int maxSelectedCount = getMaxSelectedCount();
    if ((maxSelectedCount > 0 && numSelected > maxSelectedCount) ||
        numSelected < getMinSelectedCount()) {
      String range = "( " + minSelectedCount + ", " +
          (maxSelectedCount >= 0 ? maxSelectedCount : "unlimited") + " )";
      return ctxParamVals.setInvalid(name, level, "Number of selected values ("
        + numSelected + ") is not allowed.  Must be within " + range);
    }

    Set<String> allTerms = vocab.getTerms();
    if (LOG.isEnabledFor(Param.VALIDATION_LOG_PRIORITY))
      LOG.log(Param.VALIDATION_LOG_PRIORITY, "Validating " + getName() + NL +
        "Valid terms: " + String.join(", ", allTerms) + NL +
        "Found terms: " + String.join(", ", selectedTerms));
    List<String> messages = new ArrayList<>();
    for (String term : selectedTerms) {
      if (!allTerms.contains(term)) {
        messages.add("Invalid value '" + term + "'.");
      }
    }

    return messages.isEmpty() ?
        ctxParamVals.setValid(name, level) :
        ctxParamVals.setInvalid(name, level, FormatUtil.join(messages, FormatUtil.NL));
  }

  private int getNumSelected(EnumParamVocabInstance vocab, List<String> selectedTerms) {
    // if countOnlyLeaves is set, must generate original tree, set values, and count the leaves
    if (_displayType != null && _displayType.equals(DisplayType.TREEBOX) && getCountOnlyLeaves()) {
      EnumParamTermNode[] rootNodes = vocab.getVocabTreeRoots();
      FieldTree tree = getParamTree(getName(), rootNodes);
      populateParamTree(tree, selectedTerms);
      return tree.getSelectedLeaves().size();
    }
    // otherwise, just count up terms and compare to max
    return selectedTerms.size();
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

  public static void populateParamTree(FieldTree tree, List<String> selectedTerms) {
    if (!selectedTerms.isEmpty()) {
      tree.setSelectedLeaves(selectedTerms);
      tree.addDefaultLeaves(selectedTerms);
    }
  }

  /**
   * @param selectMode
   *          the selectMode to set
   */
  public void setSelectMode(SelectMode selectMode) {
    _selectMode = selectMode;
  }

  /**
   * @return the selectMode
   */
  public SelectMode getSelectModeEnum() {
    return _selectMode;
  }

  @Override
  protected boolean isEmptyValue(String value) {
    return super.isEmptyValue(value) || new JSONArray(value).length() == 0;
  }

  /**
   * Builds the default value (and sanity default value) of the "current" enum values
   */
  // FIXME: this method is public only to support WdkQueryPlugin.java, which is
  //    due to be retired when ApiFed starts using the WDK service (or UniDB is
  //    in use).  When either of these happens, change this back to private.
  public String getDefault(String existingStableValue, EnumParamVocabInstance cache) throws WdkModelException {
    String trimmedExistingValue = trimInvalidValues(existingStableValue, cache);
    if (!isEmptyValue(trimmedExistingValue)) {
      return trimmedExistingValue;
    }
    String defaultFromModel = getXmlDefault();
    LOG.debug("applySelectMode(): select mode: '" + _selectMode + "', default from model = " + defaultFromModel);
    if (defaultFromModel != null) {
      // default defined in the model, validate default values before returning
      String[] defaults = isMultiPick() ?
          defaultFromModel.split("\\s*,\\s*") :
          new String[] { defaultFromModel };
      List<String> trimmedDefaults = new ArrayList<>();
      for (String def : defaults) {
        if (cache.getTerms().contains(def)) {
          trimmedDefaults.add(def);
        }
        else {
          // the given default doesn't match any term
          String errorMessage = "The default value from model, '" +
              defaultFromModel + "', is not a valid term for param " +
              getFullName() + ", please double check this default value.";
          if (isDependentParam()) {
            // Need to investigate and make sure the default is as intended;
            // cannot throw exception here, since the default might not be valid
            // for a different depended value.
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
      return new JSONArray(trimmedDefaults).toString();
    }

    return getDefaultWithSelectMode(
        cache.getTerms(), _selectMode, _multiPick,
        cache.getTermTreeListRef().isEmpty() ? null :
          cache.getTermTreeListRef().get(0));
  }

  /**
   * Reads the existing stable value and tries to extract valid terms from the
   * value, keeping them while discarding invalid terms.  Returns a value
   * representing the remaining valid terms.
   * 
   * @param existingStableValue an existing stable value
   * @param cache cache containing populated vocabulary
   * @return a valid value containing any remaining valid terms
   */
  private String trimInvalidValues(String existingStableValue, EnumParamVocabInstance cache) {
    JSONArray validValues = new JSONArray();
    if (isEmptyValue(existingStableValue)) {
      return validValues.toString();
    }
    String[] values = JsonUtil.toStringArray(new JSONArray(existingStableValue));
    for (String value : values) {
      if (cache.getTerms().contains(value)) {
        // this value is valid
        validValues.put(value);
      }
    }
    return validValues.toString();
  }

  /**
   * Determines and returns the sanity default for this param in the following
   * way: if sanitySelectMode is not null, use it to choose params; if it is,
   * use default (i.e. however param normally gets default)
   *
   * @param sanitySelectMode
   *   select mode form model (ParamValuesSet)
   *
   * @return default value for this param, based on cached vocab values
   */
  private String getSanityDefaultValue(EnumParamVocabInstance vocab,
      SelectMode sanitySelectMode, boolean isMultiPick, String sanityDefaultNoSelectMode) throws WdkModelException {
    LOG.info("Getting sanity default value with passed mode: " + sanitySelectMode);
    if (sanitySelectMode != null) {
      return getDefaultWithSelectMode(vocab.getTerms(), sanitySelectMode, isMultiPick,
          vocab.getTermTreeListRef().isEmpty() ? null : vocab.getTermTreeListRef().get(0));
    }
    String defaultVal = getDefault(null, vocab);
    LOG.info("Sanity select mode is null; using sanity default (" + sanityDefaultNoSelectMode +
        ") or default (" + defaultVal + ")");
    return sanityDefaultNoSelectMode != null ? sanityDefaultNoSelectMode : defaultVal;
  }

  private static String getDefaultWithSelectMode(Set<String> terms, SelectMode selectMode, boolean isMultiPick, EnumParamTermNode firstTreeNode) {
    // single pick can only select one value
    if (selectMode == null || !isMultiPick)
      selectMode = SelectMode.FIRST;
    if (selectMode.equals(SelectMode.ALL)) {
      JSONArray array = new JSONArray();
      for (String term : terms) {
        array.put(term);
      }
      return array.toString();
    }
    else if (selectMode.equals(SelectMode.FIRST)) {
      JSONArray array = new JSONArray();
      Stack<EnumParamTermNode> stack = new Stack<EnumParamTermNode>();
      if (firstTreeNode != null)
        stack.push(firstTreeNode);
      while (!stack.empty()) {
        EnumParamTermNode node = stack.pop();
        array.put(node.getTerm());
        for (EnumParamTermNode child : node.getChildren()) {
          stack.push(child);
        }
      }
      return array.toString();
    }
    return new JSONArray().toString();
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {
    _selectMode = ((EnumParamSuggestion) suggest).getSelectModeEnum();
  }

  /**
   * @return the suppressNode
   */
  public boolean isSuppressNode() {
    return _suppressNode;
  }

  /**
   * @param suppressNode
   *          the suppressNode to set
   */
  public void setSuppressNode(boolean suppressNode) {
    _suppressNode = suppressNode;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // throw error if user selects treeBox displayType but multiSelect=false
    //   note: no technical reason not to allow this, but we think UX for this is bad
    if (!isMultiPick() && DisplayType.TREEBOX.equals(_displayType)) {
      String contextQueryName = _container == null ? "null" : _container.getFullName();
      throw new WdkModelException("Param ['" + getFullName() +
          "'] in context query ['" + contextQueryName + "']: " +
          "TreeBox display type cannot be selected when multiPick is false.");
    }
  }

  /**
   * Override version in param to consider selectMode when deciding if default
   * conflicts with allowEmpty setting.
   */
  @Override
  protected boolean isInvalidEmptyDepended() {
    return super.isInvalidEmptyDepended()
      && SelectMode.NONE.equals(_selectMode);
  }

  /**
   * enum params are always stale if any depended param is stale
   */
  @Override
  public boolean isStale(Set<String> staleDependedParamsFullNames) {
    return true;
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) {
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

  public JSONObject getJsonValues(EnumParamVocabInstance cache)
      throws WdkModelException {
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

  /**
   * Special case to support legacy values. Old DBs may contain comma-delimited
   * values for multi-pick, which we will still try to convert into the new
   * stable value format: stringified JSON Array of String
   */
  @Override
  public String getStandardizedStableValue(String stableValue) {
    return standardizeStableValue(stableValue, isMultiPick());
  }

  public static String standardizeStableValue(String stableValue, boolean isMultiPick) {
    try {
      if (stableValue == null || stableValue.isEmpty()) {
        return "[]";
      }
      return new JSONArray(stableValue).toString();
    }
    catch(JSONException e) {
      // unable to convert to JSON; convert from single pick external value or old format
      String[] values = isMultiPick ? stableValue.split(",") : new String[] { stableValue };
      return new JSONArray(values).toString();
    }
  }

  /**
   * Special case to support different external stable value formats for
   * single-pick and multi-pick values.  Internal stable value is always a
   * stringified JSON Array of String (see getStandardizedStableValue() above);
   * keep as is (multi-pick) or convert to either raw string (single-pick).
   * 
   * @param standardizedStableValue stringified JSON array of string with 0 or 1
   * values (single-pick) or any number of values (multi-pick)
   * @return stable value expected by clients.  For single-pick this is a raw
   * string value representing the single value, or an empty string for empty
   * value.  For multi-pick, it's the same as the interal stable value: a
   * stringified JSON array of terms (strings)
   */
  @Override
  public String getExternalStableValue(String standardizedStableValue) {
    try {
      JSONArray array = parseValue(standardizedStableValue, getName());
      if (isMultiPick()) {
        return array.toString();
      }
      else {
        switch (array.length()) {
          case 0: return "";
          case 1: return array.getString(0);
          default: throw new WdkRuntimeException("Single-pick enum param '" +
              getName() + "' has multiple values: " + array);
        }
      }
    }
    catch (JSONException e) {
      throw new WdkRuntimeException("Passed value is not a standardized enum value: " + standardizedStableValue);
    }
  }

  /**
   * @param standardizedStableValue supposedly standardized value (will test)
   * @param paramName name of parameter
   * @return JSON array of string values if standardized
   * @throws WdkRuntimeException if not a standardized enum param value
   */
  private static JSONArray parseValue(String standardizedStableValue, String paramName) {
    try {
      JSONArray array = new JSONArray(standardizedStableValue);
      for (int i = 0; i < array.length(); i++) {
        array.getString(i);
      }
      return array;
    }
    catch (JSONException e) {
      throw new WdkRuntimeException("Value passed for param '" + paramName +
          "', '" + standardizedStableValue + "' is not a JSON array of strings.", e);
    }
  }
}
