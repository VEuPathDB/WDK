package org.gusdb.wdk.model.answer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.FilterOption;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.filter.FilterSummary;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.DefaultResultSizePlugin;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.ResultSize;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeValue;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.TabularReporter;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

/**
 * <p>
 * A list of {@link RecordInstance}s representing one page of the answer to a {@link Question}. The
 * constructor of the Answer provides a handle ( {@link QueryInstance}) on the {@link ResultList} that is the
 * list of primary keys for the all the records (not * just one page) that are the answer to the
 * {@link Question}. The {@link ResultList} also has a column that contains the row number (RESULT_TABLE_I) so
 * that a list of primary keys for a single page can be efficiently accessed.
 * </p>
 * 
 * <p>
 * The AnswerValue is lazy in that it only constructs the set of {@link RecordInstance}s for the page when the
 * first RecordInstance is requested.
 * </p>
 * 
 * <p>
 * The initial request triggers the creation of skeletal {@link RecordInstance}s for the page. They contain
 * only primary keys (these being acquired from the {@link ResultList}).
 * </p>
 * 
 * <p>
 * These skeletal {@link RecordInstance}s are also lazy in that they only run an attributes {@link Query} when
 * an attribute provided by that query is requested. When they do run an attribute query, its
 * {@link QueryInstance} is put into joinMode. This means that the attribute query joins with the table
 * containing the primary keys, and, in one database query, generates rows containing the attribute values for
 * all the {@link RecordInstance}s in the page.
 * </p>
 * 
 * <p>
 * similar lazy loading can be applied to table {@link Query} too.
 * </p>
 * 
 * <p>
 * The method {@link AnswerValue#integrateAttributesQuery} is invoked by the first RecordInstance in the page
 * upon the first request for an attribute provided by an attributes query. The query is a join with the list
 * of primary keys, and so has a row for each {@link RecordInstance} in the page, and columns that provide the
 * attribute values (plus RESULT_TABLE_I). The values in the rows are integrated into the corresponding
 * {@link RecordInstance} (now no longer skeletal). {@link AnswerValue#integrateAttributesQuery} may be called
 * a number of times, depending upon how many attribute queries the {@link RecordClass} contains.
 * </p>
 * 
 * <p>
 * Attribute queries are guaranteed to provide one row for each {@link RecordInstance} in the page. An
 * exception is thrown otherwise.
 * </p>
 * 
 * <p>
 * During a standard load of an AnswerValue, we do the following:
 * 1. Apply filter to the IDs in the cache (FilterInstance takes SQL for IDs, wraps to filter, and returns)
 * 2. Apply sorting (AnswerValue takes SQL from filter, join with appropriate attribute queries, wrap to sort, and returns)
 * 3. Apply paging (add rownum, etc. to SQL)
 * Then run SQL!!  Creates template RecordInstances (non populated with attributes)
 * 4. Apply SQL from step 3, join with attribute queries to build results to return to user
 * Attribute fetch is lazy-loaded, but cache all attributes from attribute query (could be big e.g. BFMV), even if don't need all of them
 * 
 * <p>
 * Created: Fri June 4 13:01:30 2004 EDT
 * </p>
 * 
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 * 
 * 
 */
public class AnswerValue {

  /**
   * Computes application of various filters to the same answer value in parallel to get result sizes
   * 
   * May eventually be deprecated if we do away with the filter grid (the only place we really load filtered result sizes)
   * 
   * @author jerric
   */
  private static class FilterSizeTask implements Runnable {

    private final AnswerValue _answer;
    private final ConcurrentMap<String, Integer> _sizes;
    private final String _filterName;
    private final boolean _useDisplay;

    public FilterSizeTask(AnswerValue answer, ConcurrentMap<String, Integer> sizes, String filterName, boolean useDisplay) {
      _answer = answer;
      _sizes = sizes;
      _filterName = filterName;
      _useDisplay = useDisplay;
    }

    @Override
    public void run() {
      try {
        int size = (_useDisplay ?
            _answer.getFilterDisplaySize(_filterName) :
            _answer.getFilterSize(_filterName));
        _sizes.put(_filterName, size);
      }
      catch (WdkModelException | WdkUserException ex) {
        _sizes.put(_filterName, -1);
      }
    }

  }

  private static final int THREAD_POOL_SIZE = 4;
  private static final int THREAD_POOL_TIMEOUT = 5; // timeout thread pool, in minutes

  private static final Logger logger = Logger.getLogger(AnswerValue.class);

  // ------------------------------------------------------------------
  // Instance variables
  // ------------------------------------------------------------------

  private User _user;

  private ResultFactory _resultFactory;
  private Question _question;

  private QueryInstance<?> _idsQueryInstance;

  private int _startIndex;
  private int _endIndex;

  private String _sortedIdSql;

  private Map<PrimaryKeyAttributeValue, RecordInstance> _pageRecordInstances;

  private Integer _resultSize; // size of total result
  private Map<String, Integer> _resultSizesByFilter = new LinkedHashMap<>();
  private Map<String, Integer> _resultSizesByProject;

  private Map<String, Boolean> _sortingMap;

  private AnswerFilterInstance _filter;

  private FilterOptionList _filterOptions;
  private FilterOptionList _viewFilterOptions;

  private String _checksum;

  private AnswerValueAttributes _attributes;

  // ------------------------------------------------------------------
  // Constructor
  // ------------------------------------------------------------------

