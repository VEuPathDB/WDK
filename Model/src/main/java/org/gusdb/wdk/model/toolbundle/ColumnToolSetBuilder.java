package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public interface ColumnToolSetBuilder {

  String getName();

  void setName(String name);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setStringReporter(ColumnToolBuilder<ColumnReporterInstance,ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setStringFilter(ColumnToolBuilder<ColumnFilterInstance,ColumnFilter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setNumberReporter(ColumnToolBuilder<ColumnReporterInstance,ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setNumberFilter(ColumnToolBuilder<ColumnFilterInstance,ColumnFilter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setDateReporter(ColumnToolBuilder<ColumnReporterInstance,ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setDateFilter(ColumnToolBuilder<ColumnFilterInstance,ColumnFilter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setOtherReporter(ColumnToolBuilder<ColumnReporterInstance,ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setOtherFilter(ColumnToolBuilder<ColumnFilterInstance,ColumnFilter> tool);

  ColumnToolSet build(WdkModel wdk) throws WdkModelException;
}
