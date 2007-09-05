/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class FullRecordCachedReporter extends Reporter {

    private static Logger logger = Logger.getLogger(TabularReporter.class);

    public static final String PROPERTY_TABLE_CACHE = "table_cache";
    public static final String PROPERTY_RECORD_ID_COLUMN = "record_id_column";

    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    public static final String FIELD_HAS_EMPTY_TABLE = "hasEmptyTable";

    private String tableCache;
    private String recordIdColumn;
    
    private boolean hasEmptyTable = true;

    public FullRecordCachedReporter(Answer answer) {
        super(answer);
    }

    /**
     * (non-Javadoc)
     * @see org.gusdb.wdk.model.report.Reporter#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, String> properties) throws WdkModelException {
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
        String name = answer.getQuestion().getName();
        if (format.equalsIgnoreCase("text")) {
            return name + "_detail.txt";
        } else { // use the defaul file name defined in the parent
            return super.getDownloadFileName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.IReporter#format(org.gusdb.wdk.model.Answer)
     */
    public void write(OutputStream out) throws WdkModelException {
        // get the columns that will be in the report
        Set<Field> fields = validateColumns(answer);

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
        formatRecord2Text(attributes, tables, answer, writer);
        writer.flush();
    }

    private Set<Field> validateColumns(Answer answer) throws WdkModelException {
        // get a map of report maker fields
        Map<String, Field> fieldMap = answer.getQuestion().getReportMakerFields();

        // the config map contains a list of column names;
        Set<Field> columns = new LinkedHashSet<Field>();

        String fieldsList = config.get(FIELD_SELECTED_COLUMNS);
        if (fieldsList == null) {
            columns.addAll(fieldMap.values());
        } else {
            String[] fields = fieldsList.split(",");
            for (String column : fields) {
                column = column.trim();
                if (!fieldMap.containsKey(column))
                    throw new WdkModelException("The column '" + column
                            + "' cannot be included in the report");
                columns.add(fieldMap.get(column));
            }
        }
        return columns;
    }

    private void formatRecord2Text(Set<AttributeField> attributes,
            Set<TableField> tables, Answer answer, PrintWriter writer)
            throws WdkModelException {
        // construct in clause
        StringBuffer sqlIn = new StringBuffer();
        
        // add a dummy table name to make sure the constructed sql is valid, in 
        // case none of the table names are selected.
        sqlIn.append("'WDK_DUMMY_TABLE'");
        for (TableField table : tables) {
            sqlIn.append(", '" + table.getName() + "'");
        }

        // construct the SQL to retrieve table cache
        String answerCache = answer.getCacheTableName();
        String indexColumn = answer.getResultIndexColumn();
        String sortingColumn = answer.getSortingIndexColumn();
        int sortingIndex = answer.getSortingIndex();
        boolean hasProjectId = answer.hasProjectId();

        StringBuffer sql = new StringBuffer("SELECT ");
        if (hasProjectId) sql.append(" ac.project_id, ");
        sql.append("ac." + recordIdColumn);
        sql.append(", tc.table_name");
        sql.append(", tc.row_count");
        sql.append(", tc.content");
        sql.append(" FROM " + answerCache + " ac");
        sql.append(", " + tableCache + " tc");
        sql.append(" WHERE tc." + recordIdColumn + " = ac." + recordIdColumn);
        sql.append(" AND tc.table_name IN (" + sqlIn.toString() + ")");
        sql.append(" AND ac." + sortingColumn + " = " + sortingIndex);
        if (hasProjectId) sql.append(" AND tc.project_id = ac.project_id");
        if (!hasEmptyTable) sql.append(" AND tc.row_count > 0 ");
        sql.append(" ORDER BY ac." + indexColumn);

        // get the result from database
        RDBMSPlatformI platform = answer.getQuestion().getWdkModel().getPlatform();
        ResultSet rsTable = null;
        try {
            rsTable = SqlUtils.getResultSet(platform.getDataSource(),
                    sql.toString());

            boolean advanced = false;
            while (answer.hasMoreRecordInstances()) {
                RecordInstance record = answer.getNextRecordInstance();
                String recordId = record.getPrimaryKey().getRecordId();
                String projectId = record.getPrimaryKey().getProjectId();

                // print out attributes of the record first
                for (AttributeField attribute : attributes) {
                    Object value = record.getAttributeValue(attribute);
                    writer.println(attribute.getDisplayName() + ": " + value);
                }
                writer.println();
                writer.flush();

                // print out cached table values of the record
                if (!advanced) {
                    if (!rsTable.next()) break; // no more records, break;
                    advanced = true;
                }
                // read the content, and put into a map
                Map<String, String> tableValues = new LinkedHashMap<String, String>();
                do {
                    // it's another record
                    if (!recordId.equals(rsTable.getString(recordIdColumn)))
                        break;
                    if (hasProjectId
                            && !projectId.equals(rsTable.getString("project_id")))
                        break;

                    // check if the table is empty
                    int tableSize = rsTable.getInt("row_count");
                    if (!hasEmptyTable && tableSize == 0) continue;

                    String tableName = rsTable.getString("table_name");
                    String content = platform.getClobData(rsTable, "content");
                    tableValues.put(tableName, content);
                } while (rsTable.next());

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
            writer.flush();
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsTable);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
    }
}
