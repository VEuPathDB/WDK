package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONException;
import org.json.JSONObject;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * A reporter that produces a tabular output from an answer.  It takes a pluggable row provider
 * that provides the data.  Typical row providers might merge the answer with a set of record attributes or a single record table.
 * @author steve
 *
 */
public abstract class SingleTableReporter extends StandardReporter {


  private static Logger logger = Logger.getLogger(TabularReporter.class);

  public static final String PROP_INCLUDE_HEADER = "includeHeader";
  public static final String PROP_COLUMN_DIVIDER = "divider";

  public static final long MAX_EXCEL_LENGTH = 1024 * 1024 * 10;

  private boolean showHeader = true;
  private String columnDivider = "\t";

  public SingleTableReporter(AnswerValue answerValue, int startIndex, int endIndex) {
      super(answerValue, startIndex, endIndex);
  }

  /*
   * 
   */
  @Override
  public void configure(Map<String, String> config) {
      super.configure(config);

      // get basic configurations
      if (config.containsKey(PROP_INCLUDE_HEADER)) {
          String value = config.get(PROP_INCLUDE_HEADER);
          showHeader = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) ? true
                  : false;
      }

      if (config.containsKey(PROP_COLUMN_DIVIDER)) {
          columnDivider = config.get(PROP_COLUMN_DIVIDER);
      }
  }

  @Override
  public void configure(JSONObject config) {
    super.configure(config);
    showHeader = (config.has(PROP_INCLUDE_HEADER) ? config.getBoolean(PROP_INCLUDE_HEADER) : true);
  }
  
  @Override
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
      if (reporterConfig.getAttachmentType().equalsIgnoreCase("text")) {
          return "text/plain";
      } else if (reporterConfig.getAttachmentType().equalsIgnoreCase("excel")) {
          return "application/vnd.ms-excel";
      } else if (reporterConfig.getAttachmentType().equalsIgnoreCase("pdf")) {
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
      logger.info("Internal format: " + reporterConfig.getAttachmentType());
      String name = getQuestion().getName();
      if (reporterConfig.getAttachmentType().equalsIgnoreCase("text")) {
          return name + "_summary.txt";
      } else if (reporterConfig.getAttachmentType().equalsIgnoreCase("excel")) {
          return name + "_summary.xls";
      } else if (reporterConfig.getAttachmentType().equalsIgnoreCase("pdf")) {
          return name + "_summary.pdf";
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
  public void write(OutputStream out) throws WdkModelException,
          NoSuchAlgorithmException, SQLException, JSONException,
          WdkUserException {
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

      // get the formatted result
      if (reporterConfig.getAttachmentType().equalsIgnoreCase("excel")) {
          format2Excel(writer);
      } else if (reporterConfig.getAttachmentType().equalsIgnoreCase("pdf")) {
          format2PDF(out);
      } else {
          format2Text(writer);
      }
  }
  
  protected abstract List<String> getHeader();
  
  protected abstract SingleTableReporterRowsProvider getRowsProvider(AnswerValue answerValue);


  private void format2Text(PrintWriter writer)
      throws WdkModelException, WdkUserException {
    // print the header
    if (showHeader) {
      for (String title : getHeader()) {
        writer.print("[" + title + "]");
        writer.print(columnDivider);
      }
      writer.println();
      writer.flush();
    }

    // get page based answers with a maximum size (defined in
    // PageAnswerIterator)
    for (AnswerValue answerValuePage : this) {
      SingleTableReporterRowsProvider rows = getRowsProvider(answerValuePage);
      while (rows.hasNext()) {
        List<Object> row = rows.next();
        for (Object value : row) {

          writer.print((value == null) ? "N/A" : value);
          writer.print(columnDivider);
        }
        writer.println();
        writer.flush();
      }
    }
  }

  private void format2PDF(OutputStream out)
          throws WdkModelException, WdkUserException {
      logger.info("format2PDF>>>");
      Document document = new Document(PageSize.LETTER.rotate());
      try {
          PdfWriter pwriter = PdfWriter.getInstance(document, out);
          document.open();

          int NumFields = getHeader().size();

          PdfPTable datatable = new PdfPTable(NumFields);

          if (showHeader) {
              for (String title : getHeader()) {
                  datatable.addCell("" + title + "");
              }
          }

          datatable.setHeaderRows(1);
          int count = 0;

          // get page based answers with a maximum size (defined in
          // PageAnswerIterator)
          for (AnswerValue answerValuePage : this) {
            SingleTableReporterRowsProvider rows = getRowsProvider(answerValuePage);
            while (rows.hasNext()) {
                List<Object> row = rows.next();
                  count++;

                  if (count % 2 == 1) {
                      datatable.getDefaultCell().setGrayFill(0.9f);
                  }

                  for (Object value : row) {
                      datatable.addCell("" + value);
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

  private void format2Excel(PrintWriter writer)
          throws WdkModelException, WdkUserException {
      int count = 0;
      String header = "<table border=\"1\">";
      writer.println(header);
      count += header.length() + 5;

      // print the header
      if (showHeader) {
          writer.println("<tr>");
          count += 5;
          for (String heading : getHeader()) {
              String title = "<th>[" + heading + "]</th>";
              writer.print(title);
              count += title.length();
          }
          writer.println();
          writer.println("</tr>");
          writer.flush();
          count += 7;
      }

      // get page based answers with a maximum size (defined in
      // PageAnswerIterator)
      for (AnswerValue answerValuePage : this) {
        SingleTableReporterRowsProvider rows = getRowsProvider(answerValuePage);
        while (rows.hasNext()) {
              List<Object> row = rows.next();
              writer.println("<tr>");
              count += 5;
              for (Object value : row) {
                  String val = "<td>" + value + "</td>";
                  writer.print(val);
                  count += val.length();
              }
              writer.println();
              writer.println("</tr>");
              writer.flush();
              count += 7;

              // logger.debug("Excel download - written: " + count);
              // check if the output exceeds the max allowed size
              if (count > MAX_EXCEL_LENGTH) {
                  writer.print("<tr><td colspan=\"" + getHeader().size() + "\">");
                  writer.print("The result size exceeds the maximum allowed "
                          + "size for downloading excel files. The rest of "
                          + "the results are ignored. Opening huge excel "
                          + "files may crash your system. If you need to "
                          + "get the complete results, please choose the "
                          + "download type as Text File, or Show in Browser.");
                  writer.println("</td></tr>");
                  break;
              }
              if (count > MAX_EXCEL_LENGTH) break;
          }
          if (count > MAX_EXCEL_LENGTH) break;
      }
      writer.println("</table>");
      writer.flush();
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
