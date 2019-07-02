package org.gusdb.wdk.model.answer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.DynamicRecordInstanceList;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.FilterOption;
import org.gusdb.wdk.model.answer.spec.FilterOptionList;
import org.gusdb.wdk.model.answer.spec.ParamsAndFiltersDbColumnFormat;
import org.gusdb.wdk.model.answer.stream.PagedAnswerRecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.ColumnFilterSqlBuilder;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

import static org.gusdb.fgputil.StringUtil.indent;

/**
 * <p>
 * A list of {@link RecordInstance}s representing one page of the answer to a
 * {@link Question}. The constructor of the Answer provides a handle ( {@link
 * QueryInstance}) on the {@link ResultList} that is the list of primary keys
 * for the all the records (not * just one page) that are the answer to the
 * {@link Question}. The {@link ResultList} also has a column that contains the
 * row number (RESULT_TABLE_I) so that a list of primary keys for a single page
 * can be efficiently accessed.
 *
 * <p>
 * The AnswerValue is lazy in that it only constructs the set of {@link
 * RecordInstance}s for the page when the first RecordInstance is requested.
 *
 * <p>
 * The initial request triggers the creation of skeletal {@link RecordInstance}s
 * for the page. They contain only primary keys (these being acquired from the
 * {@link ResultList}).
 *
 * <p>
 * These skeletal {@link RecordInstance}s are also lazy in that they only run an
 * attributes {@link Query} when an attribute provided by that query is
 * requested. When they do run an attribute query, its {@link QueryInstance} is
 * put into joinMode. This means that the attribute query joins with the table
 * containing the primary keys, and, in one database query, generates rows
 * containing the attribute values for all the {@link RecordInstance}s in the
 * page.
 *
 * <p>
 * similar lazy loading can be applied to table {@link Query} too.
 *
 * <p>
 * The method {@code AnswerValue#integrateAttributesQuery} is invoked by the
 * first RecordInstance in the page upon the first request for an attribute
 * provided by an attributes query. The query is a join with the list of primary
 * keys, and so has a row for each {@link RecordInstance} in the page, and
 * columns that provide the attribute values (plus RESULT_TABLE_I). The values
 * in the rows are integrated into the corresponding {@link RecordInstance} (now
 * no longer skeletal). {@code AnswerValue#integrateAttributesQuery} may be
 * called a number of times, depending upon how many attribute queries the
 * {@link RecordClass} contains.
 *
 * <p>
 * Attribute queries are guaranteed to provide one row for each {@link
 * RecordInstance} in the page. An exception is thrown otherwise.
 *
 * <p>
 * During a standard load of an AnswerValue, we do the following: 1. Apply
 * filter to the IDs in the cache (FilterInstance takes SQL for IDs, wraps to
 * filter, and returns) 2. Apply sorting (AnswerValue takes SQL from filter,
 * join with appropriate attribute queries, wrap to sort, and returns) 3. Apply
 * paging (add rownum, etc. to SQL) Then run SQL!!  Creates template
 * RecordInstances (non populated with attributes) 4. Apply SQL from step 3,
 * join with attribute queries to build results to return to user Attribute
 * fetch is lazy-loaded, but cache all attributes from attribute query (could be
 * big e.g. BFMV), even if don't need all of them
 *
 * @since Fri June 4 13:01:30 2004 EDT
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */
public class AnswerValue {

  private static final Logger LOG = Logger.getLogger(AnswerValue.class);

  public static final int UNBOUNDED_END_PAGE_INDEX = -1;

  // ------------------------------------------------------------------
  // Instance variables
  // ------------------------------------------------------------------

  // basic information about this answer
  protected final User _user;
  private final RunnableObj<AnswerSpec> _validAnswerSpec;
  protected final AnswerSpec _answerSpec;

  // values derived from basic info
  private final WdkModel _wdkModel;
  private final QueryInstance<?> _idsQueryInstance;
  private final AnswerValueAttributes _attributes;
  protected ResultSizeFactory _resultSizeFactory; // may be reassigned by subclasses

  // sorting and paging for this answer- may be modified externally
  private int _startIndex;
  private int _endIndex;
  private Map<String, Boolean> _sortingMap;

  // values generated and cached from the above
  private String _sortedIdSql;
  private String _checksum;

  // ------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------

