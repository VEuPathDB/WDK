package org.gusdb.wdk.model.question;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisXml;
import org.gusdb.wdk.model.analysis.StepAnalysisXml.StepAnalysisContainer;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ColumnType;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamReference;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeCategory;
import org.gusdb.wdk.model.record.attribute.AttributeCategoryTree;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.report.ReporterRef;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferences;

/**
 * A class representing a binding between a RecordClass and a Query. On the
 * website, a question is displayed in categories, and are called searches.
 * 
 * A question can override some parts of the underlying query: paramRef,
 * paramValue/sqlMacro. However, a question shouldn't introduce new paramRef or
 * paramValue that is not defined in the query.
 * 
 * A question can override some parts of referenced recordClass: it can define
 * dynamicAttributes to introduce new attributes; it can define summaryView to
 * add views (or override views of the same name) to the record type.
 * 
 * Created: Fri June 4 11:19:30 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2007-01-10 14:54:53 -0500 (Wed, 10 Jan
 *          2007) $ $Author$
 */
public class Question extends WdkModelBase implements AttributeFieldContainer, StepAnalysisContainer {

  public static final String BOOLEAN_QUESTION_PREFIX = "boolean_question_";

  private static final String DYNAMIC_QUERY_SUFFIX = "_dynamic";

  protected static final Logger LOG = Logger.getLogger(Question.class);
  
  private String _recordClassRef;

  private String _idQueryRef;

  private String _name;

  private String _displayName;
  
  private String _iconName;

  private List<WdkModelText> _descriptions = new ArrayList<WdkModelText>();
  private String _description;

  private List<WdkModelText> _summaries = new ArrayList<WdkModelText>();
  private String _summary;

  private List<WdkModelText> _helps = new ArrayList<WdkModelText>();
  private String _help;

  private QuestionSet _questionSet;

  private Query _query;

  protected RecordClass _recordClass;

  /**
   * if set to true, the result won't be paged, and all the records will be
   * displayed on the summary page. default false.
   */
  private boolean _fullAnswer = false;

  /**
   * the reference to the js file that will be included in the question page.
   */
  private String _customJavascriptFile = "";

  private List<AttributeList> _attributeLists = new ArrayList<AttributeList>();

  private String[] _defaultSummaryAttributeNames;
  private Map<String, AttributeField> _defaultSummaryAttributeFields = new LinkedHashMap<String, AttributeField>();
  private Map<String, Boolean> _defaultSortingMap = new LinkedHashMap<String, Boolean>();

  private List<DynamicAttributeSet> _dynamicAttributeSets = new ArrayList<DynamicAttributeSet>();
  protected DynamicAttributeSet _dynamicAttributeSet;
  private Query _dynamicAttributeQuery;

  /**
   * if set to true, if the result of the question has only 1 row, the strategy
   * workspace page will be skipped, and user is redirected to the record page
   * automatically. No strategy is created in this case. default false.
   */
  private boolean _noSummaryOnSingleRecord = false;

  /**
   * TODO - Not sure if it's used anywhere.
   */
  @Deprecated
  private boolean _ignoreSubType = false;

  /**
   * the default short name used in the step box in strategy workspace.
   */
  private String _shortDisplayName;

  private List<ParamReference> _paramRefs = new ArrayList<ParamReference>();

  private List<WdkModelText> _sqlMacroList = new ArrayList<WdkModelText>();
  /**
   * the macros that can be used to override the same macros/paramValues in the
   * referenced id query.
   */
  private Map<String, String> _sqlMacroMap = new LinkedHashMap<String, String>();

  private List<SummaryView> _summaryViewList = new ArrayList<>();
  private Map<String, SummaryView> _summaryViewMap = new LinkedHashMap<>();
  
  private List<StepAnalysisXml> _stepAnalysisList = new ArrayList<>();
  private Map<String, StepAnalysis> _stepAnalysisMap = new LinkedHashMap<>();

  /**
   * new build flag on what build this question is introduced.
   */
  private String _newBuild;

  /**
   * revise build flag on what build this question is revised.
   */
  private String _reviseBuild;
  
  private final Map<String, Filter> _filters = new LinkedHashMap<>();

  /**
   * Filters to on associated record class to ignore for this Question.
   */
  private final Set<String> _ignoredFiltersFromRecordClass = new HashSet<>();
  
  
  private List<QuestionSuggestion> _suggestions = new ArrayList<>();

  private String _urlSegment;


  // /////////////////////////////////////////////////////////////////////
  // setters called at initialization
  // /////////////////////////////////////////////////////////////////////

  /**
   * default constructor used by model parser
   */
  public Question() {}

