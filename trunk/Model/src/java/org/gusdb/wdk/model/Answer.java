package org.gusdb.wdk.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.report.Reporter;

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

    private Question question;

    private QueryInstance idsQueryInstance;

    private QueryInstance attributesQueryInstance;

    private RecordInstance[] pageRecordInstances;

    int startRecordInstanceI;

    int endRecordInstanceI;

    private int recordInstanceCursor;

    private String recordIdColumnName;

    private String recordProjectColumnName;

    private boolean isBoolean = false;

    private Integer resultSize; // size of total result

    private Map<String, Integer> resultSizesByProject = null;

    private Map<String, Boolean> sortingAttributes;

    private Map<String, AttributeField> summaryAttributes;

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
     */
    Answer(Question question, QueryInstance idsQueryInstance,
            int startRecordInstanceI, int endRecordInstanceI,
            Map<String, Boolean> sortingAttributes) throws WdkModelException {
        this.question = question;
        this.idsQueryInstance = idsQueryInstance;
        this.isBoolean = (idsQueryInstance instanceof BooleanQueryInstance);
        this.recordInstanceCursor = 0;
        this.startRecordInstanceI = startRecordInstanceI;
        this.endRecordInstanceI = endRecordInstanceI;

        // get sorting columns
        this.sortingAttributes = sortingAttributes;

        this.summaryAttributes = new LinkedHashMap<String, AttributeField>();
        
        this.idsQueryInstance.setRecordClass(question.getRecordClass());
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

    public int getPageSize() {
        return pageRecordInstances == null ? 0 : pageRecordInstances.length;
    }

    public int getPageCount() throws WdkModelException {
        int total = (resultSize == null) ? getResultSize() : resultSize;
        int pageSize = endRecordInstanceI - startRecordInstanceI + 1;
        int pageCount = (int) Math.round(Math.ceil((float) total / pageSize));
        logger.debug("#Pages: " + pageCount + ",\t#Total: " + total
                + ",\t#PerPage: " + pageSize);
        return pageCount;
    }

    public int getResultSize() throws WdkModelException {
        if (resultSize == null || resultSizesByProject == null) {
            resultSizesByProject = new LinkedHashMap<String, Integer>();
            // fill the project column name
            findPrimaryKeyColumnNames();

            ResultList rl = idsQueryInstance.getResult();
            int counter = 0;
            while (rl.next()) {
                counter++;
                // get the project id
                if (recordProjectColumnName != null) {
                    String project = rl.getValue(recordProjectColumnName).toString();
                    int subCounter = 0;
                    if (resultSizesByProject.containsKey(project))
                        subCounter = resultSizesByProject.get(project);
                    resultSizesByProject.put(project, ++subCounter);
                }
            }
            rl.close();
            resultSize = new Integer(counter);
        }
        return resultSize.intValue();
    }

    public Map<String, Integer> getResultSizesByProject()
            throws WdkModelException {
        // fill the result size map grouped by project id
        // if ( resultSizesByProject == null ) getResultSize();
        // return resultSizesByProject;

        // fill the result size map grouped by project id
        // if (resultSizesByProject == null){
        if (idsQueryInstance.getResultMessage().equals("")) { // added by
            // Cary P
            getResultSize();
        } else {// Added by Cary P
            String message = idsQueryInstance.getResultMessage();
            String[] sizes = message.split(",");
            for (String size : sizes) {
                String[] ss = size.split(":");
                resultSizesByProject.put(ss[0], Integer.parseInt(ss[1]));
            }
        }// End Cary P
        // }
        return resultSizesByProject;

    }

    public boolean isDynamic() {
        return getQuestion().isDynamic();
    }

    /**
     * @return Map where key is param name and value is param value
     */
    public Map<String, Object> getParams() {
        return idsQueryInstance.getValuesMap();
    }

    /**
     * @return Map where key is param display name and value is param value
     */
    public Map<String, Object> getDisplayParams() {
        Map<String, Object> displayParamsMap = new LinkedHashMap<String, Object>();
        Map<String, Object> paramsMap = getParams();
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

    // this method is wrong. it should be plural, and return
    // all the attributes query instances. this returns only the last
    // one made, which is bogus. it is used by wdkSummary --showQuery
    // which itself should be --showQueries
    public QueryInstance getAttributesQueryInstance() {
        return attributesQueryInstance;
    }

    public QueryInstance getIdsQueryInstance() {
        return idsQueryInstance;
    }

    public Map<String, AttributeField> getAttributeFields() {
        return question.getAttributeFields();
    }

    public Map<String, AttributeField> getReportMakerAttributeFields() {
        return question.getReportMakerAttributeFields();
    }

    public Map<String, TableField> getReportMakerTableFields() {
        return question.getReportMakerTableFields();
    }

    public boolean isSummaryAttribute(String attName) {
        return question.isSummaryAttribute(attName);
    }

    private void releaseRecordInstances() {
        if (pageRecordInstances != null && pageRecordInstances.length > 0) {
            pageRecordInstances = new RecordInstance[0];
            recordInstanceCursor = 0;
        }
    }

    // Returns null if we have already returned the last instance
    public RecordInstance getNextRecordInstance() throws WdkModelException {
        try {
            initPageRecordInstances();

            RecordInstance nextInstance = null;
            if (recordInstanceCursor < pageRecordInstances.length) {
                nextInstance = pageRecordInstances[recordInstanceCursor];
                recordInstanceCursor++;
            }
            if (nextInstance == null) {
                // clean up the record instances
                releaseRecordInstances();
            }
            return nextInstance;
        } catch (WdkModelException ex) {
            releaseRecordInstances();
            throw ex;
        }
    }

    public boolean hasMoreRecordInstances() throws WdkModelException {
        try {
            initPageRecordInstances();

            if (pageRecordInstances == null) {
                logger.warn("pageRecordInstances is still null");
            }
            if (recordInstanceCursor >= pageRecordInstances.length) {
                return false;
            } else return true;
        } catch (WdkModelException ex) {
            releaseRecordInstances();
            throw ex;
        }
    }

    public Integer getDatasetId() throws WdkModelException {
        Integer datasetId = idsQueryInstance.getQueryInstanceId();
        if (datasetId == null) idsQueryInstance.getResultAsTableName();
        return idsQueryInstance.getQueryInstanceId();
    }

    // ///////////////////////////////////////////////////////////////////
    // print methods
    // ///////////////////////////////////////////////////////////////////

    public String printAsRecords() throws WdkModelException, WdkUserException {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        for (int i = 0; i < pageRecordInstances.length; i++) {
            buf.append(pageRecordInstances[i].print());
            buf.append("---------------------" + newline);
        }
        return buf.toString();
    }

    /**
     * print summary attributes, one per line Note: not sure why this is needed
     */
    public String printAsSummary() throws WdkModelException, WdkUserException {
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        for (int i = 0; i < pageRecordInstances.length; i++) {
            buf.append(pageRecordInstances[i].printSummary());
        }
        return buf.toString();
    }

    /**
     * print summary attributes in tab delimited table with header of attr.
     * names
     */
    public String printAsTable() throws WdkModelException, WdkUserException {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer();

        initPageRecordInstances();

        // print summary info
        buf.append("# of Records: " + getResultSize() + ",\t# of Pages: "
                + getPageCount() + ",\t# Records per Page: " + getPageSize()
                + newline);

        if (pageRecordInstances.length == 0) return buf.toString();

        for (int i = -1; i < pageRecordInstances.length; i++) {

            // only print
            for (String nextAttName : getSummaryAttributes().keySet()) {
                // make header
                if (i == -1) buf.append(nextAttName + "\t");

                // make data row
                else {
                    AttributeField field = getAttributeFields().get(nextAttName);
                    Object value = pageRecordInstances[i].getAttributeValue(field);
                    if (value == null) value = "";
                    // only print part of the string
                    String str = value.toString().trim();
                    if (str.length() > 50) str = str.substring(0, 47) + "...";
                    buf.append(str + "\t");
                }
            }
            buf.append(newline);
        }

        return buf.toString();
    }

    public Reporter createReport(String reporterName, Map<String, String> config)
            throws WdkModelException {
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
     */
    void integrateAttributesQueryResult(QueryInstance attributesQueryInstance)
            throws WdkModelException {
        // TEST
        // logger.debug("Question is: " + question.hashCode());
        // logger.debug("#Summary Attributes: "
        // + question.getSummaryAttributes().size());

        this.attributesQueryInstance = attributesQueryInstance;

        boolean isDynamic = attributesQueryInstance.getQuery().getParam(
                DynamicAttributeSet.RESULT_TABLE) != null;

        logger.debug("AttributeQuery is: "
                + attributesQueryInstance.getQuery().getFullName());
        logger.debug("isDynamic=" + isDynamic);

        CacheTable cacheTable = idsQueryInstance.getCacheTable();
        Set<SortingColumn> sortingColumns = getSortingColumns(sortingAttributes);
        attributesQueryInstance.setSortingColumns(sortingColumns);
        attributesQueryInstance.initJoinMode(cacheTable,
                recordProjectColumnName, recordIdColumnName,
                startRecordInstanceI, endRecordInstanceI, isDynamic);

        // Initialize with nulls (handle missing attribute rows)
        Map<PrimaryKeyValue, RecordInstance> recordInstanceMap = new LinkedHashMap<PrimaryKeyValue, RecordInstance>();
        for (RecordInstance recordInstance : pageRecordInstances) {
            setColumnValues(recordInstance, attributesQueryInstance, isDynamic,
                    recordIdColumnName, recordProjectColumnName, null);
            PrimaryKeyValue primaryKey = recordInstance.getPrimaryKey();
            recordInstanceMap.put(primaryKey, recordInstance);
        }

        // int pageIndex = 0;
        // int idsResultTableI = startRecordInstanceI;
        Set<PrimaryKeyValue> primaryKeySet = new LinkedHashSet<PrimaryKeyValue>();
        ResultList attrQueryResultList = attributesQueryInstance.getResult();
        while (attrQueryResultList.next()) {

            String id = attrQueryResultList.getValue(recordIdColumnName).toString();
            String project = null;
            if (recordProjectColumnName != null) {
                project = attrQueryResultList.getValue(recordProjectColumnName).toString();
            }

            PrimaryKeyValue attrPrimaryKey = new PrimaryKeyValue(
                    getQuestion().getRecordClass().getPrimaryKeyField(),
                    project, id.toString());

            if (primaryKeySet.contains(attrPrimaryKey)) {
                String msg = "Result Table "
                        + cacheTable.getCacheTableFullName()
                        + " for Attribute query "
                        + attributesQueryInstance.getQuery().getFullName()
                        + " " + " has more than one row for " + attrPrimaryKey;
                // close connection before throwing out the exception
                attrQueryResultList.close();
                throw new WdkModelException(msg);
            } else {
                primaryKeySet.add(attrPrimaryKey);
            }

            RecordInstance recordInstance = recordInstanceMap.get(attrPrimaryKey);
            if (recordInstance == null) {
                throw new WdkModelException(
                        "Can't find record instance for primary key '"
                                + attrPrimaryKey + "' from attribute query " 
                                + attributesQueryInstance.getQuery().getFullName());
            }

            setColumnValues(recordInstance, attributesQueryInstance, isDynamic,
                    recordIdColumnName, recordProjectColumnName,
                    attrQueryResultList);
        }
        attrQueryResultList.close();
    }

    private void setColumnValues(RecordInstance recordInstance,
            QueryInstance attributesQueryInstance, boolean isDynamic,
            String recordIdColumnName, String recordProjectColumnName,
            ResultList attrQueryResultList) throws WdkModelException {

        Column[] columns = attributesQueryInstance.getQuery().getColumns();

        for (int i = 0; i < columns.length; i++) {
            String colName = columns[i].getName();
            if (colName.equalsIgnoreCase(recordIdColumnName)) continue;
            if (colName.equalsIgnoreCase(recordProjectColumnName)) continue;
            Object value = null;
            if (attrQueryResultList != null)
                value = attrQueryResultList.getValue(colName);

            if (isDynamic) recordInstance.setAttributeValue(colName, value,
                    attributesQueryInstance.getQuery());
            else recordInstance.setAttributeValue(colName, value);
        }
    }

    public String[] findPrimaryKeyColumnNames() {
        String[] names = findPrimaryKeyColumnNames(idsQueryInstance.getQuery());
        recordIdColumnName = names[0];
        recordProjectColumnName = names[1];
        return names;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    /**
     * If not already initialized, initialize the page's record instances,
     * setting each with its id (either just primary key or that and project, if
     * using a federated data source).
     */
    private void initPageRecordInstances() throws WdkModelException {
        if (pageRecordInstances != null) return;

        RecordClass recordClass = question.getRecordClass();
        
        // set instance variables projectColumnName and idsColumnName
        findPrimaryKeyColumnNames();
        idsQueryInstance.projectColumnName = recordProjectColumnName;
        idsQueryInstance.primaryKeyColumnName = recordIdColumnName;
        idsQueryInstance.setSortingColumns(getSortingColumns(sortingAttributes));

        ResultList rl = idsQueryInstance.getPersistentResultPage(
                startRecordInstanceI, endRecordInstanceI);

        Vector<RecordInstance> tempRecordInstances = new Vector<RecordInstance>();

        while (rl.next()) {
            String project = null;
            if (recordProjectColumnName != null)
                project = rl.getValue(recordProjectColumnName).toString();
            String id = rl.getValue(recordIdColumnName).toString();
            RecordInstance nextRecordInstance = recordClass.makeRecordInstance(
                    project, id);
            nextRecordInstance.setDynamicAttributeFields(question.getDynamicAttributeFields());

            nextRecordInstance.setAnswer(this);
            tempRecordInstances.add(nextRecordInstance);
        }
        pageRecordInstances = new RecordInstance[tempRecordInstances.size()];
        tempRecordInstances.copyInto(pageRecordInstances);
        rl.close();
    }

    /**
     * Given a set of columns, find the id and project column names The project
     * column is optional. Assumption: the id and project columns are the first
     * two columns, but, they may be (id, project) or (project, id)
     * 
     * @return array where first element is pk col name, second is project
     */
    static String[] findPrimaryKeyColumnNames(Query query) {
        Column[] columns = query.getColumns();
        String[] names = new String[2];

        // assume id is in first column and no project column
        names[0] = columns[0].getName();
        names[1] = null;

        // having two columns, one is for Id and one for project
        if (columns.length > 1) {
            if (columns[0].getName().toUpperCase().indexOf("PROJECT") != -1) {
                names[0] = columns[1].getName();
                names[1] = columns[0].getName();
            } else if (columns[1].getName().toUpperCase().indexOf("PROJECT") != -1) {
                names[1] = columns[1].getName();
            }
        }
        return names;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(question.getDisplayName());

        Map<String, Object> params = getParams();

        for (Object key : params.keySet()) {
            sb.append(" " + key + ":" + params.get(key));
        }
        return sb.toString();
    }

    public Answer newAnswer() throws WdkModelException {
        // instead of cloning all parts of an answer, just initialize it as a
        // new answer, and the queries can be re-run without any assumption
        return newAnswer(startRecordInstanceI, endRecordInstanceI);
    }

    public Answer newAnswer(int startIndex, int endIndex)
            throws WdkModelException {
        Answer answer = new Answer(question, idsQueryInstance,
                startIndex, endIndex, sortingAttributes);
        answer.summaryAttributes = new LinkedHashMap<String, AttributeField>(
                this.summaryAttributes);
        return answer;
    }

    /**
     * @return Returns the endRecordInstanceI.
     */
    public int getEndRecordInstanceI() {
        return endRecordInstanceI;
    }

    /**
     * @return Returns the startRecordInstanceI.
     */
    public int getStartRecordInstanceI() {
        return startRecordInstanceI;
    }

    public String getResultMessage() {
        return idsQueryInstance.getResultMessage();
    }

    /**
     * @return get the name as a combination of question full name, parameter
     *         and values
     */
    public String getName() {
        StringBuffer nameBuf = new StringBuffer();
        nameBuf.append(question.getDisplayName() + ": ");

        Map<String, Param> params = question.getParamMap();
        Map<String, Object> paramValues = idsQueryInstance.getValuesMap();

        boolean first = true;
        for (String paramName : paramValues.keySet()) {
            Param param = params.get(paramName);
            Object value = paramValues.get(paramName);
            if (!first) nameBuf.append(", ");
            else first = false;
            nameBuf.append(param.getPrompt() + " = ");
            nameBuf.append(value);
        }
        return nameBuf.toString();
    }

    private Set<SortingColumn> getSortingColumns(
            Map<String, Boolean> sortingAttributes) throws WdkModelException {
        Set<SortingColumn> columns = new LinkedHashSet<SortingColumn>();
        for (String attrName : sortingAttributes.keySet()) {
            boolean ascending = sortingAttributes.get(attrName);
            Set<SortingColumn> subColumns = findColumns(attrName, ascending);
            for (SortingColumn column : subColumns) {
                if (!columns.contains(column))
                // ignore the order of the children, since it should be the
                    // same
                    // as the parent
                    columns.add(column);
            }
        }
        return columns;
    }

    private Set<SortingColumn> findColumns(String attrName, boolean ascending)
            throws WdkModelException {
        AttributeField attribute = question.getAttributeFields().get(attrName);
        if (attribute == null)
            throw new WdkModelException("The sorting attribute " + attrName
                    + " cannot be found in the record.");

        Set<SortingColumn> columns = new LinkedHashSet<SortingColumn>();
        if (attribute instanceof PrimaryKeyField) {
            PrimaryKeyField primaryKey = (PrimaryKeyField) attribute;
            Column column = null;
            // get project column, if have
            if (primaryKey.hasProjectParam()) {
                column = idsQueryInstance.getQuery().getColumn(
                        recordProjectColumnName);
                String tableName = column.getSortingTable();
                if (tableName == null)
                    tableName = idsQueryInstance.getResultAsTableName();
                String columnName = column.getSortingColumn();
                boolean lowerCase = column.isLowerCase();
                SortingColumn sColumn = new SortingColumn(tableName,
                        columnName, ascending, lowerCase);
                if (!columns.contains(sColumn)) columns.add(sColumn);
            }
            // get record id column
            column = idsQueryInstance.getQuery().getColumn(recordIdColumnName);
            String tableName = column.getSortingTable();
            if (tableName == null)
                tableName = idsQueryInstance.getResultAsTableName();
            String columnName = column.getSortingColumn();
            boolean lowerCase = column.isLowerCase();
            SortingColumn sColumn = new SortingColumn(tableName, columnName,
                    ascending, lowerCase);
            if (!columns.contains(sColumn)) columns.add(sColumn);
        } else if (attribute instanceof ColumnAttributeField) {
            ColumnAttributeField columnAttribute = (ColumnAttributeField) attribute;
            Column column = columnAttribute.getColumn();
            // check if sortingTable presents
            if (column == null)
                throw new WdkModelException(
                        "Null column in Column Attribute: '"
                                + columnAttribute.getName()
                                + "'.  This may happen if you have declared a column attribute in the record, but the underlying query doesn't have that column declared");
            String tableName = column.getSortingTable();
            if (tableName == null) {
                if (column.isDynamicColumn()) {// use cache table
                    tableName = idsQueryInstance.getResultAsTableName();
                } else {// no sorting table specified, error
                    throw new WdkModelException(
                            "The sortingTable property is missing in Column: "
                                    + column.getQuery().getFullName()
                                    + column.getName());
                }
            }

            String columnName = column.getSortingColumn();
            boolean lowerCase = column.isLowerCase();
            SortingColumn sColumn = new SortingColumn(tableName, columnName,
                    ascending, lowerCase);
            if (!columns.contains(sColumn)) columns.add(sColumn);
        } else if (attribute instanceof TextAttributeField) {
            TextAttributeField textAttribute = (TextAttributeField) attribute;
            String text = textAttribute.getText();
            // parse out the children
            Set<String> children = getChildren(text);
            for (String child : children) {
                Set<SortingColumn> subColumns = findColumns(child, ascending);
                for (SortingColumn column : subColumns) {
                    if (!columns.contains(column))
                    // ignore the order of the children, since it should be
                        // the
                        // same as the parent
                        columns.add(column);
                }
            }
        } else if (attribute instanceof LinkAttributeField) {
            LinkAttributeField linkAttribute = (LinkAttributeField) attribute;
            String visible = linkAttribute.getVisible();
            // parse out the children
            Set<String> children = getChildren(visible);
            for (String child : children) {
                Set<SortingColumn> subColumns = findColumns(child, ascending);
                for (SortingColumn column : subColumns) {
                    if (!columns.contains(column))
                    // ignore the order of the children, since it should be
                        // the
                        // same as the parent
                        columns.add(column);
                }
            }
        } else {
            throw new WdkModelException("Uknown attribute type: "
                    + attribute.getName() + " ("
                    + attribute.getClass().getName() + ")");
        }
        return columns;
    }

    private Set<String> getChildren(String text) {
        Set<String> children = new LinkedHashSet<String>();
        Pattern pattern = Pattern.compile("\\$\\$(.+?)\\$\\$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String child = matcher.group(1);
            if (!children.contains(child)) children.add(child);
        }
        return children;
    }

    public String[] getSortingAttributeNames() {
        int size = Math.min(3, sortingAttributes.size());
        String[] attributeNames = new String[size];
        int count = 0;
        for (String attrName : sortingAttributes.keySet()) {
            attributeNames[count++] = attrName;
            if (count >= size) break;
        }
        return attributeNames;
    }

    public boolean[] getSortingAttributeOrders() {
        int size = Math.min(3, sortingAttributes.size());
        boolean[] attributeOrders = new boolean[size];
        int count = 0;
        for (String attrName : sortingAttributes.keySet()) {
            attributeOrders[count++] = sortingAttributes.get(attrName);
            if (count >= size) break;
        }
        return attributeOrders;
    }

    public List<AttributeField> getDisplayableAttributes() {
        List<AttributeField> displayAttributes = new ArrayList<AttributeField>();
        Map<String, AttributeField> attributes = question.getAttributeFields();
        Map<String, AttributeField> summaryAttributes = getSummaryAttributes();
        for (String attriName : attributes.keySet()) {
            AttributeField attribute = attributes.get(attriName);

            // if the sortable property is null, then check if all associated
            // columns are sortable
            if (attribute.sortable == null) {
                Map<String, Boolean> stub = new HashMap<String, Boolean>();
                stub.put(attriName, true);
                try {
                    // all associated columns are sortable if this call doesn't
                    // throw out exception
                    getSortingColumns(stub);
                    attribute.sortable = new Boolean(true);
                    // logger.info( "Attribute " + attriName + " is sortable."
                    // );
                } catch (WdkModelException e) {
                    // the attribute contains unsortable column(s), skip it
                    // logger.info( "Attribute " + attriName + " is not
                    // sortable." );
                    attribute.sortable = new Boolean(false);
                }
            }

            // the sortable attribute cannot be internal
            if (attribute.getInternal()) continue;

            // skip the attributes that are already displayed
            if (summaryAttributes.containsKey(attriName)) continue;

            displayAttributes.add(attribute);
        }
        return displayAttributes;
    }

    public Map<String, AttributeField> getSummaryAttributes() {
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

    public String[] getAllIds() throws WdkModelException {
        List<String> ids = new ArrayList<String>();
        findPrimaryKeyColumnNames();
        ResultList rl = idsQueryInstance.getResult();
        while (rl.next()) {
            String id = rl.getValue(recordIdColumnName).toString();
            ids.add(id);
        }
        rl.close();
        String[] array = new String[ids.size()];
        ids.toArray(array);
        return array;
    }

    public String getCacheTableName() throws WdkModelException {
        return idsQueryInstance.getResultAsTableName();
    }

    public String getResultIndexColumn() {
        return ResultFactory.RESULT_TABLE_I;
    }

    public String getSortingIndexColumn() {
        return ResultFactory.COLUMN_SORTING_INDEX;
    }

    public int getSortingIndex() throws WdkModelException {
        idsQueryInstance.setSortingColumns(getSortingColumns(sortingAttributes));
        return idsQueryInstance.getSortingIndex();
    }

    public boolean hasProjectId() {
        String[] pkColumns = findPrimaryKeyColumnNames();
        return (pkColumns[1] != null);
    }
    
    public Object getSubTypeValue() {
        return idsQueryInstance.getSubTypeValue();
    }

    /**
     * @param expandSubType
     * @see org.gusdb.wdk.model.QueryInstance#setExpandSubType(boolean)
     */
    public void setExpandSubType(boolean expandSubType) {
        idsQueryInstance.setExpandSubType(expandSubType);
    }
}
