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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.TableValue;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;

/**
 * @author Cary P.
 * 
 */
public class XMLReporter extends Reporter {

    private static Logger logger = Logger.getLogger(XMLReporter.class);

    private static final String NEW_LINE = System.getProperty("line.separator");

    public static final String PROPERTY_TABLE_CACHE = "table_cache";
    public static final String PROPERTY_RECORD_ID_COLUMN = "record_id_column";

    public static final String FIELD_SELECTED_COLUMNS = "o-fields";
    public static final String TABLE_SELECTED_COLUMNS = "o-tables";
    public static final String FIELD_HAS_EMPTY_TABLE = "hasEmptyTable";

    private String tableCache;
    private String recordIdColumn;

    private boolean hasEmptyTable = false;

    private String sqlInsert;
    private String sqlQuery;

    public XMLReporter(AnswerValue answerValue, int startIndex, int endIndex) {
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

        tableCache = properties.get(PROPERTY_TABLE_CACHE);

        // check required properties
        recordIdColumn = properties.get(PROPERTY_RECORD_ID_COLUMN);
        logger.info(" tableCache:" + tableCache + "recordIdColumn: "
                + recordIdColumn);
        if (tableCache != null && recordIdColumn == null)
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
        } else if (format.equalsIgnoreCase("pdf")) {
            return "application/pdf";
        } else { // use the default content type defined in the parent class
            return "text/xml";// return super.getHttpContentType();
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
            return name + ".xml";
        } else { // use the default file name defined in the parent
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
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

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

        // get the formatted result
        WdkModel wdkModel = getQuestion().getWdkModel();
        DBPlatform platform = wdkModel.getQueryPlatform();

        RecordClass recordClass = getQuestion().getRecordClass();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // construct the insert sql
        StringBuffer sqlInsert = new StringBuffer("INSERT INTO ");
        sqlInsert.append(tableCache).append(" (wdk_table_id, ");
        for (String column : pkColumns) {
            sqlInsert.append(column).append(", ");
        }
        sqlInsert.append(" table_name, row_count, content) VALUES (");
        sqlInsert.append(wdkModel.getUserPlatform().getNextIdSqlExpression(
                "apidb", "wdkTable"));
        sqlInsert.append(", ");
        for (int i = 0; i < pkColumns.length; i++) {
            sqlInsert.append("?, ");
        }
        sqlInsert.append("?, ?, ?)");

        // construct the query sql
        StringBuffer sqlQuery = new StringBuffer("SELECT ");
        sqlQuery.append("count(*) AS cache_count FROM ").append(tableCache);
        sqlQuery.append(" WHERE ");
        for (String column : pkColumns) {
            sqlQuery.append(column).append(" = ? AND ");
        }
        sqlQuery.append(" table_name = ?");

        this.sqlInsert = sqlInsert.toString();
        this.sqlQuery = sqlQuery.toString();
        PreparedStatement psInsert = null;
        PreparedStatement psQuery = null;
        try {
            if (tableCache != null) {
                // want to cache the table content
                DataSource dataSource = platform.getDataSource();
                psInsert = SqlUtils.getPreparedStatement(dataSource,
                        sqlInsert.toString());
                psQuery = SqlUtils.getPreparedStatement(dataSource,
                        sqlQuery.toString());
            }
            int recordCount = 0;
            AnswerValue av = this.getAnswerValue();
            // get page based answers with a maximum size (defined in
            // PageAnswerIterator)
            writer.println("<?xml version='1.0' encoding='UTF-8'?>");
            writer.println("<response>");
            writer.println("<recordset id='" + av.getChecksum() + "' count='"
                    + this.getResultSize() + "' type='"
                    + av.getQuestion().getRecordClass().getType() + "'>");
            for (AnswerValue pageAnswer : this) {
                for (RecordInstance record : pageAnswer.getRecordInstances()) {
                    writer.println("<record id='" + record.getPrimaryKey()
                            + "'>");
                    // print out attributes of the record first
                    formatAttributes(record, attributes, writer);

                    // print out tables
                    formatTables(record, tables, writer, pageAnswer, psInsert,
                            psQuery);
                    writer.flush();

                    // count the records processed so far
                    recordCount++;
                    writer.println("</record>");
                }
            }
            writer.println("</recordset>");
            writer.println("</response>");
            writer.flush();
            logger.info("Totally " + recordCount + " records dumped");
        }
        finally {
            SqlUtils.closeStatement(psQuery);
            SqlUtils.closeStatement(psInsert);
        }
    }

