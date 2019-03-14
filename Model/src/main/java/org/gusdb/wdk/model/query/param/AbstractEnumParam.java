package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.FieldTree;
import org.gusdb.wdk.model.SelectableItem;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
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

  private int depthExpanded = 0;

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
   * @param minSelectedCount
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

  private EnumParamVocabInstance getVocabInstance(PartiallyValidatedStableValues context)
      throws WdkModelException {
    return getVocabInstance(context.getUser(), context);
  }

  public EnumParamVocabInstance getVocabInstance(User user, SemanticallyValid<QueryInstanceSpec> context)
      throws WdkModelException {
    return getVocabInstance(user, context.get().toMap());
  }

  @Override
  protected String getDefault(PartiallyValidatedStableValues stableVals) throws WdkModelException {
    LOG.debug("Default value requested for param " + getName() + " with context values " +
        FormatUtil.prettyPrint(stableVals, Style.SINGLE_LINE));
    String value = getDefault(getVocabInstance(stableVals));
    LOG.debug("Returning default value of '" + value + "' for dependent param " + getName());
    return value;
  }

  @Override
  public String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) throws WdkModelException {
    return getSanityDefaultValue(getVocabInstance(user, contextParamValues), sanitySelectMode, getMultiPick(), getSanityDefault());
  }

  public EnumParamVocabInstance getVocabInstance(SemanticallyValid<QueryInstanceSpec> spec) throws WdkModelException {
    return getVocabInstance(spec.get().getUser(), spec.get().toMap());
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
  protected ParamValidity validateValue(PartiallyValidatedStableValues ctxParamVals, ValidationLevel level)
      throws WdkModelException {

    final String name = getName();
    final String stableValue = ctxParamVals.get(name);

    LOG.debug("param=" + getFullName() + " - validating: " + stableValue +
        ", with contextParamValues=" + FormatUtil.prettyPrint(ctxParamVals));

    if (stableValue.isEmpty() && !_allowEmpty)
      return ctxParamVals.setInvalid(name, "At least one value for "
          + getPrompt() + " must be selected.");

    // all other validation requires a DB lookup, so exit here if syntactic
    if (level.equals(ValidationLevel.SYNTACTIC)) {
      return ctxParamVals.setValid(name);
    }

    // if semantic or runnable, must verify term counts and validity
    EnumParamVocabInstance vocab = getVocabInstance(ctxParamVals);
    List<String> selectedTerms = Arrays.asList(((EnumParamHandler)_handler)
        .toRawValue(ctxParamVals.getUser(), stableValue));

    // verify that user did not select too few or too many values for this param
    int numSelected = getNumSelected(vocab, selectedTerms);
    if ((maxSelectedCount > 0 && numSelected > maxSelectedCount) ||
        (_minSelectedCount > 0 && numSelected < _minSelectedCount)) {
      String range = (_minSelectedCount > 0 ? "[ " + _minSelectedCount : "( Inf") + ", " +
          (maxSelectedCount > 0 ? maxSelectedCount + " ]" : "Inf )");
      return ctxParamVals.setInvalid(name, "Number of selected values ("
        + numSelected + ") was not in range " + range + " for parameter "
        + getPrompt());
    }

    Set<String> allTerms = vocab.getTerms();
    List<String> messages = new ArrayList<>();
    for (String term : selectedTerms) {
      if (!allTerms.contains(term)) {
        messages.add("Invalid term for param [" + getFullName() + "]: " + term + ".");
      }
    }
    return messages.isEmpty() ?
        ctxParamVals.setValid(name) :
        ctxParamVals.setInvalid(name, FormatUtil.join(messages, FormatUtil.NL));
  }

  private int getNumSelected(EnumParamVocabInstance vocab, List<String> selectedTerms) {
    // if countOnlyLeaves is set, must generate original tree, set values, and count the leaves
    if (_displayType != null && _displayType.equals(DISPLAY_TREEBOX) && getCountOnlyLeaves()) {
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
  // FIXME: this method is public only to support WdkQueryPlugin.java, which is
  //    due to be retired when ApiFed starts using the WDK service (or UniDB is
  //    in use).  When either of these happens, change this back to private.
  public String getDefault(EnumParamVocabInstance cache) throws WdkModelException {
    String defaultFromModel = getXmlDefault();
    LOG.debug("applySelectMode(): select mode: '" + selectMode + "', default from model = " + defaultFromModel);
    if (defaultFromModel != null) {
      // default defined in the model, validate default values before returning
      String[] defaults = getMultiPick() ?
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
      return FormatUtil.join(trimmedDefaults.toArray(new String[trimmedDefaults.size()]), ",");
    }

    return getDefaultWithSelectMode(
        cache.getTerms(), selectMode, _multiPick,
        cache.getTermTreeListRef().isEmpty() ? null :
          cache.getTermTreeListRef().get(0));
  }


  /**
   * Determines and returns the sanity default for this param in the following way: if sanitySelectMode is not
   * null, use it to choose params; if it is, use default (i.e. however param normally gets default)
   * 
   * @param sanitySelectMode
   *          select mode form model (ParamValuesSet)
   * @return default value for this param, based on cached vocab values
   * @throws WdkModelException 
   */
  public String getSanityDefaultValue(EnumParamVocabInstance vocab,
      SelectMode sanitySelectMode, boolean isMultiPick, String sanityDefaultNoSelectMode) throws WdkModelException {
    LOG.info("Getting sanity default value with passed mode: " + sanitySelectMode);
    if (sanitySelectMode != null) {
      return getDefaultWithSelectMode(vocab.getTerms(), sanitySelectMode, isMultiPick,
          vocab.getTermTreeListRef().isEmpty() ? null : vocab.getTermTreeListRef().get(0));
    }
    String defaultVal = getDefault(vocab);
    LOG.info("Sanity select mode is null; using sanity default (" + sanityDefaultNoSelectMode +
        ") or default (" + defaultVal + ")");
    return sanityDefaultNoSelectMode != null ? sanityDefaultNoSelectMode : defaultVal;
  }

  private static String getDefaultWithSelectMode(Set<String> terms, SelectMode selectMode, boolean isMultiPick, EnumParamTermNode firstTreeNode) {
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
    return "";
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

    // make sure empty param value is either valid or does not happen
    String defaultFromModel = getXmlDefault();
    if ((defaultFromModel == null || defaultFromModel.isEmpty()) &&
        SelectMode.NONE.equals(selectMode) && !_allowEmpty) {
      String containerName = getContainer() == null ? "unknown" : getContainer().getFullName();
      String msg = "Default value for param '" + getFullName() +
          "' in question '" + containerName + "' cannot be valid " +
          "since the default must be empty but allowEmpty is false.";
      LOG.warn(msg);
      //throw new WdkModelException(msg);
    }
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
}
