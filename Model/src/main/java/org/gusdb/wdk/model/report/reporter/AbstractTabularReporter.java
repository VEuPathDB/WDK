package org.gusdb.wdk.model.report.reporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.FileBasedRecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.CsvResultList;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONObject;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * A reporter that produces a tabular output from an answer. It takes a pluggable row provider that provides
 * the data. Typical row providers might merge the answer with a set of record attributes or a single record
 * table.
 * 
 * @author steve
 */
public abstract class AbstractTabularReporter extends StandardReporter {

  @SuppressWarnings("unused")
  private static Logger LOG = Logger.getLogger(AbstractTabularReporter.class);

  public static final String FIELD_HAS_HEADER = "includeHeader";
  public static final String FIELD_DIVIDER = "divider";

  protected static final long MAX_EXCEL_LENGTH = 1024 * 1024 * 10;

  protected boolean _includeHeader = true;
  protected String _divider = "\t";

  public static interface RowsProvider extends Iterable<List<Object>> { }

  protected abstract List<String> getHeader() throws WdkUserException, WdkModelException;

  protected abstract RowsProvider getRowsProvider(RecordInstance record)
      throws WdkUserException, WdkModelException;

  protected AbstractTabularReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public AbstractTabularReporter configure(Map<String, String> config) throws ReporterConfigException {
    super.configure(config);

    // get basic configurations
    if (config.containsKey(FIELD_HAS_HEADER)) {
      String value = config.get(FIELD_HAS_HEADER);
      _includeHeader = (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true"));
    }

    if (config.containsKey(FIELD_DIVIDER)) {
      _divider = config.get(FIELD_DIVIDER);
    }

    return this;
  }

  @Override
  public AbstractTabularReporter configure(JSONObject config) throws ReporterConfigException {
    super.configure(config);
    _includeHeader = (config.has(FIELD_HAS_HEADER) ? config.getBoolean(FIELD_HAS_HEADER) : true);
    return this;
  }

  @Override
  public String getHttpContentType() {
    switch (getStandardConfig().getAttachmentType()) {
      case "text":
        return "text/plain";
      case "excel":
        return "application/vnd.ms-excel";
      case "pdf":
        return "application/pdf";
      case "csv":
        return "text/csv";
      default:
        return super.getHttpContentType();
    }
  }

  @Override
  public String getDownloadFileName() {
    return getDownloadFileName(getQuestion().getName());
  }

  protected String getDownloadFileName(String baseName) {
    String suffix = getFileNameSuffix();
    switch (getStandardConfig().getAttachmentType()) {
      case "text":
        return baseName + "_" + suffix + ".txt";
      case "excel":
        return baseName + "_" + suffix + ".xls";
      case "pdf":
        return baseName + "_" + suffix + ".pdf";
      case "csv":
        return baseName + "_" + suffix + ".csv";
      default:
        return super.getDownloadFileName();
    }
  }

  protected String getFileNameSuffix() {
    return "Summary";
  }

  @Override
  public void write(OutputStream out) throws WdkModelException {
    try {
      // get the formatted result
      switch (getStandardConfig().getAttachmentType()) {
        case "text":
        case "plain": // text, but shown in browser
          format2Text(out); break;
        case "excel":
          format2Excel(out); break;
        case "pdf":
          format2PDF(out); break;
        case "csv":
          format2CSV(out); break;
        default:
          throw new WdkUserException("Illegal download type: " + getStandardConfig().getAttachmentType());
      }
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to write tabular report", e);
    }
  }

  private void format2CSV(OutputStream out) throws WdkModelException, WdkUserException {
    try (CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(out),
        FileBasedRecordStream.BUFFER_SIZE), CsvResultList.COMMA, CsvResultList.QUOTE, CsvResultList.ESCAPE)) {

      List<String> colNames = getHeader();
      if (_includeHeader) {
        writer.writeNext(colNames.toArray(new String[colNames.size()]));
      }
      try (RecordStream records = getRecords()) {
        for (RecordInstance record : records) {
          for (List<Object> row : getRowsProvider(record)) {
            writer.writeNext(Functions.mapToList(row, obj -> String.valueOf(obj)).toArray(new String[colNames.size()]));
          }
        }
      }
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to completely write CSV report", e);
    }
  }

  protected void format2Text(OutputStream out) throws WdkModelException, WdkUserException {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
    // print the header
    if (_includeHeader) {
      for (String title : getHeader()) {
        writer.print("[" + title + "]");
        writer.print(_divider);
      }
      writer.println();
      writer.flush();
    }

    try (RecordStream records = getRecords()) {
      for (RecordInstance record : records) {
        for (List<Object> row : getRowsProvider(record)) {
          for (Object value : row) {
            writer.print((value == null) ? "N/A" : value);
            writer.print(_divider);
          }
          writer.println();
          writer.flush();
        }
      }
    }
  }

  protected void format2PDF(OutputStream out) throws WdkModelException, WdkUserException {

    Document document = new Document(PageSize.LETTER.rotate());
    try {
      PdfWriter pwriter = PdfWriter.getInstance(document, out);
      document.open();

      int NumFields = getHeader().size();

      PdfPTable datatable = new PdfPTable(NumFields);

      if (_includeHeader) {
        for (String title : getHeader()) {
          datatable.addCell("" + title + "");
        }
      }

      datatable.setHeaderRows(1);
      int count = 0;

      try (RecordStream records = getRecords()) {
        for (RecordInstance record : records) {
          for (List<Object> row : getRowsProvider(record)) {
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
      }

      datatable.setSplitLate(false);
      datatable.setWidthPercentage(100);
      document.setMargins(10, 10, 10, 10);
      document.add(datatable);
      // close the document (the outputstream closed by calling code)
      document.close();
      out.flush();
    }

    catch (DocumentException | IOException ex) {
      throw new WdkModelException("Unable to write PDF tabular report", ex);
    }

  }

  protected void format2Excel(OutputStream out) throws WdkModelException, WdkUserException {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
    int count = 0;
    String header = "<table border=\"1\">";
    writer.println(header);
    count += header.length() + 5;

    // print the header
    if (_includeHeader) {
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

    try (RecordStream records = getRecords()) {
      for (RecordInstance record : records) {
        for (List<Object> row : getRowsProvider(record)) {
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
            writer.print("The result size exceeds the maximum allowed " +
                "size for downloading excel files. The rest of " +
                "the results are ignored. Opening huge excel " +
                "files may crash your system. If you need to " +
                "get the complete results, please choose the " +
                "download type as Text File, or Show in Browser.");
            writer.println("</td></tr>");
            break;
          }
          if (count > MAX_EXCEL_LENGTH)
            break;
        }
        if (count > MAX_EXCEL_LENGTH)
          break;
      }
    }
    writer.println("</table>");
    writer.flush();
  }
}