  /**
   * @param startIndex
   *   The index of the first <code>RecordInstance</code> in the page. (>=1)
   * @param endIndex
   *   The index of the last <code>RecordInstance</code> in the page,
   *   inclusive.
   */
  public AnswerValue(User user, RunnableObj<AnswerSpec> validAnswerSpec, int startIndex,
      int endIndex, Map<String, Boolean> sortingMap) throws WdkModelException {
    _user = user;
    _validAnswerSpec = validAnswerSpec;
    _answerSpec = validAnswerSpec.get();
    _wdkModel = _answerSpec.getWdkModel();
    _idsQueryInstance = Query.makeQueryInstance(_answerSpec.getQueryInstanceSpec().getRunnable().getLeft());
    Question question = _answerSpec.getQuestion();
    _attributes = new AnswerValueAttributes(_user, question);
    _resultSizeFactory = new ResultSizeFactory(this);

    _startIndex = startIndex;
    _endIndex = endIndex;

    // get sorting columns
    if (sortingMap == null) {
      sortingMap = question.getSortingAttributeMap();
    }
    _sortingMap = sortingMap;

    LOG.debug("AnswerValue created for question: " + question.getDisplayName());
  }

  @Override
  public AnswerValue clone() {
    return new AnswerValue(this, this._startIndex, this._endIndex);
  }

  public AnswerValue cloneWithNewPaging(int startIndex, int endIndex) {
    return new AnswerValue(this, startIndex, endIndex);
  }

  /**
   * A copy constructor with start- and end-index modification
   * <p>
   * <b>NOTE</b>: DO NOT make this constructor public; continue to use clone
   * methods since they are overridden in SingleRecordAnswerValue and must be
   * honored.
   *
   * @param answerValue
   *   source answer value
   * @param startIndex
   *   1-based start index (inclusive)
   * @param endIndex
   *   end index (inclusive), or a negative value for all records
   */
  private AnswerValue(AnswerValue answerValue, int startIndex, int endIndex) {
    _user = answerValue._user;
    _validAnswerSpec = answerValue._validAnswerSpec;
    _answerSpec = answerValue._answerSpec;
    _wdkModel = answerValue._wdkModel;
    _idsQueryInstance = answerValue._idsQueryInstance;
    _attributes = new AnswerValueAttributes(_user, _answerSpec.getQuestion());
    // Note: do not copy result size data (i.e. _resultSizesByFilter and
    //   _resultSizesByProject); they are essentially caches and should be
    //   rebuilt by each new AnswerValue
    _resultSizeFactory = new ResultSizeFactory(this);
    _startIndex = startIndex;
    _endIndex = endIndex;
    _sortingMap = new LinkedHashMap<>(answerValue._sortingMap);
  }

  public User getUser() {
    return _user;
  }

  public RunnableObj<AnswerSpec> getRunnableAnswerSpec() {
    return _validAnswerSpec;
  }

