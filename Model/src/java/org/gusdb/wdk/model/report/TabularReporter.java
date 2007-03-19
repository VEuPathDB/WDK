/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.LinkValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public class TabularReporter extends Reporter {

    public static final String FIELD_HAS_HEADER = "includeHeader";
    public static final String FIELD_DIVIDER = "divider";
    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    private boolean hasHeader = true;
    private String divider = "\t";

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.IReporter#format(org.gusdb.wdk.model.Answer)
     */
    public String format(Answer answer) throws WdkModelException {
        // get basic configurations
        if (config.containsKey(FIELD_HAS_HEADER)) {
            String value = config.get(FIELD_HAS_HEADER);
            hasHeader = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"))
                    ? true
                    : false;
        }

        if (config.containsKey(FIELD_DIVIDER)) {
            divider = config.get(FIELD_DIVIDER);
        }

        // get the columns that will be in the report
        Set<AttributeField> columns = validateColumns(answer);

        // get the formatted result
        return formatResult(columns, answer);
    }

    private Set<AttributeField> validateColumns(Answer answer)
            throws WdkModelException {
        // the config map contains a list of column names;
        Map<String, AttributeField> summary = answer.getSummaryAttributes();
        Set<AttributeField> columns = new LinkedHashSet<AttributeField>();

        String fieldsList = config.get(FIELD_SELECTED_COLUMNS);
        if (fieldsList == null) {
            columns.addAll(summary.values());
        } else {
            Map<String, AttributeField> attributes = answer.getQuestion().getReportMakerAttributeFields();
            String[] fields = fieldsList.split(",");
            for (String column : fields) {
                column = column.trim();
                if (column.equalsIgnoreCase("default")) {
                    columns.clear();
                    columns.addAll(summary.values());
                    break;
                }
                if (!attributes.containsKey(column))
                    throw new WdkModelException("The column '" + column
                            + "' cannot included in the report");
                columns.add(attributes.get(column));
            }
        }
        return columns;
    }

    private String formatResult(Set<AttributeField> columns, Answer answer)
            throws WdkModelException {
        StringBuffer result = new StringBuffer();
        String newLine = System.getProperty("line.separator");

        // print the header
        if (hasHeader) {
            result.append("#");
            for (AttributeField column : columns) {
                result.append(column.getDisplayName());
                result.append(divider);
            }
            result.append(newLine);
        }

        while (answer.hasMoreRecordInstances()) {
            RecordInstance record = answer.getNextRecordInstance();
            for (AttributeField column : columns) {
                Object value = record.getAttributeValue(column);
                if (value instanceof LinkValue) {
                    result.append(((LinkValue) value).getValue());
                } else {
                    result.append(value);
                }
                result.append(divider);
            }
            result.append(newLine);
        }
        return result.toString();
    }
}
