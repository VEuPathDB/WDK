package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
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
public abstract class Param extends WdkModelBase implements Cloneable {
  
  public static final String RAW_VALUE_SUFFIX = "_raw";
  public static final String INVALID_VALUE_SUFFIX = "_invalid";

  protected static Logger logger = Logger.getLogger(Param.class);

  @Override
  public abstract Param clone();
  
  public abstract String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException;

  protected abstract void applySuggection(ParamSuggestion suggest);

  /**
   * The input the method can be either raw data or dependent data
   * 
   * @param user
   * @param rawOrDependentValue
   */
  protected abstract void validateValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException;

  protected abstract void appendJSONContent(JSONObject jsParam, boolean extra) throws JSONException;

  protected String id;
  protected String name;
  protected String prompt;

  private List<WdkModelText> helps;
  protected String help;

  // both default value and empty values will be used to construct default raw value. these values themselves
  // are neither valid raw values nor stable values.
  private String defaultValue;
  private String emptyValue;

  protected boolean visible;
  protected boolean readonly;

  private Group group;

  private List<ParamSuggestion> suggestions;
  protected boolean allowEmpty;

  protected ParamSet paramSet;

  protected WdkModel wdkModel;

  private List<ParamConfiguration> noTranslations;

  /**
   * if this flag is set to true, the internal value will be the same as dependent value. This flag is useful
   * when the dependent value is sent to other sites to process using ProcessQuery.
   */
  private boolean noTranslation = false;

  protected Question contextQuestion;
  protected Query contextQuery;

  private List<ParamHandlerReference> handlerReferences;
  private ParamHandlerReference handlerReference;
  protected ParamHandler handler;

  public Param() {
    visible = true;
    readonly = false;
    group = Group.Empty();
    helps = new ArrayList<WdkModelText>();
    suggestions = new ArrayList<ParamSuggestion>();
    noTranslations = new ArrayList<ParamConfiguration>();
    allowEmpty = false;
    emptyValue = null;
    defaultValue = null;
    handlerReferences = new ArrayList<>();
  }

  public Param(Param param) {
    super(param);
    this.id = param.id;
    this.name = param.name;
    this.prompt = param.prompt;
    this.help = param.help;
    this.defaultValue = param.defaultValue;
    this.visible = param.visible;
    this.readonly = param.readonly;
    this.group = param.group;
    this.allowEmpty = param.allowEmpty;
    this.emptyValue = param.emptyValue;
    this.paramSet = param.paramSet;
    this.wdkModel = param.wdkModel;
    this.noTranslation = param.noTranslation;
    this.resolved = param.resolved;
    if (param.handlerReferences != null) {
      this.handlerReferences = new ArrayList<>();
      for (ParamHandlerReference reference : param.handlerReferences) {
        this.handlerReferences.add(new ParamHandlerReference(this, reference));
      }
    }
    this.handlerReference = param.handlerReference;
    this.handler = param.handler;
    this.contextQuestion = param.contextQuestion;
    this.contextQuery = param.contextQuery;
  }