  /**
   * @param question
   *          The <code>Question</code> to which this is the <code>Answer</code> .
   * @param idsQueryInstance
   *          The <co de>QueryInstance</code> that provides a handle on the ResultList containing all primary
   *          keys that are the result for the question (not just one page worth).
   * @param startIndex
   *          The index of the first <code>RecordInstance</code> in the page. (>=1)
   * @param endIndex
   *          The index of the last <code>RecordInstance</code> in the page, inclusive.
   */
  public AnswerValue(User user, Question question, QueryInstance<?> idsQueryInstance, int startIndex,
      int endIndex, Map<String, Boolean> sortingMap, AnswerFilterInstance filter) {
    logger.debug("AnswerValue being created for question: " + question.getDisplayName());
    _user = user;
    _question = question;
    _attributes = new AnswerValueAttributes(_user, _question);
    _resultFactory = question.getWdkModel().getResultFactory();
    _idsQueryInstance = idsQueryInstance;
    _startIndex = startIndex;
    _endIndex = endIndex;

    // get sorting columns
    if (sortingMap == null)
      sortingMap = question.getSortingAttributeMap();
    _sortingMap = sortingMap;

    // get the view (old filters)
    _filter = filter;

    logger.debug("AnswerValue created for question: " + question.getDisplayName());
  }

  /**
   * A copy constructor with start- and end-index modification
   * 
   * @param answerValue
   * @param startIndex
   * @param endIndex
   */
  public AnswerValue(AnswerValue answerValue, int startIndex, int endIndex) {
    _startIndex = startIndex;
    _endIndex = endIndex;

    _user = answerValue._user;
    _idsQueryInstance = answerValue._idsQueryInstance;
    _question = answerValue._question;
    _attributes = new AnswerValueAttributes(_user, _question);
    _resultFactory = answerValue._resultFactory;
    _resultSize = answerValue._resultSize;
    _resultSizesByFilter = new LinkedHashMap<String, Integer>(answerValue._resultSizesByFilter);
    if (answerValue._resultSizesByProject != null)
      _resultSizesByProject = new LinkedHashMap<String, Integer>(answerValue._resultSizesByProject);

    _sortingMap = new LinkedHashMap<String, Boolean>(answerValue._sortingMap);
    _filter = answerValue._filter;

    logger.debug("AnswerValue created by copying another AnswerValue");
  }

  // ------------------------------------------------------------------
  // Public Methods
  // ------------------------------------------------------------------

  /**
   * provide property that user's term for question
   */
  /**
   * @return
   */
  public Question getQuestion() {
    return _question;
  }

  public User getUser() {
    return _user;
  }

  public AnswerValueAttributes getAttributes() {
    return _attributes;
  }

  /**
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public int getPageSize() throws WdkModelException, WdkUserException {
    initPageRecordInstances();
    return _pageRecordInstances.size();
  }

  public int getPageCount() throws WdkModelException, WdkUserException {
    int total = getResultSize();
    int pageSize = _endIndex - _startIndex + 1;
    int pageCount = (int) Math.round(Math.ceil((float) total / pageSize));
    logger.debug("#Pages: " + pageCount + ",\t#Total: " + total + ",\t#PerPage: " + pageSize);
    return pageCount;
  }

  public int getResultSize() throws WdkModelException, WdkUserException {
    if (_resultSize == null || !_idsQueryInstance.getIsCacheable()) {
      _resultSize = new DefaultResultSizePlugin().getResultSize(this);
    }
    logger.debug("getting result size: cache=" + _resultSize + ", isCacheable=" + _idsQueryInstance.getIsCacheable());
    return _resultSize;
  }

  public int getDisplayResultSize() throws WdkModelException, WdkUserException {
    ResultSize plugin = _question.getRecordClass().getResultSizePlugin();
    logger.debug("getting Display result size.");
    return plugin.getResultSize(this);
  }

  public Map<String, Integer> getResultSizesByProject() throws WdkModelException, WdkUserException {
    if (_resultSizesByProject == null) {
      _resultSizesByProject = new LinkedHashMap<String, Integer>();

      // make sure the project_id is defined in the record
      PrimaryKeyAttributeField primaryKey = _question.getRecordClass().getPrimaryKeyAttributeField();
      if (!primaryKey.hasColumn(Utilities.COLUMN_PROJECT_ID)) {
        String projectId = _question.getWdkModel().getProjectId();
        // no project_id defined in the record, use the full size
        _resultSizesByProject.put(projectId, getResultSize());
      }
      else {
        // need to run the query first
        ResultList resultList;
        // for portal
        String message = _idsQueryInstance.getResultMessage();
        if (_filter == null)
          resultList = _idsQueryInstance.getResults();
        else
          resultList = _filter.getResults(this);

        try {
          boolean hasMessage = (message != null && message.length() > 0);
          if (hasMessage) {
            String[] sizes = message.split(",");
            for (String size : sizes) {
              String[] parts = size.split(":");
              if (parts.length > 1 && parts[1].matches("^\\d++$")) {
                _resultSizesByProject.put(parts[0], Integer.parseInt(parts[1]));
              }
              else {
                // make sure if the message is not expected, the
                // correct result size can still be retrieved
                // from
                // cached result.
                hasMessage = false;
              }
            }
          }
          // if the previous step fails, make sure the result size can
          // still be calculated from cache.
          if (!hasMessage) {
            while (resultList.next()) {
              if (!hasMessage) {
                // also count by project
                String project = resultList.get(Utilities.COLUMN_PROJECT_ID).toString();
                int subCounter = 0;
                if (_resultSizesByProject.containsKey(project))
                  subCounter = _resultSizesByProject.get(project);
                // if subContent < 0, it is an error code. don't
                // change it.
                if (subCounter >= 0)
                  _resultSizesByProject.put(project, ++subCounter);
              }
            }
          }
        }
        finally {
          resultList.close();
        }
      }
    }
    return _resultSizesByProject;

  }

  public boolean isDynamic() {
    return getQuestion().isDynamic();
  }

  /**
   * @return Map where key is param display name and value is param value
   */
  public Map<String, String> getParamDisplays() {
    Map<String, String> displayParamsMap = new LinkedHashMap<String, String>();
    Map<String, String> paramsMap = _idsQueryInstance.getParamStableValues();
    Param[] params = _question.getParams();
    for (int i = 0; i < params.length; i++) {
      Param param = params[i];
      displayParamsMap.put(param.getPrompt(), paramsMap.get(param.getName()));
    }
    return displayParamsMap;
  }

