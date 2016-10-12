package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.RecordInstance;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * A reporter that produces a tabular output from an answer. It takes a pluggable row provider that provides
 * the data. Typical row providers might merge the answer with a set of record attributes or a single record
 * table.
 * 
 * @author steve
 */
public abstract class AbstractTabularReporter extends BaseTabularReporter {

  private static Logger LOG = Logger.getLogger(AbstractTabularReporter.class);

  public static interface RowsProvider extends Iterable<List<Object>> { }

  protected abstract List<String> getHeader() throws WdkUserException, WdkModelException;

  protected abstract RowsProvider getRowsProvider(RecordInstance record)
      throws WdkUserException, WdkModelException;

  public AbstractTabularReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
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

  @Override
  protected void format2PDF(OutputStream out) throws WdkModelException, WdkUserException {
    LOG.info("format2PDF>>>");
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

  @Override
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