  /**
   * copy constructor
   * 
   * @param question
   */
  public Question(Question question) {
    super(question);
    _description = question._description;
    _displayName = question._displayName;
    _iconName = question._iconName;

    // TODO - need to deep-copy dynamicAttributeSet as well
    _dynamicAttributeSet = question._dynamicAttributeSet;

    _help = question._help;

    // need to deep-copy query as well
    _query = question._query;
    _idQueryRef = question._idQueryRef;
    _questionSet = question._questionSet;
    _recordClass = question._recordClass;
    _recordClassRef = question._recordClassRef;
    _defaultSortingMap.putAll(question._defaultSortingMap);
    _summary = question._summary;
    _defaultSummaryAttributeFields.putAll(question._defaultSummaryAttributeFields);

    _noSummaryOnSingleRecord = question._noSummaryOnSingleRecord;
    _shortDisplayName = question._shortDisplayName;
    _customJavascriptFile = question._customJavascriptFile;

    _paramRefs = new ArrayList<>(question._paramRefs);

    if (_sqlMacroList != null)
      _sqlMacroList = new ArrayList<>(question._sqlMacroList);
    _sqlMacroMap = new LinkedHashMap<>(question._sqlMacroMap);
    _filters.putAll(new LinkedHashMap<>(question._filters));
    _ignoredFiltersFromRecordClass.addAll(new HashSet<>(question._ignoredFiltersFromRecordClass));
  }

  public String getNewBuild() {
    return _newBuild;
  }

  public void setNewBuild(String newBuild) {
    _newBuild = newBuild;
  }

  public String getReviseBuild() {
    return _reviseBuild;
  }

  public void setReviseBuild(String reviseBuild) {
    _reviseBuild = reviseBuild;
  }

  /**
   * @return if the question a newly introduced in the current build.
   */
  public boolean isNew() {
    String currentBuild = _wdkModel.getBuildNumber();
    if (currentBuild == null)
      return false; // current release is not set
    else
      return (currentBuild.equals(_newBuild));
  }

  /**
   * @return if the question is revised in the current build.
   */
  public boolean isRevised() {
    String currentBuild = _wdkModel.getBuildNumber();
    if (currentBuild == null)
      return false; // current release is not set
    else
      return (currentBuild.equals(_reviseBuild));
  }
  
  public void addSuggestion(QuestionSuggestion suggestion) {
    _suggestions.add(suggestion);
  }

  @Override
  public WdkModel getWdkModel() {
    return this._wdkModel;
  }

  public void setWdkModel(WdkModel wdkModel) {
    this._wdkModel = wdkModel;
  }

  public void setName(String name) {
    _name = name.trim();
  }

  public void setUrlName(String urlName) {
    // XML Model alias for URL segment
    setUrlSegment(urlName);
  }
  
  public void setUrlSegment(String urlSegment) {
    _urlSegment = urlSegment;
  }

  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  public void addSummary(WdkModelText summary) {
    _summaries.add(summary);
  }

  public void addHelp(WdkModelText help) {
    _helps.add(help);
  }

  public void setRecordClassRef(String recordClassRef) {
    _recordClassRef = recordClassRef;
  }

