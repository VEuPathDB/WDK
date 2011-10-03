/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;

/**
 * @author xingao
 * 
 *         there is a newer version in ApiCommonShared.
 */
@Deprecated
public class FullRecordCachedReporter extends Reporter {

    private static Logger logger = Logger.getLogger(TabularReporter.class);

    public static final String PROPERTY_TABLE_CACHE = "table_cache";
    public static final String PROPERTY_RECORD_ID_COLUMN = "record_id_column";

    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    public static final String FIELD_HAS_EMPTY_TABLE = "hasEmptyTable";

    private String tableCache;
    private String recordIdColumn;

    private boolean hasEmptyTable = false;

    public FullRecordCachedReporter(AnswerValue answerValue, int startIndex,
            int endIndex) {
        super(answerValue, startIndex, endIndex);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.Reporter#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, String> properties)
            throws WdkModelException {
        super.setProperties(properties);

        // check required properties
        tableCache = properties.get(PROPERTY_TABLE_CACHE);
        recordIdColumn = properties.get(PROPERTY_RECORD_ID_COLUMN);

        if (tableCache == null || tableCache.length() == 0)
            throw new WdkModelException("The required property for reporter "
                    + this.getClass().getName() + ", " + PROPERTY_TABLE_CACHE
                    + ", is missing");

        if (recordIdColumn == null || recordIdColumn.length() == 0)
            throw new WdkModelException("The required property for reporter "
                    + this.getClass().getName() + ", "
                    + PROPERTY_RECORD_ID_COLUMN + ", is missing");
    }

    /*
     * 
     */
    @Override
    public void configure(Map<String, String> config) {
        super.configure(config);

        // get basic configurations
        if (config.containsKey(FIELD_HAS_EMPTY_TABLE)) {
            String value = config.get(FIELD_HAS_EMPTY_TABLE);
            hasEmptyTable = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) ? true
                    : false;
        }
    }

    public String getConfigInfo() {
        return "This reporter does not have config info yet.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.Reporter#getHttpContentType()
     */
    @Override
    public String getHttpContentType() {
        if (format.equalsIgnoreCase("text")) {
            return "text/plain";
        } else { // use the default content type defined in the parent class
            return super.getHttpContentType();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.Reporter#getDownloadFileName()
     */
    @Override
    public String getDownloadFileName() {
        logger.info("Internal format: " + format);
        String name = getQuestion().getName();
        if (format.equalsIgnoreCase("text")) {
            return name + "_detail.txt";
        } else { // use the defaul file name defined in the parent
            return super.getDownloadFileName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.report.IReporter#format(org.gusdb.wdk.model.Answer)
     */
    @Override
    protected void write(OutputStream out) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // get the columns that will be in the report
        Set<Field> fields = validateColumns();

        Set<AttributeField> attributes = new LinkedHashSet<AttributeField>();
        Set<TableField> tables = new LinkedHashSet<TableField>();
        for (Field field : fields) {
            if (field instanceof AttributeField) {
                attributes.add((AttributeField) field);
            } else if (field instanceof TableField) {
                tables.add((TableField) field);
            }
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        formatRecord2Text(attributes, tables, writer);
        writer.flush();
    }

    private Set<Field> validateColumns() throws WdkModelException {
        // get a map of report maker fields
        Map<String, Field> fieldMap = getQuestion().getFields(
                FieldScope.REPORT_MAKER);

        // the config map contains a list of column names;
        Set<Field> columns = new LinkedHashSet<Field>();

        String fieldsList = config.get(FIELD_SELECTED_COLUMNS);
        if (fieldsList == null) {
            columns.addAll(fieldMap.values());
        } else {
            String[] fields = fieldsList.split(",");
            for (String column : fields) {
                column = column.trim();
                if (fieldMap.containsKey(column)) {
                	columns.add(fieldMap.get(column));
                }
            }
        }
        return columns;
    }

    private void formatRecord2Text(Set<AttributeField> attributes,
            Set<TableField> tables, PrintWriter writer)
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        logger.debug("Include empty table: " + hasEmptyTable);

        // construct in clause
        StringBuffer sqlIn = new StringBuffer();
        // add a dummy table name to make sure the constructed sql is valid, in
        // case none of the table names are selected.
        sqlIn.append("'WDK_DUMMY_TABLE'");
        for (TableField table : tables) {
            sqlIn.append(", '" + table.getName() + "'");
        }
        RecordClass recordClass = getQuestion().getRecordClass();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // construct the SQL by join cache table with data table
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append("table_name, row_count, content ");
        sql.append("FROM ").append(tableCache);
        for (int index = 0; index < pkColumns.length; index++) {
            sql.append((index == 0) ? " WHERE " : " AND ");
            sql.append(pkColumns[index]).append(" = ?");
        }

        // get the result from database
        DBPlatform platform = getQuestion().getWdkModel().getQueryPlatform();
        PreparedStatement ps = null;
        try {
            ps = SqlUtils.getPreparedStatement(platform.getDataSource(),
                    sql.toString());

            // get page based answers with a maximum size (defined in
            // PageAnswerIterator)
            for (AnswerValue answerValue : this) {
                for (RecordInstance record : answerValue.getRecordInstances()) {
                    // print out attributes of the record first
                    for (AttributeField attribute : attributes) {
                        AttributeValue value = record.getAttributeValue(attribute.getName());
                        writer.println(attribute.getDisplayName() + ": "
                                + value.getValue());
                    }
                    writer.println();
                    writer.flush();

                    // skip he following section if no table field is selected
                    if (tables.size() == 0) continue;

                    // get the cached data of the record
                    Map<String, String> pkValues = record.getPrimaryKey().getValues();
                    long start = System.currentTimeMillis();
                    for (int index = 0; index < pkColumns.length; index++) {
                        Object value = pkValues.get(pkColumns[index]);
                        ps.setObject(index + 1, value);
                    }
                    ResultSet resultSet = ps.executeQuery();
                    SqlUtils.verifyTime(wdkModel, sql.toString(),
                            "wdk-report-full-select-cache", start);
                    Map<String, String> tableValues = new LinkedHashMap<String, String>();
                    while (resultSet.next()) {
                        // check if display empty tables
                        int size = resultSet.getInt("row_count");
                        if (!hasEmptyTable && size == 0) continue;

                        String tableName = resultSet.getString("table_name");
                        String content = platform.getClobData(resultSet,
                                "content");
                        tableValues.put(tableName, content);
                    }
                    resultSet.close();

                    // output the value, preserving the order
                    for (TableField table : tables) {
                        String tableName = table.getName();
                        if (tableValues.containsKey(tableName))
                            writer.println(tableValues.get(tableName));
                        writer.flush();
                    }
                    writer.println();
                    writer.println("------------------------------------------------------------");
                    writer.println();
                    writer.flush();
                }
            }
            writer.flush();
        } finally {
            SqlUtils.closeStatement(ps);
        }
    }

    @Override
    protected void complete() {
    // do nothing
    }

    @Override
    protected void initialize() throws SQLException {
    // do nothing
    }
}
