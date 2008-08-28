package org.gusdb.wdk.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.BooleanQueryInstance;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.AnswerInfo;
import org.json.JSONException;

/**
 * Answer.java
 * 
 * Created: Fri June 4 13:01:30 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2007-01-22 12:33:24 -0500 (Mon, 22 Jan
 *          2007) $ $Author$
 */

/**
 * A list of RecordInstances representing one page of the answer to a Question.
 * The constructor of the Answer provides a handle (QueryInstance) on the
 * ResultList that is the list of primary keys for the all the records (not *
 * just one page) that are the answer to the Question. The ResultList also has a
 * column that contains the row number (RESULT_TABLE_I) so that a list of
 * primary keys for a single page can be efficiently accessed.
 * 
 * The Answer is lazy in that it only constructs the set of RecordInstances for
 * the page when the first RecordInstance is requested.
 * 
 * The initial request triggers the creation of skeletal RecordInstances for the
 * page. They contain only primary keys (these being acquired from the
 * ResultList).
 * 
 * These skeletal RecordInstances are also lazy in that they only run an
 * attributes query when an attribute provided by that query is requested. When
 * they do run an attribute query, its QueryInstance is put into joinMode. This
 * means that the attribute query joins with the table containing the primary
 * keys, and, in one database query, generates rows containing the attribute
 * values for all the RecordInstances in the page.
 * 
 * The method <code>integrateAttributesQueryResult</> is invoked by the
 * first RecordInstance in the page upon the first request for an attribute 
 * provided by an attributes query. The query is a join with the list of 
 * primary keys, and so has a row for each RecordInstance in the page, and
 * columns that provide the attribute values (plus RESULT_TABLE_I).  The 
 * values in the rows are integrated into the corresponding RecordInstance 
 * (now no longer skeletal).  <code>integrateAttributesQueryResult</> may
 * be called a number of times, depending upon how many attribute queries
 * the record class contains.
 * 
 * Attribute queries are guaranteed to provide one row for each RecordInstance
 * in the page.  An exception is thrown otherwise.
 *
 */
public class Answer {

    private static final Logger logger = Logger.getLogger(Answer.class);

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    private AnswerInfo answerInfo;

    private ResultFactory resultFactory;
    private Question question;

    private QueryInstance idsQueryInstance;

    private int startIndex;
    private int endIndex;

    private boolean isBoolean = false;

    private String pagedIdSql;

    private Map<PrimaryKeyAttributeValue, RecordInstance> pageRecordInstances;

    private Integer resultSize; // size of total result
    private Map<String, Integer> resultSizesByProject;

    private Map<String, Boolean> sortingAttributes;
    private Map<String, AttributeField> summaryAttributes;

    private SummaryView summaryView;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * @param question
     *            The <code>Question</code> to which this is the
     *            <code>Answer</code>.
     * @param idsQueryInstance
     *            The <co de>QueryInstance</code> that provides a handle on the
     *            ResultList containing all primary keys that are the result for
     *            the question (not just one page worth).
     * @param startRecordInstanceI
     *            The index of the first <code>RecordInstance</code> in the
     *            page. (>=1)
     * @param endRecordInstanceI
     *            The index of the last <code>RecordInstance</code> in the
     *            page, inclusive.
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    Answer(Question question, QueryInstance idsQueryInstance, int startIndex,
            int endIndex, Map<String, Boolean> sortingAttributes,
            SummaryView summaryView) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        this.question = question;
        this.resultFactory = question.getWdkModel().getResultFactory();
        this.idsQueryInstance = idsQueryInstance;
        this.isBoolean = (idsQueryInstance instanceof BooleanQueryInstance);
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        // get sorting columns
        this.sortingAttributes = sortingAttributes;
        this.summaryAttributes = new LinkedHashMap<String, AttributeField>();

        // get the view
        if (summaryView == null)
            summaryView = question.getRecordClass().getDefaultView();
        this.summaryView = summaryView;