  public QueryInstance<?> getIdsQueryInstance() {
    return _idsQueryInstance;
  }

  /**
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public RecordInstance[] getRecordInstances() throws WdkModelException, WdkUserException {
    initPageRecordInstances();

    RecordInstance[] array = new RecordInstance[_pageRecordInstances.size()];
    _pageRecordInstances.values().toArray(array);
    return array;
  }

  public RecordInstance getRecordInstance(PrimaryKeyAttributeValue primaryKey) {
    return _pageRecordInstances.get(primaryKey);
  }

  public String getQueryChecksum(boolean extra) throws WdkModelException {
    return _idsQueryInstance.getQuery().getChecksum(extra);
  }

  /**
   * the checksum of the iq query, plus the filter information on the answer.
   * 
   * @return
   * @throws WdkUserException
   */
  public String getChecksum() throws WdkModelException, WdkUserException {
    if (_checksum == null) {
      JSONObject jsContent = new JSONObject();
      jsContent.put("query-checksum", _idsQueryInstance.getChecksum());

      // add the old filter into the content; the old filter will be deprecated in the future releases, and
      // this line will be removed.
      if (_filter != null)
        jsContent.put("old-filter", _filter.getName());

      // new filters have been applied, get the content for it
      if (_filterOptions != null)
        jsContent.put("filters", _filterOptions.getJSON());

      // encrypt the content to make step-independent checksum
      _checksum = Utilities.encrypt(jsContent.toString());
    }
    return _checksum;
  }

  /**
   * the answer's key is the checksum of the answer, plus the filter, if any.
   * 
   * @return
   * @throws WdkUserException
   */
  public String getAnswerStringKey() throws WdkModelException, WdkUserException {
    return getChecksum();
  }

  // ///////////////////////////////////////////////////////////////////
  // print methods
  // ///////////////////////////////////////////////////////////////////

  public String printAsRecords() throws WdkModelException, WdkUserException {
    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer();

    initPageRecordInstances();

    for (RecordInstance recordInstance : _pageRecordInstances.values()) {
      buf.append(recordInstance.print());
      buf.append("---------------------" + newline);
    }
    return buf.toString();
  }

  /**
   * print summary attributes, one per line Note: not sure why this is needed
   * 
   * @throws WdkUserException
   * 
   */
  public String printAsSummary() throws WdkModelException, WdkUserException {
    StringBuffer buf = new StringBuffer();

    initPageRecordInstances();

    for (RecordInstance recordInstance : _pageRecordInstances.values()) {
      buf.append(recordInstance.printSummary());
    }
    return buf.toString();
  }

  /**
   * print summary attributes in tab delimited table with header of attr. names
   * 
   * @throws WdkUserException
   * 
   */
  public String printAsTable() throws WdkModelException, WdkUserException {
    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer();

    initPageRecordInstances();

    // print summary info
    buf.append("# of Records: " + getResultSize() + ",\t# of Pages: " + getPageCount() +
        ",\t# Records per Page: " + getPageSize() + newline);

    if (_pageRecordInstances.size() == 0)
      return buf.toString();

    Map<String, AttributeField> attributes = _attributes.getSummaryAttributeFieldMap();
    for (String nextAttName : attributes.keySet()) {
      buf.append(nextAttName + "\t");
    }
    buf.append(newline);
    for (RecordInstance recordInstance : _pageRecordInstances.values()) {
      // only print
      for (String nextAttName : attributes.keySet()) {
        // make data row
        AttributeValue value = recordInstance.getAttributeValue(nextAttName);
        // only print part of the string
        String str = value.getBriefDisplay();
        buf.append(str + "\t");
      }
      buf.append(newline);
    }
    return buf.toString();
  }

  /**
   * Creates a reporter that covers all rows in the answer.
   * 
   * @param reporterName
   * @param config
   * @return
   * @throws WdkUserException
   */
  public Reporter createReport(String reporterName, Map<String, String> config) throws WdkModelException,
      WdkUserException {
    // get the full answer
    int endI = getResultSize();
    return createReport(reporterName, config, 1, endI);
  }

  public Reporter createReport(String reporterName, Map<String, String> config, int startI, int endI)
      throws WdkModelException {
    Reporter reporter = createReportSub(reporterName, startI, endI);
    reporter.configure(config);
    return reporter;
  }
  
  public Reporter createReport(String reporterName, JSONObject config) throws WdkModelException,
  WdkUserException {
    // get the full answer
    int endI = getResultSize();
    return createReport(reporterName, config, 1, endI);
  }

  public Reporter createReport(String reporterName, JSONObject config, int startI, int endI)
      throws WdkModelException {
    Reporter reporter = createReportSub(reporterName, startI, endI);
    reporter.configure(config);
    return reporter;
  }

