package org.gusdb.wdk.model.columntool;

import java.time.LocalDateTime;
import java.util.Map;

import org.gusdb.wdk.model.report.Reporter;

import io.vulpine.lib.json.schema.SchemaBuilder;

public class ToolInterfaces {

  public interface ColumnToolElement {
    void setProperties(Map<String,String> properties);
    SchemaBuilder getInputSpec();
  }

  public interface ColumnReporter<T> extends ColumnToolElement, Reporter {
    SchemaBuilder getOutputSpec();
  }
  public interface ColumnFilter<T> extends ColumnToolElement{}

  public interface StringColumnReporter extends ColumnReporter<String>{}
  public interface StringColumnFilter extends ColumnFilter<String>{}

  public interface DateColumnReporter extends ColumnReporter<LocalDateTime>{}
  public interface DateColumnFilter extends ColumnFilter<LocalDateTime>{}

  public interface NumberColumnReporter extends ColumnReporter<Double>{}
  public interface NumberColumnFilter extends ColumnFilter<Double>{}

  public interface ObjectColumnReporter extends ColumnReporter<Object>{}
  public interface ObjectColumnFilter extends ColumnFilter<Object>{}
  
}
