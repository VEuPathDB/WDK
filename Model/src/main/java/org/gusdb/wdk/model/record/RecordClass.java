package org.gusdb.wdk.model.record;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Named;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisXml;
import org.gusdb.wdk.model.analysis.StepAnalysisXml.StepAnalysisContainer;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.columntool.DefaultColumnToolBundleRef;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.FilterDefinition;
import org.gusdb.wdk.model.filter.FilterReference;
import org.gusdb.wdk.model.filter.StepFilter;
import org.gusdb.wdk.model.filter.StepFilterDefinition;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ColumnType;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.AttributeList;
import org.gusdb.wdk.model.question.CategoryList;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeCategoryTree;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.IdAttributeField;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.report.ReporterRef;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.gusdb.wdk.model.test.sanity.OptionallyTestable;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.BasketSnapshotQueryPlugin;
import org.gusdb.wdk.model.user.FavoriteReference;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferences;

/**
 * RecordClass is the core entity in WDK, and it defined the type of the data
 * that is presented in WDK driven system.
 * <p>
 * Records are normally retrieved by running questions, and each question is
 * associated with one recordClass type.
 * <p>
 * A recordClass defines the attribute fields and table fields for records, and
 * for a given primary key, a RecordInstance can be instantiated, and the
 * instance will holds attribute values and table values.
 * <p>
 * A record can have multiple attributes, but for each attribute, it can have
 * only one value; the tables can have multiple attributes, and each attribute
 * might have zero or more values. Please refer to the AttributeQueryReference
 * and TableQueryReference for details about defining the attribute and table
 * queries.
 *
 * @author jerric
 */
public class RecordClass extends WdkModelBase implements AttributeFieldContainer, StepAnalysisContainer, OptionallyTestable, NamedObject {

  private static final Logger LOG = Logger.getLogger(RecordClass.class);

  private static final Set<Character> VOWELS = new HashSet<>(Arrays.asList('a', 'e', 'i', 'o', 'u'));

  /**
   * Returns a list of DynamicRecordInstance representing the records to which
   * the passed primary key value currently maps.
   *
   * @param user user to execute queries under
   * @param pkValue primary key value to look up
   * @return a list of record instances associated with the passed primary key
   * @throws WdkModelException if anything goes wrong
   */
  public static List<RecordInstance> getRecordInstances(User user, PrimaryKeyValue pkValue) throws WdkModelException {
    try {
      RecordClass recordClass = pkValue.getPrimaryKeyDefinition().getRecordClass();
      return mapToList(
          recordClass.lookupPrimaryKeys(user, pkValue.getRawValues()),
          fSwallow(idMap -> new DynamicRecordInstance(user, recordClass, idMap)));
    }
    catch (RecordNotFoundException rnfe) {
      return Collections.emptyList();
    }
    catch (Exception e) {
      // since input to this method is already a PrimaryKeyValue (not Map),
      // should not see these Exceptions
      throw WdkModelException.translateFrom(e);
    }
  }

  /**
   * This method takes in a bulk attribute or table query, and adds the primary
   * key columns as params into the SQL, and return the a Query with the params.
   */
  public static SqlQuery prepareQuery(WdkModel wdkModel, SqlQuery query, String[] paramNames)
      throws WdkModelException {
    Map<String, Column> columns = query.getColumnMap();
    Map<String, Param> originalParams = query.getParamMap();
    SqlQuery newQuery = query.clone();
    // do not cache the single-line query
    newQuery.setIsCacheable(false);

    // find the new params to be created
    List<String> newParams = new ArrayList<>();
    for (String column : paramNames) {
      if (!originalParams.containsKey(column))
        newParams.add(column);
    }

    // create the missing primary key params for the query
    ParamSet paramSet = wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    for (String columnName : newParams) {
      StringParam param;
      if (paramSet.contains(columnName)) {
        param = (StringParam) paramSet.getParam(columnName);
      }
      else {
        param = new StringParam();
        Column column = columns.get(columnName);
        ColumnType type = column.getType();
        boolean number = !type.isText();
        param.setName(columnName);
        param.setNumber(number);

        param.excludeResources(wdkModel.getProjectId());
        param.resolveReferences(wdkModel);
        paramSet.addParam(param);
      }
      newQuery.addParam(param);
    }

    // if the new query is SqlQuery, modify the sql
    if (!newParams.isEmpty()) {
      StringBuilder builder = new StringBuilder("SELECT f.* FROM (");
      builder.append(newQuery.getSql())
        .append(") f WHERE ");
      boolean firstColumn = true;
      for (String columnName : newParams) {
        if (firstColumn)
          firstColumn = false;
        else
          builder.append(" AND ");
        builder.append("f.").append(columnName)
          .append(" = $$").append(columnName).append("$$");
      }

      // replace the id_sql macro
      StringBuilder idqBuilder = new StringBuilder();
      for (String column : paramNames) {
        if (idqBuilder.length() == 0)
          idqBuilder.append("(SELECT ");
        else
          idqBuilder.append(", ");
        idqBuilder.append("SUBSTR($$" + column + "$$, 1, 4000) AS " + column);
      }
      idqBuilder.append(wdkModel.getAppDb().getPlatform().getDummyTable())
        .append(")");

      String idSql = idqBuilder.toString();
      String sql = builder.toString()
        .replace(Utilities.MACRO_ID_SQL, idSql)
        .replace(Utilities.MACRO_ID_SQL_NO_FILTERS, idSql);

      newQuery.setSql(sql);
    }
    return newQuery;
  }

  private RecordClassSet recordClassSet;

  private String allRecordsQueryRef;
  private Query allRecordsQuery;

  private List<AttributeQueryReference> attributesQueryRefList = new ArrayList<>();

  private Map<String, Query> attributeQueries = new LinkedHashMap<>();
  private Map<String, Query> tableQueries = new LinkedHashMap<>();

  private List<PrimaryKeyDefinition> primaryKeyDefinitionList = new ArrayList<>();
  private PrimaryKeyDefinition primaryKeyDefinition;

  private IdAttributeField idAttributeField;

  private List<AttributeField> attributeFieldList = new ArrayList<>();
  private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<>();

  private List<TableField> tableFieldList = new ArrayList<>();
  private Map<String, TableField> tableFieldsMap = new LinkedHashMap<>();

  private String name;
  private String fullName;

  /** The name of the FontAwesome icon to use in various parts of the website */
  private String iconName;

  /**
   * the native versions are the real name of the record class.  the non-native
   * are potentially different, for display purposes.  This can happen if a
   * ResultSizeQueryReference is supplied, that provides non-native result
   * counts and display names
   */
  private String nativeDisplayName;
  private String nativeDisplayNamePlural;
  private String nativeShortDisplayName;
  private String nativeShortDisplayNamePlural;

  private String displayName;
  private String description;
  private String displayNamePlural;
  private String shortDisplayName;
  private String shortDisplayNamePlural;

  /**
   * An option that provides SQL to post-process an Answer result, providing a
   * custom result size count. If present, induces construction of a non-default
   * result size plugin that uses this sql
   */
  private ResultSizeQueryReference resultSizeQueryRef;

  /**
   * An option that provides SQL to post-process an Answer result, providing a
   * custom property value.
   */
  private ResultPropertyQueryReference resultPropertyQueryRef;