  private Reporter createReportSub(String reporterName, int startI, int endI)
      throws WdkModelException {
    // get Reporter
    Map<String, ReporterRef> rptMap = _question.getRecordClass().getReporterMap();
    ReporterRef rptRef = rptMap.get(reporterName);
    if (rptRef == null)
      throw new WdkModelException("The reporter " + reporterName + " is " + "not registered for " +
          _question.getRecordClass().getFullName());
    String rptImp = rptRef.getImplementation();
    if (rptImp == null)
      throw new WdkModelException("The reporter " + reporterName + " is " + "not registered for " +
          _question.getRecordClass().getFullName());

    try {
      Class<?> rptClass = Class.forName(rptImp);
      Class<?>[] paramClasses = { AnswerValue.class, int.class, int.class };
      Constructor<?> constructor = rptClass.getConstructor(paramClasses);

      Object[] params = { this, startI, endI };
      Reporter reporter = (Reporter) constructor.newInstance(params);
      reporter.setProperties(rptRef.getProperties());
      reporter.setWdkModel(rptRef.getWdkModel());
      return reporter;
    }
    catch (ClassNotFoundException ex) {
      throw new WdkModelException(ex);
    }
    catch (InstantiationException ex) {
      throw new WdkModelException(ex);
    }
    catch (IllegalAccessException ex) {
      throw new WdkModelException(ex);
    }
    catch (SecurityException ex) {
      throw new WdkModelException(ex);
    }
    catch (NoSuchMethodException ex) {
      throw new WdkModelException(ex);
    }
    catch (IllegalArgumentException ex) {
      throw new WdkModelException(ex);
    }
    catch (InvocationTargetException ex) {
      throw new WdkModelException(ex);
    }
  }


  /**
   * Iterate through all the pages of the answer, and each page is represented by an AnswerValue object.
   * 
   * @return
   * @throws WdkUserException
   */
  public Iterable<AnswerValue> getFullAnswers() throws WdkModelException, WdkUserException {
    // user tabular reporter as answer iterator
    int resultSize = this.getResultSize();
    TabularReporter reporter = new TabularReporter(this, 1, resultSize);
    return reporter;
  }

  // ------------------------------------------------------------------
  // Package Methods
  // ------------------------------------------------------------------

  /**
   * Integrate into the page's RecordInstances the attribute values from a particular attributes query. The
   * attributes query result includes only rows for this page.
   * 
   * The query is obtained from Column, and the query should not be modified.
   * 
   * @throws WdkUserException
   * 
   */
  public void integrateAttributesQuery(Query attributeQuery) throws WdkModelException, WdkUserException {
    logger.debug("Integrating attributes query " + attributeQuery.getFullName());
    initPageRecordInstances();

    WdkModel wdkModel = _question.getWdkModel();
    // has to get a clean copy of the attribute query, without pk params
    // appended
    attributeQuery = (Query) wdkModel.resolveReference(attributeQuery.getFullName());

    //logger.debug("filling attribute values from answer " + attributeQuery.getFullName());
    for (Column column : attributeQuery.getColumns()) {
      logger.trace("column: '" + column.getName() + "'");
    }
    // if (attributeQuery instanceof SqlQuery)
    // logger.debug("SQL: \n" + ((SqlQuery) attributeQuery).getSql());

    String sql = getPagedAttributeSql(attributeQuery);
    int count = 0;

    // get and run the paged attribute query sql
    DatabaseInstance platform = wdkModel.getAppDb();
    DataSource dataSource = platform.getDataSource();

    ResultList resultList = null;
    try {
      resultList = new SqlResultList(SqlUtils.executeQuery(dataSource, sql, attributeQuery.getFullName() +
          "__attr-paged"));

      // fill in the column attributes
      PrimaryKeyAttributeField pkField = _question.getRecordClass().getPrimaryKeyAttributeField();
      Map<String, AttributeField> fields = _question.getAttributeFieldMap();

      while (resultList.next()) {
        // get primary key
        Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
        for (String column : pkField.getColumnRefs()) {
          pkValues.put(column, resultList.get(column));
        }
        PrimaryKeyAttributeValue primaryKey = new PrimaryKeyAttributeValue(pkField, pkValues);
        RecordInstance record = _pageRecordInstances.get(primaryKey);

        if (record == null) {
          StringBuffer error = new StringBuffer();
          error.append("Paged attribute query [");
          error.append(attributeQuery.getFullName());
          error.append("] returns rows that doesn't match the paged ");
          error.append("records. (");
          for (String pkName : pkValues.keySet()) {
            error.append(pkName).append(" = ");
            error.append(pkValues.get(pkName)).append(", ");
          }
          error.append(").\nPaged Attribute SQL:\n").append(sql);
          error.append("\n").append("Paged ID SQL:\n").append(getPagedIdSql());
          throw new WdkModelException(error.toString());
        }

        // fill in the column attributes
        for (String columnName : attributeQuery.getColumnMap().keySet()) {
          AttributeField field = fields.get(columnName);
          if (field != null && (field instanceof ColumnAttributeField)) {
            // valid attribute field, fill it in
            Object objValue = resultList.get(columnName);
            ColumnAttributeValue value = new ColumnAttributeValue((ColumnAttributeField) field, objValue);
            record.addAttributeValue(value);
          }
        }
        count++;
      }
    }
    catch (SQLException e) {
      logger.error("Error executing attribute query using SQL \"" + sql + "\"", e);
      throw new WdkModelException(e);
    }
    finally {
      if (resultList != null)
        resultList.close();
    }

    if (count != _pageRecordInstances.size()) {
      throw new WdkModelException("the integrated attribute query " +
          "doesn't return the same number of records in the current " + "page. Paged attribute sql:\n" + sql);
    }
    logger.debug("Attribute query [" + attributeQuery.getFullName() + "] integrated.");
  }

  // ------------------------------------------------------------------
  // Private Methods
  // ------------------------------------------------------------------

