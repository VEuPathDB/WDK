package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The param is used by Query to provide inputs to the query. Each query holds a separate copy of each param,
 * since a param can be customized on query level, or even on question level, if the query is an ID query.
 * 
 * The param values will go through a life cycle in a following way. First, we gets the value from user input
 * as raw value; then it is transformed into reference value, which is used in URLs, and saved in user's
 * steps. Then when the value is used to execute a query, the user-dependent value will be transformed into
 * internal value, and is fed to the query instance.
 * 
 * If the noTranslation is set to true, the last stage of the transform will be disabled, and the
 * user-dependent value will be used as internal value.
 * 
 * You could provide your own java handler code to process the values in each stage of the life cycle of the
 * param values.
 * 
 * @author xingao
 * 
 *         There are four possible inputs to a param:
 * 
 *         raw data: the data retrieved by processQuestion action, which can be very long, and needs to be
 *         compressed.
 * 
 *         stable data: the data used in URLs and saved in user's steps.
 * 
 *         Internal data: the data used in the SQL.
 * 
 *         signature: the data used to generate checksum, which will be used to index a cache. The signature
 *         should not contain any user related information to make sure the cache can be shared between used.
 * 
 *         We define the following transformations between value types:
 * 
 *         raw -> stable
 * 
 *         stable -> raw
 * 
 *         stable -> internal
 * 
 *         stable -> signature
 * 
 * 
 */
public abstract class Param extends WdkModelBase implements Cloneable, Comparable<Param>, NamedObject {

  public static final String RAW_VALUE_SUFFIX = "_raw";
  public static final String INVALID_VALUE_SUFFIX = "_invalid";

  @Override
  public abstract Param clone();

  public abstract String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException;

  protected abstract void applySuggestion(ParamSuggestion suggest);

  /**
   * The input the method can be either raw data or dependent data
   * 
   * @param user
   * @param rawOrDependentValue
   */
  protected abstract void validateValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException;

  /**
   * TODO: probably want to remove this method, and methods that call it.  it seems to be consumed only by 
   * StepBean (but no jsp or tags), and by the model cacher in fix package, which writes it to db, but never
   * reads it
   * @param jsParam
   * @param extra
   * @throws JSONException
   */
  protected abstract void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException;

  protected String _id;
  protected String _name;
  protected String _prompt;

  private List<WdkModelText> _helps;
  protected String _help;

  // requested by PRISM, array will contain different values for different projects
  private List<WdkModelText> _visibleHelps;
  protected String _visibleHelp;

  // both default value and empty values will be used to construct default raw value. these values themselves
  // are neither valid raw values nor stable values.
  protected String _defaultValue;
  private String _emptyValue;

  // sometimes different values are desired for normal operation vs. sanity test;
  //   in that case, this value will be used if it exists
  private String _sanityDefaultValue;

  protected boolean _visible;
  protected boolean _readonly;

  private Group _group;

  private List<ParamSuggestion> _suggestions;
  protected boolean _allowEmpty;

  protected ParamSet _paramSet;
  private Map<String, Param> _dependentParamsMap = new HashMap<String, Param>();
  private Set<Param> _dependentParams = new HashSet<Param>();

  private List<ParamConfiguration> _noTranslations;

  /**
   * if this flag is set to true, the internal value will be the same as dependent value. This flag is useful
   * when the dependent value is sent to other sites to process using ProcessQuery.
   */
  private boolean _noTranslation = false;

  protected Question _contextQuestion;
  protected ParameterContainer _container;

  private List<ParamHandlerReference> _handlerReferences;
  private ParamHandlerReference _handlerReference;
  protected ParamHandler _handler;

