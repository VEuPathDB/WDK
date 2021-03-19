package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class UserPreferences {

  // represents the maximum number of sorts we apply to an answer
  public static final int MAX_NUM_SORTING_COLUMNS = 3;

  public final static String PREF_ITEMS_PER_PAGE = "preference_global_items_per_page";

  public final static String SORTING_ATTRIBUTES_SUFFIX = "_sort";
  public final static String SUMMARY_ATTRIBUTES_SUFFIX = "_summary";

  public final static String SUMMARY_VIEW_PREFIX = "summary_view_";
  public final static String RECORD_VIEW_PREFIX = "record_view_";

  public final static String DEFAULT_SUMMARY_VIEW_PREF_SUFFIX = "";

  /**
   * the preferences for the user: <prefName, prefValue>. It only contains the preferences for the current
   * project
   */
  private final Map<String, String> _globalPreferences;
  private final Map<String, String> _projectPreferences;

  public UserPreferences() {
    _globalPreferences = new LinkedHashMap<String, String>();
    _projectPreferences = new LinkedHashMap<String, String>();
  }

  public void setProjectPreference(String prefName, String prefValue) {
    if (prefValue == null)
      prefValue = prefName; // acting as a flag
    _projectPreferences.put(prefName, prefValue);
  }

  public String getProjectPreference(String key) {
    return _projectPreferences.get(key);
  }

  public void unsetProjectPreference(String prefName) {
    _projectPreferences.remove(prefName);
  }

  public void setProjectPreferences(Map<String, String> projectPreferences) {
    assignPrefs(projectPreferences, _projectPreferences);
  }

  public Map<String, String> getProjectPreferences() {
    return new LinkedHashMap<String, String>(_projectPreferences);
  }

  public void clearProjectPreferences() {
    _projectPreferences.clear();
  }

  public void setGlobalPreference(String prefName, String prefValue) {
    if (prefValue == null)
      prefValue = prefName; // acting as a flag
    _globalPreferences.put(prefName, prefValue);
  }

  public String getGlobalPreference(String key) {
    return _globalPreferences.get(key);
  }

  public void unsetGlobalPreference(String prefName) {
    _globalPreferences.remove(prefName);
  }

  public void setGlobalPreferences(Map<String, String> globalPreferences) {
    assignPrefs(globalPreferences, _globalPreferences);
  }

  public Map<String, String> getGlobalPreferences() {
    return new LinkedHashMap<String, String>(_globalPreferences);
  }

  public void clearGlobalPreferences() {
    _globalPreferences.clear();
  }

  public void clearPreferences() {
    _globalPreferences.clear();
    _projectPreferences.clear();
  }

  private static void assignPrefs(Map<String,String> from, Map<String,String> to) {
    to.clear();
    for (Entry<String,String> pref : from.entrySet()) {
      to.put(pref.getKey(), pref.getValue());
    }
  }
}
