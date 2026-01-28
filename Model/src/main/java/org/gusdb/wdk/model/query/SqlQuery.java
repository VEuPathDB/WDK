package org.gusdb.wdk.model.query;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.AttributeMetaQueryHandler;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static org.gusdb.wdk.model.AttributeMetaQueryHandler.getDynamicallyDefinedAttributes;

/**
 * An SqlQuery is used to access data from a database with SQL, and if the SQL
 * is relatively slow, it can be optionally cached for better performance.
 * <p>
 * the param can be embedded into SQL template in such form: $$param_name$$. the
 * param_name is the name of the param, and it doesn't have the paramSet name
 * prefix.
 * <p>
 * You can also define macros in the model and those macros will be substituted
 * into the SQL template. The difference between param and macro is that the
 * value of the macro is defined in the model and substituted into the SQL
 * template at initialization time, it will become a part of the SQL template;
 * while the value of param is provided by the user at run time, and it is
 * substituted into SQL template to produce the final SQL, but it doesn't change
 * the SQL template itself.
 *
 * @author Jerric Gao
 */
public class SqlQuery extends Query {

  private static final Logger LOG = Logger.getLogger(SqlQuery.class);

  private static final boolean DEFAULT_IS_CACHEABLE = true;

  public static final String PARTITION_KEYS_MACRO = "%%PARTITION_KEYS%%";

  public static final String PARTITION_KEYS_PLACEHOLDER = "'PLACEHOLDER'";

  /**
   * Macro name automatically populated with the ordered list of column names
   * from the attributeMetaQueryRef, formatted for use in a PostgreSQL crosstab
   * AS clause (e.g. "col1 text, col2 text, col3 text").
   */
  public static final String META_ATTRIBUTE_COLUMNS_FOR_CROSSTAB = "META_ATTRIBUTE_COLUMNS_FOR_CROSSTAB";

  private List<WdkModelText> _sqlList;
  private String _sql;
  private List<WdkModelText> _sqlMacroList;
  private Map<String, String> _sqlMacroMap;
  private boolean _clobRow;
  private boolean _useDBLink;

  /**
   * A flag to check if the cached has been set. if not set, the value from
   * parent querySet will be used, or default if no query set is present
   */
  private Boolean _isCacheable;

  private String _attributeMetaQueryRef;
  private List<WdkModelText> _dependentTableList;
  private Map<String, String> _dependentTableMap;

  public SqlQuery() {
    super();
    _clobRow = false;
    _sqlList = new ArrayList<>();
    _sqlMacroList = new ArrayList<>();
    _sqlMacroMap = new LinkedHashMap<>();
    _dependentTableList = new ArrayList<>();
    _dependentTableMap = new LinkedHashMap<>();
  }

  public SqlQuery(SqlQuery query) {
    super(query);
    _clobRow = query._clobRow;
    _sql = query._sql;
    _isCacheable = query._isCacheable;
    _useDBLink = query._useDBLink;

    if (query._sqlList != null) _sqlList = new ArrayList<>(query._sqlList);
    if (query._sqlMacroMap != null)
      _sqlMacroMap = new LinkedHashMap<>(query._sqlMacroMap);
    if (query._sqlMacroList != null)
      _sqlMacroList = new ArrayList<>(query._sqlMacroList);
    if (query._dependentTableMap != null)
      _dependentTableMap = new LinkedHashMap<>(query._dependentTableMap);
    if (query._dependentTableList != null)
      _dependentTableList = new ArrayList<>(query._dependentTableList);
  }

  @Override
  protected SqlQueryInstance makeInstance(RunnableObj<QueryInstanceSpec> spec) throws WdkModelException {
    return new SqlQueryInstance(spec);
  }

  public void addSql(WdkModelText sql) {
    _sqlList.add(sql);
  }

  public void addSqlParamValue(WdkModelText sqlMacro) {
    _sqlMacroList.add(sqlMacro);
  }

  public void addSqlParamValue(String macro, String value) {
    _sqlMacroMap.put(macro, value);
  }

  public String getSql() {
    return replaceMacros(_sql);
  }

  public boolean isUseDBLink() {
    return _useDBLink;
  }

  public void setUseDBLink(boolean useDBLink) {
    _useDBLink = useDBLink;
  }

  /**
   * @return whether this query should be cached
   */
  @Override
  public boolean isCacheable() {
    // check if global caching is turned off, if off, then return false
    if (!_wdkModel.getModelConfig().isCaching()) return false;
    // if this query's value has been set, use it
    if (_isCacheable != null) {
      return _isCacheable;
    }
    // if not, use QuerySet's value or default
    return (getQuerySet() != null ?
        getQuerySet().isCacheable() :
        DEFAULT_IS_CACHEABLE);
  }

  /**
   * @param isCacheable
   *          the cached to set
   */
  public void setIsCacheable(boolean isCacheable) {
    _isCacheable = isCacheable;
  }