  public void setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
    _recordClassRef = recordClass.getFullName();
  }

  public void setQueryRef(String queryTwoPartName) {
    _idQueryRef = queryTwoPartName;
  }

  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  public void setIconName(String iconName) {
    _iconName = iconName;
  }

  public void setCustomJavascript(String customJavascript) {
    _customJavascriptFile = customJavascript;
  }

  public void addAttributeList(AttributeList attributeList) {
    _attributeLists.add(attributeList);
  }

  public void addDynamicAttributeSet(DynamicAttributeSet dynamicAttributes) {
    dynamicAttributes.setQuestion(this);
    _dynamicAttributeSets.add(dynamicAttributes);
  }

  public Map<String, Field> getFields(FieldScope scope) {
    Map<String, Field> fields = new LinkedHashMap<String, Field>();
    Map<String, AttributeField> attributes = getAttributeFieldMap(scope);
    Map<String, TableField> tables = _recordClass.getTableFieldMap(scope);

    fields.putAll(attributes);
    fields.putAll(tables);
    return fields;
  }

  public Query getDynamicAttributeQuery() {
    return _dynamicAttributeQuery;
  }

  // /////////////////////////////////////////////////////////////////////

  /**
   * make an answer with default page size and sorting and no filters applied
   * 
   * @param paramErrors
   * @return
   * @throws WdkUserException 
   */
  public AnswerValue makeAnswerValue(User user,
      Map<String, String> dependentValues, boolean validate, int assignedWeight)
      throws WdkModelException, WdkUserException {
    LOG.debug("makeAnswerValue() with NO FILTERS applied:  FIRST page, (will also query.makeInstance() first)");
    int pageStart = 1;
    int pageEnd = Utilities.DEFAULT_PAGE_SIZE;
    Map<String, Boolean> sortingMap = new LinkedHashMap<String, Boolean>(
        _defaultSortingMap);
    AnswerFilterInstance filter = _recordClass.getDefaultFilter();
    AnswerValue answerValue = makeAnswerValue(user, dependentValues, pageStart,
        pageEnd, sortingMap, filter, validate, assignedWeight);
    if (_fullAnswer) {
      int resultSize = answerValue.getResultSizeFactory().getResultSize();
      if (resultSize > pageEnd)
        answerValue.setPageIndex(pageStart, resultSize);
    }
    return answerValue;
  }

  /**
   * make an answer by given page range, sorted by the given attribute list.
   * 
   * @param paramErrors
   * @param i
   * @param j
   * @param sortingAttributes
   * @return
   * @throws WdkUserException 
   */
  public AnswerValue makeAnswerValue(User user,
      Map<String, String> dependentValues, int pageStart, int pageEnd,
      Map<String, Boolean> sortingAttributes, AnswerFilterInstance filter,
      boolean validate, int assignedWeight) throws WdkModelException, WdkUserException {
    LOG.debug("makeAnswerValue() any page, (will also query.makeInstance() first)");
    Map<String, String> context = new LinkedHashMap<String, String>();
    context.put(Utilities.QUERY_CTX_QUESTION, getFullName());

    QueryInstance<?> qi = _query.makeInstance(user, dependentValues, validate,
        assignedWeight, context);
    AnswerValue answerValue = new AnswerValue(user, this, qi, pageStart,
        pageEnd, sortingAttributes, filter);

    return answerValue;
  }

  public Param[] getParams() {
    return _query.getParams();
  }

  public Map<String, Param> getParamMap() {
    return _query.getParamMap();
  }

  public Map<Group, Map<String, Param>> getParamMapByGroups() {
    Param[] params = _query.getParams();
    Map<Group, Map<String, Param>> paramGroups = new LinkedHashMap<Group, Map<String, Param>>();
    for (Param param : params) {
      Group group = param.getGroup();
      Map<String, Param> paramGroup;
      if (paramGroups.containsKey(group)) {
        paramGroup = paramGroups.get(group);
      } else {
        paramGroup = new LinkedHashMap<String, Param>();
        paramGroups.put(group, paramGroup);
      }
      paramGroup.put(param.getName(), param);
    }
    return paramGroups;
  }

  public Map<Group, Map<String, Param>> getParamMapByGroups(String displayType) {
    Param[] params = _query.getParams();
    Map<Group, Map<String, Param>> paramGroups = new LinkedHashMap<Group, Map<String, Param>>();
    for (Param param : params) {
      Group group = param.getGroup();
      if (!group.getDisplayType().equalsIgnoreCase(displayType))
        continue;
      Map<String, Param> paramGroup;
      if (paramGroups.containsKey(group)) {
        paramGroup = paramGroups.get(group);
      } else {
        paramGroup = new LinkedHashMap<String, Param>();
        paramGroups.put(group, paramGroup);
      }
      paramGroup.put(param.getName(), param);
    }
    return paramGroups;
  }

  public String getDescription() {
    return _description;
  }

  public String getSummary() {
    return _summary;
  }

  public String getHelp() {
    return _help;
  }

  public String getDisplayName() {
    if (_displayName == null)
      _displayName = getFullName();
    return _displayName;
  }

  public String getIconName() {
    return _iconName;
  }

  public String getCustomJavascript() {
    return _customJavascriptFile;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public String getRecordClassName() {
    return _recordClassRef;
  }

  public Map<String, ReporterRef> getReporterMap() {
    Map<String, ReporterRef> reporterMap = new LinkedHashMap<>();
    // Add record class reporters first, reporters from questions second. This way question reporters
    // will override record class reporters.
    reporterMap.putAll(_recordClass.getReporterMap());
    for (AttributeField dynAttr : _dynamicAttributeSet.getAttributeFieldMap().values()) {
      reporterMap.putAll(dynAttr.getReporters());
    }
    return reporterMap;
}

  public Query getQuery() {
    return _query;
  }

    /**
     * Instead of calling this, call setQueryRef() and resolveReferences()
     */
    @Deprecated
    public void setQuery(Query q) throws WdkModelException {
    _query = q;
    _idQueryRef = q.getFullName();
    _query.setContextQuestion(this);
  }

  public String getName() {
    return _name;
  }
  
  public String getUrlSegment() {
    return _urlSegment;
  }

  public String getFullName() {
    if (_questionSet == null)
      return _name;
    else
      return _questionSet.getName() + "." + _name;
  }

  public String getQuestionSetName() {
    return _questionSet.getName();
  }

  public String getQueryName() {
    return _query.getName();
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");

    StringBuffer saNames = new StringBuffer();
    if (_recordClass != null) {
      Map<String, AttributeField> summaryFields = getAttributeFieldMap(FieldScope.NON_INTERNAL);
      for (String saName : summaryFields.keySet()) {
        saNames.append(saName + ", ");
      }
    }
    StringBuffer buf = new StringBuffer(
        "Question: name='" + _name + "'" + newline +
        "  recordClass='" + _recordClassRef + "'" + newline +
        "  query='" + _idQueryRef + "'" + newline +
        "  displayName='" + getDisplayName() + "'" + newline +
        "  customJavascript='" + getCustomJavascript() + "'" + newline +
        "  summary='" + getSummary() + "'" + newline +
        "  description='" + getDescription() + "'" + newline +
        "  summaryAttributes='" + saNames + "'" + newline +
        "  help='" + getHelp() + "'" + newline);
    if (_dynamicAttributeSet != null)
      buf.append(_dynamicAttributeSet.toString());
    return buf.toString();
  }

  public boolean isDynamic() {
    return _dynamicAttributeSet != null;
  }

    public DynamicAttributeSet getDynamicAttributeSet() {
  return _dynamicAttributeSet;
    }

  /**
   * A indicator to the controller whether this question should make answers
   * that contains all records in one page or not.
   * 
   * @return the fullAnswer
   */
  public boolean isFullAnswer() {
    return _fullAnswer;
  }

  /**
   * Set the indicator to the controller that suggests this question to make
   * answers containing all records in one page, or not.
   * 
   * @param fullAnswer
   *          the fullAnswer to set
   */
  public void setFullAnswer(boolean fullAnswer) {
    _fullAnswer = fullAnswer;
  }

  /**
   * if true, when the result has only one record, we will skip the results
   * page and redirect user to the record page.
   * 
   * @return the noSummaryOnSingleRecord
   */
  public boolean isNoSummaryOnSingleRecord() {
    return _noSummaryOnSingleRecord;
  }

  /**
   * @param noSummaryOnSingleRecord
   *          the noSummaryOnSingleRecord to set
   */
  public void setNoSummaryOnSingleRecord(boolean noSummaryOnSingleRecord) {
    _noSummaryOnSingleRecord = noSummaryOnSingleRecord;
  }

  // /////////////////////////////////////////////////////////////////////
  // package methods
  // /////////////////////////////////////////////////////////////////////

  Map<String, AttributeField> getDynamicAttributeFields() {
    return _dynamicAttributeSet.getAttributeFieldMap();
  }

  /**
   * The difference between this method and getAttribute(SUMMARY) is that the
   * getAttribute(SUMMARY) will get the configured summary list, and if the list
   * is not configured, it will return all non-internal attribute fields;
   * meanwhile this method returns the configured list if it is configured,
   * otherwise it only return a limited number of attribtue fields for display
   * purpose.
   * 
   * @return
   */
  public Map<String, AttributeField> getSummaryAttributeFieldMap() {
    Map<String, AttributeField> attributeFields = new LinkedHashMap<String, AttributeField>();

    // always put ID as the first field
    AttributeField pkField = _recordClass.getIdAttributeField();
    attributeFields.put(pkField.getName(), pkField);

    if (_defaultSummaryAttributeFields.size() > 0) {
      attributeFields.putAll(_defaultSummaryAttributeFields);
    } else {
      attributeFields = _recordClass.getSummaryAttributeFieldMap();
    }

    // add weight to the list
    // for now, it is commented out, maybe re-activated in the future.
    // Map<String, AttributeField> dynamicFields =
    // dynamicAttributeSet.getAttributeFieldMap();
    // AttributeField weightField =
    // dynamicFields.get(Utilities.COLUMN_WEIGHT);
    // attributeFields.put(weightField.getName(), weightField);

    return attributeFields;
  }

  @Override
  public Map<String, AttributeField> getAttributeFieldMap() {
    return getAttributeFieldMap(FieldScope.ALL);
  }

  @Override
  public AttributeField[] getAttributeFields() {
    AttributeField[] array = {};
    return getAttributeFieldMap(FieldScope.ALL).values().toArray(array);
  }

  public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope) {
    Map<String, AttributeField> attributeFields = new LinkedHashMap<String, AttributeField>();

    // always put primary key as the first field
    AttributeField pkField = _recordClass.getIdAttributeField();
    attributeFields.put(pkField.getName(), pkField);

    attributeFields.putAll(_recordClass.getAttributeFieldMap(scope));

    attributeFields.putAll(_dynamicAttributeSet.getAttributeFieldMap(scope));

    return attributeFields;
  }

  public Map<String, AttributeField> getDynamicAttributeFieldMap(FieldScope scope) {
    return _dynamicAttributeSet.getAttributeFieldMap(scope);
  }
  
  public AttributeCategoryTree getAttributeCategoryTree(FieldScope scope)
      throws WdkModelException {

    // get trimmed copy of category tree
    AttributeCategoryTree tree = _recordClass.getAttributeCategoryTree(scope);

    // integrate dynamic attributes into tree as first root node
    AttributeCategory dynamic = new AttributeCategory();
    dynamic.setName("dynamic");
    dynamic.setDisplayName("Search-Specific");
    for (AttributeField field : _dynamicAttributeSet.getAttributeFieldMap(scope).values()) {
      if (field.getName().equals(Utilities.COLUMN_WEIGHT)) {
        tree.addAttributeToCategories(field);
      } else {
        dynamic.addField(field);
      }
    }
    if (!dynamic.getFields().isEmpty()) {
      tree.prependAttributeCategory(dynamic);
    }

    return tree;
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    if (_resolved)
      return;
    super.resolveReferences(model);
    this._wdkModel = model;

    try {
      // it must happen before dynamicAttributeSet, because it is
      // referenced
      // in the dynamicAttributeSet.
      _recordClass = (RecordClass) model.resolveReference(_recordClassRef);

      // the id query is always cloned to keep a reference to the
      // question.
      _query = (Query) model.resolveReference(_idQueryRef);
      _query = _query.clone();

      // check if we have customized sqlMacros
      if (_query instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) _query;
        for (String macro : _sqlMacroMap.keySet()) {
          sqlQuery.addSqlParamValue(macro, _sqlMacroMap.get(macro));
        }
      }

      // check if we have customized params;
      if (_paramRefs.size() > 0) {
        String queryName = _query.getFullName();
        Map<String, Param> params = _query.getParamMap();
        for (ParamReference paramRef : _paramRefs) {
          String paramName = paramRef.getElementName();
          if (!params.containsKey(paramName))
            throw new WdkModelException("The paramRef [" + paramName
                + "] defined in QUESTION [" + getFullName()
                + "] doesn't exist in the " + "referenced id query ["
                + queryName + "].");
          Param param = ParamReference.resolveReference(model, paramRef, _query);
          _query.addParam(param);
        }
        // resolve the param references after all params are present
        for (Param param : _query.getParams()) {
          param.resolveReferences(model);
        }
      }
      _query.setContextQuestion(this);

      // all the id queries should have weight column
      _query.setHasWeight(true);

      // dynamic attribute set need to be initialized after the id query.
      _dynamicAttributeQuery = createDynamicAttributeQuery(model);
      _dynamicAttributeQuery.resolveReferences(model);
      _dynamicAttributeSet.resolveReferences(model);

      // make sure we always display weight for combined question
      // if (query.isCombined()) {
      // AttributeField weight = dynamicAttributeSet
      // .getAttributeFieldMap().get(Utilities.COLUMN_WEIGHT);
      // weight.setRemovable(false);
      // }

      // resolve default summary attributes
      if (_defaultSummaryAttributeNames != null) {
        Map<String, AttributeField> attributeFields = getAttributeFieldMap();
        for (String fieldName : _defaultSummaryAttributeNames) {
          AttributeField field = attributeFields.get(fieldName);
          if (field == null)
            throw new WdkModelException("Summary attribute field [" + fieldName
                + "] defined in question [" + getFullName() + "] is invalid.");
          _defaultSummaryAttributeFields.put(fieldName, field);
        }
      }
      _defaultSummaryAttributeNames = null;

      // make sure we create index on primary keys
      _query.setIndexColumns(_recordClass.getIndexColumns());

      // resolve summary views
      for (SummaryView summaryView : _summaryViewMap.values()) {
        summaryView.resolveReferences(model);
      }
      
      // resolve step analysis refs
      for (StepAnalysis stepAnalysisRef : _stepAnalysisMap.values()) {
        StepAnalysisXml stepAnalysisXml = (StepAnalysisXml)stepAnalysisRef;
        stepAnalysisXml.setContainer(this);
        stepAnalysisXml.resolveReferences(model);
        // make sure each analysis plugin is appropriate for this question
        stepAnalysisRef.getAnalyzerInstance().validateQuestion(this);
      }

      // generate URL segment for this question since optional in XML model
      if (_urlSegment == null || _urlSegment.isEmpty()) {
        setUrlSegment(_name);
      }

      // register this URL segment with the model to ensure uniqueness
      _wdkModel.registerQuestionUrlSegment(_urlSegment, getFullName());

      // make sure this question's query provides columns for each part of the primary key
      Set<String> queryColumnNames = _query.getColumnMap().keySet();
      String[] requiredColumns = _recordClass.getPrimaryKeyDefinition().getColumnRefs();
      for (String requiredColumn : requiredColumns) {
        if (!queryColumnNames.contains(requiredColumn)) {
          throw new WdkModelException("Question '" + getFullName() + "' refers to query '" +
              _query.getFullName() + "' which does not contain column '"  + requiredColumn +
              "' required by record class '" + _recordClass.getFullName() + "'.");
        }
      }

    }
    catch (WdkModelException ex) {
      LOG.error("resolving question '" + getFullName() + " failed. " + ex);
      throw ex;
    }

    _resolved = true;
  }

  public void setQuestionSet(QuestionSet questionSet) {
    _questionSet = questionSet;
  }

  // /////////////////////////////////////////////////////////////////////
  // Protected Methods
  // /////////////////////////////////////////////////////////////////////

  /**
   * This method is use to clone the question, excluding dynamic attributes
   * 
   * @return
   */
  @Override
  public Map<String, Boolean> getSortingAttributeMap() {
    Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();

    for (String attrName : _defaultSortingMap.keySet()) {
      map.put(attrName, _defaultSortingMap.get(attrName));
      if (map.size() >= UserPreferences.MAX_NUM_SORTING_COLUMNS)
        break;
    }

    // no sorting map defined, use the definition in recordClass
    if (map.size() == 0)
      map = _recordClass.getSortingAttributeMap();

    return map;
  }

  /**
   * @return the ignoreSubType
   */
  @Deprecated
  public boolean isIgnoreSubType() {
    return _ignoreSubType;
  }

  /**
   * @param ignoreSubType
   *          the ignoreSubType to set
   */
  @Deprecated
  public void setIgnoreSubType(boolean ignoreSubType) {
    _ignoreSubType = ignoreSubType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude descriptions
    boolean hasDescription = false;
    for (WdkModelText description : _descriptions) {
      if (description.include(projectId)) {
        if (hasDescription) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one description for project " + projectId);
        } else {
          _description = description.getText();
          hasDescription = true;
        }
      }
    }
    _descriptions = null;

    // exclude summaries
    boolean hasSummary = false;
    for (WdkModelText summ : _summaries) {
      if (summ.include(projectId)) {
        if (hasSummary) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one summary for project " + projectId);
        } else {
          _summary = summ.getText();
          hasSummary = true;
        }
      }
    }
    _summaries = null;

    // exclude helps
    boolean hasHelp = false;
    for (WdkModelText help : _helps) {
      if (help.include(projectId)) {
        if (hasHelp) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one help for project " + projectId);
        } else {
          _help = help.getText();
          hasHelp = true;
        }
      }
    }
    _helps = null;

    // exclude summary and sorting attribute list
    boolean hasAttributeList = false;
    for (AttributeList attributeList : _attributeLists) {
      if (attributeList.include(projectId)) {
        if (hasAttributeList) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one <attributesList> for " + "project "
              + projectId);
        } else {
          _defaultSummaryAttributeNames = attributeList.getSummaryAttributeNames();
          _defaultSortingMap = attributeList.getSortingAttributeMap();
          hasAttributeList = true;
        }
      }
    }
    _attributeLists = null;

    // exclude dynamic attribute set
    boolean hasDynamicAttributes = false;
    for (DynamicAttributeSet dynamicAttributeSet : _dynamicAttributeSets) {
      if (dynamicAttributeSet.include(projectId)) {
        if (hasDynamicAttributes) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one <dynamicAttributes> for " + "project "
              + projectId);
        } else {
          dynamicAttributeSet.excludeResources(projectId);
          _dynamicAttributeSet = dynamicAttributeSet;
          hasDynamicAttributes = true;
        }
      }
    }
    _dynamicAttributeSets = null;

    // add weight as an attribute
    if (_dynamicAttributeSet == null) {
      DynamicAttributeSet dynamicSet = new DynamicAttributeSet();
      dynamicSet.setQuestion(this);
      dynamicSet.excludeResources(projectId);
      _dynamicAttributeSet = dynamicSet;
    }

    // exclude param refs
    for (int i = _paramRefs.size() - 1; i >= 0; i--) {
      ParamReference paramRef = _paramRefs.get(i);
      if (paramRef.include(projectId)) {
        paramRef.excludeResources(projectId);
      } else {
        _paramRefs.remove(i);
      }
    }

    // exclude sql macros
    for (WdkModelText macro : _sqlMacroList) {
      if (macro.include(projectId)) {
        macro.excludeResources(projectId);
        String name = macro.getName();
        if (_sqlMacroMap.containsKey(name))
          throw new WdkModelException("The macro " + name
              + " is duplicated in question " + getFullName());

        _sqlMacroMap.put(macro.getName(), macro.getText());
      }
    }
    _sqlMacroList = null;

    // exclude the summary views
    for (SummaryView view : _summaryViewList) {
      if (view.include(projectId)) {
        view.excludeResources(projectId);
        String name = view.getName();
        if (_summaryViewMap.containsKey(name))
          throw new WdkModelException("The summary view '" + name
              + "' is duplicated in question " + getFullName());

        _summaryViewMap.put(name, view);
      }
    }
    _summaryViewList = null;
    
    // exclude step analyses
    for (StepAnalysisXml analysis : _stepAnalysisList) {
      if (analysis.include(projectId)) {
        analysis.excludeResources(projectId);
        String name = analysis.getName();
        if (_stepAnalysisMap.containsKey(name)) {
          throw new WdkModelException("The step analysis '" + name
              + "' is duplicated in question " + getFullName());
        }
        _stepAnalysisMap.put(name, analysis);
      }
    }
    _stepAnalysisList = null;
    
    // excluding suggestions
    for (QuestionSuggestion suggestion : _suggestions) {
      if (suggestion.include(projectId)) {
        suggestion.excludeResources(projectId);
        _newBuild = suggestion.getNewBuild();
        _reviseBuild = suggestion.getReviseBuild();
      }
    }
  }

  /**
   * If dynamic attributes are defined, a new attribute query will be created
   * which includes all the dynamic attributes.
   * 
   * @param wdkModel
   * @return
   */
  private Query createDynamicAttributeQuery(WdkModel wdkModel)
      throws WdkModelException {
    SqlQuery query = new SqlQuery();
    query.setIsCacheable(false);
    query.setName(_query.getName() + DYNAMIC_QUERY_SUFFIX);
    // put the dynamic query into the same query set of the id query.
    QuerySet querySet = _query.getQuerySet();
    querySet.addQuery(query);

    // set the columns, which as the same column as the id query
    boolean hasWeight = false;
    for (Column column : _query.getColumns()) {
      query.addColumn(new Column(column));
      if (column.getName().equals(Utilities.COLUMN_WEIGHT))
        hasWeight = true;
    }
    if (!hasWeight) {
      // create and add the weight column
      query.addColumn(createDynamicNumberColumn(Utilities.COLUMN_WEIGHT));
    }

    // create and add columns for answer param values
    for (AnswerParam param : AnswerParam.getExposedParams(getParamMap().values())) {
      String paramName = param.getName();
      try {
        query.addColumn(createDynamicNumberColumn(paramName));
      }
      catch (WdkModelException e) {
        throw new WdkModelException("Question '" + _name + "' contains answer param [" +
            paramName + "] with the same name as one of its query's columns.");
      }
    }

    // dynamic query doesn't have sql defined, here just fill in the stub
    // sql; the real sql will be constructed by answerValue
    query.setSql("");

    query.excludeResources(wdkModel.getProjectId());

    return query;
  }

  private static Column createDynamicNumberColumn(String name) {
    Column column = new Column();
    column.setName(name);
    column.setType(ColumnType.NUMBER);
    column.setWidth(12);
    return column;
  }

  public void setShortDisplayName(String shortDisplayName) {
    _shortDisplayName = shortDisplayName;
  }

  /**
   * the default short name used in the step box in strategy workspace.
   */
  public String getShortDisplayName() {
    return (_shortDisplayName == null) ? getDisplayName() : _shortDisplayName;
  }

  public AnswerParam[] getTransformParams(RecordClass recordClass) {
    List<AnswerParam> list = new ArrayList<AnswerParam>();
    String rcName = recordClass.getFullName();
    for (Param param : _query.getParams()) {
      if (param instanceof AnswerParam) {
        AnswerParam answerParam = (AnswerParam) param;
        Map<String, RecordClass> recordClasses = answerParam.getRecordClasses();
        if (recordClasses.containsKey(rcName))
          list.add(answerParam);
      }
    }
    AnswerParam[] array = new AnswerParam[list.size()];
    list.toArray(array);
    return array;
  }

  public void addParamRef(ParamReference paramRef) {
    _paramRefs.add(paramRef);
  }

  public void addSqlParamValue(WdkModelText sqlMacro) {
    _sqlMacroList.add(sqlMacro);
  }

  public Map<String, SummaryView> getSummaryViews() {
    Map<String, SummaryView> map = new LinkedHashMap<>(_summaryViewMap);
    // get views from record
    Map<String, SummaryView> recordMap = _recordClass.getSummaryViews();

    // don't override the views defined in the question
    for (String name : recordMap.keySet()) {
      if (!map.containsKey(name)) {
        map.put(name, recordMap.get(name));
      }
    }

    return map;
  }

  public SummaryView getSummaryView(String viewName) throws WdkUserException {
    SummaryView view = _summaryViewMap.get(viewName);
    if (view != null)
      return view;

    return _recordClass.getSummaryView(viewName);
  }

  public SummaryView getDefaultSummaryView() {
    // first look for default in the views defined in question
    for (SummaryView view : _summaryViewMap.values()) {
      if (view.isDefault())
        return view;
    }
    // then look for the default in the views from record
    Map<String, SummaryView> viewsFromRecord = _recordClass.getSummaryViews();
    for (SummaryView view : viewsFromRecord.values()) {
      if (view.isDefault())
        return view;
    }
    // return the first view from question
    if (_summaryViewMap.size() > 0)
      return _summaryViewMap.values().iterator().next();
    // return the first view from record
    if (viewsFromRecord.size() > 0)
      return viewsFromRecord.values().iterator().next();

    return null;
  }

  public void addSummaryView(SummaryView view) {
    if (_summaryViewList == null) {
      // method called after model parsing
      _summaryViewMap.put(view.getName(), view);
    } else {
      _summaryViewList.add(view);
    }
  }

  public Map<String, StepAnalysis> getStepAnalyses() {
    Map<String, StepAnalysis> map = new LinkedHashMap<>(_stepAnalysisMap);
    // get values from record
    Map<String, StepAnalysis> recordMap = _recordClass.getStepAnalyses();

    // don't override the values defined in the question
    for (String name : recordMap.keySet()) {
      if (!map.containsKey(name)) {
        map.put(name, recordMap.get(name));
      }
    }

    return map;
  }

  public StepAnalysis getStepAnalysis(String name) throws WdkUserException {
    StepAnalysis sa = _stepAnalysisMap.get(name);
    if (sa != null)
      return sa;

    return _recordClass.getStepAnalysis(name);
  }

  public void addStepAnalysis(StepAnalysisXml analysis) {
    _stepAnalysisList.add(analysis);
  }

  public Map<String, SearchCategory> getCategories(String usedBy, boolean strict) {
    Map<String, SearchCategory> categories = _wdkModel.getCategories(usedBy, strict);
    Map<String, SearchCategory> map = new LinkedHashMap<>();
    String questionName = getFullName();
    for (String name : categories.keySet()) {
      SearchCategory category = categories.get(name);
      if (category.hasQuestion(questionName, usedBy)) {
        map.put(name, category);
      }
    }
    return map;
  }

  public final void printDependency(PrintWriter writer, String indent) throws WdkModelException {
    writer.println(indent + "<question name=\"" + getName() + "\" recordClass=\"" + _recordClass.getFullName() + "\">");
    String indent1 = indent + WdkModel.INDENT;
    String indent2 = indent1 + WdkModel.INDENT;
    
    // print dynamic attributes
    if (_dynamicAttributeSet != null) {
      Map<String, AttributeField> attributes = _dynamicAttributeSet.getAttributeFieldMap();
      writer.println(indent1 + "<dynamicAttributes size=\"" + attributes.size() + "\">");
      String[] attributeNames = attributes.keySet().toArray(new String[0]);
      Arrays.sort(attributeNames);
      for (String attributeName : attributeNames) {
        attributes.get(attributeName).printDependency(writer, indent2);
      }
      writer.println(indent1 + "</dynamicAttributes>");
    }
    
    // print query
    _query.printDependency(writer, indent);
    writer.print(indent + "</question>");
  }
  
  // used to set question specific filters
  public void addFilter(Filter filter) {
    LOG.debug("QUESTION: ADDING FILTER: " + filter.getKey() + " for question: " + getFullName() + "\n");
    _filters.put(filter.getKey(), filter);
  }

  public void addIgnoredFilterFromRecordClass(String filterKey) {
    LOG.debug("QUESTION: ADDING FILTER TO IGNORE LIST: " + filterKey + " for question " + getFullName() + "\n");
    _ignoredFiltersFromRecordClass.add(filterKey);
  }

  /**
   * Returns a set of filters (by name) for this question.  Only non-view-only
   * filters are included in this list.  View-only filters are available via
   * getViewFilters() or by name.
   * 
   * @return map of all non-view-only filters, from filter name to filter
   */
  public Map<String, Filter> getFilters() {
    LOG.debug("QUESTION: GETTING ALL FILTERs");
    return getExclusiveFilterMap(false);
  }

  /**
   * Returns a set of view filters (by name) for this question.
   * 
   * @return map of all non-view-only filters, from filter name to filter
   */
  public Map<String, Filter> getViewFilters() {
    LOG.debug("QUESTION: GETTING VIEW FILTERs");
    return getExclusiveFilterMap(true);
  }

  private Map<String, Filter> getExclusiveFilterMap(boolean viewOnly) {
    return Stream.concat(
        _recordClass.getFilters().values().stream()
            .filter(filter -> !_ignoredFiltersFromRecordClass.contains(filter.getKey())),
        _filters.values().stream()
            .filter(filter -> viewOnly == filter.getIsViewOnly())
    ).collect(Collectors.toMap(Filter::getKey, Function.identity()));
  }

  public Filter getFilter(String filterName) throws WdkModelException {
    Filter filter = getFilterOrNull(filterName);
    if (filter == null) throw new WdkModelException("Can't find filter '" + filterName + "' in question " + getFullName());
    return filter;
  }
  
  public Filter getFilterOrNull(String filterName) throws WdkModelException {
    if (_ignoredFiltersFromRecordClass.contains(filterName)) return null;
    Filter filter = _filters.get(filterName);
    if (filter == null) filter = _recordClass.getFilter(filterName); 
    return filter;
  }

  /**
   * Used when these values are patched in to this Question after the XML parsing and resolution phase is over.
   */
  public void setDefaultSummaryAttributeNames(String[] names) {
    _defaultSummaryAttributeNames = names;
  }

  public boolean isTransform() {
    return getQuery().isTransform();
  }

  public boolean isCombined() {
    return getQuery().isCombined();
  }

  public boolean isBoolean() {
    return getQuery().isBoolean();
  }

  @Override
  public String getNameForLogging() {
    return getFullName();
  }
}
