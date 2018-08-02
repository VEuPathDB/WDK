package org.gusdb.wdk.model;

import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.fgputil.functional.Functions.reduce;
import static org.gusdb.wdk.model.RngAnnotations.getRngFields;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.runtime.UnfetchableInstanceException;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.RngAnnotations.FieldSetter;
import org.gusdb.wdk.model.ontology.OntologyAttribute;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.report.DynamicAttributeReporterReference;

/**
 * This class contains a method to populate the column and attribute field objects associated with a database
 * loaded attribute.
 * 
 * @author crisl-adm
 * @author rdoherty
 *
 */
public class AttributeMetaQueryHandler {

  public static final boolean CACHE_META_QUERY_RESULTS = true;

  private static final List<Class<?>> CLIENT_CLASSES = Arrays.asList(new Class<?>[] {
      Column.class, QueryColumnAttributeField.class, DynamicAttributeReporterReference.class, OntologyAttribute.class
  });

  private AttributeMetaQueryHandler() {}

  public static List<Map<String, Object>> getDynamicallyDefinedAttributes(String queryName, WdkModel wdkModel)
      throws WdkModelException {
    try {
      return CacheMgr.get().getAttributeMetaQueryCache().getValue(queryName,
          new DynamicallyDefinedAttributeFetcher(wdkModel));
    }
    catch (Exception e) {
      if (e instanceof UnfetchableInstanceException) {
        e = (Exception)e.getCause();
      }
      throw (e instanceof WdkModelException ? (WdkModelException)e :
        new WdkModelException("Could not fetch dynamically defined attributes " +
            "for query '" + queryName + "'", e));
    }
  }

  private static class DynamicallyDefinedAttributeFetcher implements ValueFactory<String, List<Map<String,Object>>> {

    private static final List<FieldSetter> FIELDS = reduce(
        mapToList(CLIENT_CLASSES, clazz -> getRngFields(clazz)),
        (previous, next) -> previous.addAll(next),
        new ListBuilder<FieldSetter>()).toList();

    private final WdkModel _wdkModel;

    public DynamicallyDefinedAttributeFetcher(WdkModel wdkModel) {
      _wdkModel = wdkModel;
    }

    @Override
    public List<Map<String,Object>> getNewValue(String queryName) throws ValueProductionException {
      try {
        SqlQuery query = (SqlQuery) _wdkModel.resolveReference(queryName);
        final List<Map<String, Object>> dynamicAttrs = new ArrayList<>();
        new SQLRunner(_wdkModel.getAppDb().getDataSource(), query.getSql(),
            query.getFullName() + "__dyn-cols").executeQuery(rs -> {
              List<String> columnNames = getColumnNames(rs.getMetaData());
              while (rs.next()) {
                dynamicAttrs.add(processAttributeRow(rs, columnNames));
              }
            });
        return dynamicAttrs;
      }
      catch (SQLRunnerException | WdkModelException e) {
        throw new ValueProductionException((Exception)e.getCause());
      }
    }

    private static List<String> getColumnNames(ResultSetMetaData metaData) throws SQLException {
      // Compile a list of database column names - the list will likely be different for
      //   every attribute meta query table.
      int columnCount = metaData.getColumnCount();
      List<String> columnNames = new ArrayList<>();
      for (int i = 1; i <= columnCount; i++ ) {
        String columnName = metaData.getColumnName(i).toLowerCase();
        columnNames.add(columnName);
      }
      return columnNames;
    }

