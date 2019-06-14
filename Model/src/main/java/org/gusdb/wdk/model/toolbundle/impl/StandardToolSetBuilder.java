package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.toolbundle.*;
import org.gusdb.wdk.model.toolbundle.ColumnToolDelegate.ColumnToolDelegateBuilder;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class StandardToolSetBuilder implements ColumnToolSetBuilder {
  private static final String ERR_NO_NAME = "Column tool name missing or empty";
  private static final String ERR_WRAP = "Error while building tool \"%s\"";

  private final ColumnToolDelegateBuilder<ColumnReporter> reporter;

  private final ColumnToolDelegateBuilder<ColumnFilter> filter;

  private String name;

  public StandardToolSetBuilder() {
    filter = new StandardToolDelegateBuilder<>();
    reporter = new StandardToolDelegateBuilder<>();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public void setStringReporter(final ColumnToolBuilder<ColumnReporter> tool) {
    addTool(AttributeFieldDataType.STRING, tool, reporter);
  }

  @Override
  public void setStringFilter(final ColumnToolBuilder<ColumnFilter> tool) {
    addTool(AttributeFieldDataType.STRING, tool, filter);
  }

  @Override
  public void setNumberReporter(final ColumnToolBuilder<ColumnReporter> tool) {
    addTool(AttributeFieldDataType.NUMBER, tool, reporter);
  }

  @Override
  public void setNumberFilter(final ColumnToolBuilder<ColumnFilter> tool) {
    addTool(AttributeFieldDataType.NUMBER, tool, filter);
  }

  @Override
  public void setDateReporter(final ColumnToolBuilder<ColumnReporter> tool) {
    addTool(AttributeFieldDataType.DATE, tool, reporter);
  }

  @Override
  public void setDateFilter(final ColumnToolBuilder<ColumnFilter> tool) {
    addTool(AttributeFieldDataType.DATE, tool, filter);
  }

  @Override
  public void setOtherReporter(final ColumnToolBuilder<ColumnReporter> tool) {
    addTool(AttributeFieldDataType.OTHER, tool, reporter);
  }

  @Override
  public void setOtherFilter(final ColumnToolBuilder<ColumnFilter> tool) {
    addTool(AttributeFieldDataType.OTHER, tool, filter);
  }

  @Override
  public ColumnToolSet build(WdkModel wdk) throws WdkModelException {
    if (isNull(name) || name.isEmpty())
      throw new WdkModelException(ERR_NO_NAME);

    try {
      var out = new StandardColumnToolSet(
        name,
        reporter.build(wdk),
        filter.build(wdk)
      );
      return out;
    } catch (Exception e) {
      throw new WdkModelException(format(ERR_WRAP, name), e);
    }
  }

  private <T extends ColumnTool> void addTool(
    final AttributeFieldDataType type,
    final ColumnToolBuilder<T> def,
    final ColumnToolDelegateBuilder<T> del
  ) {
    def.setKey(name);
    def.setColumnType(type);
    del.addTool(def);
  }
}