    private Set<Field> validateColumns() throws WdkModelException {
        // get a map of report maker fields
        Map<String, Field> fieldMap = getQuestion().getFields(
                FieldScope.REPORT_MAKER);

        // the config map contains a list of column names;
        Set<Field> columns = new LinkedHashSet<Field>();

        String fieldsList = config.get(FIELD_SELECTED_COLUMNS);
        String tablesList = config.get(TABLE_SELECTED_COLUMNS);
        if (fieldsList == null) fieldsList = "none";
        if (tablesList == null) tablesList = "none";
        logger.info("fieldsList = " + fieldsList + "    tablesList = "
                + tablesList);
        if (fieldsList.equals("all") && tablesList.equals("all")) {
            columns.addAll(fieldMap.values());
        } else {
            if (fieldsList.equals("all")) {
                logger.info("FIELDSLIST ALL");
                for (String k : fieldMap.keySet()) {
                    Field f = fieldMap.get(k);
                    if (f.getClass().getName().contains("AttributeField"))
                        columns.add(f);
                }
            } else if (!fieldsList.equals("none")) {
                String[] fields = fieldsList.split(",");
                for (String column : fields) {
                    column = column.trim();
                    if (fieldMap.containsKey(column)) {
                        columns.add(fieldMap.get(column));
                    }
                }
            }
            if (tablesList.equals("all")) {
                for (String k : fieldMap.keySet()) {
                    Field f = fieldMap.get(k);
                    if (f.getClass().getName().contains("TableField"))
                        columns.add(f);
                }
            } else if (!tablesList.equals("none")) {
                String[] tables = tablesList.split(",");
                for (String column : tables) {
                    column = column.trim();
                    if (!fieldMap.containsKey(column))
                        throw new WdkModelException("The column '" + column
                                + "' cannot be included in the report");
                    columns.add(fieldMap.get(column));
                }
            }
        }
        return columns;
    }

    private void formatAttributes(RecordInstance record,
            Set<AttributeField> attributes, PrintWriter writer)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // print out attributes of the record first
        for (AttributeField field : attributes) {
            AttributeValue value = record.getAttributeValue(field.getName());
            writer.println("<field name='" + field.getName() + "' title='"
                    + field.getDisplayName() + "'><![CDATA[" + value
                    + "]]></field>");
        }
        // print out attributes of the record first
        writer.println();
        writer.flush();
    }

    private void formatTables(RecordInstance record, Set<TableField> tables,
            PrintWriter writer, AnswerValue answerValue,
            PreparedStatement psInsert, PreparedStatement psQuery)
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        DBPlatform platform = getQuestion().getWdkModel().getQueryPlatform();
        RecordClass recordClass = record.getRecordClass();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // print out tables of the record
        boolean needUpdate = false;
        for (TableField table : tables) {
            TableValue tableValue = record.getTableValue(table.getName());

            AttributeField[] fields = table.getAttributeFields(FieldScope.REPORT_MAKER);

            // output table header
            StringBuffer sb = new StringBuffer();
            sb.append("<table name='" + table.getDisplayName() + "'>"
                    + NEW_LINE);
            int tableSize = 0;
            for (Map<String, AttributeValue> row : tableValue) {
                tableSize++;
                sb.append("<row>" + NEW_LINE);
                for (AttributeField field : fields) {
                    String fieldName = field.getName();
                    AttributeValue value = row.get(fieldName);
                    sb.append("<field name='" + fieldName + "'><![CDATA["
                            + value.getValue() + "]]></field>" + NEW_LINE);
                }
                sb.append("</row>" + NEW_LINE);
            }
            sb.append("</table>" + NEW_LINE);
            String content = sb.toString();
            // check if the record has been cached
            if (tableCache != null) {
                Map<String, String> pkValues = record.getPrimaryKey().getValues();
                long start = System.currentTimeMillis();
                for (int index = 1; index <= pkColumns.length; index++) {
                    Object value = pkValues.get(pkColumns[index - 1]);
                    psQuery.setObject(index, value);
                }
                psQuery.setString(pkColumns.length + 1, table.getName());
                ResultSet rs = psQuery.executeQuery();
                SqlUtils.verifyTime(wdkModel, sqlQuery,
                        "wdk-report-xml-select-count", start);
                rs.next();
                int count = rs.getInt("cache_count");
                if (count == 0) {
                    // insert into table cache
                    int index;
                    for (index = 1; index <= pkColumns.length; index++) {
                        Object value = pkValues.get(pkColumns[index - 1]);
                        psInsert.setObject(index, value);
                    }
                    psInsert.setString(index++, table.getName());
                    psInsert.setInt(index++, tableSize);
                    platform.setClobData(psInsert, index++, content, false);
                    psInsert.addBatch();
                    needUpdate = true;
                }
                rs.close();
            }

            // write to the stream
            if (hasEmptyTable || tableSize > 0) {
                writer.println(content);
                writer.flush();
            }
        }
        if (tableCache != null && needUpdate) {
            long start = System.currentTimeMillis();
            psInsert.executeBatch();
            SqlUtils.verifyTime(wdkModel, sqlInsert, "wdk-report-xml-insert",
                    start);
        }
    }

    @Override
    protected void complete() {
        // do nothing
    }

    @Override
    protected void initialize() throws WdkModelException {
        // do nothing
    }
}
