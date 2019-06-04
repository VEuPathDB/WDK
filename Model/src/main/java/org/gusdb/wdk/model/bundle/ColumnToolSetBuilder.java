package org.gusdb.wdk.model.bundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public interface ColumnToolSetBuilder {

  String getName();

  void setName(String name);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setStringReporter(ColumnToolBuilder<ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setStringFilter(ColumnToolBuilder<ColumnFilter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setNumberReporter(ColumnToolBuilder<ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setNumberFilter(ColumnToolBuilder<ColumnFilter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setDateReporter(ColumnToolBuilder<ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setDateFilter(ColumnToolBuilder<ColumnFilter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setOtherReporter(ColumnToolBuilder<ColumnReporter> tool);

  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void setOtherFilter(ColumnToolBuilder<ColumnFilter> tool);

  ColumnToolSet build(WdkModel wdk) throws WdkModelException;
}