  public WdkModel getWdkModel() {
    return wdkModel;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  void setParamSet(ParamSet paramSet) {
    this.paramSet = paramSet;
  }

  public String getFullName() {
    return paramSet.getName() + "." + name;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    if (prompt == null)
      return name;
    return prompt;
  }

  public void addHelp(WdkModelText help) {
    this.helps.add(help);
  }

  public String getHelp() {
    if (help == null)
      return getPrompt();
    return help;
  }

  void setHelp(String help) {
    this.help = help;
  }

  public void setDefault(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * @throws WdkModelException
   *           if unable to retrieve default value
   */
  public String getDefault() throws WdkModelException {
    return defaultValue;
  }

  /**
   * @return Returns the readonly.
   */
  public boolean isReadonly() {
    return this.readonly;
  }

  /**
   * @param readonly
   *          The readonly to set.
   */
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  /**
   * @return Returns the visible.
   */
  public boolean isVisible() {
    return this.visible;
  }

  /**
   * @param visible
   *          The visible to set.
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * @return the allowEmpty
   */
  public boolean isAllowEmpty() {
    return this.allowEmpty;
  }

  public void setAllowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
  }

  /**
   * @return the emptyValue
   */
  public String getEmptyValue() {
    return emptyValue;
  }

  /**
   * @param emptyValue
   *          the emptyValue to set
   */
  public void setEmptyValue(String emptyValue) {
    this.emptyValue = emptyValue;
  }

  /**
   * @return the group
   */
  public Group getGroup() {
    return group;
  }

  /**
   * @param group
   *          the group to set
   */
  public void setGroup(Group group) {
    this.group = group;
  }

  public void addSuggest(ParamSuggestion suggest) {
    this.suggestions.add(suggest);
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    String classnm = this.getClass().getName();
    StringBuffer buf = new StringBuffer(classnm + ": name='" + name + "'" + newline + "  prompt='" + prompt +
        "'" + newline + "  help='" + help + "'" + newline + "  default='" + defaultValue + "'" + newline +
        "  readonly=" + readonly + newline + "  visible=" + visible + newline + "  noTranslation=" +
        noTranslation + newline);
    if (group != null)
      buf.append("  group=" + group.getName() + newline);

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

    // exclude helps
    boolean hasHelp = false;
    for (WdkModelText help : helps) {
      if (help.include(projectId)) {
        if (hasHelp) {
          throw new WdkModelException("The param " + getFullName() + " has more than one help for project " +
              projectId);
        }
        else {
          this.help = help.getText();
          hasHelp = true;
        }
      }
    }
    helps = null;

    // exclude suggestions
    boolean hasSuggest = false;
    for (ParamSuggestion suggest : suggestions) {
      if (suggest.include(projectId)) {
        if (hasSuggest)
          throw new WdkModelException("The param " + getFullName() +
              " has more than one <suggest> for project " + projectId);

        suggest.excludeResources(projectId);
        defaultValue = suggest.getDefault();
        allowEmpty = suggest.isAllowEmpty();
        emptyValue = suggest.getEmptyValue();

        applySuggection(suggest);

        hasSuggest = true;

      }
    }
    suggestions = null;

    // exclude noTranslations
    boolean hasNoTranslation = false;
    for (ParamConfiguration noTrans : noTranslations) {
      if (noTrans.include(projectId)) {
        if (hasNoTranslation)
          throw new WdkModelException("The param " + getFullName() +
              " has more than one <noTranslation> for project " + projectId);
        noTranslation = noTrans.isValue();
        hasNoTranslation = true;
      }
    }
    noTranslations = null;

    // exclude handler references
    for (ParamHandlerReference reference : handlerReferences) {
      if (reference.include(projectId)) {
        // make sure the handler is not defined more than once
        if (handlerReference != null)
          throw new WdkModelException("param handler is defined more than " + "once for project " +
              projectId + " in param " + getFullName());
        reference.excludeResources(projectId);
        handlerReference = reference;
      }
    }
    handlerReferences = null;
  }

  public JSONObject getJSONContent(boolean extra) throws JSONException {
    JSONObject jsParam = new JSONObject();
    jsParam.put("name", getFullName());

    appendJSONContent(jsParam, extra);
    return jsParam;
  }

  /**
   * @throws WdkModelException
   *           if unable to load resources from model
   */
  public void setResources(WdkModel model) throws WdkModelException {
    this.wdkModel = model;
  }

  public final String replaceSql(String sql, String internalValue) {
    String regex = "\\$\\$" + name + "\\$\\$";
    // escape all single quotes in the value
    return sql.replaceAll(regex, Matcher.quoteReplacement(internalValue));
  }

  public void validate(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    // handle the empty case
    if (stableValue == null || stableValue.length() == 0) {
      if (!allowEmpty)
        throw new WdkModelException("The parameter '" + getPrompt() + "' does not allow empty value");
      // otherwise, got empty value and is allowed, no need for further
      // validation.
    }
    else {
      // value is not empty, the sub classes will complete further
      // validation
      validateValue(user, stableValue, contextValues);
    }
  }

  public void addNoTranslation(ParamConfiguration noTranslation) {
    this.noTranslations.add(noTranslation);
  }

  public boolean isNoTranslation() {
    return noTranslation;
  }

  public void setNoTranslation(boolean noTranslation) {
    this.noTranslation = noTranslation;
  }

  /**
   * Set the question where the param is used. The params in a question are always cloned when question is
   * initialized, therefore, each param object will refer to one question uniquely.
   * 
   * @param question
   */
  public void setContextQuestion(Question question) {
    this.contextQuestion = question;
  }

  public void setContextQuery(Query query) {
    this.contextQuery = query;
  }

  public Query getContextQuery() {
    return contextQuery;
  }

  public void setHandler(ParamHandler handler) {
    handler.setParam(this);
    this.handler = handler;
  }

  /**
   * Transform raw param value into stable value.
   * 
   * @param user
   * @param rawValue
   * @param contextValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public String getStableValue(User user, Object rawValue, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    return handler.toStableValue(user, rawValue, contextValues);
  }

  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    return handler.getStableValue(user, requestParams);
  }

  /**
   * Transform stable param value back to raw value;
   * 
   * @param user
   * @param stableValue
   * @param contextValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public Object getRawValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    return handler.toRawValue(user, stableValue, contextValues);
  }

  /**
   * Transform stable param value into internal value. The noTranslation and quote flags should be handled by
   * the plugin.
   * 
   * @param user
   * @param stableValue
   *          if the value is empty, and if empty is allow, the assigned empty value will be used as stable
   *          value to be transformed into the internal.
   * @param contextValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public String getInternalValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    if (stableValue == null || stableValue.length() == 0)
      if (isAllowEmpty())
        stableValue = getEmptyValue();

    return handler.toInternalValue(user, stableValue, contextValues);
  }

  public String getSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    return handler.toSignature(user, stableValue, contextValues);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (resolved)
      return;

    super.resolveReferences(wdkModel);

    this.wdkModel = wdkModel;

    // resolve reference for handler
    if (handlerReference != null) {
      try {
        Class<? extends ParamHandler> handlerClass = Class.forName(handlerReference.getImplementation()).asSubclass(
            ParamHandler.class);
        handler = handlerClass.newInstance();
        handler.setProperties(handlerReference.getProperties());
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        throw new WdkModelException(ex);
      }
      handlerReference = null;
    }
    if (handler == null)
      throw new WdkModelException("The param handler is not provided for param " + getFullName());

    // the handler might not be initialized from reference, it might be created
    // by the param by default.
    handler.setParam(this);
    handler.setWdkModel(wdkModel);
  }

  public Set<String> getAllValues() throws WdkModelException {
    Set<String> values = new LinkedHashSet<>();
    values.add(getDefault());
    return values;
  }

  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    handler.prepareDisplay(user, requestParams, contextValues);
  }
  
  public final void printDependency(PrintWriter writer, String indent) throws WdkModelException {
    writer.println(indent + "<" + getClass().getSimpleName() + " name=\"" + getFullName() + "\">");
    printDependencyContent(writer, indent + WdkModel.INDENT);
    writer.println(indent + "</" + getClass().getSimpleName() + ">");
  }
  
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    // by default, print nothing
  }
}
