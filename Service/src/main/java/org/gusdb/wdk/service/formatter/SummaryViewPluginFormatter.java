package org.gusdb.wdk.service.formatter;

import java.util.Collection;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.answer.SummaryView;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK Summary View Plugins.  Summary View Plugin JSON will have the following form:
 * {
 *   name: String,
 *   display: String,
 *   description: String
 * }
 * 
 * @author jlong
 */
public class SummaryViewPluginFormatter {

  public static JSONArray getSummaryViewPluginsJson(Collection<SummaryView> summaryViews) {
    JSONArray summaryViewPluginsJson = new JSONArray();
    for (SummaryView summaryView : summaryViews) {
      summaryViewPluginsJson.put(getSummaryViewPluginJson(summaryView));
    }
    return summaryViewPluginsJson;
  }

  public static JSONObject getSummaryViewPluginJson(SummaryView summaryView) {
    return new JSONObject()
      .put(JsonKeys.NAME, summaryView.getName())
      .put(JsonKeys.DISPLAY_NAME, summaryView.getDisplay())
      .put(JsonKeys.DESCRIPTION, summaryView.getDescription());
  }
}
