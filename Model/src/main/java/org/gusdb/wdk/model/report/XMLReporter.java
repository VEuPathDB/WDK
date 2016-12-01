package org.gusdb.wdk.model.report;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
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

/**
 * @author Cary P.
 */
public class XMLReporter extends StandardReporter {

  private static Logger LOG = Logger.getLogger(XMLReporter.class);

  private static final Function<TableValue, TwoTuple<Integer,String>> tableFormatter =
      new Function<TableValue, TwoTuple<Integer,String>>() {
    @Override public TwoTuple<Integer, String> apply(TableValue tableValue) {
      return formatTable(tableValue);
    }
  };

  private TableCache _tableCache;

  public XMLReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public void setProperties(Map<String, String> properties) throws WdkModelException {
    super.setProperties(properties);
    String cacheTableName = TableCache.getCacheTableName(properties);
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
      writer.println("<?xml version='1.0' encoding='UTF-8'?>");
      writer.println("<response>");
      String type = normalizeProperty(getQuestion().getRecordClass().getDisplayName());
      writer.println("<recordset id='" + _baseAnswer.getChecksum() + "' count='" +
          _baseAnswer.getResultSizeFactory().getResultSize() + "' type='" + type + "'>");
      if (_tableCache != null) {
        _tableCache.open();
      }
      for (RecordInstance record : records) {

        recordCount++;
        String id = normalizeProperty(record.getIdAttributeValue().getValue().toString());
        writer.println("<record id='" + id + "'>");

        // print out attributes of the record first
        formatAttributes(record, getSelectedAttributes(), writer);

        // print out tables
        formatTables(record, getSelectedTables(), getStandardConfig().getIncludeEmptyTables(),
            writer, _tableCache, tableFormatter);

        // count the records processed so far
        writer.println("</record>");
        writer.flush();
      }
      writer.println("</recordset>");
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

  private static void formatAttributes(RecordInstance record, Set<AttributeField> attributes, PrintWriter writer)
      throws WdkModelException, WdkUserException {
    // print out attributes of the record first
    for (AttributeField field : attributes) {
      AttributeValue value = record.getAttributeValue(field.getName());
      writer.println("<field name='" + field.getName() + "' title='" + field.getDisplayName() +
          "'><![CDATA[" + value + "]]></field>");
    }
    // print out attributes of the record first
    writer.println();
    writer.flush();
  }

  private static TwoTuple<Integer, String> formatTable(TableValue tableValue) {
    try {
      TableField table = tableValue.getTableField();
      StringBuilder sb = new StringBuilder();
      sb.append("<table name='" + table.getDisplayName() + "'>" + NL);
      int tableSize = 0;
      for (Map<String, AttributeValue> row : tableValue) {
        tableSize++;
        sb.append("<row>" + NL);
        for (AttributeField field : table.getAttributeFields(FieldScope.REPORT_MAKER)) {
          String fieldName = field.getName();
          AttributeValue value = row.get(fieldName);
          sb.append("<field name='" + fieldName + "'><![CDATA[" + value.getValue() + "]]></field>" + NL);
        }
        sb.append("</row>" + NL);
      }
      sb.append("</table>" + NL);
      return new TwoTuple<Integer, String>(tableSize, sb.toString());
    }
    catch (WdkUserException | WdkModelException e) {
      throw new WdkRuntimeException("Unable to format table value into XML", e);
    }
  }
}
