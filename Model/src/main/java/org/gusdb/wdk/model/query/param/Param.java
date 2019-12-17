package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;

/**
 * Params are used by Query objects and other ParamContainers to provide inputs
 * to the container. Each container holds a separate copy of each param, since a
 * param can be customized at the query/container level, or even at question
 * level, if the query is an ID query.
 *
 * The param values will go through a life cycle in a following way. First, we get the value from user input
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
 *         should not contain any user related information to make sure the cache can be shared between users.
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

  private static final Logger LOG = Logger.getLogger(Param.class);

  public static final Level VALIDATION_LOG_PRIORITY = Level.DEBUG;

  protected static final boolean EMPTY_DESPITE_ALLOWEMPTY_FALSE_IS_FATAL = false;

  @Override
  public abstract Param clone();

  public abstract String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException;

  protected abstract void applySuggestion(ParamSuggestion suggest);

  /**
   * Validates the value of this parameter only.
   * <p>
   * This method can assume that any depended/parent params are already present
   * in contextParamValues; if not, this may throw a WdkModelException.  If
   * validation fails, this method should not throw an exception, but call
   * contextParamValues.setInvalid() with its parameter name and a reason for
   * the failure. The only way to get a ParamValidity is by calling one of the
   * setValid() methods on contextParamValues.
   *
   * @param contextParamValues
   *   partially validated stable value set
   * @param level
   *   level of validation
   *
   * @return proper param validity value
   *
   * @throws WdkModelException
   *   if application error happens while trying to validate
   */
  protected abstract ParamValidity validateValue(PartiallyValidatedStableValues contextParamValues, ValidationLevel level)
      throws WdkModelException;

  protected String _id;
  protected String _name;
  protected String _prompt;

  private List<WdkModelText> _helps;
  protected String _help;

  // requested by PRISM, array will contain different values for different projects
  private List<WdkModelText> _visibleHelps;
  protected String _visibleHelp;

  // both default value and empty values will be used to construct default raw value. these values themselves
  // are neither valid raw values nor stable values.  See FIXME below in getInternalValue()
  protected String _xmlDefaultValue;
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
  private Map<String, Param> _dependentParamsMap = new HashMap<>();
  private Set<Param> _dependentParams = new HashSet<>();

  private List<ParamConfiguration> _noTranslations;

  /**
   * if this flag is set to true, the internal value will be the same as
   * dependent value. This flag is useful when the dependent value is sent to
   * other sites to process using ProcessQuery.
   */
  private boolean _noTranslation;

  protected Question _contextQuestion;
  protected ParameterContainer _container;

  private List<ParamHandlerReference> _handlerReferences;
  private ParamHandlerReference _handlerReference;
  protected ParamHandler _handler;

  public Param() {
    _visible = true;
    _readonly = false;
    _group = Group.Empty();
    _helps = new ArrayList<>();
    _visibleHelps = new ArrayList<>();
    _suggestions = new ArrayList<>();
    _noTranslations = new ArrayList<>();
    _allowEmpty = false;
    // in most cases empty string is preferable empty value to null
    // TODO: determine if non-null, non-empty value should be required in XML if allowEmpty=true
    _emptyValue = "";
    _xmlDefaultValue = null;
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
    _xmlDefaultValue = param._xmlDefaultValue;
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
    _dependentParamsMap = new HashMap<>(param._dependentParamsMap);
    _dependentParams = new HashSet<>(param._dependentParams);
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

  public String getXmlDefault() {
    return _xmlDefaultValue;
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

  @Override
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
    return _visibleHelp;
  }

  void setVisibleHelp(String visibleHelp) {
    _visibleHelp = visibleHelp;
  }

  /**
   * Sets and validates a default value assigned in the model XML
   *
   * @param xmlDefaultValue incoming default value
   * @throws WdkModelException if incoming value is invalid
   */
  public void setDefault(String xmlDefaultValue) throws WdkModelException {
    _xmlDefaultValue = xmlDefaultValue;
  }

  /**
   * Determines a default value for this parameter given the passed stable
   * values.  It can be assumed that any depended param values required for this
   * param have already been filled in and validated.
   *
   * @param stableValues
   *   depended values (guaranteed to be present and already valid)
   *
   * @throws WdkModelException
   *   if unable to retrieve default value
   */
  protected String getDefault(PartiallyValidatedStableValues stableValues) throws WdkModelException {
    return _xmlDefaultValue == null ? "" : _xmlDefaultValue;
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
      .append("  xmlDefault='").append(_xmlDefaultValue).append("'").append(NL)
      .append("  sanityDefault='").append(_sanityDefaultValue).append("'").append(NL)
      .append("  readonly=").append(_readonly).append(NL)
      .append("  visible=").append(_visible).append(NL)
      .append("  noTranslation=").append(_noTranslation).append(NL);
    if (_group != null)
      buf.append("  group='").append(_group.getName()).append("'").append(NL);

    return buf.toString();
  }

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
        _xmlDefaultValue = suggest.getDefault();
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
    return sql.replaceAll(regex, Matcher.quoteReplacement(internalValue));
  }

  private void validationLog(SupplierWithException<String> logMessage) throws WdkModelException {
    if (LOG.isEnabledFor(VALIDATION_LOG_PRIORITY)) {
      try {
        LOG.log(VALIDATION_LOG_PRIORITY, "Container '" + 
            (getContainer() == null ? "null!!" : getContainer().getFullName()) +
            "', Param '" + getName() + "': " + logMessage.get());
      }
      catch (Exception e) {
        throw WdkModelException.translateFrom(e);
      }
    }
  }

  public ParamValidity validate(PartiallyValidatedStableValues stableValues, ValidationLevel level, FillStrategy fillStrategy)
      throws WdkModelException {

    validationLog(() -> "Beginning validation at level: " + level);

    // check to see if this param has already been validated at at least this level
    if (stableValues.hasParamBeenValidated(getName()) &&
        stableValues.getParamValidity(getName()).getLevel().isGreaterThanOrEqualTo(level)) {
      validationLog(() -> "Already validated! isValid = " + stableValues.getParamValidity(getName()).isValid());
      return stableValues.getParamValidity(getName());
    }

    // check if value is empty but empty value allowed
    String value = stableValues.get(getName());
    if (isEmptyValue(value) &&
        isAllowEmpty() &&
        !fillStrategy.shouldFillWhenMissing()) {
      // make sure entry present (might have been missing);
      //  empty value will be filled in at query execution time (internal value conversion)
      validationLog(() -> "Has empty value but this is allowed here and we are not to fill.  Marking valid.");
      return stableValues.setValid(getName(), level);
    }

    // Determine if a default value must be generated
    boolean defaultValueRequired =
        stableValues.get(getName()) == null &&
        fillStrategy.shouldFillWhenMissing();

    // Determine if we will need to run dependent queries within this param
    //   (either to generate a default or simply produce results of depended
    //   queries.  If so, and if running dependent queries requires runnable
    //   values of dependent params, then any depended values will need to be
    //   validated at the runnable level.
    boolean dependedQueriesNeedToBeRun = defaultValueRequired ||
        level.isGreaterThanOrEqualTo(ValidationLevel.DISPLAYABLE);

    // validate any parent (depended) params; if a default for this param will
    //   be generated, then these params MUST be validated at the RUNNABLE level
    //   so that their values can be used to run dependent queries in the params
    //   that depend on them
    ValidationLevel parentLevel = dependedQueriesNeedToBeRun &&
        runningDependedQueriesRequiresRunnableParents() ?
            ValidationLevel.RUNNABLE : level;
    Optional<ParamValidity> invalidityResult = validateDependedParams(stableValues, level, parentLevel, fillStrategy);
    if (invalidityResult.isPresent()) {
      return invalidityResult.get();
    }

    // all parents passed validation; handle case where empty value is always allowed (per flag)
    validationLog(() -> "All parents were valid; continuing with validation...");

    // empty value not generally allowed; fill with default value if required
    if (defaultValueRequired) {
      // fill in default value; value will still be validated below (cheap because vocabs are cached)
      validationLog(() -> "Has empty value and we are to fill with default.");
      stableValues.put(getName(), getDefault(stableValues));
    }

    // handle cases where value is still empty after possibly being populated by a default
    value = stableValues.get(getName()); // refresh local var
    Optional<ParamValidity> validityOpt = handleEmptyValueCases(value, defaultValueRequired, stableValues, level);
    if (validityOpt.isPresent()) return validityOpt.get();

    // sub-classes will complete further validation
    validationLog(() -> "Passing validation to subclass " + getClass().getSimpleName()+ "; value = " + stableValues.get(getName()));
    ParamValidity validity = validateValue(stableValues, level);

    // if valid or (invalid but we were not asked to replace with default) return
    if (validity.isValid() || !fillStrategy.shouldFillWhenInvalid()) {
      validationLog(() -> "Is " + (validity.isValid() ? "valid" : "invalid") + "; returning status.");
      return validity;
    }

    // invalid and asked to replace with default; do it, then revalidate, but first, must validate parents (again)
    invalidityResult = validateDependedParams(stableValues, level, ValidationLevel.RUNNABLE, fillStrategy);
    if (invalidityResult.isPresent()) {
      return invalidityResult.get();
    }

    validationLog(() -> "Value was found invalid but we were asked to fill if invalid; getting default");
    stableValues.put(getName(), getDefault(stableValues));
    validationLog(() -> "Got default value: " + stableValues.get(getName()) + ", checking empty value cases...");

    value = stableValues.get(getName()); // refresh local var
    validityOpt = handleEmptyValueCases(value, defaultValueRequired, stableValues, level);
    if (validityOpt.isPresent()) return validityOpt.get();

    validationLog(() -> "Default value: " + stableValues.get(getName()) + " is not empty, will now validate.");
    ParamValidity defaultsValidity = validateValue(stableValues, level);

    validationLog(() -> "Populated default value is " + (defaultsValidity.isValid() ? "valid" : "invalid") + "; returning status.");
    return defaultsValidity;
  }

  protected boolean isEmptyValue(String value) {
    return value == null || value.isEmpty();
  }

  private Optional<ParamValidity> handleEmptyValueCases(String value, boolean defaultValueRequired,
      PartiallyValidatedStableValues stableValues, ValidationLevel level) throws WdkModelException {
    if (isEmptyValue(value)) {
      String msgPrefix = "Is still empty (defaultUsed=" + defaultValueRequired + ") ";
      // empty value is still allowed if param is not depended on and validation level is displayable or less
      if (level.isLessThanOrEqualTo(ValidationLevel.DISPLAYABLE) && getDependentParams().isEmpty()) {
        validationLog(() -> msgPrefix + "but allowed due to validation level.");
        return Optional.of(stableValues.setValid(getName(), level));
      }
      if (isAllowEmpty()) {
        validationLog(() -> msgPrefix + "but allowed because allowEmpty=true");
        return Optional.of(stableValues.setValid(getName(), level));
      }
      else {
        validationLog(() -> msgPrefix + "and cannot be empty; marking invalid.");
        return Optional.of(stableValues.setInvalid(getName(), level, "Cannot be empty" +
            (defaultValueRequired ? ", but no default value exists." : ".")));
      }
    }
    return Optional.empty();
  }

  /**
   * @return true if the generation of this param's initial display value
   * requires that its depended params have runnably valid values.  Typically
   * this method will return false unless this param needs to run vocabulary
   * queries in order to generate its initial display value
   */
  protected boolean runningDependedQueriesRequiresRunnableParents() {
    return false;
  }

  private Optional<ParamValidity> validateDependedParams(
      PartiallyValidatedStableValues stableValues, ValidationLevel level, ValidationLevel parentLevel, FillStrategy fillStrategy) throws WdkModelException {
    Set<Param> dependedParams = getDependedParams();
    validationLog(() -> "Checking " + dependedParams.size() + " depended params, will use validation level: " + parentLevel);
    Map<String, String> dependedParamValidationErrors = new HashMap<>();
    for (Param parent : dependedParams) {
      validationLog(() -> "Found depended param " + parent.getName() + ", will validate it first...");
      parent.validate(stableValues, parentLevel, fillStrategy);
      validationLog(() -> "Back from parent validation.  Was " + parent.getName() + " valid? " + stableValues.isParamValid(parent.getName()));
      if (!stableValues.isParamValid(parent.getName())) {
        dependedParamValidationErrors.put(parent.getName(), stableValues.getParamValidity(parent.getName()).getMessage());
        // continue to validate parents so caller has most complete information
      }
    }

    if (!dependedParamValidationErrors.isEmpty()) {
      // this param fails validation because its parents failed
      validationLog(() -> "Not all parents were valid so marking invalid.");
      return Optional.of(stableValues.setInvalid(getName(), level,
          "At least one parameter that '" + getName() + "' depends on is invalid or missing. Errors: " + NL +
          FormatUtil.prettyPrint(dependedParamValidationErrors, Style.MULTI_LINE)));
    }

    return Optional.empty();
  }

  /**
   * Returns list of parameters this parameter depends on.  AbstractDependentParam overrides this
   * method to return any depended params for dependent params.
   */
  public Set<Param> getDependedParams() {
    return Collections.emptySet();
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
   * @throws WdkModelException if error occurs assigning values related to the
   * passed question
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
   */
  public String toStableValue(User user, Object rawValue)
      throws WdkModelException, WdkUserException {
    return _handler.toStableValue(user, rawValue);
  }

  /**
   * Transform stable param value into internal value. The noTranslation and
   * quote flags should be handled by the plugin.
   *
   * @return internal value to be used to create query SQL
   */
  public String getInternalValue(RunnableObj<QueryInstanceSpec> queryInstanceSpec)
      throws WdkModelException {
    String stableValue = queryInstanceSpec.get().get(getName());
    if ((stableValue == null || isEmptyValue(stableValue)) && isAllowEmpty()) {
      // FIXME: RRD: need to determine if getEmptyValue really returns a stable
      //             value or internal.  In another place (forget where- maybe
      //             QueryInstance?) we seem to be popping the empty value in at
      //             the last moment as an internal value during param
      //             population (right before query execution).  Need to
      //             discuss with DD.  For now return as if it is an internal value.
      return getEmptyValue();
    }
    LOG.debug("\n  PARAM: " + getName() + "\n  QIS: " + queryInstanceSpec.get());
    return _handler.toInternalValue(queryInstanceSpec);
  }

  /**
   * Creates and returns a signature representing the current value of this
   * param
   *
   * @param spec
   *   context (used by some handler subclasses)
   *
   * @return param value signature
   *
   * @throws WdkModelException
   *   if unable to create signature
   */
  public String getSignature(RunnableObj<QueryInstanceSpec> spec)
      throws WdkModelException {
    String stableValue = spec.get().get(getName());
    return stableValue == null ? "" : _handler.toSignature(spec);
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
        _handler = handlerClass.getDeclaredConstructor().newInstance();
        _handler.setProperties(_handlerReference.getProperties());
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
          IllegalArgumentException | InvocationTargetException |
          NoSuchMethodException | SecurityException ex) {
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

  public final void printDependency(PrintWriter writer, String indent) throws WdkModelException {
    writer.println(indent + "<" + getClass().getSimpleName() + " name=\"" + getFullName() + "\">");
    printDependencyContent(writer, indent + WdkModel.INDENT);
    writer.println(indent + "</" + getClass().getSimpleName() + ">");
  }

  /**
   * @param writer location to which dependencies should be written
   * @param indent how much to indent at this "level"
   * @throws WdkModelException if unable to print dependency content
   */
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    // by default, print nothing
  }

  public void addHandler(ParamHandlerReference handlerReference) {
    _handlerReferences.add(handlerReference);
  }

  /**
   * Backlink to dependent params, set by dependent params.
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
    var answer = new HashSet<>(_dependentParams);
    for (var dependent : _dependentParams)
      answer.addAll(dependent.getAllDependentParams());
    return answer;
  }

  /**
   * By default, params are not dependent, and so do not become stale.  It must
   * be overridden by possibly dependent params.  The definition of stale is:
   * given a possible changes in values of the depended params provided on
   * input, this param is stale if a previous value for it might no longer be
   * valid, e.g. vocabulary of acceptable values has changed.
   * <p>
   * This is more nuanced than simply asking if a depended param is in the
   * passed list. Sometimes declare a dependency on a param for reasons other
   * than vocabulary generation (e.g. internal value generation).
   * 
   * @param staleDependedParamsFullNames a set of stale depended params used
   * to inform logic deciding if this param's value is also stale
   */
  public boolean isStale(Set<String> staleDependedParamsFullNames) {
    return false;
  }

  public Set<Param> getStaleDependentParams() {
    Set<String> ss = new HashSet<>();
    ss.add(getFullName());
    return getStaleDependentParams(ss);
  }

  /**
   * given an input list of changed or stale params, return a list of
   * (recursively) dependent params that are stale as a consequence.
   *
   * @param staleDependedParamNames
   *   the names of depended params whose value has changed, either directly or
   *   because they are stale.
   *
   * @return a list of stale dependent params
   */
  private Set<Param> getStaleDependentParams(Set<String> staleDependedParamNames) {

    Set<Param> staleDependentParams = new HashSet<>(); // return value

    for (Param dependentParam : getDependentParams()) {

      // if dependent param is stale, add it to stale depended list and recurse
      // to find kids' stale dependents (unless already visited)
      if (!staleDependedParamNames.contains(dependentParam.getName())
        && dependentParam.isStale(staleDependedParamNames)) {

        staleDependentParams.add(dependentParam);

        Set<String> newStaleDependedParams = new HashSet<>(
            staleDependedParamNames);
        newStaleDependedParams.add(getFullName());

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

  /**
   * @throws WdkModelException if depended param refs cannot be resolved
   */
  public void resolveDependedParamRefs() throws WdkModelException {
    // nothing to do for most params; overridden by AbstractDependentParam
  }

  /**
   * @throws WdkModelException
   *   if is depended and default value conflicts with allowEmpty setting
   */
  public void checkAllowEmptyVsEmptyDefault() throws WdkModelException {
    // make sure empty param value is either valid or does not happen
    if (isInvalidEmptyDepended()) {
      String containerName = getContainer() == null
        ? "unknown"
        : getContainer().getFullName();
      String msg = "Default value for param '" + getFullName() +
          "' in question '" + containerName + "' cannot be valid " +
          "since the default value is empty but allowEmpty is false and " +
          "the param is depended on by " + getDependentParams().stream()
          .map(NamedObject::getName).collect(Collectors.joining(", "));
      if (EMPTY_DESPITE_ALLOWEMPTY_FALSE_IS_FATAL) {
        throw new WdkModelException(msg);
      }
      else {
        LOG.warn(msg);
      }
    }
  }

  protected boolean isInvalidEmptyDepended() {
    return !getDependentParams().isEmpty()
      && (_xmlDefaultValue == null || _xmlDefaultValue.isEmpty())
      && !_allowEmpty;
  }

  public String getStandardizedStableValue(String stableValue) {
    return stableValue;
  }

  public String getExternalStableValue(String stableValue) {
    return stableValue;
  }
}
