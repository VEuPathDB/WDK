package org.gusdb.wdk.model.question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.SummaryView;
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
import org.gusdb.wdk.model.user.User;

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
public class Question extends WdkModelBase implements AttributeFieldContainer {

  public static final String BOOLEAN_QUESTION_PREFIX = "boolean_question_";

  private static final String DYNAMIC_QUERY_SUFFIX = "_dynamic";

  protected static final Logger logger = Logger.getLogger(Question.class);
  
  private String recordClassRef;

  private String idQueryRef;

  private String name;

  private String displayName;

  private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
  private String description;

  private List<WdkModelText> summaries = new ArrayList<WdkModelText>();
  private String summary;

  private List<WdkModelText> helps = new ArrayList<WdkModelText>();
  private String help;

  private QuestionSet questionSet;

  private Query query;

  protected RecordClass recordClass;

  /**
   * if set to true, the result won't be paged, and all the records will be
   * displayed on the summary page. default false.
   */
  private boolean fullAnswer = false;

  /**
   * the reference to the js file that will be included in the question page.
   */
  private String customJavascriptFile = "";

  private List<AttributeList> attributeLists = new ArrayList<AttributeList>();

  private String[] defaultSummaryAttributeNames;
  private Map<String, AttributeField> defaultSummaryAttributeFields = new LinkedHashMap<String, AttributeField>();
  private Map<String, Boolean> defaultSortingMap = new LinkedHashMap<String, Boolean>();

  private List<DynamicAttributeSet> dynamicAttributeSets = new ArrayList<DynamicAttributeSet>();
  private DynamicAttributeSet dynamicAttributeSet;
  private Query dynamicAttributeQuery;

  private WdkModel wdkModel;

  /**
   * if set to true, if the result of the question has only 1 row, the strategy
   * workspace page will be skipped, and user is redirected to the record page
   * automatically. No strategy is created in this case. default false.
   */
  private boolean noSummaryOnSingleRecord = false;

  /**
   * TODO - Not sure if it's used anywhere.
   */
  @Deprecated
  private boolean ignoreSubType = false;

  /**
   * the default short name used in the step box in strategy workspace.
   */
  private String shortDisplayName;

  private List<ParamReference> paramRefs = new ArrayList<ParamReference>();

  private List<WdkModelText> sqlMacroList = new ArrayList<WdkModelText>();
  /**
   * the macros that can be used to override the same macros/paramValues in the
   * referenced id query.
   */
  private Map<String, String> sqlMacroMap = new LinkedHashMap<String, String>();

  private List<SummaryView> summaryViewList = new ArrayList<SummaryView>();
  private Map<String, SummaryView> summaryViewMap = new LinkedHashMap<String, SummaryView>();

  /**
   * new build flag on what build this question is introduced.
   */
  private String newBuild;

  /**
   * revise build flag on what build this question is revised.
   */
  private String reviseBuild;

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
    this.description = question.description;
    this.displayName = question.displayName;

    // TODO - need to deep-copy dynamicAttributeSet as well
    this.dynamicAttributeSet = question.dynamicAttributeSet;

    this.help = question.help;

    // need to deep-copy query as well
    this.query = question.query;
    this.idQueryRef = question.idQueryRef;
    this.questionSet = question.questionSet;
    this.recordClass = question.recordClass;
    this.recordClassRef = question.recordClassRef;
    this.defaultSortingMap.putAll(question.defaultSortingMap);
    this.summary = question.summary;
    this.defaultSummaryAttributeFields.putAll(question.defaultSummaryAttributeFields);
    this.wdkModel = question.wdkModel;

    this.noSummaryOnSingleRecord = question.noSummaryOnSingleRecord;
    this.shortDisplayName = question.shortDisplayName;
    this.customJavascriptFile = question.customJavascriptFile;

    this.paramRefs = new ArrayList<ParamReference>(question.paramRefs);

