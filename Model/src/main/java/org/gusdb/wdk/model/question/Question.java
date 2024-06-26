package org.gusdb.wdk.model.question;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.BuildTracking;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisXml;
import org.gusdb.wdk.model.analysis.StepAnalysisXml.StepAnalysisContainer;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ColumnType;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamReference;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.report.ReporterRef;
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
public class Question extends WdkModelBase implements AttributeFieldContainer, StepAnalysisContainer, NamedObject, BuildTracking {

  public static final String DYNAMIC_QUERY_SUFFIX = "_dynamic";

  protected static final Logger LOG = Logger.getLogger(Question.class);

  private String _recordClassRef;

  private String _idQueryRef;

  private String _name;

  private String _displayName;

  private String _iconName;

  private List<WdkModelText> _descriptions = new ArrayList<>();
  private String _description;

  private List<WdkModelText> _summaries = new ArrayList<>();
  private String _summary;

  private List<WdkModelText> _helps = new ArrayList<>();
  private String _help;

  private List<WdkModelText> _searchVisibleHelps = new ArrayList<>();
  private String _searchVisibleHelp;

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

  private List<AttributeList> _attributeLists = new ArrayList<>();

  private String[] _defaultSummaryAttributeNames;
  private Map<String, AttributeField> _defaultSummaryAttributeFields = new LinkedHashMap<>();
  private Map<String, Boolean> _defaultSortingMap = new LinkedHashMap<>();

  private List<DynamicAttributeSet> _dynamicAttributeSets = new ArrayList<>();
  protected DynamicAttributeSet _dynamicAttributeSet;
  private Query _dynamicAttributeQuery;

  /**
   * if set to true, if the result of the question has only 1 row, the strategy
   * workspace page will be skipped, and user is redirected to the record page
   * automatically. No strategy is created in this case. default false.
   */
  private boolean _noSummaryOnSingleRecord = false;

  /**
   * the default short name used in the step box in strategy workspace.
   */
  private String _shortDisplayName;

  private List<ParamReference> _paramRefs = new ArrayList<>();

  private List<WdkModelText> _sqlMacroList = new ArrayList<>();

  /**
   * the macros that can be used to override the same macros/paramValues in the
   * referenced id query.
   */
  private Map<String, String> _sqlMacroMap = new LinkedHashMap<>();

  private List<SummaryView> _summaryViewList = new ArrayList<>();
  private LinkedHashMap<String, SummaryView> _summaryViewMap = new LinkedHashMap<>();

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

  // /////////////////////////////////////////////////////////////////////
  // setters called at initialization
  // /////////////////////////////////////////////////////////////////////

  /**
   * default constructor used by model parser
   */
  public Question() {}

