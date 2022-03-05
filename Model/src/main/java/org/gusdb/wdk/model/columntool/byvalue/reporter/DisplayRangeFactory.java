package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.util.function.BiFunction;

import javax.sql.DataSource;

import org.gusdb.fgputil.Range;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.functional.FunctionalInterfaces.BiFunctionWithException;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONObject;

public class DisplayRangeFactory {

  public static final String PROP_DISPLAY_RANGE_MIN = "displayRangeMin";
  public static final String PROP_DISPLAY_RANGE_MAX = "displayRangeMax";

  private final DataSource _appDb;
  private final String _attributeFieldName;
  private final String _jointAttributeSql;

  public DisplayRangeFactory(
      DataSource appDb,
      String attributeFieldName,
      String jointAttributeSql) {
    _appDb = appDb;
    _attributeFieldName = attributeFieldName;
    _jointAttributeSql = jointAttributeSql;
  }

  public <T extends Comparable<T>> Range<T> calculateDisplayRange(JSONObject config,
      BiFunction<JSONObject,String,T> getBoundaryFromJson,
      BiFunctionWithException<ResultSet,String,T> getBoundaryFromResultSet
  ) throws ReporterConfigException {

    T displayRangeMin = getBoundaryFromJson.apply(config, PROP_DISPLAY_RANGE_MIN);
    T displayRangeMax = getBoundaryFromJson.apply(config, PROP_DISPLAY_RANGE_MAX);

    if (displayRangeMin != null && displayRangeMax != null && displayRangeMin.compareTo(displayRangeMax) >= 0) {
      throw new ReporterConfigException(PROP_DISPLAY_RANGE_MAX + " must be greater than " + PROP_DISPLAY_RANGE_MIN);
    }

    // if range min or max not specified, then find them
    if (displayRangeMin == null || displayRangeMax == null) {

      // SQL to find min and max of the data range
      String sql = "select " +
          "min(" + _attributeFieldName + ") as min, " +
          "max(" + _attributeFieldName + ") as max " +
          "from (" + _jointAttributeSql + ")";

      // fill tuple with min/max
      Range<T> dataRange = new SQLRunner(_appDb, sql).executeQuery(rs -> {
        return rs.next()
            ? Functions.swallowAndGet(() -> new Range<T>(
                getBoundaryFromResultSet.apply(rs,"min"),
                getBoundaryFromResultSet.apply(rs,"max")))
            : Functions.doThrow(() -> new WdkRuntimeException(
                "Could not find data range for result (no rows)"));
      });

      // assign data range bounds as needed
      if (displayRangeMin == null || displayRangeMin.compareTo(dataRange.getEnd()) >= 0) {
        displayRangeMin = dataRange.getBegin();
      }
      if (displayRangeMax == null || displayRangeMax.compareTo(dataRange.getBegin()) <= 0) {
        displayRangeMax = dataRange.getEnd();
      }
    }

    return new Range<T>(displayRangeMin, displayRangeMax);
  }
}