  private String getPagedAttributeSql(Query attributeQuery) throws WdkModelException, WdkUserException {
    // get the paged SQL of id query
    String idSql = getPagedIdSql();

    PrimaryKeyAttributeField pkField = _question.getRecordClass().getPrimaryKeyAttributeField();

    // combine the id query with attribute query
    String attributeSql = getAttributeSql(attributeQuery);
    StringBuffer sql = new StringBuffer(" /* the desired attributes, for a page of sorted results */ "
        + " SELECT aq.* FROM (");
    sql.append(idSql);
    sql.append(") pidq, (/* attribute query that returns attributes in a page */ ").append(attributeSql).append(
        ") aq WHERE ");

    boolean firstColumn = true;
    for (String column : pkField.getColumnRefs()) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append(" AND ");
      sql.append("aq.").append(column).append(" = pidq.").append(column);
    }
    return sql.toString();
  }

  public void integrateTableQuery(TableField tableField) throws WdkModelException, WdkUserException {
    initPageRecordInstances();

    WdkModel wdkModel = _question.getWdkModel();
    // has to get a clean copy of the attribute query, without pk params
    // appended
    Query tableQuery = tableField.getQuery();
    tableQuery = (Query) wdkModel.resolveReference(tableQuery.getFullName());

    /*logger.debug("integrate table query from answer: " + tableQuery.getFullName());
    for (Param param : tableQuery.getParams()) {
       logger.debug("param: " + param.getName());
    }
    */
    // get and run the paged attribute query sql
    String sql = getPagedTableSql(tableQuery);

    DatabaseInstance platform = wdkModel.getAppDb();
    DataSource dataSource = platform.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, tableQuery.getFullName() + "_table-paged");
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }
    ResultList resultList = new SqlResultList(resultSet);

    // initialize table values
    for (RecordInstance record : _pageRecordInstances.values()) {
      PrimaryKeyAttributeValue primaryKey = record.getPrimaryKey();
      TableValue tableValue = new TableValue(_user, primaryKey, tableField, true);
      record.addTableValue(tableValue);
    }

    // make table values
    PrimaryKeyAttributeField pkField = _question.getRecordClass().getPrimaryKeyAttributeField();
    while (resultList.next()) {
      // get primary key
      Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
      for (String column : pkField.getColumnRefs()) {
        pkValues.put(column, resultList.get(column));
      }
      PrimaryKeyAttributeValue primaryKey = new PrimaryKeyAttributeValue(pkField, pkValues);
      RecordInstance record = _pageRecordInstances.get(primaryKey);
      primaryKey.setValueContainer(record);

      if (record == null) {
        StringBuffer error = new StringBuffer();
        error.append("Paged table query [" + tableQuery.getFullName());
        error.append("] returned rows that doesn't match the paged ");
        error.append("records. (");
        for (String pkName : pkValues.keySet()) {
          Object pkValue = pkValues.get(pkName);
          error.append(pkName + " = " + pkValue + ", ");
        }
        error.append(").\nPaged table SQL:\n" + sql);
        error.append("\n" + "Paged ID SQL:\n" + getPagedIdSql());
        throw new WdkModelException(error.toString());
      }

      TableValue tableValue = record.getTableValue(tableField.getName());
      // initialize a row in table value
      tableValue.initializeRow(resultList);
    }
    logger.debug("Table query [" + tableQuery.getFullName() + "] integrated.");
  }

  private String getPagedTableSql(Query tableQuery) throws WdkModelException, WdkUserException {
    // get the paged SQL of id query
    String idSql = getPagedIdSql();

    PrimaryKeyAttributeField pkField = _question.getRecordClass().getPrimaryKeyAttributeField();

    // combine the id query with attribute query
    // make an instance from the original attribute query, and attribute
    // query has only one param, user_id. Note that the original
    // attribute query is different from the attribute query held by the
    // recordClass.
    Map<String, String> params = new LinkedHashMap<String, String>();
    String userId = Integer.toString(_user.getUserId());
    params.put(Utilities.PARAM_USER_ID, userId);
    QueryInstance<?> queryInstance = tableQuery.makeInstance(_user, params, true, 0,
        new LinkedHashMap<String, String>());
    String tableSql = queryInstance.getSql();
    StringBuffer sql = new StringBuffer("SELECT tq.* FROM (");
    sql.append(idSql);
    sql.append(") pidq, (").append(tableSql).append(") tq WHERE ");

    boolean firstColumn = true;
    for (String column : pkField.getColumnRefs()) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append(" AND ");
      sql.append("tq.").append(column).append(" = pidq.").append(column);
    }

    // replace the id_sql macro.  this sql must include filters (but not view filters)
    return sql.toString().replace(Utilities.MACRO_ID_SQL, getPagedIdSql(true));
  }

  public String getAttributeSql(Query attributeQuery) throws WdkModelException, WdkUserException {
    String queryName = attributeQuery.getFullName();
    Query dynaQuery = _question.getDynamicAttributeQuery();
    String sql;
    if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
      // the dynamic query doesn't have sql defined, the sql will be
      // constructed from the id query cache table.
      sql = _idsQueryInstance.getSql();
    }
    else {
      // make an instance from the original attribute query, and attribute
      // query has only one param, user_id. Note that the original
      // attribute query is different from the attribute query held by the
      // recordClass.
      Map<String, String> params = new LinkedHashMap<String, String>();
      String userId = Integer.toString(_user.getUserId());
      params.put(Utilities.PARAM_USER_ID, userId);
      QueryInstance<?> attributeQueryInstance;
      try {
        attributeQueryInstance = attributeQuery.makeInstance(_user, params, true, 0,
            new LinkedHashMap<String, String>());
      }
      catch (WdkUserException ex) {
        throw new WdkModelException(ex);
      }
      sql = attributeQueryInstance.getSql();

      // replace the id_sql macro.  the injected sql must include filters (but not view filters)
      sql = sql.replace(Utilities.MACRO_ID_SQL, getIdSql(null, true));
    }
    return sql;
  }

  public String getSortedIdSql() throws WdkModelException, WdkUserException {
      if (_sortedIdSql == null) _sortedIdSql = getSortedIdSql(false);
      return _sortedIdSql;
  }

  private String getSortedIdSql(boolean excludeViewFilters) throws WdkModelException, WdkUserException {

    String[] pkColumns = _question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();

    // get id sql
    String idSql = getIdSql(null, excludeViewFilters);

    // get sorting attribute queries
    Map<String, String> attributeSqls = new LinkedHashMap<String, String>();
    List<String> orderClauses = new ArrayList<String>();
    prepareSortingSqls(attributeSqls, orderClauses);

    StringBuffer sql = new StringBuffer("/* the ID query results, sorted */ SELECT ");
    boolean firstColumn = true;
    for (String pkColumn : pkColumns) {
      if (firstColumn)
        firstColumn = false;
      else
        sql.append(", ");
      sql.append("idq." + pkColumn);
    }

    sql.append(" FROM " + idSql + " idq");
    // add all tables involved
    for (String shortName : attributeSqls.keySet()) {
      sql.append(", (").append(attributeSqls.get(shortName)).append(") ");
      sql.append(shortName);
    }

    // add primary key join conditions
    boolean firstClause = true;
    for (String shortName : attributeSqls.keySet()) {
      for (String column : pkColumns) {
        if (firstClause) {
          sql.append(" WHERE ");
          firstClause = false;
        }
        else
          sql.append(" AND ");

        sql.append("idq.").append(column);
        sql.append(" = ");
        sql.append(shortName).append(".").append(column);
      }
    }

    // add order clause
    // always append primary key columns as the last sorting columns,
    // otherwise Oracle may generate unstable results through pagination
    // when the sorted columns are not unique.
    sql.append(" ORDER BY ");
    for (String clause : orderClauses) {
      sql.append(clause).append(", ");
    }
    firstClause = true;
    for (String column : pkColumns) {
      if (firstClause)
        firstClause = false;
      else
        sql.append(", ");
      sql.append("idq.").append(column);
    }

    logger.debug("sorted id sql constructed.");
    return sql.toString();
  }

  private String getPagedIdSql() throws WdkModelException, WdkUserException {
      return getPagedIdSql(false);
  }

  private String getPagedIdSql(boolean excludeViewFilters) throws WdkModelException, WdkUserException {
    String sortedIdSql = getSortedIdSql(excludeViewFilters);
    DatabaseInstance platform = _question.getWdkModel().getAppDb();
    String sql = platform.getPlatform().getPagedSql(sortedIdSql, _startIndex, _endIndex);

    // add comments to the sql
    sql = " /* a page of sorted ids */ " + sql;

    logger.debug("paged id sql constructed.");

    return sql;
  }

  public String getIdSql() throws WdkModelException, WdkUserException {
      return getIdSql(null, false);
  }

  private String getIdSql(String excludeFilter, boolean excludeViewFilters) throws WdkModelException, WdkUserException {
    try {
      String innerSql = _idsQueryInstance.getSql();

      // add comments to id sql
      innerSql = " /* the ID query */" + innerSql;

      int assignedWeight = _idsQueryInstance.getAssignedWeight();
      // apply old filter
      if (_filter != null) {
        innerSql = _filter.applyFilter(_user, innerSql, assignedWeight);
        innerSql = " /* old filter applied on id query */ " + innerSql;
      }

      // apply "new" filters
      if (_filterOptions != null) {
        logger.debug("applyFilters(): found filterOptions to apply to the ID SQL: excludeFilter: " + excludeFilter);
        innerSql = applyFilters(innerSql, _filterOptions, excludeFilter);
        innerSql = " /* new filter applied on id query */ " + innerSql;
      }
      // apply view filters if requested
      boolean viewFiltersApplied = (_viewFilterOptions != null && _viewFilterOptions.getSize() > 0);
      if (viewFiltersApplied && !excludeViewFilters){
        logger.debug("apply viewFilters(): excludeFilter: " + excludeFilter);
        innerSql = applyFilters(innerSql, _viewFilterOptions, excludeFilter);
        innerSql = " /* new view filter applied on id query */ " + innerSql;
      }
     
      innerSql = "(" + innerSql + ")";
      logger.debug("AnswerValue: ID SQL constructed with all filters:\n" + innerSql);

      return innerSql;
    }
    catch (WdkModelException | WdkUserException ex) {
      logger.error(ex.getMessage(), ex);
      ex.printStackTrace();
      throw ex;
    }
  }

  private String applyFilters(String innerSql, FilterOptionList filterOptions, String excludeFilter)
      throws WdkModelException, WdkUserException {

    if (filterOptions != null) {
      for (FilterOption filterOption : filterOptions.getFilterOptions().values()) {
        logger.debug("applying FilterOption:" + filterOption.getJSON().toString(2));
        if (excludeFilter == null || !filterOption.getKey().equals(excludeFilter)) {
          if (!filterOption.isDisabled()) {
            Filter filter = _question.getFilter(filterOption.getKey());
            innerSql = filter.getSql(this, innerSql, filterOption.getValue());
          }
        }
      }
    }
    return innerSql;
  }

  private void prepareSortingSqls(Map<String, String> sqls, Collection<String> orders)
      throws WdkModelException, WdkUserException {
    Map<String, AttributeField> fields = _question.getAttributeFieldMap();
    Map<String, String> querySqls = new LinkedHashMap<String, String>();
    Map<String, String> queryNames = new LinkedHashMap<String, String>();
    Map<String, String> orderClauses = new LinkedHashMap<String, String>();
    WdkModel wdkModel = _question.getWdkModel();
    logger.debug("sorting map: " + _sortingMap);
    for (String fieldName : _sortingMap.keySet()) {
      AttributeField field = fields.get(fieldName);
      if (field == null)
        continue;
      boolean ascend = _sortingMap.get(fieldName);
      Map<String, ColumnAttributeField> dependents = field.getColumnAttributeFields();
      for (ColumnAttributeField dependent : dependents.values()) {
        Column column = dependent.getColumn();
        //logger.debug("field [" + fieldName + "] depends on column [" + column.getName() + "]");
        Query query = column.getQuery();
        String queryName = query.getFullName();
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
        String sortingColumn = column.getSortingColumn();
        if (sortingColumn == null)
          sortingColumn = column.getName();
        boolean ignoreCase = column.isIgnoreCase();
        if (!orderClauses.containsKey(sortingColumn)) {
          // dependent not processed, process it
          StringBuffer clause = new StringBuffer();
          if (ignoreCase)
            clause.append("lower(");
          clause.append(queryNames.get(queryName));
          clause.append(".");
          clause.append(sortingColumn);
          if (ignoreCase)
            clause.append(")");
          clause.append(ascend ? " ASC" : " DESC");
          orderClauses.put(sortingColumn, clause.toString());
        }
      }
    }

    // fill the map of short name and sqls
    for (String queryName : queryNames.keySet()) {
      String shortName = queryNames.get(queryName);
      String sql = querySqls.get(queryName);
      sqls.put(shortName, sql);
    }
    orders.addAll(orderClauses.values());
  }

  /**
   * If not already initialized, initialize the page's record instances, setting each with its id (either just
   * primary key or that and project, if using a federated data source).
   * @param applyViewFilters 
   * 
   * @throws WdkUserException
   * 
   */
  private void initPageRecordInstances() throws WdkModelException, WdkUserException {
    if (_pageRecordInstances != null)
      return;

    //logger.debug("Initializing paged records......");
    _pageRecordInstances = new LinkedHashMap<PrimaryKeyAttributeValue, RecordInstance>();

    try {
      String sql = getPagedIdSql();
      WdkModel wdkModel = _question.getWdkModel();
      DatabaseInstance platform = wdkModel.getAppDb();
      DataSource dataSource = platform.getDataSource();
      ResultSet resultSet;
      try {
        resultSet = SqlUtils.executeQuery(dataSource, sql, _idsQueryInstance.getQuery().getFullName() +
            "__id-paged");
      }
      catch (SQLException e) {
        throw new WdkModelException(e);
      }
      try (ResultList resultList = new SqlResultList(resultSet)) {
        RecordClass recordClass = _question.getRecordClass();
        PrimaryKeyAttributeField pkField = recordClass.getPrimaryKeyAttributeField();
        while (resultList.next()) {
          // get primary key. the primary key is supposed to be translated to
          // the current ones from the id query, and no more translation
          // needed.
          //
          // If this assumption is false, then we need to join the alias query
          // into the paged id query as well.
          Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
          for (String column : pkField.getColumnRefs()) {
            Object value = resultList.get(column);
            pkValues.put(column, value);
          }
          RecordInstance record = new RecordInstance(this, pkValues);
          _pageRecordInstances.put(record.getPrimaryKey(), record);
        }
      }

      // check if the number of records is expected
      int resultSize = getResultSize();
      int expected = Math.min(_endIndex, resultSize) - _startIndex + 1;

      if (expected != _pageRecordInstances.size()) {
        StringBuffer buffer = new StringBuffer();
        for (String name : _attributes.getSummaryAttributeFieldMap().keySet()) {
          if (buffer.length() > 0)
            buffer.append(", ");
          buffer.append(name);
        }
        logger.debug("resultSize: " + resultSize + ", start: " + _startIndex + ", end: " + _endIndex);
        logger.debug("expected: " + expected + ", actual: " + _pageRecordInstances.size());
        logger.debug("Paged ID SQL:\n" + sql);
        throw new WdkModelException("The number of results returned " + "by the id query " +
            _idsQueryInstance.getQuery().getFullName() +
            " changes when it is joined to the query (or queries) " + "for attribute set (" + buffer +
            ").\n" + "id query: " + expected + " records\n" + "join(id query, attribute query): " +
            _pageRecordInstances.size() + " records\n" +
            "Check that the ID query returns no nulls or duplicates, " +
            "and that the attribute-query join " + "does not change the row count.");
      }
    }
    catch (WdkModelException | WdkUserException ex) {
      logger.error(ex.getMessage(), ex);
      ex.printStackTrace();
      throw ex;
    }

    //logger.debug("Paged records initialized.");
  }

  /**
   * @return Returns the endRecordInstanceI.
   */
  public int getEndIndex() {
    return _endIndex;
  }

  /**
   * @return Returns the startRecordInstanceI.
   */
  public int getStartIndex() {
    return _startIndex;
  }

  public String getResultMessage() {
    return _idsQueryInstance.getResultMessage();
  }

  public Map<String, Boolean> getSortingMap() {
    return new LinkedHashMap<String, Boolean>(_sortingMap);
  }

  /**
   * Set a new sorting map
   * 
   * @param sortingMap
   * @throws WdkModelException 
   */
  public void setSortingMap(Map<String, Boolean> sortingMap) throws WdkModelException {
    if (sortingMap == null) {
      sortingMap = _question.getSortingAttributeMap();
    }
    // make sure all sorting columns exist
    StringBuilder buffer = new StringBuilder("set sorting: ");
    Map<String, AttributeField> attributes = _question.getAttributeFieldMap();
    Map<String, Boolean> validMap = new LinkedHashMap<String, Boolean>();
    for (String attributeName : sortingMap.keySet()) {
      buffer.append(attributeName + "=" + sortingMap.get(attributeName) + ", ");
      // if a sorting attribute is invalid, instead of throwing out an
      // exception, ignore it.
      if (!attributes.containsKey(attributeName)) {
        // throw new
        // WdkModelException("the assigned sorting attribute ["
        // + attributeName + "] doesn't exist in the answer of "
        // + "question " + question.getFullName());
        logger.debug("Invalid sorting attribute: User #" + _user.getUserId() + ", question: '" +
            _question.getFullName() + "', attribute: '" + attributeName + "'");
      }
      else {
        validMap.put(attributeName, sortingMap.get(attributeName));
      }
    }
    logger.debug(buffer);
    _sortingMap.clear();
    _sortingMap.putAll(validMap);

    _sortedIdSql = null;
    _pageRecordInstances = null;
  }

  /**
   * This method is redundant with getAllIds(), consider deprecate either one of them.
   * 
   * @return returns a list of all primary key values.
   * @throws WdkUserException
   */
  public Object[][] getPrimaryKeyValues() throws WdkModelException, WdkUserException {
    String[] columns = _question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
    List<Object[]> buffer = new ArrayList<Object[]>();

    ResultList resultList;
    if (_filter == null)
      resultList = _idsQueryInstance.getResults();
    else
      resultList = _filter.getResults(this);

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

  public int getFilterDisplaySize(String filterName)
      throws WdkModelException, WdkUserException {
    return getFilterSize(filterName, true);
  }

  public int getFilterSize(String filterName)
      throws WdkModelException, WdkUserException {
    return getFilterSize(filterName, false);
  }

  public Map<String, Integer> getFilterDisplaySizes() {
    return getFilterSizes(true);
  }

  public Map<String, Integer> getFilterSizes() {
    return getFilterSizes(false);
  }

  private Map<String, Integer> getFilterSizes(boolean useDisplay) {
    RecordClass recordClass = _question.getRecordClass();
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    ConcurrentMap<String, Integer> sizes = new ConcurrentHashMap<>(filters.length);

    // use a thread pool to get filter sizes in parallel
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    for (AnswerFilterInstance filter : filters) {
      executor.execute(new FilterSizeTask(this, sizes, filter.getName(), useDisplay));
    }

    // wait for executor to finish.
    executor.shutdown();
    try {
      if (!executor.awaitTermination(THREAD_POOL_TIMEOUT, TimeUnit.MINUTES)) {
        executor.shutdownNow();
      }
    }
    catch (InterruptedException ex) {
      executor.shutdownNow();
    }

    return sizes;
  }

  private int getFilterSize(String filterName, boolean useDisplay)
      throws WdkModelException, WdkUserException {
    Integer size = _resultSizesByFilter.get(filterName);
    if (size != null && _idsQueryInstance.getIsCacheable()) {
      return size;
    }

    RecordClass recordClass = _question.getRecordClass();
    String innerSql = _idsQueryInstance.getSql();
    int assignedWeight = _idsQueryInstance.getAssignedWeight();

    // ignore invalid filters
    AnswerFilterInstance filter = recordClass.getFilterInstance(filterName);
    if (filter != null)
      innerSql = filter.applyFilter(_user, innerSql, assignedWeight);

    // if display count requested, use custom plugin; else use default
    ResultSize countPlugin = (useDisplay ?
        _question.getRecordClass().getResultSizePlugin() :
        new DefaultResultSizePlugin());

    // get size, cache, and return
    size = countPlugin.getResultSize(this, innerSql);
    _resultSizesByFilter.put(filterName, size);
    return size;
  }

  /**
   * @return the filter
   */
  public AnswerFilterInstance getFilter() {
    return _filter;
  }

  public void setFilterInstance(String filterName) {
    if (filterName != null) {
      RecordClass recordClass = _question.getRecordClass();
      setFilterInstance(recordClass.getFilterInstance(filterName));
    }
    else
      _filter = null;
  }

  /**
   * @param filter
   *          the filter to set
   */
  public void setFilterInstance(AnswerFilterInstance filter) {
    _filter = filter;
    reset();
  }

  private void reset() {
    _sortedIdSql = null;
    _pageRecordInstances = null;
    _resultSize = null;
    _resultSizesByFilter.clear();
    _resultSizesByProject = null;
    _checksum = null;
  }

  /**
   * Get a list of all the primary key tuples of all the records in the answer. It is a shortcut of iterating
   * through all the pages and get the primary keys.
   * 
   * This method is redundant with getPrimaryKeyValues()(), consider deprecate either one of them.
   * 
   * @return
   * @throws WdkUserException
   */
  public List<String[]> getAllIds() throws WdkModelException, WdkUserException {
    String idSql = getSortedIdSql();
    PrimaryKeyAttributeField pkField = _question.getRecordClass().getPrimaryKeyAttributeField();
    String[] pkColumns = pkField.getColumnRefs();
    List<String[]> pkValues = new ArrayList<String[]>();
    WdkModel wdkModel = _question.getWdkModel();
    DataSource dataSource = wdkModel.getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, idSql, _idsQueryInstance.getQuery().getFullName() +
          "__all-ids");
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
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    return pkValues;
  }

  public void setPageIndex(int startIndex, int endIndex) {
    _startIndex = startIndex;
    _endIndex = endIndex;
    _sortedIdSql = null;
    _pageRecordInstances = null;
  }

  public void setFilterOptions(FilterOptionList filterOptions) {
    logger.debug("****Answer Value: Setting filterOptions");
    if (_filterOptions != null) {
      logger.debug("size:" + filterOptions.getSize() + ", and they are: " + filterOptions.getJSON());
    }
    else {
      logger.debug("NULL");
    }
    _filterOptions = filterOptions;
    reset();
  }

  public void setViewFilterOptions(FilterOptionList viewFilterOptions) {
    _viewFilterOptions = viewFilterOptions;
    reset();
  }

  public FilterOptionList getFilterOptions() {
    return _filterOptions;
  }

  public FilterSummary getFilterSummary(String filterName) throws WdkModelException, WdkUserException {
    // need to exclude the given filter from the idSql, so that the selection of the current filter won't
    // affect the background;
    String idSql = getIdSql(filterName, false);
    Filter filter = _question.getFilter(filterName);
    return filter.getSummary(this, idSql);
  }
}