        // save the answer
        AnswerFactory answerFactory = question.getWdkModel().getAnswerFactory();
        answerFactory.saveAnswer(this);
    }

    /**
     * A copy constructor, and
     * 
     * @param answer
     * @param startIndex
     * @param endIndex
     */
    public Answer(Answer answer, int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        this.idsQueryInstance = answer.idsQueryInstance;
        this.isBoolean = answer.isBoolean;
        this.question = answer.question;
        this.resultFactory = answer.resultFactory;
        this.sortingAttributes = new LinkedHashMap<String, Boolean>(
                answer.sortingAttributes);
        this.summaryAttributes = new LinkedHashMap<String, AttributeField>(
                answer.summaryAttributes);
        this.summaryView = answer.summaryView;
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * provide property that user's term for question
     */
    public Question getQuestion() {
        return this.question;
    }

    public int getPageSize() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initPageRecordInstances();
        return pageRecordInstances.size();
    }

    public int getPageCount() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        int total = getResultSize();
        int pageSize = endIndex - startIndex + 1;
        int pageCount = (int) Math.round(Math.ceil((float) total / pageSize));
        logger.debug("#Pages: " + pageCount + ",\t#Total: " + total
                + ",\t#PerPage: " + pageSize);
        return pageCount;
    }

    public int getResultSize() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resultSize == null) {
            // need to run the query first
            ResultList rl = idsQueryInstance.getResults();
            String message = idsQueryInstance.getResultMessage();
            boolean hasMessage = (message != null && message.length() > 0);
            if (hasMessage) {
                String[] sizes = idsQueryInstance.getResultMessage().split(",");
                for (String size : sizes) {
                    String[] parts = size.split(":");
                    resultSizesByProject.put(parts[0],
                            Integer.parseInt(parts[1]));
                }
            }
            int counter = 0;
            resultSizesByProject = new LinkedHashMap<String, Integer>();

            while (rl.next()) {
                counter++;

                if (!hasMessage) {
                    // also count by project
                    String project = rl.get(Utilities.COLUMN_PROJECT_ID).toString();
                    int subCounter = 0;
                    if (resultSizesByProject.containsKey(project))
                        subCounter = resultSizesByProject.get(project);
                    resultSizesByProject.put(project, ++subCounter);
                }
            }
            rl.close();
            resultSize = counter;
        }
        return resultSize.intValue();
    }

    public Map<String, Integer> getResultSizesByProject()
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        if (resultSizesByProject == null) getResultSize();
        return resultSizesByProject;

    }

    public boolean isDynamic() {
        return getQuestion().isDynamic();
    }

    /**
     * @return Map where key is param display name and value is param value
     */
    public Map<String, Object> getDisplayParams() {
        Map<String, Object> displayParamsMap = new LinkedHashMap<String, Object>();
        Map<String, Object> paramsMap = idsQueryInstance.getValues();
        Param[] params = question.getParams();
        for (int i = 0; i < params.length; i++) {
            Param param = params[i];
            displayParamsMap.put(param.getPrompt(),
                    paramsMap.get(param.getName()));
        }
        return displayParamsMap;
    }

    public boolean getIsBoolean() {
        return this.isBoolean;
    }

    public QueryInstance getIdsQueryInstance() {
        return idsQueryInstance;
    }

    public RecordInstance[] getRecordInstances() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initPageRecordInstances();

        RecordInstance[] array = new RecordInstance[pageRecordInstances.size()];
        pageRecordInstances.values().toArray(array);
        return array;
    }

    public RecordInstance getRecordInstance(PrimaryKeyAttributeValue primaryKey) {
        return pageRecordInstances.get(primaryKey);
    }

    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return idsQueryInstance.getChecksum();
    }

    // ///////////////////////////////////////////////////////////////////
    // print methods
    // ///////////////////////////////////////////////////////////////////

    public String printAsRecords() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        for (RecordInstance recordInstance : pageRecordInstances.values()) {
            buf.append(recordInstance.print());
            buf.append("---------------------" + newline);
        }
        return buf.toString();
    }

    /**
     * print summary attributes, one per line Note: not sure why this is needed
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public String printAsSummary() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        for (RecordInstance recordInstance : pageRecordInstances.values()) {
            buf.append(recordInstance.printSummary());
        }
        return buf.toString();
    }

    /**
     * print summary attributes in tab delimited table with header of attr.
     * names
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public String printAsTable() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        // print summary info
        buf.append("# of Records: " + getResultSize() + ",\t# of Pages: "
                + getPageCount() + ",\t# Records per Page: " + getPageSize()
                + newline);

        if (pageRecordInstances.size() == 0) return buf.toString();

        Map<String, AttributeField> attributes = getSummaryAttributes();
        for (String nextAttName : attributes.keySet()) {
            buf.append(nextAttName + "\t");
        }
        buf.append(newline);
        for (RecordInstance recordInstance : pageRecordInstances.values()) {
            // only print
            for (String nextAttName : attributes.keySet()) {
                // make data row
                AttributeValue value = recordInstance.getAttributeValue(nextAttName);
                // only print part of the string
                String str = value.getBriefValue();
                buf.append(str + "\t");
            }
            buf.append(newline);
        }
        return buf.toString();
    }

    public Reporter createReport(String reporterName, Map<String, String> config)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // get the full answer
        int endI = getResultSize();
        return createReport(reporterName, config, 1, endI);
    }

    public Reporter createReport(String reporterName,
            Map<String, String> config, int startI, int endI)
            throws WdkModelException {
        // get Reporter
        Map<String, ReporterRef> rptMap = question.getRecordClass().getReporterMap();
        ReporterRef rptRef = rptMap.get(reporterName);
        if (rptRef == null)
            throw new WdkModelException("The reporter " + reporterName + " is "
                    + "not registered for "
                    + question.getRecordClass().getFullName());
        String rptImp = rptRef.getImplementation();
        if (rptImp == null)
            throw new WdkModelException("The reporter " + reporterName + " is "
                    + "not registered for "
                    + question.getRecordClass().getFullName());

        try {
            Class<?> rptClass = Class.forName(rptImp);
            Class<?>[] paramClasses = { Answer.class, int.class, int.class };
            Constructor<?> constructor = rptClass.getConstructor(paramClasses);

            Object[] params = { this, startI, endI };
            Reporter reporter = (Reporter) constructor.newInstance(params);
            reporter.setProperties(rptRef.getProperties());
            reporter.configure(config);
            reporter.setWdkModel(rptRef.getWdkModel());
            return reporter;
        } catch (ClassNotFoundException ex) {
            throw new WdkModelException(ex);
        } catch (InstantiationException ex) {
            throw new WdkModelException(ex);
        } catch (IllegalAccessException ex) {
            throw new WdkModelException(ex);
        } catch (SecurityException ex) {
            throw new WdkModelException(ex);
        } catch (NoSuchMethodException ex) {
            throw new WdkModelException(ex);
        } catch (IllegalArgumentException ex) {
            throw new WdkModelException(ex);
        } catch (InvocationTargetException ex) {
            throw new WdkModelException(ex);
        }
    }

    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    /**
     * Integrate into the page's RecordInstances the attribute values from a
     * particular attributes query. The attributes query result includes only
     * rows for this page.
     * 
     * The query is obtained from Column, and the query should not be modified.
     * 
     * @throws SQLException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    void integrateAttributesQuery(Query attributeQuery) throws SQLException,
            NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException {
        initPageRecordInstances();

        // get and run the paged attribute query sql
        String sql = getPagedAttributeSql(attributeQuery);
        DBPlatform platform = question.getWdkModel().getQueryPlatform();
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        ResultList resultList = new SqlResultList(resultSet);

        // fill in the column attributes
        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();
        Map<String, AttributeField> fields = question.getAttributeFields();
        while (resultList.next()) {
            // get primary key
            Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
            for (String column : pkField.getColumnRefs()) {
                pkValues.put(column, resultList.get(column));
            }
            PrimaryKeyAttributeValue primaryKey = new PrimaryKeyAttributeValue(
                    pkField, pkValues);
            RecordInstance record = pageRecordInstances.get(primaryKey);

            // fill in the column attributes
            for (String columnName : attributeQuery.getColumnMap().keySet()) {
                AttributeField field = fields.get(columnName);
                if (field != null && (field instanceof ColumnAttributeField)) {
                    // valid attribute field, fill it in
                    Object objValue = resultList.get(columnName);
                    ColumnAttributeValue value = new ColumnAttributeValue(
                            (ColumnAttributeField) field, objValue);
                    record.addColumnAttributeValue(value);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private String getPagedAttributeSql(Query attributeQuery)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        // get the paged SQL of id query
        String idSql = getPagedIdSql();

        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();

        // combine the id query with attribute query
        String attributeSql = getAttributeSql(attributeQuery);
        StringBuffer sql = new StringBuffer("SELECT a.* FROM (");
        sql.append(idSql);
        sql.append(") i, (").append(attributeSql).append(") a WHERE ");

        boolean firstColumn = true;
        for (String column : pkField.getColumnRefs()) {
            if (firstColumn) firstColumn = false;
            else sql.append(" AND ");
            sql.append("a.").append(column).append(" = i.").append(column);
        }
        return sql.toString();
    }

    private String getAttributeSql(Query attributeQuery)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        String queryName = attributeQuery.getFullName();
        Query dynaQuery = question.getDynamicAttributeQuery();
        if (dynaQuery != null && queryName.equals(dynaQuery.getFullName())) {
            // the dynamic query doesn't have sql defined, the sql will be
            // constructed from the id query cache table.
            String cacheTable = CacheFactory.normalizeTableName(idsQueryInstance.getQuery().getFullName());
            int instanceId = idsQueryInstance.getInstanceId();

            StringBuffer sql = new StringBuffer("SELECT * FROM ");
            sql.append(cacheTable).append(" WHERE ");
            sql.append(CacheFactory.COLUMN_INSTANCE_ID);
            sql.append(" = ").append(instanceId);
            return sql.toString();
        } else if (!(attributeQuery instanceof SqlQuery)
                || attributeQuery.isCached()) {
            // cache the attribute query, and use the cache; no params needed
            // for attribute query
            Map<String, Object> params = new LinkedHashMap<String, Object>();
            QueryInstance queryInstance = attributeQuery.makeInstance(params);
            String cacheTable = CacheFactory.normalizeTableName(queryName);
            int instanceId = resultFactory.getInstanceId(queryInstance);

            StringBuffer sql = new StringBuffer("SELECT * FROM ");
            sql.append(cacheTable);
            sql.append(" WHERE ");
            sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(" = ");
            sql.append(instanceId);
            return sql.toString();
        } else return ((SqlQuery) attributeQuery).getSql();

    }

    private String getPagedIdSql() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        if (pagedIdSql != null) return pagedIdSql;

        // get id sql
        String idSql = getIdSql();

        // get sorting attribute queries
        Map<String, String> attributeSqls = new LinkedHashMap<String, String>();
        List<String> orderClauses = new ArrayList<String>();
        prepareSortingSqls(attributeSqls, orderClauses);

        StringBuffer sql = new StringBuffer("SELECT i.* FROM (");
        sql.append(idSql).append(") i");
        // add all tables involved
        for (String shortName : attributeSqls.keySet()) {
            sql.append(", (").append(attributeSqls.get(shortName)).append(") ");
            sql.append(shortName);
        }

        // add primary key join conditions
        String[] pkColumns = question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
        boolean firstClause = true;
        for (String shortName : attributeSqls.keySet()) {
            for (String column : pkColumns) {
                if (firstClause) {
                    sql.append(" WHERE ");
                    firstClause = false;
                } else sql.append(" AND ");

                sql.append("i.").append(column);
                sql.append(" = ");
                sql.append(shortName).append(".").append(column);
            }
        }

        // add order clause
        if (orderClauses.size() > 0) {
            sql.append(" ORDER BY ");
            firstClause = true;
            for (String clause : orderClauses) {
                if (firstClause) firstClause = false;
                else sql.append(", ");
                sql.append(clause);
            }
        }

        DBPlatform platform = question.getWdkModel().getQueryPlatform();
        pagedIdSql = platform.getPagedSql(sql.toString(), startIndex, endIndex);
        return pagedIdSql;
    }

    private String getIdSql() throws NoSuchAlgorithmException, SQLException,
            WdkModelException, JSONException, WdkUserException {
        if (summaryView != null) { // get a summary view
            Param columParam = summaryView.getSummaryTable().getColumnParam();
            Param rowParam = summaryView.getSummaryTable().getRowParam();

            // prepare the params for summary view
            Map<String, Object> params = new LinkedHashMap<String, Object>();
            params.put(columParam.getName(), summaryView.getColumnTerm());
            params.put(rowParam.getName(), summaryView.getRowTerm());
            params.put(summaryView.getAnswerParam().getName(), getChecksum());

            // get a summary view query instance
            Query query = summaryView.getSummaryQuery();
            QueryInstance instance = query.makeInstance(params);
            return instance.getSql();
        } else { // get the id query directly
            return idsQueryInstance.getSql();
        }
    }

    private void prepareSortingSqls(Map<String, String> sqls,
            Collection<String> orders) throws WdkModelException, JSONException,
            NoSuchAlgorithmException, SQLException, WdkUserException {
        Map<String, AttributeField> fields = question.getAttributeFields();
        Map<String, String> querySqls = new LinkedHashMap<String, String>();
        Map<String, String> queryNames = new LinkedHashMap<String, String>();
        Map<String, String> orderClauses = new LinkedHashMap<String, String>();
        for (String fieldName : sortingAttributes.keySet()) {
            AttributeField field = fields.get(fieldName);
            boolean ascend = sortingAttributes.get(fieldName);
            for (ColumnAttributeField dependent : field.getDependents()) {
                Query query = dependent.getColumn().getQuery();
                String queryName = query.getFullName();

                // handle query
                if (!queryNames.containsKey(queryName)) {
                    // query not processed yet, process it
                    String shortName = "a" + queryNames.size();
                    String sql = getAttributeSql(query);
                    queryNames.put(queryName, shortName);
                    querySqls.put(queryName, sql);
                }

                // handle column
                String dependentName = dependent.getName();
                if (!orderClauses.containsKey(dependentName)) {
                    // dependent not processed, process it
                    StringBuffer clause = new StringBuffer();
                    clause.append(queryNames.get(queryName));
                    clause.append(".");
                    clause.append(dependentName);
                    clause.append(ascend ? " ASC" : " DESC");
                    orderClauses.put(dependentName, clause.toString());
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
     * If not already initialized, initialize the page's record instances,
     * setting each with its id (either just primary key or that and project, if
     * using a federated data source).
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    private void initPageRecordInstances() throws NoSuchAlgorithmException,
            SQLException, JSONException, WdkModelException, WdkUserException {
        if (pageRecordInstances != null) return;

        // store answer info
        AnswerFactory answerFactory = question.getWdkModel().getAnswerFactory();
        answerInfo = answerFactory.saveAnswer(this);

        this.pageRecordInstances = new LinkedHashMap<PrimaryKeyAttributeValue, RecordInstance>();

        String sql = getPagedIdSql();
        DBPlatform platform = question.getWdkModel().getQueryPlatform();
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        ResultList resultList = new SqlResultList(resultSet);
        PrimaryKeyAttributeField pkField = question.getRecordClass().getPrimaryKeyAttributeField();
        while (resultList.next()) {
            // get primary key. the primary key is supposed to be translated to
            // the current ones from the id query, and no more translation
            // needed.
            // 
            // If this assumption is false, then we need to join the alias query
            // into the paged id query as well.
            Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
            for (String column : pkField.getColumnRefs()) {
                pkValues.put(column, resultList.get(column));
            }
            PrimaryKeyAttributeValue primaryKey = new PrimaryKeyAttributeValue(
                    pkField, pkValues);
            RecordInstance record = new RecordInstance(this, primaryKey);
            pageRecordInstances.put(primaryKey, record);
        }
    }

    /**
     * @return Returns the endRecordInstanceI.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * @return Returns the startRecordInstanceI.
     */
    public int getStartIndex() {
        return startIndex;
    }

    public String getResultMessage() {
        return idsQueryInstance.getResultMessage();
    }

    public Map<String, Boolean> getSortingAttributes() {
        return new LinkedHashMap<String, Boolean>(sortingAttributes);
    }

    public List<AttributeField> getDisplayableAttributes() {
        List<AttributeField> displayAttributes = new ArrayList<AttributeField>();
        Map<String, AttributeField> attributes = question.getAttributeFields();
        Map<String, AttributeField> summaryAttributes = getSummaryAttributes();
        for (String attriName : attributes.keySet()) {
            AttributeField attribute = attributes.get(attriName);

            // the sortable attribute cannot be internal
            if (attribute.isInternal()) continue;

            // skip the attributes that are already displayed
            if (summaryAttributes.containsKey(attriName)) continue;

            displayAttributes.add(attribute);
        }
        return displayAttributes;
    }

    public Map<String, AttributeField> getSummaryAttributes() {
        // the list might be different from the ones in question, since they
        // can be customized.
        if (summaryAttributes.size() == 0) {
            summaryAttributes.putAll(question.getSummaryAttributes());
        }
        return new LinkedHashMap<String, AttributeField>(summaryAttributes);
    }

    public void setSumaryAttributes(String[] attributeNames) {
        summaryAttributes.clear();
        for (String attributeName : attributeNames) {
            AttributeField field = question.getAttributeFields().get(
                    attributeName);
            summaryAttributes.put(attributeName, field);
        }
    }

    /**
     * @return returns a list of all primary key values.
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     */
    public Object[][] getPrimaryKeyValues() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        String[] columns = question.getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
        int resultSize = getResultSize();
        Object[][] ids = new String[resultSize][columns.length];

        ResultList resultList = idsQueryInstance.getResults();
        int rowIndex = 0;
        while (resultList.next()) {
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                ids[rowIndex][columnIndex] = resultList.get(columns[columnIndex]);
            }
        }
        return ids;
    }

    public AnswerInfo getAnswerInfo() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        initPageRecordInstances();
        return answerInfo;
    }

    public Map<String, Map<String, Integer>> getSummaryCount(
            String summaryTableName) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        initPageRecordInstances();
        SummaryTable summaryTable = question.getRecordClass().getSummaryTable(
                summaryTableName);
        return summaryTable.getSummaryCount(this);
    }

    public Map<String, Map<String, Map<String, Integer>>> getAllSummaryCount()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        Map<String, Map<String, Map<String, Integer>>> allSummaries = new LinkedHashMap<String, Map<String, Map<String, Integer>>>();
        for (SummaryTable summaryTable : question.getRecordClass().getSummaryTables()) {
            String summaryName = summaryTable.getName();
            allSummaries.put(summaryName, getSummaryCount(summaryName));
        }
        return allSummaries;
    }
}
