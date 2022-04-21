package org.gusdb.wsf.common;

import java.util.Map;

public interface WsfRequest {

  String PARAM_REQUEST = "request";
  String REMOTE_EXECUTE_TIMEOUT_ISO_8601_CONTEXT_KEY = "timeout_iso_8601";

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
