package org.gusdb.wdk.model.report.reporter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.report.PropertiesProvider;
import org.gusdb.wdk.model.report.util.TableCache;

/**
 * @author Cary P.
 */
public class XMLReporter extends StandardReporter {

  private static Logger LOG = Logger.getLogger(XMLReporter.class);

  private TableCache _tableCache;

  @Override
  public XMLReporter setProperties(PropertiesProvider reporterRef) throws WdkModelException {
    super.setProperties(reporterRef);
    String cacheTableName = TableCache.getCacheTableName(_properties);
    if (cacheTableName != null) {
      _tableCache = new TableCache(getQuestion().getRecordClass(), _wdkModel.getAppDb(), cacheTableName);
    }
    return this;
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
        formatTables(record, getSelectedTables(), getStandardConfig().getIncludeEmptyTables(), writer, _tableCache, XMLReporter::formatTable);

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

  private static int formatTable(ThreeTuple<TableValue, Writer, Boolean> inputs) {
    try {
      TableValue tableValue = inputs.getFirst();
      TableField table = tableValue.getTableField();
      Collection<AttributeField> reportTableFields = table.getReporterAttributeFieldMap().values();
      Writer out = inputs.getSecond();
      boolean includeEmptyTables = inputs.getThird();

      String tableBegin = "      <table name=\"" + table.getDisplayName() + "\">" + NL;
      String tableEnd   = "      </table>" + NL;

      int tableSize = 0;
      for (Map<String, AttributeValue> row : tableValue) {
        tableSize++;
        if (tableSize == 1) out.write(tableBegin);
        out.write("        <row>" + NL);
        for (AttributeField field : reportTableFields) {
          String fieldName = field.getName();
          AttributeValue value = row.get(fieldName);
          out.write("          <field name=\"" + fieldName + "\"><![CDATA[" + value.getValue() + "]]></field>" + NL);
        }
        out.write("        </row>" + NL);
      }

      // if didn't already write tableBegin but writing empty table XML...
      if (tableSize == 0 && includeEmptyTables) out.write(tableBegin);

      // if wrote some rows or writing empty table XML...
      if (tableSize > 0 || includeEmptyTables) out.write(tableEnd);

      return tableSize;
    }
    catch (Exception e) {
      throw new WdkRuntimeException("Unable to format table value into XML", e);
    }
  }
}
