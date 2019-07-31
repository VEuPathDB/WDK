package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.toolbundle.*;
import org.gusdb.wdk.model.record.attribute.AttributeField;

import java.util.Optional;

public class StandardColumnToolSet implements ColumnToolSet {

  private final String _name;

  private final ColumnToolDelegate<ColumnReporterInstance,ColumnReporter> _reporter;

  private final ColumnToolDelegate<ColumnFilterInstance,ColumnFilter> _filter;

  StandardColumnToolSet(
    final String name,
    final ColumnToolDelegate<ColumnReporterInstance,ColumnReporter> reporter,
    final ColumnToolDelegate<ColumnFilterInstance,ColumnFilter> filter
  ) {
    _name = name;
    _reporter = reporter;
    _filter = filter;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public boolean hasFilterFor(final AttributeField field) {
    return _filter.hasToolFor(field.getDataType());
  }

  @Override
  public Optional<ColumnFilterInstance> makeFilterInstance(
    final AttributeField field,
    final AnswerValue val,
    final ColumnToolConfig config
  ) throws WdkModelException {
    return _filter.makeInstance(field, val, config);
  }

  @Override
  public Optional<ColumnFilter> getFilterFor(AttributeField field) {
    return _filter.getTool(field);
  }

  @Override
  public boolean hasReporterFor(final AttributeField field) {
    return _reporter.hasToolFor(field.getDataType());
  }

  @Override
  public Optional<ColumnReporterInstance> makeReporterInstance(
    final AttributeField field,
    final AnswerValue val,
    final ColumnToolConfig config
  ) throws WdkModelException {
    return _reporter.makeInstance(field, val, config);
  }

  @Override
  public Optional<ColumnReporter> getReporterFor(AttributeField field) {
    return _reporter.getTool(field);
  }
}
