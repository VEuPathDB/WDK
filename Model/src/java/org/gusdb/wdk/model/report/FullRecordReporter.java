/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeFieldValue;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.LinkValue;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.TableFieldValue;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.implementation.SqlUtils;

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

    private boolean hasEmptyTable = true;

    public FullRecordReporter(Answer answer) {
        super(answer);
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
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

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

        // get the formatted result
        WdkModel wdkModel = answer.getQuestion().getWdkModel();
        RDBMSPlatformI platform = wdkModel.getPlatform();

        // check if we need to use project id
        boolean hasProjectId = answer.hasProjectId();

        PreparedStatement psCache = null;
        PreparedStatement psCheck = null;
        try {
            if (tableCache != null) {
                // want to cache the table content
                DataSource dataSource = platform.getDataSource();
                psCache = SqlUtils.getPreparedStatement(dataSource, "INSERT "
                        + "INTO " + tableCache + " (" + recordIdColumn
                        + ", table_name, row_count, content"
                        + (hasProjectId ? ", project_id)" : ")")
                        + " VALUES (?, ?, ?, ?" + (hasProjectId ? ", ?)" : ")"));
                psCheck = SqlUtils.getPreparedStatement(dataSource, "SELECT "
                        + "count(*) AS cache_count FROM " + tableCache
                        + " WHERE " + recordIdColumn + " = ? "
                        + (hasProjectId ? " AND project_id = ?" : ""));
            }
            while (answer.hasMoreRecordInstances()) {
                RecordInstance record = answer.getNextRecordInstance();

                // print out attributes of the record first
                formatAttributes(record, attributes, writer);

                // check if the record has been cached
                psCheck.setString(1, record.getPrimaryKey().getRecordId());
                if (hasProjectId) {
                    String projectId = record.getPrimaryKey().getProjectId();
                    psCheck.setString(2, projectId);
                }
                boolean hasCached = false;
                ResultSet rs = psCheck.executeQuery();
                try {
                    rs.next();
                    int count = rs.getInt("cache_count");
                    if (count > 0) hasCached = true;
                } finally {
                    rs.close();
                }

                // print out tables
                formatTables(record, tables, writer, answer, psCache, hasCached);

                writer.println();
                writer.println("------------------------------------------------------------");
                writer.println();
                writer.flush();
            }
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psCheck);
                SqlUtils.closeStatement(psCache);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
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

    private void formatAttributes(RecordInstance record,
            Set<AttributeField> attributes, PrintWriter writer)
            throws WdkModelException {
        // print out attributes of the record first
        for (AttributeField attribute : attributes) {
            Object value = record.getAttributeValue(attribute);
            writer.println(attribute.getDisplayName() + ": " + value);
        }
        // print out attributes of the record first
        writer.println();
        writer.flush();
    }

    private void formatTables(RecordInstance record, Set<TableField> tables,
            PrintWriter writer, Answer answer, PreparedStatement psCache,
            boolean hasCached) throws WdkModelException, SQLException {
        RDBMSPlatformI platform = answer.getQuestion().getWdkModel().getPlatform();
        boolean hasProjectId = answer.hasProjectId();

        // print out tables of the record
        for (TableField table : tables) {
            TableFieldValue tableValue = record.getTableValue(table.getName());
            Iterator rows = tableValue.getRows();

            AttributeField[] fields = table.getReportMakerFields();

            // output table header
            StringBuffer sb = new StringBuffer();
            sb.append("Table: " + table.getDisplayName() + NEW_LINE);
            for (AttributeField attribute : fields) {
                sb.append("[" + attribute.getDisplayName() + "]\t");
            }
            sb.append(NEW_LINE);

            int tableSize = 0;
            while (rows.hasNext()) {
                tableSize++;
                Map rowMap = (Map) rows.next();
                Iterator colNames = rowMap.keySet().iterator();
                while (colNames.hasNext()) {
                    String colName = (String) colNames.next();
                    Object fVal = rowMap.get(colName);
                    // depending on the types of the object, print out
                    // the value of it
                    if (fVal == null) {
                        fVal = "";
                    } else if (fVal instanceof AttributeFieldValue) {
                        fVal = ((AttributeFieldValue) fVal).getValue();
                    } else if (fVal instanceof LinkValue) {
                        fVal = ((LinkValue) fVal).getVisible();
                    }
                    sb.append(fVal.toString() + "\t");
                }
                sb.append(NEW_LINE);
            }
            tableValue.getClose();
            String content = sb.toString();

            if (tableCache != null && !hasCached) {
                // save into table cache
                String recordId = record.getPrimaryKey().getRecordId();
                psCache.setString(1, recordId);
                psCache.setString(2, table.getName());
                psCache.setInt(3, tableSize);
                platform.updateClobData(psCache, 4, content, false);
                if (hasProjectId) {
                    String projectId = record.getPrimaryKey().getProjectId();
                    psCache.setString(5, projectId);
                }
                psCache.addBatch();
            }

            // write to the stream
            if (hasEmptyTable || tableSize > 0) {
                writer.println(content);
                writer.flush();
            }
        }
        if (tableCache != null && !hasCached) psCache.executeBatch();
    }
}