  /**
   * An option that provides SQL to map from a PK to a partition key
  */
  private String partitionKeysQueryRef;

    /**
   * A pluggable way to compute the result size.  For example, count the number
   * of genes in a list of transcripts. The default is overridden with a plugin
   * supplied in the XML model, if provided.
   */
  private ResultSize resultSizePlugin = new DefaultResultSizePlugin();

  /**
   * A pluggable way to compute a result property.  For example, count the
   * number of genes in a list of transcripts that are missing transcripts.
   */
  private ResultProperty resultPropertyPlugin;

  private String customBooleanQueryClassName;

  private BooleanQuery booleanQuery;

  private String _customSnapshotBasketQueryPluginClassName;
  
  private String attributeOrdering;

  // for sanity testing
  private boolean doNotTest;
  private List<ParamValuesSet> unexcludedParamValuesSets = new ArrayList<>();
  private ParamValuesSet paramValuesSet;

  private List<ReporterRef> reporterList = new ArrayList<>();
  private Map<String, ReporterRef> reporterMap = new LinkedHashMap<>();

  private List<AttributeList> attributeLists = new ArrayList<>();

  private String[] defaultSummaryAttributeNames;
  private Map<String, AttributeField> defaultSummaryAttributeFields = new LinkedHashMap<>();
  private Map<String, Boolean> defaultSortingMap = new LinkedHashMap<>();

  /**
   * if true, the basket feature will be turn on for the records of this type.
   */
  private boolean useBasket = true;

  private List<FavoriteReference> favorites = new ArrayList<>();
  private String favoriteNoteFieldName;
  private AttributeField favoriteNoteField;

  private List<SummaryView> summaryViewList = new ArrayList<>();
  private Map<String, SummaryView> summaryViewMap = new LinkedHashMap<>();

  private List<RecordView> recordViewList = new ArrayList<>();
  private Map<String, RecordView> recordViewMap = new LinkedHashMap<>();

  private List<StepAnalysisXml> stepAnalysisList = new ArrayList<>();
  private Map<String, StepAnalysis> stepAnalysisMap = new LinkedHashMap<>();

  private List<FilterReference> _filterReferences = new ArrayList<>();
  private Map<String, StepFilter> _stepFilters = new LinkedHashMap<>();

  private String _urlSegment;

  private SqlQuery _partitionKeysSqlQuery;

  private String defaultColumnToolBundleRef;

  // ////////////////////////////////////////////////////////////////////
  // Called at model creation time
  // ////////////////////////////////////////////////////////////////////

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIconName(String iconName) { this.iconName = iconName; }

  public String getIconName() { return this.iconName; }

  public String getDisplayName() {
    return (displayName == null) ? getName() : displayName;
  }

  public String getNativeDisplayName() {
    return (nativeDisplayName == null) ? getName() : nativeDisplayName;
  }