  /**
   * Sets an optional reference to a meta columns query
   */
  public void setAttributeMetaQueryRef(String attributeMetaQueryRef) {
    _attributeMetaQueryRef = attributeMetaQueryRef;
  }

  /**
   * this method is called by other WDK objects. It is not called by the model
   * xml parser.
   */
  public void setSql(String sql) {
    // append new line to the end, in case the last line is a comment;
    // otherwise, all modified sql will fail.
    _sql = sql + "\n";
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsQuery, boolean extra)
      throws JSONException {
    if (extra) {
      // add macro into the content
      String[] macroNames = new String[_sqlMacroMap.size()];
      _sqlMacroMap.keySet().toArray(macroNames);
      Arrays.sort(macroNames);
      JSONObject jsMacros = new JSONObject();
      for (String macroName : macroNames) {
        jsMacros.put(macroName, _sqlMacroMap.get(macroName));
      }
      jsQuery.put("macros", jsMacros);

      // add sql
      String sql = getSql().replaceAll("\\s+", " ");
      jsQuery.put("sql", sql);
    }
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude sql
    for (WdkModelText sql : _sqlList) {
      if (sql.include(projectId)) {
        sql.excludeResources(projectId);
        this.setSql(sql.getText());
        break;
      }
    }
    _sqlList = null;

    // exclude sql
    for (WdkModelText dependentTable : _dependentTableList) {
      if (dependentTable.include(projectId)) {
        dependentTable.excludeResources(projectId);
        String table = dependentTable.getText();
        _dependentTableMap.put(table, table);
      }
    }
    _dependentTableList = null;

    // exclude macros
    for (WdkModelText macro : _sqlMacroList) {
      if (macro.include(projectId)) {
        macro.excludeResources(projectId);
        String name = macro.getName();
        if (_sqlMacroMap.containsKey(name))
          throw new WdkModelException("The macro " + name
              + " is duplicated in query " + getFullName());

        _sqlMacroMap.put(macro.getName(), macro.getText());
      }
    }
    _sqlMacroList = null;
  }

  @Override
  public void resolveQueryReferences(WdkModel wdkModel)
      throws WdkModelException {
    // apply the sql macros into sql
    if (_sql == null)
      throw new WdkModelException("null sql in " + getFullName());

    // don't replace the sql here. the macros have to be replaced on the fly
    // in order to inject overridden macros from question.
    String sql = replaceMacros(_sql);

    // verify the all param macros have been replaced
    Matcher matcher = Pattern.compile("&&([^&]+)&&").matcher(sql);
    if (matcher.find())
      throw new WdkModelException("SqlParamValue macro " + matcher.group(1)
          + " found in <sql> of query " + getFullName()
          + ", but it's not defined.");
  }

  private String replaceMacros(String sql) {
    for (String paramName : _sqlMacroMap.keySet()) {
      String pattern = "&&" + paramName + "&&";
      String value = _sqlMacroMap.get(paramName);
      // escape the & $ \ chars in the value
      sql = sql.replaceAll(pattern, Matcher.quoteReplacement(value));
    }
    return sql;
  }

  @Override
  public SqlQuery clone() {
    return new SqlQuery(this);
  }

  /**
   * This is a way to declare the query returns clob columns. This property is
   * used when we generate download cache from table queries. If the
   * concatenated size of the column values exceeds the DBMS limit for string
   * columns, this flag should be set to true, so that the result can be casted
   * into a CLOB. However, since writing and reading clobs are much slower than
   * a normal string column, the flag should be set to false if CLOB is not
   * needed.
   *
   * @return the clobRow
   */
  public boolean isClobRow() {
    return _clobRow;
  }

  /**
   * @param clobRow
   *          the clobRow to set
   */
  public void setClobRow(boolean clobRow) {
    _clobRow = clobRow;
  }

  public void addDependentTable(WdkModelText dependentTable) {
    _dependentTableList.add(dependentTable);
  }

  /**
   * This is a way to declare the tables the SQL depends on without having to
   * parse the SQL. This feature is used in generating download cache.
   */
  public String[] getDependentTables() {
    String[] array = new String[_dependentTableMap.size()];
    _dependentTableMap.keySet().toArray(array);
    return array;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (_resolved) return;

    // Continue only if an attribute meta query reference is provided
    // IMPORTANT: Must populate macro BEFORE super.resolveReferences() which validates macros
    if (_attributeMetaQueryRef != null) {
      Timer timer = new Timer();
      for (Map<String,Object> row : getDynamicallyDefinedAttributes(_attributeMetaQueryRef, wdkModel)) {
        Column column = new Column();
        // Need to set this here since this column originates from the database
        column.setQuery(SqlQuery.this);
        AttributeMetaQueryHandler.populate(column, row);
        _columnMap.put(column.getName(), column);
      }

      populateCrosstabMacroWithDiscoveredColumns(wdkModel);

      LOG.debug("Took " + timer.getElapsedString() + " to resolve AttributeMetaQuery: " + _attributeMetaQueryRef);
    }

    super.resolveReferences(wdkModel);

    // set the dblink flag if any of the params is a datasetParam;
    for (Param param : getParams()) {
      if (param instanceof DatasetParam) {
        _useDBLink = true;
        break;
      }
    }
  }

