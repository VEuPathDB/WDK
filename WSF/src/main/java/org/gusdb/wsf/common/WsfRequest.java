package org.gusdb.wsf.common;

import java.util.Map;

public interface WsfRequest {

  String PARAM_REQUEST = "request";

  /**
   * @return the projectId
   */
  String getProjectId();

  /**
   * @return the params
   */
  Map<String, String> getParams();

  /**
   * @return the orderedColumns
   */
  String[] getOrderedColumns();

  /**
   * @return a map of ordered columns, where the key is the column name, and the
   *   value is the zero-based order of that column.
   */
  Map<String, Integer> getColumnMap();

  /**
   * The context can be used to hold additional information, such as user id,
   * calling query name, etc, which can be used by plugins.
   *
   * @return the context
   */
  Map<String, String> getContext();

}
