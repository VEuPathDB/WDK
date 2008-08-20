/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author xingao
 * 
 */
public class TabularReporter extends Reporter {

    private static Logger logger = Logger.getLogger(TabularReporter.class);

    public static final String FIELD_HAS_HEADER = "includeHeader";
    public static final String FIELD_DIVIDER = "divider";
    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";

    private boolean hasHeader = true;
    private String divider = "\t";

    public TabularReporter(Answer answer, int startIndex, int endIndex) {
        super(answer, startIndex, endIndex);
    }

    /*
     * 
     */
    @Override
    public void configure(Map<String, String> config) {
        super.configure(config);

        // get basic configurations
        if (config.containsKey(FIELD_HAS_HEADER)) {
            String value = config.get(FIELD_HAS_HEADER);
            hasHeader = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) ? true
                    : false;
        }

        if (config.containsKey(FIELD_DIVIDER)) {
            divider = config.get(FIELD_DIVIDER);
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
        } else if (format.equalsIgnoreCase("excel")) {
            return "application/vnd.ms-excel";
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
            return name + "_summary.txt";
        } else if (format.equalsIgnoreCase("excel")) {
            return name + "_summary.xls";
        } else if (format.equalsIgnoreCase("pdf")) {
            return name + "_summary.pdf";
        } else { // use the default file name defined in the parent
            return super.getDownloadFileName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.IReporter#format(org.gusdb.wdk.model.Answer)
     */
    public void write(OutputStream out) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

        // get the columns that will be in the report
        Set<AttributeField> columns = validateColumns();

        // get the formatted result
        if (format.equalsIgnoreCase("excel")) {
            format2Excel(columns, writer);
        } else if (format.equalsIgnoreCase("pdf")) {
            format2PDF(columns, out);
        } else {
            format2Text(columns, writer);
        }
    }

    private Set<AttributeField> validateColumns() throws WdkModelException {
        // the config map contains a list of column names;
        Map<String, AttributeField> summary = getSummaryAttributes();
        Set<AttributeField> columns = new LinkedHashSet<AttributeField>();

        String fieldsList = config.get(FIELD_SELECTED_COLUMNS);
        if (fieldsList == null) {
            columns.addAll(summary.values());
        } else {
            Map<String, AttributeField> attributes = getQuestion().getAttributeFields(
                    FieldScope.ReportMaker);
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

    private void format2Text(Set<AttributeField> fields, PrintWriter writer)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // print the header
        if (hasHeader) {
            for (AttributeField field : fields) {
                writer.print("[" + field.getDisplayName() + "]");
                writer.print(divider);
            }
            writer.println();
            writer.flush();
        }

        // get page based answers with a maximum size (defined in
        // PageAnswerIterator)
        for (Answer answer : this) {
            for (RecordInstance record : answer.getRecordInstances()) {
                for (AttributeField field : fields) {
                    AttributeValue value = record.getAttributeValue(field.getName());
                    writer.print(value.getValue());
                    writer.print(divider);
                }
                writer.println();
                writer.flush();
            }
        }
    }

    private void format2PDF(Set<AttributeField> fields, OutputStream out)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        logger.info("format2PDF>>>");
        Document document = new Document(PageSize.LETTER.rotate());
        try {
            PdfWriter pwriter = PdfWriter.getInstance(document, out);
            document.open();

            int NumFields = fields.size();

            PdfPTable datatable = new PdfPTable(NumFields);

            if (hasHeader) {
                for (AttributeField field : fields) {
                    datatable.addCell("" + field.getDisplayName() + "");
                }
            }

            datatable.setHeaderRows(1);
            int count = 0;

            // get page based answers with a maximum size (defined in
            // PageAnswerIterator)
            for (Answer answer : this) {
                for (RecordInstance record : answer.getRecordInstances()) {
                    count++;

                    if (count % 2 == 1) {
                        datatable.getDefaultCell().setGrayFill(0.9f);
                    }

                    for (AttributeField field : fields) {
                        AttributeValue value = record.getAttributeValue(field.getName());
                        datatable.addCell("" + value.getValue());
                    }

                    if (count % 2 == 1) {
                        datatable.getDefaultCell().setGrayFill(1);
                    }

                    if (count % 500 == 0) {
                        pwriter.flush();
                    }
                }
            }

            datatable.setSplitLate(false);
            datatable.setWidthPercentage(100);
            document.setMargins(10, 10, 10, 10);
            document.add(datatable);
            document.close();
            out.flush();
        }

        catch (DocumentException de) {
            throw new WdkModelException(de);
            // de.printStackTrace();
            // System.err.println("document: " + de.getMessage());
        }

        catch (IOException ex) {
            throw new WdkModelException(ex);
        }

        // close the document (the outputstream is also closed internally)

    }

    private void format2Excel(Set<AttributeField> fields, PrintWriter writer)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        writer.println("<table border=\"1\">");

        // print the header
        if (hasHeader) {
            writer.println("<tr>");
            for (AttributeField field : fields) {
                writer.print("<th>[" + field.getDisplayName() + "]</th>");
            }
            writer.println();
            writer.println("</tr>");
            writer.flush();
        }

        // get page based answers with a maximum size (defined in
        // PageAnswerIterator)
        for (Answer answer : this) {
            for (RecordInstance record : answer.getRecordInstances()) {
                writer.println("<tr>");
                for (AttributeField field : fields) {
                    AttributeValue value = record.getAttributeValue(field.getName());
                    writer.print("<td>");
                    writer.print(value.getValue());
                    writer.print("</td>");
                }
                writer.println();
                writer.println("</tr>");
                writer.flush();
            }
        }
        writer.println("</table>");
        writer.flush();
    }
}
