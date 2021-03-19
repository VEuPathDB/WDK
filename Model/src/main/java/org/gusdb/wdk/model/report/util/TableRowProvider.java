package org.gusdb.wdk.model.report.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValueRow;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.report.reporter.AbstractTabularReporter.RowsProvider;

public class TableRowProvider implements RowsProvider {

  private final RecordInstance _record;
  private final TableField _tableField;

  public TableRowProvider(RecordInstance record, TableField tableField) {
    _record = record;
    _tableField = tableField;
  }

  @Override
  public Iterator<List<Object>> iterator() {
    try {
      return IteratorUtil.transform(
          _record.getTableValue(_tableField.getName()).iterator(),
          getTableRowConverter(_record, _tableField));
    }
    catch (WdkModelException | WdkUserException e) {
      throw new WdkRuntimeException("Unable to create iterator over table rows for table " + _tableField.getName(), e);
    }
  }

  private static Function<TableValueRow,List<Object>> getTableRowConverter(
      final RecordInstance record, final TableField tableField) {
    Collection<AttributeField> fields = tableField.getReporterAttributeFieldMap().values();
    return tableRow -> {
      try {
        List<Object> values = new ArrayList<Object>();
        values.add(record.getIdAttributeValue().getDisplay());
        for (AttributeField field : fields) {
          AttributeValue attrValue = tableRow.get(field.getName());
          values.add((attrValue == null) ? "N/A" : attrValue.getValue());
        }
        return values;
      }
      catch (WdkUserException | WdkModelException e) {
        throw new WdkRuntimeException("Unable to create value row for table " + tableField.getName(), e);
      }
    };
  }
}
