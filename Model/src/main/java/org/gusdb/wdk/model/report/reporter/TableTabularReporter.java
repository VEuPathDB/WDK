package org.gusdb.wdk.model.report.reporter;

import static org.gusdb.fgputil.ListBuilder.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.single.SingleRecordAnswerValue;
import org.gusdb.wdk.model.answer.stream.FileBasedRecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.answer.stream.SingleRecordStream;
import org.gusdb.wdk.model.answer.stream.SingleTableRecordStream;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.util.TableRowProvider;
import org.json.JSONObject;

public class TableTabularReporter extends AbstractTabularReporter {

  private TableField _tableField;
  private Collection<AttributeField> _tableAttributes;

  public TableTabularReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public TableTabularReporter configure(Map<String,String> config) throws ReporterConfigException {
    super.configure(config);
    return setTable();
  }

  @Override
  public TableTabularReporter configure(JSONObject config) throws ReporterConfigException {
    super.configure(config);
    return setTable();
  }

  private TableTabularReporter setTable() throws ReporterConfigException {
    Set<TableField> tables = getSelectedTables();
    if (tables.size() != 1 || !getSelectedAttributes().isEmpty()) {
      throw new ReporterConfigException("This report supports exactly one table and no attributes.");
    }
    _tableField = tables.iterator().next();
    _tableAttributes = _tableField.getReporterAttributeFieldMap().values();
    return this;
  }

  /**
   * Override for table tabular; we actually are NOT doing answer paging in this
   * reporter since only one query is required.  We can just stream the table
   * results out the door, reading one RecordInstance at a time.
   */
  @Override
  public RecordStream getRecords() throws WdkModelException {
    if (_baseAnswer instanceof SingleRecordAnswerValue) {
      try {
        return new SingleRecordStream((SingleRecordAnswerValue)_baseAnswer);
      }
      catch (WdkUserException e) {
        throw new WdkModelException(e.getMessage(), e);
      }
    }
    RecordClass recordClass = _baseAnswer.getAnswerSpec().getQuestion().getRecordClass();
    if (idAttributeContainsNonPkFields(recordClass)) {
      // need to use FileBasedRecordStream to support both this table and any needed attributes
      return new FileBasedRecordStream(_baseAnswer,
          asList(recordClass.getIdAttributeField()),
          asList(_tableField));
    }
    else {
      // the records returned by this stream will have only PK and this single table field populated
      return new SingleTableRecordStream(_baseAnswer, _tableField);
    }
  }

  private static boolean idAttributeContainsNonPkFields(RecordClass recordClass) throws WdkModelException {
    Collection<AttributeField> deps = recordClass.getIdAttributeField().getDependencies();
    List<String> pkColumns = Arrays.asList(recordClass.getPrimaryKeyDefinition().getColumnRefs());
    for (AttributeField dep : deps) {
      if (!pkColumns.contains(dep.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected List<String> getHeader() throws WdkUserException, WdkModelException {
    List<String> list = new ArrayList<String>();
    list.add(_baseAnswer.getAnswerSpec().getQuestion().getRecordClass().getIdAttributeField().getDisplayName());
    for (AttributeField field : _tableAttributes) {
      list.add(field.getDisplayName());
    }
    return list;
  }

  @Override
  protected RowsProvider getRowsProvider(RecordInstance record)
      throws WdkUserException, WdkModelException {
    return new TableRowProvider(record, _tableField);
  }

  @Override
  protected String getFileNameSuffix() {
    return _tableField.getName();
  }
}
