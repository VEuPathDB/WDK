package org.gusdb.wdk.model.columntool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public class ColumnToolFactory {

  public static ColumnFilter getColumnFilterInstance(AttributeField column, String toolName) throws WdkModelException {
    return getColumnToolInstance(column, toolName, ColumnToolElementRefPair::getFilter, "filter", ColumnFilter.class);
  }

  public static ColumnReporter getColumnReporterInstance(AttributeField column, String toolName) throws WdkModelException {
    return getColumnToolInstance(column, toolName, ColumnToolElementRefPair::getReporter, "reporter", ColumnReporter.class);
  }

  public static <T extends ColumnToolElement> T getColumnToolInstance(AttributeField column,
      String toolName, Function<ColumnToolElementRefPair,ColumnToolElementRef> elementDiscriminator, String type, Class<T> clazz) throws WdkModelException {
    ColumnToolElementRef implRef = Optional.ofNullable(column.getColumnToolElementPairs().get(toolName))
        .map(elementDiscriminator)
        .orElseThrow(() -> new WdkModelException("Unable to find tool '" + toolName + "' on column '" + column.getName()));
    if (implRef == null) {
      throw new WdkModelException("Unable to find " + type + " on tool '" + toolName + "' on column '" + column.getName());
    }
    @SuppressWarnings("unchecked") // already checked during resolveReferences()
    T instance = (T) createInstance(implRef.getImplementation());
    instance.setProperties(new HashMap<>(implRef.getProperties()));
    return instance;
  }

  public static Object createInstance(String className) throws WdkModelException {
    try {
      return Class.forName(className).getConstructor().newInstance();
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
        IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new WdkModelException("Could not instantiate class: " + className, e);
    }
  }

}