    if (sqlMacroList != null)
      this.sqlMacroList = new ArrayList<WdkModelText>(question.sqlMacroList);
    this.sqlMacroMap = new LinkedHashMap<String, String>(question.sqlMacroMap);
  }

  public String getNewBuild() {
    return newBuild;
  }

  public void setNewBuild(String newBuild) {
    this.newBuild = newBuild;
  }

  public String getReviseBuild() {
    return reviseBuild;
  }

  public void setReviseBuild(String reviseBuild) {
    this.reviseBuild = reviseBuild;
  }

  /**
   * @return if the question a newly introduced in the current build.
   */
  public boolean isNew() {
    String currentBuild = wdkModel.getBuildNumber();
    if (currentBuild == null)
      return false; // current release is not set
    else
      return (currentBuild.equals(newBuild));
  }

  /**
   * @return if the question is revised in the current build.
   */
  public boolean isRevised() {
    String currentBuild = wdkModel.getBuildNumber();
    if (currentBuild == null)
      return false; // current release is not set
    else
      return (currentBuild.equals(reviseBuild));
  }

  @Override
  public WdkModel getWdkModel() {
    return this.wdkModel;
  }

  public void setWdkModel(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addDescription(WdkModelText description) {
    this.descriptions.add(description);
  }

  public void addSummary(WdkModelText summary) {
    this.summaries.add(summary);
  }

  public void addHelp(WdkModelText help) {
    this.helps.add(help);
  }

  public void setRecordClassRef(String recordClassRef) {
    this.recordClassRef = recordClassRef;
  }

  public void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
    this.recordClassRef = recordClass.getFullName();
  }

  public void setQueryRef(String queryTwoPartName) {
    this.idQueryRef = queryTwoPartName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setCustomJavascript(String customJavascript) {
    this.customJavascriptFile = customJavascript;
  }

  public void addAttributeList(AttributeList attributeList) {
    this.attributeLists.add(attributeList);
  }

  public void addDynamicAttributeSet(DynamicAttributeSet dynamicAttributes) {
    dynamicAttributes.setQuestion(this);
    this.dynamicAttributeSets.add(dynamicAttributes);
  }

  public Map<String, Field> getFields(FieldScope scope) {
    Map<String, Field> fields = new LinkedHashMap<String, Field>();
    Map<String, AttributeField> attributes = getAttributeFieldMap(scope);
    Map<String, TableField> tables = recordClass.getTableFieldMap(scope);

    fields.putAll(attributes);
    fields.putAll(tables);
    return fields;
  }

  public Query getDynamicAttributeQuery() {
    return dynamicAttributeQuery;
  }

  // /////////////////////////////////////////////////////////////////////

  /**
   * make an answer with default page size
   * 
   * @param paramErrors
   * @return
   */
  public AnswerValue makeAnswerValue(User user,
      Map<String, String> dependentValues, boolean validate, int assignedWeight)
      throws WdkModelException {
    int pageStart = 1;
    int pageEnd = Utilities.DEFAULT_PAGE_SIZE;
    Map<String, Boolean> sortingMap = new LinkedHashMap<String, Boolean>(
        defaultSortingMap);
    AnswerFilterInstance filter = recordClass.getDefaultFilter();
    AnswerValue answerValue = makeAnswerValue(user, dependentValues, pageStart,
        pageEnd, sortingMap, filter, validate, assignedWeight);
    if (this.fullAnswer) {
      int resultSize = answerValue.getResultSize();
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
   */
  public AnswerValue makeAnswerValue(User user,
      Map<String, String> dependentValues, int pageStart, int pageEnd,
      Map<String, Boolean> sortingAttributes, AnswerFilterInstance filter,
      boolean validate, int assignedWeight) throws WdkModelException {
    Map<String, String> context = new LinkedHashMap<String, String>();
    context.put(Utilities.QUERY_CTX_QUESTION, getFullName());

    QueryInstance qi = query.makeInstance(user, dependentValues, validate,
        assignedWeight, context);
    AnswerValue answerValue = new AnswerValue(user, this, qi, pageStart,
        pageEnd, sortingAttributes, filter);

    return answerValue;
  }

  public Param[] getParams() {
    return query.getParams();
  }

  public Map<String, Param> getParamMap() {
    return query.getParamMap();
  }

  public Map<Group, Map<String, Param>> getParamMapByGroups() {
    Param[] params = query.getParams();
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
    Param[] params = query.getParams();
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
    return description;
  }

  public String getSummary() {
    return summary;
  }

  public String getHelp() {
    return help;
  }

  public String getDisplayName() {
    if (displayName == null)
      displayName = getFullName();
    return displayName;
  }

  public String getCustomJavascript() {
    return customJavascriptFile;
  }

  public RecordClass getRecordClass() {
    return this.recordClass;
  }

  public Query getQuery() {
    return this.query;
  }

  public void setQuery(Query q) {
    this.query = q;
    this.idQueryRef = q.getFullName();
    query.setContextQuestion(this);
  }

  public String getName() {
    return name;
  }

  public String getFullName() {
    if (questionSet == null)
      return name;
    else
      return questionSet.getName() + "." + name;
  }

  public String getQuestionSetName() {
    return questionSet.getName();
  }

  public String getQueryName() {
    return this.query.getName();
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");

    StringBuffer saNames = new StringBuffer();
    Map<String, AttributeField> summaryFields = getAttributeFieldMap(FieldScope.NON_INTERNAL);
    for (String saName : summaryFields.keySet()) {
      saNames.append(saName + ", ");
    }
    StringBuffer buf = new StringBuffer("Question: name='" + name + "'"
        + newline + "  recordClass='" + recordClassRef + "'" + newline
        + "  query='" + idQueryRef + "'" + newline + "  displayName='"
        + getDisplayName() + "'" + newline + "  customJavascript='"
        + getCustomJavascript() + "'" + newline + "  summary='" + getSummary()
        + "'" + newline + "  description='" + getDescription() + "'" + newline
        + "  summaryAttributes='" + saNames + "'" + newline + "  help='"
        + getHelp() + "'" + newline);
    buf.append(dynamicAttributeSet.toString());
    return buf.toString();
  }

  public boolean isDynamic() {
    return dynamicAttributeSet != null;
  }

  /**
   * A indicator to the controller whether this question should make answers
   * that contains all records in one page or not.
   * 
   * @return the fullAnswer
   */
  public boolean isFullAnswer() {
    return fullAnswer;
  }

  /**
   * Set the indicator to the controller that suggests this question to make
   * answers containing all records in one page, or not.
   * 
   * @param fullAnswer
   *          the fullAnswer to set
   */
  public void setFullAnswer(boolean fullAnswer) {
    this.fullAnswer = fullAnswer;
  }

  /**
   * if true, when the result has only one record, we will skip the strategy
   * page and redirect user to the record page.
   * 
   * @return the noSummaryOnSingleRecord
   */
  public boolean isNoSummaryOnSingleRecord() {
    return noSummaryOnSingleRecord;
  }

  /**
   * @param noSummaryOnSingleRecord
   *          the noSummaryOnSingleRecord to set
   */
  public void setNoSummaryOnSingleRecord(boolean noSummaryOnSingleRecord) {
    this.noSummaryOnSingleRecord = noSummaryOnSingleRecord;
  }

  // /////////////////////////////////////////////////////////////////////
  // package methods
  // /////////////////////////////////////////////////////////////////////

  Map<String, AttributeField> getDynamicAttributeFields() {
    return dynamicAttributeSet.getAttributeFieldMap();
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

    // always put primary key as the first field
    AttributeField pkField = recordClass.getPrimaryKeyAttributeField();
    attributeFields.put(pkField.getName(), pkField);

    if (defaultSummaryAttributeFields.size() > 0) {
      attributeFields.putAll(defaultSummaryAttributeFields);
    } else {
      attributeFields = recordClass.getSummaryAttributeFieldMap();
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

  public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope) {
    Map<String, AttributeField> attributeFields = new LinkedHashMap<String, AttributeField>();

    // always put primary key as the first field
    AttributeField pkField = recordClass.getPrimaryKeyAttributeField();
    attributeFields.put(pkField.getName(), pkField);

    attributeFields.putAll(recordClass.getAttributeFieldMap(scope));

    attributeFields.putAll(dynamicAttributeSet.getAttributeFieldMap(scope));

    return attributeFields;
  }

  public AttributeCategoryTree getAttributeCategoryTree(FieldScope scope)
      throws WdkModelException {

    // get trimmed copy of category tree
    AttributeCategoryTree tree = recordClass.getAttributeCategoryTree(scope);

    // integrate dynamic attributes into tree as first root node
    AttributeCategory dynamic = new AttributeCategory();
    dynamic.setName("dynamic");
    dynamic.setDisplayName("Search-Specific");
    for (AttributeField field : dynamicAttributeSet.getAttributeFieldMap(scope).values()) {
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
    if (resolved)
      return;
    super.resolveReferences(model);
    this.wdkModel = model;

    try {
      // it must happen before dynamicAttributeSet, because it is
      // referenced
      // in the dynamicAttributeSet.
      this.recordClass = (RecordClass) model.resolveReference(recordClassRef);

      // the id query is always cloned to keep a reference to the
      // question.
      query = (Query) model.resolveReference(idQueryRef);
      query = query.clone();

      // check if we have customized sqlMacros
      if (query instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) query;
        for (String macro : sqlMacroMap.keySet()) {
          sqlQuery.addSqlParamValue(macro, sqlMacroMap.get(macro));
        }
      }

      // check if we have customized params;
      if (paramRefs.size() > 0) {
        String queryName = query.getFullName();
        Map<String, Param> params = query.getParamMap();
        for (ParamReference paramRef : paramRefs) {
          String paramName = paramRef.getElementName();
          if (!params.containsKey(paramName))
            throw new WdkModelException("The paramRef [" + paramName
                + "] defined in QUESTION [" + getFullName()
                + "] doesn't exist in the " + "referenced id query ["
                + queryName + "].");
          Param param = ParamReference.resolveReference(model, paramRef, query);
          query.addParam(param);
        }
        // resolve the param references after all params are present
        for (Param param : query.getParams()) {
          param.resolveReferences(model);
        }
      }
      query.setContextQuestion(this);

      // all the id queries should have weight column
      query.setHasWeight(true);

      // dynamic attribute set need to be initialized after the id query.
      this.dynamicAttributeQuery = createDynamicAttributeQuery(model);
      dynamicAttributeQuery.resolveReferences(model);
      dynamicAttributeSet.resolveReferences(model);

      // make sure we always display weight for combined question
      // if (query.isCombined()) {
      // AttributeField weight = dynamicAttributeSet
      // .getAttributeFieldMap().get(Utilities.COLUMN_WEIGHT);
      // weight.setRemovable(false);
      // }

      // resolve default summary attributes
      if (defaultSummaryAttributeNames != null) {
        Map<String, AttributeField> attributeFields = getAttributeFieldMap();
        for (String fieldName : defaultSummaryAttributeNames) {
          AttributeField field = attributeFields.get(fieldName);
          if (field == null)
            throw new WdkModelException("Summary attribute field [" + fieldName
                + "] defined in question [" + getFullName() + "] is invalid.");
          defaultSummaryAttributeFields.put(fieldName, field);
        }
      }
      defaultSummaryAttributeNames = null;

      // make sure we create index on primary keys
      query.setIndexColumns(recordClass.getIndexColumns());

      // resolve summary views
      for (SummaryView summaryView : summaryViewMap.values()) {
        summaryView.resolveReferences(model);
      }
    } catch (WdkModelException ex) {
      logger.error("resolving question '" + getFullName() + " failed. " + ex);
      throw ex;
    }

    resolved = true;
  }

  public void setQuestionSet(QuestionSet questionSet) {
    this.questionSet = questionSet;
  }

  // /////////////////////////////////////////////////////////////////////
  // Protected Methods
  // /////////////////////////////////////////////////////////////////////

  /**
   * This method is use to clone the question, excluding dynamic attributes
   * 
   * @return
   */
  public Map<String, Boolean> getSortingAttributeMap() {
    Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();

    for (String attrName : defaultSortingMap.keySet()) {
      map.put(attrName, defaultSortingMap.get(attrName));
      if (map.size() >= User.SORTING_LEVEL)
        break;
    }

    // no sorting map defined, use the definition in recordClass
    if (map.size() == 0)
      map = recordClass.getSortingAttributeMap();

    return map;
  }

  /**
   * @return the ignoreSubType
   */
  @Deprecated
  public boolean isIgnoreSubType() {
    return ignoreSubType;
  }

  /**
   * @param ignoreSubType
   *          the ignoreSubType to set
   */
  @Deprecated
  public void setIgnoreSubType(boolean ignoreSubType) {
    this.ignoreSubType = ignoreSubType;
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
    for (WdkModelText description : descriptions) {
      if (description.include(projectId)) {
        if (hasDescription) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one description for project " + projectId);
        } else {
          this.description = description.getText();
          hasDescription = true;
        }
      }
    }
    descriptions = null;

    // exclude summaries
    boolean hasSummary = false;
    for (WdkModelText summ : summaries) {
      if (summ.include(projectId)) {
        if (hasSummary) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one summary for project " + projectId);
        } else {
          this.summary = summ.getText();
          hasSummary = true;
        }
      }
    }
    summaries = null;

    // exclude helps
    boolean hasHelp = false;
    for (WdkModelText help : helps) {
      if (help.include(projectId)) {
        if (hasHelp) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one help for project " + projectId);
        } else {
          this.help = help.getText();
          hasHelp = true;
        }
      }
    }
    helps = null;

    // exclude summary and sorting attribute list
    boolean hasAttributeList = false;
    for (AttributeList attributeList : attributeLists) {
      if (attributeList.include(projectId)) {
        if (hasAttributeList) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one <attributesList> for " + "project "
              + projectId);
        } else {
          this.defaultSummaryAttributeNames = attributeList.getSummaryAttributeNames();
          this.defaultSortingMap = attributeList.getSortingAttributeMap();
          hasAttributeList = true;
        }
      }
    }
    attributeLists = null;

    // exclude dynamic attribute set
    boolean hasDynamicAttributes = false;
    for (DynamicAttributeSet dynamicAttributeSet : dynamicAttributeSets) {
      if (dynamicAttributeSet.include(projectId)) {
        if (hasDynamicAttributes) {
          throw new WdkModelException("The question " + getFullName()
              + " has more than one <dynamicAttributes> for " + "project "
              + projectId);
        } else {
          dynamicAttributeSet.excludeResources(projectId);
          this.dynamicAttributeSet = dynamicAttributeSet;
          hasDynamicAttributes = true;
        }
      }
    }
    dynamicAttributeSets = null;

    // add weight as an attribute
    if (dynamicAttributeSet == null) {
      DynamicAttributeSet dynamicSet = new DynamicAttributeSet();
      dynamicSet.setQuestion(this);
      dynamicSet.excludeResources(projectId);
      this.dynamicAttributeSet = dynamicSet;
    }

    // exclude param refs
    for (int i = paramRefs.size() - 1; i >= 0; i--) {
      ParamReference paramRef = paramRefs.get(i);
      if (paramRef.include(projectId)) {
        paramRef.excludeResources(projectId);
      } else {
        paramRefs.remove(i);
      }
    }

    // exclude sql macros
    for (WdkModelText macro : sqlMacroList) {
      if (macro.include(projectId)) {
        macro.excludeResources(projectId);
        String name = macro.getName();
        if (sqlMacroMap.containsKey(name))
          throw new WdkModelException("The macro " + name
              + " is duplicated in question " + getFullName());

        sqlMacroMap.put(macro.getName(), macro.getText());
      }
    }
    sqlMacroList = null;

    // exclude the summary views
    for (SummaryView view : summaryViewList) {
      if (view.include(projectId)) {
        view.excludeResources(projectId);
        String name = view.getName();
        if (summaryViewMap.containsKey(name))
          throw new WdkModelException("The summary view '" + name
              + "' is duplicated in question " + getFullName());

        summaryViewMap.put(name, view);
      }
    }
    summaryViewList = null;
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
    query.setName(this.query.getName() + DYNAMIC_QUERY_SUFFIX);
    // put the dynamic query into the same query set of the id query.
    QuerySet querySet = this.query.getQuerySet();
    querySet.addQuery(query);

    // set the columns, which as the same column as the id query
    boolean hasWeight = false;
    for (Column column : this.query.getColumns()) {
      query.addColumn(new Column(column));
      if (column.getName().equals(Utilities.COLUMN_WEIGHT))
        hasWeight = true;
    }
    if (!hasWeight) {
      // create and add the weight column
      Column column = new Column();
      column.setName(Utilities.COLUMN_WEIGHT);
      column.setType(ColumnType.NUMBER);
      column.setWidth(12);
      query.addColumn(column);
    }

    // dynamic query doesn't have sql defined, here just fill in the stub
    // sql; the real sql will be constructed by answerValue
    query.setSql("");

    query.excludeResources(wdkModel.getProjectId());

    return query;
  }

  public void setShortDisplayName(String shortDisplayName) {
    this.shortDisplayName = shortDisplayName;
  }

  /**
   * the default short name used in the step box in strategy workspace.
   */
  public String getShortDisplayName() {
    return (shortDisplayName == null) ? getDisplayName() : shortDisplayName;
  }

  public AnswerParam[] getTransformParams(RecordClass recordClass) {
    List<AnswerParam> list = new ArrayList<AnswerParam>();
    String rcName = recordClass.getFullName();
    for (Param param : query.getParams()) {
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
    this.paramRefs.add(paramRef);
  }

  public void addSqlParamValue(WdkModelText sqlMacro) {
    this.sqlMacroList.add(sqlMacro);
  }

  public Map<String, SummaryView> getSummaryViews() {
    Map<String, SummaryView> map = new LinkedHashMap<>(summaryViewMap);
    // get views from record
    Map<String, SummaryView> recordMap = recordClass.getSummaryViews();

    // don't override the views defined in the question
    for (String name : recordMap.keySet()) {
      if (!map.containsKey(name)) {
        map.put(name, recordMap.get(name));
      }
    }

    return map;
  }

  public SummaryView getSummaryView(String viewName) throws WdkUserException {
    SummaryView view = summaryViewMap.get(viewName);
    if (view != null)
      return view;

    return recordClass.getSummaryView(viewName);
  }

  public SummaryView getDefaultSummaryView() {
    // first look for default in the views defined in question
    for (SummaryView view : summaryViewMap.values()) {
      if (view.isDefault())
        return view;
    }
    // then look for the default in the views from record
    Map<String, SummaryView> viewsFromRecord = recordClass.getSummaryViews();
    for (SummaryView view : viewsFromRecord.values()) {
      if (view.isDefault())
        return view;
    }
    // return the first view from question
    if (summaryViewMap.size() > 0)
      return summaryViewMap.values().iterator().next();
    // return the first view from record
    if (viewsFromRecord.size() > 0)
      return viewsFromRecord.values().iterator().next();

    return null;
  }

  public void addSummaryView(SummaryView view) {
    if (summaryViewList == null)
      summaryViewMap.put(view.getName(), view);
    else
      summaryViewList.add(view);
  }

  public Map<String, SearchCategory> getCategories(String usedBy, boolean strict) {
    Map<String, SearchCategory> categories = wdkModel.getCategories(usedBy, strict);
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
}
