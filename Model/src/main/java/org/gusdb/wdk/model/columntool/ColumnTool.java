package org.gusdb.wdk.model.columntool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

public class ColumnTool extends WdkModelBase implements NamedObject {

  private String _name;
  private Map<AttributeFieldDataType, ColumnToolElementRefPair> _typeMap = new HashMap<>();

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  public ColumnToolElementRefPair getElementPair(AttributeFieldDataType dataType) {
    return _typeMap.get(dataType);
  }

  public void setStringPair(ColumnToolElementRefPair pair) {
    _typeMap.put(AttributeFieldDataType.STRING, pair);
  }

  public void setDatePair(ColumnToolElementRefPair pair) {
    _typeMap.put(AttributeFieldDataType.DATE, pair);
  }

  public void setNumberPair(ColumnToolElementRefPair pair) {
    _typeMap.put(AttributeFieldDataType.NUMBER, pair);
  }

  public void setOtherPair(ColumnToolElementRefPair pair) {
    _typeMap.put(AttributeFieldDataType.OTHER, pair);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // make sure provided classes exist and are compatible types (may differentiate by data type in the future)
    checkType(AttributeFieldDataType.STRING, "reporter", ColumnToolElementRefPair::getReporter, c -> c instanceof ColumnReporter);
    checkType(AttributeFieldDataType.STRING, "filter", ColumnToolElementRefPair::getFilter, c -> c instanceof ColumnFilter);
    checkType(AttributeFieldDataType.DATE, "reporter", ColumnToolElementRefPair::getReporter, c -> c instanceof ColumnReporter);
    checkType(AttributeFieldDataType.DATE, "filter", ColumnToolElementRefPair::getFilter, c -> c instanceof ColumnFilter);
    checkType(AttributeFieldDataType.NUMBER, "reporter", ColumnToolElementRefPair::getReporter, c -> c instanceof ColumnReporter);
    checkType(AttributeFieldDataType.NUMBER, "filter", ColumnToolElementRefPair::getFilter, c -> c instanceof ColumnFilter);
    checkType(AttributeFieldDataType.OTHER, "reporter", ColumnToolElementRefPair::getReporter, c -> c instanceof ColumnReporter);
    checkType(AttributeFieldDataType.OTHER, "filter", ColumnToolElementRefPair::getFilter, c -> c instanceof ColumnFilter);
  }

  private void checkType(AttributeFieldDataType columnType, String toolType,
      Function<ColumnToolElementRefPair,ColumnToolElementRef> implementationGetter,
      Predicate<Object> isCorrectType) throws WdkModelException {
    ColumnToolElementRefPair pair = _typeMap.get(columnType);
    if (pair == null) return;
    ColumnToolElementRef ref = implementationGetter.apply(pair);
    if (ref == null) return;
    Object o = ColumnToolFactory.createInstance(ref.getImplementation());
    if (!isCorrectType.test(o)) {
      throw new WdkModelException("Invalid implementation class for " + columnType + " column " + toolType + ": " + ref.getImplementation());
    }
    // we know it is a ColumnReporter or ColumnFilter
    ColumnToolElement<?> element = (ColumnToolElement<?>)o;
    if (!element.getSupportedDataTypes().contains(columnType)) {
      throw new WdkModelException(element.getClass().getName() + " is assigned to attribute of type " + columnType + " but does not support this type.");
    }
    
  }
}
