package org.gusdb.wdk.model.toolbundle.reporter;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

import java.io.OutputStream;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance.ColumnProcessor;
import org.gusdb.wdk.model.toolbundle.impl.AbstractColumnTool;
import org.gusdb.wdk.model.toolbundle.reporter.report.AbstractReport;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractSingleTypeColumnReporter extends AbstractColumnTool<ColumnReporterInstance> implements ColumnReporter {

  protected static class TypedAggregationColumnProcessor<T extends Comparable<T>> implements ColumnProcessor {

    private final AbstractReport<T> _report;

    public TypedAggregationColumnProcessor(AbstractReport<T> report) {
      _report = report;
    }

    @Override
    public void processValue(String field, OutputStream out) throws WdkModelException {
      _report.pushValue(_report.parse(field));
    }

    @Override
    public void complete(OutputStream out) throws WdkModelException {
      try { Jackson.writeValue(out, _report); }
      catch (Exception e) { throw new WdkModelException(e); }
    }
  }

  private final AttributeFieldDataType _type;

  protected AbstractSingleTypeColumnReporter(AttributeFieldDataType type) {
    _type = type;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return _type.equals(type);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type, JsonNode js) {
    return isCompatibleWith(type) && js.isObject();
  }
}