  public Param() {
    _visible = true;
    _readonly = false;
    _group = Group.Empty();
    _helps = new ArrayList<WdkModelText>();
    _visibleHelps = new ArrayList<WdkModelText>();
    _suggestions = new ArrayList<ParamSuggestion>();
    _noTranslations = new ArrayList<ParamConfiguration>();
    _allowEmpty = false;
    _emptyValue = null;
    _defaultValue = null;
    _sanityDefaultValue = null;
    _handlerReferences = new ArrayList<>();
  }

  public Param(Param param) {
    super(param);
    _id = param._id;
    _name = param._name;
    _prompt = param._prompt;
    _help = param._help;
    _visibleHelp = param._visibleHelp;
    _defaultValue = param._defaultValue;
    _sanityDefaultValue = param._sanityDefaultValue;
    _visible = param._visible;
    _readonly = param._readonly;
    _group = param._group;
    _allowEmpty = param._allowEmpty;
    _emptyValue = param._emptyValue;
    _paramSet = param._paramSet;
    _wdkModel = param._wdkModel;
    _noTranslation = param._noTranslation;
    _resolved = param._resolved;
    if (param._handlerReferences != null) {
      _handlerReferences = new ArrayList<>();
      for (ParamHandlerReference reference : param._handlerReferences) {
        _handlerReferences.add(new ParamHandlerReference(this, reference));
      }
    }
    _handlerReference = param._handlerReference;
    if (param._handler != null)
      _handler = param._handler.clone(this);
    _contextQuestion = param._contextQuestion;
    _container = param._container;
    _dependentParamsMap = new HashMap<String, Param>(param._dependentParamsMap);
    _dependentParams = new HashSet<Param>(param._dependentParams);
  }

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  /**
   * @return the id
   */
  public String getId() {
    return _id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    _id = id;
  }

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  void setParamSet(ParamSet paramSet) {
    _paramSet = paramSet;
  }

  public String getFullName() {
    if (_name == null) return null;
    String paramSetName = (_paramSet == null ? "<unknown_param_set>" : _paramSet.getName());
    return paramSetName + "." + _name;
  }

  public void setPrompt(String prompt) {
    _prompt = prompt;
  }

  public String getPrompt() {
    if (_prompt == null)
      return _name;
    return _prompt;
  }

  public void addHelp(WdkModelText help) {
    _helps.add(help);
  }

  public String getHelp() {
    if (_help == null)
      return getPrompt();
    return _help;
  }

  void setHelp(String help) {
    _help = help;
  }

public void addVisibleHelp(WdkModelText visibleHelp) {
    _visibleHelps.add(visibleHelp);
  }

  public String getVisibleHelp() {
		// if (visibleHelp == null)
		// return getHelp(); //should return empty???
    return _visibleHelp;
  }

  void setVisibleHelp(String visibleHelp) {
    _visibleHelp = visibleHelp;
  }

  public void setDefault(String defaultValue) {
    _defaultValue = defaultValue;
  }

  /**
   * @throws WdkModelException
   *           if unable to retrieve default value
   */
  public String getDefault() throws WdkModelException {
    return _defaultValue;
  }

  public void setSanityDefault(String sanityDefaultValue) {
    _sanityDefaultValue = sanityDefaultValue;
  }

  public final String getSanityDefault() {
    if (_sanityDefaultValue == null && isAllowEmpty() && getEmptyValue() != null) {
      return getEmptyValue();
    }
    return _sanityDefaultValue;
  }

  /**
   * @return Returns the readonly.
   */
  public boolean isReadonly() {
    return _readonly;
  }

  /**
   * @param readonly
   *          The readonly to set.
   */
  public void setReadonly(boolean readonly) {
    _readonly = readonly;
  }

  /**
   * @return Returns the visible.
   */
  public boolean isVisible() {
    return _visible;
  }

  /**
   * @param visible
   *          The visible to set.
   */
  public void setVisible(boolean visible) {
    _visible = visible;
  }

  /**
   * @return the allowEmpty
   */
  public boolean isAllowEmpty() {
    return _allowEmpty;
  }

