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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.json.JSONException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author xingao
 * 
 */
public class FullRecordReporter extends Reporter {

    private static Logger logger = Logger.getLogger(TabularReporter.class);

    private static final String NEW_LINE = System.getProperty("line.separator");

    public static final String PROPERTY_TABLE_CACHE = "table_cache";
    public static final String PROPERTY_RECORD_ID_COLUMN = "record_id_column";

    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    public static final String FIELD_HAS_EMPTY_TABLE = "hasEmptyTable";

    private String tableCache;
    private String recordIdColumn;

    private boolean hasEmptyTable = false;

    private String sqlInsert;
    private String sqlQuery;

    public FullRecordReporter(AnswerValue answerValue, int startIndex,
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
        } else if (format.equalsIgnoreCase("pdf")) {
            return name + "_detail.pdf";
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

        if (format.equalsIgnoreCase("pdf")) {
            formatRecord2PDF(attributes, tables, out);
            return;

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

            // get page based answers with a maximum size (defined in
            // PageAnswerIterator)
            for (AnswerValue pageAnswer : this) {
                for (RecordInstance record : pageAnswer.getRecordInstances()) {
                    // print out attributes of the record first
                    formatAttributes(record, attributes, writer);

                    // print out tables
                    formatTables(record, tables, writer, pageAnswer, psInsert,
                            psQuery);

                    writer.println();
                    writer.println("------------------------------------------------------------");
                    writer.println();
                    writer.flush();

                    // count the records processed so far
                    recordCount++;
                    if (recordCount % 100 == 0) {
                        logger.info(recordCount + " records dumped so far");
                    }
                }
            }
            logger.info("Totally " + recordCount + " records dumped");
        } finally {
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

    private void formatAttributes(RecordInstance record,
            Set<AttributeField> attributes, PrintWriter writer)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // print out attributes of the record first
        for (AttributeField field : attributes) {
            AttributeValue value = record.getAttributeValue(field.getName());
            writer.println(field.getDisplayName() + ": " + value);
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
            sb.append("Table: " + table.getDisplayName() + NEW_LINE);
            for (AttributeField attribute : fields) {
                sb.append("[").append(attribute.getDisplayName()).append("]\t");
            }
            sb.append(NEW_LINE);

            int tableSize = 0;
            for (Map<String, AttributeValue> row : tableValue) {
                tableSize++;
                for (AttributeField field : fields) {
                    AttributeValue value = row.get(field.getName());
                    sb.append(value.getValue()).append("\t");
                }
                sb.append(NEW_LINE);
            }
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
                        "wdk-report-full-select-count", start);
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
            SqlUtils.verifyTime(wdkModel, sqlInsert, "wdk-report-full-insert",
                    start);
        }
    }

    private void formatRecord2PDF(Set<AttributeField> attributes,
            Set<TableField> tables, OutputStream out) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {

        logger.info("format2PDF>>>");
        Document document = new Document(PageSize.LETTER.rotate());

        try {
            // PdfWriter pwriter =
            PdfWriter.getInstance(document, out);
            document.open();

            // get page based answers with a maximum size (defined in
            // PageAnswerIterator)
            for (AnswerValue answerValue : this) {
                for (RecordInstance record : answerValue.getRecordInstances()) {
                    // print out attributes of the record first
                    for (AttributeField field : attributes) {
                        AttributeValue value = record.getAttributeValue(field.getName());
                        document.add(new Paragraph(field.getDisplayName()
                                + ": " + value));
                    }

                    // print out tables of the record
                    for (TableField table : tables) {
                        TableValue tableValue = record.getTableValue(table.getName());

                        // check if table is empty
                        Iterator<Map<String, AttributeValue>> iterator = tableValue.iterator();
                        if (!hasEmptyTable && !iterator.hasNext()) {
                            continue;
                        }

                        AttributeField[] fields = table.getAttributeFields(FieldScope.REPORT_MAKER);

                        // output table header
                        document.add(new Paragraph("Table: "
                                + table.getDisplayName()));
                        int NumColumns = fields.length;
                        PdfPTable datatable = new PdfPTable(NumColumns);
                        for (AttributeField attribute : fields) {
                            datatable.addCell("" + attribute.getDisplayName()
                                    + "");
                        }

                        datatable.setHeaderRows(1);

                        while (iterator.hasNext()) {
                            Map<String, AttributeValue> row = iterator.next();
                            for (String fieldName : row.keySet()) {
                                AttributeValue value = row.get(fieldName);
                                Object objValue = value.getValue();
                                if (objValue == null) objValue = "";
                                datatable.addCell(objValue.toString());
                            }
                        }
                        document.add(datatable);
                    }

                    // out.flush();
                }
            }
            document.close();
        } catch (DocumentException de) {
            throw new WdkModelException(de);
        }
        // catch ( IOException ex ) {
        // throw new WdkModelException( ex );
        // }

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
