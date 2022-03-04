package org.gusdb.wdk.model.columntool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import org.gusdb.fgputil.functional.Either;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public class ColumnToolFactory {

  public static Optional<ColumnFilter> tryColumnFilterInstance(AttributeField column, String toolName) {
    return getColumnToolInstance(column, toolName, ColumnToolElementRefPair::getFilter, "filter", ColumnFilter.class).left();
  }

  public static ColumnFilter getColumnFilterInstance(AttributeField column, String toolName) throws WdkModelException {
    return getColumnToolInstance(column, toolName, ColumnToolElementRefPair::getFilter, "filter", ColumnFilter.class)
        .leftOrElseThrowWithRight(msg -> new WdkModelException(msg));
  }

  public static Optional<ColumnReporter> tryColumnReporterInstance(AttributeField column, String toolName) {
    return getColumnToolInstance(column, toolName, ColumnToolElementRefPair::getReporter, "reporter", ColumnReporter.class).left();
  }

  public static ColumnReporter getColumnReporterInstance(AttributeField column, String toolName) throws WdkModelException {
    return getColumnToolInstance(column, toolName, ColumnToolElementRefPair::getReporter, "reporter", ColumnReporter.class)
        .leftOrElseThrowWithRight(msg -> new WdkModelException(msg));
  }

  public static <T extends ColumnToolElement<T>> Either<T,String> getColumnToolInstance(AttributeField column,
      String toolName, Function<ColumnToolElementRefPair,ColumnToolElementRef> elementDiscriminator, String type, Class<T> clazz) {

    // try to look up tool with this name and type on this column
    ColumnToolElementRefPair implPair = column.getColumnToolElementPairs().get(toolName);
    if (implPair == null) return Either.right("Unable to find tool '" + toolName + "' on column '" + column.getName() + "'");

    // see if there is an element of the requested type on this tool
    ColumnToolElementRef implRef = elementDiscriminator.apply(implPair);
    if (implRef == null) return Either.right("Unable to find " + type + " on tool '" + toolName + "' on column '" + column.getName());

    // found implementation ref; try to make an instance and set properties
    @SuppressWarnings("unchecked") // already checked during resolveReferences()
    T instance = (T) createInstance(implRef.getImplementation());
    instance.setModelProperties(new HashMap<>(implRef.getProperties()));
    instance.setAttributeField(column);
    return Either.left(instance);
  }

  public static Object createInstance(String className) {
    try {
      return Class.forName(className).getConstructor().newInstance();
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
        IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new WdkRuntimeException("Could not instantiate class: " + className, e);
    }
  }

}