  public void setAllowEmpty(boolean allowEmpty) {
    _allowEmpty = allowEmpty;
  }

  /**
   * @return the emptyValue
   */
  public String getEmptyValue() {
    return _emptyValue;
  }

  /**
   * @param emptyValue
   *          the emptyValue to set
   */
  public void setEmptyValue(String emptyValue) {
    _emptyValue = emptyValue;
  }

  /**
   * @return the group
   */
  public Group getGroup() {
    return _group;
  }

  /**
   * @param group
   *          the group to set
   */
  public void setGroup(Group group) {
    _group = group;
  }

  public void addSuggest(ParamSuggestion suggest) {
    _suggestions.add(suggest);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(getClass().getName())
      .append(": name='").append(_name).append("'").append(NL)
      .append("  prompt='").append(_prompt).append("'").append(NL)
      .append("  help='").append(_help).append("'").append(NL)
      .append("  visibleHelp='").append(_visibleHelp).append("'").append(NL)
      .append("  default='").append(_defaultValue).append("'").append(NL)
      .append("  sanityDefault='").append(_sanityDefaultValue).append("'").append(NL)
      .append("  readonly=").append(_readonly).append(NL)
      .append("  visible=").append(_visible).append(NL)
      .append("  noTranslation=").append(_noTranslation).append(NL);
    if (_group != null)
      buf.append("  group='").append(_group.getName()).append("'").append(NL);

    return buf.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude visibleHelps
    boolean hasVisibleHelp = false;
    for (WdkModelText visibleHelp : _visibleHelps) {
      if (visibleHelp.include(projectId)) {
        if (hasVisibleHelp) {
          throw new WdkModelException("The param " + getFullName() + " has more than one visibleHelp for project " +
              projectId);
        }
        else {
          _visibleHelp = visibleHelp.getText();
          hasVisibleHelp = true;
        }
      }
    }
    _visibleHelps = null;

 // exclude helps
    boolean hasHelp = false;
    for (WdkModelText help : _helps) {
      if (help.include(projectId)) {
        if (hasHelp) {
          throw new WdkModelException("The param " + getFullName() + " has more than one help for project " +
              projectId);
        }
        else {
          _help = help.getText();
          hasHelp = true;
        }
      }
	  }
    _helps = null;

    // exclude suggestions
    boolean hasSuggest = false;
    for (ParamSuggestion suggest : _suggestions) {
      if (suggest.include(projectId)) {
        if (hasSuggest)
          throw new WdkModelException("The param " + getFullName() +
              " has more than one <suggest> for project " + projectId);

        suggest.excludeResources(projectId);
        _defaultValue = suggest.getDefault();
        _allowEmpty = suggest.isAllowEmpty();
        _emptyValue = suggest.getEmptyValue();

        applySuggestion(suggest);

        hasSuggest = true;

      }
    }
    _suggestions = null;

    // exclude noTranslations
    boolean hasNoTranslation = false;
    for (ParamConfiguration noTrans : _noTranslations) {
      if (noTrans.include(projectId)) {
        if (hasNoTranslation)
          throw new WdkModelException("The param " + getFullName() +
              " has more than one <noTranslation> for project " + projectId);
        _noTranslation = noTrans.isValue();
        hasNoTranslation = true;
      }
    }
    _noTranslations = null;

    // exclude handler references
    for (ParamHandlerReference reference : _handlerReferences) {
      if (reference.include(projectId)) {
        // make sure the handler is not defined more than once
        if (_handlerReference != null)
          throw new WdkModelException("param handler is defined more than " + "once for project " +
              projectId + " in param " + getFullName());
        reference.excludeResources(projectId);
        _handlerReference = reference;
      }
    }
    _handlerReferences = null;
  }

  public JSONObject getChecksumJSON(boolean extra) throws JSONException {
    JSONObject jsParam = new JSONObject();
    jsParam.put("name", getFullName());

    appendChecksumJSON(jsParam, extra);
    return jsParam;
  }