  public String getDescription() {
    return (description == null) ? "" : description;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
    this.nativeDisplayName = displayName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDisplayNamePlural() {
    if (displayNamePlural != null)
      return displayNamePlural;

    return getPlural(getDisplayName());
  }

  public String getNativeDisplayNamePlural() {
    if (nativeDisplayNamePlural != null)
      return nativeDisplayNamePlural;

    return getPlural(getNativeDisplayName());
  }

  public void setDisplayNamePlural(String displayNamePlural) {
    this.displayNamePlural = displayNamePlural;
    this.nativeDisplayNamePlural = displayNamePlural;
  }

  public String getShortDisplayNamePlural() {
    if (shortDisplayNamePlural != null)
      return shortDisplayNamePlural;

    return getPlural(getShortDisplayName());
  }

  public String getNativeShortDisplayNamePlural() {
    if (nativeShortDisplayNamePlural != null)
      return nativeShortDisplayNamePlural;

    return getPlural(getNativeShortDisplayName());
  }

  public void setShortDisplayNamePlural(String shortDisplayNamePlural) {
    this.shortDisplayNamePlural = shortDisplayNamePlural;
    this.nativeShortDisplayNamePlural = shortDisplayNamePlural;
  }

  public void setUrlName(String urlName) {
    // XML Model alias for URL segment
    setUrlSegment(urlName);
  }

  public void setUrlSegment(String urlSegment) {
    _urlSegment = urlSegment;
  }

  public String getUrlSegment() {
    return _urlSegment;
  }

  public SqlQuery getPartitionKeysSqlQuery() { return _partitionKeysSqlQuery; }

  private static String getPlural(String recordClassName) {
    if (recordClassName == null || recordClassName.isEmpty())
      return recordClassName;

    int length = recordClassName.length();
    char last = recordClassName.charAt(length - 1);
    if (last == 'o')
      return recordClassName + "es";
    if (last == 'y') {
      char second = recordClassName.charAt(length - 2);
      if (!VOWELS.contains(second))
        return recordClassName.substring(0, length - 1) + "ies";
    }
    return recordClassName + "s";
  }

  @SuppressWarnings("unused") // ModelXmlParser
  public void setDefaultColumnToolBundleRef(DefaultColumnToolBundleRef ref) {
    defaultColumnToolBundleRef = ref.getRef();
  }

  public String getDefaultColumnToolBundleRef() {
    return defaultColumnToolBundleRef;
  }

  public void setAllRecordsQueryRef(String queryRef) {
    allRecordsQueryRef = queryRef;
  }

  public Query getAllRecordsQuery() {
    return allRecordsQuery;
  }

  public ResultSize getResultSizePlugin() {
    return resultSizePlugin;
  }

  public ResultProperty getResultPropertyPlugin() {
    return resultPropertyPlugin;
  }

  public String getCustomBooleanQueryClassName() {
    return customBooleanQueryClassName;
  }

  public void setAttributeOrdering(String attOrder) {
    this.attributeOrdering = attOrder;
  }

  public void setPrimaryKeyDefinition(PrimaryKeyDefinition primaryKeyDefinition) {
    // add this pkDef to list; single value will be chosen during excludeResources()
    primaryKeyDefinition.setRecordClass(this);
    primaryKeyDefinitionList.add(primaryKeyDefinition);
  }

  public PrimaryKeyDefinition getPrimaryKeyDefinition() {
    return primaryKeyDefinition;
  }

  /**
   * @param attributesQueryRef
   *          two part query name (set.name)
   */
  public void addAttributesQueryRef(AttributeQueryReference attributesQueryRef) {
    attributesQueryRef.setRecordClass(this);
    attributesQueryRefList.add(attributesQueryRef);
  }

  @SuppressWarnings("unused") // ModelXmlParser
  public void addAttributeField(AttributeField attributeField) {
    attributeField.setContainer(this);
    attributeFieldList.add(attributeField);
  }

  @SuppressWarnings("unused") // ModelXmlParser
  public void addTableField(TableField tableField) {
    tableField.setRecordClass(this);
    tableFieldList.add(tableField);
  }

  @SuppressWarnings("unused") // ModelXmlParser
  public void addReporterRef(ReporterRef reporter) {
    reporterList.add(reporter);
  }

  public void setDoNotTest(boolean doNotTest) {
    this.doNotTest = doNotTest;
  }

  @Override
  public boolean getDoNotTest() {
    return doNotTest;
  }

  @SuppressWarnings("unused") // ModelXmlParser
  public void addParamValuesSet(ParamValuesSet newParamValuesSet) {
    unexcludedParamValuesSets.add(newParamValuesSet);
  }

  public ParamValuesSet getParamValuesSet() {
    return paramValuesSet == null ? new ParamValuesSet() : paramValuesSet;
  }

  /**
   * @param tree no longer used 
   */
  @Deprecated
  public void setAttributeCategoryTree(AttributeCategoryTree tree) {
     // TODO: remove from RNG/model; this is now a no-op
  }

  /**
   * @param categoryList no longer used 
   */
  @Deprecated
  public void addCategoryList(CategoryList categoryList) {
    // TODO: remove from RNG/model; this is now a no-op
  }

  public void setResultSizeQueryRef(ResultSizeQueryReference ref) {
    resultSizeQueryRef = ref;
  }

  @SuppressWarnings("unused") // ModelXmlParser
  public void setPartitionKeysQueryRef(String ref) {
    partitionKeysQueryRef = ref;
  }

    @SuppressWarnings("unused") // ModelXmlParser
  public void setResultPropertyQueryRef(ResultPropertyQueryReference ref) {
    resultPropertyQueryRef = ref;
  }

  @SuppressWarnings("unused") // XML Digester (attribute)
  public void setCustomBooleanQueryClassName(String className) {
    customBooleanQueryClassName = className;
  }

  @SuppressWarnings("unused") // XML Digester (attribute)
  public void setCustomSnapshotBasketQueryPluginClassName(String className) {
    _customSnapshotBasketQueryPluginClassName = className;
  }

  // ////////////////////////////////////////////////////////////
  // public getters
  // ////////////////////////////////////////////////////////////

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getFullName() {
    return fullName;
  }

  public Map<String, TableField> getTableFieldMap() {
    return getTableFieldMap(FieldScope.ALL);
  }

  public Map<String, TableField> getTableFieldMap(FieldScope scope) {
    Map<String, TableField> fields = new LinkedHashMap<>();

    for (TableField field : tableFieldsMap.values())
      if (scope.isFieldInScope(field))
        fields.put(field.getName(), field);

    return fields;
  }

  // used by report maker, adding display names in map so later the tables show
  // sorted by display name
  public Map<String, TableField> getTableFieldMap(FieldScope scope, boolean useDisplayNamesAsKeys) {
    if (!useDisplayNamesAsKeys)
      return getTableFieldMap(scope);

    Map<String, TableField> fields = new LinkedHashMap<>();
    for (TableField field : tableFieldsMap.values())
      if (scope.isFieldInScope(field))
        fields.put(field.getDisplayName(), field);

    return fields;
  }

  public TableField[] getTableFields() {
    Map<String, TableField> tables = getTableFieldMap();
    TableField[] array = new TableField[tables.size()];
    tables.values().toArray(array);
    return array;
  }

  @Override
  public Optional<AttributeField> getAttributeField(String key) {
    return idAttributeField.getName().equals(key)
        ? Optional.of(idAttributeField)
        : Optional.ofNullable(attributeFieldMap.get(key));
  }

  @Override
  public Map<String, AttributeField> getAttributeFieldMap() {

    Map<String, AttributeField> fields = new LinkedHashMap<>();

    // always put primary key field first
    fields.put(idAttributeField.getName(), idAttributeField);

    fields.putAll(attributeFieldMap);

    return fields;
  }

  public Field[] getFields() {
    int attributeCount = attributeFieldMap.size();
    int tableCount = tableFieldsMap.size();
    Field[] fields = new Field[attributeCount + tableCount];
    // copy attribute fields
    attributeFieldMap.values().toArray(fields);
    // copy table fields
    TableField[] tableFields = getTableFields();
    System.arraycopy(tableFields, 0, fields, attributeCount, tableCount);
    return fields;
  }

  public Reference getReference() throws WdkModelException {
    return new Reference(getFullName());
  }

  public Map<String, ReporterRef> getReporterMap() {
    return new LinkedHashMap<>(reporterMap);
  }

  public ResultSizeQueryReference getResultSizeQueryRef() {
    return resultSizeQueryRef;
  }

  public ResultPropertyQueryReference getResultPropertyQueryRef() {
    return resultPropertyQueryRef;
  }

  public String getPartitionKeysQueryRef() {
    return partitionKeysQueryRef;
  }

  public BooleanQuery getBooleanQuery() {
    return booleanQuery;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("Record: name='" + name + "'").append(NL);

    buf.append("--- Attributes ---").append(NL);

    for (AttributeField attribute : attributeFieldMap.values())
      buf.append(attribute.getName()).append(NL);

    buf.append("--- Tables ---").append(NL);

    for (TableField table : tableFieldsMap.values())
      buf.append(table.getName()).append(NL);

    return buf.toString();
  }

  /*
   * <sanityRecord ref="GeneRecordClasses.GeneRecordClass" primaryKey="PF11_0344"/>
   */
  public String getSanityTestSuggestion() {
    String indent = "    ";
    String newline = System.getProperty("line.separator");
    StringBuilder buf = new StringBuilder(newline + newline + indent + "<sanityRecord ref=\"" + getFullName() +
        "\"" + newline + indent + indent + indent + "primaryKey=\"FIX_pk\">" + newline);
    buf.append(indent + "</sanityRecord>");
    return buf.toString();
  }

  // /////////////////////////////////////////////////////////////////////////
  // package scope methods
  // /////////////////////////////////////////////////////////////////////////

  void setRecordClassSet(RecordClassSet recordClassSet) {
    this.recordClassSet = recordClassSet;
    this.fullName = recordClassSet.getName() + "." + name;
  }

  public RecordClassSet getRecordClassSet() {
    return recordClassSet;
  }

  Query getAttributeQuery(String queryFullName) {
    return attributeQueries.get(queryFullName);
  }

  public Map<String, Query> getAttributeQueries() {
    return new LinkedHashMap<>(attributeQueries);
  }

  public Map<String, Query> getTableQueries() {
    return new LinkedHashMap<>(tableQueries);
  }

  TableField getTableField(String tableName) throws WdkModelException {
    TableField tableField = tableFieldsMap.get(tableName);
    if (tableField == null) {
      String message = "Record " + getName() + " does not have a table field with name '" + tableName + "'.";
      throw new WdkModelException(message);
    }
    return tableField;
  }

  public boolean hasAllRecordsQuery() {
    return (allRecordsQuery != null);
  }

  public Long getAllRecordsCount(User user) throws WdkModelException {
    try {
      String baseSql = Query.makeQueryInstance(QueryInstanceSpec.builder()
          .buildRunnable(user, allRecordsQuery, StepContainer.emptyContainer())).getSql();
      String sql = "select count(*) from ( " + baseSql + " )";
      return new SQLRunner(_wdkModel.getAppDb().getDataSource(),
          sql, fullName + "-all-records-count")
        .executeQuery(new SingleLongResultSetHandler())
        .orElseThrow(() -> new WdkModelException(
            "Count query did not return single value.  SQL: " + sql));
    }
    catch (SQLRunnerException e) {
      // unwrap exception and rewrap as WdkModelException
      return WdkModelException.unwrap(e);
    }
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    if (_resolved)
      return;
    super.resolveReferences(model);

    if (name.isEmpty() || name.indexOf('\'') >= 0)
      throw new WdkModelException("recordClass name cannot be empty or " + "having single quotes: " + name);

    // resolve primary key references
    primaryKeyDefinition.resolveReferences(model);

    if (partitionKeysQueryRef != null) {
      Object o = _wdkModel.resolveReference(partitionKeysQueryRef);
      if (! (o instanceof SqlQuery))
        throw new WdkModelException("Partition Key query ref " + partitionKeysQueryRef + " must reference an SqlQuery");
      _partitionKeysSqlQuery = (SqlQuery) o;
    }

    // create column attribute fields for primary key columns if they don't already exist
    createPrimaryKeySubFields(model.getProjectId());

    // Resolve default column tool bundle for this recordclass;
    //   this must be done before resolving references for the attribute fields.
    if (defaultColumnToolBundleRef == null) {
      // assign system-wide default, which is validated in WdkModel
      defaultColumnToolBundleRef = model.getDefaultColumnToolBundleRef();
    }
    else {
      // make sure default set here refers to a registered tool bundle
      model.getColumnToolBundleMap().getToolBundle(defaultColumnToolBundleRef);
    }

    // resolve the references for attribute queries
    resolveAttributeQueryReferences(model);

    // resolve references for the attribute fields
    for (AttributeField field : attributeFieldMap.values()) {
      try {
        field.resolveReferences(model);
      }
      catch (WdkModelException e) {
        throw new WdkModelException("Unable to resolve reference of field '" + field.getName() +
            "' in RecordClass '" + getFullName() + "'.", e);
      }
    }

    if (allRecordsQueryRef != null) {
      allRecordsQuery = (Query) _wdkModel.resolveReference(allRecordsQueryRef);
      String[] expectedColumns = primaryKeyDefinition.getColumnRefs();
      Set<String> queryColumns = allRecordsQuery.getColumnMap().keySet();
      String queryDefString = "allRecordsQuery '" + allRecordsQueryRef + "' configured for record class '" + fullName + "'";
      String badColsMsg = queryDefString + " has columns that do not match the record class's PK columns";
      if (expectedColumns.length != queryColumns.size()) {
        throw new WdkModelException(badColsMsg);
      }
      for (String column : expectedColumns) {
        if (!queryColumns.contains(column)) {
          throw new WdkModelException(badColsMsg);
        }
      }
      if (allRecordsQuery.getParams().length != 0) {
        throw new WdkModelException(queryDefString + " cannot have parameters");
      }
    }

    if (resultSizeQueryRef != null) {
      resultSizeQueryRef.resolveReferences(model);
      displayName = resultSizeQueryRef.getRecordDisplayName();
      shortDisplayName = resultSizeQueryRef.getRecordShortDisplayName();
      displayNamePlural = resultSizeQueryRef.getRecordDisplayNamePlural();
      shortDisplayNamePlural = resultSizeQueryRef.getRecordShortDisplayNamePlural();
        Query query = (Query) _wdkModel.resolveReference(resultSizeQueryRef.getTwoPartName());
      resultSizePlugin = new SqlQueryResultSizePlugin(query);
    }

    if (resultPropertyQueryRef != null) {
      resultPropertyQueryRef.resolveReferences(model);
        Query query = (Query) _wdkModel.resolveReference(resultPropertyQueryRef.getTwoPartName());
      resultPropertyPlugin = new SqlQueryResultPropertyPlugin(query, resultPropertyQueryRef.getPropertyName());
    }

    if (customBooleanQueryClassName != null) {
      try {
        Class<? extends BooleanQuery> clazz =
            Class.forName(customBooleanQueryClassName).asSubclass(BooleanQuery.class);
        booleanQuery = clazz.getDeclaredConstructor().newInstance();
        booleanQuery.setRecordClass(this);
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
          IllegalArgumentException | InvocationTargetException |
          NoSuchMethodException | SecurityException ex) {
        throw new WdkModelException("Can't create java class for " +
            "customBooleanQueryClassName from class name '" +
            customBooleanQueryClassName + "'", ex);
      }
    }
    else {
      booleanQuery = new BooleanQuery(this);
    }

    // resolve the references for table queries
    resolveTableFieldReferences(model);

    if (attributeOrdering != null)
      attributeFieldMap = sortAllAttributes();

    // resolve filters
    resolveFilterReferences(model);

    // resolve default summary attributes
    if (defaultSummaryAttributeNames != null) {
      Map<String, AttributeField> attributeFields = getAttributeFieldMap();
      for (String fieldName : defaultSummaryAttributeNames) {
        AttributeField field = attributeFields.get(fieldName);
        String fieldDefStr = "Summary attribute field [" + fieldName + "] defined in RecordClass [" + getFullName() + "]";
        if (field == null) throw new WdkModelException(fieldDefStr + " is invalid.");
        if (field.isInternal()) throw new WdkModelException(fieldDefStr + " is internal.");
        defaultSummaryAttributeFields.put(fieldName, field);
      }
    }
    defaultSummaryAttributeNames = null;

    // resolve the favorite note reference to attribute field
    if (favoriteNoteFieldName != null) {
      favoriteNoteField = attributeFieldMap.get(favoriteNoteFieldName);
      if (favoriteNoteField == null)
        throw new WdkModelException("The attribute '" + favoriteNoteFieldName +
            "' for the default favorite " + "note content of recordClass '" + getFullName() + "' is invalid.");
    }

    // resolve references for views
    for (SummaryView summaryView : summaryViewMap.values())
      summaryView.resolveReferences(model);

    for (RecordView recordView : recordViewMap.values())
      recordView.resolveReferences(model);

    // resolve step analysis refs
    for (StepAnalysis stepAnalysisRef : stepAnalysisMap.values()) {
      StepAnalysisXml stepAnalysisXml = (StepAnalysisXml)stepAnalysisRef;
      stepAnalysisXml.setContainer(this);
      stepAnalysisXml.resolveReferences(model);
    }

    // resolve reporters
    for (ReporterRef reporterRef : reporterMap.values())
      reporterRef.resolveReferences(model);

    for (AttributeField attribute : attributeFieldMap.values()) {
      for (ReporterRef reporterRef : attribute.getReporters().values()) {
        if (reporterMap.containsKey(reporterRef.getName()))
          throw new WdkModelException(
            "Duplicate reporter with name: " + reporterRef.getName());
        reporterMap.put(reporterRef.getName(), reporterRef);
      }
    }

    // register this URL segment with the model to ensure uniqueness
    _wdkModel.registerRecordClassUrlSegment(_urlSegment, getFullName());

    _resolved = true;
  }

  private void resolveAttributeQueryReferences(WdkModel wdkModel) throws WdkModelException {
    String[] pkColumns = primaryKeyDefinition.getColumnRefs();
    List<String> pkColumnList = Arrays.asList(pkColumns);
    for (AttributeQueryReference reference : attributesQueryRefList) {
      try {
        // validate attribute query
        SqlQuery query = (SqlQuery) wdkModel.resolveReference(reference.getTwoPartName());
        validateBulkQuery(query);

        // resolving dynamic column attribute fields
        reference.resolveReferences(wdkModel);

        // add fields into record level, and associate columns
        Map<String, AttributeField> fields = reference.getAttributeFieldMap();
        Map<String, Column> columns = query.getColumnMap();
        for (AttributeField field : fields.values()) {
          field.setContainer(this);
          String fieldName = field.getName();
          // check if the attribute is duplicated
          if (attributeFieldMap.containsKey(fieldName))
            throw new WdkModelException("The attribute " + fieldName +
              " is duplicated in the recordClass " + getFullName());

          // check if attribute name is same as a table
          if (tableFieldsMap.containsKey(fieldName))
            throw new WdkModelException("The attribute " + fieldName +
              " has the same name as a table in the recordClass " +
              getFullName());

          // check if attribute name is same as a pk column
          if (pkColumnList.contains(fieldName)) {
            throw new WdkModelException(
              "The attribute " + fieldName + " in attributeQueryRef "
                + reference.getTwoPartName() + " cannot be the same as a "
                + "primary key column.  Use a pkColumnAttribute tag to declare "
                + "non-default PK column attribute field behavior.");
          }

          // link columnAttributes with columns
          if (field instanceof QueryColumnAttributeField) {
            Column column = columns.get(fieldName);
            if (column == null) {
              throw new WdkModelException(
                "Column is missing for the " + "QueryColumnAttributeField " +
                  fieldName + " in recordClass " + getFullName());
            }
            ((QueryColumnAttributeField) field).setColumn(column);
          }
          attributeFieldMap.put(fieldName, field);
        }

        SqlQuery attributeQuery = prepareQuery(wdkModel, query, pkColumns);
        attributeQueries.put(query.getFullName(), attributeQuery);

        if (_partitionKeysSqlQuery == null && attributeQuery.getSql().contains(SqlQuery.PARTITION_KEYS_MACRO)) {
          throw new WdkModelException("Attribute query " + attributeQuery.getFullName()

          + "contains the macro " + SqlQuery.PARTITION_KEYS_MACRO
              + " but record class " + getName() + " does not define a partition key query ref");
        }
        // intentionally using unprepared query
        assignAttributeFieldDataTypes(query);
      }
      catch (WdkModelException e) {
        throw new WdkModelException("Error while resolving attribute query " +
          "reference \"" + reference.getTwoPartName() + "\" in record type \"" +
          getFullName() + "\"", e);
      }
    }
  }

  private void assignAttributeFieldDataTypes(SqlQuery query) throws WdkModelException {
    final var types = query.resolveColumnTypes();
    types.keySet()
      .stream()
      .map(attributeFieldMap::get)
      .filter(ColumnAttributeField.class::isInstance)
      .map(ColumnAttributeField.class::cast)
      .forEach(field -> field.setDataType(types.get(field.getName())));
  }

  private void resolveTableFieldReferences(WdkModel wdkModel) throws WdkModelException {
    String[] paramNames = primaryKeyDefinition.getColumnRefs();

    // resolve the references for table queries
    for (TableField tableField : tableFieldsMap.values()) {
      tableField.resolveReferences(wdkModel);

      SqlQuery query = tableField.getUnwrappedQuery();

      if (_partitionKeysSqlQuery == null && query.getSql().contains(SqlQuery.PARTITION_KEYS_MACRO)) {
        throw new WdkModelException("Table query " + query.getFullName()

            + "contains the macro " + SqlQuery.PARTITION_KEYS_MACRO
            + " but record class " + getName() + " does not define a partition key query ref");
      }


      SqlQuery tableQuery = RecordClass.prepareQuery(wdkModel, query, paramNames);
      tableQueries.put(query.getFullName(), tableQuery);
    }

  }

  private void resolveFilterReferences(WdkModel wdkModel) throws WdkModelException {

    // automatically add basket view filter to each recordclass
    StepFilter basketFilter = new InBasketFilter(_wdkModel);
    _stepFilters.put(basketFilter.getKey(), basketFilter);

    // resolve step filter references
    for (FilterReference reference : _filterReferences) {
      StepFilter filter = resolveStepFilterReferenceByName(reference.getName(), _wdkModel, "recordClass " + getFullName());
      if (_stepFilters.containsKey(filter.getKey())) {
        throw new WdkModelException("Non-unique step filter key '" + filter.getKey() + "detected in record class " + getFullName());
      }
      _stepFilters.put(filter.getKey(), filter);
    }
    _filterReferences.clear();

  }
  public static StepFilter resolveStepFilterReferenceByName(String name, WdkModel wdkModel, String location) throws WdkModelException {
    FilterDefinition definition = (FilterDefinition) wdkModel.resolveReference(name);
    if (definition instanceof StepFilterDefinition) {
      return ((StepFilterDefinition) definition).getStepFilter();
    }
    else {
      throw new WdkModelException("The filter ref '" + name + "', declared at " + location + ", is not a stepFilter.");
    }
  }

  /**
   * A bulk query is either an original attribute or table query, that is, it either doesn't any param, or
   * just one param with the name of Utilities.PARAM_USER_ID.
   */
  void validateBulkQuery(Query query) throws WdkModelException {
    validateQuery(query);

    // Further limit the attribute/table query to have only user_id param
    // (optional). This is required to enable bulk query rewriting.
    String message = "Bulk query '" + query.getFullName() + "' can have only a '" + Utilities.PARAM_USER_ID +
        "' param, and it is optional.";
    Param[] params = query.getParams();
    if (params.length > 1)
      throw new WdkModelException(message);
    else if (params.length == 1 && !params[0].getName().equals(Utilities.PARAM_USER_ID))
      throw new WdkModelException(message);
  }

  /**
   * validate a query, and make sure it returns primary key columns, and the params of it can have only
   * primary_key-column-mapped params (optional) and user_id param (optional).
   */
  void validateQuery(Query query) throws WdkModelException {
    String[] pkColumns = primaryKeyDefinition.getColumnRefs();
    Map<String, String> pkColumnMap = new LinkedHashMap<>();
    for (String column : pkColumns)
      pkColumnMap.put(column, column);

    // make sure the params contain only primary key params, and (optional)
    // user_id param; but they can have less params than primary key
    // columns. WDK will append the missing ones automatically.
    for (Param param : query.getParams()) {
      String paramName = param.getName();
      if (paramName.equals(Utilities.PARAM_USER_ID))
        continue;
      if (!pkColumnMap.containsKey(paramName))
        throw new WdkModelException("The attribute or table query " + query.getFullName() + " has param " +
            paramName + ", and it doesn't match with any of the primary key " + "columns.");
    }

    // make sure the attribute/table query returns primary key columns
    Map<String, Column> columnMap = query.getColumnMap();
    for (String column : primaryKeyDefinition.getColumnRefs()) {
      if (!columnMap.containsKey(column))
        throw new WdkModelException("The query " + query.getFullName() + " of " +
            getFullName() + " doesn't return the required primary key column " + column);
    }
  }

  public void setResources(WdkModel wdkModel) {
    // set the resource in reporter
    for (ReporterRef reporter : reporterMap.values()) {
      reporter.setResources(wdkModel);
    }
  }

  private Map<String, AttributeField> sortAllAttributes() throws WdkModelException {
    String[] orderedAtts = attributeOrdering.split(",");
    Map<String, AttributeField> orderedAttsMap = new LinkedHashMap<>();

    // primaryKey first
    orderedAttsMap.put(idAttributeField.getName(), idAttributeField);

    for (String nextAtt : orderedAtts) {
      nextAtt = nextAtt.trim();
      if (!orderedAttsMap.containsKey(nextAtt)) {
        AttributeField nextAttField = attributeFieldMap.get(nextAtt);

        if (nextAttField == null) {
          String message = "RecordClass " + getFullName() + " defined attribute " + nextAtt + " in its " +
              "attribute ordering, but that is not a valid " + "attribute for this RecordClass";
          throw new WdkModelException(message);
        }
        orderedAttsMap.put(nextAtt, nextAttField);
      }
    }
    // add all attributes not in the ordering
    for (String nextAtt : attributeFieldMap.keySet()) {
      if (!orderedAttsMap.containsKey(nextAtt)) {
        AttributeField nextField = attributeFieldMap.get(nextAtt);
        orderedAttsMap.put(nextAtt, nextField);
      }
    }
    return orderedAttsMap;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    // first add the default reporter; XML may want to override
    ReporterRef defaultReporterRef = DefaultJsonReporter.createReference();
    defaultReporterRef.excludeResources(projectId);
    reporterMap.put(DefaultJsonReporter.RESERVED_NAME, defaultReporterRef);

    // exclude reporters
    boolean defaultOverridden = false;
    for (ReporterRef reporter : reporterList) {
      if (reporter.include(projectId)) {
        reporter.excludeResources(projectId);
        String reporterName = reporter.getName();
        if (reporterMap.containsKey(reporterName)) {
          if (reporterName.equals(DefaultJsonReporter.RESERVED_NAME) && !defaultOverridden) {
            LOG.warn("A reporter in recordClass " + getFullName() +
                " is overriding the default WDK reporter by using the name '" +
                DefaultJsonReporter.RESERVED_NAME + "'.");
            defaultOverridden = true;
          }
          // disallow duplicate definition
          else throw new WdkModelException("Reporter name '" + reporterName + "' is duplicated in recordClass " + getFullName());
        }
        reporterMap.put(reporterName, reporter);
      }
    }
    reporterList = null;

    // exclude primary key definitions and ensure exactly one remains
    for (PrimaryKeyDefinition pkDef : primaryKeyDefinitionList) {
      if (pkDef.include(projectId)) {
        if (primaryKeyDefinition != null) {
          // already found one that should be included
          throw new WdkModelException("Found more than one included <primaryKey> value in recordClass " + getFullName());
        }
        primaryKeyDefinition = pkDef;
      }
    }
    if (primaryKeyDefinition == null) {
      throw new WdkModelException("After exclusion, the primaryKey of recordClass " + getFullName() +
          " is not set.  Please define a <primaryKey> tag in the recordClass for project '" + projectId + "'.");
    }

    // exclude primary key children
    primaryKeyDefinition.excludeResources(projectId);

    // exclude attributes
    for (AttributeField field : attributeFieldList) {
      if (field.include(projectId)) {
        field.excludeResources(projectId);
        String fieldName = field.getName();
        // make sure only one ID attribute field exists
        if (field instanceof IdAttributeField) {
          if (this.idAttributeField != null)
            throw new WdkModelException("More than one ID attribute field present in recordClass " + getFullName());
          this.idAttributeField = (IdAttributeField) field;
        }
        // make sure PK column fields and only PK column fields match PK columns
        if (field instanceof PkColumnAttributeField && !primaryKeyDefinition.hasColumn(fieldName)) {
          throw new WdkModelException("PkColumnAttributes can only be defined with the name of a PK column, not " + fieldName + " in recordClass " + getFullName());
        }
        if (!(field instanceof PkColumnAttributeField) && primaryKeyDefinition.hasColumn(fieldName)) {
          throw new WdkModelException("Only PkColumnAttributes can be defined with the name of a PK column.");
        }
        if (attributeFieldMap.containsKey(fieldName)) {
          throw new WdkModelException("The attribute " + fieldName + " is duplicated in recordClass " + getFullName());
        }
        if (tableFieldsMap.containsKey(fieldName)) {
          throw new WdkModelException("The attribute " + fieldName + " has the same name as a table in the recordClass " + getFullName());
        }
        attributeFieldMap.put(fieldName, field);
      }
    }
    attributeFieldList = null;

    // make sure there is an ID attribute
    if (idAttributeField == null) {
      throw new WdkModelException("The idAttribute of recordClass " + getFullName() +
          " is not set. Please define a <idAttribute> tag in the recordClass.");
    }

    // exclude table fields
    for (TableField field : tableFieldList) {
      if (field.include(projectId)) {
        field.excludeResources(projectId);
        String fieldName = field.getName();
        if (tableFieldsMap.containsKey(fieldName))
          throw new WdkModelException("The table " + fieldName + " is duplicated in recordClass " +
              getFullName());
        if (attributeFieldMap.containsKey(fieldName))
          throw new WdkModelException("The table" + fieldName +
              " has the same name as an attribute in the recordClass " + getFullName());


        tableFieldsMap.put(fieldName, field);
      }
    }
    tableFieldList = null;

    // exclude query refs
    Map<String, AttributeQueryReference> attributesQueryRefs = new LinkedHashMap<>();
    for (AttributeQueryReference queryRef : attributesQueryRefList) {
      if (queryRef.include(projectId)) {
        String refName = queryRef.getTwoPartName();
        if (attributesQueryRefs.containsKey(refName)) {
          throw new WdkModelException("recordClass " + getFullName() +
              " has more than one attributeQueryRef \"" + refName + "\"");
        }
        else {
          queryRef.excludeResources(projectId);
          attributesQueryRefs.put(refName, queryRef);
        }
      }
    }
    attributesQueryRefList.clear();
    attributesQueryRefList.addAll(attributesQueryRefs.values());

    // exclude paramValuesSets
    for (ParamValuesSet pvs : unexcludedParamValuesSets) {
      if (pvs.include(projectId)) {
        if (paramValuesSet != null)
          throw new WdkModelException("Duplicate <paramErrors> included in record class " + getName() +
              " for projectId " + projectId);
        paramValuesSet = pvs;

      }
    }

    // exclude summary and sorting attribute list
    boolean hasAttributeList = false;
    for (AttributeList attributeList : attributeLists) {
      if (attributeList.include(projectId)) {
        if (hasAttributeList) {
          throw new WdkModelException("The question " + getFullName() +
              " has more than one <attributesList> for " + "project " + projectId);
        }
        else {
          this.defaultSummaryAttributeNames = attributeList.getSummaryAttributeNames();
          this.defaultSortingMap = attributeList.getSortingAttributeMap();
          hasAttributeList = true;
        }
      }
    }
    attributeLists = null;

    // exclude favorite references
    for (FavoriteReference favorite : favorites) {
      if (favorite.include(projectId)) {
        if (favoriteNoteFieldName != null)
          throw new WdkModelException("The favorite tag is " + "duplicated on the recordClass " +
              getFullName());
        this.favoriteNoteFieldName = favorite.getNoteField();
      }
    }
    favorites = null;

    // exclude the summary views
    Map<String, SummaryView> summaryViews = new LinkedHashMap<>();
    for (SummaryView view : summaryViewList) {
      if (view.include(projectId)) {
        view.excludeResources(projectId);
        String summaryViewName = view.getName();
        if (summaryViews.containsKey(summaryViewName))
          throw new WdkModelException("The summary view '" + summaryViewName + "' is duplicated in record " +
              getFullName());

        summaryViews.put(summaryViewName, view);
      }
    }
    summaryViewList = null;

    // add WDK supported views to all record classes, first
    for (SummaryView view : SummaryView.createSupportedSummaryViews(this)) {
      view.excludeResources(projectId);
      summaryViewMap.put(view.getName(), view);
    }

    // then add user defined views to override WDK supported ones
    for (SummaryView view : summaryViews.values()) {
      summaryViewMap.put(view.getName(), view);
    }

    // exclude step analyses
    for (StepAnalysisXml analysis : stepAnalysisList) {
      if (analysis.include(projectId)) {
        analysis.excludeResources(projectId);
        String stepAnalysisName = analysis.getName();
        if (stepAnalysisMap.containsKey(stepAnalysisName)) {
          throw new WdkModelException("The step analysis '" + stepAnalysisName + "' is duplicated in question " +
              getFullName());
        }
        stepAnalysisMap.put(stepAnalysisName, analysis);
      }
    }
    stepAnalysisList = null;

    // exclude the summary views
    Map<String, RecordView> recordViews = new LinkedHashMap<>();
    for (RecordView view : recordViewList) {
      if (view.include(projectId)) {
        view.excludeResources(projectId);
        String recordViewName = view.getName();
        if (recordViews.containsKey(recordViewName))
          throw new WdkModelException("The record view '" + recordViewName + "' is duplicated in record " +
              getFullName());

        recordViews.put(recordViewName, view);
      }
    }
    recordViewList = null;

    // add WDK supported views to all record classes first
    for (RecordView view : RecordView.createSupportedRecordViews()) {
      view.excludeResources(projectId);
      recordViewMap.put(view.getName(), view);
    }

    // then add user defined views to override WDK supported ones
    for (RecordView view : recordViews.values()) {
      recordViewMap.put(view.getName(), view);
    }

    // exclude filter references
    List<FilterReference> references = new ArrayList<>();
    for (FilterReference reference : _filterReferences) {
      if (reference.include(projectId)) {
        reference.excludeResources(projectId);
        references.add(reference);
      }
    }
    _filterReferences.clear();
    _filterReferences.addAll(references);

  }

  /**
   * Make sure all pk columns has a corresponding ColumnAttributeField
   */
  private void createPrimaryKeySubFields(String projectId) throws WdkModelException {
    String[] pkColumns = primaryKeyDefinition.getColumnRefs();
    LOG.debug("[" + getName() + "] Creating PK subfields for columns: " + FormatUtil.arrayToString(pkColumns));
    for (String pkColumnName : pkColumns) {
      if (attributeFieldMap.containsKey(pkColumnName)) {
        AttributeField pkColumnField = attributeFieldMap.get(pkColumnName);
        if (pkColumnField instanceof PkColumnAttributeField) {
          // model defined a PkColumnAttributeField for this column; don't
          // generate
          continue;
        }
        // model defined an attribute but NOT a pkColumnAttribute for this PK
        // column; error
        throw new WdkModelException("RecordClass [" + getFullName() +
          "] contains attribute [" + pkColumnName + "] with the same name as a primary key column.  " +
          "Columns declared in the primary key are automatically given internal PkColumnAttributeFields," +
          "or you may declare them as <pkColumnAttribute> to expose them or assign additional properties.");
      }

      // model did not define field for this PK column; create
      PkColumnAttributeField field = new PkColumnAttributeField();
      field.setName(pkColumnName);
      field.setInternal(true);
      field.setContainer(this);
      field.excludeResources(projectId);
      LOG.debug("Adding PkColumnAttributeField '" + pkColumnName + "' to '" + getFullName() + "'.");
      attributeFieldMap.put(pkColumnName, field);
    }
  }

  public void addAttributeList(AttributeList attributeList) {
    this.attributeLists.add(attributeList);
  }

  public Map<String, AttributeField> getSummaryAttributeFieldMap() {
    Map<String, AttributeField> attributeFields = new LinkedHashMap<>();

    // always put primary key as the first field
    attributeFields.put(idAttributeField.getName(), idAttributeField);

    if (!defaultSummaryAttributeFields.isEmpty()) {
      attributeFields.putAll(defaultSummaryAttributeFields);
    }
    else {
      // get the first N non-internal attributes
      for (AttributeField field : attributeFieldMap.values()) {
        if (FieldScope.NON_INTERNAL.isFieldInScope(field)) {
          attributeFields.put(field.getName(), field);
          if (attributeFields.size() >= Utilities.DEFAULT_SUMMARY_ATTRIBUTE_SIZE) {
            break;
          }
        }
      }
    }
    return attributeFields;
  }

  @Override
  public Map<String, Boolean> getSortingAttributeMap() {
    Map<String, Boolean> map = new LinkedHashMap<>();
    int count = 0;
    for (String attrName : defaultSortingMap.keySet()) {
      map.put(attrName, defaultSortingMap.get(attrName));
      count++;
      if (count >= UserPreferences.MAX_NUM_SORTING_COLUMNS)
        break;
    }

    // has to sort at least on something, primary key as default
    if (map.isEmpty()) {
      return getIdSortingAttributeMap();
    }

    return map;
  }

  public Map<String, Boolean> getIdSortingAttributeMap() {
    return new MapBuilder<String, Boolean>(new LinkedHashMap<>())
        .put(idAttributeField.getName(), true).toMap();
  }

  public String getChecksum() {
    return null;
  }

  public void setUseBasket(boolean useBasket) {
    this.useBasket = useBasket;
  }

  /**
   * @return if true, the basket feature will be available for this record type.
   */
  public boolean isUseBasket() {
    return useBasket;
  }

  /**
   * The real time question is used on the basket page to display the current
   * records in the basket.
   */
  public Question getRealtimeBasketQuestion() throws WdkModelException {
    return (Question) _wdkModel.resolveReference(String.format(
      "%s.%s%s",
      Utilities.INTERNAL_QUESTION_SET,
      getFullName().replace('.', '_'),
      BasketFactory.REALTIME_BASKET_QUESTION_SUFFIX
    ));
  }

  /**
   * The snapshot question is used when exporting basket to a strategy, and the
   * step will use this question to get a snapshot of those records in basket,
   * and store them in the
   */
  public Question getSnapshotBasketQuestion() throws WdkModelException {
    return (Question) _wdkModel.resolveReference(String.format(
      "%s.%s%s",
      Utilities.INTERNAL_QUESTION_SET,
      getFullName().replace('.', '_'),
      BasketFactory.SNAPSHOT_BASKET_QUESTION_SUFFIX
    ));
  }

  /**
   * @return the shortDisplayName
   */
  public String getShortDisplayName() {
    return (shortDisplayName != null) ? shortDisplayName : getDisplayName();
  }

  public String getNativeShortDisplayName() {
    return (nativeShortDisplayName != null)
      ? nativeShortDisplayName
      : getNativeDisplayName();
  }

  /**
   * @param shortDisplayName
   *          the shortDisplayName to set
   */
  public void setShortDisplayName(String shortDisplayName) {
    this.shortDisplayName = shortDisplayName;
  }

  public void addFavorite(FavoriteReference favorite) {
    this.favorites.add(favorite);
  }

  public AttributeField getFavoriteNoteField() {
    return favoriteNoteField;
  }

  public Map<String, SummaryView> getSummaryViews() {
    return new LinkedHashMap<>(summaryViewMap);
  }

  public SummaryView getSummaryView(String viewName) throws WdkUserException {
    if (summaryViewMap.containsKey(viewName)) {
      return summaryViewMap.get(viewName);
    }
    else {
      throw new WdkUserException("Unknown summary view for record class " + "["
        + getFullName() + "]: " + viewName);
    }
  }

  public void addSummaryView(SummaryView view) {
    if (summaryViewList == null)
      summaryViewMap.put(view.getName(), view);
    else
      summaryViewList.add(view);
  }

  public Map<String, StepAnalysis> getStepAnalyses() {
    return new LinkedHashMap<>(stepAnalysisMap);
  }

  public StepAnalysis getStepAnalysis(String analysisName) throws WdkUserException {
    if (stepAnalysisMap.containsKey(analysisName)) {
      return stepAnalysisMap.get(analysisName);
    }
    else {
      throw new WdkUserException("Unknown step analysis for record class " + "["
        + getFullName() + "]: " + analysisName);
    }
  }

  public void addStepAnalysis(StepAnalysisXml analysis) {
    stepAnalysisList.add(analysis);
  }

  public Map<String, RecordView> getRecordViews() {
    return new LinkedHashMap<>(recordViewMap);
  }

  public RecordView getRecordView(String viewName) throws WdkUserException {
    if (recordViewMap.containsKey(viewName)) {
      return recordViewMap.get(viewName);
    }
    else {
      throw new WdkUserException("Unknown record view for record class " + "[" +
        getFullName() + "]: " + viewName);
    }
  }

  public RecordView getDefaultRecordView() {
    for (RecordView view : recordViewMap.values()) {
      if (view.isDefault())
        return view;
    }

    if (!recordViewMap.isEmpty())
      return recordViewMap.values().iterator().next();

    return null;
  }

  public void addRecordView(RecordView view) {
    if (recordViewList == null)
      recordViewMap.put(view.getName(), view);
    else
      recordViewList.add(view);
  }

  public boolean hasMultipleRecords(User user, Map<String, Object> pkValues)
      throws WdkModelException, RecordNotFoundException {
    List<Map<String, Object>> records = lookupPrimaryKeys(user, pkValues);
    return records.size() > 1;
  }

  /**
   * use alias query to lookup old ids and convert to new ids
   */
  public List<Map<String, Object>> lookupPrimaryKeys(User user, Map<String, Object> pkValues)
      throws WdkModelException, RecordNotFoundException {
    return primaryKeyDefinition.lookUpPrimaryKeys(user, pkValues);
  }

  public String[] getIndexColumns() {
    // only need to index the pk columns;
    return primaryKeyDefinition.getColumnRefs();
  }

  public final void printDependency(PrintWriter writer, String indent) throws WdkModelException {
    writer.println(indent + "<recordClass name=\"" + getName() + "\">");
    String indent1 = indent + WdkModel.INDENT;
    String indent2 = indent1 + WdkModel.INDENT;

    // print attributes
    if (!attributeFieldMap.isEmpty()) {
      writer.println(indent1 + "<attributes size=\"" + attributeFieldMap.size() + "\">");
      String[] attributeNames = attributeFieldMap.keySet().toArray(new String[0]);
      Arrays.sort(attributeNames);
      for (String attributeName : attributeNames) {
        attributeFieldMap.get(attributeName).printDependency(writer, indent2);
      }
      writer.println(indent1 + "</attributes>");
    }

    // print attribute queries
    if (!attributeQueries.isEmpty()) {
      writer.println(indent1 + "<attributeQueries size=\"" + attributeQueries.size() + "\">");
      String[] queryNames = attributeQueries.keySet().toArray(new String[0]);
      Arrays.sort(queryNames);
      for (String queryName : queryNames) {
        attributeQueries.get(queryName).printDependency(writer, indent2);
      }
      writer.println(indent1 + "</attributeQueries>");
    }

    // print tables
    if (!tableFieldsMap.isEmpty()) {
      writer.println(indent1 + "<tables size=\"" + tableFieldsMap.size() + "\">");
      String[] tableNames = tableFieldsMap.keySet().toArray(new String[0]);
      Arrays.sort(tableNames);
      for (String tableName : tableNames) {
        tableFieldsMap.get(tableName).printDependency(writer, indent2);
      }
      writer.println(indent1 + "</tables>");
    }

    writer.println(indent + "</recordClass>");
  }

  public void addFilterReference(FilterReference reference) {
    _filterReferences.add(reference);
  }

  public void addStepFilter(StepFilter filter) {
    _stepFilters.put(filter.getKey(), filter);
  }

  /**
   * try to find a filter with the associated key.
   *
   * @return null if not found
   */
  public Filter getFilter(String key) {
    return _stepFilters.get(key);
  }

  public Map<String, StepFilter> getStepFilters() {
    return new LinkedHashMap<>(_stepFilters);
  }

  /**
   * Returns a set of filters (by name) for this question.  Only non-view-only
   * filters are included in this list.  View-only filters are only available
   * by name.
   *
   * @return map of all non-view-only filters, from filter name to filter
   */
  public Map<String, Filter> getFilters() {
    // get all step filters
    LOG.debug("RECORDCLASS: GETTING ALL FILTERs");
    Map<String, Filter> filters = new LinkedHashMap<>();
    for (StepFilter filter : _stepFilters.values()) {
      if (!filter.getFilterType().isViewOnly()) {
        LOG.debug("RECORDCLASS: filter name: " + filter.getKey());
        filters.put(filter.getKey(), filter);
      }
    }
    return filters;
  }

  public IdAttributeField getIdAttributeField() {
    return idAttributeField;
  }

  public boolean idAttributeHasNonPkMacros() throws WdkModelException {
    List<String> idAttrRefs = Functions.mapToList(idAttributeField.getDependencies(), Named.TO_NAME);
    List<String> pkColumnRefs = Arrays.asList(primaryKeyDefinition.getColumnRefs());
    for (String idAttrRef : idAttrRefs) {
      if (!pkColumnRefs.contains(idAttrRef)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getNameForLogging() {
    return getFullName();
  }

  /**
   * Resolves any custom snapshot plugin and returns it or, if none specified,
   * returns an instance of the default plugin.  This code should probably live
   * in resolveReferences() but cannot since the snapshot query is needed
   * before resolveReferences is called.
   * 
   * @return basket snapshot query plugin for this record class
   * @throws WdkModelException if unable to resolve class
   */
  public BasketSnapshotQueryPlugin getBasketSnapshotQueryPlugin() throws WdkModelException {
    if (_customSnapshotBasketQueryPluginClassName == null) {
      return new BasketSnapshotQueryPlugin().setRecordClass(this);
    }
    else {
      try {
        Class<? extends BasketSnapshotQueryPlugin> clazz =
            Class.forName(_customSnapshotBasketQueryPluginClassName).asSubclass(BasketSnapshotQueryPlugin.class);
        return clazz.getDeclaredConstructor().newInstance().setRecordClass(this);
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
          IllegalArgumentException | InvocationTargetException |
          NoSuchMethodException | SecurityException ex) {
        throw new WdkModelException("Can't create java class for " +
            "customSnapshotBasketQueryPluginClassName from class name '" +
            _customSnapshotBasketQueryPluginClassName + "'", ex);
      }
    }
  }
}
