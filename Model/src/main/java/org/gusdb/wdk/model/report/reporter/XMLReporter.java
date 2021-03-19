package org.gusdb.wdk.model.report.reporter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.report.ReporterInfo;
import org.gusdb.wdk.model.report.util.TableCache;

/**
 * @author Cary P.
 */
public class XMLReporter extends StandardReporter {

  private static Logger LOG = Logger.getLogger(XMLReporter.class);

  private static final Function<TableValue, TwoTuple<Integer,String>> TABLE_XML_FORMATTER = tableValue -> formatTable(tableValue);

  private TableCache _tableCache;

  public XMLReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public void setProperties(ReporterInfo reporterRef) throws WdkModelException {
    super.setProperties(reporterRef);
    String cacheTableName = TableCache.getCacheTableName(_properties);
    if (cacheTableName != null) {
      _tableCache = new TableCache(getQuestion().getRecordClass(), _wdkModel.getAppDb(), cacheTableName);
    }
  }

  @Override
  public String getHttpContentType() {
    return "text/xml";
  }

  @Override
  public String getDownloadFileName() {
    return getQuestion().getName() + ".xml";
  }

  @Override
  public void write(OutputStream out) throws WdkModelException {
    try (RecordStream records = getRecords()) {
      int recordCount = 0;
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
      writer.println("<response>");
      String type = normalizeProperty(getQuestion().getRecordClass().getDisplayName());
      writer.println("  <recordset id=\"" + _baseAnswer.getChecksum() + "\" count=\"" +
          _baseAnswer.getResultSizeFactory().getResultSize() + "\" type=\"" + type + "\">");
      if (_tableCache != null) {
        _tableCache.open();
      }
      for (RecordInstance record : records) {

        recordCount++;
        writer.println("    <record>");

        // print out the primary key of this record
        formatPrimaryKey(record, writer);

        // print out attributes
        formatAttributes(record, getSelectedAttributes(), writer);

        // print out tables
        formatTables(record, getSelectedTables(), getStandardConfig().getIncludeEmptyTables(), writer, _tableCache, TABLE_XML_FORMATTER);

        // count the records processed so far
        writer.println("    </record>");
        writer.flush();
      }
      writer.println("  </recordset>");
      writer.println("</response>");
      writer.flush();
      LOG.info("Totally " + recordCount + " records dumped");
    }
    catch (WdkUserException | SQLException e) {
      throw new WdkModelException("Unable to produce XML report", e);
    }
    finally {
      if (_tableCache != null) {
        _tableCache.close();
      }
    }
  }

  private static String normalizeProperty(String property) {
    return property.replaceAll("(<[^>]*>)|([&\"']+)", "_");
  }

  private void formatPrimaryKey(RecordInstance record, PrintWriter writer) {
    writer.println("      <primaryKey>");
    for (Entry<String,String> pkCol : record.getPrimaryKey().getValues().entrySet()) {
      writer.println("        <column name=\"" + pkCol.getKey() + "\"><![CDATA[" + pkCol.getValue() + "]]></column>");
    }
    writer.println("      </primaryKey>");
  }

  private static void formatAttributes(RecordInstance record, Set<AttributeField> attributes, PrintWriter writer)
      throws WdkModelException, WdkUserException {
    // print out attributes of the record first
    for (AttributeField field : attributes) {
      AttributeValue value = record.getAttributeValue(field.getName());
      writer.println("      <field name=\"" + field.getName() + "\" title=\"" + field.getDisplayName() +
          "\"><![CDATA[" + value.toString() + "]]></field>");
    }
    // print out attributes of the record first
    writer.println();
    writer.flush();
  }

  private static TwoTuple<Integer, String> formatTable(TableValue tableValue) {
    try {
      TableField table = tableValue.getTableField();
      StringBuilder sb = new StringBuilder();
      sb.append("      <table name=\"" + table.getDisplayName() + "\">" + NL);
      int tableSize = 0;
      Collection<AttributeField> reportTableFields = table.getReporterAttributeFieldMap().values();
      for (Map<String, AttributeValue> row : tableValue) {
        tableSize++;
        sb.append("        <row>" + NL);
        for (AttributeField field : reportTableFields) {
          String fieldName = field.getName();
          AttributeValue value = row.get(fieldName);
          sb.append("          <field name=\"" + fieldName + "\"><![CDATA[" + value.getValue() + "]]></field>" + NL);
        }
        sb.append("        </row>" + NL);
      }
      sb.append("      </table>" + NL);
      return new TwoTuple<Integer, String>(tableSize, sb.toString());
    }
    catch (WdkUserException | WdkModelException e) {
      throw new WdkRuntimeException("Unable to format table value into XML", e);
    }
  }
}
