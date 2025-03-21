package org.gusdb.wdk.model.report.reporter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ProcedureWithException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.TableValueRow;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.report.PropertiesProvider;
import org.gusdb.wdk.model.report.util.TableCache;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author xingao
 */
public class FullRecordReporter extends StandardReporter {

  private static Logger LOG = Logger.getLogger(FullRecordReporter.class);

  private TableCache _tableCache = null;

  @Override
  public FullRecordReporter setProperties(PropertiesProvider reporterRef) throws WdkModelException {
    super.setProperties(reporterRef);
    setTableCache();
    return this;
  }

  private void setTableCache() {
    String cacheTableName = TableCache.getCacheTableName(_properties);
    if (cacheTableName != null) {
      _tableCache = new TableCache(getQuestion().getRecordClass(), _wdkModel.getAppDb(), cacheTableName);
    }
  }

  @Override
  public String getHttpContentType() {
    switch (getStandardConfig().getAttachmentType()) {
      case "text":
        return "text/plain";
      case "pdf":
        return "application/pdf";
      default:
        return super.getHttpContentType();
    }
  }

  @Override
  public String getDownloadFileName() {
    String name = getQuestion().getName();
    switch (getStandardConfig().getAttachmentType()) {
      case "text":
        return name + "_detail.txt";
      case "pdf":
        return name + "_detail.pdf";
      default:
        return super.getDownloadFileName();
    }
  }

  @Override
  public void write(OutputStream out) throws WdkModelException {
    try (RecordStream records = getRecords()) {
      if (getStandardConfig().getAttachmentType().equals("pdf")) {
        formatRecord2PDF(out, records, getSelectedAttributes(), getSelectedTables(),
            getStandardConfig().getIncludeEmptyTables());
      }
      else {
        formatRecord2Text(out, records, getSelectedAttributes(), getSelectedTables(),
            getStandardConfig().getIncludeEmptyTables(), _tableCache);
      }
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to write full record report", e);
    }
  }

  private static void formatRecord2Text(OutputStream out, Iterable<RecordInstance> records,
    Set<AttributeField> selectedAttributes, Set<TableField> selectedTables,
    boolean includeEmptyTables, TableCache tableCache)
        throws SQLException, WdkModelException, WdkUserException {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
    LOG.debug("FullRecordReporter: formatRecord2Text(): looping through records calling formatAttributes and formatTables for each");
    try {
      if (tableCache != null) {
        tableCache.open();
      }
      int recordCount = 0;
      for (RecordInstance record : records) {
        //LOG.debug("formatRecord2Text() - looping on records: one record: ");
        // print out attributes of the record first
        formatAttributes(record, selectedAttributes, writer);

        // print out tables (may get table formatting from cache)
        formatTables(record, selectedTables, includeEmptyTables, writer, tableCache, FullRecordReporter::writeTable);

        writer.println();
        writer.println("------------------------------------------------------------");
        writer.println();
        writer.flush();

        // count the records processed so far
        recordCount++;
        if (recordCount % 100 == 0) {
          LOG.info("FullRecordReporter: formatRecord2Text(): " + recordCount + " records dumped so far");
        }
      }
      LOG.info("FullRecordReporter: formatRecord2Text(): Totally: " + recordCount + " records dumped");
    }
    finally {
      if (tableCache != null) {
        tableCache.close();
      }
    }
  }

  private static void formatAttributes(RecordInstance record, Set<AttributeField> attributes, PrintWriter writer)
      throws WdkModelException, WdkUserException {
    // print out attributes of the record first
    for (AttributeField field : attributes) {
      AttributeValue value = record.getAttributeValue(field.getName());
      writer.println(field.getDisplayName() + ": " + value);
    }
    // print out attributes of the record first
    writer.println();
    writer.flush();
    //LOG.debug("FullRecordReporter: formatAttributes(): ------- done one more record");
  }

  /**
   * Returns a tuple of table_size (# of rows) and formatted string
   * 
   * @param inputs table value for one row
   * @return table size and formatted table
   * @throws WdkRuntimException if unable to format table
   */
  private static int writeTable(ThreeTuple<TableValue, Writer, Boolean> inputs) {
    try {
      TableValue tableValue = inputs.getFirst();
      TableField table = tableValue.getTableField();
      Collection<AttributeField> fields = table.getReporterAttributeFieldMap().values();
      Writer out = inputs.getSecond();
      boolean includeEmptyTables = inputs.getThird();

      // output table header
      ProcedureWithException writeHeader = () -> {
        out.write("Table: " + table.getDisplayName() + NL);
        for (AttributeField attribute : fields) {
          out.write("[");
          out.write(attribute.getDisplayName());
          out.write("]\t");
        }
        out.write(NL);
      };

      int tableSize = 0;
      for (Map<String, AttributeValue> row : tableValue) {

        // rows present; write header first time through the loop
        if (tableSize == 0) writeHeader.perform();
        tableSize++;
        for (AttributeField field : fields) {
          AttributeValue value = row.get(field.getName());
          out.write(String.valueOf(value.getValue()));
          out.write("\t");
        }
        out.write(NL);
      }

      // even if no rows, print header if includeEmptyTables is true
      if (tableSize == 0 && includeEmptyTables) {
        writeHeader.perform();
      }

      //LOG.debug("FullRecordReporter: formatTable(): tableSize: " + tableSize);
      return tableSize;
    }
    catch (Exception e) {
      throw new WdkRuntimeException("Unable to format table value", e);
    }
  }

  private static void formatRecord2PDF(OutputStream out, Iterable<RecordInstance> records,
      Set<AttributeField> attributes, Set<TableField> tables, boolean includeEmptyTables)
          throws WdkModelException, WdkUserException {

    LOG.info("format2PDF>>>");
    Document document = new Document(PageSize.LETTER.rotate());

    try {
      PdfWriter pwriter = PdfWriter.getInstance(document, out);
      document.open();

      for (RecordInstance record : records) {
        // print out attributes of the record first
        for (AttributeField field : attributes) {
          AttributeValue value = record.getAttributeValue(field.getName());
          document.add(new Paragraph(field.getDisplayName() + ": " + value));
        }

        // print out tables of the record
        for (TableField table : tables) {
          TableValue tableValue = record.getTableValue(table.getName());

          // check if table is empty
          Iterator<TableValueRow> iterator = tableValue.iterator();
          if (!includeEmptyTables && !iterator.hasNext()) {
            continue;
          }

          Collection<AttributeField> fields = table.getReporterAttributeFieldMap().values();

          // output table header
          document.add(new Paragraph("Table: " + table.getDisplayName()));
          int numColumns = fields.size();
          PdfPTable datatable = new PdfPTable(numColumns);
          for (AttributeField attribute : fields) {
            datatable.addCell("" + attribute.getDisplayName() + "");
          }

          datatable.setHeaderRows(1);

          while (iterator.hasNext()) {
            Map<String, AttributeValue> row = iterator.next();
            for (String fieldName : row.keySet()) {
              AttributeValue value = row.get(fieldName);
              Object objValue = value.getValue();
              if (objValue == null)
                objValue = "";
              datatable.addCell(objValue.toString());
            }
          }
          document.add(datatable);
        }
      }
      document.close();
      pwriter.close();
    }
    catch (DocumentException de) {
      throw new WdkModelException(de);
    }
  }
}
