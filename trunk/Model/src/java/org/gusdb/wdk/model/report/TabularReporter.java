/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.LinkValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Cell;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

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

    public TabularReporter(Answer answer) {
        super(answer);
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
        } else if ( format.equalsIgnoreCase( "pdf" ) ) {
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
        String name = answer.getQuestion().getName();
        if (format.equalsIgnoreCase("text")) {
            return name + "_summary.txt";
        } else if (format.equalsIgnoreCase("excel")) {
            return name + "_summary.xls";
        } else if ( format.equalsIgnoreCase( "pdf" ) ) {
            return name + "_summary.pdf";
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
        Set<AttributeField> columns = validateColumns(answer);

        // get the formatted result
        if (format.equalsIgnoreCase("excel")) {
            format2Excel(columns, answer, writer);
        } else if ( format.equalsIgnoreCase( "pdf" ) ) {
            format2PDF( columns, answer, out );
        } else {
            format2Text(columns, answer, writer);
        }
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

    private void format2Text(Set<AttributeField> columns, Answer answer,
            PrintWriter writer) throws WdkModelException {
        // print the header
        if (hasHeader) {
            for (AttributeField column : columns) {
                writer.print("[" + column.getDisplayName() + "]");
                writer.print(divider);
            }
            writer.println();
            writer.flush();
        }

        while (answer.hasMoreRecordInstances()) {
            RecordInstance record = answer.getNextRecordInstance();
            for (AttributeField column : columns) {
                Object value = record.getAttributeValue(column);
                if (value instanceof LinkValue) {
                    writer.print(((LinkValue) value).getValue());
                } else {
                    writer.print(value);
                }
                writer.print(divider);
            }
            writer.println();
            writer.flush();
        }
    }
    
  private void format2PDF( Set< AttributeField > columns, Answer answer,
            OutputStream out ) throws WdkModelException {
      logger.info( "format2PDF>>>");
      Document document = new Document(PageSize.A4.rotate());
      try {
	  PdfWriter pwriter = PdfWriter.getInstance(document, out);
	  document.open();

	int NumColumns = columns.size();

	PdfPTable datatable = new PdfPTable(NumColumns);

	if ( hasHeader ) {
            for ( AttributeField column : columns ) {
                datatable.addCell("" + column.getDisplayName() + "");
            }
        }
        
	datatable.setHeaderRows(1);
	int count = 0;
	while ( answer.hasMoreRecordInstances() ) {
            RecordInstance record = answer.getNextRecordInstance();

	    count++;
	
	    if(count % 2 == 1){
		datatable.getDefaultCell().setGrayFill(0.9f);
	    }

            for ( AttributeField column : columns ) {
                Object value = record.getAttributeValue( column );
                if ( value instanceof LinkValue ) {
                    datatable.addCell( ( ( LinkValue ) value ).getValue() );
                } else {
		    //PdfPCell cell = new PdfPCell(new Paragraph(""+value) );
		    datatable.addCell(""+value);

                }
            }
	    
	    if(count % 2 == 1){
		datatable.getDefaultCell().setGrayFill(1); 
	    }

	    if(count % 500 == 0){
		pwriter.flush();
	    }
        }

	datatable.setSplitLate(false); 
	datatable.setWidthPercentage(100);
	document.setMargins(10,10,10,10);
	document.add(datatable);
	document.close();
	out.flush();
      }

      catch(DocumentException de) {
	  throw new WdkModelException( de );
	  //            de.printStackTrace();
	  //            System.err.println("document: " + de.getMessage());
      }

      catch ( IOException ex ) {
            throw new WdkModelException( ex );
        }

      // close the document (the outputstream is also closed internally)
   
  }

     private void format2Excel(Set<AttributeField> columns, Answer answer,
            PrintWriter writer) throws WdkModelException {
        writer.println("<table border=\"1\">");

        // print the header
        if (hasHeader) {
            writer.println("<tr>");
            for (AttributeField column : columns) {
                writer.print("<th>[" + column.getDisplayName() + "]</th>");
            }
            writer.println();
            writer.println("</tr>");
            writer.flush();
        }

        while (answer.hasMoreRecordInstances()) {
            RecordInstance record = answer.getNextRecordInstance();
            writer.println("<tr>");
            for (AttributeField column : columns) {
                Object value = record.getAttributeValue(column);
                writer.print("<td>");
                if (value instanceof LinkValue) {
                    writer.print(((LinkValue) value).getValue());
                } else {
                    writer.print(value);
                }
                writer.print("</td>");
            }
            writer.println();
            writer.println("</tr>");
            writer.flush();
        }
        writer.println("</table>");
        writer.flush();
    }
}