  private void populateCrosstabMacroWithDiscoveredColumns(WdkModel wdkModel) throws WdkModelException {
    // check if this query's SQL contains a crosstab macro that needs to be filled in with columns discovered by the meta query
    if (_sql.contains(META_ATTRIBUTE_COLUMNS_FOR_CROSSTAB)) {

      // This macro is only supported on Postgres
      if (wdkModel.getModelConfig().getAppDB().getPlatformEnum() != SupportedPlatform.POSTGRESQL) {
        throw new WdkModelException("SQL Query " + getName() + ": Crosstab macro is only supported on Postgres App DBs.");
      }

      // Populate the crosstab macro with ordered column definitions from the meta query
      // If no columns returned, use a placeholder to prevent SQL parse errors
      List<String> metaColumnDefs = new ArrayList<>();
      for (Column column : _columnMap.values()) {
        metaColumnDefs.add(column.getName() + " " + column.getType().getPostgresType());
      }
      String macroValue = metaColumnDefs.isEmpty() ?
          "_no_columns_defined text" :
          String.join(", ", metaColumnDefs);
      addSqlParamValue(META_ATTRIBUTE_COLUMNS_FOR_CROSSTAB, macroValue);
    }
  }

  @Override
  public Map<String, AttributeFieldDataType> resolveColumnTypes() throws WdkModelException {
    var types = new LinkedHashMap<String, AttributeFieldDataType>();

    // use place holder for partition keys, to produce syntactically correct SQL
    var sql = applyParams(getSql(), paramMap.values().iterator()).replaceAll(PARTITION_KEYS_MACRO, PARTITION_KEYS_PLACEHOLDER);

    // DB column name casing may not match xml name casing.
    var names = _columnMap.keySet()
      .stream()
      .collect(Collectors.toMap(String::toLowerCase, Function.identity()));

    if (isNull(sql) || sql.isBlank())
      return handleEmptySql();

    try (
      var con = _wdkModel.getAppDb().getDataSource().getConnection();
      var ps = con.prepareStatement(sql)
    ) {
      var meta = ps.getMetaData();
      var cols = meta.getColumnCount();

      for (int i = 1; i <= cols; i++) {
        var name = names.get(meta.getColumnName(i).toLowerCase());
        var type = meta.getColumnType(i);

        if (name != null)
          types.put(name, AttributeFieldDataType.fromSqlType(type));
      }
    } catch (SQLException e) {
      return handleColumnTypeException(e);
    }

    return types;
  }

  private String applyParams(String sql, Iterator<Param> params) {
    if (!params.hasNext())
      return sql;
    var param = params.next();
    var value = param.getParamHandler().toEmptyInternalValue();
    return applyParams(param.replaceSql(sql, "", value), params);
  }

  private Map<String, AttributeFieldDataType> handleEmptySql()
  throws WdkModelException {
    if (getName().endsWith(Question.DYNAMIC_QUERY_SUFFIX))
      // TODO: What should actually be done with this?  Is this why query
      //       instance was needed?
      return super.resolveColumnTypes();
    else
      throw new WdkModelException("Empty SQL in query " + getFullName());

  }

  private static final Pattern MACRO = Pattern.compile("##[A-Z_]+##");
  private static final Pattern PARAM = Pattern.compile("\\${2}\\w+\\${2}");

  private Map<String, AttributeFieldDataType> handleColumnTypeException(
    final SQLException ex
  ) throws WdkModelException {
    // use place holder for partition keys, to produce syntactically correct SQL
    var sql = getSql().replaceAll(PARTITION_KEYS_MACRO, PARTITION_KEYS_PLACEHOLDER);

    var macro = MACRO.matcher(sql);
    var param = PARAM.matcher(sql);

    // If this query does not contain an unparsed macro or param, then it was
    // just bad SQL
    if (!macro.find() && !param.find())
      throw new WdkModelException(String.format("Database error while "
          + "attempting to parse sqlQuery %s: %s",
        getFullName(), sql), ex);

    var invalid = _columnMap.values().stream()
      .anyMatch(not(Column::wasTypeSet));

    if (invalid) {
      throw new WdkModelException(String.format(
        "Due to one or more macros and/or params the \"columnType\" value must "
          + "be set for each column in the sqlQuery \"%s\".\n\n"
          + "Macros: %s\n"
          + "Sql: " + sql,
        getFullName(),
        Stream.concat(
          macro.reset().results().map(MatchResult::group),
          param.reset().results().map(MatchResult::group)
        )
          .filter(not(String::isBlank))
          .collect(Collectors.joining(", "))),
      ex);
    }

    return super.resolveColumnTypes();
  }
}
