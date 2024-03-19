package org.gusdb.wdk.model.fix.table.edaanalysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRow;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Stores values of a database row in the EDA analysis table
 */
public class AnalysisRow implements TableRow {

  // basic analysis information
  private String _analysisId;
  private String _datasetId;
  private JSONObject _descriptor;

  // bookkeeping; update these if values change in descriptor
  private int _numFilters;
  private int _numComputations;
  private int _numVisualizations;

  public AnalysisRow(ResultSet rs, DBPlatform platform) throws SQLException {
    _analysisId = rs.getString("analysis_id");
    _datasetId = rs.getString("study_id");
    _descriptor = new JSONObject(platform.getClobData(rs, "analysis_descriptor"));
    _numFilters = rs.getInt("num_filters");
    _numComputations = rs.getInt("num_computations");
    _numVisualizations = rs.getInt("num_visualizations");
  }

  public Object[] toOrderedValues() {
    return new Object[] {
        _datasetId, _descriptor.toString(), _numFilters, _numComputations, _numVisualizations, _analysisId
    };
  }

  @Override
  public String getDisplayId() {
    return _analysisId;
  }

  public String getDatasetId() {
    return _datasetId;
  }

  /**
   * Returns the current descriptor for this analysis.  Note this object can be
   * edited in-place before returning the row result; however if the number of
   * filters, computations, or visualizations is modified, you must also call
   * <code>refreshStats()</code> to refresh those counts or they will be inaccurate
   * in the database.
   *
   * @return descriptor for this analysis
   */
  public JSONObject getDescriptor() {
    return _descriptor;
  }

  /**
   * Sets a new descriptor and refreshes the stats (number of filters, computations,
   * and visualizations) on this analysis.
   *
   * @param descriptor new descriptor
   */
  public void setDescriptor(JSONObject descriptor) {
    _descriptor = descriptor;
    refreshStats();
  }

  public void refreshStats() {
    _numFilters = _descriptor.getJSONObject("subset").getJSONArray("descriptor").length();
    JSONArray computations = Optional.ofNullable(_descriptor.optJSONArray("computations")).orElse(new JSONArray());
    _numComputations = computations.length();
    _numVisualizations = 0;
    for (int i = 0; i < _numComputations; i++) {
      _numVisualizations += computations.getJSONObject(i).getJSONArray("visualizations").length();
    }
  }
}
