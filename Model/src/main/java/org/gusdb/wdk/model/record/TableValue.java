package org.gusdb.wdk.model.record;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;

/**
 * A TableValue object represents the values of the table that are associated
 * with a record instance.
 * 
 * If the table value of a single record is accessed, the table query will be be
 * combined with the primary key columns of the record instance, and then the
 * values of each row will be read and cached in the table value for future use.
 * 
 * If the record exists in the context on a answer (such as in the summary page,
 * or in the download report), the table query will be combined with the sorted
 * id query, and then the query will be executed in a "bulk" mode, and the table
 * value object cannot be used (because we don't want to cache all that many
 * values). the external program is responsible for combining the table query
 * with the sorted and paged id query, and read the values directly.
 * 
 * @author jerric
 */
public class TableValue implements Iterable<TableValueRow> {

  public static final int MAX_TABLE_VALUE_ROWS = 30000;

  protected final TableField _tableField;
  protected List<TableValueRow> _rows;

  public TableValue(TableField tableField) {
    _tableField = tableField;
    _rows = new ArrayList<>();
  }

  public TableField getTableField() {
    return _tableField;
  }

  public String getName() {
    return _tableField.getName();
  }

  public String getDisplayName() {
    return _tableField.getDisplayName();
  }

  public int getNumRows() {
    return _rows.size();
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getName())
        .append(": name='").append(_tableField.getName()).append("'").append(NL)
        .append("  displayName='").append(_tableField.getDisplayName()).append("'").append(NL)
        .append("  help='").append(_tableField.getHelp()).append("'").append(NL)
        .toString();
  }

  public void write(StringBuilder buf) {
    // display the headers of the table
    for (AttributeField attributeField : _tableField.getAttributeFieldMap().values()) {
      buf.append('[');
      buf.append(attributeField.getDisplayName());
      buf.append("]\t");
    }
    buf.append(NL);
    // print rows
    for (Map<String, AttributeValue> row : this) {
      for (String name : row.keySet()) {
        AttributeValue value = row.get(name);
        buf.append("'");
        buf.append(value);
        buf.append("'\t");
      }
      buf.append(NL);
    }
  }

  public void toXML(StringBuilder buf, String rowTag, String ident) {
    for (Map<String, AttributeValue> row : this) {
      buf.append(ident + "<" + rowTag + ">" + NL);
      for (String name : row.keySet()) {
        // get the value
        AttributeValue value = row.get(name);
        buf.append(ident + "    " + "<" + name + ">");
        buf.append(value);
        buf.append("</" + name + ">" + NL);
      }
      buf.append(ident + "</" + rowTag + ">" + NL);
    }
  }

  public void initializeRow(ResultList resultList) throws WdkModelException {
    _rows.add(new TableValueRow(_tableField.getAttributeFieldMap(), resultList));
  }

  @Override
  public Iterator<TableValueRow> iterator() {
    return _rows.iterator();
  }

}