  /**
   * copy constructor
   */
  public Question(Question question) {
    super(question);
    _description = question._description;
    _displayName = question._displayName;
    _iconName = question._iconName;

    // TODO - need to deep-copy dynamicAttributeSet as well
    _dynamicAttributeSet = question._dynamicAttributeSet;

    _help = question._help;
    _searchVisibleHelp = question._searchVisibleHelp;

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

  protected static String getInternalQuestionName(String prefix, RecordClass recordClass) {
    return prefix + recordClass.getFullName().replace('.', '_');
  }

  @Override
  public String getNewBuild() {
    return _newBuild;
  }

  @Override
  public void setNewBuild(String newBuild) {
    _newBuild = newBuild;
  }

  @Override
  public String getReviseBuild() {
    return _reviseBuild;
  }

  @Override
  public void setReviseBuild(String reviseBuild) {
    _reviseBuild = reviseBuild;
  }

  public void addSuggestion(QuestionSuggestion suggestion) {
    _suggestions.add(suggestion);
  }

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public void setName(String name) {
    _name = name.trim();
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

  public void addSearchVisibleHelp(WdkModelText searchVisibleHelp) {
    _searchVisibleHelps.add(searchVisibleHelp);
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

  public Map<String, Field> getFields() {
    Map<String, Field> fields = new LinkedHashMap<>();
    Map<String, AttributeField> attributes = getAttributeFieldMap();
    Map<String, TableField> tables = _recordClass.getTableFieldMap();

    fields.putAll(attributes);
    fields.putAll(tables);
    return fields;
  }

  public Query getDynamicAttributeQuery() {
    return _dynamicAttributeQuery;
  }

  // /////////////////////////////////////////////////////////////////////

  public Param[] getParams() {
    return _query.getParams();
  }

  public Map<String, Param> getParamMap() {
    return Functions.getMapFromValues(Arrays.asList(getParams()), Named.TO_NAME);
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

  public String getSearchVisibleHelp() {
    return _searchVisibleHelp;
  }

  public String getDisplayName() {
    return _displayName != null ? _displayName : _name;
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

  @Override
  public String getName() {
    return _name;
  }

  @Override
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
    StringBuilder saNames = new StringBuilder();
    if (_recordClass != null) {
      Map<String, AttributeField> summaryFields = getAttributeFieldMap();
      for (String saName : summaryFields.keySet()) {
        saNames.append(saName + ", ");
      }
    }
    StringBuilder buf = new StringBuilder(
        "Question: name='" + _name + "'" + NL +
        "  recordClass='" + _recordClassRef + "'" + NL +
        "  query='" + _idQueryRef + "'" + NL +
        "  displayName='" + getDisplayName() + "'" + NL +
        "  customJavascript='" + getCustomJavascript() + "'" + NL +
        "  summary='" + getSummary() + "'" + NL +
        "  description='" + getDescription() + "'" + NL +
        "  summaryAttributes='" + saNames + "'" + NL +
        "  help='" + getHelp() + "'" + NL);
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
   * otherwise it only return a limited number of attribute fields for display
   * purpose.
   */
  public Map<String, AttributeField> getSummaryAttributeFieldMap() {
    Map<String, AttributeField> attributeFields = new LinkedHashMap<>();

    // always put ID as the first field
    AttributeField pkField = _recordClass.getIdAttributeField();
    attributeFields.put(pkField.getName(), pkField);

    if (_defaultSummaryAttributeFields.isEmpty()) {
      attributeFields = _recordClass.getSummaryAttributeFieldMap();
    } else {
      attributeFields.putAll(_defaultSummaryAttributeFields);
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
  public Optional<AttributeField> getAttributeField(String name) {
    return Optional.ofNullable(_recordClass.getAttributeField(name)
        .orElse(_dynamicAttributeSet.getAttributeFieldMap().get(name)));
  }

  @Override
  public Map<String, AttributeField> getAttributeFieldMap() {

    Map<String, AttributeField> attributeFields = new LinkedHashMap<>();

    // always put primary key as the first field
    AttributeField pkField = _recordClass.getIdAttributeField();
    attributeFields.put(pkField.getName(), pkField);

    attributeFields.putAll(_recordClass.getAttributeFieldMap());

    attributeFields.putAll(_dynamicAttributeSet.getAttributeFieldMap());

    return attributeFields;
  }

  public Map<String, AttributeField> getDynamicAttributeFieldMap() {
    return _dynamicAttributeSet.getAttributeFieldMap();
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    if (_resolved)
      return;
    super.resolveReferences(model);

    try {
      // it must happen before dynamicAttributeSet, because it is referenced
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
      if (!_paramRefs.isEmpty()) {
        String queryName = _query.getFullName();
        Map<String, Param> params = _query.getParamMap();
        for (ParamReference paramRef : _paramRefs) {
          String paramName = paramRef.getElementName();
          if (!params.containsKey(paramName))
            throw new WdkModelException("The paramRef [" + paramName
                + "] defined in QUESTION [" + getFullName()
                + "] doesn't exist in the referenced id query [" + queryName + "].");
          Param param = ParamReference.resolveReference(model, paramRef, _query);
          _query.addParam(param);
        }

        // resolve the param references after all params are present
        for (Param param : _query.getParams())
          param.resolveReferences(model);

        // once param references are resolved, resolve dependent params
        for (Param param : _query.getParams())
          param.resolveDependedParamRefs();

        // once both resolveRefs AND dependedParamRefs have been called, do validation check
        for (Param param : _query.getParams())
          param.checkAllowEmptyVsEmptyDefault();
      }
      _query.setContextQuestion(this);

      // all the id queries should have weight column
      _query.setHasWeight(true);

      // dynamic attribute set need to be initialized after the id query.
      _dynamicAttributeQuery = createDynamicAttributeQuery(model);
      _dynamicAttributeQuery.resolveReferences(model);
      _dynamicAttributeSet.resolveReferences(model);

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
      for (SummaryView summaryView : _summaryViewMap.values())
        summaryView.resolveReferences(model);

      // resolve step analysis refs
      for (StepAnalysis stepAnalysisRef : _stepAnalysisMap.values()) {
        StepAnalysisXml stepAnalysisXml = (StepAnalysisXml)stepAnalysisRef;
        stepAnalysisXml.setContainer(this);
        stepAnalysisXml.resolveReferences(model);
        // make sure each analysis plugin is appropriate for this question
        stepAnalysisRef.getAnalyzerInstance().validateQuestion(this);
      }

      // make sure this question's query provides columns for each part of the
      // primary key
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
   */
  @Override
  public Map<String, Boolean> getSortingAttributeMap() {
    Map<String, Boolean> map = new LinkedHashMap<>();

    for (String attrName : _defaultSortingMap.keySet()) {
      map.put(attrName, _defaultSortingMap.get(attrName));
      if (map.size() >= UserPreferences.MAX_NUM_SORTING_COLUMNS)
        break;
    }

    // no sorting map defined, use the definition in recordClass
    if (map.isEmpty())
      map = _recordClass.getSortingAttributeMap();

    return map;
  }

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

    // exclude searchVisibleHelps
    boolean hasSearchVisibleHelp = false;
    for (WdkModelText searchVisibleHelp : _searchVisibleHelps) {
      if (searchVisibleHelp.include(projectId)) {
        if (hasSearchVisibleHelp) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one searchVisibleHelp for project " + projectId);
        } else {
          _searchVisibleHelp = searchVisibleHelp.getText();
          hasSearchVisibleHelp = true;
        }
      }
    }
    _searchVisibleHelps = null;

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

  public void addParamRef(ParamReference paramRef) {
    _paramRefs.add(paramRef);
  }

  public void addSqlParamValue(WdkModelText sqlMacro) {
    _sqlMacroList.add(sqlMacro);
  }

  private static Optional<SummaryView> findFirstDefault(Map<String,SummaryView> map) {
    return map.values().stream().filter(SummaryView::isDefault).findFirst();
  }

  private static Iterable<Entry<String,SummaryView>> getTrimmedViews(
      Optional<SummaryView> defaultView, Map<String,SummaryView> allViews) {
    return () ->
      allViews.entrySet().stream().filter(view ->
        defaultView.isEmpty() ||
        !defaultView.get().getName().equals(view.getKey())
      ).iterator();
  }

  public List<SummaryView> getOrderedSummaryViews() {

    // get view maps from question and parent record class
    Map<String, SummaryView> questionViews = _summaryViewMap;
    Map<String, SummaryView> recordClassViews = _recordClass.getSummaryViews();

    // find first summary view specified as default; look in question, then recordclass
    Optional<SummaryView> first = findFirstDefault(questionViews);
    if (first.isEmpty()) first = findFirstDefault(recordClassViews);

    // add default summary view as first element; if no default specified, first summary view will be selected
    List<SummaryView> viewsToReturn = new ArrayList<>();
    first.ifPresent(viewsToReturn::add);

    // add remaining question views first
    for (var entry : getTrimmedViews(first, questionViews)) {
      viewsToReturn.add(entry.getValue());
    }

    // add remaining record class views, careful not to override question views with the same name
    for (var entry : getTrimmedViews(first, recordClassViews)) {
      if (!questionViews.containsKey(entry.getKey())) {
        viewsToReturn.add(entry.getValue());
      }
    }

    return viewsToReturn;
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
    if (!_summaryViewMap.isEmpty())
      return _summaryViewMap.values().iterator().next();
    // return the first view from record
    if (!viewsFromRecord.isEmpty())
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
    _filters.put(filter.getKey(), filter);
  }

  public void addIgnoredFilterFromRecordClass(String filterKey) {
    LOG.info("Adding filter '" + filterKey + "' to ignore list for question '" + getFullName() + "'.");
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
    return getExclusiveFilterMap(false);
  }

  /**
   * Returns a set of view filters (by name) for this question.
   *
   * @return map of all view-only filters, from filter name to filter
   */
  public Map<String, Filter> getViewFilters() {
    return getExclusiveFilterMap(true);
  }

  private Map<String, Filter> getExclusiveFilterMap(boolean viewOnly) {

    return Stream.concat(
        _recordClass.getFilters().values().stream()
            .filter(filter -> !_ignoredFiltersFromRecordClass.contains(filter.getKey())),
        _filters.values().stream()
            .filter(filter -> viewOnly == filter.getFilterType().isViewOnly())
    ).collect(Collectors.toMap(Filter::getKey, Function.identity()));
  }

  public Optional<Filter> getFilter(String filterName) {
    if (filterName == null) {
      return Optional.empty();
    }
    if (_ignoredFiltersFromRecordClass.contains(filterName)) return null;
    Filter filter = _filters.get(filterName);
    if (filter == null) {
      filter = _recordClass.getFilter(filterName);
    }
    return Optional.ofNullable(filter);
  }

  /**
   * Used when these values are patched in to this Question after the XML parsing and resolution phase is over.
   */
  public void setDefaultSummaryAttributeNames(String[] names) {
    _defaultSummaryAttributeNames = names;
  }

  public boolean isBoolean() {
    return getQuery().isBoolean();
  }

  @Override
  public String getNameForLogging() {
    return getFullName();
  }

}