  public AnswerSpec getAnswerSpec() {
    return _answerSpec;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public QueryInstance<?> getIdsQueryInstance() {
    return _idsQueryInstance;
  }

  public AnswerValueAttributes getAttributes() {
    return _attributes;
  }

  public ResultSizeFactory getResultSizeFactory() {
    return _resultSizeFactory;
  }

  /**
   * @return Map where key is param display name and value is param value
   */
  public Map<String, String> getParamDisplays() {
    Map<String, String> displayParamsMap = new LinkedHashMap<>();
    ReadOnlyMap<String, String> paramsMap = _idsQueryInstance.getParamStableValues();
    Param[] params = _answerSpec.getQuestion().getParams();
    for (Param param : params) {
      displayParamsMap.put(param.getPrompt(), paramsMap.get(param.getName()));
    }
    return displayParamsMap;
  }

  /**
   * the checksum of the iq query, plus the filter information on the answer.
   */
  public String getChecksum() throws WdkModelException {
    if (_checksum == null) {
      JSONObject jsContent = new JSONObject();
      jsContent.put("query-checksum", _idsQueryInstance.getChecksum());

      // add the legacy filter into the content
      if (_answerSpec.getLegacyFilter().isPresent()) {
        jsContent.put("legacy-filter", _answerSpec.getLegacyFilterName().get());
      }

      // if filters have been applied, get the content for it
      jsContent.put("filters", ParamsAndFiltersDbColumnFormat.formatFilters(_answerSpec.getFilterOptions()));
      // FIXME: decide whether view filters should also be added to checksum here

      // encrypt the content to make step-independent checksum
      _checksum = EncryptionUtil.encrypt(JsonUtil.serialize(jsContent));
    }
    return _checksum;
  }

  /**
   * Convenience method to provide a page of DynamicRecordInstances.  Future code
   * should explicitly create a DynamicRecordInstanceMap from this answer value.
   *
   * @return page of dynamic records as an array
   */
  public RecordInstance[] getRecordInstances() throws WdkModelException, WdkUserException {
    return new DynamicRecordInstanceList(this).values()
      .toArray(new RecordInstance[0]);
  }

  // FIXME!!!  Look at how this is called- we should not be using PagedAnswerRecordStream!
  // seems to be called in XmlQuestion.java, XmlAnswerService.java and DatasetParamHandler.java
  /**
   * Iterate through all the records of the answer
   *
   * @return record stream of this answer value
   */
  public RecordStream getFullAnswer() {
    return new PagedAnswerRecordStream(this, 200);
  }

  /**
   * This method returns a paged attribute sql query.  It is
   * now public because the FileBasedRecordStream object
   * uses it to acquire an sql statement to execute.
   */
  public String getPagedAttributeSql(Query attributeQuery, boolean sortPage)
  throws WdkModelException {
    LOG.debug("AnswerValue: getPagedAttributeSql(): " +
      attributeQuery.getFullName() + " --boolean sortPage: " + sortPage);

    // get the paged SQL of id query
    String idSql = getPagedIdSql(false, sortPage);

    // combine the id query with attribute query
    String attributeSql = getAttributeSql(attributeQuery);
    StringBuilder sql = new StringBuilder(" /* the desired attributes, for a page of sorted results */ "
        + " SELECT aq.* FROM (");
    sql.append(idSql);
    sql.append(") pidq, (/* attribute query that returns attributes in a page */ ").append(attributeSql).append(
        ") aq WHERE ");

    boolean firstColumn = true;
    for (String column : getAnswerSpec().getQuestion().getRecordClass().getPrimaryKeyDefinition().getColumnRefs()) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append(" AND ");
      sql.append("aq.").append(column).append(" = pidq.").append(column);
    }
    if (sortPage) {
      // sort by underlying idq row_index if requested
      sql.append(" ORDER BY pidq.row_index");
    }
    LOG.debug("AnswerValue: getPagedAttributeSql(): " + sql.toString());
    return sql.toString();

  }

  public ResultList getTableFieldResultList(TableField tableField) throws WdkModelException {

    WdkModel wdkModel = _answerSpec.getQuestion().getWdkModel();

    // has to get a clean copy of the attribute query, without pk params appended
    Query tableQuery = tableField.getUnwrappedQuery();

    // get and run the paged table query sql
    LOG.debug("AnswerValue: getTableFieldResultList(): going to getPagedTableSql()");
    String sql = getPagedTableSql(tableQuery);
    LOG.debug("AnswerValue: getTableFieldResultList(): back from getPagedTableSql()");
    DatabaseInstance platform = wdkModel.getAppDb();
    DataSource dataSource = platform.getDataSource();
    ResultSet resultSet = null;
    try {
      LOG.debug("AnswerValue: getTableFieldResultList(): returning SQL for TableField '" + tableField.getName() + "': \n" + sql);
      resultSet = SqlUtils.executeQuery(dataSource, sql, tableQuery.getFullName() + "_table-paged");
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }

    return new SqlResultList(resultSet);
  }

  public String getPagedTableSql(Query tableQuery) throws WdkModelException {
    // get the paged SQL of id query
    String idSql = getPagedIdSql();

    // Combine the id query with table query.  Make an instance from the
    // original table query; a table query has only one param, user_id. Note
    // that the original table query is different from the table query held by
    // the recordClass.  The user_id param will be added by the query instance.
    RunnableObj<QueryInstanceSpec> tableQuerySpec = QueryInstanceSpec.builder()
      .buildRunnable(_user, tableQuery, StepContainer.emptyContainer());
    String tableSql = Query.makeQueryInstance(tableQuerySpec).getSql();

    DBPlatform platform = _wdkModel.getAppDb().getPlatform();
    String tableSqlWithRowIndex = "(SELECT tq.*, " + platform.getRowNumberColumn() + " as row_index FROM (" + tableSql + ") tq ";
    StringBuilder sql = new StringBuilder()
        .append("SELECT tqi.* FROM (")
        .append(idSql)
        .append(") pidq, ")
        .append(tableSqlWithRowIndex)
        .append(") tqi WHERE ");

    boolean firstColumn = true;
    for (String column : getAnswerSpec().getQuestion().getRecordClass().getPrimaryKeyDefinition().getColumnRefs()) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append(" AND ");
      sql.append("tqi.").append(column).append(" = pidq.").append(column);
    }
    sql.append(" ORDER BY pidq.row_index, tqi.row_index");

