package org.gusdb.wdk.model.report.reporter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.report.ReporterInfo;
import org.gusdb.wdk.model.report.util.TableCache;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author xingao
 */
public class FullRecordReporter extends StandardReporter {

  private static Logger LOG = Logger.getLogger(FullRecordReporter.class);

  private static final Function<TableValue, TwoTuple<Integer,String>> TABLE_FORMATTER = tableValue -> formatTable(tableValue);

  private TableCache _tableCache = null;

  public FullRecordReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public void setProperties(ReporterInfo reporterRef) throws WdkModelException {
    super.setProperties(reporterRef);
    setTableCache();
  }

  private void setTableCache() {
    String cacheTableName = TableCache.getCacheTableName(_properties);
    if (cacheTableName != null) {
      _tableCache = new TableCache(getQuestion().getRecordClass(), _wdkModel.getAppDb(), cacheTableName);
    }
  }

  // special case to support model-independent code in FullRecordFileCreator.java
  // TODO: find out if that file is still in use; if not, delete and delete this method
  public void setProperties(Map<String, String> properties) {
    _properties = new HashMap<>(properties);
    setTableCache();
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
        formatTables(record, selectedTables, includeEmptyTables, writer, tableCache, TABLE_FORMATTER);

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
   * @param tableValue table value for one row
   * @return table size and formatted table
   * @throws WdkRuntimException if unable to format table
   */
  private static TwoTuple<Integer,String> formatTable(TableValue tableValue) {
    try {
      TableField table = tableValue.getTableField();
      AttributeField[] fields = table.getAttributeFields(FieldScope.REPORT_MAKER);
      // output table header
      StringBuffer sb = new StringBuffer();
      sb.append("Table: " + table.getDisplayName() + NL);
      for (AttributeField attribute : fields) {
        sb.append("[").append(attribute.getDisplayName()).append("]\t");
      }
      sb.append(NL);
  
      int tableSize = 0;
      for (Map<String, AttributeValue> row : tableValue) {
        tableSize++;
        for (AttributeField field : fields) {
          AttributeValue value = row.get(field.getName());
          sb.append(value.getValue()).append("\t");
        }
        sb.append(NL);
      }
      //LOG.debug("FullRecordReporter: formatTable(): tableSize: " + tableSize);
      return new TwoTuple<Integer, String>(tableSize, sb.toString());
    }
    catch (WdkModelException | WdkUserException e) {
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
          Iterator<Map<String, AttributeValue>> iterator = tableValue.iterator();
          if (!includeEmptyTables && !iterator.hasNext()) {
            continue;
          }

          AttributeField[] fields = table.getAttributeFields(FieldScope.REPORT_MAKER);

          // output table header
          document.add(new Paragraph("Table: " + table.getDisplayName()));
          int NumColumns = fields.length;
          PdfPTable datatable = new PdfPTable(NumColumns);
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
