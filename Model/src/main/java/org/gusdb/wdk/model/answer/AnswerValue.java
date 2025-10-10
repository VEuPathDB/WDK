package org.gusdb.wdk.model.answer;

import static org.gusdb.fgputil.StringUtil.indent;
import static org.gusdb.fgputil.functional.Functions.swallowAndGet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
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
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyIterator;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.ResultSetPrimaryKeyIterator;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

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
  protected final User _requestingUser;
  private final RunnableObj<AnswerSpec> _validAnswerSpec;
  protected final AnswerSpec _answerSpec;
  private final Question _question;
  private final boolean _avoidCacheHit;

  // values derived from basic info
  private final WdkModel _wdkModel;
  private final QueryInstance<?> _idsQueryInstance;
  protected ResultSizeFactory _resultSizeFactory; // may be reassigned by subclasses
  private String _partitionKeysString; // to be used in an IN clause

  // paging for this answer
  // default is to return the entire result
  // NOTE: first index is 1 and page size is inclusive of startIndex and endIndex
  //       thus to get the first page of 10, use [1,10]
  private int _startIndex = 1;
  private int _endIndex = UNBOUNDED_END_PAGE_INDEX;

  // sorting for this answer
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
   * @param avoidCacheHit 
   */
  public AnswerValue(RunnableObj<AnswerSpec> validAnswerSpec, int startIndex,
      int endIndex, Map<String, Boolean> sortingMap, boolean avoidCacheHit) throws WdkModelException {
    _validAnswerSpec = validAnswerSpec;
    _answerSpec = validAnswerSpec.get();
    _question = _answerSpec.getQuestion().get(); // must be present if answerspec is valid
    _wdkModel = _answerSpec.getWdkModel();
    _requestingUser = validAnswerSpec.get().getRequestingUser();
    _avoidCacheHit = avoidCacheHit;
    _idsQueryInstance = Query.makeQueryInstance(_answerSpec.getQueryInstanceSpec().getRunnable().getLeft(), avoidCacheHit);
    _resultSizeFactory = new ResultSizeFactory(this);
    _sortingMap = sortingMap == null? new HashMap<String, Boolean>() : sortingMap;
    setPageIndex(startIndex, endIndex);
    LOG.debug("AnswerValue created for question: " + _question.getDisplayName());
  }

  @Override
  public AnswerValue clone() {
    // convert to runtime exception safe here because changes should not produce -new- exception
    return swallowAndGet(() -> new AnswerValue(this, this._startIndex, this._endIndex));
  }

  public AnswerValue cloneWithNewPaging(int startIndex, int endIndex) {
    // convert to runtime exception safe here because changes should not produce -new- exception
    return swallowAndGet(() -> new AnswerValue(this, startIndex, endIndex));
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
   *   end index (inclusive), or UNBOUNDED_END_PAGE_INDEX for all records
   * @throws WdkModelException
   */
  private AnswerValue(AnswerValue answerValue, int startIndex, int endIndex) throws WdkModelException {
    this(
      answerValue._validAnswerSpec,
      startIndex,
      endIndex,
      new LinkedHashMap<>(answerValue._sortingMap),
      answerValue._avoidCacheHit
    );
  }

  public User getRequestingUser() {
    return _requestingUser;
  }

  public RunnableObj<AnswerSpec> getRunnableAnswerSpec() {
    return _validAnswerSpec;
  }

  public AnswerSpec getAnswerSpec() {
    return _answerSpec;
  }

  public Question getQuestion() {
    return _question;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public QueryInstance<?> getIdsQueryInstance() {
    return _idsQueryInstance;
  }

  public ResultSizeFactory getResultSizeFactory() {
    return _resultSizeFactory;
  }

  public boolean cacheInitiallyExistedForSpec() throws WdkModelException {
    return _idsQueryInstance.cacheInitiallyExistedForSpec();
  }

  /**
   * @return Map where key is param display name and value is param value
   */
  public Map<String, String> getParamDisplays() {
    Map<String, String> displayParamsMap = new LinkedHashMap<>();
    ReadOnlyMap<String, String> paramsMap = _idsQueryInstance.getParamStableValues();
    Param[] params = _question.getParams();
    for (Param param : params) {
      displayParamsMap.put(param.getPrompt(), paramsMap.get(param.getName()));
    }
    return displayParamsMap;
  }

  /**
   * the checksum of the id query, plus the filter information on the answer.
   */
  public String getChecksum() throws WdkModelException {
    if (_checksum == null) {
      JSONObject jsContent = new JSONObject();
      jsContent.put("query-checksum", _idsQueryInstance.getChecksum());

      // if filters have been applied, get the content for them
      jsContent.put("filters", ParamsAndFiltersDbColumnFormat.formatFilters(_answerSpec.getFilterOptions()));

      // if view filters have been applied, get the content for them
      jsContent.put("viewFilters", ParamsAndFiltersDbColumnFormat.formatFilters(_answerSpec.getViewFilterOptions()));

      // if column filters have been applied, get the content for them
      jsContent.put("columnFilters", ParamsAndFiltersDbColumnFormat.formatColumnFilters(_answerSpec.getColumnFilterConfig()));

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
  public RecordInstance[] getRecordInstances() throws WdkModelException {
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
  
  public String getAnswerAttributeSql(Query attributeQuery, boolean sortPage)
  throws WdkModelException {
    return _startIndex == 1 && _endIndex == UNBOUNDED_END_PAGE_INDEX && !sortPage
        ? getUnsortedUnpagedAttributeSql(attributeQuery) :
          getPagedAttributeSql(attributeQuery, sortPage);
  }

  private String getPagedAttributeSql(Query attributeQuery, boolean sortPage)
  throws WdkModelException {
    LOG.debug("AnswerValue: getPagedAttributeSql(): " +
      attributeQuery.getFullName() + " --boolean sortPage: " + sortPage);

    // get the paged SQL of id query (include the row index if we are sorting the page)
    String idSql = getPagedIdSql(sortPage);

    // combine the id query with attribute query
    String attributeSql = getAttributeSql(attributeQuery);
    StringBuilder sql = new StringBuilder(" /* the desired attributes, for a page of sorted results */ "
        + " SELECT aq.* FROM (");
    sql.append(idSql);
    sql.append(") pidq, (/* attribute query that returns attributes in a page */ ").append(attributeSql).append(
        ") aq WHERE ");

    boolean firstColumn = true;
    for (String column : _question.getRecordClass().getPrimaryKeyDefinition().getColumnRefs()) {
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
  
  public String getAnswerTableSql(Query tableQuery)
  throws WdkModelException {
    return _startIndex == 1 && _endIndex == UNBOUNDED_END_PAGE_INDEX && _sortingMap.isEmpty()
        ? getUnsortedUnpagedTableSql(tableQuery) :
          getPagedTableSql(tableQuery);
  }

  public ResultList getTableFieldResultList(TableField tableField) throws WdkModelException {

    // has to get a clean copy of the attribute query, without pk params appended
    Query tableQuery = tableField.getUnwrappedQuery();

    // get and run the paged table query sql
    LOG.debug("AnswerValue: getTableFieldResultList(): going to getPagedTableSql()");

    String sql = getAnswerTableSql(tableQuery);
        
    LOG.debug("AnswerValue: getTableFieldResultList(): back from getPagedTableSql()");
    DatabaseInstance platform = _wdkModel.getAppDb();
    DataSource dataSource = platform.getDataSource();
    ResultSet resultSet = null;
    try {
      LOG.debug("AnswerValue: getTableFieldResultList(): returning SQL for TableField '" + tableField.getName() + "': \n" + sql);
      resultSet = SqlUtils.executeQuery(dataSource, sql, tableQuery.getFullName() + "_table");
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }

    return new SqlResultList(resultSet);
  }

  private String getPagedTableSql(Query tableQuery) throws WdkModelException {
    // get the paged SQL of id query
    String idSql = getPagedIdSql(true);

    // Combine the id query with table query.  Make an instance from the
    // original table query; a table query has only one param, user_id. Note
    // that the original table query is different from the table query held by
    // the recordClass.  The user_id param will be added by the query instance.
    RunnableObj<QueryInstanceSpec> tableQuerySpec = QueryInstanceSpec.builder()
      .buildRunnable(_requestingUser, tableQuery, StepContainer.emptyContainer());
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
    for (String column : _question.getRecordClass().getPrimaryKeyDefinition().getColumnRefs()) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append(" AND ");
      sql.append("tqi.").append(column).append(" = pidq.").append(column);
    }
    sql.append(" ORDER BY pidq.row_index, tqi.row_index");

    // replace the id_sql macro.  this sql must include filters (but not view filters)
    String sqlWithIdSql = sql.toString().replace(Utilities.MACRO_ID_SQL, getPagedIdSql(true));
    return sqlWithIdSql.replace(Utilities.MACRO_ID_SQL_NO_FILTERS, "(" + getNoFiltersIdSql() + ")");
  }

  public String getUnsortedUnpagedAttributeSql(Query attributeQuery) throws WdkModelException {
    String attrSql = getAttributeSql(attributeQuery);
    return getUnsortedUnpagedSql(attrSql);
  }
  
  public String getUnsortedUnpagedTableSql(Query tableQuery) throws WdkModelException {

    RunnableObj<QueryInstanceSpec> tableQuerySpec = QueryInstanceSpec.builder()
      .buildRunnable(_requestingUser, tableQuery, StepContainer.emptyContainer());
    String tableSql = Query.makeQueryInstance(tableQuerySpec).getSql();
    return getUnsortedUnpagedSql(tableSql);
  }
  
  public String getUnsortedUnpagedSql(String embeddedSql) throws WdkModelException {
    // get the unsorted and unpaged SQL of id query. 
    String idSql = getIdSql(null);
    
    String[] pkColumns = _question.getRecordClass().getPrimaryKeyDefinition().getColumnRefs();
    String pkColumnsString = String.join(", ", pkColumns);
    
    String[] sqlArray = { 
        "select * from (",
        "-- this is the embedded attribute or table query",
        embeddedSql,
        ") eq",
        "where (" + pkColumnsString + ")", 
        "IN (select " + pkColumnsString + " from " + idSql + " idsql)",
        "order by " + pkColumnsString
    };
    String sql = String.join(System.lineSeparator() , sqlArray);

    // replace the id_sql macro.  this sql must include filters (but not view filters)
    String sqlWithIdSql = sql.toString().replace(Utilities.MACRO_ID_SQL, idSql);
    return sqlWithIdSql.replace(Utilities.MACRO_ID_SQL_NO_FILTERS, "(" + getNoFiltersIdSql() + ")");
  }

  public String getUnfilteredAttributeSql(Query attrQuery)
  throws WdkModelException {
    String queryName = attrQuery.getFullName();
    Query dynaQuery = _question.getDynamicAttributeQuery();
    String sql;
    if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
      // the dynamic query doesn't have sql defined, the sql will be
      // constructed from the id query cache table.
      sql = getBaseIdSql(true);
    } else {
      // Make an instance from the original attribute query; an attribute
      // query has only one param, user_id. Note that the original
      // attribute query is different from the attribute query held by the
      // recordClass.  The user_id param will be added by the builder.
      // TODO: decide if construction of this validated spec should be moved elsewhere?
      RunnableObj<QueryInstanceSpec> attrQuerySpec = QueryInstanceSpec.builder()
        .buildValidated(_requestingUser, attrQuery, StepContainer.emptyContainer(),
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
    Query dynaQuery = _question.getDynamicAttributeQuery();
    String sql;
    if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
      // the dynamic query doesn't have sql defined, the sql will be
      // constructed from the id query cache table.
      sql = getBaseIdSql(true);
    }
    else {
      // Make an instance from the original attribute query; an attribute
      // query has only one param, user_id. Note that the original
      // attribute query is different from the attribute query held by the
      // recordClass.  The user_id param will be added by the builder.
      // TODO: decide if construction of this validated spec should be moved elsewhere?
      RunnableObj<QueryInstanceSpec> attrQuerySpec = QueryInstanceSpec.builder()
          .buildValidated(_requestingUser, attributeQuery, StepContainer.emptyContainer(),
              ValidationLevel.RUNNABLE, FillStrategy.NO_FILL)
          .getRunnable()
          .getOrThrow(spec -> new WdkModelException(
              "Attribute query spec found invalid: " + spec.getValidationBundle().toString()));

      // get attribute query sql from the instance
      sql = Query.makeQueryInstance(attrQuerySpec).getSql()
        // replace the id sql macro.
        // the injected sql must include filters
        .replace(Utilities.MACRO_ID_SQL, getIdSql(null))
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
    if (_sortedIdSql == null) {
      _sortedIdSql = createSortedIdSql();
    }
    return _sortedIdSql;
  }

  private String createSortedIdSql() throws WdkModelException {
    LOG.debug("AnswerValue: getSortedIdSql()");
    String[] pkColumns = _question.getRecordClass().getPrimaryKeyDefinition().getColumnRefs();

    // get id sql
    String idSql = getIdSql(null);

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
    return getPagedIdSql(false);
  }

  private String getPagedIdSql(boolean includeRowIndex) throws WdkModelException {
    String sortedIdSql = getSortedIdSql();
    DatabaseInstance platform = _wdkModel.getAppDb();
    String sql = platform.getPlatform().getPagedSql(sortedIdSql, _startIndex, _endIndex, includeRowIndex);

    // add comments to the sql
    sql = "\n/* a page of sorted ids */\n" + sql;

    LOG.debug("AnswerValue: getPagedIdSql() : paged id sql constructed: " + sql);

    return sql;
  }

  public String getIdSql() throws WdkModelException {
    return getIdSql(null);
  }

  private String getBaseIdSql(boolean sorted) throws WdkModelException {
    String sql = sorted? _idsQueryInstance.getSql() : _idsQueryInstance.getSqlUnsorted();
    // get base ID sql from query instance
    String innerSql = "\n/* the ID query */\n" + sql;

    // add answer param columns
    return "\n/* answer param value cols applied on id query */\n"
      + applyAnswerParams(innerSql, _question.getParamMap(),
        _idsQueryInstance.getParamStableValues());
  }

  protected String getIdSql(String excludeFilter) throws WdkModelException {

    // get base ID sql from query instance and answer params
    String innerSql = getBaseIdSql(false);

    // apply "new" filters
    if (!_answerSpec.getFilterOptions().isEmpty()) {
      innerSql = applyFilters(innerSql, _answerSpec.getFilterOptions(), excludeFilter);
      innerSql = "\n/* new filter applied on id query */\n" + innerSql;
    }

    // apply view filters if requested
    if (!_answerSpec.getViewFilterOptions().isEmpty()) {
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
    final var q = "SELECT\n" + Utilities.COLUMN_WEIGHT + ",\n"
      + Arrays.stream(_question.getRecordClass()
        .getPrimaryKeyDefinition().getColumnRefs())
        .collect(Collectors.joining("\n, ", "  ", "\n"))
      + "FROM (\n" + indent(sql) + "\n) colFilter_outer";

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
        innerSql = filterOption.getFilter().get().getSql(this, innerSql, filterOption.getValue());
    return innerSql;
  }

  private void prepareSortingSqls(Map<String, String> sqls, Collection<String> orders)
      throws WdkModelException {
    Map<String, AttributeField> fields = _question.getAttributeFieldMap();
    Map<String, String> querySqls = new LinkedHashMap<>();
    Map<String, String> queryNames = new LinkedHashMap<>();
    Map<String, String> orderClauses = new LinkedHashMap<>();
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
          query = (Query) _wdkModel.resolveReference(queryName);

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

  /**
   * Calculates the size of the page for this answer i.e. the number of rows this
   * answer will return.  Typically this is the difference between start and end
   * index (+1 due to inclusivity of start and end indexes), but may be smaller
   * if end index is higher than the total result size.  This method will never
   * return a negative number, or a value large than the total result size.
   *
   * @return number of rows returned by this answer value
   * @throws WdkModelException if unable to calculate result size
   */
  public int getPageSize() throws WdkModelException {
    // assumptions: startIndex > 0, endIndex == UNBOUNDED_END_PAGE_INDEX || >= startIndex
    int resultSize = _resultSizeFactory.getResultSize();
    int endIndex = _endIndex == UNBOUNDED_END_PAGE_INDEX ? resultSize : Math.min(_endIndex, resultSize);
    int pageSize = endIndex < _startIndex ? 0 : endIndex - _startIndex + 1;
    LOG.debug("AnswerValue: getPageSize(): " + pageSize);
    return pageSize;
  }

  public Optional<String> getResultMessage() throws WdkModelException {
    return _idsQueryInstance.getResultMessage();
  }

  public Map<String, Boolean> getSortingMap() {
    return new LinkedHashMap<>(_sortingMap);
  }

  public List<SortDirectionSpec<AttributeField>> getSortingColumns() {
    return SortDirectionSpec.convertSorting(_sortingMap, _question.getAttributeFieldMap());
  }

  /**
   * Set a new sorting map
   */
  public void setSortingMap(Map<String, Boolean> sortingMap) {
    if (sortingMap == null) {
      sortingMap = _question.getSortingAttributeMap();
    }
    // make sure all sorting columns exist
    StringBuilder buffer = new StringBuilder("set sorting: ");
    Map<String, AttributeField> attributes = _question.getAttributeFieldMap();
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
        LOG.debug("AnswerValue: setSortingMap(): Invalid sorting attribute: User #" + _requestingUser.getUserId() + ", question: '" +
            _question.getFullName() + "', attribute: '" + attributeName + "'");
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

  private String getPartitionKeysString() throws WdkModelException {
    if (_partitionKeysString != null) return _partitionKeysString;

    RecordClass rc = _question.getRecordClass();
    PrimaryKeyDefinition pkd = rc.getPrimaryKeyDefinition();
    String idSql = getIdSql();
    String partSql = rc.getPartitionKeySqlQuery().getSql();

    String sql =
        "SELECT distinct partitionkey " +
            " FROM (" + idSql + ") ids, (" + partSql + ") parts" +
            " WHERE " + pkd.createJoinClause("ids", "parts");
    try {
      _partitionKeysString =  new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql,
           rc.getName()+ "__partitionKey").executeQuery(rs -> {
        List<String> partKeys = new ArrayList<>();
        while (rs.next()) {
          partKeys.add(rs.getString("partitionKey"));
        }
        return String.join(", ", partKeys);
      });

      return _partitionKeysString;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException((Exception)e.getCause());
    }
  }

  private void reset() {
    _sortedIdSql = null;
    _checksum = null;
    _resultSizeFactory.clear();
  }

  /**
   * Creates a closable iterator of IDs for this answer
   *
   * NOTE! caller must close the return value to avoid resource leaks.
   *
   * @return an iterator of all the primary key tuples of all the records in the answer
   * @throws WdkModelException if unable to execute ID query
   */
  public PrimaryKeyIterator getAllIds() throws WdkModelException {
    try {
      PrimaryKeyDefinition pkDef = _question.getRecordClass().getPrimaryKeyDefinition();
      DataSource dataSource = _wdkModel.getAppDb().getDataSource();
      String idSql = getSortedIdSql();
      String queryDescriptor = _idsQueryInstance.getQuery().getFullName() + "__all-ids";
      return new ResultSetPrimaryKeyIterator(pkDef, SqlUtils.executeQuery(dataSource, idSql, queryDescriptor));
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to execute ID query", e);
    }
  }

  public void setPageIndex(int startIndex, int endIndex) {
    // do some checks
    if (startIndex < 1)
      throw new IllegalArgumentException("startIndex must be greater than zero");
    if (endIndex < 0)
      // all records requested; set to constant's value
      endIndex = UNBOUNDED_END_PAGE_INDEX;
    // Note we do not throw if startIndex > endIndex;
    //    this turns out to be a legitimate case (no records requested)
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
    String idSql = getIdSql(filterName);
    Optional<Filter> filter = _answerSpec.getQuestion().get().getFilter(filterName);
    if (filter.isPresent()) {
      return filter.get().getSummaryJson(this, idSql);
    }
    else {
      throw new WdkUserException("Filter name '" + filterName +
          "' is not a valid filter on question " + _answerSpec.getQuestion().get().getName());
    }
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
    final String[] refs = _question.getRecordClass()
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

  public static String wrapToReturnOnlyPkAndSelectedCols(String sql,
      RecordClass rc, Collection<QueryColumnAttributeField> fields) {

    List<String> cols = new ListBuilder<String>()
      .addAll(Arrays.asList(rc.getPrimaryKeyDefinition().getColumnRefs()))
      .addAll(fields.stream().map(Field::getName).collect(Collectors.toList()))
      .toList();
    
    return new StringBuilder()
      .append("/* SingleAttributeRecordStream */\nSELECT\n  ")
      .append(String.join(",\n  ", cols))
      .append("\n FROM (\n")
      .append(sql)
      .append("\n) sarsc")
      .toString();
  }
}