  /**
   * @throws WdkModelException
   *           if unable to load resources from model
   */
  public void setResources(WdkModel model) throws WdkModelException {
    _wdkModel = model;
  }

  public String replaceSql(String sql, String internalValue) {
    String regex = "\\$\\$" + _name + "\\$\\$";
    // escape all single quotes in the value

    //logger.debug("\n\nPARAM SQL:\n\n" + sql.replaceAll(regex, Matcher.quoteReplacement(internalValue)) + "\n\n");

    return sql.replaceAll(regex, Matcher.quoteReplacement(internalValue));
  }

  public void validate(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    // handle the empty case
    if (stableValue == null || stableValue.isEmpty()) {
      if (!_allowEmpty) {
        throw new WdkModelException("The parameter '" + getPrompt() + "' does not allow empty value");
        // otherwise, got empty value and is allowed, no need for further validation
      }
    }
    else {
      // value is not empty, the sub classes will complete further validation
      validateValue(user, stableValue, contextParamValues);
    }
  }

  public void addNoTranslation(ParamConfiguration noTranslation) {
    _noTranslations.add(noTranslation);
  }

  public boolean isNoTranslation() {
    return _noTranslation;
  }

  public void setNoTranslation(boolean noTranslation) {
    _noTranslation = noTranslation;
  }

  /**
   * Set the question where the param is used. The params in a question are always cloned when question is
   * initialized, therefore, each param object will refer to one question uniquely.
   * 
   * @param question
   * @throws WdkModelException
   */
  public void setContextQuestion(Question question) throws WdkModelException {
    _contextQuestion = question;
  }

  public Question getContextQuestion() {
    return _contextQuestion;
  }

  public void setContainer(ParameterContainer container) {
    _container = container;
  }

  public ParameterContainer getContainer() {
    return _container;
  }

  public void setHandler(ParamHandler handler) {
    handler.setParam(this);
    _handler = handler;
  }