    // replace the id_sql macro.  this sql must include filters (but not view filters)
    String sqlWithIdSql = sql.toString().replace(Utilities.MACRO_ID_SQL, getPagedIdSql(true, true));
    return sqlWithIdSql.replace(Utilities.MACRO_ID_SQL_NO_FILTERS, "(" + getNoFiltersIdSql() + ")");
  }

  public String getUnfilteredAttributeSql(Query attrQuery)
  throws WdkModelException {
    String queryName = attrQuery.getFullName();
    Query dynaQuery = _answerSpec.getQuestion().getDynamicAttributeQuery();
    String sql;
    if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
      // the dynamic query doesn't have sql defined, the sql will be
      // constructed from the id query cache table.
      sql = getBaseIdSql();
    } else {
      // Make an instance from the original attribute query; an attribute
      // query has only one param, user_id. Note that the original
      // attribute query is different from the attribute query held by the
      // recordClass.  The user_id param will be added by the builder.
      // TODO: decide if construction of this validated spec should be moved elsewhere?
      RunnableObj<QueryInstanceSpec> attrQuerySpec = QueryInstanceSpec.builder()
        .buildValidated(_user, attrQuery, StepContainer.emptyContainer(),
          ValidationLevel.RUNNABLE, FillStrategy.NO_FILL)
        .getRunnable()
        .getOrThrow(spec -> new WdkModelException(
          "Attribute query spec found invalid: " + spec.getValidationBundle().toString()));

      var fSql = getNoFiltersIdSql();
      // get attribute query sql from the instance
      sql = Query.makeQueryInstance(attrQuerySpec).getSql()
        // replace the id sql macro.
        // the injected sql must include filters (but not view filters)
        .replace(Utilities.MACRO_ID_SQL, fSql)
        // replace the no-filters id sql macro.
        // the injected sql must NOT include filters
        // (but should return any dynamic columns)
        .replace(Utilities.MACRO_ID_SQL_NO_FILTERS,  "(" + fSql + ")");
    }
    return sql;
  }

  public String getFilteredAttributeSql(
    final Query attrQuery,
    final boolean sort
  ) throws WdkModelException {
    final var wrapped = joinToIds(getAttributeSql(attrQuery));

    if (!sort)
      return wrapped;

    final var cols = getSortingColumns()
      .stream()
      .filter(spec -> spec.getItem() instanceof QueryColumnAttributeField)
      .iterator();

    if (!cols.hasNext())
      return wrapped;

    final var out = new StringBuilder(wrapped).append("\nORDER BY\n inq.");

    boolean first = true;
    while (cols.hasNext()) {
      final var spec = cols.next();

      if (!first)
        out.append("\n, inq.");

      out.append(spec.getItemName())
        .append(' ')
        .append(spec.getDirection().toString());
      first=false;
    }

    return out.toString();
  }

  public String getAttributeSql(Query attributeQuery) throws WdkModelException {
    String queryName = attributeQuery.getFullName();
    Query dynaQuery = _answerSpec.getQuestion().getDynamicAttributeQuery();
    String sql;
    if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
      // the dynamic query doesn't have sql defined, the sql will be
      // constructed from the id query cache table.
      sql = getBaseIdSql();
    }
    else {
      // Make an instance from the original attribute query; an attribute
      // query has only one param, user_id. Note that the original
      // attribute query is different from the attribute query held by the
      // recordClass.  The user_id param will be added by the builder.
      // TODO: decide if construction of this validated spec should be moved elsewhere?
      RunnableObj<QueryInstanceSpec> attrQuerySpec = QueryInstanceSpec.builder()
          .buildValidated(_user, attributeQuery, StepContainer.emptyContainer(),
              ValidationLevel.RUNNABLE, FillStrategy.NO_FILL)
          .getRunnable()
          .getOrThrow(spec -> new WdkModelException(
              "Attribute query spec found invalid: " + spec.getValidationBundle().toString()));

      // get attribute query sql from the instance
      sql = Query.makeQueryInstance(attrQuerySpec).getSql()
        // replace the id sql macro.
        // the injected sql must include filters (but not view filters)
        .replace(Utilities.MACRO_ID_SQL, getIdSql(null, true))
        // replace the no-filters id sql macro.
        // the injected sql must NOT include filters
        // (but should return any dynamic columns)
        .replace(Utilities.MACRO_ID_SQL_NO_FILTERS,  "(" + getNoFiltersIdSql() + ")");
    }
    return sql;
  }

  protected String getNoFiltersIdSql() throws WdkModelException {
    return _idsQueryInstance.getSql();
  }

  public String getSortedIdSql() throws WdkModelException {
      if (_sortedIdSql == null) _sortedIdSql = getSortedIdSql(false);
      return _sortedIdSql;
  }

  private String getSortedIdSql(boolean excludeViewFilters) throws WdkModelException {
    LOG.debug("AnswerValue: getSortedIdSql()");
    String[] pkColumns = _answerSpec.getQuestion().getRecordClass().getPrimaryKeyDefinition().getColumnRefs();

    // get id sql
    String idSql = getIdSql(null, excludeViewFilters);

    // get sorting attribute queries
    Map<String, String> attributeSqls = new LinkedHashMap<>();
    List<String> orderClauses = new ArrayList<>();
    prepareSortingSqls(attributeSqls, orderClauses);

    StringBuilder sql = new StringBuilder("\n/* the ID query results, sorted */"
      + "\nSELECT\n  ");
    boolean firstColumn = true;
    for (String pkColumn : pkColumns) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append("\n, ");
      sql.append("idq.").append(pkColumn);
    }

    sql.append("\nFROM\n").append(idSql).append(" idq");
    // add all tables involved
    for (String shortName : attributeSqls.keySet()) {
      sql.append("\n, (\n")
        .append(indent(attributeSqls.get(shortName)))
        .append(") ")
        .append(shortName);
    }

    // add primary key join conditions
    boolean firstClause = true;
    for (String shortName : attributeSqls.keySet()) {
      for (String column : pkColumns) {
        if (firstClause) {
          sql.append("\nWHERE\n  ");
          firstClause = false;
        }
        else
          sql.append("\n  AND ");

        sql.append("idq.").append(column)
          .append(" = ").append(shortName)
          .append(".").append(column);
      }
    }

    // add order clause
    // always append primary key columns as the last sorting columns,
    // otherwise Oracle may generate unstable results through pagination
    // when the sorted columns are not unique.
    sql.append("\nORDER BY\n  ");
    for (String clause : orderClauses)
      sql.append(clause).append("\n, ");

    firstClause = true;
    for (String column : pkColumns) {
      if (firstClause)
        firstClause = false;
      else
        sql.append("\n, ");
      sql.append("idq.").append(column);
    }

    String outputSql = sql.toString();
    LOG.debug("AnswerValue: getSortedIdSql(): Constructed the following sorted ID SQL: " + outputSql);
    return outputSql;
  }

  public String getPagedIdSql() throws WdkModelException {
    return getPagedIdSql(false, false);
  }

  private String getPagedIdSql(boolean excludeViewFilters, boolean includeRowIndex) throws WdkModelException {
    String sortedIdSql = getSortedIdSql(excludeViewFilters);
    DatabaseInstance platform = _answerSpec.getQuestion().getWdkModel().getAppDb();
    String sql = platform.getPlatform().getPagedSql(sortedIdSql, _startIndex, _endIndex, includeRowIndex);

    // add comments to the sql
    sql = "\n/* a page of sorted ids */\n" + sql;

    LOG.debug("AnswerValue: getPagedIdSql() : paged id sql constructed: " + sql);

    return sql;
  }

  public String getIdSql() throws WdkModelException {
    return getIdSql(null, false);
  }

  private String getBaseIdSql() throws WdkModelException {
    // get base ID sql from query instance
    String innerSql = "\n/* the ID query */\n" + _idsQueryInstance.getSql();

    // add answer param columns
    return "\n/* answer param value cols applied on id query */\n"
      + applyAnswerParams(innerSql, _answerSpec.getQuestion().getParamMap(),
        _idsQueryInstance.getParamStableValues());
  }

  protected String getIdSql(String excludeFilter, boolean excludeViewFilters) throws WdkModelException {

    // get base ID sql from query instance and answer params
    String innerSql = getBaseIdSql();

    // apply old-style answer filter
    if (_answerSpec.getLegacyFilter().isPresent()) {
      innerSql = _answerSpec.getLegacyFilter().get().applyFilter(_user, innerSql, _idsQueryInstance.getAssignedWeight());
      innerSql = "\n/* old filter applied on id query */\n" + innerSql;
    }

    // apply "new" filters
    if (!_answerSpec.getFilterOptions().isEmpty()) {
      innerSql = applyFilters(innerSql, _answerSpec.getFilterOptions(), excludeFilter);
      innerSql = "\n/* new filter applied on id query */\n" + innerSql;
    }

    // apply view filters if requested
    if (!_answerSpec.getViewFilterOptions().isEmpty() && !excludeViewFilters) {
      innerSql = applyFilters(innerSql, _answerSpec.getViewFilterOptions(), excludeFilter);
      innerSql = "\n/* new view filter applied on id query */\n" + innerSql;
    }

    if (!_answerSpec.getColumnFilterConfig().isEmpty())
      innerSql = applyColumnFilters(innerSql);

    innerSql = "(\n" + indent(innerSql) + "\n)";
    LOG.debug("AnswerValue: getIdSql(): ID SQL constructed with all filters:\n" + innerSql);

    return innerSql;
  }

  private String applyColumnFilters(String sql) throws WdkModelException {
    // Apply just the PK cols in the record class order (getIdSql can
    // append columns)
    final var q = "SELECT\n"
      + Arrays.stream(getAnswerSpec().getQuestion().getRecordClass()
        .getPrimaryKeyDefinition().getColumnRefs())
        .collect(Collectors.joining("\n, ", "  ", "\n"))
      + "FROM (\n" + indent(sql) + "\n)";

    return ColumnFilterSqlBuilder.buildFilteredSql(this, q);
  }

  private static String applyAnswerParams(
    String innerSql,
    Map<String, Param> paramMap,
    ReadOnlyMap<String, String> paramValues
  ) {

    // gather list of answer params for this question
    List<AnswerParam> answerParams = AnswerParam.getExposedParams(paramMap.values());

    // if no answer params, then return incoming SQL
    if (answerParams.isEmpty())
      return innerSql;

    // build list of columns to add, then join into string
    String extraCols = answerParams.stream()
      .map(AnswerParam::getName)
      .map(param -> "\n, " + paramValues.get(param) + " as " + param)
      .collect(Collectors.joining(""));

    // return wrapped innerSql including new columns
    return "\nSELECT\n  papidsql.*" + extraCols + "\nFROM (\n" +
      indent(innerSql) + "\n) papidsql ";
  }

  private String applyFilters(String innerSql, FilterOptionList filterOptions, String excludeFilter)
      throws WdkModelException {
    for (FilterOption filterOption : filterOptions)
      if (!filterOption.getKey().equals(excludeFilter) && !filterOption.isDisabled())
        innerSql = filterOption.getFilter().getSql(this, innerSql, filterOption.getValue());

    return innerSql;
  }

  private void prepareSortingSqls(Map<String, String> sqls, Collection<String> orders)
      throws WdkModelException {
    Map<String, AttributeField> fields = _answerSpec.getQuestion().getAttributeFieldMap();
    Map<String, String> querySqls = new LinkedHashMap<>();
    Map<String, String> queryNames = new LinkedHashMap<>();
    Map<String, String> orderClauses = new LinkedHashMap<>();
    WdkModel wdkModel = _answerSpec.getQuestion().getWdkModel();
    LOG.debug("AnswerValue: prepareSortingSqls(): sorting map: " + _sortingMap); //e.g.: {primary_key=true}
    final String idQueryNameStub = "answer_id_query";
    queryNames.put(idQueryNameStub, "idq");
    for (String fieldName : _sortingMap.keySet()) {
      AttributeField field = fields.get(fieldName);
      if (field == null) continue;
      boolean ascend = _sortingMap.get(fieldName);
      Map<String, ColumnAttributeField> dependents = field.getColumnAttributeFields();
      for (ColumnAttributeField dependent : dependents.values()) {

        // set default values for PK and simple columns
        String queryName = idQueryNameStub;
        String sortingColumn = dependent.getName();
        boolean ignoreCase = false;

        // handle query columns
        if (dependent instanceof QueryColumnAttributeField) {
          Column column = ((QueryColumnAttributeField)dependent).getColumn();
          //logger.debug("field [" + fieldName + "] depends on column [" + column.getName() + "]");
          Query query = column.getQuery();
          queryName = query.getFullName();
          // cannot use the attribute query from record, need to get it
          // back from wdkModel, since the query has pk params appended
          query = (Query) wdkModel.resolveReference(queryName);

          // handle query
          if (!queryNames.containsKey(queryName)) {
            // query not processed yet, process it
            String shortName = "aq" + queryNames.size();
            String sql = getAttributeSql(query);

            // add comments to sql
            sql = " /* attribute query used for sorting: " + queryName + " */ " + sql;
            queryNames.put(queryName, shortName);
            querySqls.put(queryName, sql);
          }

          // handle column
          String customSortingColumn = column.getSortingColumn();
          if (customSortingColumn != null) {
            sortingColumn = customSortingColumn;
          }
          ignoreCase = column.isIgnoreCase();
        }

        if (!orderClauses.containsKey(sortingColumn)) {
          // dependent not processed, process it
          StringBuilder clause = new StringBuilder();
          if (ignoreCase) clause.append("lower(");
          clause.append(queryNames.get(queryName));
          clause.append(".");
          clause.append(sortingColumn);
          if (ignoreCase) clause.append(")");
          clause.append(ascend ? " ASC" : " DESC");
          orderClauses.put(sortingColumn, clause.toString());
        }
      }
    }

    // fill the map of short name and sqls
    for (String queryName : queryNames.keySet()) {
      if (querySqls.containsKey(queryName)) {
        // generating a map from short name (e.g. aq1) to attribute query SQL
        sqls.put(queryNames.get(queryName), querySqls.get(queryName));
      }
    }
    orders.addAll(orderClauses.values());
  }

  public int getEndIndex() {
    return _endIndex;
  }

  public int getStartIndex() {
    return _startIndex;
  }

  public int getPageSize() throws WdkModelException {
    int resultSize = _resultSizeFactory.getResultSize();
    int n = (_endIndex == UNBOUNDED_END_PAGE_INDEX ? resultSize : Math.min(_endIndex, resultSize)) - _startIndex + 1;
    LOG.debug("AnswerValue: getPageSize(): " + n);
    return n;
  }

  public Optional<String> getResultMessage() throws WdkModelException {
    return _idsQueryInstance.getResultMessage();
  }

  public Map<String, Boolean> getSortingMap() {
    return new LinkedHashMap<>(_sortingMap);
  }

  public List<SortDirectionSpec<AttributeField>> getSortingColumns() {
    return SortDirectionSpec.convertSorting(_sortingMap,
      _answerSpec.getQuestion().getAttributeFieldMap());
  }

  /**
   * Set a new sorting map
   */
  public void setSortingMap(Map<String, Boolean> sortingMap) {
    if (sortingMap == null) {
      sortingMap = _answerSpec.getQuestion().getSortingAttributeMap();
    }
    // make sure all sorting columns exist
    StringBuilder buffer = new StringBuilder("set sorting: ");
    Map<String, AttributeField> attributes = _answerSpec.getQuestion().getAttributeFieldMap();
    Map<String, Boolean> validMap = new LinkedHashMap<>();
    for (String attributeName : sortingMap.keySet()) {
      buffer.append(attributeName + "=" + sortingMap.get(attributeName) + ", ");
      // if a sorting attribute is invalid, instead of throwing out an
      // exception, ignore it.
      if (!attributes.containsKey(attributeName)) {
        // throw new
        // WdkModelException("the assigned sorting attribute ["
        // + attributeName + "] doesn't exist in the answer of "
        // + "question " + question.getFullName());
        LOG.debug("AnswerValue: setSortingMap(): Invalid sorting attribute: User #" + _user.getUserId() + ", question: '" +
            _answerSpec.getQuestion().getFullName() + "', attribute: '" + attributeName + "'");
      }
      else {
        validMap.put(attributeName, sortingMap.get(attributeName));
      }
    }
    LOG.debug(buffer);
    _sortingMap.clear();
    _sortingMap.putAll(validMap);
    _sortedIdSql = null;
  }

  /**
   * This method is redundant with getAllIds(), consider deprecate either one of them.
   *
   * @return returns a list of all primary key values.
   */
  public Object[][] getPrimaryKeyValues() throws WdkModelException {
    String[] columns = _answerSpec.getQuestion().getRecordClass().getPrimaryKeyDefinition().getColumnRefs();
    List<Object[]> buffer = new ArrayList<>();

    Optional<AnswerFilterInstance> legacyFilter = _answerSpec.getLegacyFilter();
    try (ResultList resultList =
          legacyFilter.isPresent() ?
          legacyFilter.get().getResults(this) :
          _idsQueryInstance.getResults()) {
      while (resultList.next()) {
        Object[] pkValues = new String[columns.length];
        for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
          pkValues[columnIndex] = resultList.get(columns[columnIndex]);
        }
        buffer.add(pkValues);
      }
      Object[][] ids = new String[buffer.size()][columns.length];
      buffer.toArray(ids);
      return ids;
    }
  }

  private void reset() {
    _sortedIdSql = null;
    _checksum = null;
    _resultSizeFactory.clear();
  }

  /**
   * Get a list of all the primary key tuples of all the records in the answer. It is a shortcut of iterating
   * through all the pages and get the primary keys.
   *
   * This method is redundant with getPrimaryKeyValues(), consider deprecate either one of them.
   */
  public List<String[]> getAllIds() throws WdkModelException {
    String idSql = getSortedIdSql();
    String[] pkColumns = _answerSpec.getQuestion().getRecordClass().getPrimaryKeyDefinition().getColumnRefs();
    List<String[]> pkValues = new ArrayList<>();
    WdkModel wdkModel = _answerSpec.getQuestion().getWdkModel();
    DataSource dataSource = wdkModel.getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, idSql, _idsQueryInstance.getQuery().getFullName() + "__all-ids");
      while (resultSet.next()) {
        String[] values = new String[pkColumns.length];
        for (int i = 0; i < pkColumns.length; i++) {
          Object value = resultSet.getObject(pkColumns[i]);
          values[i] = (value == null) ? null : value.toString();
        }
        pkValues.add(values);
      }
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
    return pkValues;
  }

  public void setPageIndex(int startIndex, int endIndex) {
    _startIndex = startIndex;
    _endIndex = endIndex;
    reset();
  }

  public void setPageToEntireResult() {
    _startIndex = 1;
    _endIndex = UNBOUNDED_END_PAGE_INDEX;
    reset();
  }


  public JSONObject getFilterSummaryJson(String filterName) throws WdkUserException, WdkModelException {
    String idSql = getIdSql(filterName, false);
    Optional<Filter> filter = _answerSpec.getQuestion().getFilter(filterName);
    if (filter.isPresent()) {
      return filter.get().getSummaryJson(this, idSql);
    }
    else {
      throw new WdkUserException("Filter name '" + filterName +
          "' is not a valid filter on question " + _answerSpec.getQuestion().getName());
    }
  }

  /**
   * Returns one big string containing all IDs in this answer value's result in
   * the following format: each '\n'-delimited line contains one record, whose
   * primary keys are joined and delimited by a comma.
   *
   * @return list of all record IDs
   */
  public String getAllIdsAsString() throws WdkModelException {
    List<String[]> pkValues = getAllIds();
    StringBuilder buffer = new StringBuilder();
    for (String[] pkValue : pkValues) {
        if (buffer.length() > 0) buffer.append("\n");
        for (int i = 0; i < pkValue.length; i++) {
            if (i > 0) buffer.append(", ");
            buffer.append(pkValue[i]);
        }
    }
    return buffer.toString();
  }

  private final static String ID_QUERY_HANDLE = "pidq";
  private final static String QUERY_HANDLE = "inq";

  /**
   * Builds a query that selects from the given query joining on the ID sql
   * returned by {@link #getIdSql()}.
   *
   * @param sql
   *   Table/Attribute query
   *
   * @return the given query joined on the non-paged id query
   *
   * @throws WdkModelException
   *   see {@link #getIdSql()}
   *
   * @see #joinToIds(String, String)
   */
  private String joinToIds(final String sql) throws WdkModelException {
    return joinToIds(sql, getIdSql());
  }

  /**
   * Builds a query that selects from the first provided query joining on the
   * second using the primary key columns.
   * <p>
   * Generated SQL should match the following:
   * <pre>
   * SELECT
   *   inq.*
   * FROM
   *   ( ${sql} ) inq,
   *   ( ${idSql} ) pidq
   * WHERE
   *   inq.${pk[i]} = pidq.${pk[i]}
   *   ...
   * </pre>
   * <p>
   * The returned query will have both inq ({@link #QUERY_HANDLE}) and pidq
   * ({@link #ID_QUERY_HANDLE}) visible in the output query for use in appending
   * {@code ORDER BY} statements or additional filters to the query.
   *
   * @param sql
   *   Attribute/table query
   * @param idSql
   *   ID query
   *
   * @return the joined SQL query.
   */
  private String joinToIds(final String sql, final String idSql) {
    final String[] refs = _answerSpec.getQuestion().getRecordClass()
      .getPrimaryKeyDefinition()
      .getColumnRefs();

    final StringBuilder out = new StringBuilder("/* joinToIds */\nSELECT\n  "
      + QUERY_HANDLE + ".* \nFROM\n  (\n")
      .append(sql)
      .append("\n  ) " + QUERY_HANDLE + "\n, (\n")
      .append(idSql)
      .append("\n  ) " + ID_QUERY_HANDLE + "\nWHERE")
      .append("\n  ");

    for (int i = 0; i < refs.length; i++) {
      if (i > 0)
        out.append("\n  AND ");

      out.append(QUERY_HANDLE + ".")
        .append(refs[i])
        .append(" = " + ID_QUERY_HANDLE + ".")
        .append(refs[i]);
    }

    return out.append("\n").toString();
  }

  public boolean entireResultRequested() {
    return _startIndex == 1 && _endIndex == UNBOUNDED_END_PAGE_INDEX;
  }
}
