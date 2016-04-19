package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.WdkModelException;

public class TableTabularReporter extends AbstractTabularReporter {

  private TableField tableField;

  public TableTabularReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }

  private TableField getTableField() throws WdkUserException, WdkModelException {
    if (tableField == null) {
      Set<Field> fields = validateColumns();
      for (Field field : fields) {
        if (field instanceof AttributeField) {
          throw new WdkUserException("This report is for Tables only, not custom columns");
        }
        else if (field instanceof TableField) {
          if (tableField != null) throw new WdkUserException("This report supports only a single table");
          tableField = (TableField) field;
        }
      }
    }
    return tableField;
  }
  
  @Override
  protected List<String> getHeader() throws WdkUserException, WdkModelException {
    AttributeField[] fields = getTableField().getAttributeFields(FieldScope.REPORT_MAKER);
    List<String> list = new ArrayList<String>();
    for (AttributeField field : fields) list.add(field.getDisplayName());
    return list;
  }
  
  @Override
  protected TabularReporterRowsProvider getRowsProvider(AnswerValue answerValuePage) throws WdkUserException, WdkModelException {
    return new TableRowProvider(answerValuePage, getTableField());
  }

}