  /**
   * Transform raw param value into stable value.
   * 
   * @param user
   * @param rawValue
   * @param contextParamValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public String toStableValue(User user, Object rawValue)
      throws WdkModelException, WdkUserException {
    return _handler.toStableValue(user, rawValue);
  }

  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    return _handler.getStableValue(user, requestParams);
  }

  /**
   * Transform stable param value back to raw value;
   * 
   * @param user
   * @param stableValue
   * @param contextParamValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public Object getRawValue(User user, String stableValue)
      throws WdkModelException {
    return _handler.toRawValue(user, stableValue);
  }

  /**
   * Transform stable param value into internal value. The noTranslation and quote flags should be handled by
   * the plugin.
   * 
   * @param user
   * @param stableValue
   *          if the value is empty, and if empty is allow, the assigned empty value will be used as stable
   *          value to be transformed into the internal.
   * @param contextParamValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public String getInternalValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
    if (stableValue == null || stableValue.length() == 0)
      if (isAllowEmpty())
        stableValue = getEmptyValue();

    return _handler.toInternalValue(user, stableValue, contextParamValues);
  }

  /**
   * 
   * @param user
   * @param stableValue
   * @param contextParamValues context (used by some subclasses)
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public String getSignature(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
    if (stableValue == null) return "";
    return _handler.toSignature(user, stableValue, contextParamValues);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (_resolved)
      return;

    super.resolveReferences(wdkModel);

    _wdkModel = wdkModel;

    // resolve reference for handler
    if (_handlerReference != null) {
      try {
        Class<? extends ParamHandler> handlerClass = Class.forName(_handlerReference.getImplementation()).asSubclass(
            ParamHandler.class);
        _handler = handlerClass.newInstance();
        _handler.setProperties(_handlerReference.getProperties());
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        throw new WdkModelException(ex);
      }
      _handlerReference = null;
    }
    if (_handler == null)
      throw new WdkModelException("The param handler is not provided for param " + getFullName());

    // the handler might not be initialized from reference, it might be created
    // by the param by default.
    _handler.setParam(this);
    _handler.setWdkModel(wdkModel);
  }

  public Set<String> getAllValues() throws WdkModelException {
    Set<String> values = new LinkedHashSet<>();
    values.add(getDefault());
    return values;
  }

  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    _handler.prepareDisplay(user, requestParams, contextParamValues);
  }

  public final void printDependency(PrintWriter writer, String indent) throws WdkModelException {
    writer.println(indent + "<" + getClass().getSimpleName() + " name=\"" + getFullName() + "\">");
    printDependencyContent(writer, indent + WdkModel.INDENT);
    writer.println(indent + "</" + getClass().getSimpleName() + ">");
  }

  /**
   * @param writer
   * @param indent
   * @throws WdkModelException if unable to print dependency content 
   */
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    // by default, print nothing
  }
  
  public void addHandler(ParamHandlerReference handlerReference) {
    _handlerReferences.add(handlerReference);
  }
  
  public String getDisplayValue(User user, String stableValue, Map<String, String> contextParamValues) throws WdkModelException {
    return _handler.getDisplayValue(user, stableValue, contextParamValues);
  }
  
  /**
   * Backlink to dependent params, set by dependent params.
   * @param param
   */
  public void addDependentParam(Param param) {
    if (!_dependentParamsMap.containsKey(param.getName())) {
      _dependentParamsMap.put(param.getName(), param);  // fix bug where multiple copies of param were added to set
      _dependentParams.add(param);
    }
  }

  public Set<Param> getDependentParams() {
    return Collections.unmodifiableSet(_dependentParams);
  }

  public Set<Param> getAllDependentParams() {
    Set<Param> answer = new HashSet<Param>();
    answer.addAll(_dependentParams);
    for (Param dependent : _dependentParams) {
      answer.addAll(dependent.getAllDependentParams());
    }
    return answer;
  }
  
  /**
   * By default, params are not dependent, and so do not become stale.  must be overridden by dependent params
   * The definition of stale is: given a possible changes in values of the depended params provided on input,
   * this param is stale if a previous value for it might no longer be valid
   * 
   * @param staleDependedParamsFullNames
   * @return
   */
  public boolean isStale(Set<String> staleDependedParamsFullNames) {
    return false;
  }
  
  public Set<Param> getStaleDependentParams() {
    Set<String> ss = new HashSet<String>();
    ss.add(getFullName());
    return getStaleDependentParams(ss);
  }


  /**
   * 
   * given an input list of changed or stale params, return a list of (recursively) dependent params that
   * are stale as a consequence. 
   * @param staleDependedParamsFullNames the names of depended params whose value has changed, either directly or
   * because they are stale.  
   * @return a list of stale dependent params
   */
  private Set<Param> getStaleDependentParams(Set<String> staleDependedParamsFullNames) {

    Set<Param> staleDependentParams = new HashSet<Param>(); // return value

    for (Param dependentParam : getDependentParams()) {

      // if dependent param is stale, add it to stale depended list and recurse to find kids' stale dependents (unless already visited)
      if (!staleDependedParamsFullNames.contains(dependentParam.getName()) && dependentParam.isStale(staleDependedParamsFullNames)) {

        staleDependentParams.add(dependentParam);

        Set<String> newStaleDependedParams = new HashSet<String>(staleDependedParamsFullNames);
        newStaleDependedParams.add(dependentParam.getFullName());

        staleDependentParams.addAll(dependentParam.getStaleDependentParams(newStaleDependedParams));
      }
    }
    return staleDependentParams;
  }

  public ParamHandler getParamHandler() {
    return _handler;
  }

  @Override
  public int compareTo(Param other) {
    return this.getFullName().compareTo(other.getFullName());
  }
}
