package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author xingao
 */
public class TabularReporter extends BaseTabularReporter {

  private static Logger LOG = Logger.getLogger(TabularReporter.class);

  public TabularReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  protected void format2Text(OutputStream out) throws WdkModelException, WdkUserException {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
    Set<AttributeField> fields = getSelectedAttributes();
    // print the header
    if (_includeHeader) {
      for (AttributeField field : fields) {
        writer.print("[" + field.getDisplayName() + "]");
        writer.print(_divider);
      }
      writer.println();
      writer.flush();
    }

    try (RecordStream records = getRecords()) {
      for (RecordInstance record : records) {
        for (AttributeField field : fields) {
          AttributeValue value = record.getAttributeValue(field.getName());
          writer.print((value == null) ? "N/A" : value.getValue());
          writer.print(_divider);
        }
        writer.println();
        writer.flush();
      }
    }
  }

  @Override
  protected void format2PDF(OutputStream out) throws WdkModelException, WdkUserException {
    LOG.info("format2PDF>>>");
    Document document = new Document(PageSize.LETTER.rotate());

    try (RecordStream records = getRecords()) {

      PdfWriter pwriter = PdfWriter.getInstance(document, out);
      document.open();

      Set<AttributeField> fields = getSelectedAttributes();
      int numFields = fields.size();

      PdfPTable datatable = new PdfPTable(numFields);

      if (_includeHeader) {
        for (AttributeField field : fields) {
          datatable.addCell("" + field.getDisplayName() + "");
        }
      }

      datatable.setHeaderRows(1);
      int count = 0;

      for (RecordInstance record : records) {
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

      datatable.setSplitLate(false);
      datatable.setWidthPercentage(100);
      document.setMargins(10, 10, 10, 10);
      document.add(datatable);
      document.close();
      out.flush();
    }
    catch (DocumentException | IOException ex) {
      throw new WdkModelException(ex);
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
      for (AttributeField field : getSelectedAttributes()) {
        String title = "<th>[" + field.getDisplayName() + "]</th>";
        writer.print(title);
        count += title.length();
      }
      writer.println();
      writer.println("</tr>");
      writer.flush();
      count += 7;
    }

    Set<AttributeField> fields = getSelectedAttributes();
    try (RecordStream records = getRecords()) {
      for (RecordInstance record : records) {
        writer.println("<tr>");
        count += 5;
        for (AttributeField field : fields) {
          AttributeValue value = record.getAttributeValue(field.getName());
          String val = "<td>" + value.getValue() + "</td>";
          writer.print(val);
          count += val.length();
        }
        writer.println();
        writer.println("</tr>");
        writer.flush();
        count += 7;
  
        // check if the output exceeds the max allowed size
        if (count > MAX_EXCEL_LENGTH) {
          writer.print("<tr><td colspan=\"" + fields.size() + "\">");
          writer.print("The result size exceeds the maximum allowed " +
              "size for downloading excel files. The rest of " +
              "the results are ignored. Opening huge excel " +
              "files may crash your system. If you need to " +
              "get the complete results, please choose the " +
              "download type as Text File, or Show in Browser.");
          writer.println("</td></tr>");
          break;
        }
      }
      writer.println("</table>");
      writer.flush();
    }
  }
}