    /**
     * Reads a row from the attribute meta query.  Each row represents an attribute, but has fields for all
     * the setters in the FIELDS constant above.
     * 
     * @param resultSet
     * @param availableColumnNames
     * @return
     * @throws SQLException
     */
    private static Map<String, Object> processAttributeRow(ResultSet resultSet, List<String> availableColumnNames)
        throws SQLException {
      Map<String, Object> row = new HashMap<>();
      Map<String, String> foundFieldTypes = new HashMap<>();
      for (FieldSetter fieldSetter : FIELDS) {
        String neededColumnName = fieldSetter.getUnderscoredName();
        String neededColumnType = fieldSetter.getMethod().getParameterTypes()[0].getName();

        // If the fieldSetter's database column name matches one available via the attribute meta query,
        // retrieve the value for the column and invoke its setter. If the value is null but the
        // fieldSetter data indicates that that field is required for the object, throw an exception.
        // Note that only string, boolean and ints are presently supported.
        if (!availableColumnNames.contains(neededColumnName)) {
          // If the fieldSetter indicates that the field is required but no corresponding column
          // is found in the attribute meta query, throw an exception.
          if (fieldSetter.isRngRequired()) {
            throw getRowProcessingException("The field " + neededColumnName + " is required for dynamic " +
                "attributes by the " + fieldSetter.getMethod().getDeclaringClass().getName() + " class");
          }
          // else ok to skip
          continue;
        }

        // Check to see if row already contains this value; this will happen if >1 client class requires
        // the same column.  We need to make sure all setters of this column require the same type.
        if (row.containsKey(neededColumnName)) {
          if (!foundFieldTypes.get(neededColumnName).equals(neededColumnType)) {
            throw getRowProcessingException("Two RNG fields with the same name (but in different clases) " +
                "have different types.");
          }
          else {
            // no need to load field again
            continue;
          }
        }

        // get the value of this field out of the result set, convert to value we know about, and add to row
        Object value = null;
        switch (neededColumnType) {
          case "java.lang.String":
            value = resultSet.getString(neededColumnName);
            break;
          case "int":
          case "java.lang.Integer":
            value = resultSet.getInt(neededColumnName);
            break;
          case "boolean":
          case "java.lang.Boolean":
            value = resultSet.getBoolean(neededColumnName);
            break;
          default:
            throw getRowProcessingException("The parameter type " + neededColumnType + " is not yet handled.");
        }
        if (resultSet.wasNull()) {
          value = null;
          if (fieldSetter.isRngRequired()) {
            throw getRowProcessingException("The field " + neededColumnName + " is required for the " +
                fieldSetter.getMethod().getDeclaringClass().getName() + " object");
          }
        }
        // add value to row
        row.put(neededColumnName, value);
        // save off this field's type so we can detect conflicting types across multiple classes
        foundFieldTypes.put(neededColumnName, neededColumnType);
      }
      return row;
    }

    private static SQLRunnerException getRowProcessingException(String message) {
      return new SQLRunnerException("Could not process row", new WdkModelException(message));
    }
  }

  public static <T> T populate(T object, Map<String, Object> row) throws WdkModelException {
    try {
      Class<?> objClass = object.getClass();
      if (!CLIENT_CLASSES.contains(objClass)) {
        throw new WdkModelException("Class " + objClass.getName() + " is not a supported attribute meta query consumer.");
      }
      // get field setters to populate
      List<FieldSetter> fieldSetters = RngAnnotations.getRngFields(objClass);
  
      // Iterate over all the possible fields for the given object
      for (FieldSetter fieldSetter : fieldSetters) {
        String fieldColumnName = fieldSetter.getUnderscoredName();
  
        // If the fieldSetter's database column name matches one available via the attribute meta query,
        // retrieve the value for the column and invoke its setter. If the value is null but the
        // fieldSetter data indicates that that field is required for the object, throw an exception.
        // Note that only string, boolean and ints are presently supported.
        if (!row.keySet().contains(fieldColumnName) || row.get(fieldColumnName) == null) {
          // If the fieldSetter indicates that the field is required but no corresponding column
          // is found in the attribute meta query, throw an exception.
          if (fieldSetter.isRngRequired()) {
            throw new WdkModelException("The field " + fieldColumnName + " is required for the " +
                objClass.getName() + " object");
          }
          // else ok to skip
          continue;
        }
  
        // row contains this field setter's value; use to populate the passed object
        fieldSetter.getMethod().invoke(object, row.get(fieldColumnName));
      }

      // return the populated object
      return object;
    }
    catch (InvocationTargetException | IllegalAccessException e) {
      throw new WdkModelException("Unable to invoke RNG-annotated method", e);
    }
  }
}
