package org.gusdb.wdk.model.columntool;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.columntool.ToolInterfaces.DateColumnFilter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.DateColumnReporter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.NumberColumnFilter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.NumberColumnReporter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.ObjectColumnFilter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.ObjectColumnReporter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.StringColumnFilter;
import org.gusdb.wdk.model.columntool.ToolInterfaces.StringColumnReporter;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

public class ColumnTool extends WdkModelBase implements NamedObject {

  private String _name;
  private Map<AttributeFieldDataType, ColumnToolElementPair> _typeMap;

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  public ColumnToolElementPair getElementPair(AttributeFieldDataType dataType) {
    return _typeMap.get(dataType);
  }

  public void addStringPair(ColumnToolElementPair pair) {
    _typeMap.put(AttributeFieldDataType.STRING, pair);
  }

  public void addDatePair(ColumnToolElementPair pair) {
    _typeMap.put(AttributeFieldDataType.DATE, pair);
  }

  public void addNumberPair(ColumnToolElementPair pair) {
    _typeMap.put(AttributeFieldDataType.NUMBER, pair);
  }

  public void addOtherPair(ColumnToolElementPair pair) {
    _typeMap.put(AttributeFieldDataType.OTHER, pair);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // make sure provided classes exist and are compatible types
    checkType(AttributeFieldDataType.STRING, "reporter", ColumnToolElementPair::getReporter, c -> c instanceof StringColumnReporter);
    checkType(AttributeFieldDataType.STRING, "filter", ColumnToolElementPair::getFilter, c -> c instanceof StringColumnFilter);
    checkType(AttributeFieldDataType.DATE, "reporter", ColumnToolElementPair::getReporter, c -> c instanceof DateColumnReporter);
    checkType(AttributeFieldDataType.DATE, "filter", ColumnToolElementPair::getFilter, c -> c instanceof DateColumnFilter);
    checkType(AttributeFieldDataType.NUMBER, "reporter", ColumnToolElementPair::getReporter, c -> c instanceof NumberColumnReporter);
    checkType(AttributeFieldDataType.NUMBER, "filter", ColumnToolElementPair::getFilter, c -> c instanceof NumberColumnFilter);
    checkType(AttributeFieldDataType.OTHER, "reporter", ColumnToolElementPair::getReporter, c -> c instanceof ObjectColumnReporter);
    checkType(AttributeFieldDataType.OTHER, "filter", ColumnToolElementPair::getFilter, c -> c instanceof ObjectColumnFilter);
  }

  private void checkType(AttributeFieldDataType columnType, String toolType,
      Function<ColumnToolElementPair,ImplementationRef> implementationGetter,
      Predicate<Object> isCorrectType) throws WdkModelException {
    ColumnToolElementPair pair = _typeMap.get(columnType);
    if (pair == null) return;
    ImplementationRef ref = implementationGetter.apply(pair);
    if (ref == null) return;
    Object o = ColumnToolFactory.createInstance(ref.getImplementation());
    if (!isCorrectType.test(o)) {
      throw new WdkModelException("Invalid implementation class for " + columnType + " column " + toolType + ": " + ref.getImplementation());
    }
  }
}
